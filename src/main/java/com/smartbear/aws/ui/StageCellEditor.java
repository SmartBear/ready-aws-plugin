package com.smartbear.aws.ui;

import com.smartbear.aws.entity.ApiDescription;
import com.smartbear.aws.entity.Stage;

import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import java.awt.Component;
import java.util.List;

public class StageCellEditor extends AbstractCellEditor implements TableCellEditor {

    private final JComboBox comboBox;
    private final List<ApiDescription> apis;

    public StageCellEditor(JComboBox comboBox, List<ApiDescription> apis) {
        this.comboBox = comboBox;
        this.apis = apis;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        comboBox.removeAllItems();
        for (Stage stage: this.apis.get(row).stages) {
            comboBox.addItem(stage);
        }
        comboBox.setSelectedItem(value);
        return comboBox;
    }

    @Override
    public Object getCellEditorValue() {
        return comboBox.getSelectedItem();
    }
}
