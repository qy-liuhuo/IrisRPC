package io.github.qylh.common.mqtt;

import lombok.Data;

@Data
public class MqttConnectionConfig {
    private String broker;
    private String username;
    private String password;
    private String clientId;
    private int connectionTimeout;
    private int keepAliveInterval;
}
