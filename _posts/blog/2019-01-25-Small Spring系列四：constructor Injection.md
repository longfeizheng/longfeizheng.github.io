---
layout: post
title: Small Spring系列四：constructor Injection
categories: Spring
description: Spring
keywords: Spring
---

> 纤云弄巧，飞星传恨，银汉迢迢暗度。金风玉露一相逢，便胜却人间无数。

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-04.jpg](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-04.jpg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-04.jpg")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-04.jpg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-04.jpg")

## 概述 ##
·
上一章我们已经实现了`setter` 注入,具体实现如下

1. 新增`PropertyValue`类来表达`<property>`标签内容
2. 新增`BeanDefinitionValueResolver`来区分`<property>`中的`ref`属性和`value`属性
3. 使用`jdk`的`PropertyEditorSupport`用于类型转换,因为`xml`都是字符串类型字面值
4. 新增`TypeConverter`封装一些列类型转换器

`spring`配置依赖注入有三种方式,`setter`注入、`constructor`注入和注解注入。我们上一章已实现`setter`注入，本章继续实现`constructor`注入。

## ConstructorArgument

新增`ConstructorArgument`来表达`constructor-arg`标签,类图如下

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/constructor_v1.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/constructor_v1.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/constructor_v1.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/constructor_v1.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/constructor_v1.png")
> 可结合上一章的类图一起看下

#### bean.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans>
    <bean id="nioCoder"
          class="com.niocoder.service.v3.NioCoderService">
        <constructor-arg ref="accountDao"/>
        <constructor-arg ref="itemDao"/>
        <constructor-arg value="1"/>
    </bean>

    <bean id="accountDao"
          class="com.niocoder.dao.v3.AccountDao">
    </bean>

    <bean id="itemDao"
          class="com.niocoder.dao.v3.ItemDao">
    </bean>
</beans>
```
> 增加constructor-arg标签

#### ConstructorArgument

用来表达`constructor-arg`标签的类，注意`ValueHolder`是其静态内部类

```java
/**
 * 描述bean的constructor-arg的所有属性 集合 如  <constructor-arg ref="accountDao"/>
 *
 * @author zhenglongfei
 */
public class ConstructorArgument {

    private final List<ValueHolder> argumentValues = new LinkedList<ValueHolder>();

    public ConstructorArgument() {
    }

    public void addArgumentValue(ValueHolder valueHolder) {
        this.argumentValues.add(valueHolder);
    }

    public List<ValueHolder> getArgumentValues() {
        return Collections.unmodifiableList(this.argumentValues);
    }

    public boolean isEmpty() {
        return this.argumentValues.isEmpty();
    }

    public int getArgumentCount() {
        return this.argumentValues.size();
    }

    public void clear() {
        this.argumentValues.clear();
    }

    /**
     * 对应每一个<constructor-arg ref="accountDao"/>标签内容
     */
    public static class ValueHolder {

        private Object value;

        private String type;

        private String name;

        public ValueHolder(Object value) {
            this.value = value;
        }

        public ValueHolder(Object value, String type) {
            this.value = value;
            this.type = type;
        }


        public ValueHolder(Object value, String type, String name) {
            this.value = value;
            this.type = type;
            this.name = name;
        }


        public void setValue(Object value) {
            this.value = value;
        }


        public Object getValue() {
            return this.value;
        }


        public void setType(String type) {
            this.type = type;
        }


        public String getType() {
            return this.type;
        }

        public void setName(String name) {
            this.name = name;
        }


        public String getName() {
            return this.name;
        }
    }
}
```
> 描述 constructor-arg的所有属性

#### BeanDefinition

```java
public interface BeanDefinition {
......
 /**
     * 获取bean.xml 中的 constructor-arg 标签的内容 <constructor-arg ref="accountDao"/>
     *
     * @return
     */
    ConstructorArgument getConstructorArgument();

    /**
     * 获取bean 的 id
     *
     * @return
     */
    String getId();

