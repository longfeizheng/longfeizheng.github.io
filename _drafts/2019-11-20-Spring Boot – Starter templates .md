---
layout: post
title: Spring Boot – Starter templates
categories: SpringBoot
description: SpringBoot
keywords: SpringBoot
---

>未老莫还乡，还乡须断肠。

a

## 概述 ##

不久之前，随着库及其依赖项的指数级增长，依赖项管理变得非常复杂，需要大量的技术专业知识才能正确完成。通过引入`String boot starter`模板。
如果你想在项目中使用任何流行的库，那么这对你如何在项目中使用的正确依赖项方面有很多帮助。

`Spring Boot`默认带有`50`多个不同的启动器模块，这些模块为许多不同的框架提供了现成的集成库，例如关系和`NoSQL`的数据库连接，`Web`服务，社交网络集成，监视库，日志记录，模板渲染，并且列表一直在继续。

### `Starter templates`如何工作？

`Spring`启动器是包含启动特定功能所需的所有相关传递依赖项的集合的模板。每个`starter`都有一个特殊的文件，其中包含`Spring`提供的所有依赖项的列表。

这些文件可以在各自的`starter`模块中的`pom.xml`文件中找到。如。

`spring-boot-starter-data-jpa`启动器`pom`文件可以在[github](https://github.com/spring-projects/spring-boot/blob/master/spring-boot-project/spring-boot-starters/spring-boot-starter-data-jpa/pom.xml)上找到。

这告诉我们，通过在我们的构建中包含`spring-boot-starter-data-jpa`依赖项，我们将自动获得`spring-orm`，`hibernate-entity-manager`和`spring-data-jpa`。这些库将为我们提供开始编写`JPA / DAO`代码的所有基本东西。

所以，下次当你想给你的项目任何具体的功能，我将建议检查现有的启动模板，看看你是否可以直接使用它。社区也在不断的增加启动模板，所以这个列表已经很长了。

### 一些流行的启动模板及其传递依赖

我列出了一些非常频繁使用`spring`的启动程序，以及它们带来的依赖关系，仅供参考。

|  启动器 |  依赖项 |
| ------------ | ------------ |
|  `spring-boot-starter` | `spring-boot`, `spring-context`, `spring-beans`  |
| `spring-boot-starter-jersey	`  | `jersey-container-servlet-core`, `jersey-container-servlet`, `jersey-server`
  |
|  `spring-boot-starter-actuator` | `spring-boot-actuator`, `micrometer-core`
  |
| `spring-boot-starter-aop	` | `spring-aop`, `aspectjrt`, `aspectjweaver`
  |
| `spring-boot-starter-data-rest	` | `spring-hateoas`, `spring-data-rest-webmvc`
  |
| `spring-boot-starter-hateoas	`  | `spring-hateoas`
  |
|  `spring-boot-starter-logging	` | `logback-classic`, `jcl-over-slf4j`, `jul-to-slf4j`
  |
|  `spring-boot-starter-log4j2	` | `log4j2`, `log4j-slf4j-impl`
  |
| `spring-boot-starter-security	`  | `spring-security-web`, `spring-security-config`
  |
| `spring-boot-starter-test	`  | `spring-test`, `spring-boot`,`junit,mockito`, `hamcrest-library`, `assertj`, `jsonassert`, `json-path`
  |
| `spring-boot-starter-web-services	`  | `spring-ws-core`
  |



原文链接：[Spring-boot-starter Maven Templates](https://howtodoinjava.com/spring-boot2/spring-boot-starter-templates/)
