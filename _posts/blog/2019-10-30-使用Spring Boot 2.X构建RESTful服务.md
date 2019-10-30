---
layout: post
title: 使用Spring Boot 2.X构建RESTful服务
categories: Java
description: Java
keywords: Java
---

> 明月松间照，清泉石上流。

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot18.jpg](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot18.jpg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot18.jpg")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot18.jpg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot18.jpg")

## 概述

`Spring Boot`是由`Pivotal`团队提供的全新框架,其设计目的是用来简化`Spring`应用的创建、运行、调试、部署等。它大大减少了基于`Spring`开发的生产级应用程序的工作量。因此，开发人员能够真正专注于以业务为中心的功能。

本章我们将通过几个步骤演示如何使用`Spring Boo`t构建`RESTful`服务。我们将创建一个简单的客户服务`CRUD`（也就是创建，读取，更新，删除）客户记录和每个客户拥有的银行帐户。

### `Spring` `Initializr`

`Spring Initializr`是展开`Spring Boot`的第一步。它用于创建`Spring Boot`应用程序的项目结构。在开始`Spring Boot`之前，我们需要弄清项目结构并确定将配置文件，属性文件和静态文件保留在何处。打开基于Web的界面开始。如下图所示，填写字段，然后单击“生成项目”按钮。

- Group: com.howtodoinjava.rest
- Artifact: customerservice
- Name: customerservice
- Package Name: com.howtodoinjava.rest.customerservice
- Dependencies: Web, JPA, H2

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot19.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot19.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot19.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot19.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot19.png")
> `Spring Initializr`创建一个项目

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot20.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot20.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot20.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot20.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot20.png")
> 项目目录结构

如下所示的`POM`文件表示启动项目的依赖关系。在`Spring Boot`中，不同的启动程序项目代表不同的`Spring`模块，例如`MVC`，`ORM`等。开发人员主要要做的是在依赖项中添加启动程序项目，`Spring Boot`将管理可传递的依赖项和版本。

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>2.2.0.RELEASE</version>
		<relativePath/> <!-- lookup parent from repository -->
	</parent>
	<groupId>com.howtodoinjava</groupId>
	<artifactId>customerservice</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<name>customerservice</name>
	<description>Demo project for Spring Boot</description>

	<properties>
		<java.version>1.8</java.version>
	</properties>

	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-data-jpa</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>

		<dependency>
			<groupId>com.h2database</groupId>
			<artifactId>h2</artifactId>
			<scope>runtime</scope>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
			<exclusions>
				<exclusion>
					<groupId>org.junit.vintage</groupId>
					<artifactId>junit-vintage-engine</artifactId>
				</exclusion>
			</exclusions>
		</dependency>
	</dependencies>

	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
			</plugin>
		</plugins>
	</build>

</project>

```

如果我们运行`mvnw dependency：tree`命令，则底层依赖关系层次结构将如下所示

```text
[INFO] Scanning for projects...
[INFO] 
[INFO] ----------------< com.codespeaks.rest:customerservice >-----------------
[INFO] Building customerservice 0.0.1-SNAPSHOT
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- maven-dependency-plugin:3.0.2:tree (default-cli) @ customerservice ---
[INFO] com.codespeaks.rest:customerservice:jar:0.0.1-SNAPSHOT
[INFO] +- org.springframework.boot:spring-boot-starter-data-jpa:jar:2.0.6.RELEASE:compile
[INFO] |  +- org.springframework.boot:spring-boot-starter:jar:2.0.6.RELEASE:compile
[INFO] |  |  +- org.springframework.boot:spring-boot:jar:2.0.6.RELEASE:compile
[INFO] |  |  +- org.springframework.boot:spring-boot-autoconfigure:jar:2.0.6.RELEASE:compile
[INFO] |  |  +- org.springframework.boot:spring-boot-starter-logging:jar:2.0.6.RELEASE:compile
[INFO] |  |  |  +- ch.qos.logback:logback-classic:jar:1.2.3:compile
[INFO] |  |  |  |  \- ch.qos.logback:logback-core:jar:1.2.3:compile
[INFO] |  |  |  +- org.apache.logging.log4j:log4j-to-slf4j:jar:2.10.0:compile
[INFO] |  |  |  |  \- org.apache.logging.log4j:log4j-api:jar:2.10.0:compile
[INFO] |  |  |  \- org.slf4j:jul-to-slf4j:jar:1.7.25:compile
[INFO] |  |  +- javax.annotation:javax.annotation-api:jar:1.3.2:compile
[INFO] |  |  \- org.yaml:snakeyaml:jar:1.19:runtime
[INFO] |  +- org.springframework.boot:spring-boot-starter-aop:jar:2.0.6.RELEASE:compile
[INFO] |  |  +- org.springframework:spring-aop:jar:5.0.10.RELEASE:compile
[INFO] |  |  \- org.aspectj:aspectjweaver:jar:1.8.13:compile
[INFO] |  +- org.springframework.boot:spring-boot-starter-jdbc:jar:2.0.6.RELEASE:compile
[INFO] |  |  +- com.zaxxer:HikariCP:jar:2.7.9:compile
[INFO] |  |  \- org.springframework:spring-jdbc:jar:5.0.10.RELEASE:compile
[INFO] |  +- javax.transaction:javax.transaction-api:jar:1.2:compile
[INFO] |  +- org.hibernate:hibernate-core:jar:5.2.17.Final:compile
[INFO] |  |  +- org.jboss.logging:jboss-logging:jar:3.3.2.Final:compile
[INFO] |  |  +- org.hibernate.javax.persistence:hibernate-jpa-2.1-api:jar:1.0.2.Final:compile
[INFO] |  |  +- org.javassist:javassist:jar:3.22.0-GA:compile
[INFO] |  |  +- antlr:antlr:jar:2.7.7:compile
[INFO] |  |  +- org.jboss:jandex:jar:2.0.3.Final:compile
[INFO] |  |  +- com.fasterxml:classmate:jar:1.3.4:compile
[INFO] |  |  +- dom4j:dom4j:jar:1.6.1:compile
[INFO] |  |  \- org.hibernate.common:hibernate-commons-annotations:jar:5.0.1.Final:compile