    /**
     * 判断bean是否有构造参数
     *
     * @return
     */
    boolean hasConstructorArgumentValues();
}

```
> 增加getConstructorArgument用于获取constructor-arg

#### GenericBeanDefinition

```java
public class GenericBeanDefinition implements BeanDefinition{
......
	    private ConstructorArgument constructorArgument = new ConstructorArgument();
		......
		@Override
    public ConstructorArgument getConstructorArgument() {
        return this.constructorArgument;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public boolean hasConstructorArgumentValues() {
        return !this.constructorArgument.isEmpty();
    }
}

```

#### XmlBeanDefinitionReader

```java
public class XmlBeanDefinitionReader{
    public static final String CONSTRUCTOR_ARG_ELEMENT = "constructor-arg";

    public static final String TYPE_ATTRIBUTE = "type";

	......

	    private void parseConstructorArgElements(Element ele, BeanDefinition bd) {
        Iterator iterator = ele.elementIterator(CONSTRUCTOR_ARG_ELEMENT);
        while (iterator.hasNext()) {
            Element element = (Element) iterator.next();
            parseConstructorArgElement(element, bd);
        }
    }

    private void parseConstructorArgElement(Element element, BeanDefinition bd) {
        String typeAttr = element.attributeValue(TYPE_ATTRIBUTE);
        String nameAttr = element.attributeValue(NAME_ATTRIBUTE);
        Object value = parsePropertyValue(element, bd, null);
        ConstructorArgument.ValueHolder valueHolder = new ConstructorArgument.ValueHolder(value);
        if (StringUtils.hasLength(typeAttr)) {
            valueHolder.setType(typeAttr);
        }
        if (StringUtils.hasLength(nameAttr)) {
            valueHolder.setName(nameAttr);
        }

        bd.getConstructorArgument().addArgumentValue(valueHolder);
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
}

```
> 解析constructor-arg

#### BeanDefinitionTestV3

```java
public class BeanDefinitionTestV3 {

    DefaultBeanFactory factory = null;
    XmlBeanDefinitionReader reader = null;

    @Before
    public void setUp() {
        factory = new DefaultBeanFactory();
        reader = new XmlBeanDefinitionReader(factory);
        reader.loadBeanDefinition(new ClassPathResource("bean-v3.xml"));
    }

    @Test
    public void testConstructorArgument() {

        BeanDefinition bd = factory.getBeanDefinition("nioCoder");
        Assert.assertEquals("com.niocoder.service.v3.NioCoderService", bd.getBeanClassName());

        ConstructorArgument args = bd.getConstructorArgument();
        List<ConstructorArgument.ValueHolder> valueHolders = args.getArgumentValues();

        Assert.assertEquals(3, valueHolders.size());

        RuntimeBeanReference ref1 = (RuntimeBeanReference) valueHolders.get(0).getValue();
        Assert.assertEquals("accountDao", ref1.getBeanName());

        RuntimeBeanReference ref2 = (RuntimeBeanReference) valueHolders.get(1).getValue();
        Assert.assertEquals("itemDao", ref2.getBeanName());

        TypedStringValue stringValue = (TypedStringValue) valueHolders.get(2).getValue();
        Assert.assertEquals("1", stringValue.getValue());
    }
}
```
> 测试constructor-arg是否能被正常解析

### ConstructorResolver

与上一章`BeanDefinitionValueResolver`对应，我们也需要实现构造器的 `ConstructorResolver`用于将解析出来的字符串转换成实例。

#### ConstructorResolver

```java
/**
 * 将<constructor-arg ref="accountDao"/>
 * accountDao 转换成实例bean
 *
 * @author zhenglongfei
 */
@Log
public class ConstructorResolver {

    private final DefaultBeanFactory factory;

    public ConstructorResolver(DefaultBeanFactory factory) {
        this.factory = factory;
    }

