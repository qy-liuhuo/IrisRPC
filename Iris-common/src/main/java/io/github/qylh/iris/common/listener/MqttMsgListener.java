package io.github.qylh.iris.common.listener;

import io.github.qylh.iris.common.msg.MqttMsg;

public interface MqttMsgListener {
    void onMessage(String topic, MqttMsg message);
}
