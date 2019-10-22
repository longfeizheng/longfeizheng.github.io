---
layout: post
title: Spring系列十：Spring FactoryBean
categories: Spring
description: Spring
keywords: Spring
---

> 重叠泪痕缄锦字，人生只有情难死。

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring14.jpg](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring14.jpg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring14.jpg")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring14.jpg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring14.jpg")


## 概述

工厂`bean`是用作在`IoC`容器中创建其他`bean`的工厂,但它是特定的`Spring`的bean。从概念上讲，工厂`bean`非常类似于工厂方法，可以在`bean`构造期间由`Spring` `IoC`容器标识，并且可以由容器用来实例化其他`bean`。

### 使用`FactoryBean`创建`bean`

要创建工厂`bean`，你只需要把创建的`bean`类实现`FactoryBean`接口，该类将创建实际所需要的`bean`。简单起见，您可以扩展`AbstractFactoryBean`类。


通过扩展`AbstractFactoryBean`类，工厂`Bean`可以简单地重写`createInstance（）`方法来创建目标`Bean`实例。此外，你必须在`getObjectType（）`方法中返回目标`bean`的类型，`spring`的自动装配功能才能正常工作。

```java
public class EmployeeFactoryBean extends AbstractFactoryBean<Object>
{
    /This method will be called by container to create new instances
    @Override
    protected Object createInstance() throws Exception
    {
        //code
    }
 
    //This method is required for autowiring to work correctly
    @Override
    public Class<EmployeeDTO> getObjectType() {
        return EmployeeDTO.class;
    }
}
```

### 为什么使用工厂`bean`

工厂`bean`主要用于实现框架某些特定功能。如下：
1. 从`JNDI`查找对象（例如数据源）时，可以使用`JndiObjectFactoryBean`。
2. 使用经典的`Spring` `AOP`为`bean`创建代理时，可以使用`ProxyFactoryBean`。
3. 在`IoC`容器中创建`Hibernate`会话工厂时，可以使用`LocalSessionFactoryBean`。

在大多数情况下，你几乎不必编写任何自定义工厂`bean`，因为它们框架的特定功能的，并且不能在`Spring` `IoC`容器范围之外使用。

### `FactoryBean` 测试

在这个例子中，我正在创建一个工厂`bean`来实例化不同类型的`Employee`对象，例如他们的`manager`, `director`等具有一些预先填充的属性。

`EmployeeDTO`代码如下：

```java
package cn.howtodoinjava.demo.model;
 
public class EmployeeDTO {
 
    private Integer id;
    private String firstName;
    private String lastName;
    private String designation;
 
    //Setters and Getters are hidden behind this comment.
 
    @Override
    public String toString() {
        return "Employee [id=" + id + ", firstName=" + firstName
                + ", lastName=" + lastName + ", type=" + designation + "]";
    }
}
```

`EmployeeFactoryBean`类继承了`AbstractFactoryBean`类，并实现了两个方法`createInstance（）`和`getObjectType（）`。

```java
import org.springframework.beans.factory.config.AbstractFactoryBean;
 
import cn.howtodoinjava.demo.model.EmployeeDTO;
 
public class EmployeeFactoryBean extends AbstractFactoryBean<Object>
{
    private String designation;
     
    public String getDesignation() {
        return designation;
    }
 
    public void setDesignation(String designation) {
        this.designation = designation;
    }
 
    //This method will be called by container to create new instances
    @Override
    protected Object createInstance() throws Exception
    {
        EmployeeDTO employee = new EmployeeDTO();
        employee.setId(-1);
        employee.setFirstName("dummy");
        employee.setLastName("dummy");
        //Set designation here
        employee.setDesignation(designation);
        return employee;
    }
 
    //This method is required for autowiring to work correctly
    @Override
    public Class<EmployeeDTO> getObjectType() {
        return EmployeeDTO.class;
    }
}
```

在配置文件中定义各种`Employee`类型，如下所示。

```xml
<bean id="manager"  class="cn.howtodoinjava.demo.factory.EmployeeFactoryBean">
    <property name="designation" value="Manager" />
</bean>
 
<bean id="director"  class="cn.howtodoinjava.demo.factory.EmployeeFactoryBean">
    <property name="designation" value="Director" />
</bean>

```

测试工厂`bean`

```java
public class TestSpringContext
{
    @SuppressWarnings("resource")
    public static void main(String[] args)
    {
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
 
        EmployeeDTO manager = (EmployeeDTO) context.getBean("manager");
        System.out.println(manager);
         
        EmployeeDTO director = (EmployeeDTO) context.getBean("director");
        System.out.println(director);
    }
}
```
输出：
```java
Employee [id=-1, firstName=dummy, lastName=dummy, type=Manager]
Employee [id=-1, firstName=dummy, lastName=dummy, type=Director]
```

如你所见，`EmployeeFactoryBean`使用相同的工厂方法创建了两个不同的员工对象。

### 获取`FactoryBean`实例本身

如果要获取`EmployeeFactoryBean`本身的实例，则可以在`bean`名称之前添加`＆`。

```java
EmployeeFactoryBean factory = (EmployeeFactoryBean) context.getBean("&director");
 
System.out.println(factory.getDesignation());
System.out.println(factory.getObjectType());
System.out.println(factory.getObject());
```
 
输出:

```java
Director
 
class cn.howtodoinjava.demo.model.EmployeeDTO
 
Employee [id=-1, firstName=dummy, lastName=dummy, type=Director]
```

---
[![https://niocoder.com/assets/images/qrcode.jpg](https://niocoder.com/assets/images/qrcode.jpg "https://niocoder.com/assets/images/qrcode.jpg")](https://niocoder.com/assets/images/qrcode.jpg "https://niocoder.com/assets/images/qrcode.jpg")



> 🙂🙂🙂关注微信公众号**java干货**
不定期分享干货资料


原文链接：[Spring ResourceLoaderAware – Read file in Spring](https://howtodoinjava.com/spring-core/spring-resource-loader-aware/)
