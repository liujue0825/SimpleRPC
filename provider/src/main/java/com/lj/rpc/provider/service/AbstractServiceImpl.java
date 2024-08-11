package com.lj.rpc.provider.service;

import com.lj.rpc.api.service.AbstractService;
import com.lj.rpc.server.annotation.RpcService;

/**
 * @author liujue
 * @version 1.0
 * @since 2024/7/19
 */
@RpcService(interfaceClass = AbstractService.class)
public class AbstractServiceImpl extends AbstractService {

    @Override
    public String abstractHello(String name) {
        return "abstract hello " + name;
    }
}
