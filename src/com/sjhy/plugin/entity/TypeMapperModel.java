package com.sjhy.plugin.entity;

import javax.swing.table.DefaultTableModel;

public class TypeMapperModel extends DefaultTableModel {
    private TypeMapperGroup typeMapperGroup;

    public TypeMapperModel() {
        super.addColumn("columnType");
        super.addColumn("javaType");
    }

    public void init(TypeMapperGroup typeMapperGroup) {
        this.typeMapperGroup =typeMapperGroup;
        removeAllRow();
        this.typeMapperGroup.getTypeMapperList().forEach(typeMapper -> super.addRow(new Object[]{typeMapper.getColumnType(), typeMapper.getJavaType()}));
    }

    private void removeAllRow() {
        int rowCount = getRowCount();
        if (rowCount>0){
            for (int i = 0; i< rowCount; i++) {
                super.removeRow(0);
            }
        }
    }

    @Override
    public void removeRow(int row) {
        super.removeRow(row);
        this.typeMapperGroup.getTypeMapperList().remove(row);
    }

    public void addRow(TypeMapper typeMapper) {
        super.addRow(new Object[]{typeMapper.getColumnType(), typeMapper.getJavaType()});
        this.typeMapperGroup.getTypeMapperList().add(typeMapper);
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        super.setValueAt(aValue, rowIndex, columnIndex);
        TypeMapper typeMapper = typeMapperGroup.getTypeMapperList().get(rowIndex);
        if (columnIndex==0){
            typeMapper.setColumnType((String) aValue);
        } else {
            typeMapper.setJavaType((String) aValue);
        }
    }
}
