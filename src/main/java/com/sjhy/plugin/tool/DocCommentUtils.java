package com.sjhy.plugin.tool;

import com.intellij.psi.PsiElement;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.javadoc.PsiDocToken;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

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

}
