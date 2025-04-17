package io.github.qylh.iris.core.config;

import lombok.Builder;
import lombok.Data;

import java.util.concurrent.TimeUnit;

@Data
@Builder
public class IrisConfig {

    private MqttConnectionConfig mqttConnectionConfig;

    private long timeout;

    private TimeUnit timeoutUnit = TimeUnit.SECONDS;

}
