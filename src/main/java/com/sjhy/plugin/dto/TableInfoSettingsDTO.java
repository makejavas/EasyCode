package com.sjhy.plugin.dto;

import com.intellij.database.model.DasNamespace;
import com.intellij.database.psi.DbElement;
import com.intellij.database.psi.DbTable;
import com.sjhy.plugin.entity.TableInfo;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 表格信息设置传输对象
 *
 * @author makejava
 * @version 1.0.0
 * @date 2021/08/14 17:40
 */
@Data
public class TableInfoSettingsDTO {
    private Map<String, TableInfoDTO> tableInfoMap;

    public TableInfoSettingsDTO() {
        this.tableInfoMap = new HashMap<>(16);
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
            element = element.getParent();
            if (element == null) {
                break;
            }
            // 未必所有的数据库都是存在三层，例如MySQL就只有两层。如果上次层不是Namespace，则不再继续获取
            if (!(element instanceof DasNamespace)) {
                break;
            }
        }
        return builder.toString();
    }

    /**
     * 读取表信息
     *
     * @param dbTable 原始表对象
     * @return 储存的表信息
     */
    public TableInfo readTableInfo(DbTable dbTable) {
        if (dbTable == null) {
            return null;
        }
        String key = generateKey(dbTable);
        TableInfoDTO dto = this.tableInfoMap.get(key);
        if (dto == null) {
            dto = new TableInfoDTO();
            dto.setName(dbTable.getName());
            dto.setComment(dbTable.getComment());
            dto.setPreName("");
            dto.setSaveModelName("");
            dto.setSavePath("");
            dto.setTemplateGroupName("");
            this.tableInfoMap.put(key, dto);
        }
        return null;
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
        if (dbTable == null) {
            return;
        }
    }
}
