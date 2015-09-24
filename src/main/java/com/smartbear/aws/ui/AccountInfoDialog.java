package com.smartbear.aws.ui;

import com.eviware.soapui.support.StringUtils;
import com.eviware.x.form.ValidationMessage;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldValidator;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;
import com.smartbear.aws.Strings;
import com.smartbear.aws.amazon.ApiReader;
import com.smartbear.aws.entity.ApiDescription;

import java.util.Collections;
import java.util.List;

public class AccountInfoDialog implements AutoCloseable {

    public static class Result {
        public final String accessKey;
        public final String secretKey;
        public final String region;
        public final List<ApiDescription> apis;

        public Result(String accessKey, String secretKey, String region, List<ApiDescription> apis) {
            this.accessKey = accessKey.trim();
            this.secretKey = secretKey.trim();
            this.region = region.trim();
            this.apis = apis;
        }
    }

    private final XFormDialog dialog;
    private final XFormField accessKeyField;
    private final XFormField secretKeyField;
    private final XFormField regionField;
    private ApiListLoader.Result loaderResult = null;

    public AccountInfoDialog() {
        this.dialog = ADialogBuilder.buildDialog(Form.class);
        this.accessKeyField = dialog.getFormField(Form.ACCESS_KEY);
        this.secretKeyField = dialog.getFormField(Form.SECRET_KEY);
        this.regionField = dialog.getFormField(Form.REGION);

        //TODO: remove before releasing
        this.accessKeyField.setValue("AKIAJGQ45ZF4SNFMH5XA");
        this.secretKeyField.setValue("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");
        this.regionField.setValue("us-east-1");

        this.accessKeyField.addFormFieldValidator(new FieldValidator("Access key"));
        this.secretKeyField.addFormFieldValidator(new FieldValidator("Secret key"));
        this.regionField.addFormFieldValidator(new FieldValidator("Region"));
        this.regionField.addFormFieldValidator(new XFormFieldValidator() {
            @Override
            public ValidationMessage[] validateField(XFormField xFormField) {
                ValidationMessage[] msg = downloadApiList();
                if (msg.length > 0) {
                    return msg;
                }
                return new ValidationMessage[0];
            }
        });
    }

    private class FieldValidator implements XFormFieldValidator {
        private final String fieldName;
        public FieldValidator(String fieldName) {
            this.fieldName = fieldName;
        }

        @Override
        public ValidationMessage[] validateField(XFormField xFormField) {
            final String value = xFormField.getValue().trim();
            if (StringUtils.isNullOrEmpty(value)) {
                return new ValidationMessage[]{new ValidationMessage(String.format(Strings.AccountInfoDialog.EMPTY_FIELD_WARNING, fieldName), xFormField)};
            }
            return new ValidationMessage[0];
        }
    }

    private ValidationMessage[] downloadApiList() {
        Result res = buildResult();
        if (!StringUtils.isNullOrEmpty(res.accessKey) && !StringUtils.isNullOrEmpty(res.secretKey) && !StringUtils.isNullOrEmpty(res.region)) {
            ApiReader reader = new ApiReader(res.accessKey, res.secretKey, res.region);
            loaderResult = ApiListLoader.downloadList(reader);
            if (StringUtils.hasContent(loaderResult.errors)) {
                return new ValidationMessage[] { new ValidationMessage(loaderResult.errors, accessKeyField) };
            }
        }
        return new ValidationMessage[0];
    }

    private Result buildResult() {
        List<ApiDescription> apis = loaderResult == null ? Collections.<ApiDescription>emptyList() : loaderResult.apis;
        return new Result(accessKeyField.getValue(), secretKeyField.getValue(), regionField.getValue(), apis);
    }

    public Result show() {
        return dialog.show() ? buildResult() : null;
    }

    @Override
    public void close() {
        dialog.release();
    }

    @AForm(name = Strings.AccountInfoDialog.PROMPT_API_DIALOG_CAPTION, description = Strings.AccountInfoDialog.PROMPT_API_DIALOG_DESCRIPTION)
    public interface Form {
        @AField(name = Strings.AccountInfoDialog.ACCESS_KEY_LABEL, description = Strings.AccountInfoDialog.ACCESS_KEY_DESCRIPTION, type = AField.AFieldType.STRING)
        public final static String ACCESS_KEY = Strings.AccountInfoDialog.ACCESS_KEY_LABEL;

        @AField(name = Strings.AccountInfoDialog.SECRET_KEY_LABEL, description = Strings.AccountInfoDialog.SECRET_KEY_DESCRIPTION, type = AField.AFieldType.STRING)
        public final static String SECRET_KEY = Strings.AccountInfoDialog.SECRET_KEY_LABEL;

        @AField(name = Strings.AccountInfoDialog.REGION_LABEL, description = Strings.AccountInfoDialog.REGION_DESCRIPTION, type = AField.AFieldType.STRING)
        public final static String REGION = Strings.AccountInfoDialog.REGION_LABEL;
    }
}
