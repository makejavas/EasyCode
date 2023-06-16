package com.sjhy.plugin.dto;

import com.intellij.database.model.DasNamespace;
import com.intellij.database.psi.DbElement;
import com.intellij.database.psi.DbTable;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiClass;
import com.sjhy.plugin.dict.GlobalDict;
import com.sjhy.plugin.entity.TableInfo;
import com.sjhy.plugin.tool.JSON;
import com.sjhy.plugin.tool.ReflectionUtils;
import com.sjhy.plugin.tool.StringUtils;
import lombok.Data;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;

/**
 * 表格信息设置传输对象
 *
 * @author makejava
 * @version 1.0.0
 * @date 2021/08/14 17:40
 */
@Data
public class TableInfoSettingsDTO {
    private Map<String, String> tableInfoMap;

    public TableInfoSettingsDTO() {
        this.tableInfoMap = new TreeMap<>();
    }

    private String generateKey(DbTable dbTable) {
        // 递归添加3层名称作为key，第一层为表名、第二层为名命空间名称、第三层为数据库名
        StringBuilder builder = new StringBuilder();
        DbElement element = dbTable;
        for (int i = 0; i < 3; i++) {
            String name = element.getName();
            if (builder.length() > 0) {
                // 添加分割符
                builder.insert(0, ".");
            }
            builder.insert(0, name);
            try {
                Method method = ReflectionUtils.getDeclaredMethod(element.getClass(), "getParent");
                if (method == null) {
                    break;
                }
                element = (DbElement) method.invoke(element);
            } catch (IllegalAccessException | InvocationTargetException e) {
                break;
            }
            // 未必所有的数据库都是存在三层，例如MySQL就只有两层。如果上次层不是Namespace，则不再继续获取
            if (!(element instanceof DasNamespace)) {
                break;
            }
        }
        return builder.toString();
    }

    private String generateKey(PsiClass psiClass) {
        return psiClass.getQualifiedName();
    }
    /**
     * 读表信息
     *
     * @param psiClass psi类
     * @return {@link TableInfo}
     */
    @SuppressWarnings("Duplicates")
    public TableInfo readTableInfo(PsiClass psiClass) {
        String key = generateKey(psiClass);
        TableInfoDTO dto = decode(this.tableInfoMap.get(key));
        dto = new TableInfoDTO(dto, psiClass);
        this.tableInfoMap.put(key, encode(dto));
        return dto.toTableInfo(psiClass);
    }

    /**
     * 读表信息
     *
     * @param dbTable 数据库表
     * @return {@link TableInfo}
     */
    @SuppressWarnings("Duplicates")
    public TableInfo readTableInfo(DbTable dbTable) {
        String key = generateKey(dbTable);
        TableInfoDTO dto = decode(this.tableInfoMap.get(key));
        // 表可能新增了字段，需要重新合并保存
        dto = new TableInfoDTO(dto, dbTable);
        this.tableInfoMap.put(key, encode(dto));
        return dto.toTableInfo(dbTable);
    }

    /**
     * 保存表信息
     *
     * @param tableInfo 表信息
     */
    public void saveTableInfo(TableInfo tableInfo) {
        if (tableInfo == null) {
            return;
        }
        DbTable dbTable = tableInfo.getObj();
        String key;
        if (dbTable != null) {
            key = generateKey(dbTable);
        } else if (tableInfo.getPsiClassObj() != null) {
            key = generateKey((PsiClass) tableInfo.getPsiClassObj());
        } else {
            Messages.showInfoMessage(tableInfo.getName() + "表配置信息保存失败", GlobalDict.TITLE_INFO);
            return;
        }
        this.tableInfoMap.put(key, encode(TableInfoDTO.valueOf(tableInfo)));
    }

    /**
     * 重置表信息
     *
     * @param dbTable 数据库表
     */
    public void resetTableInfo(DbTable dbTable) {
        String key = generateKey(dbTable);
        this.tableInfoMap.put(key, encode(new TableInfoDTO(null, dbTable)));
    }

    /**
     * 删除表信息
     *
     * @param dbTable 数据库表
     */
    public void removeTableInfo(DbTable dbTable) {
        String key = generateKey(dbTable);
        this.tableInfoMap.remove(key);
    }

    private static String encode(TableInfoDTO tableInfo) {
        return Base64.getEncoder().encodeToString(JSON.toJson(tableInfo).getBytes());
    }

    private static TableInfoDTO decode(String base64) {
        if (StringUtils.isEmpty(base64)) {
            return null;
        }
        return JSON.parse(new String(Base64.getDecoder().decode(base64)), TableInfoDTO.class);
    }
}
