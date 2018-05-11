package com.sjhy.plugin.tool;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.sjhy.plugin.entity.Callback;
import com.sjhy.plugin.entity.TableInfo;
import com.sjhy.plugin.entity.Template;
import com.sjhy.plugin.service.ConfigService;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class VelocityUtils {
    //单例模式
    private static class Instance {
        private static final VelocityUtils ME = new VelocityUtils();
    }
    public static VelocityUtils getInstance() {
        return Instance.ME;
    }
    private VelocityUtils(){
        velocityEngine = new VelocityEngine();
    }

    private final VelocityEngine velocityEngine;
    private CacheDataUtils cacheDataUtils = CacheDataUtils.getInstance();
    private TableInfoUtils tableInfoUtils = TableInfoUtils.getInstance();
    private NameUtils nameUtils = NameUtils.getInstance();
    private FileUtils fileUtils = FileUtils.getInstance();

    private String generate(String template, Map<String,Object> map, String encode) {
        VelocityContext velocityContext = new VelocityContext();
        if (map==null) {
            map = new HashMap<>();
        }
        map.forEach(velocityContext::put);
        StringWriter stringWriter = new StringWriter();
        velocityEngine.setProperty("input.encode", encode);
        velocityEngine.setProperty("output.encode", encode);
        velocityEngine.evaluate(velocityContext, stringWriter, "", template);
        return stringWriter.toString();
    }

    //设置全局参数
    private Map<String, Object> handlerMap() {
        ConfigService configService = ConfigService.getInstance();
        Map<String, Object> map = new HashMap<>();
        String encode = configService.getEncode();
        String author = configService.getAuthor();
        List<TableInfo> tableInfoList = tableInfoUtils.handler(cacheDataUtils.getDbTableList());
        Module selectModule = cacheDataUtils.getSelectModule();

        map.put("encode", encode);
        //所有表数据
        map.put("tableInfoList", tableInfoList);
        //作者
        map.put("author", author);
        //工具类
        map.put("tool", nameUtils);
        //设置的包名
        map.put("packageName", cacheDataUtils.getPackageName());
        if (selectModule!=null){
            //module路径
            map.put("modulePath", new File(selectModule.getModuleFilePath()).getParent());
        }
        return map;
    }

    //创建目录
    private boolean createPath(String savePath) {
        File path = new File(savePath);
        if (!new File(savePath).exists()) {
            int result = JOptionPane.showConfirmDialog(null, "Save Path Is Not Exists, Confirm Create?", "Title Info", JOptionPane.OK_CANCEL_OPTION);
            if(result==0){
                if(!path.mkdirs()){
                    return false;
                }
            }else{
                return false;
            }
        }
        if (path.isFile()){
            JOptionPane.showMessageDialog(null, "Error,Save Path Is File!");
            return false;
        }
        return true;
    }

    //创建或覆盖文件
    private boolean coverFile(File file) {
        if (file.exists()) {
            if (file.isDirectory()){
                JOptionPane.showMessageDialog(null, "Error,Save File Is Path!");
                return false;
            }
            int result = JOptionPane.showConfirmDialog(null, "File "+file.getName()+" Exists, Confirm Continue?", "Title Info", JOptionPane.OK_CANCEL_OPTION);
            return result == 0;
        }else{
            try {
                if(!file.createNewFile()){
                    return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public void handler() {
        AtomicReference<String> savePath = new AtomicReference<>(cacheDataUtils.getSavePath());
        if (!createPath(savePath.get())){
            return;
        }
        List<TableInfo> tableInfoList = tableInfoUtils.handler(cacheDataUtils.getDbTableList());
        List<Template> templateList = cacheDataUtils.getSelectTemplate();
        ConfigService configService = ConfigService.getInstance();
        String encode = configService.getEncode();
        Map<String, Object> map = handlerMap();

        tableInfoList.forEach(tableInfo -> {
            Callback callback = new Callback();
            map.put("tableInfo", tableInfo);
            map.put("importList", getImportList(tableInfo));
            map.put("callback", callback);
            templateList.forEach(template -> {
                //重置路径
                callback.setSavePath(cacheDataUtils.getSavePath());
                String content = generate(template.getCode(), map, encode).trim();
                //保存的文件名
                String fileName = callback.getFileName();
                //保存路径
                String callbackSavePath = callback.getSavePath();
                //是否使用保存路径
                if (callbackSavePath!=null && callbackSavePath.trim().length()>0){
                    if (!createPath(callbackSavePath)){
                        return;
                    }
                    savePath.set(callbackSavePath);
                }
                if (fileName==null) {
                    fileName = tableInfo.getName()+"Default.java";
                }
                File file = new File(new File(savePath.get()), fileName);
                //覆盖或创建文件
                if (!coverFile(file)){
                    return;
                }
                fileUtils.write(file, content);
            });
        });
        JOptionPane.showMessageDialog(null, "Code Generate Success!");
        //刷新整个项目
        VirtualFileManager.getInstance().syncRefresh();
    }

    private Set<String> getImportList(TableInfo tableInfo) {
        Set<String> result = new TreeSet<>();
        tableInfo.getFullColumn().forEach(columnInfo -> {
            if (!columnInfo.getType().startsWith("java.lang")){
                result.add(columnInfo.getType());
            }
        });
        return result;
    }
}
