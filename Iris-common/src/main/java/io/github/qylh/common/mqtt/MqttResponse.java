package io.github.qylh.common.mqtt;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MqttResponse {
    private String clientId;
    private int code;
    private String msg;
}
