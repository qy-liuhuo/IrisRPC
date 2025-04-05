package io.github.qylh.iris.client;

import io.github.qylh.iris.common.config.MqttConnectionConfig;
import io.github.qylh.iris.common.msg.MqttRequest;
import io.github.qylh.iris.common.msg.MqttResponse;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ClientProxy implements InvocationHandler {

    private static Requester requestInvoker;


    public ClientProxy(MqttConnectionConfig config) {
        requestInvoker = new Requester();
        requestInvoker.init(config);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        MqttRequest request = new MqttRequest();
        request.setServiceName(method.getDeclaringClass().getSimpleName());
        request.setMethodName(method.getName());
        request.setArgs(args);
        request.setArgsType(method.getParameterTypes());
        MqttResponse response = requestInvoker.request(request);
        if (response == null) {
            return null;
        }
        return response.getData();
    }

    public <T>T getProxy(Class<T> clazz){
        Object o = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, this);
        return (T) o;
    }
}
