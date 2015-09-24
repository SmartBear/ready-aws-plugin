package com.smartbear.aws.entity;

import javax.json.JsonObject;
import java.util.List;

public class ApiDescription {
    public final String id;
    public final String name;
    public final String description;
    public final String baseUrl;
    public final List<Stage> stages;

    private Stage stage = null;

    public ApiDescription(String id, String name, String description, String baseUrl, List<Stage> stages) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.baseUrl = baseUrl;
        this.stages = stages;
    }

    public ApiDescription(JsonObject src, List<Stage> stages) {
        this.id = src.getString("id", "");
        this.name = src.getString("name", "");
        this.description = src.getString("description", "");
        this.baseUrl = src.getString("baseURL", "");
        this.stages = stages;
    }

    @Override
    public String toString() {
        return String.format("id=%s, name=%s, description%s, stages=%s\r\n", id, name, description, stages);
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
