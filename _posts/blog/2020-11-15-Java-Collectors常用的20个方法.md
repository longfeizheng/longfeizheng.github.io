---
layout: post
title: Java-Collectors常用的20个方法
categories: java
description: java
keywords: java
---
> 相思相见知何日？此时此夜难为情。

![](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/java/java41.jpg)


## `返回List集合: toList()`

用于将元素累积到`List`集合中。它将创建一个新`List`集合（不会更改当前集合）。

```java
List<Integer> integers = Arrays.asList(1,2,3,4,5,6,6);
integers.stream().map(x -> x*x).collect(Collectors.toList());
// output: [1,4,9,16,25,36,36]
```

## `返回Set集合: toSet()`

用于将元素累积到`Set`集合中。它会删除重复元素。

```java
List<Integer> integers = Arrays.asList(1,2,3,4,5,6,6);
integers.stream().map(x -> x*x).collect(Collectors.toSet());
// output: [1,4,9,16,25,36]
```

## `返回指定的集合: toCollection()`

可以将元素雷击到指定的集合中。

```java
List<Integer> integers = Arrays.asList(1,2,3,4,5,6,6);
integers
    .stream()
    .filter(x -> x >2)
    .collect(Collectors.toCollection(LinkedList::new));
// output: [3,4,5,6,6]
```

## `计算元素数量: Counting()`

用于返回计算集合中存在的元素个数。

```java
List<Integer> integers = Arrays.asList(1,2,3,4,5,6,6);
Long collect = integers
                   .stream()
                   .filter(x -> x <4)
                   .collect(Collectors.counting());
// output: 3
```

## `求最小值: minBy()`

用于返回列表中存在的最小值。

```java
List<Integer> integers = Arrays.asList(1,2,3,4,5,6,6);
List<String> strings = Arrays.asList("alpha","beta","gamma");
integers
    .stream()
    .collect(Collectors.minBy(Comparator.naturalOrder()))
    .get();
// output: 1
strings
   .stream()
   .collect(Collectors.minBy(Comparator.naturalOrder()))
   .get();
// output: alpha
```

按照整数排序返回1，按照字符串排序返回alpha

可以使用reverseOrder（）方法反转顺序。

```java
List<Integer> integers = Arrays.asList(1,2,3,4,5,6,6);
List<String> strings = Arrays.asList("alpha","beta","gamma");
integers
    .stream()
    .collect(Collectors.minBy(Comparator.reverseOrder()))
    .get();
// output: 6
strings
   .stream()
   .collect(Collectors.minBy(Comparator.reverseOrder()))
   .get();
// output: gamma

```

同时可以自定义的对象定制比较器。

## `求最大值: maxBy()`

和最小值方法类似，使用`maxBy（）`方法来获得最大值。

```java
List<String> strings = Arrays.asList("alpha","beta","gamma");
strings
   .stream()
   .collect(Collectors.maxBy(Comparator.naturalOrder()))
   .get();
// output: gamma
```


## `分区列表:partitioningBy()`

用于将一个集合划分为2个集合并将其添加到映射中，1个满足给定条件，另一个不满足，例如从集合中分离奇数。因此它将在`map`中生成2条数据，1个以`true`为`key`，奇数为值，第2个以`false`为`key`，以偶数为值。

```java
List<String> strings = Arrays.asList("a","alpha","beta","gamma");
Map<Boolean, List<String>> collect1 = strings
          .stream()
          .collect(Collectors.partitioningBy(x -> x.length() > 2));
// output: {false=[a], true=[alpha, beta, gamma]}
```

这里我们将长度大于2的字符串与其余字符串分开。

## `返回不可修改的List集合：toUnmodifiableList（）`

用于创建只读`List`集合。任何试图对此不可修改`List`集合进行更改的尝试都将导致`UnsupportedOperationException`。

```java
List<String> strings = Arrays.asList("alpha","beta","gamma");
List<String> collect2 = strings
       .stream()
       .collect(Collectors.toUnmodifiableList());
// output: ["alpha","beta","gamma"]
```

## `返回不可修改的Set集合：toUnmodifiableSet()`

用于创建只读`Set`集合。任何试图对此不可修改`Set`集合进行更改的尝试都将导致`UnsupportedOperationException`。它会删除重复元素。

```java
List<String> strings = Arrays.asList("alpha","beta","gamma","alpha");
Set<String> readOnlySet = strings
       .stream()
       .sorted()
       .collect(Collectors.toUnmodifiableSet());
// output: ["alpha","beta","gamma"]
```

## `连接元素：Joining（）`

用指定的字符串链接集合内的元素。

```java
List<String> strings = Arrays.asList("alpha","beta","gamma");
String collect3 = strings
     .stream()
     .distinct()
     .collect(Collectors.joining(","));
// output: alpha,beta,gamma
String collect4 = strings
     .stream()
     .map(s -> s.toString())
     .collect(Collectors.joining(",","[","]"));
// output: [alpha,beta,gamma]
```

## `Long类型集合的平均值：averagingLong()`

查找`Long`类型集合的平均值。

注意：返回的是`Double`类型而不是 `Long`类型
```java
List<Long> longValues = Arrays.asList(100l,200l,300l);
Double d1 = longValues
    .stream()
    .collect(Collectors.averagingLong(x -> x * 2));
// output: 400.0
```

## `Integer类型集合的平均值：averagingInt()`

查找`Integer`类型集合的平均值。

注意：返回的是`Double`类型而不是 `int`类型

```java
List<Integer> integers = Arrays.asList(1,2,3,4,5,6,6);
Double d2 = integers
    .stream()
    .collect(Collectors.averagingInt(x -> x*2));
// output: 7.714285714285714
```

