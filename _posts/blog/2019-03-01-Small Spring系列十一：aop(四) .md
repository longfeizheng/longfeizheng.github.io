---
layout: post
title: Small Spring系列十一：aop (四)
categories: Spring
description: Spring
keywords: Spring
---

>回眸一笑百媚生，六宫粉黛无颜色。

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-12.jpg](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-12.jpg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-12.jpg")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-12.jpg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-12.jpg")

## 概述 ##

在前四篇,我们已经实现了使用`Cglib`实现了`aop`动态代理。但是在`spring`中如果代理对象实现了接口，则默认使用`jdk`动态代理，也可以通过配置强制使用`cglib`代理。本篇，我们使用`jdk`动态代理来完善`aop`

### 准备工作

#### INioCoderService

新增接口类,因为使用`jdk`动态代理，代理对象必须实现接口。

```java
package com.niocoder.service.v6;

/**
 * 测试 JDK 动态代理
 */
public interface INioCoderService {

    void placeOrder();
}
```

#### NioCoderService

实现`INioCoderService`接口。

```java
@Component(value = "nioCoder")
public class NioCoderService implements INioCoderService {

    public NioCoderService() {

    }


    public void placeOrder() {
        System.out.println("place order");
        MessageTracker.addMsg("place order");
    }

    public void placeOrderV2() {
        System.out.println("no interception");
    }
}

```

#### bean-v6.XML

设置`Pointcut`为`v6`包下面的`placeOrder`方法。

```XML
<?xml version="1.0" encoding="UTF-8"?>
<!-- 增加namespace-->
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:aop="http://www.springframework.org/schema/aop">

    <!-- 扫描哪个包下面的文件 -->
    <context:component-scan base-package="com.niocoder.service.v6">

    </context:component-scan>

    <!-- 模拟 TransactionManager-->
    <bean id="tx" class="com.niocoder.tx.TransactionManager"/>

    <!-- aop 配置-->
    <aop:config>
        <!-- aop 核心配置 依赖tx-->
        <aop:aspect ref="tx">
            <!-- 切入点配置 包下面的placeOrder 方法-->
            <aop:pointcut id="placeOrder"
                          expression="execution(* com.niocoder.service.v6.*.placeOrder(..))"/>
            <!-- 通知配置，-->
            <aop:before pointcut-ref="placeOrder" method="start"/>
            <aop:after-returning pointcut-ref="placeOrder" method="commit"/>
            <aop:after-throwing pointcut-ref="placeOrder" method="rollback"/>
        </aop:aspect>

    </aop:config>
</beans>
```

### 创建jdk代理工厂

根据类图[aop 动态获取代理实例](https://niocoder.com/2019/02/22/Small-Spring%E7%B3%BB%E5%88%97%E4%B9%9D-aop(%E4%BA%8C)/#aop-%E5%8A%A8%E6%80%81%E8%8E%B7%E5%8F%96%E4%BB%A3%E7%90%86%E5%AE%9E%E4%BE%8B) 创建`JdkAopProxyFactory`代理工厂。

#### JdkAopProxyFactory

```java
package com.niocoder.aop.framework;

import com.niocoder.aop.Advice;
import com.niocoder.util.Assert;
import com.niocoder.util.ClassUtils;
import lombok.extern.java.Log;
import org.aopalliance.intercept.MethodInterceptor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhenglongfei
 */
@Log
public class JdkAopProxyFactory implements AopProxyFactory, InvocationHandler {

    private final AopConfig config;

    public JdkAopProxyFactory(AopConfig config) throws AopConfigException {
        Assert.notNull(config, "AdvisedSupport must not be null");
        if (config.getAdvices().size() == 0) {
            throw new AopConfigException("No advices specified");
        }
        this.config = config;
    }

    @Override
    public Object getProxy() {
        return getProxy(ClassUtils.getDefaultClassLoader());
    }

    @Override
    public Object getProxy(ClassLoader classLoader) {

        log.info("Creating JDK dynamic proxy: target source is " + this.config.getTargetObject());

        Class<?>[] proxiedInterfaces = config.getProxiedInterfaces();

        return Proxy.newProxyInstance(classLoader, proxiedInterfaces, this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        Object target = this.config.getTargetObject();
        Object retVal;
        List<Advice> chain = this.config.getAdvices(method);
        if (chain.isEmpty()) {
            retVal = method.invoke(target, args);
        } else {
            List<MethodInterceptor> interceptors = new ArrayList<MethodInterceptor>();
            interceptors.addAll(chain);
            // We need to create a method invocation...
            retVal = new ReflectiveMethodInvocation(target, method, args, interceptors).proceed();
        }

        return retVal;
    }
}

```

#### AspectJAutoProxyCreator

在`AspectJAutoProxyCreator`的`createProxy`方法中添加`JDK`代理支持。

```java
public class AspectJAutoProxyCreator implements BeanPostProcessor {
  ......
  protected Object createProxy(List<Advice> advices, Object bean) {
       AopConfigSupport config = new AopConfigSupport();
       for (Advice advice : advices) {
           config.addAdvice(advice);
       }

       Set<Class> targetInterfaces = ClassUtils.getAllInterfacesForClassAsSet(bean.getClass());
       for (Class<?> targetInterface : targetInterfaces) {
           config.addInterface(targetInterface);
       }

       config.setTargetObject(bean);

       AopProxyFactory proxyFactory = null;
       if (config.getProxiedInterfaces().length == 0) {
           proxyFactory = new CglibProxyFactory(config);
       } else {
           // JDK 代理
           proxyFactory = new JdkAopProxyFactory(config);
       }

       return proxyFactory.getProxy();
   }
  ......
}
```

#### ApplicationContextTest

测试`jdk`动态代理。

```java
public class ApplicationContextTest {
    @Before
    public void setUp() {
        MessageTracker.clearMsgs();
    }

    @Test
    public void testGetBeanProperty() {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("bean-v6.xml");
        INioCoderService nioCoderService = (INioCoderService) ctx.getBean("nioCoder");

        nioCoderService.placeOrder();

        List<String> msgs = MessageTracker.getMsgs();

        Assert.assertEquals(3, msgs.size());
        Assert.assertEquals("start tx", msgs.get(0));
        Assert.assertEquals("place order", msgs.get(1));
        Assert.assertEquals("commit tx", msgs.get(2));
    }
}
```

输出：

```java
start tx
place order
commit tx
```

#### 代码下载

- github:[https://github.com/longfeizheng/small-spring/tree/20190220_aop_v8](https://github.com/longfeizheng/small-spring/tree/20190220_aop_v8)


## 代码下载 ##

- github:[https://github.com/longfeizheng/small-spring](https://github.com/longfeizheng/small-spring)

## 参考资料 ##

---
[从零开始造Spring](https://mp.weixin.qq.com/s/gbvdwpPtQcjyaigRBDjd-Q)
