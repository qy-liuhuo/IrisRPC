package io.github.qylh.iris.demo;

import io.github.qylh.iris.client.ClientProxy;
import io.github.qylh.iris.common.config.MqttConnectionConfig;

public class IrisClient {
    public static void main(String[] args) {
        MqttConnectionConfig mqttConnectionConfig = MqttConnectionConfig.builder()
                .broker("tcp://localhost:1883")
                .connectionTimeout(10)
                .keepAliveInterval(60)
                .clientId("IrisClient")
                .build();
        ClientProxy clientProxy = new ClientProxy(mqttConnectionConfig);
        TestService testService = clientProxy.getProxy(TestService.class);
        String result = testService.test(10);
        System.out.println(result);
    }
}
