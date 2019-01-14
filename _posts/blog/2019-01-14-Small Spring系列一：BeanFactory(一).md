---
layout: post
title: BeanFactory(一)
categories: Spring
description: Spring
keywords: Spring
---

> 人生如逆旅，我亦是行人。

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-01.jpg](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-01.jpg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-01.jpg")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-01.jpg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-01.jpg")

## 前言 ##

Spring是一个开放源代码的设计层面框架，它解决的是业务逻辑层和其他各层的松耦合问题，因此它将面向接口的编程思想贯穿整个系统应用。

## 准备 ##

- `bean-v1.xml`配置`bean`的信息
- `BeanDefinition`用于存放`bean`的定义
- `BeanFactory`获取bean`的信息，实例化`bean`
- `BeanFactoryTest`测试`BeanFactory`是否可用


### bean-v1.xml ###

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans>
    <bean id = "nioCoder"
        class = "com.niocoder.service.v1.NioCoderService">
    </bean>

    <bean id ="invalidBean"
        class="xxx.xxx">
    </bean>
</beans>
```
### BeanDefinition ###

`bean-v1.xml`中定义了每个`bean`，但这些信息我们该如何存储呢？
`spring`是通过`BeanDefinition`接口来描述`bean`的定义

#### BeanDefinition ####

```java
package com.niocoder.beans;

/**
 * bean.xml bean的定义
 * @author zhenglongfei
 */
public interface BeanDefinition {

    /**
     * 获取bean.xml中 bean的全名 如 "com.niocoder.service.v1.NioCoderService"
     * @return
     */
    String getBeanClassName();
}

```

#### GenericBeanDefinition ####

`GenericBeanDefinition`实现了`BeanDefinition`接口
```java
package com.niocoder.beans.factory.support;


import com.niocoder.beans.BeanDefinition;

/**
 * BeanDefinition 实现类
 *
 * @author zhenglongfei
 */
public class GenericBeanDefinition implements BeanDefinition {

    private String id;
    private String beanClassName;

    public GenericBeanDefinition(String id, String beanClassName) {
        this.id = id;
        this.beanClassName = beanClassName;
    }

    public String getBeanClassName() {
        return this.beanClassName;
    }
}


```

### BeanFactory ###

我们已经使用`BeanDefinition`来描述`bean-v1.xml`的`bean`的定义,下面我们使用`BeanFactory`来获取`bean`的实例

#### BeanFactory ####

```java
package com.niocoder.beans.factory;

import com.niocoder.beans.BeanDefinition;

/**
 * 创建bean的实例
 * @author zhenglongfei
 */
public interface BeanFactory {

    /**
     * 获取bean的定义
     * @param beanId
     * @return
     */
    BeanDefinition getBeanDefinition(String beanId);

    /**
     * 获取bean的实例
     * @param beanId
     * @return
     */
    Object getBean(String beanId);
}

```

#### DefaultBeanFactory ####

`DefaultBeanFactory`实现了`BeanFactory`接口

```java
package com.niocoder.beans.factory.support;

import com.niocoder.beans.BeanDefinition;
import com.niocoder.beans.factory.BeanCreationException;
import com.niocoder.beans.factory.BeanDefinitionStoreException;
import com.niocoder.beans.factory.BeanFactory;
import com.niocoder.util.ClassUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * BeanFactory的默认实现类
 *
 * @author zhenglongfei
 */
public class DefaultBeanFactory implements BeanFactory {

    private static final String ID_ATTRIBUTE = "id";

    private static final String CLASS_ATTRIBUTE = "class";
    /**
     * 存放BeanDefinition
     */
    private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    /**
     * 根据文件名称加载,解析bean.xml
     *
     * @param configFile
     */
    public DefaultBeanFactory(String configFile) {
        loadBeanDefinition(configFile);
    }

    /**
     * 具体解析bean.xml的方法 使用dom4j
     *
     * @param configFile
     */
    private void loadBeanDefinition(String configFile) {
        ClassLoader cl = ClassUtils.getDefaultClassLoader();
        try (InputStream is = cl.getResourceAsStream(configFile)) {
            SAXReader reader = new SAXReader();
            Document doc = reader.read(is);

            Element root = doc.getRootElement();
            Iterator<Element> elementIterator = root.elementIterator();
            while (elementIterator.hasNext()) {
                Element ele = elementIterator.next();
                String id = ele.attributeValue(ID_ATTRIBUTE);
                String beanClassName = ele.attributeValue(CLASS_ATTRIBUTE);
                BeanDefinition bd = new GenericBeanDefinition(id, beanClassName);
                this.beanDefinitionMap.put(id, bd);
            }
        } catch (Exception e) {
            throw new BeanDefinitionStoreException("IOException parsing XML document", e);
        }
    }

    @Override
    public BeanDefinition getBeanDefinition(String beanId) {
        return this.beanDefinitionMap.get(beanId);
    }

    @Override
    public Object getBean(String beanId) {
        BeanDefinition bd = this.getBeanDefinition(beanId);
        if (bd == null) {
            throw new BeanCreationException("BeanDefinition does not exist");
        }
        ClassLoader cl = ClassUtils.getDefaultClassLoader();

        String beanClassName = bd.getBeanClassName();
        try {
            // 使用反射创建bean的实例，需要对象存在默认的无参构造方法
            Class<?> clz = cl.loadClass(beanClassName);
            return clz.newInstance();
        } catch (Exception e) {
            throw new BeanCreationException("Bean Definition does not exist");
        }
    }
}

```

### BeanFactoryTest ###

以上，我们已经创建了`bean.xml`，`BeanDefinition`来描述`bean`的定义，并且使用`BeanFactory`来获取`bean`的实例。下面我们来测试一下`BeanFactory`是否可用。

```java
package com.niocoder.test.v1;

import com.niocoder.beans.BeanDefinition;
import com.niocoder.beans.factory.BeanCreationException;
import com.niocoder.beans.factory.BeanDefinitionStoreException;
import com.niocoder.beans.factory.BeanFactory;
import com.niocoder.beans.factory.support.DefaultBeanFactory;
import com.niocoder.service.v1.NioCoderService;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * BeanFactory 测试类
 */
public class BeanFactoryTest {

    /**
     * 测试获取bean
     */
    @Test
    public void testGetBean() {
        BeanFactory factory = new DefaultBeanFactory("bean-v1.xml");
        BeanDefinition bd = factory.getBeanDefinition("nioCoder");

        assertEquals("com.niocoder.service.v1.NioCoderService", bd.getBeanClassName());

        NioCoderService nioCoderService = (NioCoderService) factory.getBean("nioCoder");

        assertNotNull(nioCoderService);
    }

    /**
     * 测试无效的bean
     */
    @Test
    public void testInvalidBean() {
        BeanFactory factory = new DefaultBeanFactory("bean-v1.xml");
        try {
            factory.getBean("invalidBean");
        } catch (BeanCreationException e) {
            return;
        }

        Assert.fail("expect BeanCreationException ");
    }

    /**
     * 测试无效的xml
     */
    @Test
    public void testInvalidXML() {
        try {
            new DefaultBeanFactory("xxx.xml");
        } catch (BeanDefinitionStoreException e) {
            return;
        }

        Assert.fail("expect BeanDefinitionStoreException ");
    }
}

```

## 代码下载 ##

- github:[https://github.com/longfeizheng/small-spring/tree/20190914_BeanFactory_v1](https://github.com/longfeizheng/small-spring/tree/20190914_BeanFactory_v1)
