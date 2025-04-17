package io.github.qylh.iris.core.listener;

import io.github.qylh.iris.core.msg.MqttMsg;

public interface MqttMsgListener {
    void onMessage(String topic, MqttMsg message);
}
