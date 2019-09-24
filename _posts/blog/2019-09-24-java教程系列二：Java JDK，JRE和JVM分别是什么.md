---
layout: post
title: java教程系列二：Java JDK，JRE和JVM分别是什么？
categories: Java
description: Java
keywords: Java
---

> 多情只有春庭月，犹为离人照落花。

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/java/java11.jpg](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/java/java11.jpg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/java/java11.jpg")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/java/java11.jpg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/java/java11.jpg")


## 概述

本章主要了解JDK，JRE和JVM之间的区别。JVM是如何工作的？什么是类加载器，解释器和JIT编译器。还有一些面试问题。

### Java程序执行过程

在深入了解Java内存区域之前，我们先了解Java源文件是如何执行的。

1. 我们使用编辑器在`Simple.Java`文件中编写源代码。
2. 程序必须编译成字节码。编译器（`javac`）将源代码编译为`Simple.class`文件。
3. 此后缀为`.class`的类文件可以在任何平台/操作系统的的`JVM`（`Java`虚拟机）中执行。
4. `JVM`负责将字节码转换为机器可执行的本机机器代码。

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/java/java12.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/java/java12.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/java/java12.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/java/java12.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/java/java12.png")

### 什么是JVM？

`Java`虚拟机（`JVM`）是​​运行`Java`字节码的虚拟机。可以通过`javax`将`.java`文件编译成`.class`文件。`.class`文件包含`JVM`可解析的字节码。

