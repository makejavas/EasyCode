package com.sjhy.plugin.constant;

import com.intellij.database.model.DataType;

import java.util.HashMap;
import java.util.Map;

/**
 * @author makejava
 * @version 1.0.0
 * @since 2018/05/04 09:19
 */
public class MybatisTypeMapper {
    public static final MybatisTypeMapper ME = new MybatisTypeMapper();
    private MybatisTypeMapper() {
        init();
    }

    private Map<String, String> map;

    private void init() {
        map = new HashMap<>();
        map.put("varchar(\\(\\d+\\))?", "VARCHAR");
        map.put("text(\\(\\d+\\))?", "VARCHAR");
        map.put("int(\\(\\d+\\))?", "INTEGER");
        map.put("timestamp(\\(\\d+\\))?", "TIMESTAMP");
    }

    public String getTypeName(DataType dataType) {
        String typeName = dataType.getSpecification();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (typeName.matches(entry.getKey())) {
                return entry.getValue();
            }
        }
        return typeName;
    }
}
