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

解析`aop`标签

#### XmlBeanDefinitionReader

添加解析`aop`的 `namespace`和分支。

```java
public class XmlBeanDefinitionReader {
    ......
    public static final String AOP_NAMESPACE_URI = "http://www.springframework.org/schema/aop";
    
    ......
    public void loadBeanDefinition(Resource resource) {
        try (InputStream is = resource.getInputStream()) {
            SAXReader reader = new SAXReader();
            Document doc = reader.read(is);

            Element root = doc.getRootElement();
            Iterator<Element> elementIterator = root.elementIterator();
            while (elementIterator.hasNext()) {
                Element ele = elementIterator.next();
                String namespaceUri = ele.getNamespaceURI();
                if (this.isDefaultNamespace(namespaceUri)) {
                    // 解析xml中定义的bean
                    parseDefaultElement(ele);
                } else if (this.isContextNamespace(namespaceUri)) {
                    // 解析xml中定义的 扫描bean 例如<context:component-scan>
                    parseComponentElement(ele);
                } else if (this.isAOPNamespace(namespaceUri)) {
                    // 解析xml中定义的合成bean <aop:config> 标签
                    parseAOPElement(ele);
                }
            }
        } catch (Exception e) {
            throw new BeanDefinitionStoreException("IOException parsing XML document", e);
        }
    }
    ......
    
    /**
     * 解析aop 合成bean
     *
     * @param ele
     */
    private void parseAOPElement(Element ele) {
        ConfigBeanDefinitionParser parser = new ConfigBeanDefinitionParser();
        parser.parse(ele, this.registry);
    }
}
```

#### BeanDefinition

增加`isSynthetic()`方法，表示该`bean`是合成的`bean`

```java
public interface BeanDefinition {
    。。。。。。
    
    /**
     * 是否是合成的 bean
     *
     * @return
     */
    boolean isSynthetic();
}
```

#### GenericBeanDefinition

实现`isSynthetic()`方法。

```java
public class GenericBeanDefinition implements BeanDefinition {
    ......
    /**
     * 表明这个bean 的定义是否是合成bean
     */
    private boolean isSynthetic = false;
    ......
    
    @Override
    public boolean isSynthetic() {
        return isSynthetic;
    }

    public void setSynthetic(boolean synthetic) {
        isSynthetic = synthetic;
    }
}
```

#### ConfigBeanDefinitionParser

