# lj-rpc-framework

## 项目简介

- 一款基于 Netty + Zookeeper + SpringBoot 实现的分布式场景下的自定义 RPC 网络通信框架。



## 基本内容

- [x] 基于 Netty 实现网络传输；
- [x] 自定义了消息协议和编解码器；
- [x] 使用 Zookeeper/Nacos 作为注册中心；
- [x] 实现了多种序列化算法，包括JDK，JSON，Hessian，ProtoStuff，Kryo等；
- [x] 实现了两种常用的动态代理方式，包括 JDK 和 CGlib；
- [x] 实现了三种常用的负载均衡策略，包括轮询，随机选择和一致性哈希；
- [x] 优化网络传输过程，包括解决黏包半包问题、添加心跳机制、复用 channel 以维持长连接；
- [x] 实现了服务信息的本地缓存与监听、服务失败重试机制等等；
- [x] 集成 Spring，自定义注解来辅助实现 RPC 组件扫描、服务注册、服务订阅等；
- [x] 集成 SpringBoot，完成自动配置，自定义 starter 模块，进一步简化配置；
- [x] 实现自定义的 SPI 机制以适应进一步扩展和偏好使用；
- [x] 添加了一系列服务容灾功能。



RPC 框架架构图：

![RPC-framework](https://liujue.oss-cn-hangzhou.aliyuncs.com/img/202408121432779.png)



## 快速开始

以`Zookeeper`作为注册中心为例：

1. 启动`Zookeeper` 注册中心。
2. 配置客户端和服务端:

- 创建两个 SpringBoot 项目 `consumer` 和 `provider`;
- 修改 `consumer` 和 `provider` 模块下的 `application.yaml` 的注册中心地址属性。

3. 启动客户端和服务端:
- 客户端和服务端都是基于 SpringBoot 构建，直接启动即可。

4. 发起远程调用: `http://{your_host}}/hello/{your_name}`



具体内容见：[快速开始](./docs/quickStart.md)



## 项目亮点

1. 项目对齐市面上的常用框架，集成 `Spring & SpringBoot` 来简化框架的使用；
2. 对整个网络通信过程进行了优化；
3. 为了保障服务调用效率，基于 Zookeeper 的监听功能配合缓存去优化服务注册和发现过程；
4. 为了提高框架的容灾能力，通过限流，熔断，重试等方式来保证服务调用的可靠性。



具体内容见：[项目亮点](./docs/highlight.md)



## 性能测试

单机环境下测试简单业务：

|            | MyRPC   | Dubbo 2.7.14 |
| ---------- | ------- | ------------ |
| 并发数     | 5000    | 5000         |
| TPS        | 24581   | 41549        |
| Error      | 4471    | 9703         |
| RTT        | 95% 8ms | 95% 50ms     |
| AVGTime/OP | 0.532   | 0.119        |
| OOM        | 无      | 无           |



具体测试内容见：[性能测试](./docs/benchmark.md)
