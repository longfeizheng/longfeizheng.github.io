---
layout: post
title: Spring Boot – 介绍
categories: SpringBoot
description: SpringBoot
keywords: SpringBoot
---

>结发为夫妻，恩爱两不疑。

a

## 概述 ##

`Spring Boot`是一个`Spring`框架模块，为`Spring`框架提供`RAD`（快速应用程序开发）功能。它高度依赖`starter`启动器，该功能非常强大且可以完美运行。

b

### 什么是`starter`启动器？

`Spring`启动器是包含启动特定功能所需的所有相关传递依赖项的集合的模板。例如，如果您想创建一个`Spring` `WebMVC`应用程序，那么在传统的设置中，你应该自己包含所有需要的依赖项。它可能会留下了版本冲突的机会，最终导致运行时异常。

使用`Spring boo`t，要创建`MVC`应用程序，只需导入`spring-boot-starter-web`依赖项即可。

```xml
<!-- Parent pom is mandatory to control versions of child dependencies -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.1.6.RELEASE</version>
    <relativePath />
</parent>
 
<!-- Spring web brings all required dependencies to build web application. -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

在`spring-boot-starter-web`依赖项之上，内部导入所有给定的依赖项并添加到项目中。请注意，有些依赖项是直接的，有些依赖项还会进一步引用其他启动模板，这些启动模板会传递地下载更多的依赖项。

另外，请注意，您不需要将版本信息提供给子依赖项。所有版本都与父启动器的版本相关。

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-json</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-tomcat</artifactId>
    </dependency>
    <dependency>
        <groupId>org.hibernate.validator</groupId>
        <artifactId>hibernate-validator</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-webmvc</artifactId>
    </dependency>
</dependencies>

```

### `Spring Boot`自动配置

使用`@EnableAutoConfiguration`注解启用自动配置。`Spring boot`自动配置扫描类路径，查找类路径中的库，然后尝试猜测它们的最佳配置，最后配置所有这样的`bean`。

自动配置试图尽可能地智能化，并且在你定义更多自己的配置时将会失效。

> 在注册了用户定义的`bea`n之后，总是会应用自动配置。

`Spring`引导自动配置逻辑是在`Spring-boot-autoconfigure.jar`中实现的。你可以在这里验证包的列表。

c

例如，查看`Spring AOP`的自动配置。它执行以下操作

1. 扫描类路径以查看是否存在`EnableAspectJAutoProxy`，`Aspect`，`Advice`和`AnnotatedElement`类。
2. 如果不存在类，则不会为`Spring` `AOP`进行自动配置。
3. 如果找到了类，则使用`Java`配置注解`@EnableAspectJAutoProxy`配置`AOP`。
4. 检查`spring.aop`属性，该值可以为`true`或`false`。
5. 基于属性的值，设置`proxyTargetClass`属性。

```java
@Configuration
@ConditionalOnClass({ EnableAspectJAutoProxy.class, Aspect.class, Advice.class,
        AnnotatedElement.class })
@ConditionalOnProperty(prefix = "spring.aop", name = "auto", havingValue = "true", matchIfMissing = true)
public class AopAutoConfiguration 
{
 
    @Configuration
    @EnableAspectJAutoProxy(proxyTargetClass = false)
    @ConditionalOnProperty(prefix = "spring.aop", name = "proxy-target-class", havingValue = "false", matchIfMissing = false)
    public static class JdkDynamicAutoProxyConfiguration {
 
    }
 
    @Configuration
    @EnableAspectJAutoProxy(proxyTargetClass = true)
    @ConditionalOnProperty(prefix = "spring.aop", name = "proxy-target-class", havingValue = "true", matchIfMissing = true)
    public static class CglibAutoProxyConfiguration {
 
    }
 
}
```

### 嵌入式服务器

`Spring Boot`应用程序始终将`tomcat`作为嵌入式服务器依赖项。这意味着你可以从命令提示符运行`Spring Boot`应用程序，而无需使用复杂的服务器基础结构。

如果需要，可以排除`tomcat`并包含任何其他嵌入式服务器。或者，你可以完全排除服务器环境。全部基于配置。

例如，下面的配置排除了`tomcat`，并将`jetty`包含为嵌入式服务器。

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <exclusions>
        <exclusion>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-tomcat</artifactId>
        </exclusion>
    </exclusions>
</dependency>
 
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jetty</artifactId>
</dependency>
```

### 引导应用程序

要运行应用程序，我们需要使用`@SpringBootApplication`注解。这等同于`@Configuration`、`@EnableAutoConfiguration`和`@ComponentScan`。

它支持扫描配置类、文件并将它们加载到`spring`上下文中。在下面的例子中，执行从`main()`方法开始。它开始加载所有的配置文件，配置它们并根据应用程序中的`application.properties`引导应用程序。`application.properties`文件在`/resources`文件夹中。

```java
@SpringBootApplication
public class MyApplication 
{
    public static void main(String[] args) 
    {
        SpringApplication.run(Application.class, args);
    }
}
```

```yaml
### Server port #########
server.port=8080
  
### Context root ########
server.contextPath=/home
```

要执行应用程序，可以从`IDE`如`eclipse`中运行`main()`方法，或者构建`jar`文件并从命令提示符中执行。

```text
$ java -jar spring-boot-demo.jar
```

### `Spring Boot`的优点

- `Spring boot`帮助解决依赖冲突。它识别所需的依赖项并为你自动导入它们。
- 它包含所有依赖项的兼容版本信息。它最小化了运行时类加载器问题。
- 它的`约定大于配置`方法帮助你在幕后配置最重要的部分。只有在需要时才重写它们。否则，一切都很完美。它有助于避免重复代码、注释和`XML`配置。
- 它提供了嵌入式`HTTP`服务器`Tomcat`，因此你可以快速开发和测试。
- 它与`eclipse`和`intelliJ idea`等`IDE`具有出色的集成。



原文链接：[Spring Boot Tutorial](https://howtodoinjava.com/spring-boot-tutorials/)
