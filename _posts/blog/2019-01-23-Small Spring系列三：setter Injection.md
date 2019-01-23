---
layout: post
title: Small Spring系列三：setter Injection
categories: Spring
description: Spring
keywords: Spring
---

> 不知何处雨，已觉此间凉。

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-03.jpg](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-03.jpg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-03.jpg")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-03.jpg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-03.jpg")

## 概述 ##

本章我们来实`spring`的`setter`注入。`bean-v2.xml`
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans>
  <bean id = "nioCoder"
      class = "com.niocoder.service.v2.NioCoderService">
      <property name ="accountDao" ref="accountDao"></property>
      <property name ="itemDao" ref="itemDao"></property>
      <property name ="url" value="http://niocoder.com/"></property>
  </bean>

  <bean id="accountDao"
        class="com.niocoder.dao.v2.AccountDao">
  </bean>

  <bean id="itemDao"
        class="com.niocoder.dao.v2.ItemDao">
  </bean>
</beans>
```

我们用`BeanDefinition`表达了`<bean>`标签中的 `id`和`class`属性，对应的`<property>`标签该如何表达呢？还有`<property>`里面的`ref`和`value`？

新增`PropertyValue`类来表示`property`标签内容，类图如下:

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/setter_v1.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/setter_v1.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/setter_v1.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/setter_v1.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/setter_v1.png")
> ref 由RuntimeBeanReference表示，value由TypedStringValue表示


### 增加PropertyValue

#### PropertyValue

```java
/**
 * 描述bean的property属性 如  <property name ="accountDao" ref="accountDao"></property>
 * @author zhenglongfei
 */
public class PropertyValue {

    /**
     * property name ="accountDao"
     */
    private final String name;

    /**
     * property ref="accountDao"
     */
    private final Object value;

    /**
     * 表示是否转换
     */
    private Boolean converted = false;

    /**
     * 转换后的实体类 ref="accountDao" 对应的 new AccountDao();
     */
    private Object convertedValue;

    public PropertyValue(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    public synchronized boolean isConverted() {
        return this.converted;
    }

    public synchronized Object getConvertedValue() {
        return convertedValue;
    }

    public synchronized void setConvertedValue(Object convertedValue) {
        this.convertedValue = convertedValue;
    }
}
```
> 表示<property>标签

#### RuntimeBeanReference

```java
/**
 * <property name ="accountDao" ref="accountDao"></property>
 * ref="accountDao"
 * 表明引用的是bean 获取是会转换成实例
 *
 * @author zhenglongfei
 */
public class RuntimeBeanReference {

    private final String beanName;

    public RuntimeBeanReference(String beanName) {
        this.beanName = beanName;
    }

    public String getBeanName() {
        return beanName;
    }
}

```
> <property>标签中的ref属性

#### TypedStringValue

```java

/**
 * <property name ="url" value="http://niocoder.com/"></property>
 * value="http://niocoder.com/"
 * 表示 引用的字符串 无需转换
 *
 * @author zhenglongfei
 */
public class TypedStringValue {

    private final String value;

