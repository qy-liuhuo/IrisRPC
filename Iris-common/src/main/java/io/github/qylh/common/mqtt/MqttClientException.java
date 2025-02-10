package io.github.qylh.common.mqtt;

public class MqttClientException extends Exception {
    public MqttClientException(String message) {
        super(message);
    }

    public MqttClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
