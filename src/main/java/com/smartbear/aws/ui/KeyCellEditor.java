package com.smartbear.aws.ui;

import com.smartbear.aws.entity.ApiDescription;
import com.smartbear.aws.entity.ApiKey;

import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import java.awt.Component;
import java.util.List;

public class KeyCellEditor extends AbstractCellEditor implements TableCellEditor {

    private final JComboBox comboBox;
    private final List<ApiDescription> apis;

    public KeyCellEditor(JComboBox comboBox, List<ApiDescription> apis) {
        this.comboBox = comboBox;
        this.apis = apis;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        comboBox.removeAllItems();
        ApiDescription api = this.apis.get(row);
        for (ApiKey key: api.getSelectedStageKeys()) {
            comboBox.addItem(key);
        }
        comboBox.setSelectedItem(value);
        return comboBox;
    }

    @Override
    public Object getCellEditorValue() {
        return comboBox.getSelectedItem();
    }
}