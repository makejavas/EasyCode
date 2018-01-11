package com.sjhy.plugin.tool;

import com.intellij.openapi.util.io.FileUtil;
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

    public void handler() {
        String savePath = cacheDataUtils.getSavePath();
        File path = new File(savePath);
        if (!new File(savePath).exists()) {
            int result = JOptionPane.showConfirmDialog(null, "Save Path Is Not Exists, Confirm Create?", "Title Info", JOptionPane.OK_CANCEL_OPTION);
            if(result==0){
                if(!path.mkdirs()){
                    return;
                }
            }else{
                return;
            }
        }
        if (path.isFile()){
            JOptionPane.showMessageDialog(null, "Error,Save Path Is File!");
            return;
        }
        List<TableInfo> tableInfoList = tableInfoUtils.handler(cacheDataUtils.getDbTableList());
        List<Template> templateList = cacheDataUtils.getSelectTemplate();
        ConfigService configService = ConfigService.getInstance();
        String encode = configService.getEncode();
        String author = configService.getAuthor();
        Map<String, Object> map = new HashMap<>();

        map.put("tableInfoList", tableInfoList);
        map.put("author", author);
        map.put("tool", nameUtils);
        map.put("packageName", cacheDataUtils.getPackageName());

        tableInfoList.forEach(tableInfo -> {
            Callback callback = new Callback();
            map.put("tableInfo", tableInfo);
            map.put("importList", getImportList(tableInfo));
            map.put("callback", callback);
            templateList.forEach(template -> {
                String content = generate(template.getCode(), map, encode).trim();
                String fileName = callback.getFileName();
                if (fileName==null) {
                    fileName = tableInfo.getName()+"Default.java";
                }
                File file = new File(path, fileName);
                try {
                    if (file.exists()) {
                        int result = JOptionPane.showConfirmDialog(null, "File "+fileName+" Exists, Confirm Continue?", "Title Info", JOptionPane.OK_CANCEL_OPTION);
                        if (result!=0){
                            return;
                        }
                    }else{
                        if(!file.createNewFile()){
                            return;
                        }
                    }
                    FileUtil.writeToFile(file, content, false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        });
        JOptionPane.showMessageDialog(null, "Code Generate Success!");
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