.........omitted for brevity.................
```

### `Application` `Properties`

我们使用基于`YAML`（一种标记语言）的属性文件将配置属性定义为比`application.properties`更具可读性。

- spring:application:name=customer-service # application name
- spring:h2:console:enabled=true # enable embedded h2 console. We are using the in-memory database.
- spring:h2:console:path=/h2-console # Path at which the h2 console is available, we will use h2 console to check in memory data later on.
- spring:jpa:show-sql=true # enable logging of SQL statements.
- server:port=8088 # Server HTTP port.
- server:servlet:context-path=/restapi # the base URL of the RESTful services

```yaml
spring:
  application:
    name: customer-service
  h2:
    console:
      enabled: true
      path: /h2-console
  jpa:
    show-sql: true

server:
  port: 8088
  servlet:
    context-path: /restapi
```

### `Domain` 实体

在此示例中，我们定义`JPA`实体以展示以下`ER`图，其中`Customer`实体与`Account`实体具有一对多关系。`Account.CustomerId`是引用`Customer.CustomerId`的外键。

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot21.jpeg](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot21.jpeg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot21.jpeg")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot21.jpeg "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot21.jpeg")

使用以下注解将这些类表示为`JPA`实体

- `@Entity` 表示该类是一个实体类。
- `@Table` 表示此实体映射到的数据库表。
- `@Id` 表示实体的主键
- `@GeneratedValue` 表示生成主键的策略，默认策略是`AUTO`策略。
- `@Column` 表示实体属性的列映射。
- `@ManyToOne` 表示从帐户到客户的多对一个关系。此关系在本例中的实体`Account`上指定。
- `@JoinColumn` 表示外键列
- `@OnDelete` 在此示例中表示级联删除操作。删除客户实体后，其所有帐户将同时被删除。
- `@JsonIgnore` 表示在序列化结束反序列化期间JSON解析器将忽略的属性。

```java
package com.howtodoinjava.customerservice.domin;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Table(name="CUSTOMER")
@Entity
public class Customer implements Serializable{
	private static final long serialVersionUID = -6759774343110776659L;
	
	@Id
	@GeneratedValue
	@Column(name="CUSTOMERID",updatable = false)
	private Integer customerId;
	
	@Column(name="NAME")
	private String customerName;
	
	@Column(name="DATEOFBIRTH" ,nullable=true)
	private LocalDate dateofBirth;
	
	@Column(name="PHONENUMBER")
	private String phoneNumber;

	public Integer getCustomerId() {
		return customerId;
	}

	public void setCustomerId(Integer customerId) {
		this.customerId = customerId;
	}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public LocalDate getDateofBirth() {
		return dateofBirth;
	}

	public void setDateofBirth(LocalDate dateofBirth) {
		this.dateofBirth = dateofBirth;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

}
```

```java
package com.howtodoinjava.customerservice.domin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Table(name="ACCOUNT")
@Entity
public class Account implements Serializable {
	
	@Id
	@GeneratedValue
	@Column(name="ACCOUNTNUMBER",updatable = false)
	private Integer accountNumber;
	
	@Column(name="ACCOUNTNAME")
	private String accountName;

	@Column(name="BALANCE")
	private BigDecimal balance;
	
