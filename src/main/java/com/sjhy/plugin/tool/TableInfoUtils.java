package com.sjhy.plugin.tool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.database.model.DasColumn;
import com.intellij.database.psi.DbTable;
import com.intellij.database.util.DasUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.ExceptionUtil;
import com.intellij.util.containers.JBIterable;
import com.sjhy.plugin.comm.AbstractService;
import com.sjhy.plugin.constants.MsgValue;
import com.sjhy.plugin.entity.ColumnInfo;
import com.sjhy.plugin.entity.TableInfo;
import com.sjhy.plugin.entity.TypeMapper;

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
     * 将DbTable转换成TableInfo
     *
     * @param dbTables 原始数据对象
     * @param title    是否提示未知类型
     * @return 处理后的数据
     */
    public List<TableInfo> toTableInfo(Collection<DbTable> dbTables, boolean title) {
        if (CollectionUtil.isEmpty(dbTables)) {
            //noinspection unchecked
            return Collections.EMPTY_LIST;
        }
        // 定义结果
        List<TableInfo> tableInfoList = new ArrayList<>();
        // 处理
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
                columnInfo.setType(getColumnType(column.getDataType().getSpecification(), title));
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
            tableInfoList.add(tableInfo);
        });
        return tableInfoList;
    }

    /**
     * 加载配置(用户自定义列与扩张选项)
     *
     * @param tableInfoList 原始数据对象
     * @param project       项目对象
     * @return 处理后的数据
     */
    public List<TableInfo> loadConfig(List<TableInfo> tableInfoList, Project project) {
        if (CollectionUtil.isEmpty(tableInfoList)) {
            return tableInfoList;
        }
        tableInfoList.forEach(tableInfo -> {
            // 读取附加数据，并进行合并
            TableInfo tableInfoConfig = read(tableInfo, project);
            // 返回空直接不处理
            if (tableInfoConfig == null) {
                return;
            }
            // 选择模型名称
            tableInfo.setSaveModelName(tableInfoConfig.getSaveModelName());
            // 选择的包名
            tableInfo.setSavePackageName(tableInfoConfig.getSavePackageName());
            // 选择的保存路径
            tableInfo.setSavePath(tableInfoConfig.getSavePath());

            // 没有列时不处理
            if (CollectionUtil.isEmpty(tableInfoConfig.getFullColumn())) {
                return;
            }

            int fullSize = tableInfoConfig.getFullColumn().size();
            // 所有列
            List<ColumnInfo> fullColumn = new ArrayList<>(fullSize);
            int pkSize = tableInfo.getPkColumn().size();
            // 主键列
            List<ColumnInfo> pkColumn = new ArrayList<>(pkSize);
            // 其他列
            List<ColumnInfo> otherColumn = new ArrayList<>(fullSize - pkSize);

            // 列信息合并
            Iterator<ColumnInfo> configColumnIterator = tableInfoConfig.getFullColumn().iterator();
            Iterator<ColumnInfo> columnIterator = tableInfo.getFullColumn().iterator();
            while (configColumnIterator.hasNext()) {
                ColumnInfo configColumn = configColumnIterator.next();
                boolean exists = false;
                while (columnIterator.hasNext()) {
                    ColumnInfo column = columnIterator.next();
                    if (!Objects.equals(configColumn.getName(), column.getName())) {
                        continue;
                    }
                    // 覆盖空列
                    if (configColumn.getType() == null) {
                        configColumn.setType(column.getType());
                    }
                    if (configColumn.getComment() == null) {
                        configColumn.setComment(column.getComment());
                    }
                    // 列对象覆盖
                    configColumn.setObj(column.getObj());

                    // 添加自定义列
                    fullColumn.add(configColumn);
                    // 是否为主键
                    if (DasUtil.isPrimary(configColumn.getObj())) {
                        pkColumn.add(configColumn);
                    } else {
                        otherColumn.add(configColumn);
                    }
                    exists = true;
                    break;
                }
                if (exists) {
                    continue;
                }
                // 添加自定义列
                fullColumn.add(configColumn);
            }

            // 基本配置覆盖
            tableInfo.setSaveModelName(tableInfoConfig.getSaveModelName());
            tableInfo.setSavePackageName(tableInfoConfig.getSavePackageName());
            tableInfo.setSavePath(tableInfoConfig.getSavePath());

            // 全部覆盖
            tableInfo.setFullColumn(fullColumn);
            tableInfo.setPkColumn(pkColumn);
            tableInfo.setOtherColumn(otherColumn);
        });
        return tableInfoList;
    }

    /**
     * 加载表配置
     * @param dbTables 原始表对象
     * @param project 项目对象
     * @param title 是否提示未知类型
     * @return 表信息对象
     */
    public List<TableInfo> loadTableInfo(Collection<DbTable> dbTables, Project project, boolean title) {
        return loadConfig(toTableInfo(dbTables, title), project);
    }

    /**
     * 通过映射获取对应的java类型类型名称
     *
     * @param typeName 列类型
     * @param title    是否提示未知类型
     * @return java类型
     */
    private String getColumnType(String typeName, boolean title) {
        for (TypeMapper typeMapper : getCurrMapper().getElementList()) {
            // 不区分大小写进行类型转换
            if (Pattern.compile(typeMapper.getColumnType(), Pattern.CASE_INSENSITIVE).matcher(typeName).matches()) {
                return typeMapper.getJavaType();
            }
        }
        if (title) {
            //弹出消息框
            Messages.showWarningDialog("发现未知类型：" + typeName, MsgValue.TITLE_INFO);
        }
        return "java.lang.Object";
    }


    /**
     * 保存数据
     *
     * @param tableInfo 表信息对象
     * @param project   项目对象
     */
    public void save(TableInfo tableInfo, Project project) {
        // 获取未修改前的原数据
        TableInfo oldTableInfo = toTableInfo(Collections.singletonList(tableInfo.getObj()), false).get(0);
        // 克隆对象，防止串改，同时原始对象丢失
        tableInfo = CloneUtils.getInstance().clone(tableInfo);
        //排除部分字段，这些字段不进行保存
        tableInfo.setOtherColumn(null);
        tableInfo.setPkColumn(null);
        // 获取迭代器
        Iterator<ColumnInfo> oldColumnIterable = oldTableInfo.getFullColumn().iterator();
        Iterator<ColumnInfo> columnIterable = tableInfo.getFullColumn().iterator();
        while (columnIterable.hasNext()) {
            // 新列
            ColumnInfo columnInfo = columnIterable.next();
            // 是否存在
            boolean exists = false;
            while (oldColumnIterable.hasNext()) {
                ColumnInfo oldColumnInfo = oldColumnIterable.next();
                // 不同列直接返回跳过
                if (!Objects.equals(oldColumnInfo.getName(), oldColumnInfo.getName())) {
                    continue;
                }
                // 类型排除
                if (Objects.equals(columnInfo.getType(), oldColumnInfo.getType())) {
                    columnInfo.setType(null);
                }
                // 注释排除
                if (Objects.equals(columnInfo.getComment(), oldColumnInfo.getComment())) {
                    columnInfo.setComment(null);
                }
                // 列存在，进行处理
                exists = true;
                break;
            }
            // 已经不存在的非自定义列直接删除
            if (!exists && !columnInfo.isCustom()) {
                columnIterable.remove();
            }
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
        String path = project.getBasePath() + SAVE_PATH;
        File dir = new File(path);
        if (!dir.exists()) {
            if (!dir.mkdir()) {
                Messages.showWarningDialog("保存失败，无法创建目录。", MsgValue.TITLE_INFO);
                return;
            }
        }
        // 获取保存文件
        File file = new File(dir, getConfigFileName(oldTableInfo.getObj()));
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
     *
     * @param tableInfo 表信息对象
     * @return 读取到的配置信息
     */
    private TableInfo read(TableInfo tableInfo, Project project) {
        // 获取保存的目录
        String path = project.getBasePath() + SAVE_PATH;
        File dir = new File(path);
        // 获取保存的文件
        File file = new File(dir, getConfigFileName(tableInfo.getObj()));
        // 文件不存在时直接保存一份
        if (!file.exists()) {
            return null;
        }
        // 读取并解析文件
        return parser(fileUtils.read(file));
    }

    /**
     * 获取配置文件名称
     *
     * @param dbTable 表信息对象
     * @return 对应的配置文件名称
     */
    private String getConfigFileName(DbTable dbTable) {
        String schemaName = DasUtil.getSchema(dbTable);
        return schemaName + "-" + dbTable.getName() + ".json";
    }

    /**
     * 对象还原
     *
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
