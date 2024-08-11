# 性能测试

基于 JMH 完成 RPC 框架的性能测试

- JMH（Java Microbenchmark Harness）是一个用于编写、运行和分析 Java 微基准测试的工具，主要由 OpenJDK 项目维护。它帮助开发者对 Java 代码进行精确的性能测试，以评估代码的性能表现。



## 1. 测试原理

1. JVM 预热：JMH 在正式基准测试之前会进行多轮预热，使 JVM 能够完成 JIT 编译和优化。这是为了避免冷启动和即时编译对测试结果的影响。
2. 多次迭代：JMH 通过多次迭代运行基准测试，以获得更稳定和准确的结果。每次迭代的结果都会被记录并用于计算统计数据。
3. 隔离环境：JMH 提供了多种隔离级别，如线程隔离、进程隔离等，以确保基准测试的环境尽可能干净，不受外界因素干扰。
4. 多种测试模式：JMH 支持多种测试模式，如吞吐量测试（每单位时间内完成的操作数）、平均时间测试（每次操作的平均时间）、抽样时间测试（随机抽样操作时间）等，以满足不同的测试需求。
5. 精准计时：JMH 使用高精度计时器和计时方法，确保测试结果的准确性。它采用了系统提供的高精度计时器，并对计时过程中的误差进行了校正和补偿。



## 2. 测试流程

- 通过编写具体的测试类来进行性能测试，项目中的测试类如下：

```java
@BenchmarkMode({Mode.All})
@Warmup(iterations = 3, time = 5)
// 测量次数,每次测量的持续时间
@Measurement(iterations = 3, time = 5)
@Threads(1000)
@Fork(1)
@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.SECONDS)
@Slf4j
public class RpcEvaluation {

    private final HelloController helloController;

    static {
        // 初始化时设置 NettyRpcClient 和 RpcResponseHandler 的日志类级别为 OFF，及关闭日志打印
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger clientLogger = loggerContext.getLogger(NettyRpcClient.class);
        clientLogger.setLevel(ch.qos.logback.classic.Level.OFF);
        Logger handlerLogger = loggerContext.getLogger(RpcResponseHandler.class);
        handlerLogger.setLevel(Level.OFF);
    }

    public RpcEvaluation() {
        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(BenchmarkAnnotationConfig.class);
        helloController = context.getBean("helloController", HelloController.class);
    }

    @Benchmark
    public void testSayHello() {
        helloController.hello("liujue");
    }

    public static void main(String[] args) throws RunnerException {
        log.info("测试开始");
        Options opt = new OptionsBuilder()
                .include(RpcEvaluation.class.getSimpleName())
                // 可以通过注解注入
                // .warmupIterations(3)
                // .warmupTime(TimeValue.seconds(10))
                // 报告输出
                .result("result.json")
                // 报告格式
                .resultFormat(ResultFormatType.JSON).build();
        new Runner(opt).run();
    }
}
```



## 3. 结果组成

1. `Benchmark`：表示基准测试的名称，通常是测试类名和方法名的组合。它指明了正在运行的具体基准测试。
2. `Mode`：表示基准测试的模式，JMH 支持多种基准测试模式，包括：

  - `Throughput`：表示吞吐量模式，度量每单位时间内完成的操作数。
  - `AverageTime`：表示平均时间模式，度量每次操作的平均时间。
  - `SampleTime`：表示采样时间模式，度量每次操作的时间分布。
  - `SingleShotTime`：表示单次调用模式，度量单次操作的时间，适用于冷启动测试。
  - `All`：表示所有模式，综合以上所有模式的度量。

1. `Cnt(Count)`：表示测量的迭代次数。每次迭代都是一次独立的基准测试运行，JMH 会取多次运行的平均值来提高结果的可靠性。
2. `Score`：这个字段表示基准测试的度量值，根据不同的模式，含义会有所不同：

  - 在 `Throughput` 模式下，`Score` 表示每秒完成的操作数（如操作/秒）。
  - 在 `AverageTime` 模式下，`Score` 表示每次操作的平均时间（如毫秒/操作）。
  - 在 `SampleTime` 模式下，`Score` 表示采样的时间值（如毫秒）。
  - 在 `SingleShotTime` 模式下，`Score` 表示单次操作的时间（如毫秒）。

