package io.github.qylh.iris.client;

import io.github.qylh.iris.core.msg.MqttRequest;
import io.github.qylh.iris.core.msg.MqttResponse;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ClientProxy implements InvocationHandler {

    private final Requester requestInvoker;

    private final Class<?> serviceClazz;

    public ClientProxy(Requester requestInvoker, Class<?> serviceClazz){
        this.requestInvoker = requestInvoker;
        this.serviceClazz = serviceClazz;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        MqttRequest request = new MqttRequest();
        request.setServiceName(this.serviceClazz.getSimpleName());
        request.setMethodName(method.getName());
        request.setArgs(args);
        request.setArgsType(method.getParameterTypes());
        MqttResponse response = requestInvoker.request(request);
        if (response == null) {
            return null;
        }
        return response.getData();
    }

    public <T>T getProxy(){
        Object o = Proxy.newProxyInstance(serviceClazz.getClassLoader(), new Class[]{serviceClazz}, this);
        return (T) o;
    }
}
