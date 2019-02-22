---
layout: post
title: Small Spring系列九：aop (二)
categories: Spring
description: Spring
keywords: Spring
---

>曾经沧海难为水 除却巫山不是云。

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-10.jpg](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-10.jpg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-10.jpg")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-10.jpg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-10.jpg")

## 概述 ##

在<a href="https://niocoder.com/2019/02/16/Small-Spring%E7%B3%BB%E5%88%97%E5%85%AB-aop(%E4%B8%80)/">Small Spring系列八：aop (一)</a>中,我们实现了`Pointcut`和`MethodLocatingFactory`,`Pointcut`根据给定一个类的方法判断是否符合`expression`表达式,`MethodLocatingFactory`更具`targetBeanName`和`methodName`返回一个`Method`对象。本章我们来实现`aop`的链式调用和`Cglib`的动态代理。

### op 链式调用

链式调用时需要用到 `aopalliance`依赖，在`pom.xml`中增加依赖。实现链式调用的类图如下:

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/apo_v4.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/apo_v4.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/apo_v4.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/apo_v4.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/apo_v4.png")


#### pom.xml

```xml
<dependency>
    <groupId>aopalliance</groupId>
    <artifactId>aopalliance</artifactId>
    <version>1.0</version>
</dependency>
```

#### Advice

`Advice`通知接口,内含有`getPointcut()`方法，返回`Pointcut`判断方法是否匹配。

```java
package com.niocoder.aop;

import org.aopalliance.intercept.MethodInterceptor;

/**
 * Created on 2019/2/17.
 *
 * @author zlf
 * @email i@merryyou.cn
 * @since 1.0
 */
public interface Advice extends MethodInterceptor {

    /**
     * 获取Pointcut
     * @return
     */
    Pointcut getPointcut();
}
```

#### AbstractAspectJAdvice

抽象类，抽取`AspectJBeforeAdvice`、`AspectJAfterReturningAdvice`和`AspectJAfterThrowingAdvice`的公共方法。`invokeAdviceMethod()`方法是执行通知类中的通知方法！

```java
package com.niocoder.aop.aspectj;

import com.niocoder.aop.Advice;
import com.niocoder.aop.Pointcut;

import java.lang.reflect.Method;

/**
 * 提出来一个抽象类
 * Created on 2019/2/17.
 *
 * @author zlf
 * @email i@merryyou.cn
 * @since 1.0
 */
public abstract class AbstractAspectJAdvice implements Advice {

    private Method adviceMethod;
    private AspectJExpressionPointcut pointcut;
    private Object adviceObject;

    public AbstractAspectJAdvice(Method adviceMethod, AspectJExpressionPointcut pointcut, Object adviceObject) {
        this.adviceMethod = adviceMethod;
        this.pointcut = pointcut;
        this.adviceObject = adviceObject;
    }

    @Override
    public Pointcut getPointcut() {
        return pointcut;
    }

    public Method getAdviceMethod() {
        return this.adviceMethod;
    }

    public void invokeAdviceMethod() throws Throwable {
        adviceMethod.invoke(adviceObject);
    }
}
```

#### AspectJAfterThrowingAdvice

前置通知方法，

```java
public class AspectJBeforeAdvice extends AbstractAspectJAdvice {

    public AspectJBeforeAdvice(Method adviceMethod, AspectJExpressionPointcut pointcut, Object adviceObject) {
        super(adviceMethod, pointcut, adviceObject);
    }

    /**
     * 执行通知方法，再执行被代理类方法
     *
     * @param invocation
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        this.invokeAdviceMethod();
        Object o = invocation.proceed();
        return o;
    }

}
```

#### AspectJAfterReturningAdvice

后置通知方法。

```java
public class AspectJAfterReturningAdvice extends AbstractAspectJAdvice {

    public AspectJAfterReturningAdvice(Method adviceMethod, AspectJExpressionPointcut pointcut, Object adviceObject) {
        super(adviceMethod, pointcut, adviceObject);
    }

    /**
     * 先执行被代理类方法，再执行通知方法
     *
     * @param invocation
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Object o = invocation.proceed();
        this.invokeAdviceMethod();
        return o;
    }

}

```

