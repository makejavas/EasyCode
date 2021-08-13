package com.sjhy.plugin.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.database.model.DasColumn;
import com.intellij.database.psi.DbTable;
import com.intellij.database.util.DasUtil;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.javadoc.PsiDocToken;
import com.intellij.util.ExceptionUtil;
import com.intellij.util.containers.JBIterable;
import com.sjhy.plugin.dict.GlobalDict;
import com.sjhy.plugin.entity.ColumnInfo;
import com.sjhy.plugin.entity.SaveFile;
import com.sjhy.plugin.entity.TableInfo;
import com.sjhy.plugin.entity.TypeMapper;
import com.sjhy.plugin.service.TableInfoService;
import com.sjhy.plugin.tool.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.jetbrains.annotations.Nullable;

/**
 * @author makejava
 * @version 1.0.0
 * @since 2018/09/02 12:13
 */
public class TableInfoServiceImpl implements TableInfoService {

    /**
     * 项目对象
     */
    private Project project;

    /**
     * 命名工具类
     */
    private NameUtils nameUtils;
    /**
     * jackson格式化工具
     */
    private ObjectMapper objectMapper;
    /**
     * 文件工具类
     */
    private FileUtils fileUtils;

    public TableInfoServiceImpl(Project project) {
        this.project = project;
        this.nameUtils = NameUtils.getInstance();
        this.objectMapper = new ObjectMapper();
        this.fileUtils = FileUtils.getInstance();
    }

