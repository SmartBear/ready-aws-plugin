package com.smartbear.aws.ui;

import com.eviware.x.form.ValidationMessage;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldValidator;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;
import com.smartbear.aws.Strings;
import com.smartbear.aws.entity.ApiDescription;
import com.smartbear.rapisupport.Service;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class ApiSelectorDialog implements AutoCloseable {

    private final XFormDialog dialog;
    private final List<ApiDescription> apis;
    private final JTable apiTable;

    public ApiSelectorDialog(List<ApiDescription> apiList) {
        this.dialog = ADialogBuilder.buildDialog(Form.class);
        this.apis = apiList;
        this.apiTable = new JTable(new ApiTableModel(this.apis));

        final JComboBox comboBox = new JComboBox(new DefaultComboBoxModel());
        TableColumn col = this.apiTable.getColumnModel().getColumn(1);
        col.setCellEditor(new StageCellEditor(comboBox, apis));

        this.apiTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int[] rows = apiTable.getSelectedRows();
                dialog.setValue(Form.DESCRIPTION, rows.length == 1 ? apis.get(rows[0]).description : "");
            }
        });

        this.dialog.getFormField(Form.NAME).setProperty("component", new JScrollPane(apiTable));
        this.dialog.getFormField(Form.NAME).setProperty("preferredSize", new Dimension(500, 150));
        this.dialog.getFormField(Form.DESCRIPTION).setProperty("preferredSize", new Dimension(500, 50));
        this.dialog.setValue(Form.DESCRIPTION, null);
        this.dialog.setValue(Form.SPEC, null);

        this.dialog.getFormField(Form.NAME).addFormFieldValidator(new XFormFieldValidator() {
            @Override
            public ValidationMessage[] validateField(XFormField xFormField) {
                int[] selected = apiTable.getSelectedRows();
                if (selected == null || selected.length == 0) {
                    return new ValidationMessage[]{new ValidationMessage(Strings.SelectApiDialog.NOTHING_SELECTED_WARNING, xFormField)};
                } else {
                    return new ValidationMessage[0];
                }
            }
        });
    }

    @Override
    public void close() {
        dialog.release();
    }

    public Result show() {
        return dialog.show() ? new Result() : null;
    }

    public class Result {
        //public final String projectName;
        public final List<ApiDescription> selectedAPIs;
        public final Set<Service> entities = EnumSet.noneOf(Service.class);

        public Result() {
            selectedAPIs = getSelected();

            //XFormField name = dialog.getFormField(NewProjectForm.PROJECT_NAME);
            //projectName = name != null ? name.getValue() : null;

            if (dialog.getBooleanValue(Form.TEST_SUITE)) {
                entities.add(Service.TEST_SUITE);
            }
            if (dialog.getBooleanValue(Form.LOAD_TEST)) {
                entities.add(Service.LOAD_TEST);
            }
            if (dialog.getBooleanValue(Form.VIRT)) {
                entities.add(Service.VIRT);
            }
            if (dialog.getBooleanValue(Form.SECUR_TEST)) {
                entities.add(Service.SECUR_TEST);
            }
        }

        private List<ApiDescription> getSelected() {
            List<ApiDescription> result = new ArrayList<>();
            int[] selected = apiTable.getSelectedRows();
            for (int index: selected) {
                result.add(apis.get(index));
            }
            return result;
        }
    }


    @AForm(name = Strings.SelectApiDialog.CAPTION, description = Strings.SelectApiDialog.DESCRIPTION)
    public interface Form {
        @AField(description = Strings.SelectApiDialog.NAME_LABEL, type = AField.AFieldType.COMPONENT)
        public final static String NAME = "Name";

        @AField(description = Strings.SelectApiDialog.DESCRIPTION_LABEL, type = AField.AFieldType.INFORMATION)
        public final static String DESCRIPTION = "Description";

        @AField(description = Strings.SelectApiDialog.DEFINITION_LABEL, type = AField.AFieldType.LABEL)
        public final static String SPEC = "Definition";

        @AField(description = "", type = AField.AFieldType.SEPARATOR)
        public final static String SEPERATOR = "Separator";

        @AField(name = "###GenerateTestSuite", description = Strings.SelectApiDialog.GEN_TEST_SUITE, type = AField.AFieldType.BOOLEAN)
        public final static String TEST_SUITE = "###GenerateTestSuite";

        @AField(name = "###GenerateLoadTest", description = Strings.SelectApiDialog.GEN_LOAD_TEST, type = AField.AFieldType.BOOLEAN)
        public final static String LOAD_TEST = "###GenerateLoadTest";

        @AField(name = "###GenerateSecurTest", description = Strings.SelectApiDialog.GEN_SECUR_TEST, type = AField.AFieldType.BOOLEAN)
        public final static String SECUR_TEST = "###GenerateSecurTest";

        @AField(name = "###GenerateVirt", description = Strings.SelectApiDialog.GEN_VIRT_HOST, type = AField.AFieldType.BOOLEAN)
        public final static String VIRT = "###GenerateVirt";
    }
}
