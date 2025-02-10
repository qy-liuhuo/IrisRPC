package io.github.qylh.common.mqtt.client;

import io.github.qylh.common.mqtt.MqttClientException;
import io.github.qylh.common.mqtt.MqttConnectionConfig;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

public class PahoMqttClient extends MqttClient{

    private org.eclipse.paho.client.mqttv3.MqttClient mqttClient;

    @Override
    public void connect(MqttConnectionConfig mqttConnectionConfig) {
        try {
            this.mqttClient = new org.eclipse.paho.client.mqttv3.MqttClient(mqttConnectionConfig.getBroker(), mqttConnectionConfig.getClientId());
            MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setUserName(mqttConnectionConfig.getUsername());
            mqttConnectOptions.setPassword(mqttConnectionConfig.getPassword().toCharArray());
            mqttConnectOptions.setConnectionTimeout(mqttConnectionConfig.getConnectionTimeout());
            mqttConnectOptions.setKeepAliveInterval(mqttConnectionConfig.getKeepAliveInterval());
            mqttConnectOptions.setCleanSession(true);
            mqttConnectOptions.setAutomaticReconnect(true);
            this.mqttClient.connect(mqttConnectOptions);
        } catch (org.eclipse.paho.client.mqttv3.MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void publish(String topic, String message, int qos) throws MqttClientException {
        try {
            this.mqttClient.publish(topic, message.getBytes(), qos, false);
        }catch (org.eclipse.paho.client.mqttv3.MqttException e) {
            throw new MqttClientException(e.getMessage());
        }
    }

    @Override
    public void publish(String[] topic, int qos) throws MqttClientException {
        for (String t : topic) {
            try {
                this.mqttClient.publish(t, new byte[0], qos, false);
            }catch (org.eclipse.paho.client.mqttv3.MqttException e) {
                throw new MqttClientException(e.getMessage());
            }
        }
    }


    @Override
    public void subscribe(String topic) {
        try {
            this.mqttClient.subscribe(topic);
        } catch (org.eclipse.paho.client.mqttv3.MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void subscribe(String[] topics) {
        for (String t : topics) {
            try {
                this.mqttClient.subscribe(t);
            } catch (org.eclipse.paho.client.mqttv3.MqttException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void unsubscribe(String topic) {
        try {
            this.mqttClient.unsubscribe(topic);
        } catch (org.eclipse.paho.client.mqttv3.MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void unsubscribe(String[] topics) {
        for (String t : topics) {
            try {
                this.mqttClient.unsubscribe(t);
            } catch (org.eclipse.paho.client.mqttv3.MqttException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void disconnect() {
        try {
            this.mqttClient.disconnect();
        } catch (org.eclipse.paho.client.mqttv3.MqttException e) {
            e.printStackTrace();
        }
    }
}
