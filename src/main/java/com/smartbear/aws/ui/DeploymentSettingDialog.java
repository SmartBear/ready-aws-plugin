package com.smartbear.aws.ui;

import com.eviware.soapui.support.StringUtils;
import com.eviware.x.form.ValidationMessage;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldListener;
import com.eviware.x.form.XFormFieldValidator;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;
import com.eviware.x.impl.swing.JComboBoxFormField;
import com.smartbear.aws.DeploymentSetting;
import com.smartbear.aws.Helper;
import com.smartbear.aws.Strings;

public final class DeploymentSettingDialog implements AutoCloseable, XFormFieldValidator {
    private final String[] endpointsValues;
    private final XFormDialog dialog;
    private final XFormField proxyIntegration;
    private final XFormField endPoints;
    private final XFormField defaultResponse;

    public DeploymentSettingDialog(String[] endpoints) {
        this.endpointsValues = endpoints;
        this.dialog = ADialogBuilder.buildDialog(Form.class);
        this.proxyIntegration = this.dialog.getFormField(Form.PROXY_INTEGRATION);
        this.endPoints = this.dialog.getFormField(Form.END_POINTS);
        this.defaultResponse = this.dialog.getFormField(Form.DEFAULT_RESPONSE);
        this.defaultResponse.setValue("200");

        if (this.endPoints instanceof JComboBoxFormField) {
            ((JComboBoxFormField) this.endPoints).setOptions(endpoints);
        }

        this.proxyIntegration.addFormFieldListener(new XFormFieldListener() {
            @Override
            public void valueChanged(XFormField xFormField, String s, String s1) {
                boolean enabled = proxyIntegrationEnabled();
                endPoints.setEnabled(enabled);
                defaultResponse.setEnabled(enabled);
            }
        });

        this.endPoints.addFormFieldValidator(this);
        this.defaultResponse.addFormFieldValidator(this);
    }

    public DeploymentSetting show() {
        if (!this.dialog.show())
            return null;
        else if (proxyIntegrationEnabled())
            return DeploymentSetting.proxyIntegration(endPoints.getValue(), defaultResponse.getValue());
        else
            return DeploymentSetting.withoutIntegration();
    }

    @Override
    public void close() {
        this.dialog.release();
    }

    @Override
    public ValidationMessage[] validateField(XFormField xFormField) {
        if (proxyIntegrationEnabled()) {
            String value = xFormField.getValue();
            if (xFormField == this.endPoints) {
                if (endpointsValues.length == 0) {
                    return new ValidationMessage[] {new ValidationMessage(Strings.Error.ABSENT_ENDPOINT, xFormField)};
                }
                if (StringUtils.isNullOrEmpty(value)) {
                    return new ValidationMessage[] {new ValidationMessage(Strings.Error.EMPTY_ENDPOINT, xFormField)};
                }
                if (Helper.stringToUrl(value) == null) {
                    return new ValidationMessage[] {new ValidationMessage(Strings.Error.INVALID_ENDPOINT_FORMAT, xFormField)};
                }
            } else if (xFormField == this.defaultResponse) {
                if (StringUtils.isNullOrEmpty(value)) {
                    return new ValidationMessage[] {new ValidationMessage(Strings.Error.EMPTY_DEFAULT_RESPONSE, xFormField)};
                }
                try {
                    int intValue = Integer.parseInt(value);
                    System.out.println(intValue);
                } catch (NumberFormatException ex) {
                    return new ValidationMessage[] {new ValidationMessage(Strings.Error.INVALID_DEFAULT_RESPONSE, xFormField)};
                }
            }
        }
        return new ValidationMessage[0];
    }

    private final boolean proxyIntegrationEnabled() {
        return Boolean.parseBoolean(proxyIntegration.getValue());
    }

    @AForm(name = Strings.DeploymentSettingDialog.NAME, description = Strings.DeploymentSettingDialog.DESCRIPTION)
    public interface Form {
        @AField(name = Strings.DeploymentSettingDialog.PROXY_INTEGRATION_LABEL, description = "", type = AField.AFieldType.BOOLEAN)
        public final static String PROXY_INTEGRATION = Strings.DeploymentSettingDialog.PROXY_INTEGRATION_LABEL;

        @AField(description = Strings.DeploymentSettingDialog.INTEGRATION_OPTIONS, type = AField.AFieldType.SEPARATOR)
        public final static String SEPARATOR = "Separator";

        @AField(name = Strings.DeploymentSettingDialog.ENDPOINTS_LABEL, description = Strings.DeploymentSettingDialog.ENDPOINTS_DESCRIPTION,
                type = AField.AFieldType.COMBOBOX, enabled = false)
        public final static String END_POINTS = Strings.DeploymentSettingDialog.ENDPOINTS_LABEL;

        @AField(name = Strings.DeploymentSettingDialog.DEFAULT_RESPONSE_LABEL, description = Strings.DeploymentSettingDialog.DEFAULT_RESPONSE_DESCRIPTION,
                type = AField.AFieldType.INT, enabled = false)
        public final static String DEFAULT_RESPONSE = Strings.DeploymentSettingDialog.DEFAULT_RESPONSE_LABEL;
    }
}
