---
layout: post
title: 什么？https://start.spring.io访问不了，本地搭建一个不就行了
categories: Spring
description: Spring
keywords: Spring
---

> 无情不似多情苦，一寸还成千万缕。

![](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/java/45.jpg)

###  前言

`Spring Initializr`从本质上来说就是一个`Web`应用程序，它能为你生成`Spring Boot`项目结构。虽然不能生成应用程序代码，但它能为你提供一个基本的项目结构，以何种编程语言（`Java`,`Kotlin`,`Groovy`）构建的`Maven`或`Gradle`构建说明文件。你只需要写应用程序的代码就好了。


`Spring Initializr` 有几种用法。

1. 通过`Web`界面使用。
2. 通过`Spring Tool Suite`使用。
3. 通过`IntelliJ IDEA`使用。
4. 使用`Spring Boot CLI`使用。

本例主要讲解`Web`界面和IntelliJ IDEA`的使用

### 搭建本地`start.spring.io`

确保本地已安装`maven`环境变量且`settings.xml`已添加`aliyun`源

```xml
 <mirror>
        <id>nexus-aliyun</id>
        <name>Nexus aliyun</name>
        <url>http://maven.aliyun.com/nexus/content/groups/public/</url>
        <mirrorOf>central</mirrorOf>
</mirror>
```

1. 下载源代码构建
    1. `git clone https://github.com/spring-io/start.spring.io.git`
    2. `cd start.spring.io`
    3. `mvn clean install -DskipTests` 时间略长,请耐心等待，会安装`node`和`yarn`依赖
        ![](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/java/46.png)
2. 本地运行应用程序
    1. `cd start-site`
    2. `mvn spring-boot:run`
    3.  然后访问 `http://localhost:8080/`
        ![](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/java/48.png)


### 通过`Spring Initializr`的`Web`界面

要使用`Spring Initializr`，最直接的办法就是用浏览器打开`http://start.spring.io`,你应该能看到以下一个表单，由于上面我们已经在本地搭建好了`Spring Initializr`也可以直接访问`http://localhost:8080/` 查看以下表单
 ![](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/java/47.png)
 
 表单的左侧上方选项是，你想用`Maven`还是`Gradle`来构建项目,何种编程语言来编写代码，以及使用`Spring Boot`的哪个版本。程序默认生成`Maven`项目，并使用`Spring Boot`的最新版本(非里程碑和快照版本)，但你也可以自由选择其他选项。
 
 表单的左侧下方是，你指定项目的一些基本信息。最起码你要提供项目的`Group`、`Artifact`、项目名称、项目描述、报名、打包方式和依赖的`Java`版本。这些 信息是用来生成`Maven`的`pom.xml`文件(或者`Gradle`的`build.gradle`文件)的。
 
 表单右侧要你指定项目依赖，最简单的方法就是在文本框里键入依赖的名称。随着你的输入会出现匹配依赖的列表，选中一个(或多个)依赖，选中的依赖就会加入项目。
 
 填完表单，选好依赖，点击`Generate`按钮，`Spring Initializr`就会为你生成一个项目。 浏览器将会以`ZIP`文件的形式(文件名取决于`Artifact`字段的内容)把这个项目下载下来。根据你的选择，`ZIP`文件的内容也会略有不同。不管怎样，`ZIP`文件都会包含一个极其基础的项目，让你能着手使用`Spring Boot`开发应用程序。
 
 解压项目目录如下：
 ```text
niocoder
├── HELP.md
├── mvnw
├── mvnw.cmd
├── pom.xml
└── src
    ├── main
    │   ├── java
    │   │   └── com
    │   │       └── niocoder
    │   │           └── niocoder
    │   │               └── NiocoderApplication.java
    │   └── resources
    │       ├── application.properties
    │       ├── static
    │       └── templates
    └── test
        └── java
            └── com
                └── niocoder
                    └── niocoder
                        └── NiocoderApplicationTests.java
```

如你所见，项目里基本没有代码，除了几个空目录外，还包含了如下几样东西。

- `pom.xml`: `Maven`构建文件说明
- `NiocoderApplication.java`: 一个带有`main()`方法的类，用于引导启动应用程序
- `NiocoderApplicationTests.java`: 一个空的`JUnit`测试类
- `application.properties`:一个空的`properties`文件，你可以根据需要添加配置属性

在`Spring Boot`应用程序中，就连空目录都有自己的意义。`static`目录放置的是`Web`应用程序的 静态内容(`JavaScript`、样式表、图片，等等)。还有,稍后你将看到，用于呈现模型数据的模板 会放在`templates`目录里。

你很可能会把`Initializr`生成的项目导入`IDE`。

### 在`IntelliJ IDEA`里创建`Spring Boot`项目

要在`IntelliJ IDEA`里创建新的`Spring Boot`应用程序，在`File`菜单里选择`New` > `Project`。选择`customer` 输入`http://localhost:8080` 

 ![](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/java/49.png)
 
 点击`next`
 
  ![](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/java/50.png)
  
  `Spring Boo`t初始化向导的第二屏要求你提供项目的一些基本信息，比如项目名称、`Maven Group`和`Artifact`、`Java`版本，以及你是想用`Maven`还是`Gradle`来构建项目。描述好项目信息之后， 点击`Next`按钮就能看到第三屏了
 
   ![](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/java/51.png)
   
   第三屏就开始问你要往项目里添加什么依赖了。和之前一样，屏幕里的复选框和`Spring Boot`起步依赖是对应的。选完之后点击`Next`就到了向导的最后一屏，点击`finish`按钮，就能在`IDE`里得到一个空的`Spring Boot`项目了。
  
  
  
  


















