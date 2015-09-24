package com.smartbear.aws.action;

import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.RestServiceFactory;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.plugins.ActionConfiguration;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.smartbear.ActionGroups;
import com.smartbear.aws.amazon.ApiReader;
import com.smartbear.aws.Strings;
import com.smartbear.aws.entity.Api;
import com.smartbear.aws.entity.ApiDescription;
import com.smartbear.aws.entity.HttpMethod;
import com.smartbear.aws.entity.HttpResource;
import com.smartbear.aws.entity.MethodParameter;
import com.smartbear.aws.ui.AccountInfoDialog;
import com.smartbear.aws.ui.ApiSelectorDialog;

import java.util.List;

@ActionConfiguration(actionGroup = ActionGroups.OPEN_PROJECT_ACTIONS, separatorBefore = true)
public class AddApiAction extends AbstractSoapUIAction<WsdlProject> {
    public AddApiAction() {
        super(Strings.AddApiAction.NAME, Strings.AddApiAction.DESCRIPTION);
    }

    @Override
    public void perform(WsdlProject wsdlProject, Object o) {
        AccountInfoDialog.Result accountInfo = null;
        try (AccountInfoDialog dlg = new AccountInfoDialog()) {
            accountInfo = dlg.show();
        }
        if (accountInfo == null) {
            return;
        }

        try {
            ApiReader reader = new ApiReader(accountInfo.accessKey, accountInfo.secretKey, accountInfo.region);
            List<ApiDescription> apis = reader.getApis();

            ApiSelectorDialog.Result selectedApis = null;
            try (ApiSelectorDialog dlg = new ApiSelectorDialog(apis)) {
                selectedApis = dlg.show();
            }

            if (selectedApis == null || selectedApis.selectedAPIs.size() == 0) {
                return;
            }

            for (ApiDescription description: selectedApis.selectedAPIs) {
                Api api = reader.getApi(description);
                RestService rest = build(api, wsdlProject, accountInfo.region);
            }
            UISupport.showInfoMessage("Successfully completed");
        } catch (Exception ex) {
            UISupport.showInfoMessage("Completed with error: " + ex.getLocalizedMessage());
        }
    }

    private RestService build(Api api, WsdlProject wsdlProject, String region) {
        try {
            RestService restService = (RestService)wsdlProject.addNewInterface(api.name, RestServiceFactory.REST_TYPE);
            restService.setDescription(api.description);
            restService.setBasePath(api.baseUrl);
            String endPoint = String.format("https://%s.execute-api.%s.amazonaws.com", api.id, region);
            restService.addEndpoint(endPoint);

            RestResource root = api.stage == null ?
                    restService.addNewResource("root", ""):
                    restService.addNewResource(api.stage.name, api.stage.name);
            root.setDescription(api.stage == null ? "" : api.stage.description);
            addResources(root, api.rootResource);
            return restService;
        } catch (Exception e) {
            return null;
        }
    }

    private void addResources(RestResource target, HttpResource source) {
        for (HttpResource child: source.resources) {
            RestResource restResource = target.addNewChildResource(child.name, child.path);
            for (HttpMethod method: child.methods) {
                RestMethod restMethod = restResource.addNewMethod(method.name);
                restMethod.setMethod(method.httpMethod);
                for (MethodParameter param: method.parameters) {
                    RestParamProperty prop = restMethod.addProperty(param.name);
                    prop.setStyle(param.style);
                    prop.setRequired(param.isRequired);
                }
                restMethod.addNewRequest("Request1");
            }
            addResources(restResource, child);
        }
    }

}
