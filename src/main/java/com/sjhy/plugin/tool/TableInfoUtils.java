package com.sjhy.plugin.tool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.database.model.DasColumn;
import com.intellij.database.psi.DbTable;
import com.intellij.database.util.DasUtil;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.ExceptionUtil;
import com.intellij.util.containers.JBIterable;
import com.sjhy.plugin.comm.AbstractService;
import com.sjhy.plugin.constants.MsgValue;
import com.sjhy.plugin.entity.ColumnInfo;
import com.sjhy.plugin.entity.TableInfo;
import com.sjhy.plugin.entity.TypeMapper;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 表信息工具类
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/17 13:10
 */
public class TableInfoUtils extends AbstractService {
    private volatile static TableInfoUtils tableInfoUtils;

    /**
     * 单例模式
     */
    public static TableInfoUtils getInstance() {
        if (tableInfoUtils == null) {
            synchronized (TableInfoUtils.class) {
                if (tableInfoUtils == null) {
                    tableInfoUtils = new TableInfoUtils();
                }
            }
        }
        return tableInfoUtils;
    }

    /**
     * 私有构造方法
     */
    private TableInfoUtils() {
    }

    /**
     * 命名工具类
     */
    private NameUtils nameUtils = NameUtils.getInstance();
    /**
     * 缓存数据工具类
     */
    private CacheDataUtils cacheDataUtils = CacheDataUtils.getInstance();
    /**
     * jackson格式化工具
     */
    private ObjectMapper objectMapper = new ObjectMapper();
    /**
     * 文件工具类
     */
    private FileUtils fileUtils = FileUtils.getInstance();

    /**
     * 保存的相对路径
     */
    private static final String SAVE_PATH = "/.idea/EasyCodeConfig";

    /**
     * 处理方法，默认加载配置
     *
     * @param dbTables 数据库表信息
     * @return 表信息
     */
    public List<TableInfo> handler(Collection<DbTable> dbTables) {
        return handler(dbTables, true);
    }

    /**
     * 数据库表处理器
     *
     * @param dbTables   数据库表
     * @param loadConfig 是否加载配置信息
     * @return 处理结果
     */
    private List<TableInfo> handler(Collection<DbTable> dbTables, boolean loadConfig) {
        List<TableInfo> result = new ArrayList<>();
        dbTables.forEach(dbTable -> {
            TableInfo tableInfo = new TableInfo();
            // 设置原属对象
            tableInfo.setObj(dbTable);
            // 设置类名
            tableInfo.setName(nameUtils.getClassName(dbTable.getName()));
            // 设置注释
            tableInfo.setComment(dbTable.getComment());
            // 设置所有列
            tableInfo.setFullColumn(new ArrayList<>());
            // 设置主键列
            tableInfo.setPkColumn(new ArrayList<>());
            // 设置其他列
            tableInfo.setOtherColumn(new ArrayList<>());
            // 处理所有列
            JBIterable<? extends DasColumn> columns = DasUtil.getColumns(dbTable);
            for (DasColumn column : columns) {
                ColumnInfo columnInfo = new ColumnInfo();
                // 原始列对象
                columnInfo.setObj(column);
                // 列类型
                columnInfo.setType(getColumnType(column.getDataType().getSpecification()));
                // 列名
                columnInfo.setName(nameUtils.getJavaName(column.getName()));
                // 列注释
                columnInfo.setComment(column.getComment());
                // 添加到全部列
                tableInfo.getFullColumn().add(columnInfo);
                // 主键列添加到主键列，否则添加到其他列
                if (DasUtil.isPrimary(column)) {
                    tableInfo.getPkColumn().add(columnInfo);
                } else {
                    tableInfo.getOtherColumn().add(columnInfo);
                }
            }
            // 判断是否加载配置信息
            if (!loadConfig) {
                result.add(tableInfo);
                return;
            }
            // 读取附加数据，并进行合并
            TableInfo tableInfoConfig = readConfig(dbTable);
            if (tableInfoConfig == null) {
                result.add(tableInfo);
                return;
            }
            tableInfo.setSaveModelName(tableInfoConfig.getSaveModelName());
            tableInfo.setSavePackageName(tableInfoConfig.getSavePackageName());
            tableInfo.setSavePath(tableInfoConfig.getSavePath());


            if (CollectionUtil.isEmpty(tableInfoConfig.getFullColumn())) {
                result.add(tableInfo);
                return;
            }
            for (int i = 0; i < tableInfoConfig.getFullColumn().size(); i++) {
                ColumnInfo columnInfo = tableInfoConfig.getFullColumn().get(i);
                //自定义附加列
                if (i > tableInfo.getFullColumn().size() - 1) {
                    tableInfo.getFullColumn().add(columnInfo);
                    tableInfo.getOtherColumn().add(columnInfo);
                    continue;
                }
                // 覆盖类型信息
                if (columnInfo.getType() != null) {
                    tableInfo.getFullColumn().get(i).setType(columnInfo.getType());
                }
                // 覆盖注释信息
                if (columnInfo.getComment() != null) {
                    tableInfo.getFullColumn().get(i).setComment(columnInfo.getComment());
                }
                // 添加扩展信息
                tableInfo.getFullColumn().get(i).setExt(columnInfo.getExt());
            }
            result.add(tableInfo);
        });
        return result;
    }

