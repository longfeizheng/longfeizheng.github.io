---
layout: post
title: Spring系列三：IoC 与 DI
categories: Spring
description: Spring
keywords: Spring
---

> 水晶帘动微风起，满架蔷薇一院香。

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring03.jpg](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring03.jpg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring03.jpg")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring03.jpg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring03.jpg")


## 概述

在软件工程中，控制反转(`IoC`)是一种设计思想，对象之间耦合在一起，在运行时自动绑定，并且它们编译时对所需要引用的对象是不确定的。在这个`spring`教程中，通过示例了解`ioc`和`spring`中的依赖注入之间的区别。

### 什么是控制反转（IOC）

在传统面向对象设计的软件系统中，它的底层由N多个对象构成，各个对象之间通过相互合作。最终实现业务流程。控制反转意指把创建和查找依赖对象的控制权交给了容器，由容器进行注入组合对象，所以对象与对象之间是松散耦合，这样也方便测试，利于功能复用，更重要的是使得程序的整个体系结构变得非常灵活，尽管有些人认为使用服务定位器模式也可以提供控制反转。

使用控制反转作为设计准则有以下优点：

1. 某个任务的执行与实现是分离的
2. 每个模块更关注与自己的设计。
3. 模块不需要关注其它系统，只需要依赖即可。
4. 模块的升级不会影响其它模块

### 什么是依赖注入（DI）

`IoC`是一种设计范例，其目标是对应用程序的各个组件提供更多控制，使这些组件可以完成工作。依赖注入是一种模式，用于创建对象依赖的对象实例，且在编译时期是无感知的。`IoC`依赖于依赖注入，因为它需要一种机制来创建且引用需要的组件。

这两个概念以这种方式协同工作，允许编写更灵活、可重用和封装的代码。因此，它们是设计面向对象解决方案的重要概念。

### 如何实现IoC

在面向对象的编程中，有几种基本技术可以实现控制反转。如下：

1. 使用工厂模式
2. 使用服务定位器模式
3. 使用以下任何给定类型的依赖项注入
  - 构造函数注入
  - setter注入
  - 注解注入

### Spring中的控制反转

`org.springframework.beans`和`org.springframework.context`包为`Spring`框架的`IoC`容器提供了基础功能。`BeanFactory`接口提供了更高级的配置项，能够管理所有对象。`ApplicationContext`接口建立在`BeanFactory`之上（它是一个子接口），并添加了其他功能，例如与`Spring`的`AOP`功能的更轻松集成，消息资源处理（用于国际化），事件传播以及特定于应用程序层的上下文，例如作为`Web`应用程序中使用的`WebApplicationContext`

`BeanFactory`是`Spring IoC`容器的主要实现，负责包含和管理上述`Bean`。`BeanFactory`接口是`Spring`中的重要的`IoC`容器接口。

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring04.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring04.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring04.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring04.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/spring04.png")

`BeanFactory`接口有许多实现。最常用的`BeanFactory`实现是`XmlBeanFactory`类。其他常用的类是`XmlWebApplicationContext`。根据`bean`的定义，工厂将返回所包含对象的不同实例（`Prototype`设计模式），或者返回单个共享实例（`Singleton`设计模式，其中实例是作用域中的单例）。的工厂）。将返回哪种类型的实例取决于`bean`工厂的配置：获取`bean`实例的`API`是相同的。

在深入研究依赖注入类型之前，首先确定在`spring`框架中创建`bean`的方式，因为它将有助于理解下一部分的内容。

### 如何在Spring中创建bean实例

`Bean`定义可以看作是创建一个或多个实际对象的配置。获取时，容器会查看命名`bean`的配置，并使用该`bean`定义封装的配置项来创建（或获取）实际对象。

#### 使用构造函数

