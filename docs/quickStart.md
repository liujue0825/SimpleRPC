# 快速开始

一个简单的 Demo 作为演示。

## 1. 建立初始模块

客户端: 

```java
@SpringBootApplication
public class ConsumerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication.class, args);
    }
}
```



服务端: 

```java
@SpringBootApplication
public class ProviderApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProviderApplication.class, args);
    }
}
```

同时需要远程调用的接口为: 
```java
public interface HelloService {
    String sayHello(String name);
}
```



## 2. 引入依赖

客户端：

```xml
<dependencies>
   <dependency>
       <groupId>com.lj</groupId>
       <artifactId>rpc-client-spring-boot-starter</artifactId>
       <version>1.0-SNAPSHOT</version>
   </dependency>
   <dependency>
       <groupId>com.lj</groupId>
       <artifactId>interface</artifactId>
       <version>0.0.1-SNAPSHOT</version>
   </dependency>
   <dependency>
       <groupId>com.lj</groupId>
       <artifactId>rpc-framework-core</artifactId>
       <version>1.0-SNAPSHOT</version>
   </dependency>
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-web</artifactId>
   </dependency>
</dependencies>
```



服务端：

```xml
<dependencies>
   <dependency>
       <groupId>com.lj</groupId>
       <artifactId>rpc-server-spring-boot-starter</artifactId>
       <version>${rpc.version}</version>
   </dependency>
   <dependency>
       <groupId>com.lj</groupId>
       <artifactId>interface</artifactId>
       <version>0.0.1-SNAPSHOT</version>
   </dependency>
   <dependency>
       <groupId>com.lj</groupId>
       <artifactId>rpc-framework-core</artifactId>
       <version>${rpc.version}</version>
   </dependency>
</dependencies>
```



## 3. 进行配置

客户端：

```yaml
server:
  port: 8881
  
rpc:
  client:
    registry-addr: 
```

- `registry-addr `处需要填写 zookeeper 注册中心的 IP 地址，默认值为 `127.0.0.1:2181` 



服务端：

```yaml
rpc:
  server:
    registry-addr: 
```

- `registry-addr` 处需要填写 zookeeper 注册中心的 IP 地址，默认值为 `127.0.0.1:2181` 



## 4. 发起远程调用

服务端：

- 通过 `@RpcComponentScan` 注解扫描需要注册的服务所在包。

```java
@RpcComponentScan(basePackages = {"com.lj.rpc.provider"})
@SpringBootApplication
public class ProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProviderApplication.class, args);
    }
}
```

- 通过 `@RpcService` 注解注册服务

```java
@RpcService(interfaceClass = HelloService.class)
public class HelloServiceImpl implements HelloService {

    @Override
    public String sayHello(String name) {
        return "hello: " + name;
    }
}
```



客户端：

- 通过`@RpcReference`注解注入fu'wu'duan：

```java
@RestController
@RequestMapping
public class HelloController {
	@RpcReference
    private HelloService helloService;   
    
    @RequestMapping("/hello/{name}")
    public String hello(@PathVariable("name") String name) {
        return helloService.sayHello(name);
    }
}
```



启动服务，发起调用：

此时通过`localhost:8881/hello/zhangsan`观察浏览器响应，即可得到 `hello: zhangsan` 的响应。
