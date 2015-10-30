package com.smartbear.aws.entity;

public final class Api {
    public final String id;
    public final String name;
    public final String description;
    public final HttpResource rootResource;
    public final Stage stage;
    public final ApiKey apiKey;


    public Api(ApiDescription src, HttpResource rootResource) {
        this.id = src.id;
        this.name = src.name;
        this.description = src.name;
        this.stage = src.getStage();
        this.apiKey = src.getApiKey();
        this.rootResource = rootResource;
    }

    @Override
    public String toString() {
        return String.format("-------\r\nid=%s\r\nname=%s\r\ndescription=%s\r\nstage=%s\r\nroot resource=%s\r\n-------\n",
                id,
                name,
                description,
                stage,
                rootResource);
    }
}
