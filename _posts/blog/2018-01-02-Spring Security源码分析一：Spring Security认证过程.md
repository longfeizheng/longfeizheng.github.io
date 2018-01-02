---
layout: post
title: Spring Security源码分析一：Spring Security认证过程（待续）
categories: Spring Security
description: Spring Security
keywords: Spring Security
---
> Spring Security是一个能够为基于Spring的企业应用系统提供声明式的安全访问控制解决方案的安全框架。它提供了一组可以在Spring应用上下文中配置的Bean，充分利用了Spring IoC，DI（控制反转Inversion of Control ,DI:Dependency Injection 依赖注入）和AOP（面向切面编程）功能，为应用系统提供声明式的安全访问控制功能，减少了为企业系统安全控制编写大量重复代码的工作。 


## 类图 ##
为了方便理解Spring Security认证流程，特意画了如下的类图，包含相关的核心认证类
[![http://dandandeshangni.oss-cn-beijing.aliyuncs.com/github/Spring%20Security/core-classdiagram.png](http://dandandeshangni.oss-cn-beijing.aliyuncs.com/github/Spring%20Security/core-classdiagram.png "http://dandandeshangni.oss-cn-beijing.aliyuncs.com/github/Spring%20Security/core-classdiagram.png")](http://dandandeshangni.oss-cn-beijing.aliyuncs.com/github/Spring%20Security/core-classdiagram.png "http://dandandeshangni.oss-cn-beijing.aliyuncs.com/github/Spring%20Security/core-classdiagram.png")



