    public Object autowireConstructor(BeanDefinition bd) {
        Constructor<?> constructorToUse = null;
        Object[] argsToUse = null;

        Class beanClass = bd.getBeanClass();

        if (null == beanClass) {
            try {
                beanClass = ClassUtils.getDefaultClassLoader().loadClass(bd.getBeanClassName());
                bd.setBeanClass(beanClass);
            } catch (ClassNotFoundException e) {
                throw new BeanCreationException(bd.getId(), "Instantiation of bean failed, can't resolve class", e);
            }
        }
        Constructor[] candidates = beanClass.getConstructors();

        BeanDefinitionValueResolver valueResolver = new BeanDefinitionValueResolver(this.factory);

        ConstructorArgument cargs = bd.getConstructorArgument();
        SimpleTypeConverter typeConverter = new SimpleTypeConverter();

        for (int i = 0; i < candidates.length; i++) {
            Class[] parameterTypes = candidates[i].getParameterTypes();
            if (parameterTypes.length != cargs.getArgumentCount()) {
                continue;
            }
            argsToUse = new Object[parameterTypes.length];
            boolean result = this.valuesMatchTypes(parameterTypes, cargs.getArgumentValues(), argsToUse, valueResolver, typeConverter);
            if (result) {
                constructorToUse = candidates[i];
                break;
            }
        }

        //找不到一个合适的构造函数
        if (constructorToUse == null) {
            throw new BeanCreationException(bd.getId(), "can't find a apporiate constructor");
        }

        try {
            return constructorToUse.newInstance(argsToUse);
        } catch (Exception e) {
            throw new BeanCreationException(bd.getId(), "can't find a create instance using " + constructorToUse);
        }
    }

    private boolean valuesMatchTypes(Class[] parameterTypes, List<ConstructorArgument.ValueHolder> argumentValues, Object[] argsToUse, BeanDefinitionValueResolver valueResolver, SimpleTypeConverter typeConverter) {

        for (int i = 0; i < parameterTypes.length; i++) {
            ConstructorArgument.ValueHolder valueHolder = argumentValues.get(i);
            // 判断参数类型有可能是RuntimeBeanReference 也有可能是TypedStringValue
            Object originalValue = valueHolder.getValue();

            try {
                // 获取真正的值
                Object resolvedValue = valueResolver.resolveValueIfNecessary(originalValue);

                // 专程对应的参数类型 如 "1" Integer
                Object convertedValue = typeConverter.convertIfNecessary(resolvedValue, parameterTypes[i]);
                argsToUse[i] = convertedValue;
            } catch (Exception e) {
                log.info(e.getMessage());
                return false;
            }
        }
        return true;
    }
}
```
>找到最适配的构造方法，使用Constructor.newInstance(args)创建对象

#### DefaultBeanFactory

```java
public class DefaultBeanFactory extends DefaultSingletonBeanRegistry implements BeanFactory, BeanDefinitionRegistry {
    private Object instantiateBean(BeanDefinition bd) {
        if (bd.hasConstructorArgumentValues()) {
            ConstructorResolver resolver = new ConstructorResolver(this);
            return resolver.autowireConstructor(bd);
        } else {
            ClassLoader cl = ClassUtils.getDefaultClassLoader();
            String beanClassName = bd.getBeanClassName();
            try {
                Class<?> clz = cl.loadClass(beanClassName);
                bd.setBeanClass(clz);
                // 使用反射创建bean的实例，需要对象存在默认的无参构造方法
                return clz.newInstance();
            } catch (Exception e) {
                throw new BeanCreationException("create bean for " + beanClassName + " failed", e);
            }
        }
    }
	}
```
> 创建bean实例时判断有没有配置构造方法，如果有则使用ConstructorResolver创建bean
#### 代码下载 ####

- github:[https://github.com/longfeizheng/small-spring/tree/20190122_constructor_v1](https://github.com/longfeizheng/small-spring/tree/20190122_constructor_v1)


## 代码下载 ##

- github:[https://github.com/longfeizheng/small-spring](https://github.com/longfeizheng/small-spring)

## 参考资料 ##

---
[从零开始造Spring](https://mp.weixin.qq.com/s/gbvdwpPtQcjyaigRBDjd-Q)
