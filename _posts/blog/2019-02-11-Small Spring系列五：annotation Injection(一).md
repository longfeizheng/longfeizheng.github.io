---
layout: post
title: Small Spring系列五：annotation Injection(一)
categories: Spring
description: Spring
keywords: Spring
---

> What a sweet burden！A joyful sorrow！

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-05.jpg](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-05.jpg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-05.jpg")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-05.jpg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/small-spring-05.jpg")

## 概述 ##

前两章我们已经实现了`setter`注入和`constructor`注入，本章我们来继续实现`annotation`注入。

思路如下:
1. 读取`xml`文件
2. 对指定`base-package`进行扫描,找到对应那些标记为`@Component`的类，创建`BeanDefinition`
  - 把`base-package`下面的`class`变成`Resource`
  - 使用`ASM`读取`Resource`中的注解信息
  - 创建`BeanDefinition`
3. 通过`BeanDefinition`创建`Bean`的实例，根据注解来注入


### 准备测试类

#### AccountDao

```java
package com.niocoder.dao.v4;

import com.niocoder.stereotype.Component;

@Component
public class AccountDao {
}
```

#### ItemDao

```java
package com.niocoder.dao.v4;

import com.niocoder.stereotype.Component;

@Component
public class ItemDao {
}
```

#### NioCoderService

```java
package com.niocoder.service.v4;

import com.niocoder.beans.factory.Autowired;
import com.niocoder.dao.v4.AccountDao;
import com.niocoder.dao.v4.ItemDao;
import com.niocoder.stereotype.Component;

/**
 * 测试注解
 */
@Component(value = "nioCoder")
public class NioCoderService {

    @Autowired
    private AccountDao accountDao;

    @Autowired
    private ItemDao itemDao;

    public AccountDao getAccountDao() {
        return accountDao;
    }

    public ItemDao getItemDao() {
        return itemDao;
    }
}
```

### bean-v4.xml

增加命名空间避免元素冲突，详情参考[链接](http://www.w3school.com.cn/xml/xml_namespaces.asp)
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!-- 增加namespace-->
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:context="http://www.springframework.org/schema/context">

    <!-- 扫描哪个包下面的文件 -->
    <context:component-scan base-package="com.niocoder.dao.v4,com.niocoder.service.v4">

    </context:component-scan>

</beans>
```
### Component和Autowired注解
`Component`注解主要用于类名上，表明该类为一个注册到容器中的`bean`。`Autowired`注解主要用于属性和方法(构造方法,`setter`方法)上，表明该类初始化时会自动将对应的属性注入。

#### Component

```java
package com.niocoder.stereotype;

import java.lang.annotation.*;

/**
 * Component 注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Component {

    String value() default "";
}
```

#### Autowired

```java
package com.niocoder.beans.factory;

import java.lang.annotation.*;

/**
 * Autowired注解
 */
@Target({ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Autowired {

    boolean required() default false;
}
```

### PackageResourceLoader

把`base-package`下面的`class`变成`Resource`

#### PackageResourceLoader

```java
/**
 * 将一个目录下的类文件加载为资源文件
 *
 * @author zhenglongfei
 */
@Log
public class PackageResourceLoader {

    /**
     * 给定一个包的路径,将包下面的文件转换为Resource
     *
     * @param basePackage
     * @return
     * @throws IOException
     */
    public Resource[] getResource(String basePackage) throws IOException {

        Assert.notNull(basePackage, "basepackage must not be null");
        String location = ClassUtils.convertClassNameToResourcePath(basePackage);
        URL url = ClassUtils.getDefaultClassLoader().getResource(location);
        File rootDir = new File(url.getFile());

        Set<File> matchingFiles = retrieveMatchingFiles(rootDir);
        Resource[] resources = new Resource[matchingFiles.size()];
        int i = 0;
        for (File file : matchingFiles) {
            resources[i++] = new FileSystemResource(file);
        }
        return resources;
    }

    private Set<File> retrieveMatchingFiles(File rootDir) throws IOException {
        if (!rootDir.exists()) {
            log.info("Skipping [" + rootDir.getAbsolutePath() + "] because is not exist");
        }

        if (!rootDir.isDirectory()) {
            log.info("Skipping [" + rootDir.getAbsolutePath() + " ] because it a Directory");
        }

        if (!rootDir.canRead()) {
            log.info("Cannot search for matching files underneath directory [" + rootDir.getAbsolutePath() +
                    "] because the application is not allowed to read the directory");
            return Collections.emptySet();
        }

        Set<File> result = new LinkedHashSet<File>(8);
        doRetrieveMatchingFiles(rootDir, result);
        return result;
    }

    protected void doRetrieveMatchingFiles(File dir, Set<File> result) throws IOException {

        File[] dirContents = dir.listFiles();
        if (dirContents == null) {
            log.info("Could not retrieve contents of directory [" + dir.getAbsolutePath() + "]");
            return;
        }
        for (File content : dirContents) {

            if (content.isDirectory()) {
                if (!content.canRead()) {
                    log.info("Skipping subdirectory [" + dir.getAbsolutePath() +
                            "] because the application is not allowed to read the directory");
                } else {
                    doRetrieveMatchingFiles(content, result);
                }
            } else {
                result.add(content);
            }

        }
    }
}
```
> 递归的读取一个指定的包目录转换为Resource

#### PackageResourcesLoaderTest

```java
public class PackageResourcesLoaderTest {

    @Test
    public void testGetResource() throws Exception {
        PackageResourceLoader loader = new PackageResourceLoader();
        Resource[] resource = loader.getResource("com.niocoder.dao.v4");
        Assert.assertEquals(2, resource.length);
    }
}
```
> 测试PackageResourceLoader

#### debug调试

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/annotation_v1.gif](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/annotation_v1.gif "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/annotation_v1.gif")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/annotation_v1.gif "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/spring/annotation_v1.gif")

#### 代码下载 ####

- github:[https://github.com/longfeizheng/small-spring/tree/20190128_annotation_v1](https://github.com/longfeizheng/small-spring/tree/20190128_annotation_v1)

## 代码下载 ##

- github:[https://github.com/longfeizheng/small-spring](https://github.com/longfeizheng/small-spring)

## 参考资料 ##

---
[从零开始造Spring](https://mp.weixin.qq.com/s/gbvdwpPtQcjyaigRBDjd-Q)
