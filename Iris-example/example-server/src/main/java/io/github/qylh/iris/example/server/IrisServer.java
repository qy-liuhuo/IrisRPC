package io.github.qylh.iris.example.server;

import io.github.qylh.iris.core.execption.MqttClientException;
import io.github.qylh.iris.core.config.MqttConnectionConfig;
import io.github.qylh.iris.server.RequestProcessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class IrisServer {
    public static void main(String[] args) throws MqttClientException, IOException {
        RequestProcessor requestProcessor = new RequestProcessor();
        List<Class<?>> classList = new ArrayList<>();
        classList.add(TestServiceImpl.class);
        MqttConnectionConfig mqttConnectionConfig = MqttConnectionConfig.builder()
                .broker("tcp://localhost:1883")
                .connectionTimeout(10)
                .keepAliveInterval(60)
                .clientId("IrisServer")
                .build();
        requestProcessor.start(classList, mqttConnectionConfig);
        System.in.read();
    }
}