package io.github.qylh.common.mqtt;

public interface MqttMsgListener {
    void onMessage(String topic, MqttMsg message);
}