真正解析`aop`标签的工具类
```java
/**
 * 解析xml中aop配置的parser
 *
 * @author zhenglongfei
 */
public class ConfigBeanDefinitionParser {

    /**
     * 定义一系列解析aop需要用的标签
     */

    private static final String ASPECT = "aspect";
    private static final String EXPRESSION = "expression";
    private static final String ID = "id";
    private static final String POINTCUT = "pointcut";
    private static final String POINTCUT_REF = "pointcut-ref";
    private static final String REF = "ref";
    private static final String BEFORE = "before";
    private static final String AFTER = "after";
    private static final String AFTER_RETURNING_ELEMENT = "after-returning";
    private static final String AFTER_THROWING_ELEMENT = "after-throwing";
    private static final String AROUND = "around";
    private static final String ASPECT_NAME_PROPERTY = "aspectName";

    /**
     * 解析aop:config 标签
     *
     * @param element
     * @param registry
     */
    public void parse(Element element, BeanDefinitionRegistry registry) {
        List<Element> childElts = element.elements();
        for (Element elt : childElts) {
            String localName = elt.getName();
            //<aop:aspect ref="tx">
            if (ASPECT.equals(localName)) {
                parseAspect(elt, registry);
            }
        }
    }

    private void parseAspect(Element aspectElement, BeanDefinitionRegistry registry) {
        String aspectId = aspectElement.attributeValue(ID);
        String aspectName = aspectElement.attributeValue(REF);

        List<BeanDefinition> beanDefinitions = new ArrayList<>();
        List<RuntimeBeanReference> beanReferences = new ArrayList<>();

        List<Element> eleList = aspectElement.elements();
        boolean adviceFoundAlready = false;
        for (int i = 0; i < eleList.size(); i++) {
            Element ele = eleList.get(i);
            if (isAdviceNode(ele)) {
                if (!adviceFoundAlready) {
                    if (!StringUtils.hasText(aspectName)) {
                        return;
                    }
                    beanReferences.add(new RuntimeBeanReference(aspectName));
                }
                GenericBeanDefinition advisorDefinition = parseAdvice(
                        aspectName, i, aspectElement, ele, registry, beanDefinitions, beanReferences);
                beanDefinitions.add(advisorDefinition);
            }
        }

        List<Element> pointcuts = aspectElement.elements(POINTCUT);
        for (Element pointcutElement : pointcuts) {
            parsePointcut(pointcutElement, registry);
        }
    }

    private GenericBeanDefinition parseAdvice(String aspectName, int order, Element aspectElement, Element adviceElement, BeanDefinitionRegistry registry, List<BeanDefinition> beanDefinitions, List<RuntimeBeanReference> beanReferences) {
        GenericBeanDefinition methodDefinition = new GenericBeanDefinition(MethodLocatingFactory.class);
        methodDefinition.getPropertyValues().add(new PropertyValue("targetBeanName", aspectName));
        methodDefinition.getPropertyValues().add(new PropertyValue("methodName", adviceElement.attributeValue("method")));
        methodDefinition.setSynthetic(true);

        // create instance factory definition
        GenericBeanDefinition aspectFactoryDef =
                new GenericBeanDefinition(AspectInstanceFactory.class);
        aspectFactoryDef.getPropertyValues().add(new PropertyValue("aspectBeanName", aspectName));
        aspectFactoryDef.setSynthetic(true);

        // register the pointcut
        GenericBeanDefinition adviceDef = createAdviceDefinition(
                adviceElement, registry, aspectName, order, methodDefinition, aspectFactoryDef,
                beanDefinitions, beanReferences);

        adviceDef.setSynthetic(true);


        // register the final advisor
        BeanDefinitionReaderUtils.registerWithGeneratedName(adviceDef, registry);

        return adviceDef;
    }

    private GenericBeanDefinition createAdviceDefinition(
            Element adviceElement, BeanDefinitionRegistry registry, String aspectName, int order,
            GenericBeanDefinition methodDef, GenericBeanDefinition aspectFactoryDef,
            List<BeanDefinition> beanDefinitions, List<RuntimeBeanReference> beanReferences) {

        GenericBeanDefinition adviceDefinition = new GenericBeanDefinition(getAdviceClass(adviceElement));
        adviceDefinition.getPropertyValues().add(new PropertyValue(ASPECT_NAME_PROPERTY, aspectName));


        ConstructorArgument cav = adviceDefinition.getConstructorArgument();
        cav.addArgumentValue(methodDef);

        Object pointcut = parsePointcutProperty(adviceElement);
        if (pointcut instanceof BeanDefinition) {
            cav.addArgumentValue(pointcut);

            beanDefinitions.add((BeanDefinition) pointcut);
        } else if (pointcut instanceof String) {
            RuntimeBeanReference pointcutRef = new RuntimeBeanReference((String) pointcut);
            cav.addArgumentValue(pointcutRef);
            beanReferences.add(pointcutRef);
        }
        cav.addArgumentValue(aspectFactoryDef);

        return adviceDefinition;
    }

    private Object parsePointcutProperty(Element element/*, ParserContext parserContext*/) {
        if ((element.attribute(POINTCUT) == null) && (element.attribute(POINTCUT_REF) == null)) {
			/*parserContext.getReaderContext().error(
					"Cannot define both 'pointcut' and 'pointcut-ref' on <advisor> tag.",
					element, this.parseState.snapshot());*/
            return null;
        } else if (element.attribute(POINTCUT) != null) {
            // Create a pointcut for the anonymous pc and register it.
            String expression = element.attributeValue(POINTCUT);
            GenericBeanDefinition pointcutDefinition = createPointcutDefinition(expression);
            //pointcutDefinition.setSource(parserContext.extractSource(element));
            return pointcutDefinition;
        } else if (element.attribute(POINTCUT_REF) != null) {
            String pointcutRef = element.attributeValue(POINTCUT_REF);
            if (!StringUtils.hasText(pointcutRef)) {
				/*parserContext.getReaderContext().error(
						"'pointcut-ref' attribute contains empty value.", element, this.parseState.snapshot());*/
                return null;
            }
            return pointcutRef;
        } else {/*
			parserContext.getReaderContext().error(
					"Must define one of 'pointcut' or 'pointcut-ref' on <advisor> tag.",
					element, this.parseState.snapshot());*/
            return null;
        }
    }

    protected GenericBeanDefinition createPointcutDefinition(String expression) {
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition(AspectJExpressionPointcut.class);
        beanDefinition.setScope(BeanDefinition.SCOPE_PROTOTYPE);
        beanDefinition.setSynthetic(true);
        beanDefinition.getPropertyValues().add(new PropertyValue(EXPRESSION, expression));
        return beanDefinition;
    }

    private Class<?> getAdviceClass(Element adviceElement) {
        String elementName = adviceElement.getName();
        if (BEFORE.equals(elementName)) {
            return AspectJBeforeAdvice.class;
        }
		/*else if (AFTER.equals(elementName)) {
			return AspectJAfterAdvice.class;
		}*/
        else if (AFTER_RETURNING_ELEMENT.equals(elementName)) {
            return AspectJAfterReturningAdvice.class;
        } else if (AFTER_THROWING_ELEMENT.equals(elementName)) {
            return AspectJAfterThrowingAdvice.class;
        }
		/*else if (AROUND.equals(elementName)) {
			return AspectJAroundAdvice.class;
		}*/
        else {
            throw new IllegalArgumentException("Unknown advice kind [" + elementName + "].");
        }
    }

    private boolean isAdviceNode(Element ele) {

        String name = ele.getName();
        return (BEFORE.equals(name) || AFTER.equals(name) || AFTER_RETURNING_ELEMENT.equals(name) ||
                AFTER_THROWING_ELEMENT.equals(name) || AROUND.equals(name));

    }

    private GenericBeanDefinition parsePointcut(Element pointcutElement, BeanDefinitionRegistry registry) {
        String id = pointcutElement.attributeValue(ID);
        String expression = pointcutElement.attributeValue(EXPRESSION);

        GenericBeanDefinition pointcutDefinition = null;
        pointcutDefinition = createPointcutDefinition(expression);

        String pointcutBeanName = id;
        if (StringUtils.hasText(pointcutBeanName)) {
            registry.registerBeanDefinition(pointcutBeanName, pointcutDefinition);
        } else {
            BeanDefinitionReaderUtils.registerWithGeneratedName(pointcutDefinition, registry);
        }
        return pointcutDefinition;
    }
}

```

