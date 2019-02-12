---
layout: post
title: Small Spring系列六：annotation Injection(二)
categories: Spring
description: Spring
keywords: Spring
---

> we never know, we just believe it.

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-06.jpg](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-06.jpg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-06.jpg")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-06.jpg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-06.jpg")

## 概述 ##

在<a href="http://niocoder.com/2019/02/11/Small-Spring%E7%B3%BB%E5%88%97%E4%BA%94-annotation-Injection(%E4%B8%80)/">Small Spring系列五：annotation Injection(一)</a>中，我们已经通过`PackageResourceLoader`将指定包下面的`class`文件转变为`Resource`资源。本章我们实现通过`ASM`读取`Resource`中的注解信息并创建`BeanDefinition`。关于`ASM`读取类信息可参考[链接](https://www.jb51.net/article/103709.htm),由于`spring`已经封装好读取操作，我们就不重复造轮子了。

### 修改pom.xml文件

在`pom.xml`中增加`spring-core-asm-3.2.18.RELEASE.jar`的依赖。

```xml
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-core-asm</artifactId>
            <version>3.2.18.RELEASE</version>
            <scope>system</scope>
            <systemPath>${project.basedir}/libs/spring-core-asm-3.2.18.RELEASE.jar</systemPath>
        </dependency>
```

### 使用ASM读取类信息

#### ClassMetadataReadingVisitor

读取class文件，获取类名，判断是否为接口，是否抽象，是否final,父类，实现接口数量等，继承`ClassVisitor`。关于`ClassVisitor`可参考[链接](https://my.oschina.net/ta8210/blog/163637)。

```java
package com.niocoder.core.type.classreading;

import com.niocoder.util.ClassUtils;
import org.springframework.asm.ClassVisitor;
import org.springframework.asm.Opcodes;
import org.springframework.asm.SpringAsmInfo;

/**
 * 读取class文件，获取类名，判断是否为接口，是否抽象，是否final,父类，实现接口数量等
 *
 * @author zhenglongfei
 */
public class ClassMetadataReadingVisitor extends ClassVisitor {

    private String className;

    private boolean isInterface;

    private boolean isAbstract;

    private boolean isFinal;

    private String superClassName;

    private String[] interfaces;

    public ClassMetadataReadingVisitor() {
        super(SpringAsmInfo.ASM_VERSION);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String supername, String[] interfaces) {
        this.className = ClassUtils.convertResourcePathToClassName(name);
        this.isInterface = ((access & Opcodes.ACC_INTERFACE) != 0);
        this.isAbstract = ((access & Opcodes.ACC_ABSTRACT) != 0);
        this.isFinal = ((access & Opcodes.ACC_FINAL) != 0);
        if (supername != null) {
            this.superClassName = ClassUtils.convertResourcePathToClassName(supername);
        }
        this.interfaces = new String[interfaces.length];
        for (int i = 0; i < interfaces.length; i++) {
            this.interfaces[i] = ClassUtils.convertResourcePathToClassName(interfaces[i]);
        }
    }

    public String getClassName() {
        return className;
    }

    public boolean isInterface() {
        return isInterface;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public boolean hasSuperClass() {
        return this.superClassName != null;
    }

    public String getSuperClassName() {
        return superClassName;
    }

    public String[] getInterfacesNames() {
        return this.interfaces;
    }

    public String[] getInterfaces() {
        return interfaces;
    }
}

```
> 注意 ClassVisitor引用的包名，是org.springframework.asm.ClassVisitor。

#### AnnotationMetadataReadingVisitor

读取class文件，注解信息如:`@Component`

```java
package com.niocoder.core.type.classreading;

import com.niocoder.core.annotation.AnnotationAttributes;
import jdk.internal.org.objectweb.asm.Type;
import org.springframework.asm.AnnotationVisitor;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * 读取class文件，注解信息
 *
 * @author zhenglongfei
 */
public class AnnotationMetadataReadingVisitor extends ClassMetadataReadingVisitor {

    /**
     * com.niocoder.stereotype.Component
     */
    private final Set<String> annotationSet = new LinkedHashSet<>(4);
    /**
     * com.niocoder.stereotype.Component
     * AnnotationAttributes  value->nioCoder
     */
    private final Map<String, AnnotationAttributes> attributeMap = new LinkedHashMap<>(4);

    public AnnotationMetadataReadingVisitor() {

    }


    @Override
    public AnnotationVisitor visitAnnotation(final String desc, boolean visible) {
        String className = Type.getType(desc).getClassName();
        this.annotationSet.add(className);
        return new AnnotationAttributesReadingVisitor(className, this.attributeMap);
    }

    public Set<String> getAnnotationTypes() {
        return this.annotationSet;
    }

    public boolean hasAnnotation(String annotationType) {
        return this.annotationSet.contains(annotationType);
    }

    public AnnotationAttributes getAnnotationAttributes(String annotationType) {
        return this.attributeMap.get(annotationType);
    }
}
```
> 注意，该类继承了ClassMetadataReadingVisitor,并且在访问注解信息时构建了AnnotationAttributesReadingVisitor

#### AnnotationAttributesReadingVisitor

读取注解的属性信息,如:`@Component(value = "nioCoder")`,使用`AnnotationAttributes`保存

```java
package com.niocoder.core.type.classreading;

import com.niocoder.core.annotation.AnnotationAttributes;
import org.springframework.asm.AnnotationVisitor;
import org.springframework.asm.SpringAsmInfo;

import java.util.Map;

/**
 * 注解中定义的方法信息
 *
 * @author zhenglongfei
 */
public class AnnotationAttributesReadingVisitor extends AnnotationVisitor {

    private final String annotationType;

    private final Map<String, AnnotationAttributes> attributesMap;

    AnnotationAttributes attributes = new AnnotationAttributes();

    public AnnotationAttributesReadingVisitor(
            String annotationType, Map<String, AnnotationAttributes> attributesMap) {
        super(SpringAsmInfo.ASM_VERSION);

        this.annotationType = annotationType;
        this.attributesMap = attributesMap;

    }

    @Override
    public final void visitEnd() {
        this.attributesMap.put(this.annotationType, this.attributes);
    }

    @Override
    public void visit(String attributeName, Object attributeValue) {
        this.attributes.put(attributeName, attributeValue);
    }
}
```
> 该类继承了AnnotationVisitor

#### AnnotationAttributes

扩展了`LinkedHashMap`，保存注解属性信息，如:`@Component(value = "nioCoder")`

```java
package com.niocoder.core.annotation;

import com.niocoder.util.Assert;

import java.util.LinkedHashMap;

import static java.lang.String.format;

/**
 * 扩展LinkedHashMap 记录注解信息
 *
 * @author zhenglongfei
 */
public class AnnotationAttributes extends LinkedHashMap<String, Object> {

    public AnnotationAttributes() {
    }

    public String getString(String attributeName) {
        return doGet(attributeName, String.class);
    }

    @SuppressWarnings("unchecked")
    private <T> T doGet(String attributeName, Class<T> expectedType) {

        Object value = this.get(attributeName);
        Assert.notNull(value, format("Attribute '%s' not found", attributeName));
        return (T) value;
    }
}

```

#### ClassReaderTest

测试类，测试上述读取类和注解的信息

```java
public class ClassReaderTest {

    @Test
    public void testGetClassMetaData() throws Exception {
        ClassPathResource resource = new ClassPathResource("com/niocoder/service/v4/NioCoderService.class");
        ClassReader reader = new ClassReader(resource.getInputStream());

        ClassMetadataReadingVisitor visitor = new ClassMetadataReadingVisitor();
        reader.accept(visitor, ClassReader.SKIP_DEBUG);

        Assert.assertFalse(visitor.isAbstract());
        Assert.assertFalse(visitor.isInterface());
        Assert.assertFalse(visitor.isFinal());
        Assert.assertEquals("com.niocoder.service.v4.NioCoderService", visitor.getClassName());
        Assert.assertEquals("java.lang.Object", visitor.getSuperClassName());
        Assert.assertEquals(0, visitor.getInterfaces().length);
    }

    @Test
    public void testGetAnnotation() throws Exception {
        ClassPathResource resource = new ClassPathResource("com/niocoder/service/v4/NioCoderService.class");
        ClassReader reader = new ClassReader(resource.getInputStream());

        AnnotationMetadataReadingVisitor visitor = new AnnotationMetadataReadingVisitor();

        reader.accept(visitor,ClassReader.SKIP_DEBUG);

        String annotation = "com.niocoder.stereotype.Component";
        Assert.assertTrue(visitor.hasAnnotation(annotation));

        AnnotationAttributes attributes = visitor.getAnnotationAttributes(annotation);

        Assert.assertEquals("nioCoder",attributes.get("value"));
    }
}
```
#### 类图

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/annotation_v2_1.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/annotation_v2_1.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/annotation_v2_1.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/annotation_v2_1.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/annotation_v2_1.png")

#### 时序图

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/annotation_v2.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/annotation_v2.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/annotation_v2.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/annotation_v2.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/annotation_v2.png")

#### 代码下载

- github:[https://github.com/longfeizheng/small-spring/tree/20190128_annotation_v2](https://github.com/longfeizheng/small-spring/tree/20190128_annotation_v2)

### 优化ASM读取类信息

我们现在想要读取类信息和注解信息需要使用`ClassReader`和`AnnotationMetadataReadingVisitor`,`ClassReader`还是在`ASM`包下。这样使用起来不太方便，耦合性很高。因此我们准备在`spring`的基础上再次封装一下，类图如下:

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/annotation_v2_2.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/annotation_v2_2.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/annotation_v2_2.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/annotation_v2_2.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/annotation_v2_2.png")
> 封装MetadataReader接口封装读取类信息和注解信息的操作，默认实现类为SimpleMetadataReader

#### MetadataReader

封装读取类信息和注解信息的所有操作

```java
package com.niocoder.core.type.classreading;

import com.niocoder.core.io.Resource;
import com.niocoder.core.type.AnnotationMetadata;
import com.niocoder.core.type.ClassMetadata;

/**
 * 封装 ClassMetadataReadingVisitor和AnnotationMetadataReadingVisitor
 *
 * @author zhenglongfei
 */
public interface MetadataReader {

    /**
     * 获取资源信息
     *
     * @return
     */
    Resource getResource();

    /**
     * 获取类信息
     *
     * @return
     */
    ClassMetadata getClassMetadata();

    /**
     * 获取注解信息
     *
     * @return
     */
    AnnotationMetadata getAnnotationMetadata();
}
```

#### ClassMetadata

封装获取类信息的接口由`ClassMetadataReadingVisitor`实现

```java
package com.niocoder.core.type;

/**
 * 封装获取类信息的接口
 *
 * @author zhenglongfei
 */
public interface ClassMetadata {

    /**
     * 获取类名称
     *
     * @return
     */
    String getClassName();

    /**
     * 判断是否是接口
     *
     * @return
     */
    boolean isInterface();

    /**
     * 判断是否抽象
     *
     * @return
     */
    boolean isAbstract();

    /**
     * 判断是否类是否final
     *
     * @return
     */
    boolean isFinal();

    /**
     * 是否父类
     *
     * @return
     */
    boolean hasSuperClass();

    /**
     * 获取父类名称
     *
     * @return
     */
    String getSuperClassName();

    /**
     * 获取所有的接口名称
     *
     * @return
     */
    String[] getInterfaceNames();
}

```

#### AnnotationMetadata

封装获取注解信息的接口，继承`ClassMetadata`，由`AnnotationMetadataReadingVisitor`负责实现

```java
package com.niocoder.core.type;

import com.niocoder.core.annotation.AnnotationAttributes;

import java.util.Set;

/**
 * 封装获取接口信息的接口
 *
 * @author zhenglongfei
 */
public interface AnnotationMetadata extends ClassMetadata {

    /**
     * 获取注解信息
     *
     * @return
     */
    Set<String> getAnnotationTypes();

    /**
     * 判断是否有该注解信息
     *
     * @param annotationType
     * @return
     */
    boolean hasAnnotation(String annotationType);

    /**
     * 获取注解的内容信息
     *
     * @param annotationType
     * @return
     */
    AnnotationAttributes getAnnotationAttributes(String annotationType);
}

```

#### SimpleMetadataReader

实现了`MetadataReader`

```java
package com.niocoder.core.type.classreading;

import com.niocoder.core.io.Resource;
import com.niocoder.core.type.AnnotationMetadata;
import com.niocoder.core.type.ClassMetadata;
import org.springframework.asm.ClassReader;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 实现MetadataReader
 *
 * @author zhenglongfei
 */
public class SimpleMetadataReader implements MetadataReader {

    private final Resource resource;

    private final ClassMetadata classMetadata;

    private final AnnotationMetadata annotationMetadata;

    public SimpleMetadataReader(Resource resource) throws IOException {
        InputStream is = new BufferedInputStream(resource.getInputStream());
        org.springframework.asm.ClassReader classReader;

        try {
            classReader = new ClassReader(is);
        } finally {
            is.close();
        }

        AnnotationMetadataReadingVisitor visitor = new AnnotationMetadataReadingVisitor();
        classReader.accept(visitor, ClassReader.SKIP_DEBUG);

        this.annotationMetadata = visitor;
        this.classMetadata = visitor;
        this.resource = resource;
    }

    @Override
    public Resource getResource() {
        return resource;
    }

    @Override
    public ClassMetadata getClassMetadata() {
        return classMetadata;
    }

    @Override
    public AnnotationMetadata getAnnotationMetadata() {
        return annotationMetadata;
    }
}

```

#### ClassMetadataReadingVisitor

实现`ClassMetadata`

```java
public class ClassMetadataReadingVisitor extends ClassVisitor implements ClassMetadata {
......
}
```

#### AnnotationMetadataReadingVisitor

实现`AnnotationMetadata`

```java
public class AnnotationMetadataReadingVisitor extends ClassMetadataReadingVisitor implements AnnotationMetadata {
......
}
```

#### 测试类MetadataReaderTest

```java
public class MetadataReaderTest {

    @Test
    public void testGetMetadata() throws Exception{
        ClassPathResource resource = new ClassPathResource("com/niocoder/service/v4/NioCoderService.class");

        MetadataReader reader = new SimpleMetadataReader(resource);
        AnnotationMetadata amd = reader.getAnnotationMetadata();

        String annotation = Component.class.getName();

        Assert.assertTrue(amd.hasAnnotation(annotation));
        AnnotationAttributes attributes = amd.getAnnotationAttributes(annotation);
        Assert.assertEquals("nioCoder",attributes.get("value"));

        Assert.assertFalse(amd.isAbstract());
        Assert.assertFalse(amd.isInterface());
        Assert.assertFalse(amd.isFinal());
    }
}
```

#### 代码下载

- github:[https://github.com/longfeizheng/small-spring/tree/20190129_annotation_v3](https://github.com/longfeizheng/small-spring/tree/20190129_annotation_v3)

### TODO


#### 代码下载 ####

- github:[https://github.com/longfeizheng/small-spring/tree/20190128_annotation_v1](https://github.com/longfeizheng/small-spring/tree/20190128_annotation_v1)

## 代码下载 ##

- github:[https://github.com/longfeizheng/small-spring](https://github.com/longfeizheng/small-spring)

## 参考资料 ##

---
[从零开始造Spring](https://mp.weixin.qq.com/s/gbvdwpPtQcjyaigRBDjd-Q)
