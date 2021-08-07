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
        if (existsAnnotation(field, "org.springframework.data.annotation.Id")) {
            return true;
        }
        if (existsAnnotation(field, "javax.persistence.Id")) {
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
        if (existsAnnotation(field, "org.springframework.data.annotation.Transient")) {
            return true;
        }
        if (existsAnnotation(field, "javax.persistence.Transient")) {
            return true;
        }
        return false;
    }

    private static boolean existsAnnotation(PsiField field, String clsName) {
        return getAnnotation(field, clsName) != null;
    }

    private static PsiAnnotation getAnnotation(PsiField field, String clsName) {
        PsiModifierList list = field.getModifierList();
        return list == null ? null : list.findAnnotation(clsName);
    }
}