#### BeanDefinitionValueResolver

`resolveValueIfNecessary`添加对`BeanDefinition`的判断

```java
public class BeanDefinitionValueResolver {

    private final AbstractBeanFactory factory;

    public BeanDefinitionValueResolver(AbstractBeanFactory factory) {
        this.factory = factory;
    }

    public Object resolveValueIfNecessary(Object value) {
        if (value instanceof RuntimeBeanReference) {
            RuntimeBeanReference ref = (RuntimeBeanReference) value;
            String refName = ref.getBeanName();
            Object bean = this.factory.getBean(refName);
            return bean;
        } else if (value instanceof TypedStringValue) {
            return ((TypedStringValue) value).getValue();
        } else if (value instanceof BeanDefinition) {
            BeanDefinition bd = (BeanDefinition) value;

            String innerBeanName = "(inner bean)" + bd.getBeanClassName() + "#" +
                    Integer.toHexString(System.identityHashCode(bd));

            return resolveInnerBean(innerBeanName, bd);
        } else {
            return value;
        }
    }

    private Object resolveInnerBean(String innerBeanName, BeanDefinition innerBd) {

        try {

            Object innerBean = this.factory.createBean(innerBd);

            if (innerBean instanceof FactoryBean) {
                try {
                    return ((FactoryBean<?>) innerBean).getObject();
                } catch (Exception e) {
                    throw new BeanCreationException(innerBeanName, "FactoryBean threw exception on object creation", e);
                }
            } else {
                return innerBean;
            }
        } catch (BeansException ex) {
            throw new BeanCreationException(
                    innerBeanName,
                    "Cannot create inner bean '" + innerBeanName + "' " +
                            (innerBd != null && innerBd.getBeanClassName() != null ? "of type [" + innerBd.getBeanClassName() + "] " : "")
                    , ex);
        }
    }
}

```

#### BeanDefinitionTest

测试是否解析所有的`aop`标签

