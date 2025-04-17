package io.github.qylh.iris.core.listener;

public interface PayLoadListener {
    void onMessage(String topic, String message);
}
