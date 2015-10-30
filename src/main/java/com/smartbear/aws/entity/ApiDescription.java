package com.smartbear.aws.entity;

import javax.json.JsonObject;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public final class ApiDescription {
    public final String id;
    public final String name;
    public final String description;
    public final List<Stage> stages;
    public final List<ApiKey> apiKeys;

    private Stage stage = null;
    private ApiKey apiKey = null;

    public ApiDescription(String id, String name, String description, List<Stage> stages) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.stages = stages;
        this.apiKeys = Collections.unmodifiableList(Collections.<ApiKey>emptyList());
    }

    public ApiDescription(JsonObject src, List<Stage> stages, List<ApiKey> allApiKeys) {
        ResponseValidator.checkApi(src);
        this.id = src.getString("id", "");
        this.name = src.getString("name", "");
        this.description = src.getString("description", "");
        this.stages = stages;
        this.apiKeys = Collections.unmodifiableList(filterKeys(allApiKeys));
    }

    private List<ApiKey> filterKeys(List<ApiKey> allApiKeys) {
        List<ApiKey> result = new LinkedList<>();
        for (ApiKey key: allApiKeys) {
            for (String stage: key.stages) {
                if (stage.startsWith(this.id + "/")) {
                    result.add(key);
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return String.format("id=%s, name=%s, description=%s, stages=%s, apiKeys=%s\r\n", id, name, description, stages, apiKeys);
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public ApiKey getApiKey() {
        return apiKey;
    }

    public void setApiKey(ApiKey key) {
        this.apiKey = key;
    }

    public List<ApiKey> getSelectedStageKeys() {
        if (stage == null) {
            return Collections.emptyList();
        }
        if (apiKeys.size() == 0) {
            return Collections.emptyList();
        }

        List<ApiKey> result = new LinkedList<>();

        for (ApiKey key: apiKeys) {
            for (String keyStage: key.stages) {
                if (keyStage.endsWith("/" + stage.name)) {
                    result.add(key);
                }
            }
        }

        return result;
    }
}
