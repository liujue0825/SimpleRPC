package com.lj.rpc.core.loadbalance.impl;

import com.lj.rpc.core.entity.RpcRequest;
import com.lj.rpc.core.entity.ServiceMessage;
import com.lj.rpc.core.loadbalance.AbstractLoadBalance;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 一致性哈希负载均衡策略, 具体内容为：
 * <p>
 * 1. 映射 services 至 Hash 值区间中；
 * <p>
 * 2. 映射请求，然后找到大于请求 Hash 值的第一个 service。
 *
 * @author liujue
 * @version 1.0
 * @date 2024/1/30 12:43
 */
public class ConsistentHashLoadBalance extends AbstractLoadBalance {

    private final Map<String, ConsistentHashSelector> selectors = new ConcurrentHashMap<>();

    @Override
    protected ServiceMessage doSelect(List<ServiceMessage> invokers, RpcRequest request) {
        String method = request.getMethod();
        // key 格式: 接口名.方法名
        String key = invokers.get(0).getServiceName() + "." + method;
        // identityHashCode 用来识别 services 是否发生过变更
        int identityHashCode = System.identityHashCode(invokers);
        ConsistentHashSelector selector = selectors.get(key);
        // 若不存在"接口.方法名"对应的选择器, 或是 invokers 列表已经发生了变更, 则初始化一个选择器
        if (selector == null || selector.identityHashCode != identityHashCode) {
            // 创建新的 selector 并缓存
            selectors.put(key, new ConsistentHashSelector(invokers, 160, identityHashCode));
            selector = selectors.get(key);
        }
        String selectKey = key;
        if (request.getParameterValues() != null && request.getParameterValues().length > 0) {
            selectKey += Arrays.stream(request.getParameterValues());
        }
        return selector.select(selectKey);
    }

    private static final class ConsistentHashSelector {
        /**
         * 存储 Hash 值与节点映射关系的TreeMap
         */
        private final TreeMap<Long, ServiceMessage> virtualInvokers;

        /**
         * 用来识别 services 列表是否发生变更的 hash 码
         */
        private final int identityHashCode;

        public ConsistentHashSelector(List<ServiceMessage> invokers, int replicaNumber, int identityHashCode) {
            this.virtualInvokers = new TreeMap<>();
            this.identityHashCode = identityHashCode;

            for (ServiceMessage invoker : invokers) {
                String address = invoker.getInetAddress();
                for (int i = 0; i < replicaNumber / 4; i++) {
                    // 对 address + i 进行 md5 运算，得到一个长度为16的字节数组
                    byte[] digest = md5(address + i);
                    // 对 digest 部分字节进行4次 hash 运算，得到四个不同的 long 型正整数
                    for (int h = 0; h < 4; h++) {
                        // h = 0 时，取 digest 中下标为 0 ~ 3 的4个字节进行位运算
                        // h = 1 时，取 digest 中下标为 4 ~ 7 的4个字节进行位运算
                        // h = 2, h = 3 时过程同上
                        long m = hash(digest, h);
                        // 将 hash 到 invoker 的映射关系存储到 virtualInvokers 中
                        virtualInvokers.put(m, invoker);
                    }
                }
            }
        }


        /**
         * 进行 md5 运算
         *
         * @param key 编码字符串 key
         * @return 编码后的摘要内容, 长度为 16 的字节数组
         */
        private byte[] md5(String key) {
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("MD5");
                byte[] bytes = key.getBytes(StandardCharsets.UTF_8);
                md.update(bytes);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            return md.digest();
        }

        /**
         * 根据摘要生成 hash 值
         *
         * @param digest md5摘要内容
         * @param number 当前索引数
         * @return hash 值
         */
        private long hash(byte[] digest, int number) {
            return (((long) (digest[3 + number * 4] & 0xFF) << 24)
                    | ((long) (digest[2 + number * 4] & 0xFF) << 16)
                    | ((long) (digest[1 + number * 4] & 0xFF) << 8)
                    | (digest[number * 4] & 0xFF))
                    & 0xFFFFFFFFL;
        }

        public ServiceMessage select(String key) {
            byte[] digest = md5(key);
            return selectForKey(hash(digest, 0));
        }

        private ServiceMessage selectForKey(long hash) {
            Map.Entry<Long, ServiceMessage> entry = virtualInvokers.ceilingEntry(hash);
            if (entry == null) {
                entry = virtualInvokers.firstEntry();
            }
            return entry.getValue();
        }
    }
}
