package com.smartbear.aws.entity;

import javax.json.JsonObject;

public class Stage {
    public final String name;
    public final String description;

    public Stage(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Stage(JsonObject src) {
        this.name = src.getString("stageName", "");
        this.description = src.getString("description", "");
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", description, name);
    }
}
