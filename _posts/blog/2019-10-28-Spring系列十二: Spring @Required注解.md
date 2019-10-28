---
layout: post
title: Spring系列十二：Spring @Required注解
categories: Spring
description: Spring
keywords: Spring
---

> 无情不似多情苦，一寸还成千万缕。

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring16.jpg](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring16.jpg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring16.jpg")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring16.jpg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring16.jpg")


## 概述

在生产级应用程序中，`IoC`容器中可能声明了成百上千个`bean`，它们之间的依赖关系通常非常复杂。`setter`注入的一个缺点是，很难检查是否设置了所有必需的属性。使用`<bean>`的`”dependency-check”`属性，可以检查属性值是否已设置，但不能检查其值是否设置为`null`或非`null`值。

除了使用依赖性检查来验证依赖性之外，还可以使用`@Required`注解来检查设置的值是否为非空值。

### `@Required`注解用法

#### `setter`方法上面加上`@Required`注解

在类文件中的`bean`属性的`setter`方法上使用`@Required`注解，如下所示：

```java
public class EmployeeFactoryBean extends AbstractFactoryBean<Object>
{
    private String designation;
     
    public String getDesignation() {
        return designation;
    }
 
    @Required
    public void setDesignation(String designation) {
        this.designation = designation;
    }
     
    //more code here
}
```

#### 注册`RequiredAnnotationBeanPostProcessor`类

`RequiredAnnotationBeanPostProcessor`是一个`Spring` `bean`后处理器，用于检查是否已设置所有带有`@Required`注解的`bean`属性。要启用此`bean`后处理器进行属性检查，必须在`Spring` `IoC`容器中注册它。

```xml
<bean class="org.springframework.beans.factory.annotation.RequiredAnnotationBeanPostProcessor" />
```

### 测试

如果没有设置任何具有`@Required`的属性，则此`bean`后处理器将抛出`BeanInitializationException`。例如，创建`EmployeeFactoryBean`类的实例而不传递用于指定的属性值，则将收到以下错误。

```xml
<bean id="manager"  class="com.howtodoinjava.demo.factory.EmployeeFactoryBean">
    <!-- <property name="designation" value="Manager" /> -->
</bean>
 
<bean class="org.springframework.beans.factory.annotation.RequiredAnnotationBeanPostProcessor" />
```

抛出以下错误信息：
```java
Caused by: org.springframework.beans.factory.BeanInitializationException: Property 'designation' is required for bean 'manager'
    at org.springframework.beans.factory.annotation.RequiredAnnotationBeanPostProcessor.postProcessPropertyValues(RequiredAnnotationBeanPostProcessor.java:156)
    at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.populateBean(AbstractAutowireCapableBeanFactory.java:1202)
    at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:537)
    ... 11 more
```

要纠正此问题，请通过取消注释`applicationContext.xml`文件中的行来传递指定值。

这样，就可以使用`@Required`注解和 `RequiredAnnotationBeanPostProcessor`类来验证在上下文初始化时是否已正确设置了所有必需的`bean`属性。

---
[![https://niocoder.com/assets/images/qrcode.jpg](https://niocoder.com/assets/images/qrcode.jpg "https://niocoder.com/assets/images/qrcode.jpg")](https://niocoder.com/assets/images/qrcode.jpg "https://niocoder.com/assets/images/qrcode.jpg")



> 🙂🙂🙂关注微信公众号**java干货**
不定期分享干货资料


原文链接：[Spring @Required Annotation](https://howtodoinjava.com/spring-core/spring-required-annotation-and-requiredannotationbeanpostprocessor-example/)
