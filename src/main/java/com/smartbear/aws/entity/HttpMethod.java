package com.smartbear.aws.entity;

import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.smartbear.aws.Helper;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public final class HttpMethod {
    public final String name;
    public final RestRequestInterface.HttpMethod httpMethod;
    public final List<MethodParameter> parameters;
    public final List<MethodResponse> responses;

    private boolean apiKeyRequired;
    private String authorizationType;

    public HttpMethod(JsonObject src) {
        ResponseValidator.checkMethod(src);
        this.name = src.getString("httpMethod", "");
        this.httpMethod = Enum.valueOf(RestRequestInterface.HttpMethod.class, this.name);
        this.apiKeyRequired = src.getBoolean("apiKeyRequired", false);
        this.authorizationType = src.getString("authorizationType", "");
        //optional parameter
        this.parameters = Collections.unmodifiableList(getParameters(src.get("requestParameters")));
        this.responses = Collections.unmodifiableList(getResponses(src));
    }

    public HttpMethod(StageMethod src) {
        this.name = src.httpMethod.toString();
        this.httpMethod = src.httpMethod;
        this.apiKeyRequired = src.apiKeyRequired;
        this.authorizationType = src.authorizationType;
        this.parameters = Collections.unmodifiableList(Collections.<MethodParameter>emptyList());
        this.responses = Collections.unmodifiableList(Collections.<MethodResponse>emptyList());
    }

    public boolean isApiKeyRequired() {
        return apiKeyRequired;
    }

    public void setApiKeyRequired(boolean apiKeyRequired) {
        this.apiKeyRequired = apiKeyRequired;
    }

    public String getAuthorizationType() {
        return authorizationType;
    }

    public void setAuthorizationType(String authorizationType) {
        this.authorizationType = authorizationType;
    }

    private List<MethodResponse> getResponses(JsonObject src) {
        JsonArray responses = ResponseParser.extractChildArray(src, "_links", "method:responses");
        List<MethodResponse> values = new LinkedList<>();
        for (JsonValue item: responses) {
            if (item instanceof JsonObject) {
                JsonObject responseJson = (JsonObject)item;
                values.add(new MethodResponse(responseJson));
            }
        }
        return values;
    }

    private List<MethodParameter> getParameters(JsonValue value) {
        List<MethodParameter> values = new LinkedList<>();
        if (value instanceof JsonObject) {
            JsonObject json = (JsonObject)value;
            for (String key: json.keySet()) {
                values.add(new MethodParameter(key, json.getBoolean(key, false)));
            }
        }
        return values;
    }

    @Override
    public String toString() {
        return String.format("%s, %s, %s, %s, %s", name, apiKeyRequired, authorizationType, Helper.collectionToString(parameters), Helper.collectionToString(responses));
    }
}
