package com.sjhy.plugin.entity;

/**
 * 抽象的可编辑元素
 *
 * @author makejava
 * @version 1.0.0
 * @date 2021/08/11 13:45
 */
public interface AbstractEditorItem<T extends AbstractItem> extends AbstractItem<T> {
    /**
     * 更改文件名称
     *
     * @param name 文件名称
     */
    void changeFileName(String name);

    /**
     * 获取文件名称
     *
     * @return {@link String}
     */
    String fileName();

    /**
     * 改变文件内容
     *
     * @param content 内容
     */
    void changeFileContent(String content);

    /**
     * 获取文件内容
     *
     * @return {@link String}
     */
    String fileContent();
}
