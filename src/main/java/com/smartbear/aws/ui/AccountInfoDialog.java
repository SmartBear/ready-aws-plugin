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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public static final String US_WEST_OREGON = "US West (Oregon)";
    public static final String US_EAST_N_VIRGINIA = "US East (N. Virginia)";
    public static final String US_EAST_OHIO = "US East (Ohio)";

    public static final String US_WEST_N_CALIFORNIA = "US West (N. California)";
    public static final String SOUTH_AMERICA_SAO_PAULO = "South America (Sao Paulo)";
    public static final String EU_IRELAND = "EU (Ireland)";
    public static final String EU_FRANKFURT = "EU (Frankfurt)";
    public static final String ASIA_PACIFIC_TOKYO = "Asia Pacific (Tokyo)";
    public static final String ASIA_PACIFIC_SINGAPORE = "Asia Pacific (Singapore)";
    public static final String ASIA_PACIFIC_SYDNEY = "Asia Pacific (Sydney)";

    //map of the all available regions, https://aws.amazon.com/about-aws/global-infrastructure/regional-product-services/?nc1=h_ls
    private static Map<String, String> regions = new HashMap<String, String>() {{
        put(US_EAST_N_VIRGINIA, "us-east-1");
        put(US_EAST_OHIO, "us-east-2");
        put(US_WEST_OREGON, "us-west-2");
        put(US_WEST_N_CALIFORNIA, "us-west-1");
        put(SOUTH_AMERICA_SAO_PAULO, "sa-east-1");
        put(EU_IRELAND, "eu-west-1");
        put(EU_FRANKFURT, "eu-central-1");
        put(ASIA_PACIFIC_TOKYO, "ap-northeast-1");
        put(ASIA_PACIFIC_SINGAPORE, "ap-southeast-1");
        put(ASIA_PACIFIC_SYDNEY, "ap-southeast-2");
    }};

    private final XFormDialog dialog;
    private final XFormField accessKeyField;
    private final XFormField secretKeyField;
    private final XFormField regionField;
    private ApiListLoader.Result loaderResult = null;

    public AccountInfoDialog(boolean isImport) {
        this.dialog = ADialogBuilder.buildDialog(isImport ? ImportForm.class : ExportForm.class);
        this.accessKeyField = dialog.getFormField(Form.ACCESS_KEY);
        this.secretKeyField = dialog.getFormField(Form.SECRET_KEY);
        this.regionField = dialog.getFormField(Form.REGION);

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
            if (loaderResult.apis.size() == 0) {
                return new ValidationMessage[] { new ValidationMessage(Strings.Error.INVALID_REGION, regionField) };
            }
        }
        return new ValidationMessage[0];
    }

    private Result buildResult() {
        List<ApiDescription> apis = loaderResult == null ? Collections.<ApiDescription>emptyList() : loaderResult.apis;
        return new Result(accessKeyField.getValue(), secretKeyField.getValue(), regions.get(regionField.getValue()), apis);
    }

    public Result show() {
        return dialog.show() ? buildResult() : null;
    }

    @Override
    public void close() {
        dialog.release();
    }

    public interface Form {
        @AField(name = Strings.AccountInfoDialog.ACCESS_KEY_LABEL, description = Strings.AccountInfoDialog.ACCESS_KEY_DESCRIPTION, type = AField.AFieldType.STRING)
        public final static String ACCESS_KEY = Strings.AccountInfoDialog.ACCESS_KEY_LABEL;

        @AField(name = Strings.AccountInfoDialog.SECRET_KEY_LABEL, description = Strings.AccountInfoDialog.SECRET_KEY_DESCRIPTION, type = AField.AFieldType.STRING)
        public final static String SECRET_KEY = Strings.AccountInfoDialog.SECRET_KEY_LABEL;

        //see available regions at the http://docs.aws.amazon.com/general/latest/gr/rande.html
        @AField(name = Strings.AccountInfoDialog.REGION_LABEL, description = Strings.AccountInfoDialog.REGION_DESCRIPTION, type = AField.AFieldType.COMBOBOX,
            values = {
                AccountInfoDialog.US_EAST_N_VIRGINIA,
                US_WEST_OREGON,
                US_WEST_N_CALIFORNIA,
                SOUTH_AMERICA_SAO_PAULO,
                EU_IRELAND,
                EU_FRANKFURT,
                ASIA_PACIFIC_TOKYO,
                ASIA_PACIFIC_SINGAPORE,
                ASIA_PACIFIC_SYDNEY},
            defaultValue = AccountInfoDialog.US_EAST_N_VIRGINIA)

        public final static String REGION = Strings.AccountInfoDialog.REGION_LABEL;
    }

    @AForm(name = Strings.AccountInfoDialog.IMPORT_CAPTION, description = Strings.AccountInfoDialog.DESCRIPTION)
    public interface ImportForm extends Form {

    }

    @AForm(name = Strings.AccountInfoDialog.EXPORT_CAPTION, description = Strings.AccountInfoDialog.DESCRIPTION)
    public interface ExportForm extends Form {

    }
}
