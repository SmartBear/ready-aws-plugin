package com.smartbear.aws.entity;

import com.smartbear.aws.Helper;

import javax.json.JsonArray;
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
        JsonArray methods = ResponseParser.extractChildArray(response, "_links", "resource:methods");
        List<String> existingMethods = new LinkedList<>();
        for (JsonValue item: methods) {
            if (item instanceof JsonObject) {
                JsonObject methodJson = (JsonObject)item;
                existingMethods.add(methodJson.getString("name"));
            }
        }

        return Collections.unmodifiableList(existingMethods);
    }
}
