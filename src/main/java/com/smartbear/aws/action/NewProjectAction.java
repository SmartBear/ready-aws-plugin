package com.smartbear.aws.action;

import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.plugins.auto.PluginImportMethod;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.smartbear.aws.Strings;
import com.smartbear.aws.amazon.ApiReader;
import com.smartbear.aws.ui.AccountInfoDialog;
import com.smartbear.aws.ui.ApiImporter;
import com.smartbear.aws.ui.ApiSelectorDialog;
import com.smartbear.rapisupport.RapiLogger;
import com.smartbear.rapisupport.ServiceFactory;

import java.util.List;

@PluginImportMethod(label = Strings.NewProjectAction.ACTION_CAPTION)
public class NewProjectAction extends AbstractSoapUIAction<WorkspaceImpl> {
    public NewProjectAction() {
        super(Strings.NewProjectAction.NAME, Strings.NewProjectAction.DESCRIPTION);
    }

    @Override
    public void perform(WorkspaceImpl workspace, Object o) {
        AccountInfoDialog.Result accountInfo = null;
        try (AccountInfoDialog dlg = new AccountInfoDialog()) {
            accountInfo = dlg.show();
        }
        if (accountInfo == null || accountInfo.apis.size() == 0) {
            return;
        }

        ApiSelectorDialog.Result selectedApis = null;
        try (ApiSelectorDialog dlg = new ApiSelectorDialog(accountInfo.apis, true)) {
            selectedApis = dlg.show();
        }

        if (selectedApis == null || selectedApis.selectedAPIs.size() == 0) {
            return;
        }

        WsdlProject wsdlProject;
        try {
            wsdlProject = workspace.createProject(selectedApis.projectName, null);
        } catch (Exception e) {
            RapiLogger.logError(e);
            UISupport.showErrorMessage(String.format(Strings.NewProjectAction.UNABLE_CREATE_ERROR, e.getClass().getName(), e.getMessage()));
            return;
        }

        if (wsdlProject == null) {
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