    /**
     * 通过DbTable获取TableInfo
     *
     * @param dbTable 原始表对象
     * @return 表信息对象
     */
    @Override
    public TableInfo getTableInfoByDbTable(DbTable dbTable) {
        if (dbTable == null) {
            return null;
        }
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
            // 短类型
            columnInfo.setShortType(nameUtils.getClsNameByFullName(columnInfo.getType()));
            // 列名
            columnInfo.setName(nameUtils.getJavaName(column.getName()));
            // 列注释
            columnInfo.setComment(column.getComment());
            // 扩展项
            columnInfo.setExt(new LinkedHashMap<>());
            // 添加到全部列
            tableInfo.getFullColumn().add(columnInfo);
            // 主键列添加到主键列，否则添加到其他列
            if (DasUtil.isPrimary(column)) {
                tableInfo.getPkColumn().add(columnInfo);
            } else {
                tableInfo.getOtherColumn().add(columnInfo);
            }
        }
        return tableInfo;
    }

    /**
     * 获取表信息并加载配置信息
     *
     * @param dbTable 原始表对象
     * @return 表信息对象
     */
    @Override
    public TableInfo getTableInfoAndConfig(DbTable dbTable) {
        TableInfo tableInfo = this.getTableInfoByDbTable(dbTable);
        // 加载配置
        String configFileName = getConfigFileName(tableInfo);
        this.loadConfig(tableInfo, configFileName);
        return tableInfo;
    }

    /**
     * 加载单个表信息配置(用户自定义列与扩张选项)
     *
     * @param tableInfo      表信息对象
     * @param configFileName 配置文件名称
     */
    private void loadConfig(TableInfo tableInfo, String configFileName) {
        if (tableInfo == null) {
            return;
        }
        // 读取配置文件中的表信息
        TableInfo tableInfoConfig = read(configFileName);
        // 返回空直接不处理
        if (tableInfoConfig == null) {
            return;
        }
        // 开始合并数据
        // 选择模型名称
        tableInfo.setSaveModelName(tableInfoConfig.getSaveModelName());
        // 选择的包名
        tableInfo.setSavePackageName(tableInfoConfig.getSavePackageName());
        // 选择的保存路径
        tableInfo.setSavePath(tableInfoConfig.getSavePath());
        // 选择的表名前缀
        tableInfo.setPreName(tableInfoConfig.getPreName());
        // 选择的模板组
        tableInfo.setTemplateGroupName(tableInfoConfig.getTemplateGroupName());

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

        for (ColumnInfo column : tableInfo.getFullColumn()) {
            boolean exists = false;
            for (ColumnInfo configColumn : tableInfoConfig.getFullColumn()) {
                if (Objects.equals(configColumn.getName(), column.getName())) {
                    exists = true;
                    // 覆盖空值
                    if (configColumn.getType() == null) {
                        configColumn.setType(column.getType());
                    }
                    // 短类型
                    if (!StringUtils.isEmpty(column.getShortType())) {
                        configColumn.setShortType(column.getShortType());
                    } else if (!StringUtils.isEmpty(configColumn.getType())) {
                        configColumn.setShortType(nameUtils.getClsNameByFullName(configColumn.getType()));
                    }
                    // 表注释覆盖
                    if (configColumn.getComment() == null) {
                        configColumn.setComment(column.getComment());
                    }
                    // 列对象覆盖
                    configColumn.setObj(column.getObj());

                    // 添加至新列表中
                    fullColumn.add(configColumn);
                    // 是否为主键
                    if (configColumn.getObj() != null) {
                        if (DasUtil.isPrimary(configColumn.getObj())) {
                            pkColumn.add(configColumn);
                        } else {
                            otherColumn.add(configColumn);
                        }
                    } else {
                        boolean isPk = tableInfo.getPkColumn() != null
                                && tableInfo.getPkColumn().stream()
                                .anyMatch(c -> Objects.equals(c.getName(), column.getName()));
                        if (isPk) {
                            pkColumn.add(configColumn);
                        } else {
                            otherColumn.add(configColumn);
                        }
                    }
                    break;
                }
            }
            // 新增的列
            if (!exists) {
                fullColumn.add(column);
            }
        }
        // 添加附加列
        for (ColumnInfo configColumn : tableInfoConfig.getFullColumn()) {
            if (configColumn.isCustom()) {
                fullColumn.add(configColumn);
            }
        }

        // 全部覆盖
        tableInfo.setFullColumn(fullColumn);
        tableInfo.setPkColumn(pkColumn);
        tableInfo.setOtherColumn(otherColumn);
    }

    /**
     * 通过映射获取对应的java类型类型名称
     *
     * @param typeName 列类型
     * @return java类型
     */
    private String getColumnType(String typeName) {
        for (TypeMapper typeMapper : CurrGroupUtils.getCurrTypeMapperGroup().getElementList()) {
            // 不区分大小写进行类型转换
            if (Pattern.compile(typeMapper.getColumnType(), Pattern.CASE_INSENSITIVE).matcher(typeName).matches()) {
                return typeMapper.getJavaType();
            }
        }
        // 没找到直接返回Object
        return "java.lang.Object";
    }

    /**
     * 类型校验，如果存在未知类型则引导用于去条件类型
     *
     * @param dbTable 原始表对象
     * @return 是否验证通过
     */
    @Override
    public boolean typeValidator(DbTable dbTable) {
        // 处理所有列
        JBIterable<? extends DasColumn> columns = DasUtil.getColumns(dbTable);
        List<TypeMapper> typeMapperList = CurrGroupUtils.getCurrTypeMapperGroup().getElementList();

        // 简单的记录报错弹窗次数，避免重复报错
        Set<String> errorCount = new HashSet<>();

        FLAG:
        for (DasColumn column : columns) {
            String typeName = column.getDataType().getSpecification();
            for (TypeMapper typeMapper : typeMapperList) {
                try {
                    // 不区分大小写查找类型
                    if (Pattern.compile(typeMapper.getColumnType(), Pattern.CASE_INSENSITIVE).matcher(typeName)
                            .matches()) {
                        continue FLAG;
                    }
                } catch (PatternSyntaxException e) {
                    if (!errorCount.contains(typeMapper.getColumnType())) {
                        Messages.showWarningDialog(
                                "类型映射《" + typeMapper.getColumnType() + "》存在语法错误，请及时修正。报错信息:" + e.getMessage(),
                                GlobalDict.TITLE_INFO);
                        errorCount.add(typeMapper.getColumnType());
                    }
                }
            }
            // 没找到类型，引导用户去添加类型
            if (MessageDialogUtils.yesNo(project, String.format("数据库类型%s，没有找到映射关系，是否去添加？", typeName))) {
                ShowSettingsUtil.getInstance().showSettingsDialog(project, "Type Mapper");
                return false;
            }
            // 用户取消添加
            return true;
        }
        return true;
    }

    /**
     * 保存数据
     *
     * @param tableInfo 表信息对象
     */
    @Override
    public void save(TableInfo tableInfo) {
        // 获取未修改前的原数据
        TableInfo oldTableInfo;
        if (tableInfo.getObj() != null) {
            oldTableInfo = getTableInfoByDbTable(tableInfo.getObj());
        } else {
            oldTableInfo = getTableInfoByPsiClass(tableInfo.getPsiClassObj());
        }
        // 克隆对象，防止串改，同时原始对象丢失
        tableInfo = CloneUtils.cloneByJson(tableInfo, false);
        //排除部分字段，这些字段不进行保存
        tableInfo.setOtherColumn(null);
        tableInfo.setPkColumn(null);
        // 获取迭代器
        Iterator<ColumnInfo> columnIterable = tableInfo.getFullColumn().iterator();
        while (columnIterable.hasNext()) {
            Iterator<ColumnInfo> oldColumnIterable = oldTableInfo.getFullColumn().iterator();
            // 新列
            ColumnInfo columnInfo = columnIterable.next();
            // 是否存在
            boolean exists = false;
            while (oldColumnIterable.hasNext()) {
                ColumnInfo oldColumnInfo = oldColumnIterable.next();
                // 不同列直接返回跳过
                if (!Objects.equals(columnInfo.getName(), oldColumnInfo.getName())) {
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
                // 不保存短类型
                columnInfo.setShortType(null);
                // 列存在，进行处理
                exists = true;
                break;
            }
            // 扩展项不能为空
            if (columnInfo.getExt() == null) {
                columnInfo.setExt(new LinkedHashMap<>());
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
            Messages.showWarningDialog("保存失败，JSON序列化错误。", GlobalDict.TITLE_INFO);
            return;
        }
        // 获取或创建保存目录
        VirtualFile dir = getEasyCodeConfigDirectory(project);
        if (dir == null) {
            return;
        }
        // 获取保存文件
        new SaveFile(project, dir.getPath(), getConfigFileName(oldTableInfo), content, true, false).write();
    }

    @Override
    public TableInfo getTableInfoByPsiClass(PsiClass psiClass) {
        TableInfo tableInfo = new TableInfo();
        tableInfo.setPsiClassObj(psiClass);
        tableInfo.setName(psiClass.getName());
        tableInfo.setComment(parsePsiClassComment(psiClass.getDocComment()));
        tableInfo.setFullColumn(new ArrayList<>());
        tableInfo.setPkColumn(new ArrayList<>());
        tableInfo.setOtherColumn(new ArrayList<>());
        for (PsiField field : psiClass.getAllFields()) {
            if (PsiClassGenerateUtils.isSkipField(field)) {
                continue;
            }
            ColumnInfo columnInfo = new ColumnInfo();
            columnInfo.setName(field.getName());
            columnInfo.setShortType(field.getType().getPresentableText());
            columnInfo.setType(field.getType().getCanonicalText());
            columnInfo.setComment(parsePsiClassComment(field.getDocComment()));
            tableInfo.getFullColumn().add(columnInfo);
            if (PsiClassGenerateUtils.isPkField(field)) {
                tableInfo.getPkColumn().add(columnInfo);
            } else {
                tableInfo.getOtherColumn().add(columnInfo);
            }
        }
        return tableInfo;
    }

    private String removeClassNameGeneric(String fullName) {
        int genericIdx = fullName.indexOf('<');
        if (genericIdx == -1) {
            return fullName;
        }
        return fullName.substring(0, genericIdx);
    }


    @Override
    public TableInfo getTableInfoAndConfigByPsiClass(PsiClass selectPsiClass) {
        TableInfo tableInfo = this.getTableInfoByPsiClass(selectPsiClass);
        // 加载配置
        String configFileName = getConfigFileName(tableInfo);
        this.loadConfig(tableInfo, configFileName);
        return tableInfo;
    }

    @Override
    public List<TableInfo> getTableInfoAndConfigByPsiClass(List<PsiClass> psiClassList) {
        if (CollectionUtil.isEmpty(psiClassList)) {
            return Collections.emptyList();
        }
        List<TableInfo> tableInfoList = new ArrayList<>(psiClassList.size());
        psiClassList.forEach(psiClass -> tableInfoList.add(this.getTableInfoAndConfigByPsiClass(psiClass)));
        return tableInfoList;
    }

    private String parsePsiClassComment(@Nullable PsiDocComment docComment) {
        if (docComment == null) {
            return null;
        }
        return Arrays.stream(docComment.getDescriptionElements())
                .filter(o -> o instanceof PsiDocToken)
                .map(PsiElement::getText)
                .findFirst()
                .map(String::trim)
                .orElse(null);
    }

    /**
     * 读取配置文件
     *
     * @param fileName 配置文件名称, 不包含路径
     * @return 读取到的配置信息
     */
    private TableInfo read(String fileName) {
        // 获取保存的目录
        VirtualFile easyCodeConfigDir = getEasyCodeConfigDirectory(project);
        if (easyCodeConfigDir == null) {
            return null;
        }
        // 获取配置文件
        VirtualFile configJsonFile = easyCodeConfigDir.findChild(fileName);
        if (configJsonFile == null) {
            return null;
        }
        Document document = FileDocumentManager.getInstance().getDocument(configJsonFile);
        if (document == null) {
            Messages.showInfoMessage(fileName + "转文档对象失败", GlobalDict.TITLE_INFO);
            return null;
        }
        // 读取并解析文件
        String json = document.getText();
        if (StringUtils.isEmpty(json)) {
            Messages.showInfoMessage(fileName + "配置文件文件为空，请尝试手动删除" + configJsonFile.getPath() + "文件！",
                    GlobalDict.TITLE_INFO);
            return null;
        }
        return parser(json, configJsonFile);
    }

    /**
     * 获取EasyCodeConfig目录
     *
     * @param project 项目
     * @return EasyCodeConfig目录
     */
    private VirtualFile getEasyCodeConfigDirectory(Project project) {
        VirtualFile baseDir = ProjectUtils.getBaseDir(project);
        if (baseDir == null) {
            Messages.showInfoMessage("无法获取项目路径", GlobalDict.TITLE_INFO);
            return null;
        }
        // 获取.idea路径
        VirtualFile ideaDir = baseDir.findChild(".idea");
        // 当获取失败时尝试通过父级目录获取，最多向上找3级目录
        VirtualFile tmpDir = baseDir;
        for (int i = 0; i < 3; i++) {
            if (ideaDir != null) {
                break;
            }
            tmpDir = tmpDir.getParent();
            // 当没有父级目录时不再继续
            if (tmpDir == null) {
                break;
            }
            ideaDir = tmpDir.findChild(".idea");
        }
        if (ideaDir == null) {
            Messages.showInfoMessage(".idea路径获取失败", GlobalDict.TITLE_INFO);
            String errorMsg = String.format("baseDir:%s, not found .idea child directory", baseDir.getPath());
            ExceptionUtil.rethrow(new IllegalStateException(errorMsg));
            return null;
        }
        // 查找或创建EasyCodeConfig路径
        VirtualFile easyCodeDir = ideaDir.findChild("EasyCodeConfig");
        if (easyCodeDir == null) {
            easyCodeDir = fileUtils.createChildDirectory(project, ideaDir, "EasyCodeConfig");
        }
        return easyCodeDir;
    }

    /**
     * 获取配置文件名称
     *
     * @param tableInfo 表信息对象
     * @return 对应的配置文件名称
     */
    private String getConfigFileName(TableInfo tableInfo) {
        if (tableInfo.getObj() != null) {
            DbTable dbTable = tableInfo.getObj();
            String schemaName = DasUtil.getSchema(dbTable);
            return schemaName + "-" + dbTable.getName() + ".json";
        } else {
            return tableInfo.getPsiClassObj().getQualifiedName() + ".json";
        }
    }

    /**
     * 对象还原
     *
     * @param str 原始JSON字符串
     * @return 解析结果
     */
    private TableInfo parser(String str, VirtualFile originalFile) {
        try {
            return objectMapper.readValue(str, TableInfo.class);
        } catch (IOException e) {
            Messages.showWarningDialog("读取配置失败，JSON反序列化异常。请尝试手动删除" + originalFile.getPath() + "文件！",
                    GlobalDict.TITLE_INFO);
            ExceptionUtil.rethrow(e);
        }
        return null;
    }
}
