package com.smartbear.aws.amazon;

import com.eviware.soapui.support.StringUtils;
import com.smartbear.aws.ApplicationException;
import com.smartbear.aws.Helper;
import com.smartbear.aws.Strings;
import com.smartbear.aws.entity.Api;
import com.smartbear.aws.entity.ApiDescription;
import com.smartbear.aws.entity.ApiKey;
import com.smartbear.aws.entity.HttpMethod;
import com.smartbear.aws.entity.HttpResource;
import com.smartbear.aws.entity.HttpResourceDescription;
import com.smartbear.aws.entity.ResponseValidator;
import com.smartbear.aws.entity.Stage;
import com.smartbear.aws.entity.StageMethod;
import com.smartbear.rapisupport.RapiLogger;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public final class ApiReader extends ApiOperationBase {
    private final static String STAGES_PATH_TMPL = "/restapis/%s/stages";
    private final static String METHOD_PATH_TMPL = "/restapis/%s/resources/%s/methods/%s";

    private final String region;
    public final String credential;

    public ApiReader(String accessKey, String secretKey, String region) {
        super(accessKey, secretKey, region);
        this.region = region;
        this.credential = String.format("%s###%s###%s", accessKey, secretKey, region);
    }

    public String getRegion() {
        return this.region;
    }

    public boolean checkConnection() throws ApplicationException {
        JsonObject json = requestExecutor.perform("GET", "/", "");
        if (!json.containsKey("_links")) {
            throw new ApplicationException(String.format(Strings.Error.UNEXPECTED_RESPONSE_FORMAT, "Check connection"));
        }
        return true;
    }

    public List<ApiDescription> getApis() throws ApplicationException {
        JsonObject json = requestExecutor.perform("GET", APIS_PATH, "");
        JsonArray items = extractItemsFromResponse(json);
        if (items == null) {
            throw new ApplicationException(String.format(Strings.Error.UNEXPECTED_RESPONSE_FORMAT, "API list"));
        }
        final List<ApiKey> apiKeys = getApiKeys();
        List<ApiDescription> apis = Helper.extractList(items, new Helper.EntityFactory<ApiDescription>() {
            @Override
            public ApiDescription create(JsonObject value) {
                //TODO: dirty solution
                String apiId = value.getString("id", "");
                List<Stage> stages;
                try {
                    stages = readStages(apiId);
                } catch (ApplicationException ex) {
                    RapiLogger.logError(ex);
                    stages = Collections.emptyList();
                }

                return new ApiDescription(value, stages, apiKeys);
            }
        });

        return Collections.unmodifiableList(apis);
    }

    public Api getApi(final ApiDescription description) throws ApplicationException {
        List<HttpResourceDescription> resources = readResourceDescriptions(description.id);

        List<HttpResource> httpResources = readResources(description.id, resources);
        HttpResource treeRoot = buildResourcesTree(httpResources);
        //TODO: check treeRoot on null and trow exception
        if (description.getStage() != null) {
            List<StageMethod> apiSummary = readApiSummary(description.id, description.getStage().deploymentId);
            StageTreeBuilder builder = new StageTreeBuilder(treeRoot, apiSummary);
            treeRoot = builder.execute();
        }

        return new Api(description, treeRoot);
    }

    public List<ApiKey> getApiKeys() throws ApplicationException {
        JsonObject res = requestExecutor.perform("GET", "/apikeys", "");
        JsonArray items = extractItemsFromResponse(res);
        if (items == null) {
            throw new ApplicationException(String.format(Strings.Error.UNEXPECTED_RESPONSE_FORMAT, "API keys"));
        }
        return Helper.extractList(items, new Helper.EntityFactory<ApiKey>() {
            @Override
            public ApiKey create(JsonObject value) {
                return new ApiKey(value);
            }
        });
    }

    private List<Stage> readStages(String apiId) throws ApplicationException {
        final String path = String.format(STAGES_PATH_TMPL, apiId);
        JsonObject json = requestExecutor.perform("GET", path, "");
        JsonArray items = extractItemsFromResponse(json);
        if (items == null) {
            throw new ApplicationException(String.format(Strings.Error.UNEXPECTED_RESPONSE_FORMAT, "API stages"));
        }

        return Helper.extractList(items, new Helper.EntityFactory<Stage>() {
            @Override
            public Stage create(JsonObject value) {
                return new Stage(value);
            }
        });
    }

    private List<StageMethod> readApiSummary(String apiId, String deploymentId) throws ApplicationException {
        JsonObject deployment = requestExecutor.perform("GET", String.format("/restapis/%s/deployments/%s", apiId, deploymentId), "embed=apisummary");
        List<StageMethod> result = new LinkedList<>();
        JsonValue value = deployment.get("apiSummary");
        if (value instanceof JsonObject) {
            JsonObject apiSummary = (JsonObject)value;
            for (String path: apiSummary.keySet()) {
                JsonObject resource = apiSummary.getJsonObject(path);
                for (String methodName: resource.keySet()) {
                    JsonObject method = resource.getJsonObject(methodName);
                    ResponseValidator.checkDeploymentMethod(method);
                    boolean apiKeyRequired = method.getBoolean("apiKeyRequired");
                    String authorizationType = method.getString("authorizationType", "NONE");
                    result.add(new StageMethod(methodName, path, apiKeyRequired, authorizationType));
                }
            }
        }
        return result;
    }

    private List<HttpResourceDescription> readResourceDescriptions(String apiId) throws ApplicationException {
        final String path = String.format(RESOURCES_PATH_TMPL, apiId);
        JsonObject json = requestExecutor.perform("GET", path, "");

        JsonArray items = extractItemsFromResponse(json);
        if (items == null) {
            throw new ApplicationException(String.format(Strings.Error.UNEXPECTED_RESPONSE_FORMAT, "API resources"));
        }
        return Helper.extractList(items, new Helper.EntityFactory<HttpResourceDescription>() {
            @Override
            public HttpResourceDescription create(JsonObject value) {
                return new HttpResourceDescription(value);
            }
        });
    }

    private List<HttpResource> readResources(String apiId, List<HttpResourceDescription> descriptions) throws ApplicationException {
        List<HttpResource> httpResources = new LinkedList<>();
        for (HttpResourceDescription description: descriptions) {
            List<HttpMethod> methods = readResourceMethods(apiId, description);
            httpResources.add(new HttpResource(description, methods));
        }
        return httpResources;
    }

    private List<HttpMethod> readResourceMethods(String apiId, HttpResourceDescription resource) throws ApplicationException {
        List<HttpMethod> methods = new LinkedList<>();
        for (String name: resource.methodsNames) {
            String methodPath = String.format(METHOD_PATH_TMPL, apiId, resource.id, name);
            JsonObject methodJson = requestExecutor.perform("GET", methodPath, "");
            HttpMethod method = new HttpMethod(methodJson);
            methods.add(method);
        }
        return methods;
    }

    private HttpResource buildResourcesTree(List<HttpResource> httpResources) {
        HttpResource root = null;
        for (HttpResource item: httpResources) {
            String parentId = item.parentId;
            if (StringUtils.isNullOrEmpty(parentId)) {
                root = item;
                continue;
            }
            for (HttpResource parent: httpResources) {
                if (parent.id.equalsIgnoreCase(parentId)) {
                    parent.resources.add(item);
                    break;
                }
            }
        }
        return root;
    }
}
