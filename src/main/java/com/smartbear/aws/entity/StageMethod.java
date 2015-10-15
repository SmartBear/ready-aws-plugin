package com.smartbear.aws.entity;

import com.eviware.soapui.impl.rest.RestRequestInterface;

public final class StageMethod {
    public final RestRequestInterface.HttpMethod httpMethod;
    public final String fullPath;
    public boolean apiKeyRequired;
    public String authorizationType;

    public StageMethod(String name, String fullPath, boolean apiKeyRequired, String authorizationType) {
        this.httpMethod = Enum.valueOf(RestRequestInterface.HttpMethod.class, name);
        this.fullPath = fullPath;
        this.apiKeyRequired = apiKeyRequired;
        this.authorizationType = authorizationType;
    }

    @Override
    public String toString() {
        return String.format("%s, %s, %s, %s", httpMethod, fullPath, apiKeyRequired, authorizationType);
    }
}
