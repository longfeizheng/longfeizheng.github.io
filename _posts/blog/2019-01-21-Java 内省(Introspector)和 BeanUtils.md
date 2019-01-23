---
layout: post
title: Java 内省(Introspector)和 BeanUtils
categories: Java
description: Java
keywords: Java
---

> 人生若只如初见，何事秋风悲画扇。

<iframe width="750" height="350" src="http://www.merryyou.cn/mp4/java04.mp4" frameborder="0" allowfullscreen></iframe>
## 概述

内省(Introspector) 是Java 语言对 JavaBean 类属性、事件的一种缺省处理方法。

JavaBean是一种特殊的类，主要用于传递数据信息，这种类中的方法主要用于访问私有的字段，且方法名符合某种命名规则。如果在两个模块之间传递信息，可以将信息封装进JavaBean中，这种对象称为“值对象”(Value Object)，或“VO”。方法比较少。这些信息储存在类的私有变量中，通过set()、get()获得。例如`UserInfo`

```java
package com.niocoder.test.introspector;

/**
 *
 */
 public class UserInfo {
     private String userName;
     private Integer age;
     private String webSite;

     public Integer getAge() {
         return age;
     }

     public void setAge(Integer age) {
         this.age = age;
     }

     public String getUserName() {
         return userName;
     }

     public void setUserName(String userName) {
         this.userName = userName;
     }

     public String getWebSite() {
         return webSite;
     }

     public void setWebSite(String webSite) {
         this.webSite = webSite;
     }
 }


```
在类UserInfo中有属性 userName, 那我们可以通过 getUserName,setUserName来得到其值或者设置新的值。通过 getUserName/setUserName来访问 userName属性，这就是默认的规则。 Java JDK中提供了一套 API 用来访问某个属性的 getter/setter 方法，这就是内省。

## JDK内省类库：

### PropertyDescriptor

PropertyDescriptor类表示JavaBean类通过存储器导出一个属性。主要方法：

1. getPropertyType()，获得属性的Class对象;
2. getReadMethod()，获得用于读取属性值的方法；getWriteMethod()，获得用于写入属性值的方法;
3. hashCode()，获取对象的哈希值;
4. setReadMethod(Method readMethod)，设置用于读取属性值的方法;
5. setWriteMethod(Method writeMethod)，设置用于写入属性值的方法。

```java
package com.niocoder.test.introspector;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

public class BeanInfoUtil {
  /**
   * @param userInfo 实例
   * @param propertyName 属性名
   * @throws Exception
   */
  public static void setProperty(UserInfo userInfo, String propertyName) throws Exception {
      PropertyDescriptor propDesc = new PropertyDescriptor(propertyName, UserInfo.class);
      Method methodSetUserName = propDesc.getWriteMethod();
      methodSetUserName.invoke(userInfo, "郑龙飞");
      System.out.println("set userName:" + userInfo.getUserName());
  }

  /**
   * @param userInfo 实例
   * @param propertyName 属性名
   * @throws Exception
   */
  public static void getProperty(UserInfo userInfo, String propertyName) throws Exception {
      PropertyDescriptor proDescriptor = new PropertyDescriptor(propertyName, UserInfo.class);
      Method methodGetUserName = proDescriptor.getReadMethod();
      Object objUserName = methodGetUserName.invoke(userInfo);
      System.out.println("get userName:" + objUserName.toString());
  }
}
```

### Introspector

将JavaBean中的属性封装起来进行操作。在程序把一个类当做JavaBean来看，就是调用Introspector.getBeanInfo()方法，得到的BeanInfo对象封装了把这个类当做JavaBean看的结果信息，即属性的信息。

getPropertyDescriptors()，获得属性的描述，可以采用遍历BeanInfo的方法，来查找、设置类的属性。具体代码如下：

```java
package com.niocoder.test.introspector;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

public class BeanInfoUtil {

  /**
    * @param userInfo 实例
    * @param propertyName 属性名
    * @throws Exception
    */
   public static void setPropertyByIntrospector(UserInfo userInfo, String propertyName) throws Exception {
       BeanInfo beanInfo = Introspector.getBeanInfo(UserInfo.class);
       PropertyDescriptor[] proDescrtptors = beanInfo.getPropertyDescriptors();
       if (proDescrtptors != null && proDescrtptors.length > 0) {
           for (PropertyDescriptor propDesc : proDescrtptors) {
               if (propDesc.getName().equals(propertyName)) {
                   Method methodSetUserName = propDesc.getWriteMethod();
                   methodSetUserName.invoke(userInfo, "niocoder");
                   System.out.println("set userName:" + userInfo.getUserName());
                   break;
               }
           }
       }
   }

   /**
    * @param userInfo 实例
    * @param propertyName 属性名
    * @throws Exception
    */
   public static void getPropertyByIntrospector(UserInfo userInfo, String propertyName) throws Exception {
       BeanInfo beanInfo = Introspector.getBeanInfo(UserInfo.class);
       PropertyDescriptor[] proDescrtptors = beanInfo.getPropertyDescriptors();
       if (proDescrtptors != null && proDescrtptors.length > 0) {
           for (PropertyDescriptor propDesc : proDescrtptors) {
               if (propDesc.getName().equals(propertyName)) {
                   Method methodGetUserName = propDesc.getReadMethod();
                   Object objUserName = methodGetUserName.invoke(userInfo);
                   System.out.println("get userName:" + objUserName.toString());
                   break;
               }
           }
       }
   }

}

```

