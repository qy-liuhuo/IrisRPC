package io.github.qylh.iris.common.listener;

public interface PayLoadListener {
    void onMessage(String topic, String message);
}
