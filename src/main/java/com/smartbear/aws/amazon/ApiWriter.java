package com.smartbear.aws.amazon;

import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.support.StringUtils;
import com.smartbear.aws.ApplicationException;
import com.smartbear.aws.DeploymentSetting;
import com.smartbear.aws.Strings;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public final class ApiWriter extends ApiOperationBase {
    private final static String RESOURCE_PATH_TMPL = RESOURCES_PATH_TMPL + "/%s";

    private final DeploymentSetting deploymentSetting;

    public ApiWriter(String accessKey, String secretKey, String region, DeploymentSetting deploymentSetting) {
        super(accessKey, secretKey, region);
        this.deploymentSetting = deploymentSetting;
    }

    public void perform(RestService restService) throws ApplicationException {
        JsonObject api = createApi(restService.getName(), restService.getDescription());
        String apiId = api.getString("id", "");
        if (StringUtils.isNullOrEmpty(apiId))
            throw new ApplicationException(String.format(Strings.Error.UNEXPECTED_RESPONSE_FORMAT, "request 'restapi:create'"));

        String rootResourceId = getRootResourceId(apiId);
        RestResourceTreeNode root = new RestResourceTreeNode(restService);

        for (RestResourceTreeNode node: root.children) {
            createResource(apiId, rootResourceId, node);
        }
    }

    public JsonObject createApi(String name, String description) throws ApplicationException {
        JsonObjectBuilder body = Json.createObjectBuilder();
        body.add("name", name);
        if (StringUtils.hasContent(description)) {
            body.add("description", description);
        }

        return requestExecutor.perform("POST", APIS_PATH, "", body.build().toString());
    }

    public void createResource(String apiId, String parentId, RestResourceTreeNode node) throws ApplicationException {
        JsonObjectBuilder body = Json.createObjectBuilder();
        body.add("pathPart", node.value);
        JsonObject result = requestExecutor.perform("POST", String.format(RESOURCE_PATH_TMPL, apiId, parentId), "", body.build().toString());
        String newResourceId = result.getString("id", "");
        if (StringUtils.isNullOrEmpty(newResourceId)) {
            throw new ApplicationException(String.format(Strings.Error.UNEXPECTED_RESPONSE_FORMAT, "request 'resource:create'"));
        }

        if (node.resource != null) {
            createMethods(apiId, newResourceId, node.resource);
        }
        for (RestResourceTreeNode child: node.children) {
            createResource(apiId, newResourceId, child);
        }
    }

    public void createMethods(String apiId, String resourceId, RestResource restResource) throws ApplicationException {
        MethodWriter methodWriter = new MethodWriter(requestExecutor, deploymentSetting, apiId, resourceId);

        for (RestMethod method: restResource.getRestMethodList()) {
            methodWriter.perform(method);
        }
    }

    public String getRootResourceId(String apiId) throws ApplicationException {
        final String path = String.format(RESOURCES_PATH_TMPL, apiId);
        JsonObject json = requestExecutor.perform("GET", path, "");
        JsonArray items = extractItemsFromResponse(json);
        if (items == null) {
            throw new ApplicationException(String.format(Strings.Error.UNEXPECTED_RESPONSE_FORMAT, "API resources"));
        }
        //by default an API is created with root "/" resource
        if (items.size() != 1) {
            throw new ApplicationException(String.format(Strings.Error.UNEXPECTED_RESPONSE_FORMAT, "API resources (empty list)"));
        }
        return items.getJsonObject(0).getString("id", "");
    }
}
