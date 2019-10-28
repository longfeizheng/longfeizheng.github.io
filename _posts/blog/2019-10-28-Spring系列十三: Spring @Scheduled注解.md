---
layout: post
title: Spring系列十三：Spring @Scheduled注解
categories: Spring
description: Spring
keywords: Spring
---

> 唯将终夜长开眼，报答平生未展眉。

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring17.jpg](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring17.jpg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring17.jpg")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring17.jpg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring17.jpg")


## 概述

`Spring`使用`@Scheduled`注解为基于`cron`表达式的任务调度和异步方法执行提供了出色的支持。可以将`@Scheduled`注解与触发器元数据一起添加到方法中。

在本文中，我们将展示以4种不同方式使用`@Scheduled`功能的方法。

### `@Scheduled`注解概述

`@Scheduled`注解用于任务调度。触发器信息需要与此注解一起提供。可以使用属性`fixedDelay`/`fixedRate`/`cron`来提供触发信息。

1. `fixedRate`使`Spring`定期运行任务，即使最后一次调用可能仍在运行。
2. `fixedDelay`专门控制最后一次执行结束时的下一次执行时间。
3. `cron`是源自`Unix cron`实用程序的功能，根据你的要求有多种选择。

```java
@Scheduled(fixedDelay =30000)
public void demoServiceMethod () {... }
 
@Scheduled(fixedRate=30000)
public void demoServiceMethod () {... }
 
@Scheduled(cron="0 0 * * * *")
public void demoServiceMethod () {... }
```

#### 启用`@Scheduled`注解

要在`Spring`应用程序中使用`@Scheduled`，必须首先在`applicationConfig.xml`文件中定义以下`xml`命名空间和模式位置定义。还添加`task：annotation-driven`以启用基于注释的任务计划。

```xml
xmlns:task="http://www.springframework.org/schema/task"
http://www.springframework.org/schema/task
http://www.springframework.org/schema/task/spring-task-3.0.xsd
 
<task:annotation-driven>

```

上面的添加是必要的，因为我们将使用基于注解的配置。


#### 使用`@Scheduled`注解

下一步是在类中创建一个类和一个方法，如下所示：

```java
public class DemoService
{
    @Scheduled(cron="*/5 * * * * ?")
    public void demoServiceMethod()
    {
        System.out.println("Method executed at every 5 seconds. Current time is :: "+ new Date());
    }
}
```
1. 使用`@Scheduled`注解将使`Spring`容器理解该注解下面的方法将作为作业运行。
2. 请记住，用`@Scheduled`注解的方法不应将参数传递给它们。
3. 他们也不应返回任何值。
4. 如果要在`@Scheduled`方法中使用外部对象，则应使用自动装配将它们注入到`DemoService`类中，而不要将其作为参数传递给`@Scheduled`方法。

### 使用`@Scheduled`注解中的`fixedDelay`属性

在此方法中，`fixedDelay`属性与`@Scheduled`注解一起使用。也可以使用`fixedRate`。

示例类如下所示：

```java
package cn.howtodoinjava.service;
 
import java.util.Date;
import org.springframework.scheduling.annotation.Scheduled;
 
public class DemoServiceBasicUsageFixedDelay
{
    @Scheduled(fixedDelay = 5000)
    //@Scheduled(fixedRate = 5000)  //Or use this
    public void demoServiceMethod()
    {
        System.out.println("Method executed at every 5 seconds. Current time is :: "+ new Date());
    }
}
```

应用程序配置如下所示：

```xml
< ?xml  version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 xmlns:task="http://www.springframework.org/schema/task"
 xmlns:util="http://www.springframework.org/schema/util"
 xmlns:context="http://www.springframework.org/schema/context"
 xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.0.xsd
        http://www.springframework.org/schema/context/ http://www.springframework.org/schema/context/spring-context.xsd
        http://www.springframework.org/schema/util/ http://www.springframework.org/schema/util/spring-util.xsd
        http://www.springframework.org/schema/task http://www.springframework.org/schema/task/spring-task-3.0.xsd">
 
    <task:annotation-driven />
 
    <bean id="demoServiceBasicUsageFixedDelay" class="cn.howtodoinjava.service.DemoServiceBasicUsageFixedDelay"></bean>
 
</beans>
```

