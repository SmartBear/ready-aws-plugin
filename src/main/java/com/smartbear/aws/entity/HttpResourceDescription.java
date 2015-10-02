package com.smartbear.aws.entity;

import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.smartbear.aws.Helper;

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
        this.id = src.getString("id", "");
        this.parentId = src.getString("parentId", "");
        this.path = src.getString("path", "");
        final String pathPart = src.getString("pathPart", "");
        this.name = pathPart.replace("/", "-");
        this.methodsNames = this.findMethods(src.get("resourceMethods"));
    }

    @Override
    public String toString() {
        return String.format("%s, %s, %s, %s, %s\r\n", id, parentId, path, name, Helper.collectionToString(methodsNames));
    }

    private List<String> findMethods(JsonValue src) {
        List<String> existingMethods = new LinkedList<>();
        if (src instanceof JsonObject) {
            JsonObject methodsObj = (JsonObject)src;
            for (String method: RestRequestInterface.HttpMethod.getMethodsAsStringArray()) {
                JsonValue methodJson = methodsObj.get(method);
                if (methodJson instanceof JsonObject) {
                    existingMethods.add(method);
                }
            }
        }
        return Collections.unmodifiableList(existingMethods);
    }
}
