---
layout: post
title: Small Spring系列七：annotation Injection(三)
categories: Spring
description: Spring
keywords: Spring
---

>秋水共长天一色 落霞与孤鹜齐飞。

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-07.jpg](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-07.jpg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-07.jpg")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-07.jpg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-07.jpg")

## 概述 ##

前两章我们已经完成了使用`ASM`读取`Annotation`、新增`SimpleMetadataReader`封装了复杂的`Vister`、同时引入了`AnnotatedBeanDefinition`和`ScannedGenericBeanDefinition`表明注解扫描的`BeanDefinition`。本章我们来实现最后的`Field Injection`。

### 分析

```java
@Component(value = "nioCoder")
public class NioCoderService {

    @Autowired
    private AccountDao accountDao;

    @Autowired
    private ItemDao itemDao;
}
```

要实现`Field Injection` 我们需要根据`class`类型从`BeanFactory`中获取一个对象然后注入。即需要在`BeanFactory`新增一个`BeanFactory.resolveDepency(Class type)`方法,如果`type`为`AccountDao`则找到(或创建)对应的实例并且返回。

这么一看，貌似是可行的,但我们在使用`Spring`的`@Autowired`注解时发现，该注解可以应用于构造器注入、属性注入和`setter`注入。`spring`提供了一个`DependencyDescriptor`来封装`@Autowired`所有情况。

再有我们确定要把`resolveDependency`方法放置到`BeanFactory`中吗？`BeanFactory`是我们的一个顶级接口，我们不希望对外暴露太多的方法,所以我们新增一个`AutowireCapableBeanFactory`接口,在`AutowireCapableBeanFactory`中增加`resolveDependency`方法。`AutowireCapableBeanFactory`继承`BeanFactory`。

#### DependencyDescriptor

封装`@Autowired`所有情况。(暂只支持字段注入,不支持方法注入)

```java
package com.niocoder.beans.factory.config;

import java.lang.reflect.Field;

/**
 * 表明属性注入或者方法注入
 *
 * @author zhenglongfei
 */
public class DependencyDescriptor {
    /**
     * 属性注入
     */
    private Field field;
    /**
     * 方法注入
     */
//    private MethodParameter methodParameter;

    private boolean required;

    public DependencyDescriptor(Field field, boolean required) {
        this.field = field;
        this.required = required;
    }

    public Class<?> getDependencyType() {
        if (this.field != null) {
            // 字段的类型如 AccountDao ItemDao
            return field.getType();
        }
        // TODO 方法注入不支持
        throw new RuntimeException("only support field dependency");
    }

    public boolean isRequired() {
        return this.required;
    }
}

```

#### AutowireCapableBeanFactory

增加`resolveDependency(DependencyDescriptor descriptor)`方法,根据`field`返回对应的实例。

```java
package com.niocoder.beans.factory.config;

import com.niocoder.beans.factory.BeanFactory;

/**
 * 表明注解 注入的beanFactory
 */
public interface AutowireCapableBeanFactory extends BeanFactory {

    /**
     * 根据字段属性的描述，获得所对应的实例
     *
     * @param descriptor
     * @return
     */
    Object resolveDependency(DependencyDescriptor descriptor);
}
```

#### DefaultBeanFactory

由实现`BeanFactory`改成实现`AutowireCapableBeanFactory`。

```java
public class DefaultBeanFactory extends DefaultSingletonBeanRegistry implements AutowireCapableBeanFactory, BeanDefinitionRegistry {

......
    @Override
    public Object resolveDependency(DependencyDescriptor descriptor) {
        Class<?> typeToMatch = descriptor.getDependencyType();
        for (BeanDefinition bd : this.beanDefinitionMap.values()) {
            // 确保BeanDefinition 有Class对象,而不是class的字符串
            resolveBeanClass(bd);
            Class<?> beanClass = bd.getBeanClass();
            if (typeToMatch.isAssignableFrom(beanClass)) {
                return this.getBean(bd.getId());
            }
        }
        return null;
    }

    public void resolveBeanClass(BeanDefinition bd) {
        if (bd.hasBeanClass()) {
            return;
        } else {
            try {
                bd.resolveBeanClass(ClassUtils.getDefaultClassLoader());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("can't load class" + bd.getBeanClassName());
            }
        }
    }

......

}
```

#### GenericBeanDefinition

新增`beanClass`属性,缓存`bean`的`class`对象

