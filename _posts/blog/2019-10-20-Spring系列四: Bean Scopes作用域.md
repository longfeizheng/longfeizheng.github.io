---
layout: post
title: Spring系列四：Bean Scopes作用域
categories: Spring
description: Spring
keywords: Spring
---

> 等闲识得东风面，万紫千红总是春。

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring05.jpg](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring05.jpg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring05.jpg")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring05.jpg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring05.jpg")


## 概述

在`Spring`框架中，我们可以在六个内置的`spring bean`作用域中创建`bean`，还可以定义`bean`范围。在这六个范围中，只有在使用支持`Web`的`applicationContext`时，其中四个可用。`singleton`和`prototype`作用域可用于任何类型的`ioc`容器。

### Spring Bean作用域类型

在`Spring`中，可以使用`spring`中的 `@Scope`注解定义`bean`的作用域。下面我们已经列出这六个在`Spring`应用程序上下文中使用的内置`bean`作用域。这些相同的作用域也适用于`spring boot` `bean`作用域。

| SCOPE  | 描述  |
| ------------ | ------------ |
|  `singleton`  |  `spring IoC`容器存在一个`bean`对象实例。|
| `prototype`   | 与单例相反，每次请求`bean`时，它都会创建一个新实例。 |
| `request`   | 在`HTTP`请求(`Request`) 的完整生命周期中，将创建并使用单个实例。 只适用于`web`环境中`Spring` `ApplicationContext`中有效。 |
|  `session`  | 在`HTTP`会话(`Session`) 的完整生命周期中，将创建并使用单个实例。 只适用于`web`环境中`Spring` `ApplicationContext`中有效。 |
| `application`   | 将在`ServletContext`的完整生命周期中创建并使用单个实例。只适用于`web`环境中`Spring` `ApplicationContext`中有效。 |
|  `websocket`  |  在WebSocket的完整生命周期中，将创建并使用单个实例。 只适用于`web`环境中`Spring` `ApplicationContext`中有效。 |

#### 单例作用域

`singleton`是`spring`容器中bean的默认作用域。它告诉容器仅创建和管理一个`bean`类实例。该单个实例存储在此类单例`bean`的缓存中，并且对该命名`bean`的所有后续请求和引用都返回该缓存的实例。

使用`Java`配置的单例作用域`bean`的示例:

```java
@Component
@Scope("singleton")  //可以省略，默认即是singleton
public class BeanClass {

}
```

使用`XML`配置的单例作用域`bean`的示例:

```XML
<!-- 后面的singleton可以省略 -->
<bean id="beanId" class="cn.howtodoinjava.BeanClass" scope="singleton" />
//or
<bean id="beanId" class="cn.howtodoinjava.BeanClass" />
```

#### 原型作用域

每次应用程序对`Bean`进行请求时，原型作用域都会创建一个新的`Bean`实例。

您应该知道，销毁`bean`生命周期方法不调用原型作用域`bean`，只调用初始化回调方法。因此，作为开发人员，您要负责清理原型作用域的`bean`实例以及其中包含的所有资源。

原型`bean`范围的`Java`配置示例:

```java
@Component
@Scope("prototype")
public class BeanClass {
}

```

原型`bean`范围的`XML`配置示例:

```XML
<bean id="beanId" class="cn.howtodoinjava.BeanClass" scope="prototype" />
```

> 通常，您应该为所有有状态`bean`使用原型范围，为无状态`bean`使用单例范围。


> 要在请求、会话、应用程序和`websocket`范围内使用`bean`，您需要注册`RequestContextListener`或`RequestContextFilter`.

#### request作用域

在请求范围中，容器为每个`HTTP`请求创建一个新实例。因此，如果服务器当前处理50个请求，那么容器最多可以有50个`bean`类的单独实例。对一个实例的任何状态更改对其他实例都是不可见的。一旦请求完成，这些实例就会被销毁。

`request`请求`bean`范围的`Java`配置示例:

```Java
@Component
@Scope("request")
public class BeanClass {
}

//or

@Component
@RequestScope
public class BeanClass {
}
```

`request`请求`bean`范围的`XML`配置示例:

```XML
<bean id="beanId" class="cn.howtodoinjava.BeanClass" scope="request" />
```

#### session作用域

