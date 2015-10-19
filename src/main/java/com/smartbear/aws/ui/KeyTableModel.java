package com.smartbear.aws.ui;

import com.smartbear.aws.entity.ApiDescription;
import com.smartbear.aws.entity.ApiKey;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.util.List;

public final class KeyTableModel implements TableModel {
    private final List<ApiDescription> apis;

    public KeyTableModel(List<ApiDescription> apis) {
        this.apis = apis;
    }

    @Override
    public int getRowCount() {
        return apis.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public String getColumnName(int columnIndex) {
        if (columnIndex == 0)
            return "Name";
        else if (columnIndex == 1)
            return "ApiKey";
        else
            return null;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0) {
            return String.class;
        } else if (columnIndex == 1) {
            return ApiKey.class;
        } else {
            return null;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 1 && apis.get(rowIndex).getSelectedStageKeys().size() > 0;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0)
            return apis.get(rowIndex).name;
        else if (columnIndex == 1)
            return apis.get(rowIndex).getApiKey();
        else
            return null;

    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex == 1 && aValue instanceof ApiKey) {
            apis.get(rowIndex).setApiKey((ApiKey) aValue);
        }
    }

    @Override
    public void addTableModelListener(TableModelListener l) {

    }

    @Override
    public void removeTableModelListener(TableModelListener l) {

    }
}