```java
public class BeanDefinitionTest extends AbstractV5Test {

    @Test
    public void testAOPBean() throws Exception {

        DefaultBeanFactory factory = (DefaultBeanFactory) this.getBeanFactory("bean-v5.xml");

        // 检查名称为 tx 的 Bean 是否生成
        {
            BeanDefinition bd = factory.getBeanDefinition("tx");
            Assert.assertTrue(bd.getBeanClassName().equalsIgnoreCase(TransactionManager.class.getName()));
        }

        // 检查placeOrder 是否正确生成
        {
            BeanDefinition bd = factory.getBeanDefinition("placeOrder");
            // 这个BeanDefinition 是合成的bean
            Assert.assertTrue(bd.isSynthetic());
            Assert.assertTrue(bd.getBeanClass().equals(AspectJExpressionPointcut.class));

            PropertyValue pv = bd.getPropertyValues().get(0);
            Assert.assertEquals("expression", pv.getName());
            Assert.assertEquals("execution(* com.niocoder.service.v5.*.placeOrder(..))", pv.getValue());
        }

        // 检查AspectJBeforeAdvice
        {
            String name = AspectJBeforeAdvice.class.getName() + "#0";
            BeanDefinition bd = factory.getBeanDefinition(name);
            Assert.assertTrue(bd.getBeanClass().equals(AspectJBeforeAdvice.class));

            // 这个BeanDefinition是合成的
            Assert.assertTrue(bd.isSynthetic());

            List<ConstructorArgument.ValueHolder> argumentValues = bd.getConstructorArgument().getArgumentValues();
            Assert.assertEquals(3, argumentValues.size());

            // 构造函数第一个参数
            {
                BeanDefinition innerBeanDef = (BeanDefinition) argumentValues.get(0).getValue();
                Assert.assertTrue(innerBeanDef.isSynthetic());
                Assert.assertTrue(innerBeanDef.getBeanClass().equals(MethodLocatingFactory.class));

                List<PropertyValue> pvs = innerBeanDef.getPropertyValues();
                Assert.assertEquals("targetBeanName", pvs.get(0).getName());
                Assert.assertEquals("tx", pvs.get(0).getValue());
                Assert.assertEquals("methodName", pvs.get(1).getName());
                Assert.assertEquals("start", pvs.get(1).getValue());
            }

            // 构造函数第二个参数
            {
                RuntimeBeanReference ref = (RuntimeBeanReference) argumentValues.get(1).getValue();
                Assert.assertEquals("placeOrder", ref.getBeanName());
            }
            chekValueHolder(argumentValues);


        }

        // 检查AspectJAfterReturningAdvice
        {
            String name = AspectJAfterReturningAdvice.class.getName() + "#0";
            BeanDefinition bd = factory.getBeanDefinition(name);
            Assert.assertTrue(bd.getBeanClass().equals(AspectJAfterReturningAdvice.class));

            // 这个BeanDefinition是合成的
            Assert.assertTrue(bd.isSynthetic());

            List<ConstructorArgument.ValueHolder> argumentValues = bd.getConstructorArgument().getArgumentValues();
            Assert.assertEquals(3, argumentValues.size());

            // 构造函数第一个参数
            {
                BeanDefinition innerBeanDef = (BeanDefinition) argumentValues.get(0).getValue();
                Assert.assertTrue(innerBeanDef.isSynthetic());
                Assert.assertTrue(innerBeanDef.getBeanClass().equals(MethodLocatingFactory.class));

                List<PropertyValue> pvs = innerBeanDef.getPropertyValues();
                Assert.assertEquals("targetBeanName", pvs.get(0).getName());
                Assert.assertEquals("tx", pvs.get(0).getValue());
                Assert.assertEquals("methodName", pvs.get(1).getName());
                Assert.assertEquals("commit", pvs.get(1).getValue());
            }

            // 构造函数第二个参数
            {
                RuntimeBeanReference ref = (RuntimeBeanReference) argumentValues.get(1).getValue();
                Assert.assertEquals("placeOrder", ref.getBeanName());
            }

            // 构造函数第三个参数
            chekValueHolder(argumentValues);
        }
        // 检查AspectJAfterThrowingAdvice
        {
            String name = AspectJAfterThrowingAdvice.class.getName() + "#0";
            BeanDefinition bd = factory.getBeanDefinition(name);
            Assert.assertTrue(bd.getBeanClass().equals(AspectJAfterThrowingAdvice.class));

            // 这个BeanDefinition是合成的
            Assert.assertTrue(bd.isSynthetic());

            List<ConstructorArgument.ValueHolder> argumentValues = bd.getConstructorArgument().getArgumentValues();
            Assert.assertEquals(3, argumentValues.size());

            // 构造函数第一个参数
            {
                BeanDefinition innerBeanDef = (BeanDefinition) argumentValues.get(0).getValue();
                Assert.assertTrue(innerBeanDef.isSynthetic());
                Assert.assertTrue(innerBeanDef.getBeanClass().equals(MethodLocatingFactory.class));

                List<PropertyValue> pvs = innerBeanDef.getPropertyValues();
                Assert.assertEquals("targetBeanName", pvs.get(0).getName());
                Assert.assertEquals("tx", pvs.get(0).getValue());
                Assert.assertEquals("methodName", pvs.get(1).getName());
                Assert.assertEquals("rollback", pvs.get(1).getValue());
            }

            // 构造函数第二个参数
            {
                RuntimeBeanReference ref = (RuntimeBeanReference) argumentValues.get(1).getValue();
                Assert.assertEquals("placeOrder", ref.getBeanName());
            }

            // 构造函数第三个参数
            chekValueHolder(argumentValues);
        }

    }

    private void chekValueHolder(List<ConstructorArgument.ValueHolder> argumentValues) {
        // 构造函数第三个参数
        {
            BeanDefinition innerBeanDef = (BeanDefinition) argumentValues.get(2).getValue();
            Assert.assertTrue(innerBeanDef.isSynthetic());
            Assert.assertTrue(innerBeanDef.getBeanClass().equals(AspectInstanceFactory.class));
            List<PropertyValue> pvs = innerBeanDef.getPropertyValues();
            Assert.assertEquals("aspectBeanName", pvs.get(0).getName());
            Assert.assertEquals("tx", pvs.get(0).getValue());
        }
    }
}
```

