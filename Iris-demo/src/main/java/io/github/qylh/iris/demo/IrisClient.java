package io.github.qylh.iris.demo;

import io.github.qylh.iris.client.ClientProxy;
import io.github.qylh.iris.client.ClientProxyFactory;
import io.github.qylh.iris.common.config.MqttConnectionConfig;

public class IrisClient {
    public static void main(String[] args) {
        MqttConnectionConfig mqttConnectionConfig = MqttConnectionConfig.builder()
                .broker("tcp://localhost:1883")
                .connectionTimeout(10)
                .keepAliveInterval(60)
                .clientId("IrisClient")
                .build();
        ClientProxyFactory clientProxyFactory = new ClientProxyFactory(mqttConnectionConfig);
        TestService testService = clientProxyFactory.getProxy(TestService.class);
        String result = testService.test(10);
        System.out.println(result);
        System.out.println(testService);
    }
}