#### AspectJAfterThrowingAdvice

后置通知异常时操作处理，如 `rollback()`。

```java
public class AspectJAfterThrowingAdvice extends AbstractAspectJAdvice {

    public AspectJAfterThrowingAdvice(Method adviceMethod, AspectJExpressionPointcut pointcut, Object adviceObject) {
        super(adviceMethod, pointcut, adviceObject);
    }

    /**
     * 先执行被代理类方法，再执行通知方法
     *
     * @param invocation
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        try {
            return invocation.proceed();
        } catch (Throwable t) {
            invokeAdviceMethod();
            throw t;
        }
    }

}
```

#### ReflectiveMethodInvocation

给定一个对象,方法和 若干拦截器，拦截器可以依次执行。

```java

public class ReflectiveMethodInvocation implements MethodInvocation {

    private final Object targetObject; // nioCoderService;
    private final Method targetMethod; // placeOrder 方法
    private final Object[] arguments; // 方法所需要的参数，这里为空

    private final List<MethodInterceptor> interceptorList;

    private int currentInterceptorIndex = -1;

    public ReflectiveMethodInvocation(Object targetObject, Method targetMethod, Object[] arguments, List<MethodInterceptor> interceptorList) {
        this.targetObject = targetObject;
        this.targetMethod = targetMethod;
        this.arguments = arguments;
        this.interceptorList = interceptorList;
    }

    @Override
    public Method getMethod() {
        return this.targetMethod;
    }

    @Override
    public Object[] getArguments() {
        return (this.arguments != null ? this.arguments : new Object[0]);
    }

    @Override
    public Object proceed() throws Throwable {
        //所有的拦截器已经调用完成
        if (this.currentInterceptorIndex == this.interceptorList.size() - 1) {
            return invokeJoinPoint();
        }

        this.currentInterceptorIndex++;

        MethodInterceptor interceptor =
                this.interceptorList.get(this.currentInterceptorIndex);
        return interceptor.invoke(this);
    }

    private Object invokeJoinPoint() throws Throwable {

        return this.targetMethod.invoke(this.targetObject, this.arguments);
    }

    @Override
    public Object getThis() {
        return this.targetObject;
    }

    @Override
    public AccessibleObject getStaticPart() {
        return this.targetMethod;
    }
}

```

#### ReflectiveMethodInvocationTest

测试拦截器是否依次执行。

```java
public class ReflectiveMethodInvocationTest {

    private AspectJBeforeAdvice beforeAdvice = null;
    private AspectJAfterReturningAdvice afterReturningAdvice;
    private AspectJAfterThrowingAdvice afterThrowingAdvice;
    private NioCoderService nioCoderService;
    private TransactionManager tx;


    @Before
    public void setUp() throws Exception {
        nioCoderService = new NioCoderService();
        tx = new TransactionManager();
        MessageTracker.clearMsgs();
        beforeAdvice = new AspectJBeforeAdvice(TransactionManager.class.getMethod("start"), null, tx);
        afterReturningAdvice = new AspectJAfterReturningAdvice(TransactionManager.class.getMethod("commit"), null, tx);
        afterThrowingAdvice = new AspectJAfterThrowingAdvice(TransactionManager.class.getMethod("rollback"), null, tx);
    }


    @Test
    public void textMethodInvocation() throws Throwable {

        Method targetMethod = NioCoderService.class.getMethod("placeOrder");
        List<MethodInterceptor> interceptorList = new ArrayList<>();
        interceptorList.add(beforeAdvice);
        interceptorList.add(afterReturningAdvice);

        ReflectiveMethodInvocation mi = new ReflectiveMethodInvocation(nioCoderService, targetMethod, new Object[0], interceptorList);
        mi.proceed();

        List<String> msgs = MessageTracker.getMsgs();

        Assert.assertEquals(3, msgs.size());
        Assert.assertEquals("start tx", msgs.get(0));
        Assert.assertEquals("place order", msgs.get(1));
        Assert.assertEquals("commit tx", msgs.get(2));
    }

    @Test
    public void testAfterThrowing() throws Throwable{

        Method targetMethod = NioCoderService.class.getMethod("placeOrderWithException");
        List<MethodInterceptor> interceptorList = new ArrayList<>();
        interceptorList.add(beforeAdvice);
        interceptorList.add(afterThrowingAdvice);

        ReflectiveMethodInvocation mi = new ReflectiveMethodInvocation(nioCoderService, targetMethod, new Object[0], interceptorList);

        try {
            mi.proceed();
        } catch (Throwable t) {
            List<String> msgs = MessageTracker.getMsgs();

            Assert.assertEquals(2, msgs.size());
            Assert.assertEquals("start tx", msgs.get(0));
            Assert.assertEquals("rollback tx", msgs.get(1));
            return;
        }

        Assert.fail("No Exception thrown");
    }
}

```

