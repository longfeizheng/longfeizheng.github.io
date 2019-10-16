---
layout: post
title: Spring系列二：IoC 容器
categories: Spring
description: Spring
keywords: Spring
---

> 还君明珠双泪垂，恨不相逢未嫁时。

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring02.jpg](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring02.jpg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring02.jpg")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring02.jpg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring02.jpg")


## 概述

`Spring IoC`容器是`Spring`框架的核心。只需要进行简单的容器配置，就可以将创建对象，使用对象，销毁对象联系在一起，从而管理从创建对象到销毁对象的整个生命周期。`Spring`容器使用依赖项注入（`DI`）来管理组成应用程序的组件。

Spring提供以下两种类型的容器。

1. `BeanFactory` 容器
2. `ApplicationContext` 容器

### BeanFactory

`BeanFactory`本质上就是一个高级工厂的接口，该工厂能够维护不同`bean`及其依赖项的对象。

`BeanFactory`使我们能够读取`bean`定义并使用`bean`工厂访问它们

```Java
InputStream is = new FileInputStream("beans.xml");
BeanFactory factory = new XmlBeanFactory(is);

//Get bean
HelloWorld obj = (HelloWorld) factory.getBean("helloWorld");
```

创建`bean`工厂的其他方法如下：

```Java
Resource resource = new FileSystemResource("beans.xml");
BeanFactory factory = new XmlBeanFactory(resource);

ClassPathResource resource = new ClassPathResource("beans.xml");
BeanFactory factory = new XmlBeanFactory(resource);
```

基本上都是使用`getBean（String）`方法在容器中检测并获取实例。 `BeanFactory`接口里面的方法非常简单

####  BeanFactory methods

`BeanFactory`接口主要有以下方法由客户端调用

1. ` boolean containsBean（String）`：如果`BeanFactory`包含与给定名称匹配的`bean`定义或`bean`实例，则返回`true`
2. `Object getBean（String）`：返回给定名称注册的`bean`的实例。根据`BeanFactory`如何配置`Bean`，将返回单个实例或者共享实例或新创建的`Bean`。当找不到该`bean`（在这种情况下它将是`NoSuchBeanDefinitionException`），或者在实例化和准备该`bean`时发生异常时，将抛出`BeansException`。
3. `Object getBean（String，Class）`：返回给定名称注册的`bean`。返回的`bean`将被强制转换为给定的`Class`。如果无法投射`bean`，则将引发相应的异常（`BeanNotOfRequiredTypeException`）。此外，适用`getBean（String）`方法的所有规则
4. `Class getType（String name）`：返回具有给定名称的`Bean`的`Class`。如果找不到与给定名称对应的`bean`，则将引发`NoSuchBeanDefinitionException`
5. `boolean isSingleton（String）`：确定给定名称注册的`bean`定义或`bean`实例是否为单例。如果找不到与给定名称对应的bean，则将引发`NoSuchBeanDefinitionException`
6. `String [] getAliases（String）`：返回给定`bean`名称的别名（如果在`bean`定义中定义了别名）

###  ApplicationContext

`ApplicationContext`容器添加了更多企业特定功能，例如从属性文件解析文本消息的功能以及将应用程序事件发布到感兴趣的事件侦听器的功能。该容器由`org.springframework.context.ApplicationContext`接口定义。

`ApplicationContext`容器包含`BeanFactory`容器的所有功能，因此通常建议在`BeanFactory`上使用它。 `BeanFactory`仍可用于轻量级应用程序，例如移动设备或基于`applet`的应用程序等。

#### ApplicationContext类型

最常用的`ApplicationContext`实现如下：

1. `FileSystemXmlApplicationContext` –此容器从`XML`文件加载`Bean`的定义。在这里，您需要向构造函数提供`XML bean`配置文件的完整路径。
2. `ClassPathXmlApplicationContext` –此容器从`XML`文件加载`Bean`的定义。在这里，您无需提供`XML`文件的完整路径，但需要正确设置`CLASSPATH`，因为此容器将在`CLASSPATH`中查找`bean`配置`XML`文件。
3. `WebXmlApplicationContext` –此容器从`Web`应用程序中加载带有所有`bean`定义的`XML`文件。

#### 如何创建ApplicationContext

用于应用程序上下文实例化的示例代码如下所示。

```java
ApplicationContext context = new FileSystemXmlApplicationContext("beans.xml");
HelloWorld obj = (HelloWorld) context.getBean("helloWorld");
```
---
[![https://niocoder.com/assets/images/qrcode.jpg](https://niocoder.com/assets/images/qrcode.jpg "https://niocoder.com/assets/images/qrcode.jpg")](https://niocoder.com/assets/images/qrcode.jpg "https://niocoder.com/assets/images/qrcode.jpg")



> 🙂🙂🙂关注微信公众号**java干货**
不定期分享干货资料


原文链接：[Spring – IoC Containers](https://howtodoinjava.com/spring-core/different-spring-ioc-containers/)