### bean的生命周期

我们已经完成解析`aop`标签，现在只需要把解析出来的标签和前面已完成的功能结合起来，在`bean`的的指定的生命周期中调用即可。

#### BeanFactory

增加`getBeansByType`方法，根据`class`类型返回所有相应的实例。

```java
public interface BeanFactory {
......
    
    /**
     * 根据 class 类型 返回所有实例
     *
     * @param type
     * @return
     */
    List<Object> getBeansByType(Class<?> type);
}
```

#### DefaultBeanFactory 

实现`DefaultBeanFactory`方法和判断是否需要创建代理累。

```java
public class DefaultBeanFactory extends AbstractBeanFactory implements
        BeanDefinitionRegistry {
     ......   
     @Override
    protected Object createBean(BeanDefinition bd) {

        // 1. 创建实例
        Object bean = instantiateBean(bd);
        // 2. 设置属性
        populateBean(bd, bean);
        // 初始化操作
        bean = initializeBean(bd, bean);

        return bean;
    }
	......
     protected Object initializeBean(BeanDefinition bd, Object bean) {
        invokeAwareMethods(bean);
        // todo 对Bean做初始化
        // 创建代理
        if (!bd.isSynthetic()) {
            return applyBeanPostProcessorsAfterInitialization(bean, bd.getId());
        }
        return bean;
    }

    private Object applyBeanPostProcessorsAfterInitialization(Object existingBean, String beanName) throws BeansException {
        Object result = existingBean;
        for (BeanPostProcessor beanPostProcessor : getBeanPostProcessors()) {
            result = beanPostProcessor.postProcessAfterInitialization(result, beanName);
            if (result == null) {
                return result;
            }
        }
        return result;
    }

    private void invokeAwareMethods(Object bean) {
        if (bean instanceof BeanFactoryAware) {
            ((BeanFactoryAware) bean).setBeanFactory(this);
        }
    }

    @Override
    public List<Object> getBeansByType(Class<?> type) {
        List<Object> result = new ArrayList<Object>();
        List<String> beanIDs = this.getBeanIDsByType(type);
        for (String beanID : beanIDs) {
            result.add(this.getBean(beanID));
        }
        return result;
    }

    private List<String> getBeanIDsByType(Class<?> type) {
        List<String> result = new ArrayList<String>();
        for (String beanName : this.beanDefinitionMap.keySet()) {
            Class<?> beanClass = null;
            try {
                beanClass = this.getType(beanName);
            } catch (Exception e) {
                log.warning("can't load class for bean :" + beanName + ", skip it.");
                continue;
            }

            if ((beanClass != null) && type.isAssignableFrom(beanClass)) {
                result.add(beanName);
            }
        }
        return result;
    }        
}
```

