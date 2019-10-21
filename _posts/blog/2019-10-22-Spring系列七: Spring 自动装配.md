---
layout: post
title: Spring系列七：Spring 自动装配
categories: Spring
description: Spring
keywords: Spring
---

> 相思相见知何日？此时此夜难为情。

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring09.jpg](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring09.jpg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring09.jpg")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring09.jpg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring09.jpg")


## 概述

在`Spring`框架中，在配置文件中声明`bean`的依赖关系是一个很好的做法，因为`Spring`容器能够自动装配协作`bean`之间的关系。这称为`spring`自动装配。

自动装配功能具有四种模式。分别是 `no`，`byName`，`byType`和`constructor`。

> 已弃用另一种自动连线模式自动检测。`Docs`说`autodetect`选项提供了太多的`magic`，最好使用更明确的声明。

- `XML`配置中的默认自动装配模式为`no`。
- `Java`配置中的默认自动装配模式是`byType`。

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring10.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring10.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring10.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring10.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring10.png")

#### 自动装配模式

1. `no`
    该选项是`spring`框架的默认选项，表示自动装配为关闭状态`OFF`。我们必须在`bean`定义中使用`<property>`标签显式设置依赖项。
2. `byName`
    此选项启用基于`bean`名称的依赖项注入。在`Bean`中自动装配属性时，属性名称用于在配置文件中搜索匹配的`Bean`定义。如果找到这样的`bean`，则将其注入属性。如果找不到这样的`bean`，则会引发错误。
3. `byType`
    此选项支持基于`bean`类型的依赖项注入。在`bean`中自动装配属性时，属性的类类型用于在配置文件中搜索匹配的`bean`定义。如果找到这样的`bean`，就在属性中注入它。如果没有找到这样的`bean`，就会引发一个错误。
4. `constructor`
    通过构造函数自动装配与`byType`相似，仅适用于构造函数参数。在启用了自动装配的`bean`中，它将查找构造函数参数的类类型，然后对所有构造函数参数执行自动装配类型。请注意，如果容器中没有一个完全属于构造函数参数类型的`bean`，则会引发致命错误。

#### `@Autowired` 注解

除了`bean`配置文件中提供的自动装配模式之外，还可以使用`@Autowired`注解在`bean`类中指定自动装配。要在`bean`类中使用`@Autowired`自动注入，必须首先使用以下配置在`spring`应用程序中启用自动注入。

##### 启用注解配置

```xml
<context:annotation-config />
```

使用配置文件中的`AutowiredAnnotationBeanPostProcessor` `bean`定义可以实现相同的目的。

```xml
<bean class ="org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor"/>
```

##### 使用`@Autowired`注解

现在，启用注解配置后，可以随意使用`@Autowired`自动连接`bean`依赖项。这可以通过三种方式完成：

###### `@Autowired`属性

在属性上使用`@Autowired`时，等效于在配置文件中通过`byType`自动注入

```java
public class EmployeeBean
{
    @Autowired
    private DepartmentBean departmentBean;
 
    public DepartmentBean getDepartmentBean() {
        return departmentBean;
    }
    public void setDepartmentBean(DepartmentBean departmentBean) {
        this.departmentBean = departmentBean;
    }
    //More code
}
```

###### `@Autowired`在属性`setter`方法上

在属性的`setter`方法上使用`@Autowired`时，它也等效于在配置文件中通过`byType`进行自动装配。

```java
public class EmployeeBean
{
    private DepartmentBean departmentBean;
 
    public DepartmentBean getDepartmentBean() {
        return departmentBean;
    }
 
    @Autowired
    public void setDepartmentBean(DepartmentBean departmentBean) {
        this.departmentBean = departmentBean;
    }
    //More code
}
```

###### `@Autowired`在构造函数上

在`bean`的构造函数上使用`@Autowired`时，它也等同于在配置文件中通过` constructor`进行自动装配。

```java

package cn.howtodoinjava.autowire.constructor;
 
public class EmployeeBean
{
    @Autowired
    public EmployeeBean(DepartmentBean departmentBean)
    {
        this.departmentBean = departmentBean;
    }
 
    private DepartmentBean departmentBean;
 
    public DepartmentBean getDepartmentBean() {
        return departmentBean;
    }
    public void setDepartmentBean(DepartmentBean departmentBean) {
        this.departmentBean = departmentBean;
    }
    //More code
}
```

#### `@Qualifier`解决冲突

我们了解到，如果我们在`byType`模式下使用自动装配，容器会在属性类类型中查找依赖项。如果找不到这样的类型，则会引发错误。但是，如果有两个或多个相同类类型的`bean`，该怎么办？

在这种情况下，`spring`将无法选择正确的`bean`来注入属性，因此你将需要使用`@Qualifier`注解来帮助容器。

要解析特定的`bean`，我们需要使用`@Qualifier`注解以及`@Autowired`注解，并将`bean`名称传递到注解参数中。看看下面的例子：

