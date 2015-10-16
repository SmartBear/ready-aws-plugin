package com.smartbear.aws.entity;

import com.smartbear.aws.Helper;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.util.LinkedList;
import java.util.List;

public final class ApiKey {
    public final String id;
    public final String name;
    public final String description;
    public final boolean enabled;
    public final List<String> stages = new LinkedList<>();


    public ApiKey(JsonObject src) {
        this.id = src.getString("id", "");
        this.name = src.getString("name", "");
        this.description = src.getString("description", "");
        this.enabled = src.getBoolean("enabled");

        JsonValue stages = src.get("stageKeys");
        if (stages instanceof JsonArray) {
            for (JsonValue item: (JsonArray)stages) {
                this.stages.add(item.toString());
            }
        }
    }

    @Override
    public String toString() {
        return String.format("id=%s, name=%s, description=%s, stages=%s", id, name, description, stages);
    }
}
