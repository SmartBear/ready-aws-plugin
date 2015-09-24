package com.smartbear.aws.ui;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.support.UISupport;
import com.eviware.x.dialogs.Worker;
import com.eviware.x.dialogs.XProgressDialog;
import com.eviware.x.dialogs.XProgressMonitor;
import com.smartbear.aws.Strings;
import com.smartbear.aws.amazon.ApiReader;
import com.smartbear.aws.entity.ApiDescription;
import java.util.Collections;
import java.util.List;

public class ApiListLoader implements Worker {
    public static class Result {
        public final List<ApiDescription> apis;
        public final String errors;

        public Result(List<ApiDescription> apis, String errors) {
            this.apis = apis;
            this.errors = errors;
        }
    }

    private boolean canceled = false;
    private final XProgressDialog waitDialog;
    private final ApiReader reader;
    private final StringBuilder errors = new StringBuilder();
    private Result result = null;

    public static Result downloadList(ApiReader reader) {
        XProgressDialog dlg = UISupport.getDialogs().createProgressDialog(Strings.ApiListLoader.LOAD_PROGRESS, 100, "", true);
        ApiListLoader worker = new ApiListLoader(dlg, reader);
        try {
            worker.waitDialog.run(worker);
        } catch (Exception ex) {
            UISupport.showErrorMessage(ex.getMessage());
            SoapUI.logError(ex);
        }
        return worker.result;
    }

    public ApiListLoader(XProgressDialog waitDialog, ApiReader reader) {
        this.waitDialog = waitDialog;
        this.reader = reader;
    }

    @Override
    public Object construct(XProgressMonitor xProgressMonitor) {
        try {
            boolean validConnection = reader.checkConnection();
            if (!validConnection)
                throw new Exception(Strings.Error.UNABLE_CONNECT);

            result = new Result(reader.getApis(), "");
        } catch (Exception ex) {
            result = new Result(Collections.<ApiDescription>emptyList(), errors.toString());
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
        return false;
    }
}
