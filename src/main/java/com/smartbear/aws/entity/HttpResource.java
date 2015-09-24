package com.smartbear.aws.entity;

import com.smartbear.aws.Helper;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class HttpResource {
    public final String id;
    public final String parentId;
    public final String path;
    public final String name;
    public final List<HttpResource> resources = new LinkedList<>();
    public final List<HttpMethod> methods;

    public HttpResource(HttpResourceDescription description, List<HttpMethod> methods) {
        this.id = description.id;
        this.parentId = description.parentId;
        this.path = description.path;
        this.name = description.name;
        this.methods = Collections.unmodifiableList(methods);
    }

    @Override
    public String toString() {
        return String.format("id=%s, parent=%s, path=%s, name=%s, resources=%s, methods=%s\r\n",
                id, parentId, path, name, Helper.collectionToString(resources), Helper.collectionToString(methods));
    }
}
