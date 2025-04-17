package io.github.qylh.iris.client;

import io.github.qylh.iris.common.config.IrisConfig;

public class ClientProxyFactory {
    private final Requester requestInvoker;

    public ClientProxyFactory(IrisConfig config){
        this.requestInvoker = new Requester(config);
        this.requestInvoker.start();
    }

    public <T>T getProxy(Class<?> serviceClazz){
        return new ClientProxy(this.requestInvoker, serviceClazz).getProxy();
    }
}
