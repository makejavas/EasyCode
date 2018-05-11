package com.sjhy.plugin.service.impl;

import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.sjhy.plugin.entity.*;
import com.sjhy.plugin.service.ConfigService;
import com.sjhy.plugin.tool.FileUtils;
import org.jetbrains.annotations.Nullable;

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
    //当前配置表组名
    private String currColumnConfigGroupName;
    //配置表组
    private Map<String, ColumnConfigGroup> columnConfigGroupMap;
    //默认编码
    private String encode;
    //作者
    private String author;


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
        if (configService==null || configService.getTemplateGroupMap()==null || configService.getTypeMapperGroupMap()==null || configService.getColumnConfigGroupMap()==null) {
            return;
        }
        //重点，没有数据时，不要序列化
        if (configService.getTypeMapperGroupMap().isEmpty()) {
            return;
        }
        if (configService.getTemplateGroupMap().isEmpty()) {
            return;
        }
        if (configService.getColumnConfigGroupMap().isEmpty()) {
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
        if (this.currColumnConfigGroupName==null) {
            this.currColumnConfigGroupName = DEFAULT_NAME;
        }
        //配置默认模板
        if (this.templateGroupMap==null) {
            this.templateGroupMap = new LinkedHashMap<>();
        }
        TemplateGroup templateGroup = new TemplateGroup();
        List<Template> templateList = new ArrayList<>();
        templateList.add(new Template("entity", loadTemplate("entity")));
        templateList.add(new Template("dao", loadTemplate("dao")));
        templateList.add(new Template("mapper", loadTemplate("mapper")));
        templateList.add(new Template("query", loadTemplate("query")));
        templateList.add(new Template("service", loadTemplate("service")));
        templateList.add(new Template("serviceimpl", loadTemplate("serviceimpl")));
        templateGroup.setName(DEFAULT_NAME);
        templateGroup.setElementList(templateList);
        this.templateGroupMap.put(DEFAULT_NAME, templateGroup);

        //配置默认类型映射
        if (this.typeMapperGroupMap==null) {
            this.typeMapperGroupMap = new LinkedHashMap<>();
        }
        TypeMapperGroup typeMapperGroup = new TypeMapperGroup();
        List<TypeMapper> typeMapperList = new ArrayList<>();
        typeMapperList.add(new TypeMapper("varchar(\\(\\d+\\))?", "java.lang.String"));
        typeMapperList.add(new TypeMapper("decimal(\\(\\d+(,\\d+)?\\))?", "java.lang.Double"));
        typeMapperList.add(new TypeMapper("integer", "java.lang.Integer"));
        typeMapperList.add(new TypeMapper("int(\\(\\d+\\))?", "java.lang.Integer"));
        typeMapperList.add(new TypeMapper("int4", "java.lang.Integer"));
        typeMapperList.add(new TypeMapper("int8", "java.lang.Long"));
        typeMapperList.add(new TypeMapper("bigint", "java.lang.Long"));
        typeMapperList.add(new TypeMapper("datetime", "java.util.Date"));
        typeMapperList.add(new TypeMapper("timestamp", "java.util.Date"));
        typeMapperList.add(new TypeMapper("boolean", "java.lang.Boolean"));
        typeMapperGroup.setName(DEFAULT_NAME);
        typeMapperGroup.setElementList(typeMapperList);
        typeMapperGroupMap.put(DEFAULT_NAME, typeMapperGroup);

        //初始化表配置
        if (this.columnConfigGroupMap==null) {
            this.columnConfigGroupMap = new LinkedHashMap<>();
        }
        ColumnConfigGroup columnConfigGroup = new ColumnConfigGroup();
        List<ColumnConfig> columnConfigList = new ArrayList<>();
        columnConfigList.add(new ColumnConfig("query", ColumnConfigType.BOOLEAN));
        columnConfigList.add(new ColumnConfig("disable", ColumnConfigType.BOOLEAN));
        columnConfigGroup.setName(DEFAULT_NAME);
        columnConfigGroup.setElementList(columnConfigList);
        columnConfigGroupMap.put(DEFAULT_NAME, columnConfigGroup);
    }

    private String loadTemplate(String name) {
        return FileUtils.getInstance().read(getClass().getResourceAsStream("/template/"+name+".vm")).replaceAll("\r", "");
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

    @Override
    public Map<String, ColumnConfigGroup> getColumnConfigGroupMap() {
        return columnConfigGroupMap;
    }

    @Override
    public void setColumnConfigGroupMap(Map<String, ColumnConfigGroup> columnConfigGroupMap) {
        this.columnConfigGroupMap = columnConfigGroupMap;
    }

    @Override
    public String getCurrColumnConfigGroupName() {
        return currColumnConfigGroupName;
    }

    @Override
    public void setCurrColumnConfigGroupName(String currColumnConfigGroupName) {
        this.currColumnConfigGroupName = currColumnConfigGroupName;
    }
}
