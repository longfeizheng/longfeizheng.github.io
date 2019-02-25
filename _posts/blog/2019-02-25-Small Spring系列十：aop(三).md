---
layout: post
title: Small Spring系列十：aop (三)
categories: Spring
description: Spring
keywords: Spring
---

>山有木兮木有枝，心悦君兮君不知。

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-11.jpg](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-11.jpg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-11.jpg")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-11.jpg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-11.jpg")

## 概述 ##

到目前位置,关于`aop`的部分，我们已经完成了以下功能

1. 根据`Bean`的名称和方法名，获取`Method`对象。`MethodLocatingFactory`
2. 给定一个类的方法，判断该方法是否符合`Pointcut`的表达式。`AspectJExpressionPointcut`
3. 实现了前置,后置和异常的通知。`AspectJBeforeAdvice`,`AspectJAfterReturningAdvice`,`AspectJAfterThrowingAdvice`
4. 实现了`Advice`按次续依次执行。`ReflectiveMethodInvocation`
5. 给定一个`AopConif`，使用`Cglib`生成一个对象的代理。

接下来只剩下最后一个工作，读取`bean-v5.xml`创建`BeanDefinition`。

### 分析aop:config标签

在创建`BeanDefinition`之前我们先看一下`aop:config`的定义，之前`Bean`的定义。

```xml
<bean id = "nioCoder"
    class = "com.niocoder.service.v1.NioCoderService">
</bean>
```

现在`Bean`的定义

```xml
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
```

之前如果`Bean`有其它额外的属性，我们都是通过`PropertyValue`或`ConstructorArgument`来表示。但`aop:config`的标签该如何表达呢？

`spring`还是通过`GenericBeanDefinition`来表示`aop:config`标签,关于标签属性的映射，参考下图

```xml
<aop:pointcut id="placeOrder"
              expression="execution(* com.niocoder.service.v5.*.placeOrder(..))"/>
```

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/apo_v6.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/apo_v6.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/apo_v6.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/apo_v6.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/apo_v6.png")