当使用构造函数方法创建`bean`时，所有普通类都可以被`Spring`使用并与之兼容。也就是说，正在创建的类不需要实现任何特定的接口或以特定的方式进行编码。仅指定`bean`类就足够了。使用基于`XML`的配置项时，可以像这样指定`bean`类：

```Java
<bean id="exampleBean" class = "cn.howtodoinjava.ExampleBean"/>
```

#### 使用静态工厂方法

在定义要使用静态工厂方法创建的`bean`以及指定包含静态工厂方法的类的`class`属性时，需要另一个名为`factory-method`的属性来指定工厂方法本身的名称。

```java
<bean id="exampleBean" class = "cn.howtodoinjava.ExampleBean" factory-method="createInstance"/>
```

`Spring`希望能够调用此方法并返回一个可用的对象，得到对象之后，该对象将被视为是通过构造函数创建的。

#### 使用实例工厂方法

以类似于通过静态工厂方法进行实例化的方式，使用实例工厂方法进行实例化是调用容器中现有`bean`的`factory`方法来创建新`bean`。

```java
<bean id="myFactoryBean"  class="cn.howtodoinjava.MyFactoryBean">

<bean id="exampleBean"  factory-bean="myFactoryBean" factory-method="createInstance"></bean>
```

### Spring的依赖注入

依赖项注入(`DI`)背后的基本原则是，对象仅通过构造函数参数、工厂方法的参数或属性来定义它们的依赖项，这些参数是在对象实例被构造或从工厂方法返回后在对象实例上配置的。然后，容器的工作是在创建bean时实际注入这些依赖项。即由`IoC`容器帮对象找相应的依赖对象并注入，而不是由对象主动去找，因此称为控制反转(`IoC`)。

#### setter 注入

通过调用无参数构造函数或无参数静态工厂方法以实例化`bean`之后，在`bean`上调用`setter`方法，可以实现基于`setter`的`DI`。
```java
public class TestSetterDI {

DemoBean demoBean = null;

public void setDemoBean(DemoBean demoBean) {
    this.demoBean = demoBean;
  }
}
```

#### 构造方法注入

基于构造函数的`DI`是通过调用具有多个参数（每个参数代表一个对象实例）的构造函数来实现的。另外，调用带有特定参数的静态工厂方法来构造`Bean`几乎是等效的，本文的其余部分将类似地考虑构造函数的参数和静态工厂方法的参数。

```java
public class ConstructorDI {

DemoBean demoBean = null;

public TestSetterDI (DemoBean demoBean) {
    this.demoBean = demoBean;
  }
}
```

#### 注解注入

注解注入只需要在需要在成员变量上添加`@Autowire`注解即可

```java
public class ConstructorDI {

@Autowire
private DemoBean demoBean;
  }
}
```

### spring面试题

#### 组件和服务之间有什么区别？

组件是一组软件，这些组件将被其它应用程序所使用，且不会进行任何更改。所谓“不更改”是指使用应用程序不会更改组件的源代码，尽管它们可以通过组件作者允许的方式扩展组件来更改组件的行为。

服务与组件相似，供外部应用程序使用。主要的区别在于本地使用的组件(比如`jar`文件、程序集、`dll`或源导入)。服务将通过同步或异步的某个远程接口（例如，`Web`服务，消息系统，`RPC`或套接字）远程使用。

#### DI与服务定位器模式有何不同？

依赖项注入器的主要好处是，它允许根据环境和使用情况注入合适的服务实现。注入不是打破这种依赖性的唯一方法，另一种方法是使用服务定位器。服务定位器的基本思想是拥有一个对象，该对象知道如何掌握应用程序可能需要的所有服务。然后，它将扫描所有此类服务，并将它们存储为单例注册表中。当要求提供服务实现时，请求者可以使用令牌查询注册表并获取适当的实现。

通常，这些注册表是通过一些配置文件填充的。关键区别在于，使用服务定位器时，服务的每个用户都对定位器具有依赖性。定位器可以隐藏对其他实现的依赖关系，但是还是需要查看定位器。

