package com.sjhy.plugin.entity;

import com.sjhy.plugin.ui.base.Item;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模板信息类
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/17 13:10
 */
public class Template implements Item {
    /**
     * 模板名称
     */
    private String name;

    public Template() {

    }

    public Template(String name, String code, boolean show) {
        System.out.println("Is Show: " + show);
        this.name = name;
        this.code = code;
        this.show = show;
    }

    /**
     * 模板代码
     */
    private String code;

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isShow() {
        return show;
    }

    public void setShow(boolean show) {
        this.show = show;
    }

    /**
     * 是否能够在生成的时候选择，
     * 主要是为了在生成对应的测试代码，
     * 测试用例的代码和源代码不是在同一个目录里面的
     */
    private boolean show;
}
