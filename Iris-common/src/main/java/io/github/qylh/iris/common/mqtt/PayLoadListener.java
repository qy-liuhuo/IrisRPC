package io.github.qylh.iris.common.mqtt;

public interface PayLoadListener {
    void onMessage(String topic, String message);
}