    public TypedStringValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

```
> <property>标签中的value属性

#### BeanDefinition
```java
public interface BeanDefinition {
	......
	/**
     * 获取bean.xmo 中的 property 标签内容 <property name ="accountDao" ref="accountDao"></property>
     * @return
     */
    List<PropertyValue> getPropertyValues();
}
> 增加getPropertyValues方法，用于获取property标签
```

#### GenericBeanDefinition

```java
public class GenericBeanDefinition implements BeanDefinition {
	......
	 List<PropertyValue> propertyValues = new ArrayList<>();
	 ......
	  @Override
    public List<PropertyValue> getPropertyValues() {
        return this.propertyValues;
    }
}

```
> 实现BeanDefinition

#### XmlBeanDefinitionReader

```java
@Log
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
                if (ele.attribute(SCOPE_ATTRIBUTE) != null) {
                    bd.setScope(ele.attributeValue(SCOPE_ATTRIBUTE));
                }
				// 解析property标签
                parsePropertyElement(ele, bd);
                this.registry.registerBeanDefinition(id, bd);
            }
        } catch (Exception e) {
            throw new BeanDefinitionStoreException("IOException parsing XML document", e);
        }
    }

	private void parsePropertyElement(Element ele, BeanDefinition bd) {
        Iterator iterator = ele.elementIterator(PROPERTY_ELEMENT);
        while (iterator.hasNext()) {
            Element propElem = (Element) iterator.next();
            String propertyName = propElem.attributeValue(NAME_ATTRIBUTE);
            if (!StringUtils.hasLength(propertyName)) {
                log.info("Tag 'property' must have a 'name' attribute");
                return;
            }

            Object val = parsePropertyValue(propElem, bd, propertyName);
            PropertyValue pv = new PropertyValue(propertyName, val);
            bd.getPropertyValues().add(pv);
        }
    }

    private Object parsePropertyValue(Element propElem, BeanDefinition bd, String propertyName) {

        String elementName = (propertyName != null) ?
                "<property> element for property '" + propertyName + "'" :
                "<constructor-arg> element";

        boolean hasRefAttribute = (propElem.attribute(REF_ATTRIBUTE) != null);
        boolean hasValueAttribute = (propElem.attribute(VALUE_ATTRIBUTE) != null);

        if (hasRefAttribute) {
            String refName = propElem.attributeValue(REF_ATTRIBUTE);
            if (!StringUtils.hasText(refName)) {
                log.info(elementName + " contains empty 'ref' attribute");
            }
			// 表示是ref =""
            RuntimeBeanReference ref = new RuntimeBeanReference(refName);
            return ref;
        } else if (hasValueAttribute) {
		 	// 表示名value=""
            TypedStringValue valueHolder = new TypedStringValue(propElem.attributeValue(VALUE_ATTRIBUTE));
            return valueHolder;
        } else {
            throw new RuntimeException(elementName + " must specify a ref or value");
        }
    }
}

```
> 解析property标签，区分ref属性和value属性

#### BeanDefinitionTestV2

```java
@Log
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
                if (ele.attribute(SCOPE_ATTRIBUTE) != null) {
                    bd.setScope(ele.attributeValue(SCOPE_ATTRIBUTE));
                }
				// 解析property标签
                parsePropertyElement(ele, bd);
                this.registry.registerBeanDefinition(id, bd);
            }
        } catch (Exception e) {
            throw new BeanDefinitionStoreException("IOException parsing XML document", e);
        }
    }

	private void parsePropertyElement(Element ele, BeanDefinition bd) {
        Iterator iterator = ele.elementIterator(PROPERTY_ELEMENT);
        while (iterator.hasNext()) {
            Element propElem = (Element) iterator.next();
            String propertyName = propElem.attributeValue(NAME_ATTRIBUTE);
            if (!StringUtils.hasLength(propertyName)) {
                log.info("Tag 'property' must have a 'name' attribute");
                return;
            }

            Object val = parsePropertyValue(propElem, bd, propertyName);
            PropertyValue pv = new PropertyValue(propertyName, val);
            bd.getPropertyValues().add(pv);
        }
    }

    private Object parsePropertyValue(Element propElem, BeanDefinition bd, String propertyName) {

        String elementName = (propertyName != null) ?
                "<property> element for property '" + propertyName + "'" :
                "<constructor-arg> element";

        boolean hasRefAttribute = (propElem.attribute(REF_ATTRIBUTE) != null);
        boolean hasValueAttribute = (propElem.attribute(VALUE_ATTRIBUTE) != null);

        if (hasRefAttribute) {
            String refName = propElem.attributeValue(REF_ATTRIBUTE);
            if (!StringUtils.hasText(refName)) {
                log.info(elementName + " contains empty 'ref' attribute");
            }
			// 表示是ref =""
            RuntimeBeanReference ref = new RuntimeBeanReference(refName);
            return ref;
        } else if (hasValueAttribute) {
		 	// 表示名value=""
            TypedStringValue valueHolder = new TypedStringValue(propElem.attributeValue(VALUE_ATTRIBUTE));
            return valueHolder;
        } else {
            throw new RuntimeException(elementName + " must specify a ref or value");
        }
    }
}

