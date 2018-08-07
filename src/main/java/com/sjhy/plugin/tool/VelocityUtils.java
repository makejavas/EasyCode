package com.sjhy.plugin.tool;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.ui.MessageDialogBuilder;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.ExceptionUtil;
import com.sjhy.plugin.constants.MsgValue;
import com.sjhy.plugin.entity.Callback;
import com.sjhy.plugin.entity.GlobalConfig;
import com.sjhy.plugin.entity.TableInfo;
import com.sjhy.plugin.entity.Template;
import com.sjhy.plugin.tool.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Velocity工具类，主要用于代码生成
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/17 13:10
 */
public class VelocityUtils {
    private volatile static VelocityUtils velocityUtils;

    /**
     * 单例模式
     */
    public static VelocityUtils getInstance() {
        if (velocityUtils == null) {
            synchronized (VelocityUtils.class) {
                if (velocityUtils == null) {
                    velocityUtils = new VelocityUtils();
                }
            }
        }
        return velocityUtils;
    }

    /**
     * 私有构造方法
     */
    private VelocityUtils() {
        velocityEngine = new VelocityEngine();
        // 修复部分用户的velocity日志记录无权访问velocity.log文件问题
        velocityEngine.setProperty( RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.Log4JLogChute" );
        velocityEngine.setProperty("runtime.log.logsystem.log4j.logger", "velocity");
    }

    /**
     * Velocity引擎
     */
    private final VelocityEngine velocityEngine;
    /**
     * 缓存工具类
     */
    private CacheDataUtils cacheDataUtils = CacheDataUtils.getInstance();
    /**
     * 表信息工具
     */
    private TableInfoUtils tableInfoUtils = TableInfoUtils.getInstance();
    /**
     * 命名工具类
     */
    private NameUtils nameUtils = NameUtils.getInstance();
    /**
     * 文件工具类
     */
    private FileUtils fileUtils = FileUtils.getInstance();
    /**
     * 导入包时过滤的包前缀
     */
    private static final String FILTER_PACKAGENAME = "java.lang";

    /**
     * 生成代码
     *
     * @param template 模板对象
     * @param map      参数集合
     * @param encode   编码类型
     * @return 生成结果
     */
    private String generate(String template, Map<String, Object> map, String encode) {
        VelocityContext velocityContext = new VelocityContext();
        if (map != null) {
            map.forEach(velocityContext::put);
        }
        StringWriter stringWriter = new StringWriter();
        velocityEngine.setProperty("input.encode", encode);
        velocityEngine.setProperty("output.encode", encode);
        velocityEngine.evaluate(velocityContext, stringWriter, "Velocity Code Generate", template);
        return stringWriter.toString();
    }

    /**
     * 设置全局参数
     *
     * @return 全局参数
     */
    private Map<String, Object> handlerMap() {
        ConfigInfo configInfo = ConfigInfo.getInstance();
        Map<String, Object> map = new HashMap<>(16);
        // 编码类型
        String encode = configInfo.getEncode();
        // 坐着名称
        String author = configInfo.getAuthor();
        // 表信息集合
        List<TableInfo> tableInfoList = tableInfoUtils.handler(cacheDataUtils.getDbTableList());
        // 选中的module
        Module selectModule = cacheDataUtils.getSelectModule();

        map.put("encode", encode);
        //所有表数据
        map.put("tableInfoList", tableInfoList);
        //作者
        map.put("author", author);
        //工具类
        map.put("tool", nameUtils);
        map.put("time", TimeUtils.getInstance());
        //设置的包名
        map.put("packageName", cacheDataUtils.getPackageName());
        if (selectModule != null) {
            //module绝对路径
            String modulePath = new File(selectModule.getModuleFilePath()).getParent();
            if (modulePath != null) {
                // 兼容Linux
                modulePath = modulePath.replace("\\", "/");

                // 针对Mac版路径做优化
                if (modulePath.contains("/.idea")) {
                    modulePath = modulePath.substring(0, modulePath.indexOf("/.idea"));
                }
            }
            map.put("modulePath", modulePath);
            map.put("moduleName", selectModule.getName());
        }
        // 项目路径
        map.put("projectPath", cacheDataUtils.getProject().getBasePath());
        return map;
    }

    /**
     * 创建目录
     *
     * @param savePath 路径
     * @return 保存结果
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean createPath(String savePath) {
        File path = new File(savePath);
        if (!new File(savePath).exists()) {
            if (MessageDialogBuilder.yesNo(MsgValue.TITLE_INFO, "Save Path Is Not Exists, Confirm Create?").isYes()) {
                if (!path.mkdirs()) {
                    return false;
                }
            } else {
                return false;
            }
        }
        if (path.isFile()) {
            Messages.showMessageDialog("Error,Save Path Is File!", MsgValue.TITLE_INFO, Messages.getErrorIcon());
            return false;
        }
        return true;
    }

    /**
     * 创建或覆盖文件
     *
     * @param file 文件
     * @return 是否成功
     */
    private boolean coverFile(File file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                Messages.showMessageDialog("Error,Save File Is Path!", MsgValue.TITLE_INFO, Messages.getErrorIcon());
                return false;
            }
            // 是否覆盖
            return MessageDialogBuilder.yesNo(MsgValue.TITLE_INFO, "File " + file.getName() + " Exists, Confirm Continue?").isYes();
        } else {
            try {
                if (!file.createNewFile()) {
                    return false;
                }
            } catch (IOException e) {
                // 直接抛出异常
                ExceptionUtil.rethrow(e);
                return false;
            }
        }
        return true;
    }

    /**
     * 生成代码
     */
    public void handler() {
        if (!createPath(cacheDataUtils.getSavePath())) {
            return;
        }
        ConfigInfo configInfo = ConfigInfo.getInstance();
        // 获取覆盖的表配置信息
        List<TableInfo> tableInfoList = coverConfigInfo();
        List<Template> templateList = CloneUtils.getInstance().cloneList(cacheDataUtils.getSelectTemplate());
        // 预处理加入全局变量
        templateList.forEach(template -> {
            String templateContent = template.getCode() + "\n";
            for (GlobalConfig globalConfig : configInfo.getGlobalConfigGroupMap().get(configInfo.getCurrGlobalConfigGroupName()).getElementList()) {
                // 需要替换两次，防止$在正则中出现问题
                templateContent = templateContent.replaceAll("\\$!?\\{?" + globalConfig.getName() + "([^a-zA-Z0-9])}?", ":::{" + globalConfig.getName() + "}$1");
                templateContent = templateContent.replace(":::{" + globalConfig.getName() + "}", globalConfig.getValue());
            }
            template.setCode(templateContent);
        });
        // 获取编码信息
        String encode = configInfo.getEncode();
        // 获取默认的配置信息
        Map<String, Object> map = handlerMap();
        // 项目路径
        String projectPath = cacheDataUtils.getProject().getBasePath();

        tableInfoList.forEach(tableInfo -> {
            Callback callback = new Callback();
            // 表信息对象
            map.put("tableInfo", tableInfo);
            // 导入的包集合
            map.put("importList", getImportList(tableInfo));
            // 回调函数
            map.put("callback", callback);
            templateList.forEach(template -> {
                // 获取保存路径
                String savePath = cacheDataUtils.getSavePath();
                // 生成代码并去除两端空格
                String content = generate(template.getCode(), map, encode).trim();
                //保存的文件名
                String fileName = callback.getFileName();
                //保存路径
                String callbackSavePath = callback.getSavePath();
                //是否使用回调中的保存路径
                if (!StringUtils.isEmpty(callbackSavePath)) {
                    // 判断是否需要凭借项目路径
                    if (callbackSavePath.startsWith("./")) {
                        callbackSavePath = projectPath + callbackSavePath.substring(1);
                    }
                    if (!createPath(callbackSavePath)) {
                        return;
                    }
                    savePath = callbackSavePath;
                }
                // 当回调中没有保存文件名时
                if (fileName == null) {
                    fileName = tableInfo.getName() + "Default.java";
                }
                File file = new File(new File(savePath), fileName);
                //覆盖或创建文件
                if (!coverFile(file)) {
                    return;
                }
                // 写入文件
                fileUtils.write(file, content);
            });
        });
        Messages.showMessageDialog("Code Generate Successful!", MsgValue.TITLE_INFO, Messages.getInformationIcon());
        //刷新整个项目
        VirtualFileManager.getInstance().syncRefresh();
    }

    /**
     * 覆盖保存配置信息
     *
     * @return 覆盖后的表配置信息
     */
    private List<TableInfo> coverConfigInfo() {
        // 选择的module名称
        final String moduleName;
        if (cacheDataUtils.getSelectModule() != null) {
            moduleName = cacheDataUtils.getSelectModule().getName();
        } else {
            moduleName = null;
        }

        AtomicBoolean isSave = new AtomicBoolean(false);
        List<TableInfo> tableInfoList = tableInfoUtils.handler(cacheDataUtils.getDbTableList());


        // 判断路径是否需要是由相对路径
        String savePath = cacheDataUtils.getSavePath();
        // 兼容Linux
        if (savePath.contains("\\")) {
            savePath = savePath.replace("\\", "/");
            cacheDataUtils.setSavePath(savePath);
        }
        String projectPath = cacheDataUtils.getProject().getBasePath();
        if (projectPath!=null) {
            // 兼容Linux路径
            if (projectPath.contains("\\")) {
                projectPath = projectPath.replace("\\", "/");
            }
            if (savePath.indexOf(projectPath) == 0) {
                savePath = savePath.substring(projectPath.length());
                if (savePath.startsWith("/")) {
                    savePath = "." + savePath;
                } else {
                    savePath = "./" + savePath;
                }
            }
        }

        final String finalSavePath = savePath;

        // 将选中表中的没有保存配置信息的表进行保存
        tableInfoList.forEach(tableInfo -> {
            // 输入当前表是选中表
            if (tableInfo.getObj() == cacheDataUtils.getSelectDbTable()) {
                // 只要所有保存信息都没修改就不进行覆盖保存
                if (Objects.equals(tableInfo.getSavePath(), finalSavePath) && Objects.equals(moduleName, tableInfo.getSaveModelName()) && Objects.equals(tableInfo.getSavePackageName(), cacheDataUtils.getPackageName())) {
                    return;
                }
            } else {
                // 只要存在任意一项保存信息就不进行覆盖保存
                if (!StringUtils.isEmpty(tableInfo.getSaveModelName()) || !StringUtils.isEmpty(tableInfo.getSavePath()) || !StringUtils.isEmpty(tableInfo.getSavePackageName())) {
                    return;
                }
            }
            // 进行覆盖保存
            tableInfo.setSavePath(finalSavePath);
            tableInfo.setSavePackageName(cacheDataUtils.getPackageName());
            tableInfo.setSaveModelName(moduleName);
            // 保存信息
            tableInfoUtils.save(tableInfo);
            isSave.set(true);
        });
        // 保存完毕后需要重新获取数据
        if (isSave.get()) {
            tableInfoList = tableInfoUtils.handler(cacheDataUtils.getDbTableList());
        }
        // 判断是否进行覆盖，（临时覆盖，不保存）
        if (cacheDataUtils.isUnifiedConfig()) {
            tableInfoList.forEach(tableInfo -> {
                tableInfo.setSavePath(cacheDataUtils.getSavePath());
                tableInfo.setSavePackageName(cacheDataUtils.getPackageName());
                if (cacheDataUtils.getSelectModule() != null) {
                    tableInfo.setSaveModelName(cacheDataUtils.getSelectModule().getName());
                }
            });
        }
        return tableInfoList;
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
            if (!columnInfo.getType().startsWith(FILTER_PACKAGENAME)) {
                result.add(columnInfo.getType());
            }
        });
        return result;
    }
}
