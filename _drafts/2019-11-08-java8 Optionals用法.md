---
layout: post
title: jav8 Optionals用法参考
categories: java8
description: java8
keywords: java8
---

>相思似海深，旧事如天远。

a

## 概述 ##

我们所有人在应用程序中一定都遇到过`NullPointerException`。当你试图使用未初始化的对象引用、使用`null`初始化或不指向任何实例时，会发生此异常。
`NULL`只是表示“缺少值”。最有可能的是，罗马人是唯一没有遇到这个零问题的人，他们从一、二、三开始计数。(无零)。

“I call it my billion-dollar mistake” – `C. A. R. Hoare`爵士，关于发明空引用

在本文中，我将讨论这个特定用例的`java 8`特性之一，即可选的。为了清晰和区分多个概念，本文分为多个部分。

### 空类型是什么？

在`Java`中，我们使用引用类型来访问对象，而当我们没有特定的对象作为引用点时，则将此类引用设置为`null`表示没有值。对吧？

`null`的使用是如此普遍，以至于我们很少花更多心思在它上面。例如，对象的字段成员被自动初始化为`null`，而程序员通常在没有初始值时将引用类型初始化为`null`
通常，在我们不知道或没有值可以提供给引用的情况下，都会使用`null`。

> 顺便说一句，在`Java`中，`null`实际上是一种类型，一种特殊类型。它没有名称，所以我们不能声明其类型的变量，也不能将任何变量强制转换为它;实际上，只有一个值可以与之关联(即文字`null`)。
>请记住，与`Java`中的任何其他类型不同，空引用可以安全地分配给任何其他引用类型，而不会出现任何错误(参见`JLS 3.10.7`和`4.1`)。


### 仅返回`null`有什么问题？

通常，`API`设计人员将描述性的`Java`文档放入`AP`I中，并在其中提到`API`可以返回空值，在这种情况下，返回值可以是`null`。现在，
问题在于`API`的调用者可能由于某种原因而错过了阅读`Javadoc`的时间，而忘记了处理`null`的情况。肯定的将来这将是一个错误。

相信我，这经常发生，并且是空指针异常的主要原因之一，尽管不是唯一的原因。因此，在这里上一课，当您第一次使用它时，请务必阅读它的`Java dcos`。

> 现在我们知道在大多数情况下`null`是一个问题，什么是最好的处理方式？

一个好的解决方案是始终使用一些值初始化对象引用，而不是使用`null`。通过这种方式，你将永远不会遇到`NullPointerException`。但是在实际中，我们总是没有一个默认值作为参考。那么，这些情况应该如何处理呢?

上述问题在许多意义上是正确的。`java 8` `Optionals`就是答案。

### `Java 8` `Optionals`如何提供解决方案？

`Optionals`是用非空值替换可空`T`引用的方法。`Optional`可以包含非空`T`引用（在这种情况下，我们称该引用为“存在”），
也可以不包含任何内容（在这种情况下，我们称该引用为“不存在”）。

> 请记住，从来没有说过`Optionals`“包含`null`"”。

```java
Optional<Integer> canBeEmpty1 = Optional.of(5);
canBeEmpty1.isPresent();                    // returns true
canBeEmpty1.get();                          // returns 5
 
Optional<Integer> canBeEmpty2 = Optional.empty();
canBeEmpty2.isPresent(); 
```

您还可以将`Optional`视为包含值或不包含值的单值容器。

重要的是要注意，`Optional`类的目的不是替换每个单个空引用。相反，其目的是帮助设计更易于理解的`API`，以便仅通过读取方法的签名即可知道是否可以期望一个可选值。
这迫使你从`Optional`中获取值并对其进行处理，同时处理`Optional`为空的情况。好吧，这正是空引用/返回值的解决方案，最终导致`NullPointerException`。

下面是一些示例，可以了解更多关于如何在应用程序代码中创建和使用`Optional`的信息。

#### 创建`Optional`对象

创建`Optional`的主要方法有3种。

- 使用`Optional.empty（）`创建空的`Optional`。
    ```java
    Optional<Integer> possible = Optional.empty(); 
    ```
- 使用`Optional.of（）`创建具有默认非空值的可选。如果在`of（）`中传递`null`，则立即引发`NullPointerException`。
    ```java
    Optional<Integer> possible = Optional.of(5);
    ``` 
- 使用`Optional.ofNullable（`）创建一个可以包含空值的`Optional`对象。如果参数为`null`，则生成的`Optional`对象将为空（请记住，该值不存在；请勿将其读取为`null`）。  

#### 如果`Optional`不为空，可执行的操作

得到`Optional`对象是第一步。现在让我们在检查它是否包含任何值之后使用它。

```java
Optional<Integer> possible = Optional.of(5); 
possible.ifPresent(System.out::println);

```

