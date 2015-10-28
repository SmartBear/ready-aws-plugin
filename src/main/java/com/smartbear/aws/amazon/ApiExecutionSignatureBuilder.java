package com.smartbear.aws.amazon;

public final class ApiExecutionSignatureBuilder extends SignatureBuilder {
    public ApiExecutionSignatureBuilder(String accessKey, String secretKey, String region, String host) {
        super(accessKey, secretKey, region, "execute-api", host);
    }

    @Override
    protected String getSignedHeaders() {
        return "host;x-amz-date";
    }

    @Override
    protected String getCanonicalHeaders() {
        return String.format("host:%s\nx-amz-date:%s\n", getHost(), getAmzDate());
    }
}
