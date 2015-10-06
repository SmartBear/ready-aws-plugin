package com.smartbear.aws;

public final class DeploymentSetting {
    public final boolean proxyIntegration;
    public final String endPoint;
    public final String defaultResponse;

    public static DeploymentSetting withoutIntegration() {
        return new DeploymentSetting(false, "", "");
    }

    public static DeploymentSetting proxyIntegration(String endPoint, String defaultResponse) {
        return new DeploymentSetting(true, endPoint, defaultResponse);
    }

    private DeploymentSetting(boolean proxyIntegration, String endPoint, String defaultResponse) {
        this.proxyIntegration = proxyIntegration;
        this.endPoint = endPoint;
        this.defaultResponse = defaultResponse;
    }
}
