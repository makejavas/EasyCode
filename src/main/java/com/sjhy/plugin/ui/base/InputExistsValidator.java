package com.sjhy.plugin.ui.base;

import com.intellij.openapi.ui.InputValidator;
import com.sjhy.plugin.tool.StringUtils;

import java.util.Collection;

/**
 * 输入存在验证器
 *
 * @author makejava
 * @version 1.0.0
 * @date 2021/08/10 17:16
 */
public class InputExistsValidator implements InputValidator {

    private Collection<String> itemList;

    public InputExistsValidator(Collection<String> itemList) {
        this.itemList = itemList;
    }

    @Override
    public boolean checkInput(String inputString) {
        return !StringUtils.isEmpty(inputString) && !itemList.contains(inputString);
    }

    @Override
    public boolean canClose(String inputString) {
        return this.checkInput(inputString);
    }
}
