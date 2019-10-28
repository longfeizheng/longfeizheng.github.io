---
layout: post
title: Spring系列九：Spring final静态常量bean初始化
categories: Spring
description: Spring
keywords: Spring
---

> 满目山河空念远，落花风雨更伤春。

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring13.jpg](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring13.jpg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring13.jpg")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring13.jpg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring13.jpg")


## 概述

本章学习使用`<util：constant>`标签将某些`Spring` `bean` `final`静态常量字段注入另外`bean`。

### `Spring util：constant`示例

从逻辑上讲，你将按照下面给出的方式执行`bean`。它有两个静态的`final`字段。`MANAGER` 和 `DIRECTOR`。

```java
public class EmployeeDTO
{
    public static final EmployeeDTO MANAGER = new EmployeeDTO("manager");
 
    public static final EmployeeDTO DIRECTOR = new EmployeeDTO("director");
 
    private Integer id;
    private String firstName;
    private String lastName;
    private String designation;
 
    public EmployeeDTO(String designation)
    {
        this.id = -1;
        this.firstName = "dummy";
        this.lastName = "dummy";
        this.designation = designation;
    }
 
    //Setters and Getters
 
    @Override
    public String toString() {
        return "Employee [id=" + id + ", firstName=" + firstName
                + ", lastName=" + lastName + ", type=" + designation + "]";
    }
}

```

您想在`EmployeeTypeWrapper`类中使用上述字段，如下所示：

```xml
<util:constant id="MANAGER"
        static-field="cn.howtodoinjava.demo.model.EmployeeDTO.MANAGER" />
 
<util:constant id="DIRECTOR"
    static-field="cn.howtodoinjava.demo.model.EmployeeDTO.DIRECTOR" />
 
<!-- Use the static final bean constants here -->
<bean name="employeeTypeWrapper" class="cn.howtodoinjava.demo.factory.EmployeeTypeWrapper">
    <property name="manager" ref="MANAGER" />
    <property name="director" ref="DIRECTOR" />
</bean>
```

`EmployeeTypeWrapper`代码如下：

```java
public class EmployeeTypeWrapper {
 
    private EmployeeDTO manager = null;
 
    private EmployeeDTO director = null;
 
    public EmployeeDTO getManager() {
        return manager;
    }
 
    public void setManager(EmployeeDTO manager) {
        this.manager = manager;
    }
 
    public EmployeeDTO getDirector() {
        return director;
    }
 
    public void setDirector(EmployeeDTO director) {
        this.director = director;
    }
}
```

### 测试

测试上面的配置

```java
ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
 
EmployeeTypeWrapper employeeTypeWrapper = (EmployeeTypeWrapper) context.getBean("employeeTypeWrapper");
 
System.out.println(employeeTypeWrapper.getManager());
System.out.println(employeeTypeWrapper.getDirector());
 

```

输出：
```java
Employee [id=-1, firstName=dummy, lastName=dummy, type=manager]
Employee [id=-1, firstName=dummy, lastName=dummy, type=director]
```

> 当然，你可以直接在代码中使用字段引用，因为它们是静态字段。但是通过配置文件中定义它们使你可以在任何时候将实现从`MANAGER`更改为`DIRECTOR`，而无需编译源代码。

---
[![https://niocoder.com/assets/images/qrcode.jpg](https://niocoder.com/assets/images/qrcode.jpg "https://niocoder.com/assets/images/qrcode.jpg")](https://niocoder.com/assets/images/qrcode.jpg "https://niocoder.com/assets/images/qrcode.jpg")



> 🙂🙂🙂关注微信公众号**java干货**
不定期分享干货资料


原文链接：[Spring util:constant to refer final static field references](https://howtodoinjava.com/spring-core/spring-declare-beans-from-final-static-field-references-using-util-constant/)