```xml
<aop:before pointcut-ref="placeOrder" method="start"/>
```
[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/apo_v7.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/apo_v7.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/apo_v7.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/apo_v7.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/apo_v7.png")

通过上图的构造器类型，我们发现这与我们之前创建`AspectJBeforeAdvice`的构造器参数不匹配。

```java
public AspectJBeforeAdvice(Method adviceMethod, AspectJExpressionPointcut pointcut, Object adviceObject) {
    super(adviceMethod, pointcut, adviceObject);
}
```
因上图的第一个构造器参数中含有`methodName`,所以我们只需将修改`adviceObject`修改为`AspectInstanceFactory`。关于`AspectInstanceFactory`的类图设计如下：

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/apo_v8.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/apo_v8.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/apo_v8.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/apo_v8.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/apo_v8.png")


#### FactoryBean<T>

工厂bean,

```java
/**
 * 工厂bean
 * @author zhenglongfei
 */
public interface FactoryBean<T> {

    T getObject() throws Exception;

    Class<?> getObjectType();
}
```

#### BeanFactoryAware

有些实现类需要用到`BeanFactory`,实现该接口即可。

```java
public interface BeanFactoryAware {

    /**
     * set beanFactory
     *
     * @param beanFactory
     * @throws BeansException
     */
    void setBeanFactory(BeanFactory beanFactory) throws BeansException;
}
```

#### AspectInstanceFactory

```java
/**
 * @author zhenglongfei
 */
public class AspectInstanceFactory implements BeanFactoryAware {

    private String aspectBeanName;

    private BeanFactory beanFactory;

    public void setAspectBeanName(String aspectBeanName) {
        this.aspectBeanName = aspectBeanName;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
        if (!StringUtils.hasText(this.aspectBeanName)) {
            throw new IllegalArgumentException(" 'aspectBeanName' is required");
        }
    }

    public Object getAspectInstance() throws Exception {
        return this.beanFactory.getBean(this.aspectBeanName);
    }
}
```

#### MethodLocatingFactory

实现 `FactoryBean<Method>`和`BeanFactoryAware `


```java
public class MethodLocatingFactory implements FactoryBean<Method>, BeanFactoryAware {

}
```

#### AbstractAspectJAdvice

将`Object adviceObject`替换成`AspectInstanceFactory adviceObjectFactory`,修改`invokeAdviceMethod()`方法，对象是从`benFactory`中根据`aspectBeanName`获取。

```java
ublic abstract class AbstractAspectJAdvice implements Advice {

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

#### AspectJBeforeAdvice

因抽象方法修改，子类构造函数也需要修改,`AspectJAfterReturningAdvice`和`AspectJAfterThrowingAdvice`也需要修改

```java
ublic class AspectJBeforeAdvice extends AbstractAspectJAdvice {

    public AspectJBeforeAdvice(Method adviceMethod, AspectJExpressionPointcut pointcut, Object adviceObject) {
        super(adviceMethod, pointcut, adviceObject);
    }
......
}
```

#### AbstractV5Test

提出公工方法到一个测试类中

```java

public class AbstractV5Test {

    protected BeanFactory getBeanFactory(String configFile) {
        DefaultBeanFactory defaultBeanFactory = new DefaultBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(defaultBeanFactory);
        reader.loadBeanDefinition(new ClassPathResource(configFile));
        return defaultBeanFactory;
    }

    protected Method getAdviceMethod(String methodName) throws Exception {
        return TransactionManager.class.getMethod(methodName);
    }

    protected AspectInstanceFactory getAspectInstanceFactory(String targetBeanName) {
        AspectInstanceFactory factory = new AspectInstanceFactory();
        factory.setAspectBeanName(targetBeanName);
        return factory;
    }
}
```

#### CglibAopProxyTest

动态获取`AspectInstanceFactory`

```java
public class CglibAopProxyTest extends AbstractV5Test {

    private static AspectJBeforeAdvice beforeAdvice = null;
    private static AspectJAfterReturningAdvice afterAdvice = null;
    private AspectJExpressionPointcut pc = null;
    private BeanFactory beanFactory = null;
    private NioCoderService nioCoderService = null;
    private AspectInstanceFactory aspectInstanceFactory = null;

    @Before
    public void setUp() throws Exception {

        MessageTracker.clearMsgs();

        String expression = "execution(* com.niocoder.service.v5.*.placeOrder(..))";
        pc = new AspectJExpressionPointcut();
        pc.setExpression(expression);
        nioCoderService = new NioCoderService();

        beanFactory = this.getBeanFactory("bean-v5.xml");
        aspectInstanceFactory = this.getAspectInstanceFactory("tx");
        aspectInstanceFactory.setBeanFactory(beanFactory);

        beforeAdvice = new AspectJBeforeAdvice(
                getAdviceMethod("start"),
                pc,
                aspectInstanceFactory);

        afterAdvice = new AspectJAfterReturningAdvice(
                getAdviceMethod("commit"),
                pc,
                aspectInstanceFactory);
    }

    @Test
    public void testGetProxy() {
        AopConfig config = new AopConfigSupport();

        config.addAdvice(beforeAdvice);
        config.addAdvice(afterAdvice);
        config.setTargetObject(nioCoderService);

        CglibProxyFactory proxyFactory = new CglibProxyFactory(config);

        NioCoderService proxy = (NioCoderService) proxyFactory.getProxy();

        proxy.placeOrder();

        List<String> msgs = MessageTracker.getMsgs();

        Assert.assertEquals(3, msgs.size());
        Assert.assertEquals("start tx", msgs.get(0));
        Assert.assertEquals("place order", msgs.get(1));
        Assert.assertEquals("commit tx", msgs.get(2));
    }
}
```

#### ReflectiveMethodInvocationTest

动态获取`AspectInstanceFactory`

```java
public class ReflectiveMethodInvocationTest extends AbstractV5Test{

    private AspectJBeforeAdvice beforeAdvice = null;
    private AspectJAfterReturningAdvice afterReturningAdvice;
    private AspectJAfterThrowingAdvice afterThrowingAdvice;
    private NioCoderService nioCoderService;
    private AspectJExpressionPointcut pc = null;
    private BeanFactory beanFactory = null;
    private AspectInstanceFactory aspectInstanceFactory = null;


    @Before
    public void setUp() throws Exception {
        nioCoderService = new NioCoderService();
        String expression = "execution(* com.niocoder.service.v5.*.placeOrder(..))";
        pc = new AspectJExpressionPointcut();
        pc.setExpression(expression);

        beanFactory = this.getBeanFactory("bean-v5.xml");
        aspectInstanceFactory = this.getAspectInstanceFactory("tx");
        aspectInstanceFactory.setBeanFactory(beanFactory);

        MessageTracker.clearMsgs();
        beforeAdvice = new AspectJBeforeAdvice(TransactionManager.class.getMethod("start"), pc, aspectInstanceFactory);
        afterReturningAdvice = new AspectJAfterReturningAdvice(TransactionManager.class.getMethod("commit"), pc, aspectInstanceFactory);
        afterThrowingAdvice = new AspectJAfterThrowingAdvice(TransactionManager.class.getMethod("rollback"), pc, aspectInstanceFactory);
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

#### 代码下载

- github:[https://github.com/longfeizheng/small-spring/tree/20190218_aop_v6](https://github.com/longfeizheng/small-spring/tree/20190218_aop_v6)


### 解析aop:config标签

TODO ....

## 代码下载 ##

- github:[https://github.com/longfeizheng/small-spring](https://github.com/longfeizheng/small-spring)

## 参考资料 ##

---
[从零开始造Spring](https://mp.weixin.qq.com/s/gbvdwpPtQcjyaigRBDjd-Q)
