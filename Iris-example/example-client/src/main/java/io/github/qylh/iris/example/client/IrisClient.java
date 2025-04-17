package io.github.qylh.iris.example.client;

import io.github.qylh.iris.client.ClientProxyFactory;
import io.github.qylh.iris.core.config.IrisConfig;
import io.github.qylh.iris.core.config.MqttConnectionConfig;
import io.github.qylh.iris.example.common.TestService;

public class IrisClient {
    public static void main(String[] args) {
        MqttConnectionConfig mqttConnectionConfig = MqttConnectionConfig.builder()
                .broker("tcp://localhost:1883")
                .connectionTimeout(10)
                .keepAliveInterval(60)
                .clientId("IrisClient")
                .build();
        IrisConfig config = IrisConfig.builder()
                .mqttConnectionConfig(mqttConnectionConfig)
                .timeout(10)
                .build();
        ClientProxyFactory clientProxyFactory = new ClientProxyFactory(config);
        TestService testService = clientProxyFactory.getProxy(TestService.class);
        String result = testService.test(10);
        System.out.println(result);
        System.out.println(testService);
    }
}
