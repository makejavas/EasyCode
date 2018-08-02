# EasyCode

### EasyCode能做什么？

EasyCode是基于IntelliJ IDEA Ultimate版开发的一个代码生成插件，主要通过自定义模板（基于velocity）来生成各种你想要的代码。通常用于生成Entity、Dao、Service、Controller。如果你动手能力强还可以用于生成HTML、JS、PHP等代码。理论上来说只要是与数据有关的代码都是可以生成的。

### 使用环境
`IntelliJ IDEA Ultimate版（172+）`

### 支持的数据库类型
因为是基于Database Tool开发，所有Database Tool支持的数据库都是支持的。

包括如下数据库：

1. MySQL
2. SQL Server
3. Oracle
4. PostgreSQL
5. Sqlite
6. Sybase
7. Derby
8. DB2
9. HSQLDB
10. H2

当然支持的数据库类型也会随着Database Tool插件的更新同步更新。


### 功能说明：
* 支持多表同时操作
* 支持同时生成多个模板
* 支持自定义模板
* 支持自定义类型映射（支持正则）
* 支持自定义扩展属性
* 支持对表属性进行手动配置
* 所有配置项目支持分组模式，在不同项目（或选择不同数据库时），只需要切换对应的分组，所有配置统一变化。
### 安装方法
* 该插件已成功添加到官网插件仓库中，现在可以直接在搜索框搜索安装。
* 手动安装方法如下：
1. [ **点击这里** ](../../releases)，下载最新的发行版本。
2. 然后进入到File->Settings->Plugins,点击Install plugin from disk...
![输入图片说明](https://images.gitee.com/uploads/images/2018/0719/143320_ac3b91d7_920085.png "安装插件")
3. 找到你下载好的安装包即可安装
4. 安装完后重启即可

### 仓库直接安装
由于官网审核较慢，通常会比发行版慢两天更新

安装方法：手写点击Browse repositories... 搜索 Easy Code

 **注意：请认准5颗星的Easy Code，两颗星的是我之前发布的老版本（由于账号丢失，不再更新维护）** 

![输入图片说明](https://images.gitee.com/uploads/images/2018/0727/112706_19564a38_920085.png "屏幕截图.png")


### 使用方法
1. 简单的生成代码

首先在IDEA右边找到数据库工具，点击加号添加好对应的数据源

接着在要生成表上面右键，就可以看到EasyCode菜单，以及子菜单（Generate Code,Config Table）生成代码与配置表。

![输入图片说明](../../raw/master/%E6%95%99%E7%A8%8B%E5%9B%BE%E7%89%87/%E5%8D%95%E8%A1%A8%E4%BB%A3%E7%A0%81%E7%94%9F%E6%88%90.gif "单表代码生成.gif")

2. 多表代码生成
可以按住Ctrl键同时选择多张表进行操作。

 **注意：多选的情况下配置信息取首选表（选中的第一张表）。** 

![输入图片说明](../../raw/master/%E6%95%99%E7%A8%8B%E5%9B%BE%E7%89%87/%E5%A4%9A%E8%A1%A8%E4%BB%A3%E7%A0%81%E7%94%9F%E6%88%90.gif "多表代码生成.gif")


**统一配置解释：勾选代表所有选中表统一用这个配置去生成代码，否则只有没配置过表的使用这个配置生成，已配置的使用自己的配置。**

**（注意：没配置过的表在生成代码后都会复用该配置，成为已配置的表）** 

### 添加类型映射

在Other Seeting中可以看到如下配置信息，上面部分是分组信息，可以复制分组，删除分组。

下面的表格是类型映射信息：左边是数据库类型（支持正则），右边是对应的java类型（必须为全称）。可以新增删除

![输入图片说明](../../raw/master/%E6%95%99%E7%A8%8B%E5%9B%BE%E7%89%87/%E6%B7%BB%E5%8A%A0%E7%B1%BB%E5%9E%8B%E6%98%A0%E5%B0%84.gif "添加类型映射.gif")

### 模板配置说明

采用velocit语法编写

![输入图片说明](https://images.gitee.com/uploads/images/2018/0719/150307_7f96fb68_920085.png "屏幕截图.png")


说明文档：

```
属性
$packageName 选择的包名(String)
$author 设置中的作者(String)
$encode 设置的编码(String)
$modulePath 选中的module路径(String)
$projectPath 项目路径(String)
对象
$tableInfo 表对象(TableInfo)
    obj 表原始对象(DasColumn,下面有贴图)
    name 表名（转换后的首字母大写）(String)
    comment 表注释(String)
    fullColumn 所有列(List<ColumnInfo>)
    pkColumn 主键列(List<ColumnInfo>)
    otherColumn 其他列(List<ColumnInfo>)
    savePackageName 保存的包名(String)
    savePath 保存路径(String)
    saveModelName 保存的model名称(String)
columnInfo 列对象(ColumnInfo)
    obj 列原始对象(DbTable,下面有贴图)
    name 列名（首字母小写）(String)
    comment 列注释(String)
    type 列类型（类型全名）(String)
    ext 附加字段（Map类型）(Map<String,Object>)
$tableInfoList 所有选中的表(List<TableInfo>)
$importList 所有需要导入的包集合(Set<String>)
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

![输入图片说明](https://images.gitee.com/uploads/images/2018/0731/091559_94caf1b3_920085.png "屏幕截图.png")

![输入图片说明](https://images.gitee.com/uploads/images/2018/0731/091758_3a8d8994_920085.png "屏幕截图.png")

### 高级货在这里

在这里添加自定义属性，这些属性都是可以动态配置的，而且还可以在模板中获取到这个属性。

这里的关系都是一一对应的，例如：如果配置了disabled属性就可以在columnInfo.ext中拿到你设置的值（如:columnInfo.ext.disabled）

![输入图片说明](../../raw/master/%E6%95%99%E7%A8%8B%E5%9B%BE%E7%89%87/%E8%87%AA%E5%AE%9A%E4%B9%89%E6%89%A9%E5%B1%95%E5%B1%9E%E6%80%A7.gif "自定义扩展属性.gif")


### 配置信息储存在哪里？

使用版本控制的开发人员可加这些信息不要添加至忽略（以便共享配置信息）

软件处于1.1.1版难免会出现问题，手动修改这里可能可以解决一些问题，请谅解。

![输入图片说明](https://images.gitee.com/uploads/images/2018/0719/150823_ffc482f0_920085.png "屏幕截图.png")

###  :sunglasses: 更多玩法就需要自己摸索了，By makejava。

QQ群：373603580
