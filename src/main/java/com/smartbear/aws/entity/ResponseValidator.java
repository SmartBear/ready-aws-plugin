package com.smartbear.aws.entity;

import com.smartbear.rapisupport.RapiLogger;

import javax.json.JsonObject;

public final class ResponseValidator {
    private static final boolean active = false;

    private static final String API_WARNING_TMPL = "Field '%s' is absent in the API response";
    private static final String[] API_FIELDS = { "id", "name", "description", "baseURL" };
    private static final String APIKEY_WARNING_TMPL = "Field '%s' is absent in the ApiKey response";
    private static final String[] APIKEY_FIELDS = { "id", "name", "description", "enabled", "stageKeys" };
    private static final String METHOD_WARNING_TMPL = "Field '%s' is absent in the Method response";
    private static final String[] METHOD_FIELDS = { "httpMethod", "apiKeyRequired", "authorizationType", "requestParameters", "methodResponses" };
    private static final String RESOURCE_WARNING_TMPL = "Field '%s' is absent in the Resource response";
    private static final String[] RESOURCE_FIELDS = { "id", "parentId", "path", "pathPart" };
    private static final String RESPONSE_WARNING_TMPL = "Field '%s' is absent in the Response response";
    private static final String[] RESPONSE_FIELDS = { "statusCode", "responseModels" };
    private static final String STAGE_WARNING_TMPL = "Field '%s' is absent in the Stage response";
    private static final String[] STAGE_FIELDS = { "stageName", "description", "deploymentId" };
    private static final String DEPLOYMENT_METHOD_WARNING_TMPL = "Field '%s' is absent in the Deployment response";
    private static final String[] DEPLOYMENT_METHOD_FIELDS = { "apiKeyRequired", "authorizationType" };

    public static void checkApi(JsonObject src) {
        checkFields(src, API_FIELDS, API_WARNING_TMPL);
    }

    public static void checkApiKey(JsonObject src) {
        checkFields(src, APIKEY_FIELDS, APIKEY_WARNING_TMPL);
    }

    public static void checkMethod(JsonObject src) {
        checkFields(src, METHOD_FIELDS, METHOD_WARNING_TMPL);
    }

    public static void checkResource(JsonObject src) {
        checkFields(src, RESOURCE_FIELDS, RESOURCE_WARNING_TMPL);
    }

    public static void checkResponse(JsonObject src) {
        checkFields(src, RESPONSE_FIELDS, RESPONSE_WARNING_TMPL);
    }

    public static void checkStage(JsonObject src) {
        checkFields(src, STAGE_FIELDS, STAGE_WARNING_TMPL);
    }

    public static void checkDeploymentMethod(JsonObject src) {
        checkFields(src, DEPLOYMENT_METHOD_FIELDS, DEPLOYMENT_METHOD_WARNING_TMPL);
    }

    private static void checkFields(JsonObject src, String[] fields, String warningTemplate) {
        if (!active) {
            return;
        }

        for (String field: fields) {
            if (!src.containsKey(field)) {
                logWarning(String.format(warningTemplate, field));
            }
        }
    }

    private static void logWarning(String message) {
        RapiLogger.log("AWS_WARNING. " + message);
        //System.out.println("AWS_WARNING. " + message);
    }
}
