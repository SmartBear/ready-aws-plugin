package com.smartbear.aws.amazon;

import com.eviware.soapui.SoapUI;
import com.smartbear.aws.Helper;
import com.smartbear.aws.entity.HttpMethod;
import com.smartbear.aws.entity.HttpResource;
import com.smartbear.aws.entity.StageMethod;

import java.util.LinkedList;
import java.util.List;

public final class StageTreeBuilder {
    private final HttpResource root;
    private final List<StageMethod> stageMethods;

    public StageTreeBuilder(HttpResource root, List<StageMethod> stageMethods) {
        this.root = root;
        this.stageMethods = stageMethods;
    }

    public HttpResource execute() {
        synchronizeMethods(root);
        removeEmpty(root);
        for (StageMethod method: stageMethods) {
            //TODO: create required resources and method
        }
        return root;
    }

    private void removeEmpty(HttpResource source) {
        List<HttpResource> toRemove = new LinkedList<>();
        for (HttpResource child: source.resources) {
            removeEmpty(child);
            if (child.methods.size() == 0 && child.resources.size() == 0) {
                toRemove.add(child);
            }
        }
        source.resources.removeAll(toRemove);
    }

    private void synchronizeMethods(HttpResource source) {
        //copy settings from the methods that are presented in the apiSummary (stageMethods)
        List<HttpMethod> toRemove = new LinkedList<>();
        for (HttpMethod method: source.methods) {
            StageMethod stageMethod = findBy(method, source.fullPath);
            if (stageMethod != null) {
                method.setApiKeyRequired(stageMethod.apiKeyRequired);
                method.setAuthorizationType(stageMethod.authorizationType);
                stageMethods.remove(stageMethod);
            } else {
                toRemove.add(method);
            }
        }

        //remove non presented in the apiSummary methods
        source.methods.removeAll(toRemove);

        //create methods that are presented in the apiSummary and don't presented in the methods
        List<StageMethod> toAdd = findBy(source.fullPath);
        for (StageMethod method: toAdd) {
            source.methods.add(new HttpMethod(method));
        }
        stageMethods.removeAll(toAdd);

        for (HttpResource child: source.resources) {
            synchronizeMethods(child);
        }
    }

    private StageMethod findBy(HttpMethod source, String fullPath) {
        for (StageMethod method: stageMethods) {
            if (method.httpMethod == source.httpMethod && method.fullPath.equalsIgnoreCase(fullPath)) {
                return method;
            }
        }
        return null;
    }

    private List<StageMethod> findBy(String fullPath) {
        List<StageMethod> methods = new LinkedList<>();
        for (StageMethod method: stageMethods) {
            if (method.fullPath.equalsIgnoreCase(fullPath)) {
                methods.add(method);
            }
        }
        return methods;
    }
}
