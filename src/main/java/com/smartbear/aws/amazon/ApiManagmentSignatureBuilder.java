package com.smartbear.aws.amazon;

public final class ApiManagmentSignatureBuilder extends SignatureBuilder {
    public ApiManagmentSignatureBuilder(String accessKey, String secretKey, String region) {
        super(accessKey, secretKey, region, "apigateway", String.format("apigateway.%s.amazonaws.com", region));
    }

    @Override
    protected String getSignedHeaders() {
        return "accept;host;x-amz-date";
    }

    @Override
    protected String getCanonicalHeaders() {
        return String.format("accept:%s\nhost:%s\nx-amz-date:%s\n", ACCEPT_TYPE, getHost(), getAmzDate());
    }
}
