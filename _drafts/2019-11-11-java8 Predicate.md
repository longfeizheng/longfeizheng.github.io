---
layout: post
title: java8 Predicate
categories: java8
description: java8
keywords: java8
---

>相思似海深，旧事如天远。

a

## 概述 ##

在`Java 8`中，`Predicate`是一个函数接口，因此可以用作`lambda`表达式或方法引用。那么，你认为我们可以在日常编程中使用这些`true/false`返回函数吗?
我要说的是，你可以在需要对类似对象的`group/collection`上的条件求值的任何地方使用`Predicate`，以便求值的结果可以是true或false。

例如，可以在这些实时用例中使用`Predicate`

1. 找到所有在特定日期出生的孩子
2. 特定的时间订了的披萨
3. 超过一定年龄的员工等等

### `Java` `Predicate`类

`Java` `Predicate`看起来很有趣。让我们去深入。

正如我所说，`Predicate`是功能接口。这意味着我们可以在需要`Predicate`的任何地方传递`lambda`表达式。例如，一种这样的方法是`Stream`接口中的`filter（）`方法。

```java
/**
 * Returns a stream consisting of the elements of this stream that match
 * the given predicate.
 *
 * <p>This is an <a href="package-summary.html#StreamOps">intermediate
 * operation</a>.
 *
 * @param predicate a non-interfering stateless predicate to apply to each element to determine if it
 * should be included in the new returned stream.
 * @return the new stream
 */
Stream<T> filter(Predicate<? super T> predicate);
```

我们可以将流假定为一种机制，以创建支持顺序和并行聚合操作的元素序列。这意味着我们可以随时通过一次调用收集并执行流中存在的所有元素的某些操作。

因此，从本质上讲，我们可以使用流和`Predicate`来–

- 首先从组中过滤某些元素，然后
- 然后对过滤后的元素执行一些操作。

### 在集合上使用`Predicate`

为了演示，我们有一个`Employee`类，如下所示：

```java
package predicateExample;
 
public class Employee {
    
   public Employee(Integer id, Integer age, String gender, String fName, String lName){
       this.id = id;
       this.age = age;
       this.gender = gender;
       this.firstName = fName;
       this.lastName = lName;
   } 
     
   private Integer id;
   private Integer age;
   private String gender;
   private String firstName;
   private String lastName;
 
   //Please generate Getter and Setters
 
   //To change body of generated methods, choose Tools | Templates.
    @Override
    public String toString() {
        return this.id.toString()+" - "+this.age.toString(); 
    }
}
```

#### 所有年龄在21岁以上的男性员工

```java
public static Predicate<Employee> isAdultMale() 
{
    return p -> p.getAge() > 21 && p.getGender().equalsIgnoreCase("M");
}
```

#### 所有女性且年龄在18岁以上的员工

```java

public static Predicate<Employee> isAdultFemale() 
{
    return p -> p.getAge() > 18 && p.getGender().equalsIgnoreCase("F");
}

```

#### 所有年龄超过给定年龄的员工

```java

public static Predicate<Employee> isAgeMoreThan(Integer age) 
{
    return p -> p.getAge() > age;
}

```

你可以在需要的时候构建更多。到目前为止一切顺利。在使用上述方法之前，我已经在`EmployeePredicates`中包含了上述3个方法。

```java
package predicateExample;
 
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
 
public class EmployeePredicates 
{
    public static Predicate<Employee> isAdultMale() {
        return p -> p.getAge() > 21 && p.getGender().equalsIgnoreCase("M");
    }
     
    public static Predicate<Employee> isAdultFemale() {
        return p -> p.getAge() > 18 && p.getGender().equalsIgnoreCase("F");
    }
     
    public static Predicate<Employee> isAgeMoreThan(Integer age) {
        return p -> p.getAge() > age;
    }
     
    public static List<Employee> filterEmployees (List<Employee> employees, 
                                                Predicate<Employee> predicate) 
    {
        return employees.stream()
                    .filter( predicate )
                    .collect(Collectors.<Employee>toList());
    }
} 
```

你会看到我创建了另一个实用程序方法`filterEmployees（）`来显示`java` `Predicate`过滤器。基本上是使代码整洁并减少重复。您也可以编写多个`Predicate`来构成`Predicate`链，就像在构建器模式中一样。

因此，在此函数中，我们传递员工列表，并传递 `Predicate`，然后此函数将返回满足参数`Predicate`中提及条件的新员工集合。

```java
package predicateExample;
 
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static predicateExample.EmployeePredicates.*;
 
public class TestEmployeePredicates 
{
    public static void main(String[] args)
    {
        Employee e1 = new Employee(1,23,"M","Rick","Beethovan");
        Employee e2 = new Employee(2,13,"F","Martina","Hengis");
        Employee e3 = new Employee(3,43,"M","Ricky","Martin");
        Employee e4 = new Employee(4,26,"M","Jon","Lowman");
        Employee e5 = new Employee(5,19,"F","Cristine","Maria");
        Employee e6 = new Employee(6,15,"M","David","Feezor");
        Employee e7 = new Employee(7,68,"F","Melissa","Roy");
        Employee e8 = new Employee(8,79,"M","Alex","Gussin");
        Employee e9 = new Employee(9,15,"F","Neetu","Singh");
        Employee e10 = new Employee(10,45,"M","Naveen","Jain");
         
        List<Employee> employees = new ArrayList<Employee>();
        employees.addAll(Arrays.asList(new Employee[]{e1,e2,e3,e4,e5,e6,e7,e8,e9,e10}));
                
        System.out.println( filterEmployees(employees, isAdultMale()) );
         
        System.out.println( filterEmployees(employees, isAdultFemale()) );
         
        System.out.println( filterEmployees(employees, isAgeMoreThan(35)) );
         
        //Employees other than above collection of "isAgeMoreThan(35)" 
        //can be get using negate()
        System.out.println(filterEmployees(employees, isAgeMoreThan(35).negate()));
    }
}
```
```text
Output:
 
[1 - 23, 3 - 43, 4 - 26, 8 - 79, 10 - 45]
[5 - 19, 7 - 68]
[3 - 43, 7 - 68, 8 - 79, 10 - 45]
[1 - 23, 2 - 13, 4 - 26, 5 - 19, 6 - 15, 9 - 15]
```

`Predicate`在Java 8中是非常好的补充，我一有机会就会使用它。

### 对`Java 8`中`Predicate`的最后思考

1. 它们将你的条件(有时是业务逻辑)移动到一个中心位置。这有助于分别对它们进行单元测试。
2. 无需将任何更改复制到多个位置。`Java` `Predicate`改善了代码维护。
3. 代码例如` filterEmployees（employees，isAdultFemale（））`比编写`if-else`块更具可读性。


原文链接：[Java Predicate Example – Predicate Filter](https://howtodoinjava.com/java8/how-to-use-predicate-in-java-8/)
