package com.sjhy.plugin.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.intellij.database.util.DasUtil;
import com.intellij.database.util.DbUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.ReflectionUtil;
import com.sjhy.plugin.config.Settings;
import com.sjhy.plugin.constants.MsgValue;
import com.sjhy.plugin.entity.Callback;
import com.sjhy.plugin.entity.SaveFile;
import com.sjhy.plugin.entity.TableInfo;
import com.sjhy.plugin.entity.Template;
import com.sjhy.plugin.service.CodeGenerateService;
import com.sjhy.plugin.service.TableInfoService;
import com.sjhy.plugin.tool.*;

import java.util.*;

/**
 * @author makejava
 * @version 1.0.0
 * @since 2018/09/02 12:50
 */
public class CodeGenerateServiceImpl implements CodeGenerateService {
    /**
     * 项目对象
     */
    private Project project;
    /**
     * 模型管理
     */
    private ModuleManager moduleManager;
    /**
     * 表信息服务
     */
    private TableInfoService tableInfoService;
    /**
     * 缓存数据工具
     */
    private CacheDataUtils cacheDataUtils;
    /**
     * 导入包时过滤的包前缀
     */
    private static final String FILTER_PACKAGE_NAME = "java.lang";

    public CodeGenerateServiceImpl(Project project) {
        this.project = project;
        this.moduleManager = ModuleManager.getInstance(project);
        this.tableInfoService = TableInfoService.getInstance(project);
        this.cacheDataUtils = CacheDataUtils.getInstance();
    }

    /**
     * 生成代码，并自动保存到对应位置，使用统一配置
     *
     * @param templates     模板
     * @param unifiedConfig 是否使用统一配置
     * @param title         是否显示提示
     */
    @Override
    public void generateByUnifiedConfig(Collection<Template> templates, boolean unifiedConfig, boolean title) {
        // 获取选中表信息
        TableInfo selectedTableInfo = tableInfoService.getTableInfoAndConfig(cacheDataUtils.getSelectDbTable());
        // 获取所有选中的表信息
        List<TableInfo> tableInfoList = tableInfoService.getTableInfoAndConfig(cacheDataUtils.getDbTableList());
        // 校验选中表的保存路径是否正确
        if (StringUtils.isEmpty(selectedTableInfo.getSavePath())) {
            Messages.showInfoMessage(selectedTableInfo.getObj().getName() + "表配置信息不正确，请尝试重新配置", MsgValue.TITLE_INFO);
            return;
        }
        // 将未配置的表进行配置覆盖
        tableInfoList.forEach(tableInfo -> {
            if (StringUtils.isEmpty(tableInfo.getSavePath())) {
                tableInfo.setSaveModelName(selectedTableInfo.getSaveModelName());
                tableInfo.setSavePackageName(selectedTableInfo.getSavePackageName());
                tableInfo.setSavePath(selectedTableInfo.getSavePath());
                tableInfoService.save(tableInfo);
            }
        });
        // 如果使用统一配置，直接全部覆盖
        if (unifiedConfig) {
            tableInfoList.forEach(tableInfo -> {
                tableInfo.setSaveModelName(selectedTableInfo.getSaveModelName());
                tableInfo.setSavePackageName(selectedTableInfo.getSavePackageName());
                tableInfo.setSavePath(selectedTableInfo.getSavePath());
            });
        }

        // 生成代码
        generate(templates, tableInfoList, title);
    }

    /**
     * 生成代码，并自动保存到对应位置
     *
     * @param templates     模板
     * @param tableInfoList 表信息对象
     * @param title         是否显示提示
     */
    private void generate(Collection<Template> templates, Collection<TableInfo> tableInfoList, boolean title) {
        generate(templates, tableInfoList, title, null);
    }

