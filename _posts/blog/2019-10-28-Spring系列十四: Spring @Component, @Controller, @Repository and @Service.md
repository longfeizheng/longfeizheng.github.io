---
layout: post
title: Spring系列十四： Spring @Component, @Controller, @Repository and @Service
categories: Spring
description: Spring
keywords: Spring
---

> 少小虽非投笔吏，论功还欲请长缨。

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring18.jpg](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring18.jpg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring18.jpg")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring18.jpg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring18.jpg")


## 概述

在`spring`自动装配中，`@Autowired`注解只处理连接部分。我们仍然需要定义bean，以便容器能够识别它们并为我们注入它们。

使用`@Component`、`@Repository`、`@Service`和`@Controller`注解，并启用自动组件扫描，`Spring`将自动将`bean`导入容器并注入依赖项。这些注解也被称为原型注解。

在开始使用这些注解之前，我们先快速了解一下关于这些注解的事实，这将帮助我们更好地决定何时使用哪个注解。

### `Spring` `bean`原型注解

#### `@Component`注解

`@Component`注解将`java`类标记为`bean`，因此`spring`的组件扫描机制可以获取它并将其拉入应用程序上下文。要使用此注解，请将其应用于类，如下所示:

```java
@Component
public class EmployeeDAOImpl implements EmployeeDAO {
    // ...
}
```

#### `@Repository`注解

尽管上面使用`@Component`足够好，但是我们可以使用更合适的注解，该注解专门为`DAO`提供些额外的功能，即`@Repository`注解。`@Repository`注解是`@Component`注解的特化，具有相似的用途和功能。除了将`DAO`导入`DI`容器之外，它还使未经检查的异常（从`DAO`方法抛出）转换为`Spring` `DataAccessException`。

####  `@Service` 注解

`@Service`注解也是`@Component`注解的特殊化。目前，它没有提供`@Component`注解以外的任何其他行为，但是最好在服务层类中使用`@Service`而不是`@Component`，因为它可以更好地指定意图。此外，其他行为和工具可能会依赖它。

#### `@Controller` 注解

`@Controller`注解将一个类标记为`Spring` `Web` `MV`C控制器。它也是`@Component`特殊化，因此标有它的`bean`将自动导入`DI`容器中。当我们将`@Controller`注解添加到一个类时，我们可以使用另一个注解，即`@RequestMapping`。将`URL`映射到类的实例方法。

> 在实际使用中，我们将遇到非常罕见的情况，需要使用`@Component`注解。大多数时候，我们将使用`@Repository`，`@Service`和`@Controller`注解。当该类不属于控制器，业务层和`dao`这三个类别中的任何一个类别时，应使用`@Component`。

如果我们想自定义`DI`容器中注册的`bean`的名称，则可以在注解属性本身中传递名称，例如`@Service（“ employeeManager”）`。

### 启用组件扫描

以上四个注解仅在由`Spring`框架的`DI`容器扫描时才进行扫描和配置。要启用此扫描，我们将需要在`applicationContext.xml`文件中使用`context：component-scan`标记。

```xml
<context:component-scan base-package="cn.howtodoinjava.demo.service" />
<context:component-scan base-package="cn.howtodoinjava.demo.dao" />
<context:component-scan base-package="cn.howtodoinjava.demo.controller" />
```

`context：component-scan`元素需要一个`base-package`属性，顾名思义，该属性指定了递归组件搜索的起点。我们可能不希望将顶层软件包交给`spring`，所以应该声明三个`component-scan`元素，每个元素都具有指向不同软件包的`base-package`属性。

### 使用`@Component`，`@Repository`，`@Service`和`@Controller`注解

正如我已经说过的，在`DAO`，管理器和控制器类上使用`@Repository`，`@Service`和`@Controller`注解。但是在现实生活中，在`DAO`和业务员层，我们通常有单独的类和接口。接口用于定义契约，类用于定义契约的实现。

始终在具体的类上的实现上添加注解；而不是通过接口。

```java
public interface EmployeeDAO
{
    //...
}
 
@Repository
public class EmployeeDAOImpl implements EmployeeDAO
{
    //...
}

```

在`bean`上具有这些构造型注解后，就可以直接使用在具体类中定义的`bean`引用。请注意，引用是类型接口。在这种情况下，`Spring` `D`I容器足够聪明，可以注入正确的实例。

### `@Component`和`@Bean`注解之间的区别

在`Spring`中，两个注解大不相同。

`@Component`用于使用类路径扫描自动检测和自动配置`bean`。在带注解的类和`Bean`之间存在隐式的一对一映射（即每个类一个`Bean`）。

`@Bean`用于显式声明单个`bean`，而不是让`Spring`为我们自动完成。

另一个很大的不同是`@Component`是类级别的注释，其中`@Bean`是方法级别的注释，默认情况下，方法的名称用作`Bean`名称。

### 测试

#### `Bean`定义

```java
public interface EmployeeDAO
{
    public EmployeeDTO createNewEmployee();
}
 
@Repository ("employeeDao")
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
}
```

```java

public interface EmployeeManager
{
    public EmployeeDTO createNewEmployee();
}
 
@Service ("employeeManager")
public class EmployeeManagerImpl implements EmployeeManager
{
    @Autowired
    EmployeeDAO dao;
     
    public EmployeeDTO createNewEmployee()
    {
        return dao.createNewEmployee();
    }
}

```

```java
@Controller ("employeeController")
public class EmployeeController
{
        @Autowired
    EmployeeManager manager;
     
    public EmployeeDTO createNewEmployee()
    {
        return manager.createNewEmployee();
    }
}
```

```java
public class EmployeeDTO {
 
    private Integer id;
    private String firstName;
    private String lastName;
}
```

#### 运行测试用例

```java
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
 
import cn.howtodoinjava.demo.service.EmployeeManager;
 
public class TestSpringContext
{
    public static void main(String[] args)
    {
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
 
        //EmployeeManager manager = (EmployeeManager) context.getBean(EmployeeManager.class);
         
        //OR this will also work
         
        EmployeeController controller = (EmployeeController) context.getBean("employeeController");
         
        System.out.println(controller.createNewEmployee());
    }
}
```

输出：

```java
Jan 22, 2015 6:17:57 PM org.springframework.context.support.ClassPathXmlApplicationContext prepareRefresh
INFO: Refreshing org.springframework.context.support.ClassPathXmlApplicationContext@1b2b2f7f:
startup date [Thu Jan 22 18:17:57 IST 2015]; root of context hierarchy
 
Jan 22, 2015 6:17:57 PM org.springframework.beans.factory.xml.XmlBeanDefinitionReader loadBeanDefinitions
 
INFO: Loading XML bean definitions from class path resource [applicationContext.xml]
 
Employee [id=1, firstName=Lokesh, lastName=Gupta]
```




---
[![https://niocoder.com/assets/images/qrcode.jpg](https://niocoder.com/assets/images/qrcode.jpg "https://niocoder.com/assets/images/qrcode.jpg")](https://niocoder.com/assets/images/qrcode.jpg "https://niocoder.com/assets/images/qrcode.jpg")



> 🙂🙂🙂关注微信公众号**java干货**
不定期分享干货资料


原文链接：[Spring @Required Annotation](https://howtodoinjava.com/spring-core/spring-required-annotation-and-requiredannotationbeanpostprocessor-example/)