输出：

```java
start tx
place order
commit tx
start tx
rollback tx
```

关于更详细的解析`aop`链式调用可参考：<a href="https://niocoder.com/2019/02/19/spring-aop-%E9%93%BE%E5%BC%8F%E8%B0%83%E7%94%A8/">spring aop 之链式调用</a>

#### 代码下载

- github:[https://github.com/longfeizheng/small-spring/tree/20190217_aop_v4](https://github.com/longfeizheng/small-spring/tree/20190217_aop_v4)

### cglib 动态代理

到此为止，我们已经实现`Pointcut`、`MethodLocatingFactory`和`aop`的链式调用,接下来我们通过`cglib`实现动态代理，在开始实现动态代理之前，我们先简单认识一下如何使用`cglib`实现动态代理。

#### 导入 spring-core-cglib 依赖

`spring`已经封装好的`cglib` jar.

```java
<dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-core-cglib</artifactId>
            <version>3.2.18.RELEASE</version>
            <scope>system</scope>
            <systemPath>${project.basedir}/libs/spring-core-cglib-3.2.18.RELEASE.jar</systemPath>
        </dependency>
```

#### CglibTest.testCallBack()

简单认识一下 `cglib`动态代理。

```java
public class CglibTest {

    /**
     * 所有方法都拦截!!!!!
     */
    @Test
    public void testCallBack() {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(NioCoderService.class);
        enhancer.setCallback(new TransactionInterceptor());
        NioCoderService nioCoderService = (NioCoderService) enhancer.create();
        nioCoderService.placeOrder();
//        nioCoderService.toString();
    }

    /**
     *
     */
    private class TransactionInterceptor implements MethodInterceptor {
        TransactionManager tx = new TransactionManager();

        public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
            tx.start();
            Object result = methodProxy.invokeSuper(o, objects);
            tx.commit();
            return result;
        }
    }
}
```

输出：
```java
start tx
place order
commit tx
```

注意！！！`nioCoderService`中的所有方法都被拦截。打开` nioCoderService.toString();`之后，同样也会前后输出`start tx`和 `commit tx`。

#### CglibTest.testFilter()

拦截指定的方法。

```java
public class CglibTest {
  /**
       * 配置多个拦截器
       */
      @Test
      public void testFilter() {
          Enhancer enhancer = new Enhancer();
          enhancer.setSuperclass(NioCoderService.class);
          enhancer.setInterceptDuringConstruction(false);

          // 两个拦截器，第二个默认不操作
          Callback[] callbacks = new Callback[]{new TransactionInterceptor(), NoOp.INSTANCE};

          Class<?>[] types = new Class[callbacks.length];
          for (int i = 0; i < types.length; i++) {
              types[i] = callbacks[i].getClass();
          }

          enhancer.setCallbackFilter(new ProxyCallbackFilter());
          enhancer.setCallbacks(callbacks);
          enhancer.setCallbackTypes(types);

          NioCoderService nioCoderService = (NioCoderService) enhancer.create();
          nioCoderService.placeOrder();
          System.out.println(nioCoderService.toString());
      }

      private class ProxyCallbackFilter implements CallbackFilter {
          public ProxyCallbackFilter() {
          }
          @Override
          public int accept(Method method) {
              if (method.getName().startsWith("place")) {
                  return 0; // 第一个拦截器
              } else {
                  return 1; // 第二个拦截器
              }
          }
      }
}
```

输出：
```java
start tx
place order
commit tx
```

`enhancer`设置多个拦截器,设置过滤器。实现针对指定方法的拦截。

### aop 动态获取代理实例

我们已经知道了如何使用`cglib`创建代理类,并且针对指定方法实现前后通知。接下我们实现`spring`动态创建和获取返回动态代理实例。类图如下：

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/apo_v5.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/apo_v5.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/apo_v5.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/apo_v5.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/apo_v5.png")

#### AopConfig

提供`CglibProxyFactory`需要用到的配置信息

```java
public interface AopConfig {

    /**
     * 目标类
     *
     * @return
     */
    Class<?> getTargetClass();

    /**
     * 目标对象
     *
     * @return
     */
    Object getTargetObject();

    /**
     * 是否是代理类
     *
     * @return
     */
    boolean isProxyTargetClass();

    /**
     * 接口
     *
     * @return
     */
    Class<?>[] getProxiedInterfaces();

    boolean isInterfaceProxied(Class<?> intf);

    /**
     * 获取通知列表
     *
     * @return
     */
    List<Advice> getAdvices();

    /**
     * 添加通知
     *
     * @param advice
     */
    void addAdvice(Advice advice);

    /**
     * 根据method 返回匹配method的通知
     *
     * @param method
     * @return
     */
    List<Advice> getAdvices(Method method/*,Class<?> targetClass*/);

    /**
     * 设置目标对象
     *
     * @param object
     */
    void setTargetObject(Object object);
}
```

#### AopConfigSupport

实现`AopConfig`.

```java
public class AopConfigSupport implements AopConfig {

    private boolean proxyTargetClass = false;

    Object targetObject = null;

    List<Advice> advices = new ArrayList<>();

    private List<Class> interfaces = new ArrayList<>();

    public AopConfigSupport() {
    }

    @Override
    public Class<?> getTargetClass() {
        return this.targetObject.getClass();
    }

    @Override
    public Object getTargetObject() {
        return this.targetObject;
    }

    @Override
    public boolean isProxyTargetClass() {
        return proxyTargetClass;
    }

    @Override
    public Class<?>[] getProxiedInterfaces() {
        return this.interfaces.toArray(new Class[this.interfaces.size()]);
    }

    @Override
    public boolean isInterfaceProxied(Class<?> intf) {
        for (Class proxyIntf : this.interfaces) {
            if (intf.isAssignableFrom(proxyIntf)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<Advice> getAdvices() {
        return this.advices;
    }

    @Override
    public void addAdvice(Advice advice) {
        this.advices.add(advice);
    }

    @Override
    public List<Advice> getAdvices(Method method) {
        List<Advice> result = new ArrayList<>();
        for (Advice advice : this.getAdvices()) {
            Pointcut pc = advice.getPointcut();
            if (pc.getMethodMatcher().matches(method)) {
                result.add(advice);
            }
        }
        return result;
    }

    public void setProxyTargetClass(boolean proxyTargetClass) {
        this.proxyTargetClass = proxyTargetClass;
    }

    @Override
    public void setTargetObject(Object object) {
        this.targetObject = object;
    }

    public void addInterface(Class<?> intf) {
        Assert.notNull(intf, "Interface must not be null");
        if (!intf.isInterface()) {
            throw new IllegalArgumentException("[" + intf.getName() + "] is not an interface");
        }
        if (!this.interfaces.contains(intf)) {
            this.interfaces.add(intf);

        }
    }
}
```

#### AopProxyFactory

获取代理类的工厂。

```java
public interface AopProxyFactory {

    /**
     * 获取代理类
     *
     * @return
     */
    Object getProxy();

    /**
     * 根据类加载器获取代理类
     *
     * @param classLoader
     * @return
     */
    Object getProxy(ClassLoader classLoader);
}
```

#### CglibProxyFactory

`cglib`实现的代理工厂

```java
public class CglibProxyFactory implements AopProxyFactory {
    // Constants for CGLIB callback array indices
    private static final int AOP_PROXY = 0;
    private static final int INVOKE_TARGET = 1;
    private static final int NO_OVERRIDE = 2;
    private static final int DISPATCH_TARGET = 3;
    private static final int DISPATCH_ADVISED = 4;
    private static final int INVOKE_EQUALS = 5;
    private static final int INVOKE_HASHCODE = 6;


    /**
     * Logger available to subclasses; static to optimize serialization
     */
    protected static final Log logger = LogFactory.getLog(CglibProxyFactory.class);


    protected final AopConfig config;

    private Object[] constructorArgs;

    private Class<?>[] constructorArgTypes;


    public CglibProxyFactory(AopConfig config) throws AopConfigException {
        Assert.notNull(config, "AdvisedSupport must not be null");
        if (config.getAdvices().size() == 0 /*&& config.getTargetSource() == AdvisedSupport.EMPTY_TARGET_SOURCE*/) {
            throw new AopConfigException("No advisors and no TargetSource specified");
        }
        this.config = config;

    }

    @Override
    public Object getProxy() {
        return getProxy(null);
    }

    @Override
    public Object getProxy(ClassLoader classLoader) {
        if (logger.isDebugEnabled()) {
            logger.debug("Creating CGLIB proxy: target source is " + this.config.getTargetClass());
        }

        try {
            Class<?> rootClass = this.config.getTargetClass();

            // Configure CGLIB Enhancer...
            Enhancer enhancer = new Enhancer();
            if (classLoader != null) {
                enhancer.setClassLoader(classLoader);
            }
            enhancer.setSuperclass(rootClass);

            enhancer.setNamingPolicy(SpringNamingPolicy.INSTANCE); //"BySpringCGLIB"
            enhancer.setInterceptDuringConstruction(false);

            Callback[] callbacks = getCallbacks(rootClass);
            Class<?>[] types = new Class<?>[callbacks.length];
            for (int x = 0; x < types.length; x++) {
                types[x] = callbacks[x].getClass();
            }

            enhancer.setCallbackFilter(new ProxyCallbackFilter(this.config));
            enhancer.setCallbackTypes(types);
            enhancer.setCallbacks(callbacks);

            // Generate the proxy class and create a proxy instance.
            Object proxy = enhancer.create();

            return proxy;
        } catch (CodeGenerationException ex) {
            throw new AopConfigException("Could not generate CGLIB subclass of class [" +
                    this.config.getTargetClass() + "]: " +
                    "Common causes of this problem include using a final class or a non-visible class",
                    ex);
        } catch (IllegalArgumentException ex) {
            throw new AopConfigException("Could not generate CGLIB subclass of class [" +
                    this.config.getTargetClass() + "]: " +
                    "Common causes of this problem include using a final class or a non-visible class",
                    ex);
        } catch (Exception ex) {
            // TargetSource.getTarget() failed
            throw new AopConfigException("Unexpected AOP exception", ex);
        }
    }

    private Callback[] getCallbacks(Class<?> rootClass) throws Exception {

        Callback aopInterceptor = new DynamicAdvisedInterceptor(this.config);

        Callback[] callbacks = new Callback[]{
                aopInterceptor,  // AOP_PROXY for normal advice
                /*targetInterceptor,  // INVOKE_TARGET invoke target without considering advice, if optimized
                new SerializableNoOp(),  // NO_OVERRIDE  no override for methods mapped to this
                targetDispatcher,        //DISPATCH_TARGET
                this.advisedDispatcher,  //DISPATCH_ADVISED
                new EqualsInterceptor(this.advised),
                new HashCodeInterceptor(this.advised)*/
        };

        return callbacks;
    }


    /**
     * General purpose AOP callback. Used when the target is dynamic or when the
     * proxy is not frozen.
     */
    private static class DynamicAdvisedInterceptor implements MethodInterceptor, Serializable {

        private final AopConfig config;

        public DynamicAdvisedInterceptor(AopConfig advised) {
            this.config = advised;
        }

        @Override
        public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {


            Object target = this.config.getTargetObject();


            List<Advice> chain = this.config.getAdvices(method/*, targetClass*/);
            Object retVal;
            // 如果没有配置 advice
            if (chain.isEmpty() && Modifier.isPublic(method.getModifiers())) {
                retVal = methodProxy.invoke(target, args);
            } else {
                List<org.aopalliance.intercept.MethodInterceptor> interceptors =
                        new ArrayList<org.aopalliance.intercept.MethodInterceptor>();

                interceptors.addAll(chain);
                // 配置advice
                retVal = new ReflectiveMethodInvocation(target, method, args, interceptors).proceed();
            }
            //retVal = processReturnType(proxy, target, method, retVal);
            return retVal;

        }
    }

    /**
     * CallbackFilter to assign Callbacks to methods.
     */
    private static class ProxyCallbackFilter implements CallbackFilter {
        private final AopConfig config;

        public ProxyCallbackFilter(AopConfig config) {
            this.config = config;

        }

        @Override
        public int accept(Method method) {
            // 注意，这里做了简化
            return AOP_PROXY;
        }

    }
}
```

#### CglibAopProxyTest

测试`cglib` 动态代理。

```java
public class CglibAopProxyTest {

    private static AspectJBeforeAdvice beforeAdvice = null;
    private static AspectJAfterReturningAdvice afterAdvice = null;
    private AspectJExpressionPointcut pc = null;
    private NioCoderService nioCoderService = null;
    private TransactionManager tx;

    @Before
    public void setUp() throws Exception {
        nioCoderService = new NioCoderService();
        tx = new TransactionManager();
        String expression = "execution(* com.niocoder.service.v5.*.placeOrder(..))";
        pc = new AspectJExpressionPointcut();
        pc.setExpression(expression);

        beforeAdvice = new AspectJBeforeAdvice(TransactionManager.class.getMethod("start"), pc, tx);

        afterAdvice = new AspectJAfterReturningAdvice(TransactionManager.class.getMethod("commit"), pc, tx);
    }

    @Test
    public void testGetProxy(){
        AopConfig config = new AopConfigSupport();

        config.addAdvice(beforeAdvice);
        config.addAdvice(afterAdvice);
        config.setTargetObject(new NioCoderService());

        CglibProxyFactory proxyFactory = new CglibProxyFactory(config);

        NioCoderService proxy = (NioCoderService)proxyFactory.getProxy();

        proxy.placeOrder();

        List<String> msgs = MessageTracker.getMsgs();

        Assert.assertEquals(3, msgs.size());
        Assert.assertEquals("start tx", msgs.get(0));
        Assert.assertEquals("place order", msgs.get(1));
        Assert.assertEquals("commit tx", msgs.get(2));
    }
}
```

#### 代码下载

- github:[https://github.com/longfeizheng/small-spring/tree/20190217_aop_v5](https://github.com/longfeizheng/small-spring/tree/20190217_aop_v5)

## 代码下载 ##

- github:[https://github.com/longfeizheng/small-spring](https://github.com/longfeizheng/small-spring)

## 参考资料 ##

---
[从零开始造Spring](https://mp.weixin.qq.com/s/gbvdwpPtQcjyaigRBDjd-Q)
