# YMATE-MAVEN-PLUGIN

[![Maven Central status](https://img.shields.io/maven-central/v/net.ymate.maven.plugins/ymate-maven-plugin.svg)](https://search.maven.org/artifact/net.ymate.maven.plugins/ymate-maven-plugin)
[![LICENSE](https://img.shields.io/github/license/suninformation/ymate-embed.svg)](https://gitee.com/suninformation/ymate-embed/blob/master/LICENSE)

本项目为基于 [YMP - 轻量级 Java 应用开发框架](https://ymate.net/) 开发的小伙伴儿们提供的一系列 Maven 插件工具，辅助快速生成代码与服务等。

## Maven包依赖

```xml
<plugin>
    <groupId>net.ymate.maven.plugins</groupId>
    <artifactId>ymate-maven-plugin</artifactId>
    <version>1.0.0</version>
</plugin>
```



## 命令行格式

```shell
mvn ymate:<命令> -D<参数1>=<值1> -D<参数n>=<值n>
```



## 命令列表

|命令|说明|
|---|---|
|`apidocs`|接口文档生成器|
|`configuration`|配置体系目录结构生成器|
|`crud`|CRUD 代码生成器|
|`dbquery`|数据库 SQL 查询|
|`decrypt`|字符串解密|
|`encrypt`|字符串加密|
|`entity`|数据实体代码生成器|
|`interceptor`|拦截器类生成器|
|`module`|模块代码生成器|
|`tomcat`|Tomcat 服务配置生成器|
|`validator`|验证器类生成器|



## 插件命令详解



### apidocs

接口文档生成器。

#### 参数列表

| 参数名称              | 必须 | 说明                                                         |
| --------------------- | ---- | ------------------------------------------------------------ |
| packageNames          | 否   | 扫描包名称集合，默认为 `${project.groupId}` ，多个用 `,` 分隔 |
| format                | 否   | 输出格式，默认值：`markdown`，取值范围： `html`、`gitbook`、`postman`、`json`、 `markdown` |
| outputDir             | 否   | 输出路径，默认为当前项目基准路径                             |
| language              | 否   | 自定义语言，默认值：空，系统语言环境                         |
| overwrite             | 否   | 是否覆盖已存在的文件，默认值：`false`                        |
| ignoredRequestMethods | 否   | 忽略的请求方法名称集合，如：`options`                        |

#### 命令示例

```shell
mvn ymate:apidocs -Dformat=postman -Doverwrite=true
```



### configuration

配置体系目录结构生成器。

#### 参数列表

|参数名称|必须|说明|
|---|---|---|
|homeDir|否|配置体系根路径，默认为当前项目基准路径，若指定路径不存在则创建|
|projectName|否|项目名称，默认值：空|
|moduleNames|否|模块名称集合，默认值：空，多个用 `,` 分隔|
|pluginNames|否|插件名称集合，默认值：空，多个用 `,` 分隔|
|repair|否|是否执行缺失文件修复（除目录结构自动补全外，该参数将对缺失的文件进行补全），默认值：false|

#### 命令示例

```shell
mvn ymate:configuration -DprojectName=default -DmoduleNames=webapp,demo -Drepair=true
```



### crud

CRUD 代码生成器。

通过规则配置文件自动生成控制器（Controller）、存储器（Repository）和页面视图（UI）等相关代码。

#### 参数列表

|参数名称|必须|说明|
|---|---|---|
|file|否|规则配置文件，默认值：`misc/crud.json`|
|action|否|指定本次生成哪些代码，默认为全部，取值范围：`controller`、`repository`、`ui`、`ui-cdn`|
|filter|否|指定本次仅生成列表中API或表的代码，默认值：空，多个名称之间用 `,` 分隔|
|fromDb|否|是否通过数据库表结构生成规则配置文件，默认值：`false`|
|simple|否|是否生成规则配置文件样例，默认值：`false`|
|apidocs|否|是否使用 API 文档注解，默认值：`false`|
|test|否|是否生成单元测试代码，默认值：`false`|
|language|否|自定义语言，默认值：空，系统语言环境|

#### 命令示例

- 生成规则配置文件样例

```shell
mvn ymate:crud -Dsimple=true
```

- 根据数据库表结构生成规则配置文件

```shell
mvn ymate:crud -DfromDb=true
```

- 代码生成

```shell
mvn ymate:crud
```



### dbquery

数据库 SQL 查询。

#### 参数列表

|参数名称|必须|说明|
|---|---|---|
|sql|是|SQL 查询语句（必须是 SELECT 语句）|
|dataSource|否|指定数据源名称，默认值：`default`|
|format|否|结果集输出格式，可选值：`table`、`markdown`、`csv`，默认值：`table`|
|dateColumns|否|指定需转换为日期格式的字段名，默认值：空，多个名称之间用 `,` 分隔|
|page|否|查询页号，默认值：`0`|
|pageSize|否|分页大小，默认值：`20`|

#### 命令示例

```shell
mvn ymate:dbquery -Dsql="select * from ym_user" -Dpage=1 -Dformat=csv
```



### decrypt

字符串解密。

#### 参数列表

| 参数名称  | 必须 | 说明                                     |
| --------- | ---- | ---------------------------------------- |
| content   | 是   | 待解密字符串                             |
| passkey   | 否   | 自定义密钥，默认值：空                   |
| implClass | 否   | 自定义密码处理器接口实现类名，默认值：空 |

#### 命令示例

```shell
mvn ymate:decrypt -Dcontent=D3ytOQrD63BlKGDMJnaYsQ==
```



### encrypt

字符串加密。

#### 参数列表

| 参数名称  | 必须 | 说明                                     |
| --------- | ---- | ---------------------------------------- |
| content   | 是   | 待加密字符串                             |
| passkey   | 否   | 自定义密钥，默认值：空                   |
| implClass | 否   | 自定义密码处理器接口实现类名，默认值：空 |

#### 命令示例

```shell
mvn ymate:encrypt -Dcontent=abc12345678
```



### entity

数据实体代码生成器。

更详尽的说明请参考文档：[JDBC - 自动生成实体类](https://ymate.net/guide/persistence/jdbc#自动生成实体类)

#### 参数列表

| 参数名称   | 必须 | 说明                                                         |
| ---------- | ---- | ------------------------------------------------------------ |
| dev        | 否   | 是否使用开发模式，默认为 `false`                             |
| overwrite  | 否   | 是否覆盖已存在的文件，默认为 `false`                         |
| cfgFile    | 否   | 加载指定的框架初始化配置文件，默认为空                       |
| dataSource | 否   | 指定数据源名称，默认为 `default`                             |
| view       | 否   | 是否为视图，默认为 `false`                                   |
| showOnly   | 否   | 是否仅在控制台输出结构信息（不生成任何文件），默认为 `false` |
| format     | 否   | 控制台输出格式，配合 `showOnly` 使用<br />可选值：`table`、`markdown`、`csv`，默认为 `table` |
| beanOnly   | 否   | 是否仅生成 JavaBean（非实体类），默认为 `false`              |
| apidocs    | 否   | 是否使用 `@ApiProperty` 文档注解，配合 `beanOnly` 使用，默认为 `false` |

#### 命令示例

```java
mvn ymate:entity
```



### interceptor

拦截器类生成器。

#### 参数列表

|参数名称|必须|说明|
|---|---|---|
|name|是|拦截器名称|
|packageName|否|拦截器包名，默认值：`${project.groupId}`|

#### 命令示例

```shell
mvn ymate:interceptor -Dname=Demo
```



### module

模块代码生成器。

#### 参数列表

|参数名称|必须|说明|
|---|---|---|
|name|是|模块名称|
|packageName|否|模块包名，默认值：`${project.groupId}`|
|projectName|否|工程构件标识，默认值：`${project.artifactId}`|

#### 命令示例

```shell
mvn ymate:module -Dname=Demo
```



### tomcat

Tomcat 服务配置生成器。

#### 参数列表

|参数名称|必须|说明|
|---|---|---|
|serviceName|是|服务名称（若在 `Windows` 环境下同时为注册服务名称）|
|catalinaHome|是|Tomcat 软件包安装路径，默认值：`${env.CATALINA_HOME}`|
|catalinaBase|否|生成的服务存放的位置，默认为当前路径|
|hostName|否|主机名称，默认值：`localhost`|
|hostAlias|否|别名，默认值：`空`|
|tomcatVersion|否|指定 Tomcat 软件包的版本，默认值：`8`，<br />目前支持：`6`、`7`、`8`、`9`、`10`|
|serverPort|否|服务端口，默认值：`8005`|
|connectorPort|否|容器端口，默认值：`8080`|
|redirectPort|否|重定向端口，默认值：`8443`|
|ajp|否|是否启用 AJP 配置，默认值：`false`|
|ajpHost|否|AJP 主机名称，默认值：`localhost`|
|ajpPort|否|AJP 端口，默认值：`8009`|

#### 命令示例

```shell
mvn ymate:tomcat -DserviceName=DemoServer -DcatalinaHome=/Users/.../apache-tomcat-8.5.75
```

**生成的文件说明：**

|文件名称|说明|
|---|---|
|`conf/server.xml`|Tomcat 服务配置文件|
|`vhost.conf`|与 Nginx 和 Apache Server 整合所需配置|
|`bin/install.bat`|Windows 环境下服务安装脚本|
|`bin/manager.bat`|Windows 环境下启动 Tomcat 服务管理器脚本|
|`bin/shutdown.bat`|Windows 环境下服务停止脚本|
|`bin/startup.bat`|Windows 环境下服务启动脚本|
|`bin/uninstall.bat`|Windows 环境下服务卸载脚本|
|`bin/manager.sh`|Linux 环境下控制服务的启动或停止等操作脚本|
|`webapps/ROOT/index.jsp`|默认首页文件|

**Linux 下服务的启动和停止：**

- 为脚本添加执行权限：

    ```shell
    chmod +x manager.sh
    ```
    
- 启动服务：

    ```shell
    ./manager.sh start
    ```
    
- 停止服务：

    ```shell
    ./manager.sh stop
    ```
    



### validator

验证器类生成器。

#### 参数列表

|参数名称|必须|说明|
|---|---|---|
|name|是|验证器名称|
|packageName|否|验证器包名，默认值：`${project.groupId}`|

#### 命令示例

```shell
mvn ymate:validator -Dname=Demo
```



## One More Thing

YMP 不仅提供便捷的 Web 及其它 Java 项目的快速开发体验，也将不断提供更多丰富的项目实践经验。

感兴趣的小伙伴儿们可以加入官方 QQ 群：[480374360](https://qm.qq.com/cgi-bin/qm/qr?k=3KSXbRoridGeFxTVA8HZzyhwU_btZQJ2)，一起交流学习，帮助 YMP 成长！

如果喜欢 YMP，希望得到你的支持和鼓励！

![Donation Code](https://ymate.net/img/donation_code.png)

了解更多有关 YMP 框架的内容，请访问官网：[https://ymate.net](https://ymate.net)