1. `Error`：这个字段表示基准测试结果的误差范围。JMH 使用标准误差（Standard Error）来表示结果的波动范围，通常使用 `±` 符号表示。这有助于了解结果的精确度。
2. `Units`：这个字段表示度量值的单位，通常是以下几种：

  - `ops/s`（操作/秒）：用于吞吐量模式，表示每秒完成的操作数。
  - `ms/op`（毫秒/操作）：用于平均时间模式，表示每次操作的平均时间。
  - `us/op`（微秒/操作）：用于采样时间模式，表示每次操作的时间。
  - `ns/op`（纳秒/操作）：同样用于时间模式，表示每次操作的时间。



结果分析：

- `Score` 值越高，说明吞吐量越大，框架的性能也就越高。
- `Error` 值越低，说明结果的误差率越低，框架的可靠性也就越高。



## 4. 测试结果

调用过程由5000个线程发起：

版本一：初始版本

```
Benchmark                                          Mode     Cnt      Score       Error  Units
RpcEvaluation.testSayHello                        thrpt       3  54224.871 ± 57992.521  ops/s
RpcEvaluation.testSayHello                         avgt       3      0.532 ±     6.159   s/op
RpcEvaluation.testSayHello                       sample  757439      0.382 ±     0.002   s/op
RpcEvaluation.testSayHello:testSayHello·p0.00    sample              0.003               s/op
RpcEvaluation.testSayHello:testSayHello·p0.50    sample              0.318               s/op
RpcEvaluation.testSayHello:testSayHello·p0.90    sample              0.387               s/op
RpcEvaluation.testSayHello:testSayHello·p0.95    sample              0.840               s/op
RpcEvaluation.testSayHello:testSayHello·p0.99    sample              2.282               s/op
RpcEvaluation.testSayHello:testSayHello·p0.999   sample              2.470               s/op
RpcEvaluation.testSayHello:testSayHello·p0.9999  sample              2.496               s/op
RpcEvaluation.testSayHello:testSayHello·p1.00    sample              2.508               s/op
RpcEvaluation.testSayHello                           ss       3      0.118 ±     0.051   s/op
```



版本二：增强服务容灾能力

```
Benchmark                                          Mode     Cnt      Score       Error  Units
RpcEvaluation.testSayHello                        thrpt       3  24581.573 ±  4471.318  ops/s
RpcEvaluation.testSayHello                         avgt       3      0.532 ±     6.159   s/op
RpcEvaluation.testSayHello                       sample  305972      0.382 ±     0.002   s/op
RpcEvaluation.testSayHello:testSayHello·p0.00    sample              0.003               s/op
RpcEvaluation.testSayHello:testSayHello·p0.50    sample              0.318               s/op
RpcEvaluation.testSayHello:testSayHello·p0.90    sample              0.387               s/op
RpcEvaluation.testSayHello:testSayHello·p0.95    sample              0.840               s/op
RpcEvaluation.testSayHello:testSayHello·p0.99    sample              2.282               s/op
RpcEvaluation.testSayHello:testSayHello·p0.999   sample              2.470               s/op
RpcEvaluation.testSayHello:testSayHello·p0.9999  sample              2.496               s/op
RpcEvaluation.testSayHello:testSayHello·p1.00    sample              2.508               s/op
RpcEvaluation.testSayHello                           ss       3      0.118 ±     0.051   s/op
```



`Dubbo2.7.14` 下 5000 个线程发起调用：

```
Benchmark                                       Mode     Cnt      Score      Error  Units
StressTest.testSayHello                        thrpt       3  41549.866 ± 9703.455  ops/s
StressTest.testSayHello                         avgt       3      0.119 ±    0.034   s/op
StressTest.testSayHello                       sample  611821      0.123 ±    0.001   s/op
StressTest.testSayHello:testSayHello·p0.00    sample              0.042              s/op
StressTest.testSayHello:testSayHello·p0.50    sample              0.119              s/op
StressTest.testSayHello:testSayHello·p0.90    sample              0.129              s/op
StressTest.testSayHello:testSayHello·p0.95    sample              0.139              s/op
StressTest.testSayHello:testSayHello·p0.99    sample              0.195              s/op
StressTest.testSayHello:testSayHello·p0.999   sample              0.446              s/op
StressTest.testSayHello:testSayHello·p0.9999  sample              0.455              s/op
StressTest.testSayHello:testSayHello·p1.00    sample              0.456              s/op
StressTest.testSayHello                           ss       3      0.058 ±    0.135   s/op
```