通过这两个类的比较可以看出，都是需要获得PropertyDescriptor，只是方式不一样：前者通过创建对象直接获得，后者需要遍历，所以使用PropertyDescriptor类更加方便。

### Test

### BeanInfoTest

```java
public class BeanInfoTest {

    public static void main(String[] args) {
        UserInfo userInfo = new UserInfo();
        userInfo.setUserName("merryyou");
        try {
            BeanInfoUtil.getProperty(userInfo, "userName");

            BeanInfoUtil.setProperty(userInfo, "userName");

            BeanInfoUtil.getProperty(userInfo, "userName");

            BeanInfoUtil.setPropertyByIntrospector(userInfo, "userName");

            BeanInfoUtil.getPropertyByIntrospector(userInfo, "userName");

            BeanInfoUtil.setProperty(userInfo, "age");

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
```
输出

```java
get userName:merryyou
set userName:郑龙飞
get userName:郑龙飞
set userName:niocoder
get userName:niocoder
Disconnected from the target VM, address: '127.0.0.1:65243', transport: 'socket'
java.lang.IllegalArgumentException: argument type mismatch
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:498)
	at com.niocoder.test.introspector.BeanInfoUtil.setProperty(BeanInfoUtil.java:13)
	at com.niocoder.test.introspector.BeanInfoTest.main(BeanInfoTest.java:19)


```

说明：`BeanInfoUtil.setProperty(userInfo, "age")`;报错是应为age属性是int数据类型，而setProperty方法里面默认给age属性赋的值是String类型。所以会爆出argument type mismatch参数类型不匹配的错误信息。


## BeanUtils

由上述可看出，内省操作非常的繁琐，所以所以Apache开发了一套简单、易用的API来操作Bean的属性——BeanUtils工具包。
- BeanUtils.getProperty(Object bean, String propertyName)
- BeanUtils.setProperty(Object bean, String propertyName, Object value)

### BeanUtilTest

```java
public class BeanUtilTest {

    public static void main(String[] args) {

        UserInfo userInfo = new UserInfo();
        try {

            BeanUtils.setProperty(userInfo, "userName", "郑龙飞");
            System.out.println("set userName:" + userInfo.getUserName());
            System.out.println("get userName:" + BeanUtils.getProperty(userInfo, "userName"));

            BeanUtils.setProperty(userInfo, "age", 18);
            System.out.println("set age:" + userInfo.getAge());
            System.out.println("get age:" + BeanUtils.getProperty(userInfo, "age"));
            System.out.println("get userName type:" + BeanUtils.getProperty(userInfo, "userName").getClass().getName());
            System.out.println("get age type:" + BeanUtils.getProperty(userInfo, "age").getClass().getName());

            PropertyUtils.setProperty(userInfo, "age", 8);
            System.out.println(PropertyUtils.getProperty(userInfo, "age"));
            System.out.println(PropertyUtils.getProperty(userInfo, "age").getClass().getName());
            // 特殊 age属性为Integer类型
            PropertyUtils.setProperty(userInfo, "age", "8");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
}
```

输出

```java
set userName:郑龙飞
get userName:郑龙飞
set age:18
get age:18
get userName type:java.lang.String
get age type:java.lang.String
8
java.lang.Integer
Disconnected from the target VM, address: '127.0.0.1:50244', transport: 'socket'
Exception in thread "main" java.lang.IllegalArgumentException: Cannot invoke com.niocoder.test.introspector.UserInfo.setAge on bean class 'class com.niocoder.test.introspector.UserInfo' - argument type mismatch - had objects of type "java.lang.String" but expected signature "java.lang.Integer"
	at org.apache.commons.beanutils.PropertyUtilsBean.invokeMethod(PropertyUtilsBean.java:2195)
	at org.apache.commons.beanutils.PropertyUtilsBean.setSimpleProperty(PropertyUtilsBean.java:2108)
	at org.apache.commons.beanutils.PropertyUtilsBean.setNestedProperty(PropertyUtilsBean.java:1914)
	at org.apache.commons.beanutils.PropertyUtilsBean.setProperty(PropertyUtilsBean.java:2021)
	at org.apache.commons.beanutils.PropertyUtils.setProperty(PropertyUtils.java:896)
	at com.niocoder.test.introspector.BeanUtilTest.main(BeanUtilTest.java:28)
Caused by: java.lang.IllegalArgumentException: argument type mismatch
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:498)
	at org.apache.commons.beanutils.PropertyUtilsBean.invokeMethod(PropertyUtilsBean.java:2127)
	... 5 more

Process finished with exit code 1
```
