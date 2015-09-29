package com.smartbear.aws.amazon;

import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.support.StringUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public final class RestResourceTreeNode {
    public final String value;
    public final RestResource resource;
    public final List<RestResourceTreeNode> children = new ArrayList<>();

    public RestResourceTreeNode(RestService restService) {
        this.value = "";
        this.resource = null;

        String path = restService.getBasePath();
        String[] parts = normalizePath(path.split("/"));
        RestResourceTreeNode parent = this;
        for (String part: parts) {
            RestResourceTreeNode child = new RestResourceTreeNode(part, null);
            parent.children.add(child);
            parent = child;
        }

        for (RestResource restResource: restService.getResourceList()) {
            parent.addResource(restResource);
        }
    }

    private RestResourceTreeNode(String value, RestResource resource) {
        this.value = value;
        this.resource = resource;
        if (this.resource != null) {
            for (RestResource child: this.resource.getChildResourceList()) {
                this.addResource(child);
            }
        }
    }

    private void addResource(RestResource resource) {
        String path = resource.getPath();
        RestResourceTreeNode parent = this;

        String[] parts = normalizePath(path.split("/"));
        for (int i = 0; i < parts.length; i++) {
            RestResourceTreeNode find = parent.findChild(parts[i]);
            if (find == null) {
                find = new RestResourceTreeNode(parts[i], i == (parts.length - 1) ? resource : null);
                parent.children.add(find);
            }
            parent = find;
        }
    }

    private String[] normalizePath(String[] path) {
        List<String> temp = new LinkedList<>();
        for (String part: path) {
            if (StringUtils.hasContent(part)) {
                temp.add(part);
            }
        }
        return temp.toArray(new String[] {});
    }

    private RestResourceTreeNode findChild(String value) {
        for (RestResourceTreeNode node: this.children) {
            if (node.value.equalsIgnoreCase(value)) {
                return node;
            }
        }
        return null;
    }
}
