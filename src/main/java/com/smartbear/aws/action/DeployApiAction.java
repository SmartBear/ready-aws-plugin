package com.smartbear.aws.action;

import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.plugins.ActionConfiguration;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.smartbear.ActionGroups;
import com.smartbear.aws.DeploymentSetting;
import com.smartbear.aws.Strings;
import com.smartbear.aws.amazon.ApiWriter;
import com.smartbear.aws.ui.AccountInfoDialog;
import com.smartbear.aws.ui.ApiExporter;
import com.smartbear.aws.ui.DeploymentSettingDialog;

@ActionConfiguration(actionGroup = ActionGroups.REST_SERVICE_ACTIONS, afterAction = "ExportWadlAction", separatorBefore = false)
public class DeployApiAction extends AbstractSoapUIAction<RestService> {
    public DeployApiAction() {
        super(Strings.DeployApiAction.NAME, Strings.DeployApiAction.DESCRIPTION);
    }

    @Override
    public void perform(RestService restService, Object o) {
        AccountInfoDialog.Result accountInfo = null;
        try (AccountInfoDialog dlg = new AccountInfoDialog(false)) {
            accountInfo = dlg.show();
        }
        if (accountInfo == null || accountInfo.apis.size() == 0) {
            return;
        }

        DeploymentSetting setting = null;
        try (DeploymentSettingDialog dlg = new DeploymentSettingDialog(restService.getEndpoints())) {
            setting = dlg.show();
        }
        if (setting == null) {
            return;
        }

        ApiWriter writer = new ApiWriter(accountInfo.accessKey, accountInfo.secretKey, accountInfo.region, setting);
        ApiExporter.deployServise(writer, restService);
    }
}