#### AspectJAutoProxyCreator

实现了`BeanPostProcessor`,创建动态代理的后处理器。

```java
public class AspectJAutoProxyCreator implements BeanPostProcessor {

    ConfigurableBeanFactory beanFactory;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

        // 判断这个bean 本身就是Advice及其子类，那就不需要再生成代理类了
        if (isInfrastructureClass(bean.getClass())) {
            return bean;
        }

        List<Advice> advices = getCandidateAdvices(bean);
        if (advices.isEmpty()) {
            return bean;
        }

        return createProxy(advices, bean);

    }

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
            //TODO 需要实现JDK 代理
            //proxyFactory = new JdkAopProxyFactory(config);
        }

        return proxyFactory.getProxy();
    }

    private List<Advice> getCandidateAdvices(Object bean) {

        List<Object> advices = this.beanFactory.getBeansByType(Advice.class);

        List<Advice> result = new ArrayList<Advice>();
        for (Object o : advices) {
            Pointcut pc = ((Advice) o).getPointcut();
            if (canApply(pc, bean.getClass())) {
                result.add((Advice) o);
            }

        }
        return result;
    }

    private boolean isInfrastructureClass(Class<?> beanClass) {
        return Advice.class.isAssignableFrom(beanClass);
    }

    public static boolean canApply(Pointcut pc, Class<?> targetClass) {

        MethodMatcher methodMatcher = pc.getMethodMatcher();

        Set<Class> classes = new LinkedHashSet<Class>(ClassUtils.getAllInterfacesForClassAsSet(targetClass));
        classes.add(targetClass);
        for (Class<?> clazz : classes) {
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                if (methodMatcher.matches(method/*, targetClass*/)) {
                    return true;
                }
            }
        }

        return false;
    }

    public void setBeanFactory(ConfigurableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;

    }
}
```

#### AbstractApplicationContext

添加`aop`的后处理器。

```java
public abstract class AbstractApplicationContext implements ApplicationContext {
    protected void registerBeanPostProcessors(ConfigurableBeanFactory beanFactory) {

        {
            AspectJAutoProxyCreator postProcessor = new AspectJAutoProxyCreator();
            postProcessor.setBeanFactory(beanFactory);
            beanFactory.addBeanPostProcessor(postProcessor);
        }

        {
            AutowiredAnnotationBeanPostProcessor processor = new AutowiredAnnotationBeanPostProcessor();
            processor.setBeanFactory(factory);
            beanFactory.addBeanPostProcessor(processor);
        }
    }

    @Override
    public Class<?> getType(String name) throws NoSuchBeanDefinitionException {
        return this.factory.getType(name);
    }

    @Override
    public List<Object> getBeansByType(Class<?> type) {
        return this.factory.getBeansByType(type);
    }
}
```
#### ApplicationContextTest

测试`aop`。

```java
public class ApplicationContextTest {

    @Test
    public void testApplicationContext() throws Exception {

        MessageTracker.clearMsgs();

        ApplicationContext ctx = new ClassPathXmlApplicationContext("bean-v5.xml");
        NioCoderService nioCoder = (NioCoderService) ctx.getBean("nioCoder");

        Assert.assertNotNull(nioCoder.getAccountDao());
        Assert.assertNotNull(nioCoder.getItemDao());

        nioCoder.placeOrder();

        //
        List<String> msgs = MessageTracker.getMsgs();

        Assert.assertEquals(3, msgs.size());
        Assert.assertEquals("start tx", msgs.get(0));
        Assert.assertEquals("place order", msgs.get(1));
        Assert.assertEquals("commit tx", msgs.get(2));
    }
}

```

#### 代码下载

- github:[https://github.com/longfeizheng/small-spring/tree/20190220_aop_v7](https://github.com/longfeizheng/small-spring/tree/20190220_aop_v7)


## 代码下载 ##

- github:[https://github.com/longfeizheng/small-spring](https://github.com/longfeizheng/small-spring)

## 参考资料 ##

---
[从零开始造Spring](https://mp.weixin.qq.com/s/gbvdwpPtQcjyaigRBDjd-Q)