```java
public class GenericBeanDefinition implements BeanDefinition {

    private Class<?> beanClass;
......
    @Override
    public Class<?> getBeanClass() {
        if (this.beanClass == null) {
            throw new IllegalStateException("Bean class name [" + this.getBeanClassName() + "] has not been resolve into an actual Class");
        }
        return this.beanClass;
    }
    @Override
    public boolean hasBeanClass() {
        return this.beanClass != null;
    }

    @Override
    public Class<?> resolveBeanClass(ClassLoader beanClassLoader) throws ClassNotFoundException {
        String className = getBeanClassName();
        if (className == null) {
            return null;
        }
        Class<?> resolvedClass = beanClassLoader.loadClass(className);
        this.beanClass = resolvedClass;
        return resolvedClass;
    }

}
```

#### DependencyDescriptorTest

测试`DependencyDescriptor`

```java
/**
 * 测试DependencyDescriptor
 */
public class DependencyDescriptorTest {

    @Test
    public void testResolveDependency() throws Exception {
        DefaultBeanFactory factory = new DefaultBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(factory);
        reader.loadBeanDefinition(new ClassPathResource("bean-v4.xml"));

        Field f = NioCoderService.class.getDeclaredField("accountDao");
        DependencyDescriptor descriptor = new DependencyDescriptor(f, true);
        Object o = factory.resolveDependency(descriptor);
        Assert.assertTrue(o instanceof AccountDao);

    }
}
```

#### 代码下载

