package io.github.qylh.client;

import io.github.qylh.common.mqtt.MqttConnectionConfig;
import io.github.qylh.common.mqtt.MqttRequest;
import io.github.qylh.common.mqtt.MqttResponse;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ClientProxy implements InvocationHandler {

    private static Requester requestInvoker;


    public ClientProxy(MqttConnectionConfig config) {
        requestInvoker = new Requester();
        requestInvoker.init(config);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        MqttRequest request = MqttRequest.builder()
                .serviceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .args(args)
                .argsType(method.getParameterTypes())
                .build();
        MqttResponse response = requestInvoker.request(request);
        if (response == null) {
            return null;
        }
        return response.getData();
    }
}
