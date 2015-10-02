package com.smartbear.aws.entity;

import com.smartbear.aws.Helper;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class HttpResource {
    public final String id;
    public final String parentId;
    public final String path;
    public final String name;
    public final List<HttpResource> resources = new LinkedList<>();
    public final List<HttpMethod> methods;
    public final List<String> params = new LinkedList<>();

    public HttpResource(HttpResourceDescription description, List<HttpMethod> methods) {
        String[] parts = description.path.split("/");
        this.id = description.id;
        this.parentId = description.parentId;
        this.path = parts.length > 0 ? parts[parts.length - 1] : "";
        this.name = description.name;
        this.methods = Collections.unmodifiableList(methods);

        Matcher matcher = Pattern.compile("\\{.*\\}").matcher(this.path);
        while (matcher.find()) {
            this.params.add(this.path.substring(matcher.start() + 1, matcher.end() - 1));
        }
    }

    @Override
    public String toString() {
        return String.format("id=%s, parent=%s, path=%s, name=%s, params=%s, resources=%s, methods=%s\r\n",
                id, parentId, path, name,
                Helper.collectionToString(params),
                Helper.collectionToString(resources),
                Helper.collectionToString(methods));
    }
}
