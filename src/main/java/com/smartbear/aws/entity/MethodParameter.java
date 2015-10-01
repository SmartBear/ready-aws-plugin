package com.smartbear.aws.entity;

import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;

public final class MethodParameter {
    private static final String HEADER_PREFIX = "method.request.header.";
    private static final String QUERY_PREFIX = "method.request.querystring.";
    private static final String PATH_PREFIX = "method.request.path.";
    public final String name;
    public final boolean isRequired;
    public RestParamsPropertyHolder.ParameterStyle style;

    public MethodParameter(String name, boolean isRequired) {
        if (name.startsWith(HEADER_PREFIX)) {
            this.name = name.substring(HEADER_PREFIX.length());
            this.style = RestParamsPropertyHolder.ParameterStyle.HEADER;
        } else if (name.startsWith(QUERY_PREFIX)) {
            this.name = name.substring(QUERY_PREFIX.length());
            this.style = RestParamsPropertyHolder.ParameterStyle.QUERY;
        } else if (name.startsWith(PATH_PREFIX)) {
            this.name = name.substring(PATH_PREFIX.length());
            this.style = RestParamsPropertyHolder.ParameterStyle.TEMPLATE;
        } else {
            this.name = name;
            this.style = RestParamsPropertyHolder.ParameterStyle.PLAIN;
        }
        this.isRequired = isRequired;
    }

    @Override
    public String toString() {
        return String.format("%s, %s, %s", name, isRequired, style);
    }
}
