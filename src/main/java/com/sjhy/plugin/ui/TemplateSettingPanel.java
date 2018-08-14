package com.sjhy.plugin.ui;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.sjhy.plugin.entity.Template;
import com.sjhy.plugin.entity.TemplateGroup;
import com.sjhy.plugin.tool.CloneUtils;
import com.sjhy.plugin.tool.ConfigInfo;
import com.sjhy.plugin.ui.base.BaseGroupPanel;
import com.sjhy.plugin.ui.base.BaseItemSelectPanel;
import com.sjhy.plugin.ui.base.TemplateEditor;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Map;

/**
 * 模板编辑主面板
 *
 * @author makejava
 * @version 1.0.0
 * @since 2018/07/18 09:33
 */
public class TemplateSettingPanel implements Configurable {
    /**
     * 配置信息
     */
    private ConfigInfo configInfo;

    /**
     * 编辑框面板
     */
    private TemplateEditor templateEditor;

    /**
     * 基本的分组面板
     */
    private BaseGroupPanel baseGroupPanel;

    /**
     * 基本的元素选择面板
     */
    private BaseItemSelectPanel<Template> baseItemSelectPanel;

    /**
     * 当前分组
     */
    private Map<String, TemplateGroup> group;

    /**
     * 当前选中分组
     */
    private String currGroupName;

    /**
     * 克隆工具
     */
    private CloneUtils cloneUtils;

    /**
     * 项目对象
     */
    private Project project;

    public TemplateSettingPanel() {
        // 存在打开的项目则使用打开的项目，否则使用默认项目
        ProjectManager projectManager = ProjectManager.getInstance();
        Project[] openProjects = projectManager.getOpenProjects();
        // 项目对象
        this.project = openProjects.length>0?openProjects[0] : projectManager.getDefaultProject();
        // 配置服务实例化
        this.configInfo = ConfigInfo.getInstance();
        // 克隆工具实例化
        this.cloneUtils = CloneUtils.getInstance();
        // 克隆对象
        this.currGroupName = this.configInfo.getCurrTemplateGroupName();
        this.group = this.cloneUtils.cloneMap(this.configInfo.getTemplateGroupMap());
    }

    /**
     * 获取设置显示的名称
     *
     * @return 名称
     */
    @Nls
    @Override
    public String getDisplayName() {
        return "Template Setting";
    }

    /**
     * 获取主面板对象
     *
     * @return 主面板对象
     */
    @Nullable
    @Override
    public JComponent createComponent() {
        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout());

        // 实例化分组面板
        this.baseGroupPanel = new BaseGroupPanel(new ArrayList<>(group.keySet())) {
            @Override
            protected void createGroup(String name) {

            }

            @Override
            protected void deleteGroup(String name) {

            }

            @Override
            protected void copyGroup(String name) {

            }

            @Override
            protected void changeGroup(String name) {

            }
        };

        // 创建元素选择面板
        this.baseItemSelectPanel = new BaseItemSelectPanel<Template>(group.get(currGroupName).getElementList()) {
            @Override
            protected void addItem(String name) {

            }

            @Override
            protected void copyItem(Template item) {

            }

            @Override
            protected void deleteItem(Template item) {

            }

            @Override
            protected void selectedItem(Template item) {
                // 如果编辑面板已经实例化，需要选释放后再初始化
                if (templateEditor == null) {
                    FileType velocityFileType = FileTypeManager.getInstance().getFileTypeByExtension("vm");
                    templateEditor = new TemplateEditor(project, item.getName() + ".vm", item.getCode(), TEMPLATE_DESCRIPTION_INFO, velocityFileType);
                    // 代码修改回调
                    templateEditor.setCallback(() -> onUpdate());
                    baseItemSelectPanel.getRightPanel().add(templateEditor.createComponent(), BorderLayout.CENTER);
                } else {
                    // 代码修改回调
                    templateEditor.setCallback(() -> onUpdate());
                    templateEditor.reset(item.getName(), item.getCode());
                }
            }
        };

        mainPanel.add(baseGroupPanel, BorderLayout.NORTH);
        mainPanel.add(baseItemSelectPanel.getComponent(), BorderLayout.CENTER);
        return mainPanel;
    }

    /**
     * 数据发生修改时调用
     */
    private void onUpdate() {
        // 同步修改的代码
        baseItemSelectPanel.getSelectedItem().setCode(templateEditor.getEditor().getDocument().getText());
    }

    /**
     * 配置是否修改过
     *
     * @return 是否修改过
     */
    @Override
    public boolean isModified() {
        return !configInfo.getTemplateGroupMap().equals(group) || !configInfo.getCurrTemplateGroupName().equals(currGroupName);
    }

    /**
     * 保存方法
     */
    @Override
    public void apply() {
        configInfo.setTemplateGroupMap(group);
        configInfo.setCurrTemplateGroupName(currGroupName);
    }

    /**
     * 重置方法
     */
    @Override
    public void reset() {
        if (!isModified()) {
            return;
        }
        // 防止对象篡改，需要进行克隆
        this.group = cloneUtils.cloneMap(configInfo.getTemplateGroupMap());
        this.currGroupName = configInfo.getCurrTemplateGroupName();
        // 重置元素选择面板
        baseItemSelectPanel.reset(this.group.get(this.currGroupName).getElementList());
    }

    /**
     * 关闭回调方法
     */
    @Override
    public void disposeUIResources() {
        // 修复兼容性问题
        if (templateEditor != null) {
            templateEditor.onClose();
        }
    }


    /**
     * 模板描述信息，说明文档
     */
    private static final String TEMPLATE_DESCRIPTION_INFO = "<pre>\n" +
            "说明文档：\n" +
            "属性\n" +
            "$packageName 选择的包名\n" +
            "$author 设置中的作者\n" +
            "$encode 设置的编码\n" +
            "$modulePath 选中的module路径\n" +
            "$projectPath 项目绝对路径\n" +
            "对象\n" +
            "$tableInfo 表对象\n" +
            "    obj 表原始对象\n" +
            "    name 表名（转换后的首字母大写）\n" +
            "    comment 表注释\n" +
            "    fullColumn 所有列\n" +
            "    pkColumn 主键列\n" +
            "    otherColumn 其他列\n" +
            "    savePackageName 保存的包名\n" +
            "    savePath 保存路径\n" +
            "    saveModelName 保存的model名称\n" +
            "columnInfo 列对象\n" +
            "    obj 列原始对象\n" +
            "    name 列名（首字母小写）\n" +
            "    comment 列注释\n" +
            "    type 列类型（类型全名）\n" +
            "    ext 附加字段（Map类型）\n" +
            "$tableInfoList 所有选中的表\n" +
            "$importList 所有需要导入的包集合\n" +
            "回调\n" +
            "&callback\n" +
            "    setFileName(String) 设置文件储存名字\n" +
            "    setSavePath(String) 设置文件储存路径，默认使用选中路径\n" +
            "工具\n" +
            "$tool\n" +
            "    firstUpperCase(String) 首字母大写方法\n" +
            "    firstLowerCase(String) 首字母小写方法\n" +
            "    getClsNameByFullName(String) 通过包全名获取类名\n" +
            "    getJavaName(String) 将下划线分割字符串转驼峰命名(属性名)\n" +
            "    getClassName(String) 将下划线分割字符串转驼峰命名(类名)\n" +
            "    append(... Object) 多个数据进行拼接\n" +
            "$time\n" +
            "    currTime(String) 获取当前时间，指定时间格式（默认：yyyy-MM-dd HH:mm:ss）\n" +
            "</pre>";
}
