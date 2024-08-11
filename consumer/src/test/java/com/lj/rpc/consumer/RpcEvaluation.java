package com.lj.rpc.consumer;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.lj.rpc.client.handler.RpcResponseHandler;
import com.lj.rpc.client.transport.netty.NettyRpcClient;
import com.lj.rpc.consumer.controller.HelloController;
import com.lj.rpc.consumer.evaluation.BenchmarkAnnotationConfig;
import lombok.extern.slf4j.Slf4j;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.concurrent.TimeUnit;

/**
 * @author liujue
 * @version 1.0
 * @description 自定义 RPC 框架性能评估
 * @since 2024/7/17
 */
@BenchmarkMode({Mode.All})
@Warmup(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
// 测量次数, 每次测量的持续时间
@Measurement(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
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
