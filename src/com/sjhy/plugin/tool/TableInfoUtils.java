package com.sjhy.plugin.tool;

import com.intellij.codeInsight.actions.ReformatCodeAction;
import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.database.model.DasColumn;
import com.intellij.database.psi.DbTable;
import com.intellij.database.util.DasUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.containers.JBIterable;
import com.sjhy.plugin.comm.ServiceComm;
import com.sjhy.plugin.entity.ColumnInfo;
import com.sjhy.plugin.entity.TableInfo;
import com.sjhy.plugin.entity.TypeMapper;
import groovy.json.JsonOutput;
import groovy.json.internal.JsonParserCharArray;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
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
    private CacheDataUtils cacheDataUtils = CacheDataUtils.getInstance();
    private JsonParserCharArray jsonParser = new JsonParserCharArray();
    private FileUtils fileUtils = FileUtils.getInstance();

    public List<TableInfo> handler(Collection<DbTable> dbTables) {
        return handler(dbTables, true);
    }

    /**
     * 数据库表处理器
     * @param dbTables 数据库表
     * @return 处理结果
     */
    private List<TableInfo> handler(Collection<DbTable> dbTables, boolean loadConfig) {
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
            if (!loadConfig) {
                result.add(tableInfo);
                return;
            }
            //附加数据
            TableInfo tableInfoConfig = readConfig(dbTable);
            if (tableInfoConfig!=null && tableInfoConfig.getFullColumn()!=null) {
                for (int i=0; i<tableInfoConfig.getFullColumn().size();i++) {
                    ColumnInfo columnInfo = tableInfoConfig.getFullColumn().get(i);
                    //自定义附加列
                    if (i>tableInfo.getFullColumn().size()-1){
                        tableInfo.getFullColumn().add(columnInfo);
                        continue;
                    }
                    if (columnInfo.getType()!=null) {
                        tableInfo.getFullColumn().get(i).setType(columnInfo.getType());
                    }
                    if (columnInfo.getComment()!=null) {
                        tableInfo.getFullColumn().get(i).setComment(columnInfo.getComment());
                    }
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


    //保存数据
    public void save(TableInfo tableInfo) {
        //排除部分字段
        tableInfo.setOtherColumn(null);
        tableInfo.setPkColumn(null);
        //将对象置空
        tableInfo.setObj(null);
        //获取原数据
        TableInfo oldTableInfo = handler(Collections.singletonList(cacheDataUtils.getSelectDbTable()), false).get(0);
        //将一致的原数据置空，保证数据的动态修改
        for (int i = 0; i < oldTableInfo.getFullColumn().size(); i++) {
            ColumnInfo columnInfo = oldTableInfo.getFullColumn().get(i);
            ColumnInfo newColumn = tableInfo.getFullColumn().get(i);
            if (columnInfo.getType()!=null && columnInfo.getType().equals(newColumn.getType())) {
                newColumn.setType(null);
            }
            if (columnInfo.getComment()!=null && columnInfo.getComment().equals(newColumn.getComment())) {
                newColumn.setComment(null);
            }
            //将对象置空
            newColumn.setObj(null);
        }
        String content = JsonOutput.toJson(tableInfo);
        ReformatCodeAction.containsAtLeastOneFile(null);
        String path = cacheDataUtils.getProject().getBasePath()+"/.idea/EasyCodeConfig";
        File dir = new File(path);
        if (!dir.exists()){
            if(!dir.mkdir()){
                return;
            }
        }
        String schemaName = DasUtil.getSchema(oldTableInfo.getObj());
        String fileName = schemaName+"-"+oldTableInfo.getObj().getName()+".json";
        File file = new File(dir, fileName);
        if (!file.exists()){
            try {
                if (!file.createNewFile()){
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
        //写入配置文件
        fileUtils.write(file, content);
        VirtualFile virtualFile = VirtualFileManager.getInstance().findFileByUrl("file://"+file.getAbsolutePath());
        if (virtualFile==null){
            return;
        }
        PsiFile psiFile = PsiManager.getInstance(cacheDataUtils.getProject()).findFile(virtualFile);
        //格式化配置文件
        new ReformatCodeProcessor(cacheDataUtils.getProject(), psiFile, null, false).run();
        VirtualFileManager.getInstance().syncRefresh();
    }

    private TableInfo readConfig(DbTable dbTable) {
        String path  = cacheDataUtils.getProject().getBasePath()+"/.idea/EasyCodeConfig";
        File dir = new File(path);
        if (!dir.exists()){
            if(!dir.mkdir()){
                return null;
            }
        }
        String schemaName = DasUtil.getSchema(dbTable);
        String fileName = schemaName+"-"+dbTable.getName()+".json";
        File file = new File(dir, fileName);
        if (!file.exists()){
            return null;
        }
        return parser(fileUtils.read(file));
    }

    //对象还原
    @SuppressWarnings("unchecked")
    private TableInfo parser(String str) {
        Map<String,Object> map = (Map<String, Object>) jsonParser.parse(str);
        TableInfo tableInfo = new TableInfo();
        tableInfo.setName((String) map.get("name"));
        tableInfo.setComment((String) map.get("comment"));
        List<Map<String, Object>> fullColumn = (List<Map<String, Object>>) map.get("fullColumn");
        if (fullColumn==null) {
            return tableInfo;
        }
        List<ColumnInfo> columnInfoList = new ArrayList<>();
        tableInfo.setFullColumn(columnInfoList);
        for (Map<String,Object> column : fullColumn) {
            ColumnInfo columnInfo = new ColumnInfo();
            columnInfo.setName((String) column.get("name"));
            columnInfo.setType((String) column.get("type"));
            columnInfo.setComment((String) column.get("comment"));
            columnInfo.setExt((Map<String, Object>) column.get("ext"));
            columnInfoList.add(columnInfo);
        }
        return tableInfo;
    }
}
