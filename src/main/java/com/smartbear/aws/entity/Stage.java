package com.smartbear.aws.entity;

import javax.json.JsonObject;

public class Stage {
    public final String name;
    public final String description;
    public final String deploymentId;

    public Stage(String name, String description) {
        this.name = name;
        this.description = description;
        this.deploymentId = "";
    }

    public Stage(JsonObject src) {
        ResponseValidator.checkStage(src);
        this.name = src.getString("stageName", "");
        this.description = src.getString("description", "");
        this.deploymentId = src.getString("deploymentId", "");
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", description, name);
    }
}
