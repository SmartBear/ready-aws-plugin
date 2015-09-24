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

    public HttpMethod(JsonObject src) {
        this.name = src.getString("httpMethod", "");
        this.httpMethod = Enum.valueOf(RestRequestInterface.HttpMethod.class, this.name);
        this.apiKeyRequired = src.getBoolean("apiKeyRequired", false);
        this.authorizationType = src.getString("authorizationType", "");

        JsonValue requestParameters = src.get("requestParameters");
        List<MethodParameter> params = new LinkedList<>();
        if (requestParameters instanceof JsonObject) {
            JsonObject json = (JsonObject) requestParameters;
            for (String key: json.keySet()) {
                params.add(new MethodParameter(key, json.getBoolean(key, false)));
            }
        }
        this.parameters = Collections.unmodifiableList(params);
    }

    @Override
    public String toString() {
        return String.format("%s, %s, %s, %s", name, apiKeyRequired, authorizationType, Helper.collectionToString(parameters));
    }
}
