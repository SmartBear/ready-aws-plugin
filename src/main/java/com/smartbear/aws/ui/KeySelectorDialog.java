package com.smartbear.aws.ui;

import com.eviware.soapui.support.action.swing.DefaultActionList;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;
import com.smartbear.aws.Strings;
import com.smartbear.aws.entity.ApiDescription;
import com.smartbear.aws.entity.ApiKey;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.List;

public final class KeySelectorDialog implements AutoCloseable {
    private final XFormDialog dialog;
    private final List<ApiDescription> apis;
    private final JTable apiTable;

    public KeySelectorDialog(List<ApiDescription> apis) {
        DefaultActionList actions = new DefaultActionList();
        actions.addAction(new AbstractAction("OK") {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.setVisible(false);
            }
        });

        this.dialog = ADialogBuilder.buildDialog(Form.class, actions, false);
        this.apis = apis;
        this.apiTable = new JTable(new KeyTableModel(this.apis));
        this.apiTable.setRowHeight(21);

        final JComboBox comboBox = new JComboBox(new DefaultComboBoxModel());
        TableColumn col = this.apiTable.getColumnModel().getColumn(1);
        col.setCellEditor(new KeyCellEditor(comboBox, apis));

        this.dialog.getFormField(Form.CONTAINER).setProperty("component", new JScrollPane(apiTable));
        this.dialog.getFormField(Form.CONTAINER).setProperty("preferredSize", new Dimension(500, 200));
    }

    public static void showIfNeeded(List<ApiDescription> apis) {
        if (!needKeyDialog(apis)) {
            return;
        }
        try (KeySelectorDialog dialog = new KeySelectorDialog(apis)) {
            dialog.show();
        }
    }

    public boolean show() {
        this.dialog.show();
        return true;
    }

    @Override
    public void close() {
        this.dialog.release();
    }

    private static boolean needKeyDialog(List<ApiDescription> apis) {
        for (ApiDescription api: apis) {
            if (api.getStage() == null) {
                continue;
            }
            for (ApiKey key: api.apiKeys) {
                for (String keyStage: key.stages) {
                    if (keyStage.endsWith("/" + api.getStage().name)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @AForm(name = Strings.KeySelectorDialog.CAPTION, description = Strings.KeySelectorDialog.DESCRIPTION, helpUrl = Strings.REPOSITORY_URL)
    public interface Form {
        @AField(name = "###Container", description = "", type = AField.AFieldType.COMPONENT)
        public final static String CONTAINER = "###Container";
    }
}