- github:[https://github.com/longfeizheng/small-spring/tree/20190202_annotation_v5](https://github.com/longfeizheng/small-spring/tree/20190202_annotation_v5)

### Field Injection

上面我们已经可以通过`class`类型来获取`bean`的实例了，那么怎么才能实现自动注入呢？

首先我们需要一个类含有`targetClass`和一个集合的属性列表`List<Class>`,每个集合中的元素调用各自的`inject(taget)`方法(反射)从而实现属性注入。类图如下:

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/annotation_v6.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/annotation_v6.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/annotation_v6.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/annotation_v6.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/annotation_v6.png")

#### InjectionElement

抽象类,表明需要注入的属性字段

```java
package com.niocoder.beans.factory.annotation;

import com.niocoder.beans.factory.config.AutowireCapableBeanFactory;

import java.lang.reflect.Member;

/**
 * @author zhenglongfei
 */
public abstract class InjectionElement {

    protected Member member;
    protected AutowireCapableBeanFactory factory;

    InjectionElement(Member member, AutowireCapableBeanFactory factory) {
        this.member = member;
        this.factory = factory;
    }

    /**
     * 属性注入的抽象方法
     *
     * @param target
     */
    public abstract void inject(Object target);
}

```

#### AutowiredFieldElement

继承抽象类`InjectionElement`实现属性注入

```java
package com.niocoder.beans.factory.annotation;

import com.niocoder.beans.factory.BeanCreationException;
import com.niocoder.beans.factory.config.AutowireCapableBeanFactory;
import com.niocoder.beans.factory.config.DependencyDescriptor;
import com.niocoder.util.*;

import java.lang.reflect.Field;


/**
 * @author zhenglongfei
 */
public class AutowiredFieldElement extends InjectionElement {

    boolean required;

    public AutowiredFieldElement(Field f, boolean required, AutowireCapableBeanFactory factory) {
        super(f, factory);
        this.required = required;
    }

    public Field getField() {
        return (Field) this.member;
    }

    @Override
    public void inject(Object target) {
        Field field = this.getField();

        try {
            DependencyDescriptor desc = new DependencyDescriptor(field, this.required);
            Object value = factory.resolveDependency(desc);
            if (value != null) {
                ReflectionUtils.makeAccessible(field);
                field.set(target, value);
            }
        } catch (Throwable e) {
            throw new BeanCreationException("could not autowire field " + field, e);
        }
    }
}

```

#### InjectionMetadata

将`targetClass`和`List<InjectionElement>`结合,从而实现属性注入

```java
package com.niocoder.beans.factory.annotation;

import java.util.List;

/**
 * @author zhenglongfei
 */
public class InjectionMetadata {

    private final Class<?> targetClass;
    private List<InjectionElement> injectionElements;

    public InjectionMetadata(Class<?> targetClass, List<InjectionElement> injectionElements) {
        this.targetClass = targetClass;
        this.injectionElements = injectionElements;
    }

    public List<InjectionElement> getInjectionElements() {
        return injectionElements;
    }

    public void inject(Object target) {
        if (injectionElements == null || injectionElements.isEmpty()) {
            return;
        }
        for (InjectionElement ele : injectionElements) {
            ele.inject(target);
        }
    }
}
```

#### InjectionMetadataTest

测试属性注入

```java
package com.niocoder.test.v4;

import com.niocoder.beans.factory.annotation.AutowiredFieldElement;
import com.niocoder.beans.factory.annotation.InjectionElement;
import com.niocoder.beans.factory.annotation.InjectionMetadata;
import com.niocoder.beans.factory.support.DefaultBeanFactory;
import com.niocoder.beans.factory.xml.XmlBeanDefinitionReader;
import com.niocoder.core.io.ClassPathResource;
import com.niocoder.dao.v4.AccountDao;
import com.niocoder.dao.v4.ItemDao;
import com.niocoder.service.v4.NioCoderService;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.LinkedList;


public class InjectionMetadataTest {

    @Test
    public void testInjection() throws Exception {
        DefaultBeanFactory factory = new DefaultBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(factory);
        reader.loadBeanDefinition(new ClassPathResource("bean-v4.xml"));

        Class<?> clz = NioCoderService.class;

        LinkedList<InjectionElement> elements = new LinkedList<InjectionElement>();

        {
            Field f = NioCoderService.class.getDeclaredField("accountDao");
            InjectionElement injectionElement = new AutowiredFieldElement(f, true, factory);
            elements.add(injectionElement);
        }

        {
            Field f = NioCoderService.class.getDeclaredField("itemDao");
            InjectionElement injectionElement = new AutowiredFieldElement(f, true, factory);
            elements.add(injectionElement);
        }

        InjectionMetadata metadata = new InjectionMetadata(clz, elements);
        NioCoderService nioCoderService = new NioCoderService();
        metadata.inject(nioCoderService);

        Assert.assertTrue(nioCoderService.getAccountDao() instanceof AccountDao);

        Assert.assertTrue(nioCoderService.getItemDao() instanceof ItemDao);
    }
}

```

#### 代码下载

- github:[https://github.com/longfeizheng/small-spring/tree/20190202_annotation_v6](https://github.com/longfeizheng/small-spring/tree/20190202_annotation_v6)

### 自动构建InjectionMetadata

以上我们只是通过手动的将`NioCoderService`转变成了`InjectionMetadata`并且调用`inject`方法,从而实现了`Field Injection`,我们需要一个类来自动帮我们处理这些操作。

#### AutowiredAnnotationBeanPostProcessor

用于自动构建`InjectionMetadata`

```java
public class AutowiredAnnotationBeanPostProcessor{

    private AutowireCapableBeanFactory beanFactory;
    private String requiredParameterName = "required";
    private boolean requiredParameterValue = true;

    /**
     * 存储需要判断的注解信息
     */
    private final Set<Class<? extends Annotation>> autowiredAnnotationTypes = new LinkedHashSet<>();

    /**
     * 添加Autowired注解
     */
    public AutowiredAnnotationBeanPostProcessor() {
        this.autowiredAnnotationTypes.add(Autowired.class);
    }

    /**
     * 给定class对象构建InjectionMetadata
     * @param clazz
     * @return
     */
    public InjectionMetadata buildAutowiringMetadata(Class<?> clazz) {
        List<InjectionElement> elements = new LinkedList<>();
        Class<?> targetClass = clazz;

        do {
            List<InjectionElement> currElements = new LinkedList<>();
            for (Field field : targetClass.getDeclaredFields()) {
                Annotation ann = findAutowiredAnnotation(field);
                if (ann != null) {
                    if (Modifier.isStatic(field.getModifiers())) {
                        continue;
                    }
                    boolean required = determineRequiredStatus(ann);
                    currElements.add(new AutowiredFieldElement(field, required, beanFactory));
                }
            }

            for (Method method : targetClass.getDeclaredMethods()) {
                //TODO
            }
            elements.addAll(0, currElements);
            targetClass = targetClass.getSuperclass();
        } while (targetClass != null && targetClass != Object.class);

        return new InjectionMetadata(clazz, elements);
    }

    /**
     * 判断是否是required是否必须
     *
     * @param ann
     * @return
     */
    private boolean determineRequiredStatus(Annotation ann) {
        try {
            Method method = ReflectionUtils.findMethod(ann.annotationType(), this.requiredParameterName);
            if (method == null) {
                // Annotations like @Inject and @Value don't have a method (attribute) named "required"
                // -> default to required status
                return true;
            }
            return (this.requiredParameterValue == (Boolean) ReflectionUtils.invokeMethod(method, ann));
        } catch (Exception ex) {
            // An exception was thrown during reflective invocation of the required attribute
            // -> default to required status
            return true;
        }
    }

    /**
     * 判断属性是否存在Autowired 注解
     *
     * @param field
     * @return
     */
    private Annotation findAutowiredAnnotation(Field field) {
        for (Class<? extends Annotation> type : this.autowiredAnnotationTypes) {
            Annotation ann = AnnotationUtils.getAnnotation(field, type);
            if (ann != null) {
                return ann;
            }
        }
        return null;
    }

    public void setBeanFactory(ConfigurableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }
}
```

#### AutowiredAnnotationProcessorTest

测试 `AutowiredAnnotationProcessor`

```java
public class AutowiredAnnotationProcessorTest {

    AccountDao accountDao = new AccountDao();
    ItemDao itemDao = new ItemDao();

    DefaultBeanFactory beanFactory = new DefaultBeanFactory() {
        @Override
        public Object resolveDependency(DependencyDescriptor descriptor) {
            if (descriptor.getDependencyType().equals(AccountDao.class)) {
                return accountDao;
            }

            if (descriptor.getDependencyType().equals(ItemDao.class)) {
                return itemDao;
            }
            throw new RuntimeException("can't support types except AccountDao and ItemDao");
        }
    };

    @Test
    public void testGetInjectionMetadata() {
        AutowiredAnnotationBeanPostProcessor processor = new AutowiredAnnotationBeanPostProcessor();
        processor.setBeanFactory(beanFactory);
        InjectionMetadata injectionMetadata = processor.buildAutowiringMetadata(NioCoderService.class);
        List<InjectionElement> elements = injectionMetadata.getInjectionElements();
        Assert.assertEquals(2, elements.size());

        assertFieldExists(elements,"accountDao");
        assertFieldExists(elements,"itemDao");

        NioCoderService nioCoderService = new NioCoderService();
        injectionMetadata.inject(nioCoderService);

        Assert.assertTrue(nioCoderService.getAccountDao() instanceof AccountDao);

        Assert.assertTrue(nioCoderService.getItemDao() instanceof ItemDao);
    }

    private void assertFieldExists(List<InjectionElement> elements, String fieldName) {
        for (InjectionElement ele : elements) {
            AutowiredFieldElement fieldElement = (AutowiredFieldElement) ele;
            Field field = fieldElement.getField();
            if (field.getName().equals(fieldName)) {
                return;
            }
        }
        Assert.fail(fieldName + "does not exist!");
    }
}
```

### 完善Field Injection

至此我们已经完成了`Field Injection`90%的工作,剩下要考虑的就是应该在什么时候调用类的这些方法？

所以我们要考虑一下`bean`的生命周期,关于`bean`的生命周期,可以看下图

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/bean_life.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/bean_life.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/bean_life.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/bean_life.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/bean_life.png")

参考[Bean的生命周期](http://niocoder.com/2016/11/08/spring-bean/)

我们需要找`InstantiationAwareBeanPostProcessor.postProcessPropertyValues()`实现`Autowired`注解。关于`BeanPostProcessor`的类图如下:

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/annotation_v7.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/annotation_v7.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/annotation_v7.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/annotation_v7.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/annotation_v7.png")

#### BeanPostProcessor

`bean`初始化之前和初始化之后的处理器操作

```java
package com.niocoder.beans.factory.config;

import com.niocoder.beans.BeansException;

/**
 * bean的后置处理器
 *
 * @author zhenglongfei
 */
public interface BeanPostProcessor {

    /**
     * 初始化之前的操作
     *
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    default Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    /**
     * 初始化之后的操作
     *
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    default Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
```

#### InstantiationAwareBeanPostProcessor


`bean`实例化之前和实例化之后的处理器操作

```java
package com.niocoder.beans.factory.config;

import com.niocoder.beans.BeansException;

/**
 * 实例化的后处理器
 *
 * @author zhenglongfei
 */
public interface InstantiationAwareBeanPostProcessor extends BeanPostProcessor {

    /**
     * 实例化之前
     *
     * @param beanClass
     * @param beanName
     * @return
     * @throws BeansException
     */
    default Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
        return null;
    }

    /**
     * 实例化之后
     *
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    default boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
        return true;
    }

    /**
     * @param bean
     * @param beanName
     * @throws BeansException
     */
    default void postProcessPropertyValues(Object bean, String beanName) throws BeansException {
    }

}
```

#### AutowiredAnnotationBeanPostProcessor

实现`InstantiationAwareBeanPostProcessor`,在`postProcessPropertyValues`方法实现属性注入

```java
/**
 * 注解注入的后处理器
 *
 * @author zhenglongfei
 */
public class AutowiredAnnotationBeanPostProcessor implements InstantiationAwareBeanPostProcessor {

......
    @Override
    public void postProcessPropertyValues(Object bean, String beanName) throws BeansException {
        InjectionMetadata metadata = buildAutowiringMetadata(bean.getClass());
        try {
            metadata.inject(bean);
        } catch (Throwable ex) {
            throw new BeanCreationException(beanName, "Injection of autowired dependencies failed", ex);
        }
    }
}
```

#### ConfigurableBeanFactory

新增`ConfigurableBeanFactory`来处理`BeanPostProcessor`

```java
package com.niocoder.beans.factory.config;

import java.util.List;

/**
 * 可配置的beanFactory
 *
 * @author zhenglongfei
 */
public interface ConfigurableBeanFactory extends AutowireCapableBeanFactory {

    /**
     * 增加BeanPostProcessor
     *
     * @param postProcessor
     */
    void addBeanPostProcessor(BeanPostProcessor postProcessor);

    /**
     * 获取 BeanPostProcessor
     *
     * @return
     */
    List<BeanPostProcessor> getBeanPostProcessors();
}
```

#### DefaultBeanFactory

`DefaultBeanFactory`从实现`AutowireCapableBeanFactory`修改为`ConfigurableBeanFactory`,增加 `beanPostProcessors`属性,
在`populateBean`方法时处理`BeanPostProcessor`。

```java
/**
 * BeanFactory的默认实现类
 *
 * @author zhenglongfei
 */
public class DefaultBeanFactory extends DefaultSingletonBeanRegistry implements ConfigurableBeanFactory, BeanDefinitionRegistry {

    /**
     * 存放BeanPostProcessor
     */
    private List<BeanPostProcessor> beanPostProcessors = new ArrayList<>();


    private void populateBean(BeanDefinition bd, Object bean) {

        for (BeanPostProcessor postProcessor : this.getBeanPostProcessors()) {
            if (postProcessor instanceof InstantiationAwareBeanPostProcessor) {
                ((InstantiationAwareBeanPostProcessor) postProcessor).postProcessPropertyValues(bean, bd.getId());
            }
        }
		......
	}
    @Override
    public void addBeanPostProcessor(BeanPostProcessor postProcessor) {
        this.beanPostProcessors.add(postProcessor);
    }

    @Override
    public List<BeanPostProcessor> getBeanPostProcessors() {
        return this.beanPostProcessors;
    }
}
```

#### AbstractApplicationContext

构造方法时注册`AutowiredAnnotationBeanPostProcessor`实现注解注入

```java

public abstract class AbstractApplicationContext implements ApplicationContext {

    private DefaultBeanFactory factory = null;

    public AbstractApplicationContext(String configFile) {
        factory = new DefaultBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(factory);
        Resource resource = this.getResourceByPath(configFile);
        reader.loadBeanDefinition(resource);
        registerBeanPostProcessors(factory);
    }

    /**
     * 具体由子类实现
     *
     * @param configFile
     * @return
     */
    protected abstract Resource getResourceByPath(String configFile);

    @Override
    public Object getBean(String beanId) {
        return factory.getBean(beanId);
    }

    protected void registerBeanPostProcessors(ConfigurableBeanFactory beanFactory) {
        AutowiredAnnotationBeanPostProcessor processor = new AutowiredAnnotationBeanPostProcessor();
        processor.setBeanFactory(factory);
        beanFactory.addBeanPostProcessor(processor);
    }
}
```

#### ApplicationContextTestV4

测试注解注入

```java
/**
 *
 */
public class ApplicationContextTestV4 {

    @Test
    public void testGetBeanProperty() {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("bean-v4.xml");
        NioCoderService nioCoder = (NioCoderService) ctx.getBean("nioCoder");

        Assert.assertNotNull(nioCoder.getAccountDao());
        Assert.assertNotNull(nioCoder.getItemDao());
    }
}
```

#### 代码下载 ####

- github:[https://github.com/longfeizheng/small-spring/tree/20190211_annotation_v7](https://github.com/longfeizheng/small-spring/tree/20190211_annotation_v7)


## 代码下载 ##

- github:[https://github.com/longfeizheng/small-spring](https://github.com/longfeizheng/small-spring)

## 参考资料 ##

---
[从零开始造Spring](https://mp.weixin.qq.com/s/gbvdwpPtQcjyaigRBDjd-Q)
