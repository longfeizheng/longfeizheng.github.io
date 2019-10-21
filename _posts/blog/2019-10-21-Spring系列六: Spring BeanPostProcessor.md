---
layout: post
title: Spring系列六：Spring BeanPostProcessor
categories: Spring
description: Spring
keywords: Spring
---

> 人如风後入江云，情似雨馀黏地絮。

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring08.jpg](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring08.jpg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring08.jpg")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring08.jpg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring08.jpg")


## 概述

`bean`后处理器允许自定义修改`spring` `bean factory`创建的新`bean`实例。如果你想在`Spring`容器完成实例化、配置和初始化`bean`之后实现一些定制逻辑，我们可以插入一个或多个`BeanPostProcessor`实现。

如果有多个`BeanPostProcessor`实例，我们可以通过设置`order`属性或实现`Ordered`接口来控制执行顺序。

### `Spring BeanPostProcessor`

`BeanPostProcessor`接口由两个回调方法组成，即`postprocessbeforeinitialize()`和`postprocessafterinitialize()`。

对于由容器创建的每个`bean`实例，后处理器都会在调用容器初始化方法之前以及在任何bean初始化回调之后都从容器获得回调。

`bean`后处理器通常检查回调接口，或者使用代理包装`bean`。例如一些`Spring AOP`基础结构类（例如`AbstractAdvisingBeanPostProcessor`）实现了`bean`后处理器，提供代理包装逻辑。

#### 如何创建`BeanPostProcessor`

`spring`中创建一个`bean`后处理器:

1. 实现`BeanPostProcessor`接口。
2. 实现回调方法。

```java
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
 
public class CustomBeanPostProcessor implements BeanPostProcessor
{
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException
    {
        System.out.println("Called postProcessBeforeInitialization() for :" + beanName);
        return bean;
    }
     
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException
    {
        System.out.println("Called postProcessAfterInitialization() for :" + beanName);
        return bean;
    }
}
```

#### 如何注册`BeanPostProcessor`

`ApplicationContext`自动检测实现`BeanPostProcessor`接口的配置元数据中定义的所有`bean`。它将这些`bean`注册为后处理器，以便以后在创建`bean`时调用它们

然后`Spring`将在调用初始化回调方法之前和之后将每个`bean`实例传递给这两个方法，在这两个方法中，你可以按自己喜欢的方式处理`bean`实例。

```java

<beans>
     <bean id="customBeanPostProcessor"
               class="com.howtodoinjava.demo.processors.CustomBeanPostProcessor" />
</beans>
```
### `BeanPostProcessor`被调用方法时

通常，`spring`的`DI`容器会执行以下操作来创建一个`bean`：

1. 通过构造函数或工厂方法重新创建`bean`实例
2. 设置属性值值和对其它`bean`的引用
3. 调用所有`*Aware`接口中定义的`setter`方法
4. 将`bean`实例传递给每个`bean`后处理器的`postProcessBeforeInitialization（）`方法
5. 调用初始化回调方法
6. 将`Bean`实例传递到每个`Bean`后处理器的`postProcessAfterInitialization（）`方法
7. 这个`bean`已经可以被使用了
8. 当容器关闭时，调用销毁回调方法

### `Spring` `BeanPostProcessor`示例

为了展示示例用法，我使用了`EmployeeDAOImpl`类，如下所示:

```java
public class EmployeeDAOImpl implements EmployeeDAO
{
    public EmployeeDTO createNewEmployee()
    {
        EmployeeDTO e = new EmployeeDTO();
        e.setId(1);
        e.setFirstName("Lokesh");
        e.setLastName("Gupta");
        return e;
    }
     
    public void initBean() {
        System.out.println("Init Bean for : EmployeeDAOImpl");
    }
     
    public void destroyBean() {
        System.out.println("Init Bean for : EmployeeDAOImpl");
    }
}
```

该`bean`及其后处理器的配置如下：

```xml
<bean id="customBeanPostProcessor" class="com.howtodoinjava.demo.processors.CustomBeanPostProcessor" />
     
<bean id="dao" class="com.howtodoinjava.demo.dao.EmployeeDAOImpl"  init-method="initBean" destroy-method="destroyBean"/>
```

现在，启动`DI`容器并查看输出：
```jva
ApplicationContext context = new ClassPathXmlApplicationContext("beans.xml");
```
输出：
```java
Called postProcessBeforeInitialization() for : dao
Init Bean for : EmployeeDAOImpl
Called postProcessAfterInitialization() for : dao
```

很显然，在初始化方法之前和之后调用了BeanPostProcessor方法。

---
[![https://niocoder.com/assets/images/qrcode.jpg](https://niocoder.com/assets/images/qrcode.jpg "https://niocoder.com/assets/images/qrcode.jpg")](https://niocoder.com/assets/images/qrcode.jpg "https://niocoder.com/assets/images/qrcode.jpg")



> 🙂🙂🙂关注微信公众号**java干货**
不定期分享干货资料


原文链接：[Spring 5 – Bean scopes](https://howtodoinjava.com/spring-core/spring-bean-scopes/)
