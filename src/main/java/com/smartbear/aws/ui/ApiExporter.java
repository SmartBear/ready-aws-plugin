package com.smartbear.aws.ui;

import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.support.UISupport;
import com.eviware.x.dialogs.Worker;
import com.eviware.x.dialogs.XProgressDialog;
import com.eviware.x.dialogs.XProgressMonitor;
import com.smartbear.aws.Strings;
import com.smartbear.aws.amazon.ApiWriter;
import com.smartbear.rapisupport.RapiLogger;

public class ApiExporter implements Worker {
    private boolean canceled;
    private final XProgressDialog waitDialog;
    private final ApiWriter apiWriter;
    private final RestService restService;
    private final StringBuilder errors = new StringBuilder();

    public ApiExporter(XProgressDialog waitDialog, ApiWriter apiWriter, RestService restService) {
        this.waitDialog = waitDialog;
        this.apiWriter = apiWriter;
        this.restService = restService;
    }

    public static void deployServise(ApiWriter apiWriter, RestService restService) {
        XProgressDialog dlg = UISupport.getDialogs().createProgressDialog(Strings.ApiExporter.EXPORT_PROGRESS, 100, "", true);
        ApiExporter worker = new ApiExporter(dlg, apiWriter, restService);
        try {
            worker.waitDialog.run(worker);
        } catch (Exception ex) {
            UISupport.showErrorMessage(ex);
            RapiLogger.logError(ex);
        }
    }

    @Override
    public Object construct(XProgressMonitor xProgressMonitor) {
        try {
            apiWriter.perform(restService);
        } catch (Exception ex) {
            RapiLogger.logError(ex);
            errors.append(String.format(Strings.ApiExporter.EXPORT_ERROR, restService.getName(), ex.getMessage()));
        }
        if (errors.length() > 0) {
            errors.append(Strings.ApiExporter.EXPORT_ERROR_TAIL);
        }

        return null;
    }

    @Override
    public void finished() {
        if (canceled) {
            return;
        }
        waitDialog.setVisible(false);
        if (errors.length() > 0) {
            UISupport.showErrorMessage(errors.toString());
        }
    }

    @Override
    public boolean onCancel() {
        canceled = true;
        waitDialog.setVisible(false);
        return true;
    }
}
