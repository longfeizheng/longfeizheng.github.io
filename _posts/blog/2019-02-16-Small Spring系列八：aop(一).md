---
layout: post
title: Small Spring系列八：aop (一)
categories: Spring
description: Spring
keywords: Spring
---

>路漫漫其修远兮 吾将上下而求索。

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-08.jpg](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-08.jpg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-08.jpg")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-08.jpg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-08.jpg")

## 概述 ##

我们终于不辱使命完成了`Spring`的注解注入，接下来我们要实现更为关键`aop`部分，在这开始之前你需要了解什么事`aop`以及`aop`的常用术语，参考[链接](http://www.cnblogs.com/songanwei/p/9417343.html)

###  准备工作

#### bean-v5.xml

我们使用`xml`配置的方式实现aop

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!-- 增加namespace-->
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:aop="http://www.springframework.org/schema/aop">

    <!-- 扫描哪个包下面的文件 -->
    <context:component-scan base-package="com.niocoder.dao.v5,com.niocoder.service.v5">

    </context:component-scan>

    <!-- 模拟 TransactionManager-->
    <bean id="tx" class="com.niocoder.tx.TransactionManager"/>

    <!-- aop 配置-->
    <aop:config>
        <!-- aop 核心配置 依赖tx-->
        <aop:aspect ref="tx">
            <!-- 切入点配置 包下面的placeOrder 方法-->
            <aop:pointcut id="placeOrder"
                          expression="execution(* com.niocoder.service.v5.*.placeOrder(..))"/>
            <!-- 通知配置，-->
            <aop:before pointcut-ref="placeOrder" method="start"/>
            <aop:after-returning pointcut-ref="placeOrder" method="commit"/>
            <aop:after-throwing pointcut-ref="placeOrder" method="rollback"/>
        </aop:aspect>

    </aop:config>
</beans>
```

####  AccountDao

```java
package com.niocoder.dao.v5;

import com.niocoder.stereotype.Component;

@Component
public class AccountDao {
}

```

####  ItemDao

```java
package com.niocoder.dao.v5;

import com.niocoder.stereotype.Component;

@Component
public class ItemDao {
}
```

####  NioCoderService

新增`placeOrder`方法用于测试`aop`,`MessageTracker`用于测试`TransactionManager`是否执行。

```java
package com.niocoder.service.v5;

import com.niocoder.beans.factory.Autowired;
import com.niocoder.dao.v5.AccountDao;
import com.niocoder.dao.v5.ItemDao;
import com.niocoder.stereotype.Component;
import com.niocoder.util.MessageTracker;

@Component("nioCoder")
public class NioCoderService {

    @Autowired
    AccountDao accountDao;

    @Autowired
    ItemDao itemDao;

    public NioCoderService() {

    }

    public AccountDao getAccountDao() {
        return accountDao;
    }

    public ItemDao getItemDao() {
        return itemDao;
    }

    public void placeOrder() {
        System.out.println("place order");
        MessageTracker.addMsg("place order");
    }
}

```

####  MessageTracker

工具类用于记录`msg`

```java
package com.niocoder.util;

import java.util.ArrayList;
import java.util.List;

/**
 * 记录msg
 */
public class MessageTracker {
    private static List<String> MESSAGES = new ArrayList<>();

    public static void addMsg(String msg) {
        MESSAGES.add(msg);
    }

    public static void clearMsgs() {
        MESSAGES.clear();
    }

    public static List<String> getMsgs() {
        return MESSAGES;
    }

}

```

####  TransactionManager

模拟事务的执行。
```java
package com.niocoder.tx;

import com.niocoder.util.MessageTracker;
import org.junit.Before;

/**
 * 用于测试AOP顺序
 */
public class TransactionManager {
    @Before
    public void setUp() {
        MessageTracker.clearMsgs();
    }

    public void start() {
        System.out.println("start tx");
        MessageTracker.addMsg("start tx");
    }

    public void commit() {
        System.out.println("commit tx");
        MessageTracker.addMsg("commit tx");
    }

    public void rollback() {
        System.out.println("rollback tx");
    }
}


```



###  Pointcut

```xml
<aop:pointcut id="placeOrder"
                          expression="execution(* com.niocoder.service.v5.*.placeOrder(..))"/>
```
我们先从最简单的`Pointcut`开始,很明显我们需要一个类来表达这个概念。当给定一个类的方法，判断该方法是否符合`pointcut`的表达式。设计类图如下:
[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/aop_v1.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/aop_v1.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/aop_v1.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/aop_v1.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/aop_v1.png")

关于`expression`表达式的解析，我们使用`org.aspectj.aspectjweaver`来实现。所以需要在`pom.xml`中添加依赖

```xml
<dependency>
            <groupId>org.aspectj</groupId>
            <artifactId>aspectjweaver</artifactId>
            <version>1.8.13</version>
        </dependency>
```

####  MethodMatcher

给定一个类的方法，判断是否匹配。

```java
package com.niocoder.aop;

import java.lang.reflect.Method;

public interface MethodMatcher {

    /**
     * 给定一个方法判断是否匹配
     *
     * @param method
     * @return
     */
    boolean matches(Method method /*,Class<?> targetClass*/);
}
```

#### Pointcut

获取`expression`和`MethodMatcher`。

```java
package com.niocoder.aop;

public interface Pointcut {

    /**
     * 获取MethodMatcher 判断方法时候匹配
     *
     * @return
     */
    MethodMatcher getMethodMatcher();

    /**
     * 获取expression表达式
     *
     * @return
     */
    String getExpression();
}

```

####  AspectJExpressionPointcut

实现`MethodMatcher`和`Pointcut`,使用`aspectj`实现。

```java
package com.niocoder.aop.aspectj;

import com.niocoder.aop.MethodMatcher;
import com.niocoder.aop.Pointcut;
import com.niocoder.util.ClassUtils;
import com.niocoder.util.StringUtils;
import org.aspectj.weaver.reflect.ReflectionWorld;
import org.aspectj.weaver.tools.*;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;


public class AspectJExpressionPointcut implements Pointcut, MethodMatcher {
    private static final Set<PointcutPrimitive> SUPPORTED_PRIMITIVES = new HashSet<PointcutPrimitive>();

    static {
        SUPPORTED_PRIMITIVES.add(PointcutPrimitive.EXECUTION);
        SUPPORTED_PRIMITIVES.add(PointcutPrimitive.ARGS);
        SUPPORTED_PRIMITIVES.add(PointcutPrimitive.REFERENCE);
        SUPPORTED_PRIMITIVES.add(PointcutPrimitive.THIS);
        SUPPORTED_PRIMITIVES.add(PointcutPrimitive.TARGET);
        SUPPORTED_PRIMITIVES.add(PointcutPrimitive.WITHIN);
        SUPPORTED_PRIMITIVES.add(PointcutPrimitive.AT_ANNOTATION);
        SUPPORTED_PRIMITIVES.add(PointcutPrimitive.AT_WITHIN);
        SUPPORTED_PRIMITIVES.add(PointcutPrimitive.AT_ARGS);
        SUPPORTED_PRIMITIVES.add(PointcutPrimitive.AT_TARGET);
    }

    /**
     * 条件表达式 即 expression="execution(* com.niocoder.service.v5.*.placeOrder(..))"
     */
    private String expression;

    private PointcutExpression pointcutExpression;

    private ClassLoader pointcutClassLoader;

    public AspectJExpressionPointcut() {

    }

    @Override
    public MethodMatcher getMethodMatcher() {
        return this;
    }

    @Override
    public String getExpression() {
        return this.expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    @Override
    public boolean matches(Method method/*, Class<?> targetClass*/) {

        // 判断是否设置条件表达式
        checkReadyToMatch();

        // 根据传入的method 返回shadowatch
        ShadowMatch shadowMatch = getShadowMatch(method);

        // 判断是否匹配
        if (shadowMatch.alwaysMatches()) {
            return true;
        }

        return false;
    }

    private ShadowMatch getShadowMatch(Method method) {

        ShadowMatch shadowMatch = null;
        try {
            shadowMatch = this.pointcutExpression.matchesMethodExecution(method);
        } catch (ReflectionWorld.ReflectionWorldException ex) {

            throw new RuntimeException("not implemented yet");
        }
        return shadowMatch;
    }

    private void checkReadyToMatch() {
        if (getExpression() == null) {
            throw new IllegalStateException("Must set property 'expression' before attempting to match");
        }
        if (this.pointcutExpression == null) {
            this.pointcutClassLoader = ClassUtils.getDefaultClassLoader();
            this.pointcutExpression = buildPointcutExpression(this.pointcutClassLoader);
        }
    }

    private PointcutExpression buildPointcutExpression(ClassLoader classLoader) {

        PointcutParser parser = PointcutParser
                .getPointcutParserSupportingSpecifiedPrimitivesAndUsingSpecifiedClassLoaderForResolution(
                        SUPPORTED_PRIMITIVES, classLoader);

        return parser.parsePointcutExpression(replaceBooleanOperators(getExpression()),
                null, new PointcutParameter[0]);
    }


    private String replaceBooleanOperators(String pcExpr) {
        String result = StringUtils.replace(pcExpr, " and ", " && ");
        result = StringUtils.replace(result, " or ", " || ");
        result = StringUtils.replace(result, " not ", " ! ");
        return result;
    }
}

```

#### PointcutTest

测试`Pointcut`。

```java
public class PointcutTest {

    @Test
    public void testPointCutTest() throws Exception {
        String expression = "execution(* com.niocoder.service.v5.*.placeOrder(..))";
        AspectJExpressionPointcut pc = new AspectJExpressionPointcut();
        pc.setExpression(expression);

        MethodMatcher mm = pc.getMethodMatcher();

        {
            Class<?> targetClass = NioCoderService.class;

            Method placeOrder = targetClass.getMethod("placeOrder");
            Assert.assertTrue(mm.matches(placeOrder));

            Method getAccountDao = targetClass.getMethod("getAccountDao");
            Assert.assertFalse(mm.matches(getAccountDao));
        }

        {
            Class<?> targetClass = com.niocoder.service.v4.NioCoderService.class;
            Method placeOrder = targetClass.getMethod("getAccountDao");
            Assert.assertFalse(mm.matches(placeOrder));
        }
    }
}
```

我们已经实现了一个简单`Pointcut`表达式，关于更多`Pointcut`可以参考[链接](https://www.cnblogs.com/liaojie970/p/7883687.html)

### 定位Method

```xml
<!-- 模拟 TransactionManager-->
    <bean id="tx" class="com.niocoder.tx.TransactionManager"/>
    <!-- aop 配置-->
    <aop:config>
        <!-- aop 核心配置 依赖tx-->
        <aop:aspect ref="tx">
            <!-- 通知配置，-->
            <aop:before pointcut-ref="placeOrder" method="start"/>
        </aop:aspect>

    </aop:config>
```

在`aop`中，我们需要根据`beanName`为`tx`，方法名称为`start`来定位到`TransactionManager.start()`方法。因此我们需要一个类，根据`targetBeanName`和`methodName`返回`Method`对象。因需要根据`beanName`返回对象，所以在此类中需要设置`BeanFactory`,并在`BeanFactory`中新增`Class<?> getType(String name)`方法。

#### BeanFactory

新增`Class<?> getType(String name)` 方法，根据`beanName`返回`Class`对象。

```java
package com.niocoder.beans.factory;

/**
 * 创建bean的实例
 *
 * @author zhenglongfei
 */
public interface BeanFactory {

    /**
     * 获取bean的实例
     *
     * @param beanId
     * @return
     */
    Object getBean(String beanId);

    /**
     * 根据bean 名称 返回 class 对象
     *
     * @param name
     * @return
     */
    Class<?> getType(String name) throws NoSuchBeanDefinitionException;
}
```

#### DefaultBeanFactory

`DefaultBeanFactory`中实现`getType`方法。

```java
public class DefaultBeanFactory extends DefaultSingletonBeanRegistry implements ConfigurableBeanFactory, BeanDefinitionRegistry {
.......
    @Override
    public Class<?> getType(String name) throws NoSuchBeanDefinitionException {
        BeanDefinition bd = this.getBeanDefinition(name);
        if (null == bd) {
            throw new NoSuchBeanDefinitionException(name);
        }
        resolveBeanClass(bd);

        return bd.getBeanClass();
    }
}
```

#### AbstractApplicationContext

因为`ApplicationContext`继承`BeanFactory`接口，所以在抽象类`AbstractApplicationContext`也需要实现`getType`方法。

```java
public abstract class AbstractApplicationContext implements ApplicationContext{
 @Override
    public Class<?> getType(String name) throws NoSuchBeanDefinitionException {
        return this.factory.getType(name);
    }
}
```

#### MethodLocatingFactory

根据`targetBeanName`和`methodName`返回`Method`对象。

```java
package com.niocoder.aop.config;

import com.niocoder.beans.BeanUtils;
import com.niocoder.beans.factory.BeanFactory;
import com.niocoder.util.StringUtils;

import java.lang.reflect.Method;

public class MethodLocatingFactory {

    private String targetBeanName;

    private String methodName;

    private Method method;

    public void setTargetBeanName(String targetBeanName) {
        this.targetBeanName = targetBeanName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }


    /**
     * 设置beanFactory 只有beanFactory才能根据bean的名称返回bean的class 对象
     * 设置时需要前置判断，beanName 和 methodName
     *
     * @param beanFactory
     */
    public void setBeanFactory(BeanFactory beanFactory) {

        if (!StringUtils.hasText(this.targetBeanName)) {
            throw new IllegalArgumentException("Property 'targetBeanName' is required");
        }
        if (!StringUtils.hasText(this.methodName)) {
            throw new IllegalArgumentException("Property 'methodName' is required");
        }

        Class<?> beanClass = beanFactory.getType(this.targetBeanName);
        if (beanClass == null) {
            throw new IllegalArgumentException("Can't determine type of bean with name '" + this.targetBeanName + "'");
        }

        // 给method 赋值
        this.method = BeanUtils.resolveSignature(this.methodName, beanClass);

        if (this.method == null) {
            throw new IllegalArgumentException("Unable to locate method [" + this.methodName +
                    "] on bean [" + this.targetBeanName + "]");
        }
    }

    /**
     * 返回Method对象
     *
     * @return
     */
    public Method getObject() {
        return this.method;
    }
}

```

#### MethodLocatingFactoryTest

测试 `MethodLocatingFactory`。

```java
public class MethodLocatingFactoryTest {

    @Test
    public void testGetMethod() throws Exception {
        DefaultBeanFactory factory = new DefaultBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(factory);
        reader.loadBeanDefinition(new ClassPathResource("bean-v5.xml"));

        MethodLocatingFactory methodLocatingFactory = new MethodLocatingFactory();
        methodLocatingFactory.setTargetBeanName("tx");
        methodLocatingFactory.setMethodName("start");
        methodLocatingFactory.setBeanFactory(factory);

        Method start = methodLocatingFactory.getObject();

        Assert.assertTrue(TransactionManager.class.equals(start.getDeclaringClass()));
        Assert.assertTrue(start.equals(TransactionManager.class.getMethod("start")));

        TransactionManager tx = (TransactionManager) factory.getBean("tx");
        start.invoke(tx);
    }
}
```

#### 代码下载

- github:[https://github.com/longfeizheng/small-spring/tree/20190215_aop_v1](https://github.com/longfeizheng/small-spring/tree/20190215_aop_v1)

## 代码下载 ##

- github:[https://github.com/longfeizheng/small-spring](https://github.com/longfeizheng/small-spring)

## 参考资料 ##

---
[从零开始造Spring](https://mp.weixin.qq.com/s/gbvdwpPtQcjyaigRBDjd-Q)
