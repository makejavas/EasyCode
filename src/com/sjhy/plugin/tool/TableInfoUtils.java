package com.sjhy.plugin.tool;

import com.intellij.database.model.DasColumn;
import com.intellij.database.psi.DbTable;
import com.intellij.database.util.DasUtil;
import com.intellij.util.containers.JBIterable;
import com.sjhy.plugin.comm.ServiceComm;
import com.sjhy.plugin.entity.ColumnInfo;
import com.sjhy.plugin.entity.TableInfo;
import com.sjhy.plugin.entity.TypeMapper;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

public class TableInfoUtils extends ServiceComm {
    //单例模式
    private static class Instance {
        private static final TableInfoUtils ME = new TableInfoUtils();
    }
    public static TableInfoUtils getInstance() {
        return Instance.ME;
    }
    private TableInfoUtils(){}
    //注入命名工具类
    private NameUtils nameUtils = NameUtils.getInstance();

    /**
     * 数据库表处理器
     * @param dbTables 数据库表
     * @return 处理结果
     */
    public List<TableInfo> handler(Collection<DbTable> dbTables) {
        List<TableInfo> result = new ArrayList<>();
        dbTables.forEach(dbTable -> {
            TableInfo tableInfo = new TableInfo();
            tableInfo.setObj(dbTable);
            tableInfo.setName(nameUtils.firstUpperCase(nameUtils.getJavaName(dbTable.getName().toLowerCase())));
            tableInfo.setComment(dbTable.getComment());
            tableInfo.setFullColumn(new ArrayList<>());
            tableInfo.setPkColumn(new ArrayList<>());
            tableInfo.setOtherColumn(new ArrayList<>());
            JBIterable<? extends DasColumn> columns = DasUtil.getColumns(dbTable);
            for (DasColumn column : columns) {
                ColumnInfo columnInfo = new ColumnInfo();
                columnInfo.setObj(column);
                columnInfo.setType(getColumnType(column.getDataType().getSpecification()));
                columnInfo.setName(nameUtils.getJavaName(column.getName().toLowerCase()));
                columnInfo.setComment(column.getComment());
                tableInfo.getFullColumn().add(columnInfo);
                if(DasUtil.isPrimary(column)){
                    tableInfo.getPkColumn().add(columnInfo);
                }else{
                    tableInfo.getOtherColumn().add(columnInfo);
                }
            }
            result.add(tableInfo);
        });
        return result;
    }

    /**
     * 通过映射获取对应的java类型类型名称
     * @param typeName 列类型
     * @return java类型
     */
    private String getColumnType(String typeName) {
        for (TypeMapper typeMapper : getCurrMapper().getElementList()) {
            if (Pattern.compile(typeMapper.getColumnType(), Pattern.CASE_INSENSITIVE).matcher(typeName).matches()) {
                return typeMapper.getJavaType();
            }
        }
        //弹出消息框
        JOptionPane.showMessageDialog(null, "未知类型："+typeName, "温馨提示", JOptionPane.PLAIN_MESSAGE);
        return "java.lang.Object";
    }
}
