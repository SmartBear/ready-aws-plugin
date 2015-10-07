package com.smartbear.aws.amazon;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRepresentation;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.support.StringUtils;
import com.smartbear.aws.ApplicationException;
import com.smartbear.aws.DeploymentSetting;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public final class MethodWriter {
    private final static String METHOD_PATH = "/restapis/%s/resources/%s/methods/%s";
    private final static String RESPONSE_PATH = METHOD_PATH + "/responses/%s";
    private final static String INTEGRATION_PATH = METHOD_PATH + "/integration";
    private final static String INTEGRATION_RESPONSES_PATH = INTEGRATION_PATH + "/responses/%s";

    private final HttpRequestExecutor requestExecutor;
    private final DeploymentSetting deploymentSetting;
    private final String apiId;
    private final String resourceId;
    private final boolean logRequestResult = false;

    public MethodWriter(HttpRequestExecutor requestExecutor, DeploymentSetting deploymentSetting, String apiId, String resourceId) {
        this.requestExecutor = requestExecutor;
        this.deploymentSetting = deploymentSetting;
        this.apiId = apiId;
        this.resourceId = resourceId;
    }

    public void perform(RestMethod method) throws ApplicationException {
        List<RestParamProperty> resourceParams = getSuitableParams(method.getResource().getParams());
        List<RestParamProperty> methodParams = getSuitableParams(method.getParams());
        methodParams.addAll(resourceParams);

        Set<String> responses = getResponseStatuses(method);

        //create method
        JsonObjectBuilder body = Json.createObjectBuilder();
        body.add("authorizationType", "NONE");
        body.add("apiKeyRequired", "false");
        body.add("requestParameters", buildMethodParamsJson(methodParams));
        performRequest(String.format(METHOD_PATH, apiId, resourceId, method.getMethod()), body.build().toString(), "Method");

        //create method responses
        for (String response: responses) {
            String path = String.format(RESPONSE_PATH, apiId, resourceId, method.getMethod(), response);
            performRequest(path, "{\"responseParameters\": {}, \"responseModels\": {}}", "Method response");
        }

        //create integration
        if (deploymentSetting.proxyIntegration) {
            String path = String.format(INTEGRATION_PATH, apiId, resourceId, method.getMethod());
            JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
            jsonBuilder.add("type", "HTTP");
            jsonBuilder.add("httpMethod", method.getMethod().toString());
            jsonBuilder.add("uri", deploymentSetting.endPoint + method.getOperation().getFullPath());
            jsonBuilder.add("requestParameters", buildIntegrationParamsJson(methodParams));
            performRequest(path, jsonBuilder.build().toString(), "Integration");

            for (String response: responses) {
                String respPath = String.format(INTEGRATION_RESPONSES_PATH, apiId, resourceId, method.getMethod(), response);
                performRequest(respPath, String.format("{\"selectionPattern\": \"%s\"}", response), "Integration response");
            }
        }
    }

    private void performRequest(String path, String body, String logTitle) throws ApplicationException {
        JsonObject result = requestExecutor.perform("PUT", path, "", body);
        if (logRequestResult) {
            SoapUI.log(String.format("%s = %s", logTitle, result));
        }
    }

    private JsonObject buildMethodParamsJson(List<RestParamProperty> params) throws ApplicationException {
        JsonObjectBuilder paramsJson = Json.createObjectBuilder();
        for (RestParamProperty param: params) {
            paramsJson.add(awsParamName(param, "method"), param.getRequired());
        }
        return paramsJson.build();
    }

    private JsonObject buildIntegrationParamsJson(List<RestParamProperty> params) throws ApplicationException {
        JsonObjectBuilder paramsJson = Json.createObjectBuilder();
        for (RestParamProperty param: params) {
            paramsJson.add(awsParamName(param, "integration"), awsParamName(param, "method"));
        }
        return paramsJson.build();
    }

    private Set<String> getResponseStatuses(RestMethod method) {
        Set<String> statuses = new HashSet<>();
        for (RestRepresentation repr: method.getRepresentations()) {
            if (repr.getType() == RestRepresentation.Type.RESPONSE) {
                for (Object item : repr.getStatus()) {
                    String status = item.toString();
                    if (StringUtils.hasContent(status)) {
                        statuses.add(status);
                    }
                }
            }
        }
        if (deploymentSetting.proxyIntegration && statuses.isEmpty()) {
            statuses.add(deploymentSetting.defaultResponse);
        }
        return statuses;
    }


    private List<RestParamProperty> getSuitableParams(RestParamsPropertyHolder properties) {
        List<RestParamProperty> result = new LinkedList<>();
        for (String paramName: properties.keySet()) {
            RestParamProperty param = properties.getProperty(paramName);
            switch (param.getStyle()) {
                case QUERY:
                case HEADER:
                case TEMPLATE:
                    result.add(param);
            }

        }
        return result;
    }

    private String awsParamName(RestParamProperty param, String prefix) throws ApplicationException {
        switch (param.getStyle()) {
            case QUERY:     return String.format("%s.request.querystring.%s", prefix, param.getName());
            case HEADER:    return String.format("%s.request.header.%s", prefix, param.getName());
            case TEMPLATE:  return String.format("%s.request.path.%s", prefix, param.getName());
        }
        //TODO: error message
        throw new ApplicationException("", null);
    }
}
