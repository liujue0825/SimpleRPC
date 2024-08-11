package com.lj.rpc.client.proxy;

import com.lj.rpc.client.entity.RequestMetaData;
import com.lj.rpc.client.config.RpcClientProperties;
import com.lj.rpc.client.retry.FailureRetry;
import com.lj.rpc.client.transport.RpcClient;
import com.lj.rpc.core.entity.RpcRequest;
import com.lj.rpc.core.entity.RpcResponse;
import com.lj.rpc.core.entity.ServiceMessage;
import com.lj.rpc.core.discovery.ServiceDiscovery;
import com.lj.rpc.core.exception.RpcException;
import com.lj.rpc.core.factory.FallbackFactory;
import com.lj.rpc.core.fallback.CircuitBreaker;
import com.lj.rpc.core.protocol.MessageHeader;
import com.lj.rpc.core.protocol.RpcMessage;

import java.lang.reflect.Method;

/**
 * 远程方法调用工具类
 *
 * <p>
 * 将网络传输的细节封装在此类中, 统一客户端需要的操作
 * </p>
 *
 * @author liujue
 * @version 1.0
 * @date 2024/1/30 10:04
 */
public class RemoteMethodCall {

    private RemoteMethodCall() {

    }

    /**
     * 发起 RPC 远程方法调用的公共方法
     *
     * @param serviceName      服务名称
     * @param method           调用的方法
     * @param args             方法参数
     * @param serviceDiscovery 服务发现中心
     * @param client           发起调用的客户端
     * @return 方法调用返回结果
     */
    public static Object remoteCall(String serviceName, Method method, Object[] args,
                                    ServiceDiscovery serviceDiscovery, RpcClient client,
                                    RpcClientProperties properties) {
        // 1. 请求: 构造 requestRpcMessage = header + body
        // 唯一需要预先指定是序列化算法类型, 也就是说需要传递一个东西能得到用户指定的序列化算法
        MessageHeader header = MessageHeader.build(properties.getSerialization());
        // 构造请求体
        RpcRequest request = new RpcRequest();
        request.setServiceName(serviceName);
        request.setMethod(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setParameterValues(args);
        // NOTE: 进行服务熔断情况判断
        // CircuitBreaker circuitBreaker = FallbackFactory.getCircuitBreaker(method.getName());
        // if (!circuitBreaker.allowRequest()) {
        //     throw new RpcException("The service triggers circuit breaker!");
        // }

        // 2. 服务发现, 得到服务端信息
        ServiceMessage serviceMessage = serviceDiscovery.discover(request);
        if (serviceMessage == null) {
            throw new RpcException(String.format("The service [%s] was not found in the remote registry center.",
                    serviceName));
        }

        // 3.1 构建通信协议信息
        RpcMessage requestRpcMessage = new RpcMessage();
        requestRpcMessage.setMessageHeader(header);
        requestRpcMessage.setBody(request);
        // 3.2 构建请求元数据
        RequestMetaData requestMetaData = RequestMetaData.builder()
                .rpcMessage(requestRpcMessage)
                .serverAddr(serviceMessage.getInetAddress())
                .port(serviceMessage.getPort())
                .timeout(properties.getTimeout())
                .build();

        // 4. 网络传输, 发送请求
        RpcMessage responseRpcMessage;
        // 4.1 正常发送
        responseRpcMessage = client.sendRequest(requestMetaData);
        // NOTE: 4.2 失败重试
        // if (responseRpcMessage == null && serviceDiscovery.checkRetry(serviceName)) {
        //     responseRpcMessage = new FailureRetry().retry(requestMetaData, client);
        // }

        // 5. 接受响应: 拿到 RpcResponse
        if (responseRpcMessage == null) {
            throw new RpcException("Remote procedure call failed.");
        }
        RpcResponse response = (RpcResponse) responseRpcMessage.getBody();

        // 6. 返回给客户端结果 returnValue
        if (response.getExceptionValue() != null) {
            throw new RpcException(response.getExceptionValue());
        }
        return response.getReturnValue();
    }
}
