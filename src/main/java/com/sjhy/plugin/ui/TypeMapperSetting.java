package com.sjhy.plugin.ui;

import com.intellij.openapi.options.Configurable;
import com.sjhy.plugin.entity.TypeMapper;
import com.sjhy.plugin.entity.TypeMapperGroup;
import com.sjhy.plugin.entity.TypeMapperModel;
import com.sjhy.plugin.tool.CloneUtils;
import com.sjhy.plugin.tool.ConfigInfo;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Map;

/**
 * 类型映射设置
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/17 13:10
 */
public class TypeMapperSetting implements Configurable {
    /**
     * 主面板
     */
    private JPanel mainPanel;
    /**
     * 类型映射分组切换下拉框
     */
    private JComboBox<String> typeMapperComboBox;
    /**
     * 分组复制按钮
     */
    private JButton typeMapperCopyButton;
    /**
     * 类型映射表
     */
    private JTable typeMapperTable;
    /**
     * 添加映射按钮
     */
    private JButton addButton;
    /**
     * 移除映射按钮
     */
    private JButton removeButton;
    /**
     * 删除分组按钮
     */
    private JButton deleteButton;

    /**
     * 是否初始化完成
     */
    private boolean init;
    /**
     * 类型映射表模型
     */
    private TypeMapperModel typeMapperModel;

    /**
     * 当前选中分组
     */
    private String currGroupName;
    /**
     * 类型映射分组集合
     */
    private Map<String, TypeMapperGroup> typeMapperGroupMap;
    /**
     * 全局配置服务
     */
    private ConfigInfo configInfo;
    /**
     * 克隆工具类
     */
    private CloneUtils cloneUtils = CloneUtils.getInstance();


    public TypeMapperSetting(ConfigInfo configInfo) {
        this.configInfo = configInfo;
        this.typeMapperGroupMap = cloneUtils.cloneMap(configInfo.getTypeMapperGroupMap());
        this.currGroupName = configInfo.getCurrTypeMapperGroupName();
        //添加类型
        addButton.addActionListener(e -> typeMapperModel.addRow(new TypeMapper("demoColumn", "java.lang.Object")));

        //移除类型
        removeButton.addActionListener(e -> {
            int[] selectRows = typeMapperTable.getSelectedRows();
            // 从后面往前面移除，防止下标错位问题。
            for (int i = selectRows.length - 1; i >= 0; i--) {
                typeMapperModel.removeRow(selectRows[i]);
            }
        });

        //切换分组
        typeMapperComboBox.addActionListener(e -> {
            if (!init) {
                return;
            }
            String value = (String) typeMapperComboBox.getSelectedItem();
            if (value == null) {
                return;
            }
            if (currGroupName.equals(value)) {
                return;
            }
            currGroupName = value;
            refresh();
        });

        //复制分组按钮
        typeMapperCopyButton.addActionListener(e -> {
            String value = JOptionPane.showInputDialog(null, "Input Group Name:", currGroupName + " Copy");
            if (value == null) {
                return;
            }
            if (value.trim().length() == 0) {
                JOptionPane.showMessageDialog(null, "Group Name Can't Is Empty!");
                return;
            }
            if (typeMapperGroupMap.containsKey(value)) {
                JOptionPane.showMessageDialog(null, "Group Name Already exist!");
                return;
            }
            // 克隆对象
            TypeMapperGroup typeMapperGroup = cloneUtils.clone(typeMapperGroupMap.get(currGroupName));
            typeMapperGroup.setName(value);
            typeMapperGroupMap.put(value, typeMapperGroup);
            currGroupName = value;
            refresh();
        });

        //删除分组
        deleteButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(null, "Confirm Delete Group " + typeMapperComboBox.getSelectedItem() + "?", "温馨提示", JOptionPane.OK_CANCEL_OPTION);
            if (result == 0) {
                if (ConfigInfo.DEFAULT_NAME.equals(currGroupName)) {
                    JOptionPane.showMessageDialog(null, "Can't Delete Default Group!");
                    return;
                }
                typeMapperGroupMap.remove(currGroupName);
                currGroupName = ConfigInfo.DEFAULT_NAME;
                refresh();
            }
        });

        // 初始化操作
        init();
    }


    /**
     * 初始化方法
     */
    private void init() {
        //初始化表格
        this.typeMapperModel = new TypeMapperModel();
        this.typeMapperTable.setModel(typeMapperModel);
        refresh();
    }

    /**
     * 刷新方法
     */
    private void refresh() {
        init = false;
        //初始化下拉框
        this.typeMapperComboBox.removeAllItems();
        typeMapperGroupMap.keySet().forEach(this.typeMapperComboBox::addItem);
        this.typeMapperComboBox.setSelectedItem(this.currGroupName);
        this.typeMapperModel.init(this.typeMapperGroupMap.get(currGroupName).getElementList());
        init = true;
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Type Mapper";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return mainPanel;
    }

    @Override
    public boolean isModified() {
        return !typeMapperGroupMap.equals(configInfo.getTypeMapperGroupMap()) || !currGroupName.equals(configInfo.getCurrTypeMapperGroupName());
    }

    @Override
    public void apply() {
        configInfo.setCurrTypeMapperGroupName(currGroupName);
        configInfo.setTypeMapperGroupMap(typeMapperGroupMap);
    }

    @Override
    public void reset() {
        init();
    }
}
