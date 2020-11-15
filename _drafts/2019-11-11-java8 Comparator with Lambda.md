---
layout: post
title: java8 Comparator with Lambda
categories: java8
description: java8
keywords: java8
---

>过尽千帆皆不是，斜晖脉脉水悠悠，肠断白频洲。

a


当我们要对可以相互比较的对象集合进行排序时，使用比较器。也可以使用`Comparable`接口完成此比较，但是它限制了你只能以一种特定的方式比较这些对象。
如果要基于多个条件/字段对该集合进行排序，则仅需使用比较器。

```java
//Compare by Id
Comparator<Employee> compareById_1 = Comparator.comparing(e -> e.getId());
 
Comparator<Employee> compareById_2 = (Employee o1, Employee o2) -> o1.getId().compareTo( o2.getId() );
 
//Compare by firstname
Comparator<Employee> compareByFirstName = Comparator.comparing(e -> e.getFirstName());
 
//how to use comparator
Collections.sort(employees, compareById);
```

### 概述

为了演示这个概念，我使用具有四个属性的Employee类。我们将使用它来理解各种用例。

```java
public class Employee {
    private Integer id;
    private String firstName;
    private String lastName;
    private Integer age;
     
    public Employee(Integer id, String firstName, String lastName, Integer age){
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
    }
     
    //Other getter and setter methods
     
    @Override
    public String toString() {
        return "\n["+this.id+","+this.firstName+","+this.lastName+","+this.age+"]"; 
    }
}
```

另外，我还写了一个方法，它总是以未排序的顺序返回雇员列表

```java
private static List<Employee> getEmployees(){
    List<Employee> employees  = new ArrayList<>();
    employees.add(new Employee(6,"Yash", "Chopra", 25));
    employees.add(new Employee(2,"Aman", "Sharma", 28));
    employees.add(new Employee(3,"Aakash", "Yaadav", 52));
    employees.add(new Employee(5,"David", "Kameron", 19));
    employees.add(new Employee(4,"James", "Hedge", 72));
    employees.add(new Employee(8,"Balaji", "Subbu", 88));
    employees.add(new Employee(7,"Karan", "Johar", 59));
    employees.add(new Employee(1,"Lokesh", "Gupta", 32));
    employees.add(new Employee(9,"Vishu", "Bissi", 33));
    employees.add(new Employee(10,"Lokesh", "Ramachandran", 60));
    return employees;
}
```

### 按名字排序

基本用例，其中将根据员工的名字对员工列表进行排序。

```java
    List<Employee> employees  = getEmployees();
     
    //Sort all employees by first name
    employees.sort(Comparator.comparing(e -> e.getFirstName()));
     
    //OR you can use below
    employees.sort(Comparator.comparing(Employee::getFirstName));
     
    //Let's print the sorted list
    System.out.println(employees);
     
Output: //Names are sorted by first name
 
[
    [3,Aakash,Yaadav,52], 
    [2,Aman,Sharma,28], 
    [8,Balaji,Subbu,88], 
    [5,David,Kameron,19], 
    [4,James,Hedge,72], 
    [7,Karan,Johar,59], 
    [1,Lokesh,Gupta,32], 
    [10,Lokesh,Ramachandran,60], 
    [9,Vishu,Bissi,33], 
    [6,Yash,Chopra,25]
]
```

### 按名字排序-倒序

如果我们想按名字排序但顺序相反该怎么办。这真的很容易；使用`reverse（）`方法。

```java
    List<Employee> employees  = getEmployees();
     
    //Sort all employees by first name; And then reversed
    Comparator<Employee> comparator = Comparator.comparing(e -> e.getFirstName());
    employees.sort(comparator.reversed());
     
    //Let's print the sorted list
    System.out.println(employees);
     
Output: //Names are sorted by first name
 
[[6,Yash,Chopra,25], 
[9,Vishu,Bissi,33], 
[1,Lokesh,Gupta,32], 
[10,Lokesh,Ramachandran,60], 
[7,Karan,Johar,59], 
[4,James,Hedge,72], 
[5,David,Kameron,19], 
[8,Balaji,Subbu,88], 
[2,Aman,Sharma,28], 
[3,Aakash,Yaadav,52]]
```

### 按姓氏排序

我们也可以使用类似的代码对姓氏进行排序。

```java
    List<Employee> employees  = getEmployees();
     
    //Sort all employees by first name
    employees.sort(Comparator.comparing(e -> e.getLastName()));
     
    //OR you can use below
    employees.sort(Comparator.comparing(Employee::getLastName));
     
    //Let's print the sorted list
    System.out.println(employees);
     
Output: //Names are sorted by first name
 
[[9,Vishu,Bissi,33], 
[6,Yash,Chopra,25], 
[1,Lokesh,Gupta,32], 
[4,James,Hedge,72], 
[7,Karan,Johar,59], 
[5,David,Kameron,19], 
[10,Lokesh,Ramachandran,60], 
[2,Aman,Sharma,28], 
[8,Balaji,Subbu,88], 
[3,Aakash,Yaadav,52]]
```

### 在多个字段上排序– `thenComparing（）`

在这里，我们首先按员工的名字对员工列表进行排序，然后对姓氏列表进行再次排序。就像我们对`SQL`语句应用排序一样。这实际上是一个非常好的功能。

现在，您无需始终对`SQL select`语句中的多个字段使用排序，也可以在`Java`中对其进行排序。

```java
List<Employee> employees  = getEmployees();
 
//Sorting on multiple fields; Group by.
Comparator<Employee> groupByComparator = Comparator.comparing(Employee::getFirstName)
                                                    .thenComparing(Employee::getLastName);
employees.sort(groupByComparator);
 
System.out.println(employees);
 
Output:
 
[3,Aakash,Yaadav,52], 
[2,Aman,Sharma,28], 
[8,Balaji,Subbu,88], 
[5,David,Kameron,19], 
[4,James,Hedge,72], 
[7,Karan,Johar,59], 
[1,Lokesh,Gupta,32],         //These both employees are 
[10,Lokesh,Ramachandran,60], //sorted on last name as well
[9,Vishu,Bissi,33], 
[6,Yash,Chopra,25]
```

### 并行排序（多个线程）

您也可以使用多个线程并行排序对象集合。如果集合足够大，可以容纳数千个对象，那么它将非常快。对于少量对象，常规排序就足够了，建议使用。

```java
//Parallel Sorting
Employee[] employeesArray = employees.toArray(new Employee[employees.size()]);
 
//Parallel sorting
Arrays.parallelSort(employeesArray, groupByComparator);
 
System.out.println(employeesArray);
 
Output:
 
[3,Aakash,Yaadav,52], 
[2,Aman,Sharma,28], 
[8,Balaji,Subbu,88], 
[5,David,Kameron,19], 
[4,James,Hedge,72], 
[7,Karan,Johar,59], 
[1,Lokesh,Gupta,32],         //These both employees are 
[10,Lokesh,Ramachandran,60], //sorted on last name as well
[9,Vishu,Bissi,33], 
[6,Yash,Chopra,25]
```

这就是将`lambda`与`Comparator`结合使用来对对象进行排序的原因。



原文链接：[Java Comparator with Lambda](https://howtodoinjava.com/java8/using-comparator-becomes-easier-with-lambda-expressions-java-8/)