#### 使用哪个更好的服务（即服务定位器或依赖项注入）？

正如上文已经说过的，关键区别在于，使用服务定位器，服务的每个用户都对定位器有依赖性。这意味着必须在输入和输出方面了解服务定位器的详细信息。因此，实际上成为选择哪种模式的决定因素。

如果维护注册表信息既简单又必要，则可以使用服务定位器，或者直接使用依赖注入，因为它对服务的使用者是无感知的

#### 构造函数注入或setter或注解注入哪个更好？

基于`constructor`的注入，会固定依赖注入的顺序；该方式不允许我们创建`bean`对象之间的循环依赖关系，这种限制其实是一种利用构造器来注入的益处 - 当你甚至没有注意到使用`setter`注入的时候，`Spring`能解决循环依赖的问题；

基于`setter`的注入，只有当对象是需要被注入的时候它才会帮助我们注入依赖，而不是在初始化的时候就注入；另一方面如果你使用基于`constructor`注入，`CGLIB`不能创建一个代理，迫使你使用基于接口的代理或虚拟的无参数构造函数。

我的偏好是注解注入，这种方式看起来非常好，精短，可读性高，不需要多余的代码，也方便维护；

#### 什么是BeanFactory ？

`BeanFactory`就像一个工厂类，其中包含一系列`bean`。`BeanFactory`在其内部保存多个`Bean`的`Bean`定义，然后在客户要求时实例化`Bean`。

`BeanFactory`能够在实例化协作对象之间创建关联。这消除了`bean`本身和`bean`客户端的配置负担。`BeanFactory`还参与`bean`的生命周期，从而调用自定义初始化和销毁​​方法。

#### 什么是ApplicationContext ？

`Bean`工厂适合简单的应用程序，但是要利用`Spring`框架的全部功能，您可能需要升级到`Spring`更高级的容器即应用程序上下文。从表面上看，应用程序上下文与`Bean`工厂相同，两者都加载`Bean`定义，将`Bean`绑定在一起并根据请求分配`Bean`。但它也提供如下功能：
- 解决文本消息的方法，包括对国际化的支持。
- 加载文件资源的通用方法。
- 为`bean`注册监听器的事件。

#### ApplicationContext的常见的实现有哪些 ？

`ApplicationContext`的三种常用实现是：

1. `ClassPathXmlApplicationContext`：它从位于类路径中的`XML`文件中加载上下文定义，并将上下文定义视为类路径资源。使用代码从应用程序的类路径中加载应用程序上下文。
   ```java
   ApplicationContext context = new ClassPathXmlApplicationContext("bean.xml");

   ```
2. `FileSystemXmlApplicationContext`：它从文件系统中的`XML`文件加载上下文定义。使用代码从文件系统中加载应用程序上下文。
  ```java
  ApplicationContext context = new FileSystemXmlApplicationContext("bean.xml");
  ```
3. `XmlWebApplicationContext`：它从`Web`应用程序中包含的XML文件中加载上下文定义。

#### BeanFactory或ApplicationContext最好使用哪个 ？

`BeanFactory`基本上只是实例化和配置`Bean`。`ApplicationContext`也可以做到这一点，它提供了支持基础设施来支持许多企业特有的特性，例如事务和`AOP`。

因此，建议使用`ApplicationContext`。

在本教程中，我们在`spring`学习了`ioc`和`di`之间的区别。


---
[![https://niocoder.com/assets/images/qrcode.jpg](https://niocoder.com/assets/images/qrcode.jpg "https://niocoder.com/assets/images/qrcode.jpg")](https://niocoder.com/assets/images/qrcode.jpg "https://niocoder.com/assets/images/qrcode.jpg")



> 🙂🙂🙂关注微信公众号**java干货**
不定期分享干货资料


原文链接：[Spring – IoC Containers](https://howtodoinjava.com/spring-core/different-spring-ioc-containers/)
