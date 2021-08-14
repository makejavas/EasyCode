package com.sjhy.plugin.dto;

import lombok.Data;

import java.util.List;

/**
 * 表格信息传输对象
 *
 * @author makejava
 * @version 1.0.0
 * @date 2021/08/14 17:28
 */
@Data
public class TableInfoDTO {
    /**
     * 表名（首字母大写）
     */
    private String name;
    /**
     * 表名前缀
     */
    private String preName;
    /**
     * 注释
     */
    private String comment;
    /**
     * 模板组名称
     */
    private String templateGroupName;
    /**
     * 所有列
     */
    private List<ColumnInfoDTO> fullColumn;
    /**
     * 保存的包名称
     */
    private String savePackageName;
    /**
     * 保存路径
     */
    private String savePath;
    /**
     * 保存的model名称
     */
    private String saveModelName;
}
