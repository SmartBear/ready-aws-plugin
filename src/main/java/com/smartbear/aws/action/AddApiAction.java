package com.smartbear.aws.action;

import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.plugins.ActionConfiguration;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.smartbear.ActionGroups;
import com.smartbear.aws.amazon.ApiReader;
import com.smartbear.aws.Strings;
import com.smartbear.aws.ui.AccountInfoDialog;
import com.smartbear.aws.ui.ApiImporter;
import com.smartbear.aws.ui.ApiSelectorDialog;
import com.smartbear.rapisupport.ServiceFactory;

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
        if (accountInfo == null || accountInfo.apis.size() == 0) {
            return;
        }

        ApiSelectorDialog.Result selectedApis = null;
        try (ApiSelectorDialog dlg = new ApiSelectorDialog(accountInfo.apis)) {
            selectedApis = dlg.show();
        }

        if (selectedApis == null || selectedApis.selectedAPIs.size() == 0) {
            return;
        }

        ApiReader reader = new ApiReader(accountInfo.accessKey, accountInfo.secretKey, accountInfo.region);

        List<RestService> services = ApiImporter.importServices(reader, selectedApis.selectedAPIs, wsdlProject);
        ServiceFactory.Build(wsdlProject, services, selectedApis.entities);
        if (services.size() > 0) {
            UISupport.select(services.get(0));
        }
    }
}
