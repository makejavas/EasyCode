package com.sjhy.plugin.tool;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageDialogBuilder;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.ExceptionUtil;
import com.sjhy.plugin.constants.MsgValue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 消息弹框工具类
 * 对message dialog弹框进行兼容处理
 *
 * @author makejava
 * @version 1.0.0
 * @since 2021/02/01 15:36
 */
public class MessageDialogUtils {

    /**
     * yes no确认框
     *
     * @param msg 消息
     * @return 是否确认
     */
    public static boolean yesNo(String msg) {
        return yesNo(null, msg);
    }

    /**
     * yes no确认框
     *
     * @param project 项目对象
     * @param msg     消息
     * @return 是否确认
     */
    public static boolean yesNo(Project project, String msg) {
        MessageDialogBuilder builder = MessageDialogBuilder.yesNo(MsgValue.TITLE_INFO, msg);
        Method method;
        // 新版本兼容
        try {
            method = builder.getClass().getMethod("ask", Project.class);
            return (boolean) method.invoke(builder, project);
        } catch (NoSuchMethodException e) {
            // 旧版本兼容
            try {
                method = builder.getClass().getMethod("isYes");
                return (boolean) method.invoke(builder);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e1) {
                ExceptionUtil.rethrow(e1);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            ExceptionUtil.rethrow(e);
        }
        return false;
    }

    /**
     * 显示确认框
     *
     * @param msg     确认框消息
     * @param project 项目
     * @return 点击按钮
     */
    public static int yesNoCancel(Project project, String msg, String yesText, String noText, String cancelText) {
        MessageDialogBuilder builder = MessageDialogBuilder
                .yesNoCancel(MsgValue.TITLE_INFO, msg)
                .yesText(yesText)
                .noText(noText)
                .cancelText(cancelText);
        Method method;
        // 新版本兼容
        try {
            method = builder.getClass().getMethod("show", Project.class);
            return (int) method.invoke(builder, project);
        } catch (NoSuchMethodException e) {
            // 旧版本兼容
            try {
                method = builder.getClass().getMethod("show");
                return (int) method.invoke(builder);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e1) {
                ExceptionUtil.rethrow(e1);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            ExceptionUtil.rethrow(e);
        }
        return Messages.NO;
    }

}
