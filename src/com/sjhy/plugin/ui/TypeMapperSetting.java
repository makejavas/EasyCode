package com.sjhy.plugin.ui;

import com.intellij.openapi.options.Configurable;
import com.sjhy.plugin.entity.TypeMapper;
import com.sjhy.plugin.entity.TypeMapperGroup;
import com.sjhy.plugin.entity.TypeMapperModel;
import com.sjhy.plugin.service.ConfigService;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class TypeMapperSetting implements Configurable {
    //主面板
    private JPanel mainPanel;
    //类型映射分组切换下拉框
    private JComboBox typeMapperComboBox;
    //分组复制按钮
    private JButton typeMapperCopyButton;
    //类型映射表
    private JTable typeMapperTable;
    //添加映射按钮
    private JButton addButton;
    //移除映射按钮
    private JButton removeButton;
    //删除分组按钮
    private JButton deleteButton;

    //是否初始化完成
    private boolean init;
    //类型映射表模型
    private TypeMapperModel typeMapperModel;

    //当前选中分组
    private String currGroupName;
    //类型映射分组集合
    private Map<String, TypeMapperGroup> typeMapperGroupMap;
    //全局配置服务
    private ConfigService configService;


    TypeMapperSetting(ConfigService configService) {
        this.configService = configService;
        //添加类型
        addButton.addActionListener(e -> typeMapperModel.addRow(new TypeMapper("demoColumn", "java.lang.Object")));

        //移除类型
        removeButton.addActionListener(e -> {
            int[] selectRows = typeMapperTable.getSelectedRows();
            for (int i = selectRows.length-1; i>=0; i--){
                typeMapperModel.removeRow(selectRows[i]);
            }
        });

        //切换分组
        typeMapperComboBox.addActionListener(e -> {
            if (!init){
                return;
            }
            String value = (String) typeMapperComboBox.getSelectedItem();
            if (value==null) {
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
            String value = JOptionPane.showInputDialog(null, "Input Group Name:", currGroupName+" Copy");
            if (value==null) {
                return;
            }
            if (value.trim().length()==0){
                JOptionPane.showMessageDialog(null, "Group Name Can't Is Empty!");
                return;
            }
            if (typeMapperGroupMap.containsKey(value)){
                JOptionPane.showMessageDialog(null, "Group Name Already exist!");
                return;
            }
            TypeMapperGroup typeMapperGroup = typeMapperGroupMap.get(currGroupName).cloneTypeMapperGroup();
            typeMapperGroup.setName(value);
            typeMapperGroupMap.put(value, typeMapperGroup);
            currGroupName = value;
            refresh();
        });

        //删除分组
        deleteButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(null, "Confirm Delete Group "+typeMapperComboBox.getSelectedItem()+"?", "温馨提示", JOptionPane.OK_CANCEL_OPTION);
            if (result==0){
                if(ConfigService.DEFAULT_NAME.equals(currGroupName)){
                    JOptionPane.showMessageDialog(null, "Can't Delete Default Group!");
                    return;
                }
                typeMapperGroupMap.remove(currGroupName);
                currGroupName = ConfigService.DEFAULT_NAME;
                refresh();
            }
        });

        init();
    }


    /**
     * 初始化方法
     */
    private void init() {
        //复制数据
        this.typeMapperGroupMap = new LinkedHashMap<>();
        for (Map.Entry<String, TypeMapperGroup> entry : configService.getTypeMapperGroupMap().entrySet()) {
            this.typeMapperGroupMap.put(entry.getKey(), entry.getValue().cloneTypeMapperGroup());
        }
        this.currGroupName = configService.getCurrTypeMapperGroupName();

        //初始化表格
        this.typeMapperModel = new TypeMapperModel();
        this.typeMapperTable.setModel(typeMapperModel);
        refresh();
    }

    /**
     * 刷新方法
     */
    @SuppressWarnings("unchecked")
    private void refresh() {
        init = false;
        //初始化下拉框
        this.typeMapperComboBox.removeAllItems();
        typeMapperGroupMap.keySet().forEach(this.typeMapperComboBox::addItem);
        this.typeMapperComboBox.setSelectedItem(this.currGroupName);
        this.typeMapperModel.init(this.typeMapperGroupMap.get(currGroupName).getTypeMapperList());
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
        return !typeMapperGroupMap.equals(configService.getTypeMapperGroupMap()) || !currGroupName.equals(configService.getCurrTypeMapperGroupName());
    }

    @Override
    public void apply() {
        configService.setCurrTypeMapperGroupName(currGroupName);
        configService.setTypeMapperGroupMap(typeMapperGroupMap);
    }

    @Override
    public void reset() {
        init();
    }
}
