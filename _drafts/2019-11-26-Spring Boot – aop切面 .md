---
layout: post
title: Spring Boot – aop 示例
categories: SpringBoot
description: SpringBoot
keywords: SpringBoot
---

>落花人独立，微雨燕双飞。

a

## 概述 ##

学习在`Spring Boot`应用程序中实现`AOP`,并使用`AspectJ`添加不同的`AOP`支持夸业务管理，，例如日志记录，概要分析，缓存和事务管理。

### 使用`Spring Boot`设置`AOP`

#### `Maven`

在`spring boot`中设置`AOP`需要包含`spring-boot-start-aop`依赖项。它将`spring-aop`和`aspectjweaver`依赖项导入到应用程序中。
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>

```

#### 启用/禁用自动配置

导入以上依赖项会触发`AopAutoConfiguration`，它会使用`@EnableAspectJAutoProxy`注解启用`AOP`。

如果`application.properties`中的`spring.aop.auto = false`，则不会激活自动配置。

```yaml
#spring.aop.auto = false    //'false' disables the auto configuration
```

#### 使用`@Aspect`创建切面

可以在`Spring Boot`中使用注解`@Aspect`创建一个切面，并使用`@Component`注解在`bean`容器中注册。

在`aspect`类中，我们可以根据需要创建通知。例如，在`com.howtodoinjava.aop`包的所有类中，下面的类应用了关于方法的通知。它捕获方法的开始时间和结束时间，并将方法的总执行时间记录在日志文件中。

```java
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
 
@Aspect
@Component
public class LoggingAspect 
{
    private static final Logger LOGGER = LogManager.getLogger(LoggingAspect.class);
     
    @Around("execution(* com.howtodoinjava.aop..*(..)))")
    public Object profileAllMethods(ProceedingJoinPoint proceedingJoinPoint) throws Throwable 
    {
        MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();
          
        //Get intercepted method details
        String className = methodSignature.getDeclaringType().getSimpleName();
        String methodName = methodSignature.getName();
          
        final StopWatch stopWatch = new StopWatch();
          
        //Measure method execution time
        stopWatch.start();
        Object result = proceedingJoinPoint.proceed();
        stopWatch.stop();
  
        //Log method execution time
        LOGGER.info("Execution time of " + className + "." + methodName + " "
                            + ":: " + stopWatch.getTotalTimeMillis() + " ms");
  
        return result;
    }
}
```

#### 示例

创建一个简单的服务类来测试以上通知。

```java
@Service
public class DomainService 
{
    public Object getDomainObjectById(Long id)
    {
        try {
            Thread.sleep(new Random().nextInt(2000));
        } catch (InterruptedException e) {
            //do some logging
        }
        return id;
    }
}
```

创建一个测试类并执行给定的方法并注意日志。

```java
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
 
@RunWith(SpringRunner.class)
@SpringBootTest
public class AopSpringBootTest 
{
    @Autowired
    private DomainService service;
     
    @Test
    public void testGetDomainObjectById() 
    {
        service.getDomainObjectById(10L);
    }
}
```

输出

```text
2019-11-07T21:02:58.390+0530 INFO Execution time of DomainService.getDomainObjectById :: 1145 ms
```

如我们所见，`AOP`通知已应用于服务方法。

### 通知类型

`Aspectj` `AOP`中有五种`advice`。

1. `@Before` : 前置通知
2. `@AfterReturning` : 后置通知
3. `@AfterThrowing` : 异常通知
4. `@After` : 最终通知
5. `@Around` : 环绕通知

### 总结

用`Spring boot`实现`AOP`只需要很少的工作，只要添加`Spring -boot-start - AOP`依赖项，我们就可以添加切面。

每当我们想禁用`aop`自动配置时，我们都可以通过切换配置文件为`false`。

创建切面和通知就像添加一些注解一样简单，例如`@ Aspect`，`@ Around`等

原文链接：[AOP with Spring Boot](https://howtodoinjava.com/spring-boot2/aop-aspectj/)