    /**
     * 生成代码，并自动保存到对应位置
     *
     * @param templates     模板
     * @param tableInfoList 表信息对象
     * @param title         是否显示提示
     * @param otherParam    其他参数
     */
    public void generate(Collection<Template> templates, Collection<TableInfo> tableInfoList, boolean title, Map<String, Object> otherParam) {
        if (CollectionUtil.isEmpty(templates) || CollectionUtil.isEmpty(tableInfoList)) {
            return;
        }
        // 处理模板，注入全局变量（克隆一份，防止篡改）
        templates = CloneUtils.cloneByJson(templates, new TypeReference<ArrayList<Template>>() {
        });
        TemplateUtils.addGlobalConfig(templates);
        // 生成代码
        for (TableInfo tableInfo : tableInfoList) {
            // 表名去除前缀
            if (!StringUtils.isEmpty(tableInfo.getPreName()) && tableInfo.getObj().getName().startsWith(tableInfo.getPreName())) {
                String newName = tableInfo.getObj().getName().replace(tableInfo.getPreName(), "");
                tableInfo.setName(NameUtils.getInstance().getClassName(newName));
            }
            // 构建参数
            Map<String, Object> param = getDefaultParam();
            // 其他参数
            if (otherParam != null) {
                param.putAll(otherParam);
            }
            // 所有表信息对象
            param.put("tableInfoList", tableInfoList);
            // 表信息对象
            param.put("tableInfo", tableInfo);
            // 设置模型路径与导包列表
            setModulePathAndImportList(param, tableInfo);
            // 设置额外代码生成服务
            param.put("generateService", new ExtraCodeGenerateUtils(this, tableInfo, title));
            for (Template template : templates) {
                Callback callback = new Callback();
                // 设置回调对象
                param.put("callback", callback);
                // 开始生成
                String code = VelocityUtils.generate(template.getCode(), param);
                // 清除前面空格
                code = code.trim();
                // 设置一个默认保存路径与默认文件名
                if (StringUtils.isEmpty(callback.getFileName())) {
                    callback.setFileName(tableInfo.getName() + "Default.java");
                }
                if (StringUtils.isEmpty(callback.getSavePath())) {
                    callback.setSavePath(tableInfo.getSavePath());
                }
                String path = callback.getSavePath();
                path = path.replace("\\", "/");
                // 针对相对路径进行处理
                if (path.startsWith(".")) {
                    path = project.getBasePath() + path.substring(1);
                }
                new SaveFile(project, path, callback.getFileName(), code, callback.isReformat(), title).write();
            }
        }
    }

    /**
     * 生成代码
     *
     * @param template  模板
     * @param tableInfo 表信息对象
     * @return 生成好的代码
     */
    @Override
    public String generate(Template template, TableInfo tableInfo) {
        // 获取默认参数
        Map<String, Object> param = getDefaultParam();
        // 表信息对象，进行克隆，防止篡改
        param.put("tableInfo", tableInfo);
        // 设置模型路径与导包列表
        setModulePathAndImportList(param, tableInfo);
        // 处理模板，注入全局变量
        TemplateUtils.addGlobalConfig(template);
        return VelocityUtils.generate(template.getCode(), param).trim();
    }

    /**
     * 设置模型路径与导包列表
     *
     * @param param     参数
     * @param tableInfo 表信息对象
     */
    private void setModulePathAndImportList(Map<String, Object> param, TableInfo tableInfo) {
        Module module = null;
        if (!StringUtils.isEmpty(tableInfo.getSaveModelName())) {
            module = this.moduleManager.findModuleByName(tableInfo.getSaveModelName());
        }
        if (module != null) {
            // 设置modulePath
            param.put("modulePath", ModuleUtil.getModuleDirPath(module));
        }
        // 设置要导入的包
        param.put("importList", getImportList(tableInfo));
    }

    /**
     * 获取默认参数
     *
     * @return 参数
     */
    private Map<String, Object> getDefaultParam() {
        // 系统设置
        Settings settings = Settings.getInstance();
        Map<String, Object> param = new HashMap<>(20);
        // 作者
        param.put("author", settings.getAuthor());
        //工具类
        param.put("tool", GlobalTool.getInstance());
        param.put("time", TimeUtils.getInstance());
        // 项目路径
        param.put("projectPath", project.getBasePath());
        // Database数据库工具
        param.put("dbUtil", ReflectionUtil.newInstance(DbUtil.class));
        param.put("dasUtil", ReflectionUtil.newInstance(DasUtil.class));
        return param;
    }

    /**
     * 获取导入列表
     *
     * @param tableInfo 表信息对象
     * @return 导入列表
     */
    private Set<String> getImportList(TableInfo tableInfo) {
        // 创建一个自带排序的集合
        Set<String> result = new TreeSet<>();
        tableInfo.getFullColumn().forEach(columnInfo -> {
            if (!columnInfo.getType().startsWith(FILTER_PACKAGE_NAME)) {
                result.add(columnInfo.getType());
            }
        });
        return result;
    }
}