    /**
     * 通过映射获取对应的java类型类型名称
     *
     * @param typeName 列类型
     * @return java类型
     */
    private String getColumnType(String typeName) {
        for (TypeMapper typeMapper : getCurrMapper().getElementList()) {
            // 不区分大小写进行类型转换
            if (Pattern.compile(typeMapper.getColumnType(), Pattern.CASE_INSENSITIVE).matcher(typeName).matches()) {
                return typeMapper.getJavaType();
            }
        }
        //弹出消息框
        Messages.showWarningDialog("发现未知类型：" + typeName, MsgValue.TITLE_INFO);
        return "java.lang.Object";
    }


    /**
     * 保存数据
     * @param tableInfo 表信息对象
     */
    public void save(TableInfo tableInfo) {
        //排除部分字段，这些字段不进行保存
        tableInfo.setOtherColumn(null);
        tableInfo.setPkColumn(null);
        //获取原数据
        TableInfo oldTableInfo = handler(Collections.singletonList(tableInfo.getObj()), false).get(0);
        //将原始对象置空
        tableInfo.setObj(null);
        //将一致的原数据置空，保证数据的动态修改
        for (int i = 0; i < oldTableInfo.getFullColumn().size(); i++) {
            ColumnInfo columnInfo = oldTableInfo.getFullColumn().get(i);
            ColumnInfo newColumn = tableInfo.getFullColumn().get(i);
            // 类型排除
            if (Objects.equals(columnInfo.getType(), newColumn.getType())) {
                newColumn.setType(null);
            }
            // 注释排除
            if (Objects.equals(columnInfo.getComment(), newColumn.getComment())) {
                newColumn.setComment(null);
            }
            //将原始对象置空
            newColumn.setObj(null);
        }
        // 获取优雅格式的JSON字符串
        String content = null;
        try {
            content = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(tableInfo);
        } catch (JsonProcessingException e) {
            ExceptionUtil.rethrow(e);
        }
        if (content == null) {
            Messages.showWarningDialog("保存失败，JSON序列化错误。", MsgValue.TITLE_INFO);
            return;
        }
        // 获取或创建保存目录
        String path = cacheDataUtils.getProject().getBasePath() + SAVE_PATH;
        File dir = new File(path);
        if (!dir.exists()) {
            if (!dir.mkdir()) {
                Messages.showWarningDialog("保存失败，无法创建目录。", MsgValue.TITLE_INFO);
                return;
            }
        }
        // 获取保存名称
        String schemaName = DasUtil.getSchema(oldTableInfo.getObj());
        String fileName = schemaName + "-" + oldTableInfo.getObj().getName() + ".json";
        File file = new File(dir, fileName);
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    Messages.showWarningDialog("保存失败，无法创建文件。", MsgValue.TITLE_INFO);
                    return;
                }
            } catch (IOException e) {
                ExceptionUtil.rethrow(e);
                Messages.showWarningDialog("保存失败，创建文件异常。", MsgValue.TITLE_INFO);
                return;
            }
        }
        //写入配置文件
        fileUtils.write(file, content);
        // 同步刷新
        VirtualFileManager.getInstance().syncRefresh();
    }

    /**
     * 读取配置文件
     * @param dbTable 表对象信息
     * @return 读取到的配置信息
     */
    private TableInfo readConfig(DbTable dbTable) {
        // 获取保存的目录
        String path = cacheDataUtils.getProject().getBasePath() + SAVE_PATH;
        File dir = new File(path);
        if (!dir.exists()) {
            if (!dir.mkdir()) {
                return null;
            }
        }
        // 获取保存的文件
        String schemaName = DasUtil.getSchema(dbTable);
        String fileName = schemaName + "-" + dbTable.getName() + ".json";
        File file = new File(dir, fileName);
        if (!file.exists()) {
            return null;
        }
        // 读取并解析文件
        return parser(fileUtils.read(file));
    }

    /**
     * 对象还原
     * @param str 原始JSON字符串
     * @return 解析结果
     */
    private TableInfo parser(String str) {
        try {
            return objectMapper.readValue(str, TableInfo.class);
        } catch (IOException e) {
            ExceptionUtil.rethrow(e);
            Messages.showWarningDialog("读取配置失败，JSON反序列化异常。", MsgValue.TITLE_INFO);
        }
        return null;
    }
}
