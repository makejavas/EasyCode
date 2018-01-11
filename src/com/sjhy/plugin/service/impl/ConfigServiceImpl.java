package com.sjhy.plugin.service.impl;

import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.sjhy.plugin.entity.Template;
import com.sjhy.plugin.entity.TemplateGroup;
import com.sjhy.plugin.entity.TypeMapper;
import com.sjhy.plugin.entity.TypeMapperGroup;
import com.sjhy.plugin.service.ConfigService;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@State(name = "EasyCodeSetting", storages = @Storage("$APP_CONFIG$/EasyCode-settings.xml"))
public class ConfigServiceImpl implements ConfigService {
    //当前类型映射组名
    private String currTypeMapperGroupName;
    //类型映射组
    private Map<String, TypeMapperGroup> typeMapperGroupMap;
    //当前模板组名
    private String currTemplateGroupName;
    //模板组
    private Map<String, TemplateGroup> templateGroupMap;
    //默认编码
    private String encode;
    //作者
    private String author;

    //是否初始化
    private Boolean init = false;

    public ConfigServiceImpl() {
        initDefault();
    }

    @Nullable
    @Override
    public ConfigService getState() {
        return this;
    }

    @Override
    public void loadState(ConfigService configService) {
        if (configService==null || configService.getTemplateGroupMap()==null || configService.getTypeMapperGroupMap()==null) {
            return;
        }
        //重点，没有数据时，不要序列化
        if (configService.getTypeMapperGroupMap().isEmpty()) {
            return;
        }
        if (configService.getTemplateGroupMap().isEmpty()) {
            return;
        }
        //加载配置信息
        XmlSerializerUtil.copyBean(configService, this);
    }

    /**
     * 初始化默认设置
     */
    private void initDefault() {
        if (this.encode==null) {
            this.encode = "UTF-8";
        }
        if (this.author==null) {
            this.author = "Mr.Wang";
        }
        if (this.currTemplateGroupName==null) {
            this.currTemplateGroupName = DEFAULT_NAME;
        }
        if (this.currTypeMapperGroupName==null) {
            this.currTypeMapperGroupName = DEFAULT_NAME;
        }
        //配置默认模板
        if (this.templateGroupMap==null) {
            this.templateGroupMap = new LinkedHashMap<>();
        }
        TemplateGroup templateGroup = new TemplateGroup();
        List<Template> templateList = new ArrayList<>();
        templateList.add(new Template("entity", loadTemplate("entity")));
        templateList.add(new Template("dao", loadTemplate("dao")));
        templateGroup.setName(DEFAULT_NAME);
        templateGroup.setTemplateList(templateList);
        this.templateGroupMap.put(DEFAULT_NAME, templateGroup);

        //配置默认类型映射
        if (this.typeMapperGroupMap==null) {
            this.typeMapperGroupMap = new LinkedHashMap<>();
        }
        TypeMapperGroup typeMapperGroup = new TypeMapperGroup();
        List<TypeMapper> typeMapperList = new ArrayList<>();
        typeMapperList.add(new TypeMapper("varchar(\\(\\d+\\))?", "java.lang.String"));
        typeMapperList.add(new TypeMapper("decimal(\\(\\d+\\))?", "java.lang.Double"));
        typeMapperList.add(new TypeMapper("integer", "java.lang.Integer"));
        typeMapperList.add(new TypeMapper("int4", "java.lang.Integer"));
        typeMapperList.add(new TypeMapper("int8", "java.lang.Long"));
        typeMapperList.add(new TypeMapper("bigint", "java.lang.Long"));
        typeMapperList.add(new TypeMapper("datetime", "java.util.Date"));
        typeMapperList.add(new TypeMapper("timestamp", "java.util.Date"));
        typeMapperGroup.setName(DEFAULT_NAME);
        typeMapperGroup.setTypeMapperList(typeMapperList);
        typeMapperGroupMap.put(DEFAULT_NAME, typeMapperGroup);
    }

    private String loadTemplate(String name) {
        try {
            return FileUtil.loadFile(new File(getClass().getResource("/template/"+name+".vm").getFile()), "UTF-8").replaceAll("\r", "");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //GET SET
    @Override
    public String getCurrTypeMapperGroupName() {
        return currTypeMapperGroupName;
    }

    @Override
    public void setCurrTypeMapperGroupName(String currTypeMapperGroupName) {
        this.currTypeMapperGroupName = currTypeMapperGroupName;
    }

    @Override
    public Map<String, TypeMapperGroup> getTypeMapperGroupMap() {
        return typeMapperGroupMap;
    }

    @Override
    public String getCurrTemplateGroupName() {
        return currTemplateGroupName;
    }

    @Override
    public void setCurrTemplateGroupName(String currTemplateGroupName) {
        this.currTemplateGroupName = currTemplateGroupName;
    }

    @Override
    public String getEncode() {
        return encode;
    }

    @Override
    public void setEncode(String encode) {
        this.encode = encode;
    }

    @Override
    public String getAuthor() {
        return author;
    }

    @Override
    public void setAuthor(String author) {
        this.author = author;
    }

    @Override
    public void setTypeMapperGroupMap(Map<String, TypeMapperGroup> typeMapperGroupMap) {
        this.typeMapperGroupMap = typeMapperGroupMap;
    }

    @Override
    public Map<String, TemplateGroup> getTemplateGroupMap() {
        return templateGroupMap;
    }

    @Override
    public void setTemplateGroupMap(Map<String, TemplateGroup> templateGroupMap) {
        this.templateGroupMap = templateGroupMap;
    }

    public Boolean getInit() {
        return init;
    }

    public void setInit(Boolean init) {
        this.init = init;
    }
}