	@Column(name="OPENINGDATE")
	private LocalDate openingDate;
	
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CUSTOMERID", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private Customer customer;
	
	private static final long serialVersionUID = -6380749575516426900L;

	public Integer getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(Integer accountNumber) {
		this.accountNumber = accountNumber;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}

	public LocalDate getOpeningDate() {
		return openingDate;
	}

	public void setOpeningDate(LocalDate openingDate) {
		this.openingDate = openingDate;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

}
```

### `Repositories`

`Spring Data JPA`在关系数据库之上抽象了持久层，并大大减少了`CRUD`操作和分页上的重复代码。通过扩展`JPA`实体及其主键类型的`JPARepository`接口,`Spring Data`将检测该接口并在运行时自动创建实现。可从继承中轻松获得的`CRUD`方法可以立即解决大多数数据访问用例。

```java
package com.howtodoinjava.customerservice.repository;

import com.howtodoinjava.customerservice.domin.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
}
```

使用J`PARepository`，我们还可以通过定义接口方法来创建自定义查询。`Spring Data JPA`从方法名称派生查询，并在运行时实现查询逻辑。`findByCustomerCustomerId`方法接受`Pageable`类型的参数`pageable`，并返`Account`类的的`Page`对象。

```java
package com.howtodoinjava.customerservice.repository;

import com.howtodoinjava.customerservice.domin.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Integer> {
	Page<Account> findByCustomerCustomerId(Integer customerId, Pageable pageable);
}

```

### `RESTful` 控制器

在`Spring MVC`(`Model`-`View`-`Controller`)中使用`@Controller`注解的控制器合并了业务逻辑和视图之间的数据流。在大多数情况下，控制器方法返回`ModelAndView`对象以呈现视图。但有时控制器方法返回的值会以`JSON/XML`格式显示给用户，而不是`HTML`页面。要实现这一点，可以使用注释`@ResponseBody`并自动将返回的值序列化为`JSON/XML`，然后将其保存到`HTTP`响应体中。`annotation` `@RestController`结合了前面的注释，为创建`RESTful`控制器提供了更多的便利。

注解`@GetMapping`，`@PostMapping`，`@PutMapping`和`@DeleteMapping`比其前身`@RequestMapping`更具`HTTP`请求特定性，前者`@RequestMapping`需要通过方法变量单独表示`HTTP`请求方法。

这分别是与客户和帐户相关的操作的两个控制器类。

```java
package com.howtodoinjava.customerservice.controller;

import com.howtodoinjava.customerservice.domin.Customer;
import com.howtodoinjava.customerservice.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/customers")
public class CustomerController {

    @Autowired
    private CustomerRepository customerRepository;

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public Customer save(@RequestBody Customer customer) {
        return customerRepository.save(customer);
    }

    @GetMapping
    public Page<Customer> all(Pageable pageable) {
        return customerRepository.findAll(pageable);

    }

    @GetMapping(value = "/{customerId}")
    public Customer findByCustomerId(@PathVariable Integer customerId) {
        return customerRepository.findById(customerId).orElseThrow(() -> new RuntimeException("Customer [customerId=" + customerId + "] can't be found"));
    }

    @DeleteMapping(value = "/{customerId}")
    public ResponseEntity<?> deleteCustomer(@PathVariable Integer customerId) {

        return customerRepository.findById(customerId).map(customer -> {
                    customerRepository.delete(customer);
                    return ResponseEntity.ok().build();
                }
        ).orElseThrow(() -> new RuntimeException("Customer [customerId=" + customerId + "] can't be found"));

    }

    @PutMapping(value = "/{customerId}")
    public ResponseEntity<Customer> updateCustomer(@PathVariable Integer customerId, @RequestBody Customer newCustomer) {

        return customerRepository.findById(customerId).map(customer -> {
            customer.setCustomerName(newCustomer.getCustomerName());
            customer.setDateofBirth(newCustomer.getDateofBirth());
            customer.setPhoneNumber(newCustomer.getPhoneNumber());
            customerRepository.save(customer);
            return ResponseEntity.ok(customer);
        }).orElseThrow(() -> new RuntimeException("Customer [customerId=" + customerId + "] can't be found"));

    }

}
```

```java
package com.howtodoinjava.customerservice.controller;

import com.howtodoinjava.customerservice.domin.Account;
import com.howtodoinjava.customerservice.domin.Customer;
import com.howtodoinjava.customerservice.repository.AccountRepository;
import com.howtodoinjava.customerservice.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/customers")
public class AccountController {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AccountRepository accountRepository;

