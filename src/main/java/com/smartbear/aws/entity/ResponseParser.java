package com.smartbear.aws.entity;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonValue;

public final class ResponseParser {
    public static JsonArray extractChildArray(JsonObject src, String parentField, String field) {
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        if (!src.containsKey(parentField)) {
            return arrayBuilder.build();
        }
        JsonValue links = src.get(parentField);
        if (links instanceof JsonObject) {
            JsonObject linksObject = (JsonObject)links;
            if (!linksObject.containsKey(field)) {
                return arrayBuilder.build();
            }
            JsonValue item = linksObject.get(field);
            if (item instanceof JsonObject) {
                return arrayBuilder.add(item).build();
            } else if (item instanceof JsonArray) {
                return (JsonArray)item;
            }
        }
        return arrayBuilder.build();
    }
}