```
> 测试是否能解析 property标签

#### 代码下载 ####

- github:[https://github.com/longfeizheng/small-spring/tree/20190121_setter_injection_v1](https://github.com/longfeizheng/small-spring/tree/20190121_setter_injection_v1)

### BeanDefinitionValueResolver

虽然我们已经使用`PropertyValue`来表达`property`标签，并且也可以区分是`ref`和`value`属性，但并没有真正的获取`ref`对应`bean`的实例。

#### BeanDefinitionValueResolver

```java

/**
 * 将<property name ="accountDao" ref="accountDao"></property>
 * accountDao 转换成实例bean
 *
 * @author zhenglongfei
 */
public class BeanDefinitionValueResolver {
	// 含factory 因为factory有getBean方法
    private final DefaultBeanFactory factory;

    public BeanDefinitionValueResolver(DefaultBeanFactory factory) {
        this.factory = factory;
    }

    public Object resolveValueIfNecessary(Object value) {
		// 判断是ref 还是value
        if (value instanceof RuntimeBeanReference) {
            RuntimeBeanReference ref = (RuntimeBeanReference) value;
            String refName = ref.getBeanName();
            Object bean = this.factory.getBean(refName);
            return bean;
        } else if (value instanceof TypedStringValue) {
            return ((TypedStringValue) value).getValue();
        } else {
            // TODO
            throw new RuntimeException("the value " + value + " has not implemented");
        }
    }
}
```
> 根据property标签的ref和value返回对应的实例或字符串

#### DefaultBeanFactory

```java

public class DefaultBeanFactory extends DefaultSingletonBeanRegistry implements BeanFactory, BeanDefinitionRegistry {
	......
	 private Object createBean(BeanDefinition bd) {

        // 1. 创建实例
        Object bean = instantiateBean(bd);
        // 2. 设置属性
        populateBean(bd, bean);

        return bean;
    }