## `Double类型集合的平均值：averagingDouble()`

查找`Double`类型集合的平均值。


```java
List<Double> doubles = Arrays.asList(1.1,2.0,3.0,4.0,5.0,5.0);
Double d3 = doubles
    .stream()
    .collect(Collectors.averagingDouble(x -> x));
// output: 3.35
```

## `创建Map：toMap（）`

根据集合的值创建`Map`。


```java
List<String> strings = Arrays.asList("alpha","beta","gamma");
Map<String,Integer> map = strings
       .stream()
       .collect(Collectors
          .toMap(Function.identity(),String::length));
// output: {alpha=5, beta=4, gamma=5}
```

创建了一个`Map`，其中集合值作为`key`，在集合中的出现次数作为值。

## `在创建`map`时处理列表的重复项`

集合中可以包含重复的值，因此，如果想从列表中创建一个`Map`，并希望使用集合值作为map的`key`，那么需要解析重复的`key`。由于map只包含唯一的`key`，可以使用比较器来实现这一点。


```java
List<String> strings = Arrays.asList("alpha","beta","gamma","beta");
Map<String,Integer> map = strings
        .stream()
        .collect(Collectors
          .toMap(Function.identity(),String::length,(i1,i2) -> i1));
// output: {alpha=5, gamma=5, beta=4}
```

`Function.identity()`指向集合中的值，`i1`和`i2`是重复键的值。可以只保留一个值，这里选择`i1`，也可以用这两个值来计算任何东西，比如把它们相加，比较和选择较大的那个，等等。

## `整数求和:summingInt ()`

查找集合中所有整数的和。它并不总是初始集合的和，就像我们在下面的例子中使用的我们使用的是字符串列表，首先我们把每个字符串转换成一个等于它的长度的整数，然后把所有的长度相加。


```java
List<String> strings = Arrays.asList("alpha","beta","gamma");
Integer collect4 = strings
      .stream()
      .collect(Collectors.summingInt(String::length));
// output: 18
```

或直接集合值和

```java
List<Integer> integers = Arrays.asList(1,2,3,4,5,6,6);
Integer sum = integers
    .stream()
    .collect(Collectors.summingInt(x -> x));
// output: 27
```

## `double求和:summingDouble ()`

类似于整数求和，只是它用于双精度值


```java
List<Double>  doubleValues = Arrays.asList(1.1,2.0,3.0,4.0,5.0,5.0);
Double sum = doubleValues
     .stream()
     .collect(Collectors.summingDouble(x ->x));
// output: 20.1
```

## `Long求和:summingLong ()`

与前两个相同，用于添加`long`值或`int`值。可以对`int`值使用`summinglong()`，但不能对`long`值使用`summingInt()`。


```java
List<Long> longValues = Arrays.asList(100l,200l,300l);
Long sum = longValues
    .stream()
    .collect(Collectors.summingLong(x ->x));
// output: 600
```

## `Long求和:summingLong ()`

与前两个相同，用于添加`long`值或`int`值。可以对`int`值使用`summinglong()`，但不能对`long`值使用`summingInt()`。


```java
List<Long> longValues = Arrays.asList(100l,200l,300l);
Long sum = longValues
    .stream()
    .collect(Collectors.summingLong(x ->x));
// output: 600
```

## `汇总整数:summarizingInt ()`

它给出集合中出现的值的所有主要算术运算值，如所有值的平均值、最小值、最大值、所有值的计数和总和。


```java
List<Integer> integers = Arrays.asList(1,2,3,4,5,6,6);
IntSummaryStatistics stats = integers
          .stream()
          .collect(Collectors.summarizingInt(x -> x ));
//output: IntSummaryStatistics{count=7, sum=27, min=1, average=3.857143, max=6}
```

可以使用`get`方法提取不同的值，如:

```java
stats.getAverage();   // 3.857143
stats.getMax();       // 6
stats.getMin();       // 1
stats.getCount();     // 7
stats.getSum();       // 27
```

## `分组函数:GroupingBy ()`

`GroupingBy()`是一种高级方法，用于从任何其他集合创建`Map`。


```java
List<String> strings = Arrays.asList("alpha","beta","gamma");
Map<Integer, List<String>> collect = strings
          .stream()
          .collect(Collectors.groupingBy(String::length));
// output: {4=[beta, beta], 5=[alpha, gamma]}
```

它将字符串长度作为`key`，并将该长度的字符串列表作为`value`。

```java
List<String> strings = Arrays.asList("alpha","beta","gamma");
Map<Integer, LinkedList<String>> collect1 = strings
            .stream()
            .collect(Collectors.groupingBy(String::length, 
                Collectors.toCollection(LinkedList::new)));
// output: {4=[beta, beta], 5=[alpha, gamma]}
```

这里指定了`Map`中需要的列表类型(`Libkedlist`)。


---
[![//p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/6b3a50846a8d40c79b4db3de866b41d5~tplv-k3u1fbpfcp-zoom-1.image](//p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/6897912b2f634918a7e30022071e12d9~tplv-k3u1fbpfcp-zoom-1.image "https://user-gold-cdn.xitu.io/2019/9/18/16d42fc88345bad5?w=258&h=258&f=jpeg&s=26702")](https://user-gold-cdn.xitu.io/2019/9/18/16d42fc88345bad5?w=258&h=258&f=jpeg&s=26702 "https://user-gold-cdn.xitu.io/2019/9/18/16d42fc88345bad5?w=258&h=258&f=jpeg&s=26702")

> 🙂🙂🙂微信公众号**java干货**







