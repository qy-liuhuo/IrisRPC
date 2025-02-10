package io.github.qylh.common.mqtt.client;

import io.github.qylh.common.mqtt.MqttClientException;
import io.github.qylh.common.mqtt.MqttConnectionConfig;

public abstract class MqttClient {
    public abstract void connect(MqttConnectionConfig mqttConnectionConfig);
    public abstract void publish(String topic, String message, int qos) throws MqttClientException;
    public abstract void publish(String[] topic, int qos) throws MqttClientException;
    public abstract void subscribe(String topic);
    public abstract void subscribe(String[] topics);
    public abstract void unsubscribe(String topic);
    public abstract void unsubscribe(String[] topics);
    public abstract void disconnect();
}