    private void populateBean(BeanDefinition bd, Object bean) {
        List<PropertyValue> pvs = bd.getPropertyValues();

        if (pvs == null || pvs.isEmpty()) {
            return;
        }

        BeanDefinitionValueResolver valueResolver = new BeanDefinitionValueResolver(this);

        try {
            for (PropertyValue pv : pvs) {
                String propertyName = pv.getName();
                Object originalValue = pv.getValue();
                Object resolvedValue = valueResolver.resolveValueIfNecessary(originalValue);
                pv.setConvertedValue(resolvedValue);
                // set 注入 使用java BeanInfo 实现
                BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass());
                PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
                for (PropertyDescriptor pd : pds) {
                    if (pd.getName().equals(propertyName)) {
                        pd.getWriteMethod().invoke(bean, resolvedValue);
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            throw new BeanCreationException("Failed to obtain BeanInfo for class [" + bd.getBeanClassName() + "]", ex);
        }
    }

    private Object instantiateBean(BeanDefinition bd) {
        ClassLoader cl = ClassUtils.getDefaultClassLoader();
        String beanClassName = bd.getBeanClassName();
        try {
            Class<?> clz = cl.loadClass(beanClassName);
            // 使用反射创建bean的实例，需要对象存在默认的无参构造方法
            return clz.newInstance();
        } catch (Exception e) {
            throw new BeanCreationException("create bean for " + beanClassName + " failed", e);
        }
    }
}
```
>创建完bean的实例之后使用java BeanInfo设置属性值

#### BeanDefinitionValueResolverTest2

```java

public class BeanDefinitionValueResolverTest2 {

    DefaultBeanFactory factory = null;
    XmlBeanDefinitionReader reader = null;
    BeanDefinitionValueResolver resolver = null;

    @Before
    public void setUp() {
        factory = new DefaultBeanFactory();
        reader = new XmlBeanDefinitionReader(factory);
        reader.loadBeanDefinition(new ClassPathResource("bean-v2.xml"));
        resolver = new BeanDefinitionValueResolver(factory);
    }

    @Test
    public void testResolveRuntimeBeanReference() {

        RuntimeBeanReference reference = new RuntimeBeanReference("accountDao");
        Object value = resolver.resolveValueIfNecessary(reference);

        Assert.assertNotNull(value);
        Assert.assertTrue(value instanceof AccountDao);
    }

    @Test
    public void testResolveTypedStringValue() {

        TypedStringValue stringValue = new TypedStringValue("http://niocoder.com/");
        Object value = resolver.resolveValueIfNecessary(stringValue);

        Assert.assertNotNull(value);
        Assert.assertEquals("http://niocoder.com/", value);
    }
}
```
>测试BeanDefinitionValueResolver

#### ApplicationContextTestV2

```java

public class ApplicationContextTestV2 {

    @Test
    public void testGetBeanProperty() {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("bean-v2.xml");
        NioCoderService nioCoderService = (NioCoderService) ctx.getBean("nioCoder");

        Assert.assertNotNull(nioCoderService.getAccountDao());
        Assert.assertNotNull(nioCoderService.getItemDao());
        Assert.assertNotNull(nioCoderService.getUrl());

        assertTrue(nioCoderService.getItemDao() instanceof ItemDao);
        assertTrue(nioCoderService.getAccountDao() instanceof AccountDao);
        assertEquals(nioCoderService.getUrl(), "http://niocoder.com/");

    }
}
```
> 测试setter 注入

#### 代码下载 ####

- github:[https://github.com/longfeizheng/small-spring/tree/20190121_setter_injection_v2](https://github.com/longfeizheng/small-spring/tree/20190121_setter_injection_v2)

### PropertyEditorSupport

如果你在`NioCoderService`中新增一个`Integer`类型的属性，你会发现上面的`setter`注入失败,这是为什么呢？

因为不管属性是任何类型（double,float,Integer等）在配置文件中都是对应的字符串类型的字面值,如果想要实现类型转换则需要使用`java.beans.PropertyEditor`的编辑器。

#### CustomNumberEditor

```java
public class CustomNumberEditor extends PropertyEditorSupport {

    private final Class<? extends Number> numberClass;

    private final NumberFormat numberFormat;

    private final boolean allowEmpty;

    public CustomNumberEditor(Class<? extends Number> numberClass, boolean allowEmpty) throws IllegalArgumentException {
        this(numberClass, null, allowEmpty);
    }

    public CustomNumberEditor(Class<? extends Number> numberClass,
                              NumberFormat numberFormat, boolean allowEmpty) throws IllegalArgumentException {

        if (numberClass == null || !Number.class.isAssignableFrom(numberClass)) {
            throw new IllegalArgumentException("Property class must be a subclass of Number");
        }
        this.numberClass = numberClass;
        this.numberFormat = numberFormat;
        this.allowEmpty = allowEmpty;
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (this.allowEmpty && !StringUtils.hasText(text)) {
            // Treat empty String as null value.
            setValue(null);
        } else if (this.numberFormat != null) {
            // Use given NumberFormat for parsing text.
            setValue(NumberUtils.parseNumber(text, this.numberClass, this.numberFormat));
        } else {
            // Use default valueOf methods for parsing text.
            setValue(NumberUtils.parseNumber(text, this.numberClass));
        }
    }

    @Override
    public String getAsText() {
        Object value = getValue();
        if (value == null) {
            return "";
        }
        if (this.numberFormat != null) {
            // Use NumberFormat for rendering value.
            return this.numberFormat.format(value);
        } else {
            // Use toString method for rendering value.
            return value.toString();
        }
    }

}
```
> Integer 类型转换器

#### CustomNumberEditorTest

```java
public class CustomNumberEditorTest {

    @Test
    public void testConvertString2Number() {

        CustomNumberEditor editor = new CustomNumberEditor(Integer.class, true);
        editor.setAsText("1");
        Object value = editor.getValue();
        Assert.assertTrue(value instanceof Integer);
        Assert.assertEquals(1, ((Integer) editor.getValue()).intValue());

        editor.setAsText("");
        Assert.assertTrue(editor.getValue() == null);

        try {
            editor.setAsText("3.1");
        } catch (IllegalArgumentException e) {
            return;
        }
        Assert.fail();
    }
}
```
>测试类

#### 代码下载 ####

- github:[https://github.com/longfeizheng/small-spring/tree/20190121_setter_injection_v3](https://github.com/longfeizheng/small-spring/tree/20190121_setter_injection_v3)

### 封装类型转换器

虽然已经实现了类型转换，但对应的每种基础类型都需要转换一下，因此我们封装一个类型转换款的方法。类图如下
[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/setter_v2.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/setter_v2.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/setter_v2.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/setter_v2.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/setter_v2.png")

#### TypeConverter

```java
/**
 * 封装类型转换的接口
 *
 * @author zhenglongfei
 * @see CustomBooleanEditor
 * @see CustomDateEditor
 * @see CustomNumberEditor
 */
public interface TypeConverter {

    /**
     * 用于类型转换
     *
     * @param value        bean.xml中value或者ref
     * @param requiredType 需要转换的类型
     * @param <T>
     * @return
     * @throws TypeMismatchException
     */
    <T> T convertIfNecessary(Object value, Class<T> requiredType) throws TypeMismatchException;
}
```
> 封装类型转换器接口

#### SimpleTypeConverter

```java
/**
 * TypeConverter 实现类
 *
 * @author zhenglongfei
 */
public class SimpleTypeConverter implements TypeConverter {

    private Map<Class<?>, PropertyEditor> defaultEditors;

    public SimpleTypeConverter() {
    }

    @Override
    public <T> T convertIfNecessary(Object value, Class<T> requiredType) throws TypeMismatchException {
        // 如果requiredType是对象或者字符串 直接返回
        if (ClassUtils.isAssignableValue(requiredType, value)) {
            return (T) value;
        } else {
            // 根据传入的requiredType选择对应的editor
            if (value instanceof String) {
                PropertyEditor editor = findDefaultEditor(requiredType);
                try {
                    editor.setAsText((String) value);
                } catch (IllegalArgumentException e) {
                    throw new TypeMismatchException(value, requiredType);
                }
                return (T) editor.getValue();
            } else {
                throw new RuntimeException("Todo : can't convert value for " + value + " class:" + requiredType);
            }
        }
    }

    private PropertyEditor findDefaultEditor(Class<?> requiredType) {
        PropertyEditor editor = this.getDefaultEditor(requiredType);
        if (editor == null) {
            throw new RuntimeException("Editor for " + requiredType + " has not been implemented");
        }
        return editor;
    }

    public PropertyEditor getDefaultEditor(Class<?> requiredType) {

        if (this.defaultEditors == null) {
            createDefaultEditors();
        }
        return this.defaultEditors.get(requiredType);
    }

    private void createDefaultEditors() {
        this.defaultEditors = new HashMap<Class<?>, PropertyEditor>(64);

        // Spring's CustomBooleanEditor accepts more flag values than the JDK's default editor.
        this.defaultEditors.put(boolean.class, new CustomBooleanEditor(false));
        this.defaultEditors.put(Boolean.class, new CustomBooleanEditor(true));
        this.defaultEditors.put(Date.class, new CustomDateEditor(true));

        this.defaultEditors.put(int.class, new CustomNumberEditor(Integer.class, false));
        this.defaultEditors.put(Integer.class, new CustomNumberEditor(Integer.class, true));
    }
}
```
> 实现类

#### bean-v2.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans>
    <bean id="nioCoder"
          class="com.niocoder.service.v2.NioCoderService">
        <property name="accountDao" ref="accountDao"></property>
        <property name="itemDao" ref="itemDao"></property>
        <property name="url" value="http://niocoder.com/"></property>
        <property name="birthday" value="2019-01-21"></property>
        <property name="flag" value="true"></property>
        <property name="version" value="1"></property>
    </bean>

    <bean id="accountDao"
          class="com.niocoder.dao.v2.AccountDao">
    </bean>

    <bean id="itemDao"
          class="com.niocoder.dao.v2.ItemDao">
    </bean>
</beans>

```
> 增加 date类型birthday boolean类型flag Integer类型version

#### DefaultBeanFactory

```java
public class DefaultBeanFactory extends DefaultSingletonBeanRegistry implements BeanFactory, BeanDefinitionRegistry {
	...
	private void populateBean(BeanDefinition bd, Object bean) {
        List<PropertyValue> pvs = bd.getPropertyValues();

        if (pvs == null || pvs.isEmpty()) {
            return;
        }

        // 处理 bean.xml 中的 ref 和 value 对应  RuntimeBeanReference TypedStringValue
        //        <property name="itemDao" ref="itemDao"></property>
        //        <property name="url" value="http://niocoder.com/"></property>
        BeanDefinitionValueResolver valueResolver = new BeanDefinitionValueResolver(this);
        // 处理bean.xml中的特殊类型 Integer Boolean Date 等
        //        <property name="birthday" value="2019-01-21"></property>
        //        <property name="flag" value="true"></property>
        //        <property name="version" value="1"></property>
        SimpleTypeConverter converter = new SimpleTypeConverter();
        try {
            for (PropertyValue pv : pvs) {
                String propertyName = pv.getName();
                Object originalValue = pv.getValue();
                Object resolvedValue = valueResolver.resolveValueIfNecessary(originalValue);
                pv.setConvertedValue(resolvedValue);
                pv.setConverted(true);
                // set 注入 使用java BeanInfo 实现
                BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass());
                PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
                for (PropertyDescriptor pd : pds) {
                    if (pd.getName().equals(propertyName)) {
                        // 类型转换
                        Object convertedValue = converter.convertIfNecessary(resolvedValue, pd.getPropertyType());
                        pd.getWriteMethod().invoke(bean, convertedValue);
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            throw new BeanCreationException("Failed to obtain BeanInfo for class [" + bd.getBeanClassName() + "]", ex);
        }
    }
	....
}
```
> 类型转换

#### TypeConverterTest

```java
public class TypeConverterTest {

    @Test
    public void testConvertStringToObject() {
        TypeConverter converter = new SimpleTypeConverter();

        // Integer
        {
            Integer integer = converter.convertIfNecessary("1", Integer.class);
            Assert.assertEquals(1, integer.intValue());

            try {
                converter.convertIfNecessary("3.1", Integer.class);
            } catch (TypeMismatchException e) {
                return;
            }
            fail();
        }

        // boolean
        {
            Boolean b = converter.convertIfNecessary("true", Boolean.class);
            Assert.assertEquals(b, b.booleanValue());

            try {
                converter.convertIfNecessary("xxxxxxxx", Integer.class);
            } catch (TypeMismatchException e) {
                return;
            }
            fail();
        }

        // date
        {
            Date birthday = converter.convertIfNecessary("2019-01-22", Date.class);
            Assert.assertTrue(birthday instanceof Date);

            try {
                converter.convertIfNecessary("20190122", Date.class);
            } catch (TypeMismatchException e) {
                return;
            }
            fail();
        }

        // AccountDao
        {
            AccountDao accountDao = converter.convertIfNecessary(new AccountDao(), AccountDao.class);
            Assert.assertNotNull(accountDao);

        }

    }
}
```

#### ApplicationContextTestV2

```java
public class ApplicationContextTestV2 {

    @Test
    public void testGetBeanProperty() {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("bean-v2.xml");
        NioCoderService nioCoderService = (NioCoderService) ctx.getBean("nioCoder");

        Assert.assertNotNull(nioCoderService.getAccountDao());
        Assert.assertNotNull(nioCoderService.getItemDao());
        Assert.assertNotNull(nioCoderService.getUrl());
        Assert.assertNotNull(nioCoderService.getBirthday());
        Assert.assertNotNull(nioCoderService.getFlag());
        Assert.assertNotNull(nioCoderService.getVersion());

        assertTrue(nioCoderService.getItemDao() instanceof ItemDao);
        assertTrue(nioCoderService.getAccountDao() instanceof AccountDao);
        assertEquals(nioCoderService.getUrl(), "http://niocoder.com/");
        assertTrue(nioCoderService.getBirthday() instanceof Date);
        assertTrue(nioCoderService.getFlag());
        assertEquals(nioCoderService.getVersion(), new Integer(1));

    }
}
```
> 校验

#### 代码下载 ####

- github:[https://github.com/longfeizheng/small-spring/tree/20190122_setter_injection_v4](https://github.com/longfeizheng/small-spring/tree/20190122_setter_injection_v4)


## 代码下载 ##

- github:[https://github.com/longfeizheng/small-spring](https://github.com/longfeizheng/small-spring)

## 参考资料 ##

---
[从零开始造Spring](https://mp.weixin.qq.com/s/gbvdwpPtQcjyaigRBDjd-Q)