在会话范围中，容器为每个`HTTP`会话创建一个新实例。因此，如果服务器有20个活动会话，那么容器最多可以有20个`bean`类的单独实例。在单个会话生命周期内的所有`HTTP`请求都可以访问该会话范围内相同的单个`bean`实例。

在会话范围内，对一个实例的任何状态更改对其他实例都是不可见的。一旦会话在服务器上被销毁/结束，这些实例就会被销毁。

`session`请求`bean`范围的`Java`配置示例:

```java
@Component
@Scope("session")
public class BeanClass {
}

//or

@Component
@SessionScope
public class BeanClass {
}
```

`session`请求`bean`范围的`XML`配置示例:

```XML
<bean id="beanId" class="cn.howtodoinjava.BeanClass" scope="session" />

```

#### application作用域

在应用程序范围内，容器为每个`web`应用程序运行时创建一个实例。它几乎类似于单例范围，只有两个不同之处。即：

1. 应用程序作用域`bean`是每个`ServletContext`的单例对象，而单例作用域`bean`是每个`ApplicationContext`的单例对象。请注意，单个应用程序可能有多个应用程序上下文。
2. 应用程序作用域`bean`作为`ServletContext`属性可见。

`application` `bean`范围的`Java`配置示例:

```java
@Component
@Scope("application")
public class BeanClass {
}

//or

@Component
@ApplicationScope
public class BeanClass {
}
```

`application` `bean`范围的`XML`配置示例:

```xml
<bean id="beanId" class="com.howtodoinjava.BeanClass" scope="application" />
```

#### websocket作用域

`WebSocket`协议支持客户端和远程主机之间的双向通信，远程主机选择与客户端通信。`WebSocket`协议为两个方向的通信提供了一个单独的`TCP`连接。这对于具有同步编辑和多用户游戏的多用户应用程序特别有用。

在这种类型的`Web`应用程序中，`HTTP`仅用于初始握手。如果服务器同意，服务器可以以`HTTP`状态101（交换协议）进行响应。如果握手成功，则`TCP`套接字保持打开状态，客户端和服务器都可以使用该套接字向彼此发送消息。

`websocket` `bean`范围的`Java`配置示例:

```java
@Component
@Scope("websocket")
public class BeanClass {
}
```


`websocket` `bean`范围的`XML`配置示例:

```xml
<bean id="beanId" class="com.howtodoinjava.BeanClass" scope="websocket" />
```

请注意，`websocket`范围内的`bean`通常是单例的，并且比任何单独的`WebSocket`会话寿命更长。


### 自定义线程作用域

`Spring`还使用类`SimpleThreadScope`提供了非默认线程作用域。若要使用此作用域，必须使用`CustomScopeConfigurer`类将其注册到容器。

```XML
<bean class="org.springframework.beans.factory.config.CustomScopeConfigurer">
    <property name="scopes">
        <map>
            <entry key="thread">
                <bean class="org.springframework.context.support.SimpleThreadScope"/>
            </entry>
        </map>
    </property>
</bean>
```

对`bean`的每个请求都将在同一线程中返回相同的实例。

线程`bean`范围的`Java`配置示例:

```java
@Component
@Scope("thread")
public class BeanClass {
}
```


线程`bean`范围的`xml`配置示例:

```xml
<bean id="beanId" class="cn.howtodoinjava.BeanClass" scope="thread" />
```

## 总结

`Spring framework`提供了六个`Spring` `bean`作用域，每个作用域内的实例具有不同的生命周期跨度。作为开发人员，我们必须明智地选择任何容器管理`bean`的范围。同样，当具有不同作用域的`bean`相互引用时，我们必须做出明智的决定。

请记住以上给出的所有信息来回答任何`spring` `bean`作用域的面试问题。

---
[![https://niocoder.com/assets/images/qrcode.jpg](https://niocoder.com/assets/images/qrcode.jpg "https://niocoder.com/assets/images/qrcode.jpg")](https://niocoder.com/assets/images/qrcode.jpg "https://niocoder.com/assets/images/qrcode.jpg")



> 🙂🙂🙂关注微信公众号**java干货**
不定期分享干货资料


原文链接：[Spring 5 – Bean scopes](https://howtodoinjava.com/spring-core/spring-bean-scopes/)
