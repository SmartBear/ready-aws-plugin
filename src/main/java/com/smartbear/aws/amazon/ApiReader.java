package com.smartbear.aws.amazon;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.support.StringUtils;
import com.smartbear.aws.ApplicationException;
import com.smartbear.aws.Helper;
import com.smartbear.aws.Strings;
import com.smartbear.aws.entity.Api;
import com.smartbear.aws.entity.ApiDescription;
import com.smartbear.aws.entity.HttpMethod;
import com.smartbear.aws.entity.HttpResource;
import com.smartbear.aws.entity.HttpResourceDescription;
import com.smartbear.aws.entity.Stage;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.stream.JsonParsingException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public final class ApiReader {
    private final static String APIS_PATH = "/restapis";
    private final static String RESOURCES_PATH_TMPL = "/restapis/%s/resources";
    private final static String STAGES_PATH_TMPL = "/restapis/%s/stages";
    private final static String METHOD_PATH_TMPL = "/restapis/%s/resources/%s/methods/%s";

    private final String accessKey;
    private final String secretKey;
    private final String region;

    public ApiReader(String accessKey, String secretKey, String region) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.region = region;
    }

    public String getRegion() {
        return this.region;
    }

    public boolean checkConnection() throws ApplicationException {
        JsonObject json = executeRequest(accessKey, secretKey, region, "GET", "/", "");
        System.out.println(json);
        return json != null;
    }


    public List<ApiDescription> getApis() throws ApplicationException {
        JsonObject json = executeRequest(accessKey, secretKey, region, "GET", APIS_PATH, "");
        JsonArray items = json.getJsonArray("item");
        if (items == null) {
            throw new ApplicationException(String.format(Strings.Error.UNEXPECTED_RESPONSE_FORMAT, "API list"));
        }
        List<ApiDescription> apis = Helper.extractList(items, new Helper.EntityFactory<ApiDescription>() {
            @Override
            public ApiDescription create(JsonObject value) {
                //TODO: dirty solution
                String apiId = value.getString("id", "");
                List<Stage> stages;
                try {
                    stages = readStages(apiId);
                } catch (ApplicationException ex) {
                    SoapUI.logError(ex);
                    stages = Collections.emptyList();
                }

                return new ApiDescription(value, stages);
            }
        });

        return Collections.unmodifiableList(apis);
    }

    public Api getApi(final ApiDescription description) throws ApplicationException {
        List<HttpResourceDescription> resources = readResourceDescriptions(description.id);

        List<HttpResource> httpResources = readResources(description.id, resources);
        HttpResource treeRoot = buildResourcesTree(httpResources);
        //TODO: check treeRoot on null and trow exception

        return new Api(description, treeRoot);
    }

    private List<Stage> readStages(String apiId) throws ApplicationException {
        final String path = String.format(STAGES_PATH_TMPL, apiId);
        JsonObject json = executeRequest(accessKey, secretKey, region, "GET", path, "");
        JsonArray items = json.getJsonArray("item");
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

    private List<HttpResourceDescription> readResourceDescriptions(String apiId) throws ApplicationException {
        final String path = String.format(RESOURCES_PATH_TMPL, apiId);
        JsonObject json = executeRequest(accessKey, secretKey, region, "GET", path, "");

        JsonArray items = json.getJsonArray("item");
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
            JsonObject methodJson = executeRequest(accessKey, secretKey, region, "GET", methodPath, "");
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

    private static JsonObject executeRequest(String accessKey, String secretKey, String region, String method, String path, String query) throws ApplicationException {
        return executeRequest(accessKey, secretKey, region, method, path, query, "");
    }

    private static JsonObject executeRequest(String accessKey, String secretKey, String region, String method, String path, String query, String body) throws ApplicationException {
        SignatureBuilder builder = new SignatureBuilder(accessKey, secretKey, region);
        String authHeader = builder.buildAuthHeader(method, path, query, body);
        String urlString = "https://" + builder.getHost() + path + (StringUtils.isNullOrEmpty(query) ? "" : "?" + query);

        URLConnection connection = null;
        try {
            URL url = new URL(urlString);
            connection = url.openConnection();
        } catch (MalformedURLException e) {
            throw new ApplicationException(String.format(Strings.Error.MALFORMED_URL, urlString), e);
        } catch (IOException e) {
            throw new ApplicationException(String.format(Strings.Error.UNABLE_CREATE_CONNECTION, urlString), e);
        }

        connection.setDoInput(true);
        if (StringUtils.hasContent(body)) {
            connection.setDoOutput(true);
        }
        connection.setRequestProperty("Content-Type", "application/x-amz-json-1.0");
        connection.setRequestProperty("X-Amz-Date", builder.getAmzDate());
        connection.setRequestProperty("Authorization", authHeader);

        try {
            connection.connect();
        } catch (IOException e) {
            throw new ApplicationException(String.format(Strings.Error.UNAVAILABLE_HOST, urlString), e);
        }

        if (StringUtils.hasContent(body)) {
            try (OutputStream output = connection.getOutputStream()) {
                output.write(body.getBytes("UTF-8"));
            } catch (IOException ex) {
                throw new ApplicationException(Strings.Error.UNABLE_SET_REQEST_BODY, ex);
            }
        }

        Reader reader;
        try {
            reader = new InputStreamReader(connection.getInputStream());
        } catch (FileNotFoundException e) {
            throw new ApplicationException(String.format(Strings.Error.UNAVAILABLE_DATA, urlString), e);
        } catch (IOException e) {
            throw new ApplicationException("", e);
        }

        try (javax.json.JsonReader jsonReader = javax.json.Json.createReader(reader)) {
            return jsonReader.readObject();
        } catch (JsonParsingException e) {
            throw new ApplicationException(String.format(Strings.Error.INVALID_JSON_RESPONSE, urlString), e);
        }
    }
}
