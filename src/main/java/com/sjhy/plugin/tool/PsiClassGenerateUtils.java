package com.sjhy.plugin.tool;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiModifierList;

/**
 * 从实体类生成代码业务工具类
 *
 * @author Mario Luo
 */
public final class PsiClassGenerateUtils {

    private PsiClassGenerateUtils() {
    }

    /**
     * 是否是主键字段
     */
    public static boolean isPkField(PsiField field) {
        if("id".equals(field.getName())) {
            return true;
        }
        PsiAnnotation idAnnotation = field.getAnnotation("org.springframework.data.annotation.Id");
        if (idAnnotation != null) {
            return true;
        }
        idAnnotation = field.getAnnotation("javax.persistence.Id");
        if (idAnnotation != null) {
            return true;
        }
        return false;
    }

    /**
     * 是否需要跳过该字段
     */
    public static boolean isSkipField(PsiField field) {
        PsiModifierList modifierList = field.getModifierList();
        if(modifierList != null && modifierList.hasExplicitModifier(PsiModifier.STATIC)) {
            return true;
        }
        PsiAnnotation annotation = field.getAnnotation("org.springframework.data.annotation.Transient");
        if (annotation != null) {
            return true;
        }
        annotation = field.getAnnotation("javax.persistence.Transient");
        if (annotation != null) {
            return true;
        }
        return false;
    }
}
