package com.smartbear.aws.entity;

import com.smartbear.aws.Helper;

import javax.json.JsonObject;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public final class MethodResponse {
    public final String statusCode;
    public final List<String> mediaTypes;

    public MethodResponse(JsonObject src) {
        ResponseValidator.checkResponse(src);
        this.statusCode = src.getString("statusCode", "200");
        List<String> list = new LinkedList<>();
        JsonObject types = src.getJsonObject("responseModels");
        for (String key: types.keySet()) {
            list.add(key);
        }
        this.mediaTypes = Collections.unmodifiableList(list);
    }

    @Override
    public String toString() {
        return String.format("code=%s, types=%s", statusCode, Helper.collectionToString(mediaTypes));
    }
}
