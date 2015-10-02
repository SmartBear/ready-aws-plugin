package com.smartbear.aws.entity;

import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.smartbear.aws.Helper;

import javax.json.JsonObject;
import javax.json.JsonValue;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public final class HttpMethod {
    public final String name;
    public final RestRequestInterface.HttpMethod httpMethod;
    public final boolean apiKeyRequired;
    public final String authorizationType;
    public final List<MethodParameter> parameters;
    public final List<MethodResponse> responses;

    public HttpMethod(JsonObject src) {
        this.name = src.getString("httpMethod", "");
        this.httpMethod = Enum.valueOf(RestRequestInterface.HttpMethod.class, this.name);
        this.apiKeyRequired = src.getBoolean("apiKeyRequired", false);
        this.authorizationType = src.getString("authorizationType", "");
        this.parameters = Collections.unmodifiableList(getParameters(src.get("requestParameters")));
        this.responses = Collections.unmodifiableList(getResponses(src.get("methodResponses")));
    }

    private List<MethodResponse> getResponses(JsonValue value) {
        List<MethodResponse> values = new LinkedList<>();
        if (value instanceof JsonObject) {
            JsonObject json = (JsonObject)value;
            for (String key: json.keySet()) {
                values.add(new MethodResponse(json.getJsonObject(key)));
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