    @PostMapping(value = "/{customerId}/accounts")
    @ResponseStatus(code = HttpStatus.CREATED)
    public Account save(@PathVariable Integer customerId, @RequestBody Account account) {
        return customerRepository.findById(customerId).map(customer -> {
            account.setCustomer(customer);
            return accountRepository.save(account);

        }).orElseThrow(() -> new RuntimeException("Customer [customerId=" + customerId + "] can't be found"));

    }

    @GetMapping(value = "/{customerId}/accounts")
    public Page<Account> all(@PathVariable Integer customerId, Pageable pageable) {
        return accountRepository.findByCustomerCustomerId(customerId, pageable);
    }

    @DeleteMapping(value = "/{customerId}/accounts/{accountId}")
    public ResponseEntity<?> deleteAccount(@PathVariable Integer customerId, @PathVariable Integer accountId) {

        if (!customerRepository.existsById(customerId)) {
            throw new RuntimeException("Customer [customerId=" + customerId + "] can't be found");
        }

        return accountRepository.findById(accountId).map(account -> {
            accountRepository.delete(account);
            return ResponseEntity.ok().build();
        }).orElseThrow(() -> new RuntimeException("Account [accountId=" + accountId + "] can't be found"));

    }

    @PutMapping(value = "/{customerId}/accounts/{accountId}")
    public ResponseEntity<Account> updateAccount(@PathVariable Integer customerId, @PathVariable Integer accountId, @RequestBody Account newAccount) {

        Customer customer = customerRepository.findById(customerId).orElseThrow(() -> new RuntimeException("Customer [customerId=" + customerId + "] can't be found"));

        return accountRepository.findById(accountId).map(account -> {
            newAccount.setCustomer(customer);
            accountRepository.save(newAccount);
            return ResponseEntity.ok(newAccount);
        }).orElseThrow(() -> new RuntimeException("Account [accountId=" + accountId + "] can't be found"));


    }

}
```

在前面的控制器类中，我们如下定义了许多`RESTful URI`

- `/customers` `HTTP Get` # 获得所有客户
- `/customers` `HTTP Post` # 创建新客户
- `/customers/{customerId}` `HTTP Get` # 获得一个客户
- `/customers/{customerId}` `HTTP Delete` # 删除客户
- `/customers/{customerId}` `HTTP Put` # 更新客户 
- `/customers/{customerId}/accounts` `HTTP Post` # 为客户创建一个帐户
- `/customers/{customerId}/accounts` `HTTP Get` # 根据客户获取帐户
- `/customers/{customerId}/accounts/{accountId}` `HTTP Delete` # 根据客户删除账户
- `/customers/{customerId}/accounts/{accountId}` `HTTP Put` # 根据客户更新帐户

在关于`REST`风格的`API`设计指导原则，它超出了本文的范围。互联网上有一些不错的文章，大家可以自行查看。

### 测试

可以在`Github`上找`到RESTful`服务示例。如果你对`Linux curl`命令不满意，我们可以通过简单地导入`Postman`集合文件来使用`Postman`调用`RESTful`服务。

检查数据库中的数据，通过`http://localhos:8088/restapi/h2-console/`访问`H2`控制台，并提供以下详细信息。

```text
Driver Class:  org.h2.Driver
JDBC URL:      jdbc:h2:mem:testdb
User Name:     sa
Password:      <blank>
```
[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot22.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot22.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot22.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot22.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot22.png")

[![https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot23.png](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot23.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot23.png")](https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot23.png "https://raw.githubusercontent.com/longfeizheng/longfeizheng.github.io/master/images/springboot/springboot23.png")

## 总结
 
`Spring Boot`并不与`Spring`框架存在竞争。恰恰相反，它使`Spring`更容易使用。在`starter`项目中，`Spring Boot`管理依赖项，使我们不必进行耗时且容易出错的依赖项管理，尤其是在应用程序复杂性增加的情况下。此外，`Spring Boot`通过检查类路径为我们执行自动配置。例如，如果`JPA`实现出现在类路径中，则`Spring Boot`将配置`DataSource`，`TransactionManager`和`EntityManagerFactory`等。
同时，覆盖`Spring Boot`为我们所做的配置非常简单。

上述代码都可以在[customerservice-RESTful](https://github.com/longfeizheng/customerservice-RESTful)上找到

---
[![https://niocoder.com/assets/images/qrcode.jpg](https://niocoder.com/assets/images/qrcode.jpg "https://niocoder.com/assets/images/qrcode.jpg")](https://niocoder.com/assets/images/qrcode.jpg "https://niocoder.com/assets/images/qrcode.jpg")



> 🙂🙂🙂关注微信公众号**java干货**
不定期分享干货资料


原文链接：[Build RESTful Services with Spring Boot 2.X in Few Steps](https://medium.com/@codespeaks/build-restful-services-with-spring-boot-2-x-in-few-steps-95c895a7abf5)
