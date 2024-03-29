package com.lj.rpc.client.proxy;

import com.lj.rpc.client.common.RequestMetaData;
import com.lj.rpc.client.config.RpcClientProperties;
import com.lj.rpc.client.transport.RpcClient;
import com.lj.rpc.core.common.RpcRequest;
import com.lj.rpc.core.common.RpcResponse;
import com.lj.rpc.core.common.ServiceMessage;
import com.lj.rpc.core.discovery.ServiceDiscovery;
import com.lj.rpc.core.enums.SerializerType;
import com.lj.rpc.core.exception.RpcException;
import com.lj.rpc.core.protocol.MessageHeader;
import com.lj.rpc.core.protocol.RpcMessage;

import java.lang.reflect.Method;

/**
 *
 * <p>
 * 将网络传输的细节封装在此类中, 统一客户端需要的操作
 * <p/>
 *
 * @author liujue
 * @version 1.0
 * @date 2024/1/30 10:04
 */
public class RemoteMethodCall {

    /**
     * 发起 rpc 远程方法调用的公共方法
     *
     * @param serviceName      服务名称
     * @param method           调用的方法
     * @param args             方法参数
     * @param serviceDiscovery 服务发现中心
     * @param client           发起调用的客户端
     * @return 方法调用返回结果
     */
    public static Object remoteCall(String serviceName, Method method, Object[] args,
                                    ServiceDiscovery serviceDiscovery, RpcClient client, RpcClientProperties properties) {
        // 1. 请求: 构造 requestRpcMessage = header + request
        // 唯一需要预先指定是序列化算法类型, 也就是说需要传递一个东西能得到用户指定的序列化算法
        MessageHeader header = MessageHeader.build(properties.getSerialization());

        RpcRequest request = new RpcRequest();
        request.setServiceName(serviceName);
        request.setMethod(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setParameterValues(args);

        RpcMessage requestRpcMessage = new RpcMessage();
        requestRpcMessage.setMessageHeader(header);
        requestRpcMessage.setBody(request);

        // 2. 服务发现, 得到服务端信息
        ServiceMessage serviceMessage = serviceDiscovery.discovery(request);
        if (serviceMessage == null) {
            throw new RpcException(String.format("The service [%s] was not found in the remote registry center.",
                    serviceName));
        }

        // 3. 网络传输, 发送请求
        RequestMetaData requestMetaData = new RequestMetaData();
        requestMetaData.setRpcMessage(requestRpcMessage);
        requestMetaData.setServerAddr(serviceMessage.getInetAddress());
        requestMetaData.setPort(serviceMessage.getPort());

        RpcMessage responseRpcMessage = client.sendRequest(requestMetaData);

        // 4. 接受响应: 拿到 RpcResponse
        if (responseRpcMessage == null) {
            throw new RpcException("Remote procedure call timeout.");
        }
        RpcResponse response = (RpcResponse) responseRpcMessage.getBody();

        // 5. 返回给客户端结果 returnValue
        if (response.getExceptionValue() != null) {
            throw new RpcException(response.getExceptionValue());
        }
        return response.getReturnValue();
    }
}