你可以重写上面的代码，也可以重写下面的代码。但是，这不推荐使用`Optional`的方式，因为它与嵌套的`null`检查相比没有太大的改进。它们看起来完全一样。

```java

if(possible.isPresent()){
    System.out.println(possible.get());
}
```
如果`Optional`对象为空，则不会打印任何内容。

#### 默认/缺省值的操作

如果确定操作结果为空，则编程中的典型模式是返回默认值。通常，您可以使用三元运算符。但是使用`Optionals`，您可以编写如下代码：

```java
//Assume this value has returned from a method
Optional<Company> companyOptional = Optional.empty();
 
//Now check optional; if value is present then return it, 
//else create a new Company object and retur it
Company company = companyOptional.orElse(new Company());
 
//OR you can throw an exception as well
Company company = companyOptional.orElseThrow(IllegalStateException::new);
```

#### 使用过滤器方法拒绝某些值

通常，你需要在对象上调用方法并检查某些属性。例如在下面的示例代码中，检查公司是否设有“财务”部门；如果有，请打印出来。

```java
Optional<Company> companyOptional = Optional.empty();
companyOptional.filter(department -> "Finance".equals(department.getName())
                    .ifPresent(() -> System.out.println("Finance is present"));
```

`filter`方法接受一个谓词作为参数。如果一个值出现在可选对象中，并且与谓词匹配，则筛选器方法将返回该值;否则，它返回一个空的可选对象。如果你对流接口使用了`filter`方法，那么你可能已经看到了类似的模式。


很好，这段代码看起来更接近问题语句，而且没有冗长的null检查!

从编写痛苦的嵌套空检查，到编写可组合、可读的声明性代码，以及更好地防止空指针异常，我们已经走过了漫长的道路。

### `Optional`内部是如何工作的？

如果打开`Optional.java`的源代码，你会发现`Optional`被定义为：

```java
/**
 * If non-null, the value; if null, indicates no value is present
 */
private final T value;
```

并且，如果你定义一个空的`Optional`，则它声明如下。`static`关键字可确保每个`VM`通常仅存在一个空实例。

默认的无参数构造函数被定义为`private`，因此你无法创建`Optional`的实例，除了上面给出的3种方法。

当你创建一个`Optional`时，最终会发生以下调用，并将传递的值分配给“ `value`”属性。

```java
this.value = Objects.requireNonNull(value);
```

当您尝试从`Optional`获取值时，如果为`value`为`null`抛出`NoSuchElementException`异常，否则会获取值：

```java
public T get() {
    if (value == null) {
        throw new NoSuchElementException("No value present");
    }
    return value;
}
```

同样，在`Optional`类中定义的其他函数仅在“值”属性附近起作用。浏览`Optional.java`的源代码以获取更多信息。

### `Optional`试图解决什么？

`Optional`的是通过考虑到有时缺少返回值来增加构建更具表现力的`API`的可能性，从而尝试减少`Java`系统中空指针异常的数量。如果从一开始就存在`Optional`，
那么今天大多数库和应用程序可能会更好地处理缺少的返回值，从而减少了空指针异常的数量以及总体上的错误总数。

通过使用`Optional`,用户不得不考虑异常情况。除了增加`null`名称的可读性之外，`Optional`最大的优点是它的惯用性证明。如果你希望你的程序能够编译，那么它将迫使你积极地考虑`null`的情况，因为你必须积极地打开`Optional`并处理失败的情况。

### `Optional`不尝试解决的问题是什么?

`Optional`并不是一种避免所有类型空指针的机制。方法和构造函数的强制输入参数仍然需要测试。

像使用`null`时一样，`Optional`不能帮助传达缺失值的含义。因此，该方法的调用者仍然必须检查`API`的`javadoc`，以了解缺少的`Optional`的含义，以便对其进行正确处理。

请注意，`Optional`不能在以下情况下使用，因为它可能不会给我们带来任何好处：

- 在`domain`类中（不可序列化）
- 在`DTO`类中（不可序列化）
- 在方法的输入参数中
- 在构造函数参数中

### 应该如何使用`Optional`？

几乎所有时候都应该使用`Optional`作为可能不返回值的函数的返回类型。

这是来自`OpenJDK`邮件列表的报价：
> The JSR-335 EG felt fairly strongly that Optional should not be on any more than needed to support the optional-return idiom only.
  Someone suggested maybe even renaming it to “OptionalReturn“.

### 总结

在本文中，我们了解了如何采用新的`Java SE 8 java.util.Optional`。`Optional`的目的不是替换代码库中的每个空引用，而是帮助您设计更好的`API`，在这种情况下，仅通过读取方法的签名，用户就可以判断返回是否期望一个可选值并适当地处理它。

原文链接：[Java 8 Optionals : Complete Reference](https://howtodoinjava.com/java8/java-8-optionals-complete-reference/)
