package com.smartbear.aws.amazon;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonValue;

public class ApiOperationBase {
    protected final static String APIS_PATH = "/restapis";
    protected final static String RESOURCES_PATH_TMPL = "/restapis/%s/resources";

    protected final HttpRequestExecutor requestExecutor;

    protected ApiOperationBase(String accessKey, String secretKey, String region) {
        this.requestExecutor = new HttpRequestExecutor(accessKey, secretKey, region);
    }

    /*protected JsonArray extractItemsFromResponse(JsonObject response) {
        return response.getJsonArray("item");
    }*/

    protected JsonArray extractItemsFromResponse(JsonObject response) {
        //IMPORTANT: _embedded and item fields are optional!!!
        //IMPORTANT: item value can be an Object instead of an Array if child element is just one
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        if (!response.containsKey("_embedded")) {
            return arrayBuilder.build();
        }
        JsonValue embdded = response.get("_embedded");
        if (embdded instanceof JsonObject) {
            JsonObject embededObject = (JsonObject)embdded;
            if (!embededObject.containsKey("item")) {
                return arrayBuilder.build();
            }
            JsonValue item = embededObject.get("item");
            if (item instanceof JsonObject) {
                return arrayBuilder.add(item).build();
            } else if (item instanceof JsonArray) {
                return (JsonArray)item;
            }
        }
        return null;
    }
}