事实上，`JVM`只是为`Java`字节码提供了运行时环境和规范。不同的厂商提供此规范的不同实现。例如，此`Wiki`页面列出了[其它JVM实现](https://en.wikipedia.org/wiki/List_of_Java_virtual_machines)。

最受欢迎的`JVM`虚拟机是`Oracle`公司提供的`Hostspot`虚拟机，（前身是`Sun Microsystems，Inc`.）。

`JVM`虚拟机使用许多先进技术，结合了最新的内存模型，垃圾收集器和自适应优化器，为`Java`应用程序提供了最佳性能。

`JVM`虚拟机有两种不同的模式,`client`模式和`server`模式。尽管`server`和`client`相似，但`server`进行了特殊调整，以最大程度地提高峰值运行速度。它用于长时间运行的服务器应用程序，它们需要尽可能快的运行速度，而不是快速启动或较小的运行时内存占用量。开发人员可以通过指定`-client`或`-server`来选择所需的模式。

`JVM`之所以称为虚拟机，是因为它提供的`API`不依赖于底层操作系统和机器硬件体系结构。这种与硬件和操作系统的独立性是`Java`程序一次写入，随处运行必要基础。

#### JVM架构

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/java/java13.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/java/java13.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/java/java13.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/java/java13.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/java/java13.png")

##### 类加载器

类加载器是用于加载类文件到`JVM`中。主要分为以下三步 加载，链接和初始化。

1. 加载
  - 为了加载类，`JVM`有3种类加载器。`Bootstrap`, `extension`和应用程序类加载器。
  - 加载类文件时，`JVM`会找到这个类的所有依赖项。
  - 首先类加载会判断当前类加载器是否存在父类，如果存在则交给父加载器加载。
  - `Bootstrap`为根类加载器，`Bootstrap`加载器尝试查找该类。它扫描`JRE` `lib`文件夹中的`rt.jar`。
  - 如果找不到类，那么`extension`加载器将在`jre \ lib \ ext`包中搜索类文件。
  - 如果还找不到类，则应用程序类加载器将在系统的 `CLASSPATH`环境变量中搜索所有`Ja`r文件和类
  - 任何类加载程序找到了类，则由该类加载器加载类；否则抛出`ClassNotFoundException`。
2. 链接 : 类加载器加载类后，将执行链接。字节码验证程序将验证生成的字节码是否正确，如果验证失败，我们将收到验证错误。它还会对类中的静态变量和方法执行内存分配。
3. 初始化 : 这是类加载的最后阶段，此处将为所有静态变量分配原始值，并执行静态块。

#####  JVM内存区域

`JVM`中的内存区域分为多个部分，以存储应用程序数据的特定部分。

- 方法区：存储类结构，例如类的基本信息，常量运行时池和方法代码。
- 堆：存储在应用程序执行期间创建的所有对象。
- 栈：存储局部变量和中间结果。所有这些变量对于创建它们的线程都是私有的。每个线程都有自己的`JVM`栈，并在创建线程时同时创建。因此，所有此类局部变量都称为线程局部变量。
- PC寄存器：存储当前正在执行的语句的物理内存地址。在Java中，每个线程都有其单独的PC寄存器。
- 本地方法区：许多底层代码都是用C和C ++等语言编写的。本地方法栈保存本机代码的指令。

### JVM执行引擎

分配给`JVM`的所有代码均由执行引擎执行。执行引擎读取字节码并一一执行。它使用两个内置的解释器和JIT编译器将字节码转换为机器码并执行。

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/java/java14.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/java/java14.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/java/java14.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/java/java14.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/java/java14.png")

使用`JVM`，解释器和编译器均会生成本机代码。不同之处在于它们如何生成本机代码，其优化程度以及优化成本。

#### 解释器

`JVM`解释器通过查找预定义的`JVM`指令到机器指令的映射，几乎将每个字节码指令转换为相应的本机指令。它直接执行字节码，不执行任何优化。

#### JIT编译器

为了提高性能，`JIT`编译器在运行时与`JVM`交互，并将适当的字节码序列编译为本地机器代码。通常，`JIT`编译器采用一段代码（和解释器一次一条语句不一样），优化代码，然后将其转换为优化的机器代码。

默认情况下，`JIT`编译器处于启用状态。您可以禁用`JIT`编译器，在这种情况下，解释器将要解释整个`Java`程序。除了诊断或解决`JIT`编译问题外，不建议禁用`JIT`编译器。

### 什么是JRE

`Java`运行时环境（`JRE`）是一个软件包，它将库（`jar`）和`Java`虚拟机以及其他组件捆绑在一起，以运行用`Java`编写的应用程序。`JRE`只是`JVM`的一部分。

要执行`Java`应用程序，只需要在计算机中安装`JRE`。 这是在计算机上执行`Java`应用程序都是最低要求。

`JRE`包含了以下组件–

1. Java HotSpot客户端虚拟机使用的DLL文件。
2. Java HotSpot服务器虚拟机使用的DLL文件。
3. Java运行时环境使用的代码库，属性设置和资源文件。例如rt.jar和charsets.jar。
4. Java扩展文件，例如localedata.jar。
5. 包含用于安全管理的文件。这些文件包括安全策略（java.policy）和安全属性（java.security）文件。
6. 包含applet支持类的Jar文件。
7. 包含供平台使用的TrueType字体文件。

`JRE`可以作为`JDK`的一部分下载，也可以单独下载。`JRE`与平台有关。您可以根据您的计算机的类型（操作系统和体系结构）选择要导入和安装的`JRE`软件包。

比如，你不能在`32`位计算机上安装`64`位`JRE`。同样，用于`Windows`的`JRE`发行版在`Linux`上将无法运行。反之亦然。

### 什么是JDK

`JDK`比`JRE`更加全面。`JDK`包含`JRE`拥有的所有部门以及用于开发，调试和监视`Java`应用程序的开发工具。当需要开发`Java`应用程序时，需要`JDK`。

`JDK`附带的几个重要组件如下：

- appletviewer –此工具可用于在没有Web浏览器的情况下运行和调试Java applet
- apt –注释处理工具
- extcheck –一种检测JAR文件冲突的实用程序
- javadoc –文档生成器，可从源代码注释自动生成文档
- jar –存档程序，它将相关的类库打包到一个JAR文件中。该工具还有助于管理JAR文件
- jarsigner – jar签名和验证工具javap –类文件反汇编程序
- javaws – JNLP应用程序的Java Web Start启动器
- JConsole – Java监视和管理控制台
- jhat – Java堆分析工具
- jrunscript – Java命令行脚本外壳
- jstack –打印Java线程的Java堆栈跟踪的实用程序
- keytool –用于操作密钥库的工具
- policytool –策略创建和管理工具
- xjc – XML绑定Java API（JAXB）API的一部分。它接受XML模式并生成Java类

与`JRE`一样，`JDK`也依赖于平台。因此，在为您的计算机下载`JDK`软件包时请多加注意。

### JDK，JRE和JVM之间的区别

基于以上讨论，我们可以得出以下这三者之间的关系

```
JRE = JVM + libraries to run Java application.

JDK = JRE + tools to develop Java Application.
```
[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/java/java15.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/java/java15.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/java/java15.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/java/java15.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/java/java15.png")

简而言之，如果你是编写代码的`Java`应用程序开发人员，则需要在计算机中安装`JDK`。但是，如果只想运行用`Java`内置的应用程序，则只需要在计算机上安装`JRE`。


### JDK，JRE和JVM相关的面试问题

如果你理解我们在这篇文章中讨论的内容，那么面对任何面试问题都不难。不过，还是要准备好回答如下问题：

#### 什么是JVM架构

上面已经详细解释过了。

#### Java有几种类型的类加载器

`Bootstrap`, `extension`、应用程序类加载器和自定义类加载器。

#### 类加载器是如何在Java中工作的？

类加载器会在其预定义位置扫描`jar`文件和类。他们扫描路径中的所有那些类文件，并查找所需的类。如果找到它们，则加载，链接并初始化类文件。

#### jre和jvm的区别？

`JVM`是用于运行Java应用程序的运行时环境的规范。`Hotspot` `JVM`是规范的这样一种实现。它加载类文件，并使用解释器和`JIT`编译器将字节码转换为机器代码并执行。

#### 解释器和jit编译器的区别？

解释器逐行解释字节码并顺序执行。这会导致性能下降。`JIT`编译器通过分析块中的代码来为该过程添加优化，然后准备更多优化的机器代码。

### JDK 和 JRE下载

- JDK:[https://www.oracle.com/technetwork/java/javase/downloads/index.html](https://www.oracle.com/technetwork/java/javase/downloads/index.html)
- JRE [https://www.oracle.com/technetwork/java/javase/downloads/server-jre8-downloads-2133154.html](https://www.oracle.com/technetwork/java/javase/downloads/server-jre8-downloads-2133154.html)



---
[![https://niocoder.com/assets/images/qrcode.jpg](https://niocoder.com/assets/images/qrcode.jpg "https://niocoder.com/assets/images/qrcode.jpg")](https://niocoder.com/assets/images/qrcode.jpg "https://niocoder.com/assets/images/qrcode.jpg")



> 🙂🙂🙂关注微信公众号**java干货**
不定期分享干货资料


原文链接：[What is Java JDK, JRE and JVM – In-depth Analysis](https://howtodoinjava.com/java/basics/jdk-jre-jvm/#interview-questions)
