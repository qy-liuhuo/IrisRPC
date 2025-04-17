package io.github.qylh.iris.client;

import io.github.qylh.iris.common.config.MqttConnectionConfig;

public class ClientProxyFactory {
    private final Requester requestInvoker;

    public ClientProxyFactory(MqttConnectionConfig config){
        this.requestInvoker = new Requester();
        this.requestInvoker.init(config);
    }

    public <T>T getProxy(Class<?> serviceClazz){
        return new ClientProxy(this.requestInvoker, serviceClazz).getProxy();
    }
}
