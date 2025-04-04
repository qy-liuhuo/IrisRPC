package io.github.qylh.iris.common.mqtt;

import io.github.qylh.iris.common.mqtt.msg.MqttMsg;

public interface MqttMsgListener {
    void onMessage(String topic, MqttMsg message);
}
