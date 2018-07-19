# EasyCode
### 简介：
* 这是一款IDEA插件，主要用于代码生成
* 只适用于IDEA Ultimate版（收费版），因为是基于收费版自带的Database Tool插件开发的，这个插件只有收费版才有。
### 功能说明：
* 支持多表同时操作
* 支持自定义模板
* 支持自定义类型映射（支持正则）
* 支持自定义扩展属性
* 支持对表属性进行手动配置
* 所有配置项目支持分组模式，在不同项目（或选择不同数据库时），只需要切换对应的分组，所有配置统一变化。
### 安装方法
* 该插件正在添加到官网插件仓库中，现在只能手动安装，添加完毕后直接在搜索框搜索安装。
* 手动安装方法如下：
1. 点击这里[https://gitee.com/makejava/EasyCode/releases](https://gitee.com/makejava/EasyCode/releases)，下载最新的发行版本。
2. 然后进入到File->Settings->Plugins,点击Install plugin from disk...
![输入图片说明](https://images.gitee.com/uploads/images/2018/0719/143320_ac3b91d7_920085.png "安装插件")
3. 找到你下载好的安装包即可安装
4. 安装完后重启即可
### 使用方法
1. 简单生成代码
在IDEA右边找到数据库工具，并添加好对应的数据源

![输入图片说明](https://images.gitee.com/uploads/images/2018/0719/144138_fe0fe8da_920085.png "使用方法1")

接着在要生成表上面右键，就可以看到EasyCode菜单，以及子菜单（Generate Code,Config Table）生成代码与配置表。
可以按住Ctrl键同时选择多张表进行操作。多选的情况下配置表只作用与鼠标右键的那张表。

![输入图片说明](https://images.gitee.com/uploads/images/2018/0719/144629_2b4df9f4_920085.png "屏幕截图.png")

接着会弹出下面这个框，可选择包名，多module的情况下可选择module，以及选择路径。然后选择好对应的模板就可以开始生成代码了。

**统一配置解释：勾选代表所有选中表统一用这个配置去生成代码，否则只有没配置过表的使用这个配置生成，已配置的使用自己的配置。**

**（注意：没配置过的表在生成代码后都会复用该配置，成为已配置的表）** 

点击ok生成代码是可能出现位配置类型弹窗，未配置的类型可在设置中配置（后面会介绍），默认会使用java.lang.Object类型代替未配置类型

![输入图片说明](https://images.gitee.com/uploads/images/2018/0719/144959_2208ec87_920085.png "屏幕截图.png")


### 类型配置说明

在Other Seeting中可以看到如下配置信息，上面部分是分组信息，可以复制分组，删除分组。

下面的表格是类型映射信息：左边是数据库类型（支持正则），右边是对应的java类型（必须为全称）。可以新增删除

![输入图片说明](https://images.gitee.com/uploads/images/2018/0719/145936_d0d5371d_920085.png "屏幕截图.png")

### 模板配置说明

采用velocit语法编写

![输入图片说明](https://images.gitee.com/uploads/images/2018/0719/150307_7f96fb68_920085.png "屏幕截图.png")


说明文档：

```
属性
$packageName 选择的包名
$author 设置中的作者
$encode 设置的编码
$modulePath 选中的module路径
对象
$tableInfo 表对象
    obj 表原始对象
    name 表名（转换后的首字母大写）
    comment 表注释
    fullColumn 所有列
    pkColumn 主键列
    otherColumn 其他列
    savePackageName 保存的包名
    savePath 保存路径
    saveModelName 保存的model名称
columnInfo 列对象
    obj 列原始对象
    name 列名（首字母小写）
    comment 列注释
    type 列类型（类型全名）
    ext 附加字段（Map类型）
$tableInfoList 所有选中的表
$importList 所有需要导入的包集合
回调
&callback
    setFileName(String) 设置文件储存名字
    setSavePath(String) 设置文件储存路径，默认使用选中路径
工具
$tool
    firstUpperCase(String) 首字母大写方法
    firstLowerCase(String) 首字母小写方法
    getClsNameByFullName(String) 通过包全名获取类名
    getJavaName(String) 将下划线分割字符串转驼峰命名(属性名)
    getClassName(String) 将下划线分割字符串转驼峰命名(类名)
    append(... Object) 多个数据进行拼接
$time
    currTime(String) 获取当前时间，指定时间格式（默认：yyyy-MM-dd HH:mm:ss）
```

### 高级货在这里

这里的关系都是一一对应的，可以在columnInfo.ext中拿到你设置的值（如:columnInfo.ext.disabled）

![输入图片说明](https://images.gitee.com/uploads/images/2018/0719/150458_2dce31fc_920085.png "屏幕截图.png")

![输入图片说明](https://images.gitee.com/uploads/images/2018/0719/150521_120cf25b_920085.png "屏幕截图.png")

![输入图片说明](https://images.gitee.com/uploads/images/2018/0719/150557_82a4c528_920085.png "屏幕截图.png")


### 配置信息储存位置

使用版本控制的开发人员可加这些信息不要添加至忽略（以便共享配置信息）

软件处于1.0版难免会出现问题，手动修改这里可能可以解决一些问题，请谅解。

![输入图片说明](https://images.gitee.com/uploads/images/2018/0719/150823_ffc482f0_920085.png "屏幕截图.png")

###  :sunglasses: 更多玩法就需要自己摸索了，By makejava。