package com.sjhy.plugin.tool;

import com.intellij.psi.PsiElement;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.javadoc.PsiDocTag;
import com.intellij.psi.javadoc.PsiDocTagValue;
import com.intellij.psi.javadoc.PsiDocToken;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文档注释工具类
 *
 * @author makejava
 * @version 1.0.0
 * @date 2021/08/16 17:37
 */
public class DocCommentUtils {

    /**
     * 获取注释信息，获取第一条文本类型注释内容，不存在则返回null
     *
     * @param docComment 文档注释
     * @return 注释内容
     */
    public static String getComment(@Nullable PsiDocComment docComment) {
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
     * 获取注释标签。
     *
     * 注释中加入自定义的注释标签，
     * 如：字段排序、是否在列表页显示、是否在详情页显示、使用的控件类型等，用户按自己需求定义：
     * @fieldOrder 1
     * @showInDetail
     * @showInList
     * @controlType input
     * @customTag customTagValue
     *
     * customTag 作为 key，customTagValue 作为 value，存入 Map 中返回。
     * 当注释标签值为空时，自动填充 true 值。
     * 自动排除 "@param" "@return" 注释标签。
     *
     * @param docComment 文档注释
     * @return 包含注释标签数据的Map
     */
    public static Map<String, Object> getCommentTagData(@Nullable PsiDocComment docComment) {
        Map<String, Object> extData = new HashMap<>();
        if (docComment == null) {
            return extData;
        }

        // 忽略的注释标签
        List<String> ignoreTagNameList = Arrays.asList("param", "return");

        // 获取所有注释标签
        PsiDocTag[] tagList = docComment.getTags();
        for (PsiDocTag tag: tagList) {
            String tagName = tag.getName();

            // 跳过忽略的注释标签
            if (ignoreTagNameList.contains(tagName)) {
                continue;
            }

            // 获取注释标签数据
            PsiElement[] dataElementList = tag.getDataElements();
            StringBuilder sb = new StringBuilder();
            for (PsiElement dataElement: dataElementList) {
                sb.append(dataElement.getText());
                if(dataElement instanceof PsiDocTagValue) {
                    // 如果被识别为 PsiDocTagValue，getText() 会被吞吃一个空格
                    // 比如 @testTag 1 2 3
                    // 获取后会，值会变成："12 3"，所以多加一个空格，保持值为："1 2 3"
                    sb.append(" ");
                }
            }
            // 去除前后无效空白，若没有 tagValue，默认填充 true 值
            String tagValue = sb.toString().trim();
            extData.put(tagName, tagValue.length() == 0 ? true : tagValue);
        }

        return extData;
    }

}
