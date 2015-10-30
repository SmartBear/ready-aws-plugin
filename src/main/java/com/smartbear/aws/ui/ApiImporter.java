package com.smartbear.aws.ui;

import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRepresentation;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.RestServiceFactory;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.support.UISupport;
import com.eviware.x.dialogs.Worker;
import com.eviware.x.dialogs.XProgressDialog;
import com.eviware.x.dialogs.XProgressMonitor;
import com.smartbear.aws.ApplicationException;
import com.smartbear.aws.AwsIamAuthRequestFilter;
import com.smartbear.aws.Strings;
import com.smartbear.aws.amazon.ApiReader;
import com.smartbear.aws.entity.Api;
import com.smartbear.aws.entity.ApiDescription;
import com.smartbear.aws.entity.ApiKey;
import com.smartbear.aws.entity.HttpMethod;
import com.smartbear.aws.entity.HttpResource;
import com.smartbear.aws.entity.MethodParameter;
import com.smartbear.aws.entity.MethodResponse;
import com.smartbear.rapisupport.RapiLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ApiImporter implements Worker {
    private Boolean canceled = false;
    private final XProgressDialog waitDialog;
    private final ApiReader reader;
    private final List<ApiDescription> apis;
    private final WsdlProject project;
    private final List<RestService> addedServices = new ArrayList<>();
    private final StringBuilder errors = new StringBuilder();

    public ApiImporter(XProgressDialog waitDialog, ApiReader reader, List<ApiDescription> apis, WsdlProject project) {
        this.waitDialog = waitDialog;
        this.reader = reader;
        this.apis = apis;
        this.project = project;
    }

    public static List<RestService> importServices(ApiReader reader, List<ApiDescription> apis, WsdlProject project) {
        KeySelectorDialog.showIfNeeded(apis);

        XProgressDialog dlg = UISupport.getDialogs().createProgressDialog(Strings.ApiImporter.IMPORT_PROGRESS, 100, "", true);
        ApiImporter worker = new ApiImporter(dlg, reader, apis, project);
        try {
            worker.waitDialog.run(worker);
        } catch (Exception ex) {
            UISupport.showErrorMessage(ex.getMessage());
            RapiLogger.logError(ex);
        }
        return worker.addedServices;
    }


    @Override
    public Object construct(XProgressMonitor xProgressMonitor) {
        for (ApiDescription description: apis) {
            try {
                Api api = reader.getApi(description);
                RestService service = build(api, project, reader.getRegion());
                addedServices.add(service);
            } catch (ApplicationException ex) {
                RapiLogger.logError(ex);
                errors.append(String.format(Strings.ApiImporter.IMPORT_ERROR, description.name, ex.getMessage()));
            }
        }

        if (errors.length() > 0) {
            errors.append(Strings.ApiImporter.IMPORT_ERROR_TAIL);
        }

        return null;
    }

    @Override
    public void finished() {
        if (canceled) {
            return;
        }
        waitDialog.setVisible(false);
        if (errors.length() > 0) {
            UISupport.showErrorMessage(errors.toString());
        }
    }

    @Override
    public boolean onCancel() {
        canceled = true;
        waitDialog.setVisible(false);
        return true;
    }

    private RestService build(Api api, WsdlProject wsdlProject, String region) {
        try {
            RestService restService = (RestService)wsdlProject.addNewInterface(api.name, RestServiceFactory.REST_TYPE);
            restService.setDescription(api.description);
            restService.setBasePath("");
            String endPoint = String.format("https://%s.execute-api.%s.amazonaws.com", api.id, region);
            restService.addEndpoint(endPoint);

            RestResource root = api.stage == null ?
                    restService.addNewResource("root", ""):
                    restService.addNewResource(api.stage.name, api.stage.name);
            root.setDescription(api.stage == null ? "" : api.stage.description);
            addResources(root, api.rootResource);
            //add ApiKey header only to the root resources, all other inherit its
            addApiKeyHeader(root, api);
            addCredentialProperty(api);
            return restService;
        } catch (Exception e) {
            return null;
        }
    }

    private static void addApiKeyHeader(RestResource root, Api api) {
        if (api.apiKey == null) {
            return;
        }
        String customPropertyName = "aws-api-key-" + api.name.replaceAll("\\s", "-");
        WsdlProject project = root.getProject();
        if (!project.hasProperty(customPropertyName)) {
            project.addProperty(customPropertyName);
        }

        ApiKey key = api.apiKey;

        project.getProperty(customPropertyName).setValue(key.id);

        RestParamProperty param = root.addProperty("x-api-key");
        String keyValue = String.format("${#Project#%s}", customPropertyName);
        param.setValue(keyValue);
        param.setDefaultValue(keyValue);
        param.setStyle(RestParamsPropertyHolder.ParameterStyle.HEADER);
    }

    private void addResources(RestResource target, HttpResource source) {
        for (HttpResource child: source.resources) {
            RestResource restResource = target.addNewChildResource(child.name, child.path);

            for (String resourceParam: child.params) {
                RestParamProperty prop = restResource.addProperty(resourceParam);
                prop.setStyle(RestParamsPropertyHolder.ParameterStyle.TEMPLATE);
                prop.setRequired(true);
            }

            for (HttpMethod method: child.methods) {
                RestMethod restMethod = restResource.addNewMethod(method.name);
                restMethod.setMethod(method.httpMethod);
                //add parameters
                for (MethodParameter param: method.parameters) {
                    if (param.style == RestParamsPropertyHolder.ParameterStyle.TEMPLATE) {
                        continue;
                    }

                    RestParamProperty prop = restMethod.addProperty(param.name);
                    prop.setStyle(param.style);
                    prop.setRequired(param.isRequired);
                }

                //add responses
                for (MethodResponse resp: method.responses) {
                    for (String mediaType: resp.mediaTypes) {
                        RestRepresentation methodResponse = restMethod.addNewRepresentation(RestRepresentation.Type.RESPONSE);
                        methodResponse.setMediaType(mediaType);
                        methodResponse.setStatus(Arrays.asList(new String[] {resp.statusCode}));
                    }
                }

                //add AWS_IAM authorization headers
                if ("AWS_IAM".equalsIgnoreCase(method.getAuthorizationType())) {
                    RestParamProperty dateHeader = restMethod.addProperty(AwsIamAuthRequestFilter.DATE_HEADER);
                    dateHeader.setStyle(RestParamsPropertyHolder.ParameterStyle.HEADER);
                    dateHeader.setRequired(false);
                    RestParamProperty authHeader = restMethod.addProperty(AwsIamAuthRequestFilter.AUTH_HEADER);
                    authHeader.setStyle(RestParamsPropertyHolder.ParameterStyle.HEADER);
                    authHeader.setRequired(true);
                }
                restMethod.addNewRequest("Request 1");
            }
            addResources(restResource, child);
        }
    }

    private void addCredentialProperty(Api api) {
        if (!isRequiredAuthorization(api.rootResource)) {
            return;
        }

        String customPropertyName = String.format(AwsIamAuthRequestFilter.CREDENTIAL_PROPERTY_TMPL, api.name.replaceAll("\\s", "-"));
        if (!project.hasProperty(customPropertyName)) {
            project.addProperty(customPropertyName);
        }

        project.getProperty(customPropertyName).setValue(reader.credential);
    }

    private boolean isRequiredAuthorization(HttpResource source) {
        for (HttpMethod method: source.methods) {
            if ("AWS_IAM".equalsIgnoreCase(method.getAuthorizationType())) {
                return true;
            }
        }
        for (HttpResource child: source.resources) {
            if (isRequiredAuthorization(child)) {
                return true;
            }
        }
        return false;
    }
}
