package com.sjhy.plugin.tool;

import com.intellij.database.model.DasColumn;
import com.intellij.database.model.DataType;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 兼容工具
 *
 * @author makejava
 * @date 2023/04/04 17:08
 */
public class CompatibleUtils {

    public static DataType getDataType(DasColumn dasColumn) {
        try {
            // 兼容2022.3.3及以上版本
            Method getDasTypeMethod = dasColumn.getClass().getMethod("getDasType");
            Object dasType = getDasTypeMethod.invoke(dasColumn);
            Method toDataTypeMethod = dasType.getClass().getMethod("toDataType");
            return (DataType) toDataTypeMethod.invoke(dasType);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            // 兼容2022.3.3以下版本
            try {
                Method getDataTypeMethod = dasColumn.getClass().getMethod("getDataType");
                return (DataType) getDataTypeMethod.invoke(dasColumn);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