### 使用属性文件的`cron`表达式

在此方法中，`cron`属性与`@Scheduled`注解一起使用。这个属性的值必须是一个cron表达式，但是，这个cron表达式将在一个属性文件中定义，并且相关属性的键将在`@Scheduled`注解中使用。

这将从源代码中解耦`cron`表达式，从而使更改变得容易。

```java
package cn.howtodoinjava.service;
 
import java.util.Date;
import org.springframework.scheduling.annotation.Scheduled;
 
public class DemoServicePropertiesExample {
 
    @Scheduled(cron = "${cron.expression}")
    public void demoServiceMethod()
    {
        System.out.println("Method executed at every 5 seconds. Current time is :: "+ new Date());
    }
 
}

```

应用程序配置如下所示：

```xml
<?xml  version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 xmlns:task="http://www.springframework.org/schema/task"
 xmlns:util="http://www.springframework.org/schema/util"
 xmlns:context="http://www.springframework.org/schema/context"
 xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.0.xsd
        http://www.springframework.org/schema/context/ http://www.springframework.org/schema/context/spring-context.xsd
        http://www.springframework.org/schema/util/ http://www.springframework.org/schema/util/spring-util.xsd
        http://www.springframework.org/schema/task http://www.springframework.org/schema/task/spring-task-3.0.xsd">
 
    <task:annotation-driven />
 
    <util:properties id="applicationProps" location="application.properties" />
 
    <context:property-placeholder properties-ref="applicationProps" />
 
    <bean id="demoServicePropertiesExample" class="cn.howtodoinjava.service.DemoServicePropertiesExample"></bean>
 
</beans>
```
`application.properties` 如下：
```java
corn.expression = */5 * * * * ?
```

### 在上下文配置中使用`cron`表达式

该方法在属性文件中配置`cron`表达式，在配置文件中使用`cron`表达式的属性键配置作业调度。主要的变化是您不需要在任何方法上使用`@Scheduled`注解。方法配置也在应用程序配置文件中完成。

示例类如下所示：

```java
package cn.howtodoinjava.service;
 
import java.util.Date;
 
public class DemoServiceXmlConfig
{
    public void demoServiceMethod()
    {
        System.out.println("Method executed at every 5 seconds. Current time is :: "+ new Date());
    }
 
}
```

应用程序配置如下所示：

```xml
<?xml  version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 xmlns:task="http://www.springframework.org/schema/task"
 xmlns:util="http://www.springframework.org/schema/util"
 xmlns:context="http://www.springframework.org/schema/context"
 xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.0.xsd
        http://www.springframework.org/schema/context/ http://www.springframework.org/schema/context/spring-context.xsd
        http://www.springframework.org/schema/util/ http://www.springframework.org/schema/util/spring-util.xsd
        http://www.springframework.org/schema/task http://www.springframework.org/schema/task/spring-task-3.0.xsd">
 
    <task:annotation-driven />
 
    <util:properties id="applicationProps" location="application.properties" />
 
    <context:property-placeholder properties-ref="applicationProps" />
 
    <bean id="demoServiceXmlConfig" class="cn.howtodoinjava.service.DemoServiceXmlConfig" />
 
    <task:scheduled-tasks>
        <task:scheduled ref="demoServiceXmlConfig" method="demoServiceMethod" cron="#{applicationProps['cron.expression']}"></task:scheduled>
    </task:scheduled-tasks>
 
</beans>
```

`application.properties` 如下：
```java
corn.expression = */5 * * * * ?
```


---
[![https://niocoder.com/assets/images/qrcode.jpg](https://niocoder.com/assets/images/qrcode.jpg "https://niocoder.com/assets/images/qrcode.jpg")](https://niocoder.com/assets/images/qrcode.jpg "https://niocoder.com/assets/images/qrcode.jpg")



> 🙂🙂🙂关注微信公众号**java干货**
不定期分享干货资料


原文链接：[Spring @Required Annotation](https://howtodoinjava.com/spring-core/spring-required-annotation-and-requiredannotationbeanpostprocessor-example/)
