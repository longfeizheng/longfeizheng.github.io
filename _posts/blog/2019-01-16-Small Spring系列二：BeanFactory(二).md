---
layout: post
title: Small Spring系列二：BeanFactory(二)
categories: Spring
description: Spring
keywords: Spring
---

> 愿君多采撷，此物最相思。

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-02.jpg](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-02.jpg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-02.jpg")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-02.jpg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-01.jpg")

## 概述 ##

在<a href="http://niocoder.com/2019/01/14/Small-Spring%E7%B3%BB%E5%88%97%E4%B8%80-BeanFactory(%E4%B8%80)/#%E5%89%8D%E8%A8%80">Small Spring系列一：BeanFactory(一)</a>中，我们用`DefaultBeanFactory`读取`bean.xlm`中的`bean`信息，并且也实现了`BeanFactory`的`getBean()`方法。但是实现的方式有些不友好，本章，我们将优化和完善`BeanFactory`。

### 问题 ###
你会发现在`DefaultBeanFactory`中存在读取`bean.xml`和反射创建对象两种操作，这不符合设计模式的[单一职责](https://baike.baidu.com/item/%E5%8D%95%E4%B8%80%E8%81%8C%E8%B4%A3%E5%8E%9F%E5%88%99/9456515)。因此我们新增加一个`XmlBeanDefinitionReader`来读取`bean.xml`。但解析的`BeanDefinition`如何交给`BeanFactory`处理呢？这里我们可以在`BeanFactory`接口中增加`registerBeanDefinition(String beanID, BeanDefinition bd)`方法，将`XmlBeanDefinitionReader`解析的`BeanDefinition`注册到`BeanFactory`中。(此时`XmlBeanDefinitionReader`中需要持有一个`BeanFactory`的实例)。类图如下:
[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/beanfactory_v2_1.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/beanfactory_v2_1.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/beanfactory_v2_1.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/beanfactory_v2_1.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-01.png")

#### BeanFactory ####

```java
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

    /**
     * 注册 BeanDefinition
     * @param beanID
     * @param bd
     */
    void registerBeanDefinition(String beanID, BeanDefinition bd);
}

```
> 增加registerBeanDefinition(String beanID, BeanDefinition bd)方法

#### DefaultBeanFactory ####

```java
/**
 * BeanFactory的默认实现类
 *
 * @author zhenglongfei
 */
public class DefaultBeanFactory implements BeanFactory {

    /**
     * 存放BeanDefinition
     */
    private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

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

    @Override
    public void registerBeanDefinition(String beanID, BeanDefinition bd) {
        this.beanDefinitionMap.put(beanID, bd);
    }
}
```
> 去除loadBeanDefinition方法

#### XmlBeanDefinitionReader ####

读取iexi`bean.xml`文件

```java
/**
 * @author zhenglongfei
 */
public class XmlBeanDefinitionReader {

    private static final String ID_ATTRIBUTE = "id";

    private static final String CLASS_ATTRIBUTE = "class";

    BeanFactory beanFactory;

    public XmlBeanDefinitionReader(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    /**
     * 具体解析bean.xml的方法 使用dom4j
     *
     * @param configFile
     */
    public void loadBeanDefinition(String configFile) {
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
                this.beanFactory.registerBeanDefinition(id, bd);
            }
        } catch (Exception e) {
            throw new BeanDefinitionStoreException("IOException parsing XML document", e);
        }
    }
}
```
> 持有BeanFactory实例，解析bean.xml之后调用registerBeanDefinition(id, bd)将BeanDefinition注册到BeanFactory中

#### BeanFactoryTest ####

修改测试类

```java
/**
 * BeanFactory 测试类
 */
public class BeanFactoryTest {

    /**
     * 测试获取bean
     */
    @Test
    public void testGetBean() {
        BeanFactory factory = new DefaultBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(factory);
        reader.loadBeanDefinition("bean-v1.xml");
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
        BeanFactory factory = new DefaultBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(factory);
        reader.loadBeanDefinition("bean-v1.xml");
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
            BeanFactory factory = new DefaultBeanFactory();
            XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(factory);
            reader.loadBeanDefinition("bean.xml");
        } catch (BeanDefinitionStoreException e) {
            return;
        }

        Assert.fail("expect BeanDefinitionStoreException ");
    }
}
```
> 使用XmlBeanDefinitionReader读取bean.xml

#### 代码下载 ####

- github:[https://github.com/longfeizheng/small-spring/tree/20190916_BeanFactory_v2_1](https://github.com/longfeizheng/small-spring/tree/20190916_BeanFactory_v2_1)

### 优化1.0 ###

我们再来重新审视一下`BeanFactory`接口中的方法，它现在除了`getBean(String beanId)`方法之外，还有`getBeanDefinition(String beanId)`和`registerBeanDefinition(String beanID, BeanDefinition bd)`。`registerBeanDefinition()`是用于`XmlBeanDefinitionReader`使用的，`XmlBeanDefinitionReader`持有`BeanFactory`的实例，那么它也会知道`BeanFactory`的`getBean()`方法，并且`BeanFactory`我们只想用它来获取`bean`的实例,不想对外暴露太多，所以我们新增一个`BeanDefinitionRegistry`接口来注册和获取`BeanDefinition`。类图如下
[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/beanfactory_v2_2.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/beanfactory_v2_2.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/beanfactory_v2_2.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/beanfactory_v2_2.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-02.png")

#### BeanFactory ####

```java
/**
 * 创建bean的实例
 * @author zhenglongfei
 */
public interface BeanFactory {

    /**
     * 获取bean的实例
     * @param beanId
     * @return
     */
    Object getBean(String beanId);
}
```
> 只保留getBean方法

#### BeanDefinitionRegistry ####

```java
/**
 * 注册BeanDefinition接口
 *
 * @author zhenglongfei
 */
public interface BeanDefinitionRegistry {

    /**
     * 获取beanDefinition
     *
     * @param beanId
     * @return
     */
    BeanDefinition getBeanDefinition(String beanId);

    /**
     * 注册beanDefinition
     *
     * @param beanId
     * @param bd
     */
    void registerBeanDefinition(String beanId, BeanDefinition bd);
}
```
> 把原先BeanFactory中getBeanDefinition()和registerBeanDefinition()提取到BeanDefinitionRegistry接口中

#### DefaultBeanFactory ####

```java
public class DefaultBeanFactory implements BeanFactory, BeanDefinitionRegistry{
  ... ...
}

```
> 实现了BeanDefinitionRegistry接口


#### XmlBeanDefinitionReader ####

```java
public class XmlBeanDefinitionReader{
	......
	 BeanDefinitionRegistry registry;

    public XmlBeanDefinitionReader(BeanDefinitionRegistry registry) {
        this.registry = registry;
    }

	public void loadBeanDefinition(String configFile){
		... ...
		this.registry.registerBeanDefinition(id, bd);
	}
}
```
> 由BeanFactory变成BeanDefinitionRegistry实例

#### 代码下载 ####

- github:[https://github.com/longfeizheng/small-spring/tree/20190916_BeanFactory_v2_2](https://github.com/longfeizheng/small-spring/tree/20190916_BeanFactory_v2_2)


### 优化2.0.1 ###

观察一下测试类`BeanFactoryTest`,当我们每次使用`BeanFactory`获取实例时，都需要使用到`XmlBeanDefinitionReader`的`loadBeanDefinition`方法。这有一些麻烦，我们该如何封装一下呢？这里我们新增一个`ApplicationContext`接口来封装这些操作。但由于配置文件的不确定性，有的可能存在`classpath`目录下，有的可能存在磁盘的目录下，又或者内存，网络等等。这里我们就简单的实现从`classpath`和磁盘目录两种情况。对应的`ClassPathXmlApplicationContext`和`FileSystemXmlApplicationContext`。由于`ClassPathXmlApplicationContext`和`FileSystemXmlApplicationContext`都需要读取文件，所以我们提取出来一个`Resource`接口，类图如下：
[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/beanfactory_v2_3.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/beanfactory_v2_3.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/beanfactory_v2_3.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/beanfactory_v2_3.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-03.png")

#### Resource ####

```java
/**
 * 抽取一个资源接口
 *
 * @author zhenglongfei
 */
public interface Resource {

    /**
     * 获取一个流
     * @return
     * @throws IOException
     */
    InputStream getInputStream() throws IOException;

    /**
     * 获取文件的描述
     * @return
     */
    String getDescription();
}
```
> 提取一个获取资源的接口

##### ClassPathResource #####

```java
public class ClassPathResource implements Resource {

    private String path;

    public ClassPathResource(String path) {
        this.path = path;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        InputStream is = ClassUtils.getDefaultClassLoader().getResourceAsStream(this.path);
        if (is == null) {
            throw new FileNotFoundException(path + " cannot be opened");
        }
        return is;
    }

    @Override
    public String getDescription() {
        return this.path;
    }
}
```
> 从classpath下获取资源

##### FileSystemResource #####

```java
public class FileSystemResource implements Resource {

    private String path;
    private File file;

    public FileSystemResource(String path) {
        this.path = path;
        this.file = new File(path);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(this.file);
    }

    @Override
    public String getDescription() {
        return this.file.getAbsolutePath();
    }
}
```
> 从磁盘目录下获取资源

#### XmlBeanDefinitionReader ####

```java
public class XmlBeanDefinitionReader {
......
    public void loadBeanDefinition(Resource resource) {
        try (InputStream is = resource.getInputStream()) {
            SAXReader reader = new SAXReader();
            Document doc = reader.read(is);

            Element root = doc.getRootElement();
            Iterator<Element> elementIterator = root.elementIterator();
            while (elementIterator.hasNext()) {
                Element ele = elementIterator.next();
                String id = ele.attributeValue(ID_ATTRIBUTE);
                String beanClassName = ele.attributeValue(CLASS_ATTRIBUTE);
                BeanDefinition bd = new GenericBeanDefinition(id, beanClassName);
                this.registry.registerBeanDefinition(id, bd);
            }
        } catch (Exception e) {
            throw new BeanDefinitionStoreException("IOException parsing XML document", e);
        }
    }
......
}
```
> 将参数修改为Resource

#### ApplicationContext ####

```java
public interface ApplicationContext extends BeanFactory {
}
```
> 用于封装读取和解析bean.xml操作的接口

##### ClassPathXmlApplicationContext #####

```java
public class ClassPathXmlApplicationContext implements ApplicationContext {

    private DefaultBeanFactory factory = null;

    public ClassPathXmlApplicationContext(String configFile) {
        factory = new DefaultBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(factory);
        Resource resource = new ClassPathResource(configFile);
        reader.loadBeanDefinition(resource);
    }

    @Override
    public Object getBean(String beanId) {
        return factory.getBean(beanId);
    }
}

```
> 从classpath下读取 bean.xml

##### FileSystemXmlApplicationContext #####

```java
public class FileSystemXmlApplicationContext implements ApplicationContext {

    private DefaultBeanFactory factory = null;

    public FileSystemXmlApplicationContext(String configFile) {
        factory = new DefaultBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(factory);
        Resource resource = new FileSystemResource(configFile);
        reader.loadBeanDefinition(resource);
    }

    @Override
    public Object getBean(String beanId) {
        return factory.getBean(beanId);
    }
}

```
> 从系统磁盘目录读取文件

##### ApplicationContextTest #####

```java
public class ApplicationContextTest {

    @Test
    public void testGetBeanFromClassPathContext() {
        ApplicationContext context = new ClassPathXmlApplicationContext("bean-v1.xml");
        NioCoderService nioCoderService = (NioCoderService) context.getBean("nioCoder");
        Assert.assertNotNull(nioCoderService);
    }

    @Test
    public void testGetBeanFromFileSystemContext() {
        ApplicationContext context = new FileSystemXmlApplicationContext("src/test/resources/bean-v1.xml");
        NioCoderService nioCoderService = (NioCoderService) context.getBean("nioCoder");
        Assert.assertNotNull(nioCoderService);
    }
}

```
> 测试ClassPathXmlApplicationContext和FileSystemXmlApplicationContext

#### 代码下载 ####

- github:[https://github.com/longfeizheng/small-spring/tree/20190916_BeanFactory_v2_3](https://github.com/longfeizheng/small-spring/tree/20190916_BeanFactory_v2_3)

### 优化2.0.2 ###

重复代码时万恶之源。在 `ClassPathXmlApplicationContext`和`FileSystemXmlApplicationContext`的 的构造方法中有冗余代码，因此我们可以考虑使用[模板方法](https://gof.quanke.name/%E6%A8%A1%E6%9D%BF%E6%96%B9%E6%B3%95%E6%A8%A1%E5%BC%8F-Template%20Method%20Pattern.html)来处理一下。类图如下：
[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/beanfactory_v2_4.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/beanfactory_v2_4.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/beanfactory_v2_4.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/beanfactory_v2_4.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-04.png")

#### AbstractApplicationContext ####

```java
public abstract class AbstractApplicationContext implements ApplicationContext {

    private DefaultBeanFactory factory = null;

    public AbstractApplicationContext(String configFile) {
        factory = new DefaultBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(factory);
        Resource resource = this.getResourceByPath(configFile);
        reader.loadBeanDefinition(resource);
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
}
```
> 新增AbstractApplicationContext抽象类，里面增加getResourceByPath抽象方法

##### ClassPathXmlApplicationContext #####

```java
public class ClassPathXmlApplicationContext extends AbstractApplicationContext {


    public ClassPathXmlApplicationContext(String configFile) {
        super(configFile);
    }

    @Override
    protected Resource getResourceByPath(String configFile) {
        return new ClassPathResource(configFile);
    }
}
```
> 返回ClassPathResource

##### FileSystemXmlApplicationContext #####

```java
public class FileSystemXmlApplicationContext extends AbstractApplicationContext {

    public FileSystemXmlApplicationContext(String configFile) {
        super(configFile);
    }

    @Override
    protected Resource getResourceByPath(String configFile) {
        return new FileSystemResource(configFile);
    }
}
```
> 返回FileSystemResource

#### 代码下载 ####

- github:[https://github.com/longfeizheng/small-spring/tree/20190916_BeanFactory_v2_4](https://github.com/longfeizheng/small-spring/tree/20190916_BeanFactory_v2_4)


### 优化3.0 ###

至此已经完成了增加`BeanDefinitionRegistry`实现接口单一职责，`ApplicationContext`封装`bean.xml`的解析和实例化。接下来我们来处理`scope`的问题。这里我们只是简单区分一下一个`bean`是否单例。为了实现接口细粒度化，我们新增`SingletonBeanRegistry`来区分一个bean是否单例。类图如下：

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/beanfactory_v2_5.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/beanfactory_v2_5.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/beanfactory_v2_5.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/beanfactory_v2_4.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-05.png")

#### BeanDefinition ####

```java
public interface BeanDefinition {

    /**
     * 单例
     */
    String SCOPE_SINGLETON = "singleton";
    /**
     * 多例
     */
    String SCOPE_PROTOTYPE = "prototype";
    /**
     * 默认为空即单例模式
     */
    String SCOPE_DEFAULT = "";

    /**
     * 是否为单例
     *
     * @return
     */
    boolean isSingleton();

    /**
     * 是否为多例
     *
     * @return
     */
    boolean isPrototype();

    /**
     * 获取scope配置
     *
     * @return
     */
    String getScope();

    /**
     * 设置scope
     *
     * @param scope
     */
    void setScope(String scope);

    /**
     * 获取bean.xml中 bean的全名 如 "com.niocoder.service.v1.NioCoderService"
     *
     * @return
     */
    String getBeanClassName();
}
```
> 单例多例为bean的属性，所以需要修该BeanDefinition

##### GenericBeanDefinition #####

```java
public class GenericBeanDefinition implements BeanDefinition {

    private String id;
    private String beanClassName;
    private boolean singleton = true;
    private boolean prototype = false;
    private String scope = SCOPE_DEFAULT;

    public GenericBeanDefinition(String id, String beanClassName) {
        this.id = id;
        this.beanClassName = beanClassName;
    }

    @Override
    public boolean isSingleton() {
        return this.singleton;
    }

    @Override
    public boolean isPrototype() {
        return this.prototype;
    }

    @Override
    public String getScope() {
        return scope;
    }

    @Override
    public void setScope(String scope) {
        this.scope = scope;
        this.singleton = SCOPE_SINGLETON.equals(scope) || SCOPE_DEFAULT.equals(scope);
        this.prototype = SCOPE_PROTOTYPE.equals(scope);
    }

    @Override
    public String getBeanClassName() {
        return this.beanClassName;
    }
}
```
> 修改BeanDefinition的实现

#### XmlBeanDefinitionReader ####

```java
public class XmlBeanDefinitionReader {
    public void loadBeanDefinition(Resource resource) {
      ......
                if (ele.attribute(SCOPE_ATTRIBUTE) != null) {
                    bd.setScope(ele.attributeValue(SCOPE_ATTRIBUTE));
                }
                this.registry.registerBeanDefinition(id, bd);
           ......
    }
}
```
> 如果bean.xml中配置scope属性则设置scope

#### SingletonBeanRegistry ####

```java
public interface SingletonBeanRegistry {

    /**
     * singlebean 注册
     *
     * @param beanName
     * @param singletonObject
     */
    void registerSingleton(String beanName, Object singletonObject);


    /**
     * 获取singlebean
     *
     * @param beanName
     * @return
     */
    Object getSingleton(String beanName);
}
```
> 表示bean 为singleton的接口

##### DefaultSingletonBeanRegistry #####

```java
public class DefaultSingletonBeanRegistry implements SingletonBeanRegistry {

    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(64);

    @Override
    public void registerSingleton(String beanName, Object singletonObject) {

        Assert.notNull(beanName, "'beanName' must not be null");

        Object oldObject = this.singletonObjects.get(beanName);
        if (oldObject != null) {
            throw new IllegalStateException("Could not register object [" + singletonObject +
                    "] under bean name '" + beanName + "': there is already object [" + oldObject + "] bound");
        }
        this.singletonObjects.put(beanName, singletonObject);
    }

    @Override
    public Object getSingleton(String beanName) {
        return this.singletonObjects.get(beanName);
    }
}
```
>SingletonBeanRegistry的实现类

#### DefaultBeanFactory ####

```java
public class DefaultBeanFactory extends DefaultSingletonBeanRegistry implements BeanFactory, BeanDefinitionRegistry {
	......
	 @Override
    public Object getBean(String beanId) {

        BeanDefinition bd = this.getBeanDefinition(beanId);
        if(bd == null){
            return null;
        }

        if(bd.isSingleton()){
            Object bean = this.getSingleton(beanId);
            if(bean == null){
                bean = createBean(bd);
                this.registerSingleton(beanId, bean);
            }
            return bean;
        }
        return createBean(bd);
    }

    private Object createBean(BeanDefinition bd) {
        ClassLoader cl = ClassUtils.getDefaultClassLoader();
        String beanClassName = bd.getBeanClassName();
        try {
            Class<?> clz = cl.loadClass(beanClassName);
            // 使用反射创建bean的实例，需要对象存在默认的无参构造方法
            return clz.newInstance();
        } catch (Exception e) {
            throw new BeanCreationException("create bean for "+ beanClassName +" failed",e);
        }
    }
......
}
```
> getBean时判断是否为singleton

#### BeanFactoryTest ####

```java
public class BeanFactoryTest {
	......
	   /**
     * 测试获取bean
     */
    @Test
    public void testGetBean() {

        reader.loadBeanDefinition(new ClassPathResource("bean-v1.xml"));
        BeanDefinition bd = factory.getBeanDefinition("nioCoder");

        assertTrue(bd.isSingleton());
        assertFalse(bd.isPrototype());
        assertEquals(BeanDefinition.SCOPE_DEFAULT, bd.getScope());
        assertEquals("com.niocoder.service.v1.NioCoderService", bd.getBeanClassName());

        NioCoderService nioCoderService = (NioCoderService) factory.getBean("nioCoder");
        assertNotNull(nioCoderService);

        NioCoderService nioCoderService1 = (NioCoderService) factory.getBean("nioCoder");
        assertTrue(nioCoderService.equals(nioCoderService1));
    }
	......
}
```
> 测试默认的bean getBean时判断是否为singleton

#### 代码下载 ####

- github:[https://github.com/longfeizheng/small-spring/tree/20190916_BeanFactory_v2_5](https://github.com/longfeizheng/small-spring/tree/20190916_BeanFactory_v2_5)

## 代码下载 ##

- github:[https://github.com/longfeizheng/small-spring](https://github.com/longfeizheng/small-spring)