```java
public class EmployeeBean{
    @Autowired
    @Qualifier("finance")
    private DepartmentBean departmentBean;
 
    public DepartmentBean getDepartmentBean() {
        return departmentBean;
    }
    public void setDepartmentBean(DepartmentBean departmentBean) {
        this.departmentBean = departmentBean;
    }
    //More code
}
```

其中重复的`bean`配置如下：
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans>
    <context:annotation-config />
 
    <bean id="employee" class="cn.howtodoinjava.autowire.constructor.EmployeeBean" autowire="constructor">
        <property name="fullName" value="Lokesh Gupta"/>
    </bean>
 
    <!--First bean of type DepartmentBean-->
    <bean id="humanResource" class="cn.howtodoinjava.autowire.constructor.DepartmentBean" >
        <property name="name" value="Human Resource" />
    </bean>
 
    <!--Second bean of type DepartmentBean-->
     <bean id="finance" class="cn.howtodoinjava.autowire.constructor.DepartmentBean" >
        <property name="name" value="Finance" />
    </bean>
</beans>
```

#### 使用`required = false`进行错误安全的自动装配

即使在自动装配`Bean`依赖项时已格外小心，仍然可能会发现奇怪的查找失败。因此，要解决此问题，您将需要使自动装配成为可选的，以便在未找到依赖项的情况下，应用程序不应引发任何异常，而自动装配应被忽略。

这可以通过两种方式完成：

- 如果要使特定的`bean`属性的非强制性的特定`bean`自动装配，可以在`@Autowired`注解中使用`required =“ false”`属性。
    ```java
    @Autowired (required=false)
    @Qualifier ("finance")
    private DepartmentBean departmentBean;`
    ```
- 如果要在全局级别（即对所有`bean`中的所有属性）应用可选的自动装配；使用以下配置设置。
   ```xml
    <bean class="org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor">
        <property name="requiredParameterValue" value="false" />
    </bean>
    
   ```

#### 从自动装配中排除bean

默认情况下，自动装配扫描并匹配范围内的所有`bean`定义。如果您想排除一些`bean`定义，这样它们就不能通过自动装配模式被注入，可以使用设置为`false`的`autowire-candidate`来做到这一点。

1. 使用`autowire-candidate`作为`false`完全将`bean`排除在自动装配候选之外。它将特定的`bean`定义完全排除在自动装配基础结构之外。
    ```xml
    <?xml version="1.0" encoding="UTF-8"?>
    <beans>
        <context:annotation-config />
     
        <bean id="employee" class="cn.howtodoinjava.autowire.constructor.EmployeeBean" autowire="constructor">
            <property name="fullName" value="Lokesh Gupta"/>
        </bean>
        <!--Will be available for autowiring-->
        <bean id="humanResource" class="cn.howtodoinjava.autowire.constructor.DepartmentBean" >
            <property name="name" value="Human Resource" />
        </bean>
     
        <!--Will not participate in autowiring-->
         <bean id="finance"      class="cn.howtodoinjava.autowire.constructor.DepartmentBean" autowire-candidate="false">
            <property name="name" value="Finance" />
        </bean>
    </beans>

    ```
2. 另一种方法是根据`bean`名称的模式匹配来限制自动装配候选对象。顶级`<beans/>`元素在其`default-autowire-candidate`属性中接受一个或多个属性。
   例如，要将自动装配候选状态限制为名称以`Impl`结尾的任何`bean`，请提供值`* Impl`。要提供多种模式，请在以逗号分隔的列表中定义它们。
   ```xml
    <?xml version="1.0" encoding="UTF-8"?>
    <beans default-autowire-candidates="*Impl,*Dao">
        <context:annotation-config />
     
        <bean id="employee" class="com.howtodoinjava.autowire.constructor.EmployeeBean" autowire="constructor">
            <property name="fullName" value="Lokesh Gupta"/>
        </bean>
        <!--Will be available for autowiring-->
        <bean id="humanResource" class="com.howtodoinjava.autowire.constructor.DepartmentBean" >
            <property name="name" value="Human Resource" />
        </bean>
     
        <!--Will not participate in autowiring-->
         <bean id="finance"      class="com.howtodoinjava.autowire.constructor.DepartmentBean" autowire-candidate="false">
            <property name="name" value="Finance" />
        </bean>
    </beans>
   ```

请注意，`bean`定义的`autowire-candidate`属性的值`true`或`false`始终优先，而对于此类`bean`，模式匹配规则将不适用。

这就是`Spring` `bean`自动装配的全部内容。


---
[![https://niocoder.com/assets/images/qrcode.jpg](https://niocoder.com/assets/images/qrcode.jpg "https://niocoder.com/assets/images/qrcode.jpg")](https://niocoder.com/assets/images/qrcode.jpg "https://niocoder.com/assets/images/qrcode.jpg")



> 🙂🙂🙂关注微信公众号**java干货**
不定期分享干货资料


原文链接：[Spring Bean Autowiring – @Autowired](https://howtodoinjava.com/spring-core/spring-beans-autowiring-concepts/)
