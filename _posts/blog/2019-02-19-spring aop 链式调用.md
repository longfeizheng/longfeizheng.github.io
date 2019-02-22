---
layout: post
title: spring aop 之链式调用
categories: Spring
description: Spring
keywords: Spring
---

>关关雎鸠，在河之洲。窈窕淑女，君子好逑。

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-09.jpg](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-09.jpg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-09.jpg")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-09.jpg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-09.jpg")

## 概述 ##

`AOP`（`Aspect Orient Programming`），我们一般称为面向方面（切面）编程，作为面向对象的一种补充，用于处理系统中分布于各个模块的横切关注点，比如事务管理、日志、缓存等等。 `Spring` `AOP`采用的是动态代理，在运行期间对业务方法进行增强，所以不会生成新类，`Spring` `AOP`提供了对`JDK`动态代理的支持以及CGLib的支持。本章我们不关注`aop`代理类的实现，我简单实现一个指定次序的链式调用。

### 实现链式调用的

`MethodInterceptor`定义拦截器链,`MethodInvocation` 递归进入下一个拦截器链中。类图如下：

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/aop_v2.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/aop_v2.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/aop_v2.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/aop_v2.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/aop_v2.png")


#### MethodInterceptor

```java
public interface MethodInterceptor {

    Object invoke(MethodInvocation invocation) throws Throwable;
}
```

#### MethodInvocation

```java
public interface MethodInvocation {

    Object proceed() throws Throwable;
}
```

#### AbstractAspectJAdvice

抽象类,实现`MethodInterceptor`

```java
public abstract class AbstractAspectJAdvice implements MethodInterceptor{

    private Method adviceMethod;

    private Object adviceObject;

    public AbstractAspectJAdvice(Method adviceMethod, Object adviceObject) {
        this.adviceMethod = adviceMethod;
        this.adviceObject = adviceObject;
    }

    public Method getAdviceMethod() {
        return this.adviceMethod;
    }

    public void invokeAdviceMethod() throws Throwable {
        adviceMethod.invoke(adviceObject);
    }
}
```

#### AspectJBeforeAdvice

前置通知

```java
public class AspectJBeforeAdvice extends AbstractAspectJAdvice {

    public AspectJBeforeAdvice(Method method, Object adviceObject) {
        super(method, adviceObject);
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable{
        this.invokeAdviceMethod();
        Object o = invocation.proceed();
        return o;
    }
}
```

#### AspectJAfterReturningAdvice

后置通知

```java
public class AspectJAfterReturningAdvice extends AbstractAspectJAdvice {

    public AspectJAfterReturningAdvice(Method method, Object adviceObject) {
        super(method, adviceObject);
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable{
        Object o = invocation.proceed();
        this.invokeAdviceMethod();
        return o;
    }
}

```

#### ReflectiveMethodInvocation

实现`MethodInvocation`，`proceed()`方法递归实现链式调用。

```java
public class ReflectiveMethodInvocation implements MethodInvocation {

    private final Object targetObject;

    private final Method targetMethod;

    private final List<MethodInterceptor> interceptorList;

    private int currentInterceptorIndex = -1;

    public ReflectiveMethodInvocation(Object targetObject, Method targetMethod, List<MethodInterceptor> interceptorList) {
        this.targetObject = targetObject;
        this.targetMethod = targetMethod;
        this.interceptorList = interceptorList;
    }

    @Override
    public Object proceed() throws Throwable {

        if (this.currentInterceptorIndex == this.interceptorList.size() - 1) {
            return invokeJoinPoint();
        }

        this.currentInterceptorIndex++;

        MethodInterceptor interceptor =
                this.interceptorList.get(this.currentInterceptorIndex);
        return interceptor.invoke(this);
    }

    private Object invokeJoinPoint() throws Throwable {

        return this.targetMethod.invoke(this.targetObject);
    }
}

```

#### NioCoderService

模拟`service`类

```java
public class NioCoderService {

    public void testAop() {
        System.out.println("http://niocoder.com/");
    }
}

```

#### TransactionManager

模拟通知类

```java
public class TransactionManager {
    public void start() {
        System.out.println("start tx");
    }

    public void commit() {
        System.out.println("commit tx");
    }

    public void rollback() {
        System.out.println("rollback tx");
    }

}

```

#### ReflectiveMethodInvocationTest

##### beforeAdvice->afterReturningAdvice

测试类，测试通知

```java
public class ReflectiveMethodInvocationTest {

    private AspectJBeforeAdvice beforeAdvice = null;

    private AspectJAfterReturningAdvice afterReturningAdvice = null;

    private NioCoderService nioCoderService;

    private TransactionManager tx;

    public void setUp() throws Exception {
        nioCoderService = new NioCoderService();
        tx = new TransactionManager();
        beforeAdvice = new AspectJBeforeAdvice(TransactionManager.class.getMethod("start"), tx);
        afterReturningAdvice = new AspectJAfterReturningAdvice(TransactionManager.class.getMethod("commit"), tx);
    }

    public void testMethodInvocation() throws Throwable {
        Method method = NioCoderService.class.getMethod("testAop");
        List<MethodInterceptor> interceptorList = new ArrayList<>();
        interceptorList.add(beforeAdvice);
        interceptorList.add(afterReturningAdvice);        

        ReflectiveMethodInvocation mi = new ReflectiveMethodInvocation(nioCoderService, method, interceptorList);

        mi.proceed();
    }


    public static void main(String[] args) throws Throwable {
        ReflectiveMethodInvocationTest reflectiveMethodInvocationTest = new ReflectiveMethodInvocationTest();
        reflectiveMethodInvocationTest.setUp();
        reflectiveMethodInvocationTest.testMethodInvocation();
    }
}
```

输出:

```java
start tx
http://niocoder.com/
commit tx
```

##### 时序图 beforeAdvice->afterReturningAdvice

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/aop_v3.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/aop_v3.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/aop_v3.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/aop_v3.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/aop_v3.png")


[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/aop_v3_1.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/aop_v3_1.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/aop_v3_1.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/aop_v3_1.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/aop_v3_1.png")

##### afterReturningAdvice->beforeAdvice

修改`interceptorList`的顺序

```java
  public void testMethodInvocation() throws Throwable {
        Method method = NioCoderService.class.getMethod("testAop");
        List<MethodInterceptor> interceptorList = new ArrayList<>();
        interceptorList.add(afterReturningAdvice);
		 interceptorList.add(beforeAdvice);

        ReflectiveMethodInvocation mi = new ReflectiveMethodInvocation(nioCoderService, method, interceptorList);

        mi.proceed();
    }
```

输出:

```java
start tx
http://niocoder.com/
commit tx
```

##### 时序图 afterReturningAdvice->beforeAdvice

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/aop_v3_2.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/aop_v3_2.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/aop_v3_2.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/aop_v3_2.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/aop_v3_2.png")


[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/aop_v3_3.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/aop_v3_3.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/aop_v3_3.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/aop_v3_3.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/aop_v3_3.png")



#### 代码下载

- github:[https://github.com/longfeizheng/data-structure-java/blob/master/src/main/java/cn/merryyou/aop](https://github.com/longfeizheng/data-structure-java/blob/master/src/main/java/cn/merryyou/aop)

## 代码下载 ##

- github:[https://github.com/longfeizheng/data-structure-java](https://github.com/longfeizheng/data-structure-java)
