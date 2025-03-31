package io.github.qylh.common.mqtt;

import lombok.Data;

@Data
public class MqttResponse {
    private String clientId;
    private int code;
    private String msg;
}
