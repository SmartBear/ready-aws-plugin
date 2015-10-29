package com.smartbear.aws.entity;

import com.smartbear.aws.Helper;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public final class HttpResourceDescription {
    public final String id;
    public final String parentId;
    public final String path;
    public final String name;
    public final List<String> methodsNames;

    public HttpResourceDescription(JsonObject src) {
        ResponseValidator.checkResource(src);
        this.id = src.getString("id", "");
        this.parentId = src.getString("parentId", "");
        this.path = src.getString("path", "");
        final String pathPart = src.getString("pathPart", "");
        this.name = pathPart.replace("/", "-");
        this.methodsNames = this.findMethods(src);
    }

    @Override
    public String toString() {
        return String.format("%s, %s, %s, %s, %s\r\n", id, parentId, path, name, Helper.collectionToString(methodsNames));
    }

    private List<String> findMethods(JsonObject response) {
        JsonArray methods = extractMethodsFromResponse(response);
        List<String> existingMethods = new LinkedList<>();
        for (JsonValue item: methods) {
            if (item instanceof JsonObject) {
                JsonObject methodJson = (JsonObject)item;
                existingMethods.add(methodJson.getString("name"));
            }
        }

        return Collections.unmodifiableList(existingMethods);
    }

    private JsonArray extractMethodsFromResponse(JsonObject response) {
        //IMPORTANT: _links and resource:methods fields are optional!!!
        //IMPORTANT: resource:methods value can be an Object instead of an Array if child element is just one
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        if (!response.containsKey("_links")) {
            return arrayBuilder.build();
        }
        JsonValue links = response.get("_links");
        if (links instanceof JsonObject) {
            JsonObject linksObject = (JsonObject)links;
            if (!linksObject.containsKey("resource:methods")) {
                return arrayBuilder.build();
            }
            JsonValue item = linksObject.get("resource:methods");
            if (item instanceof JsonObject) {
                return arrayBuilder.add(item).build();
            } else if (item instanceof JsonArray) {
                return (JsonArray)item;
            }
        }
        return arrayBuilder.build();
    }
}
