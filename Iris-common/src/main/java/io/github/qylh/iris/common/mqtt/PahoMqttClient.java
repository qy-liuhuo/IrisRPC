/*
 *    Licensed to the Apache Software Foundation (ASF) under one
 *    or more contributor license agreements.  See the NOTICE file
 *    distributed with this work for additional information
 *    regarding copyright ownership.  The ASF licenses this file
 *    to you under the Apache License, Version 2.0 (the
 *    "License"); you may not use this file except in compliance
 *    with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing,
 *    software distributed under the License is distributed on an
 *    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *    KIND, either express or implied.  See the License for the
 *    specific language governing permissions and limitations
 *    under the License.
 */
package io.github.qylh.iris.common.mqtt;

import io.github.qylh.iris.common.config.MqttConnectionConfig;
import io.github.qylh.iris.common.execption.MqttClientException;
import io.github.qylh.iris.common.listener.MqttMsgListener;
import io.github.qylh.iris.common.listener.PayLoadListener;
import io.github.qylh.iris.common.msg.MqttMsg;
import io.github.qylh.iris.common.msg.MqttRequest;
import io.github.qylh.iris.common.msg.MqttResponse;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class PahoMqttClient extends MqttClient {
    
    private org.eclipse.paho.client.mqttv3.MqttClient mqttClient;

    
    @Override
    public void connect(MqttConnectionConfig mqttConnectionConfig) throws MqttClientException {
        try {
            MqttClientPersistence mqttClientPersistence = new MemoryPersistence();
            this.mqttClient = new org.eclipse.paho.client.mqttv3.MqttClient(mqttConnectionConfig.getBroker(), mqttConnectionConfig.getClientId(), mqttClientPersistence);
            MqttConnectOptions mqttConnectOptions = getMqttConnectOptions(mqttConnectionConfig);
            this.mqttClient.connect(mqttConnectOptions);
        } catch (org.eclipse.paho.client.mqttv3.MqttException e) {
            throw new MqttClientException("Failed to connect to broker ReasonCode is:" + e.getReasonCode());
        }
    }

    private static MqttConnectOptions getMqttConnectOptions(MqttConnectionConfig mqttConnectionConfig) {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        if (mqttConnectionConfig.getUsername() != null && mqttConnectionConfig.getPassword() != null) {
            mqttConnectOptions.setUserName(mqttConnectionConfig.getUsername());
            mqttConnectOptions.setPassword(mqttConnectionConfig.getPassword().toCharArray());
        }
        mqttConnectOptions.setConnectionTimeout(mqttConnectionConfig.getConnectionTimeout());
        mqttConnectOptions.setKeepAliveInterval(mqttConnectionConfig.getKeepAliveInterval());
        mqttConnectOptions.setCleanSession(true);
        mqttConnectOptions.setAutomaticReconnect(true);
        return mqttConnectOptions;
    }

    @Override
    public void publish(String topic, MqttMsg message) throws MqttClientException {
        message.setClientId(this.mqttClient.getClientId());
        try {
            this.mqttClient.publish(topic, message.toPahoMqttMessage());
        } catch (org.eclipse.paho.client.mqttv3.MqttException e) {
            throw new MqttClientException(e.getMessage());
        }
    }
    
    @Override
    public void publish(String[] topics, MqttMsg message) throws MqttClientException {
        message.setClientId(this.mqttClient.getClientId());
        for (String topic : topics) {
            try {
                this.mqttClient.publish(topic, message.toPahoMqttMessage());
            } catch (org.eclipse.paho.client.mqttv3.MqttException e) {
                throw new MqttClientException(e.getMessage());
            }
        }
    }

    @Override
    public void publish(String topic, String message, int qos) throws MqttClientException {
        try {
            this.mqttClient.publish(topic, message.getBytes(), qos, false);
        } catch (org.eclipse.paho.client.mqttv3.MqttException e) {
            throw new MqttClientException(e.getMessage());
        }
    }

    @Override
    public void publish(String[] topic, String message, int qos) throws MqttClientException {
        for (String t : topic) {
            try {
                this.mqttClient.publish(t, message.getBytes(), qos, false);
            } catch (org.eclipse.paho.client.mqttv3.MqttException e) {
                throw new MqttClientException(e.getMessage());
            }
        }
    }

    @Override
    public void subscribe_request(String topic, MqttMsgListener mqttMsgListener) {
        try {
            IMqttMessageListener iMqttMessageListener = (topic1, message) -> mqttMsgListener.onMessage(topic1, MqttRequest.fromPahoMqttMessage((MqttMessage) message));
            this.mqttClient.subscribe(topic, iMqttMessageListener);
        } catch (org.eclipse.paho.client.mqttv3.MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void subscribe_request(String[] topics, MqttMsgListener mqttMsgListener) {
        IMqttMessageListener iMqttMessageListener = (topic1, message) -> mqttMsgListener.onMessage(topic1, MqttRequest.fromPahoMqttMessage((MqttMessage) message));
        for (String t : topics) {
            try {
                this.mqttClient.subscribe(t, iMqttMessageListener);
            } catch (org.eclipse.paho.client.mqttv3.MqttException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void subscribe_response(String topic, MqttMsgListener mqttMsgListener) {
        try {
            IMqttMessageListener iMqttMessageListener = (topic1, message) -> mqttMsgListener.onMessage(topic1, MqttResponse.fromPahoMqttMessage((MqttMessage) message));
            this.mqttClient.subscribe(topic, iMqttMessageListener);
        } catch (org.eclipse.paho.client.mqttv3.MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void subscribe_response(String[] topics, MqttMsgListener mqttMsgListener) {
        IMqttMessageListener iMqttMessageListener = (topic1, message) -> mqttMsgListener.onMessage(topic1, MqttResponse.fromPahoMqttMessage((MqttMessage) message));
        for (String t : topics) {
            try {
                this.mqttClient.subscribe(t, iMqttMessageListener);
            } catch (org.eclipse.paho.client.mqttv3.MqttException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void subscribe_payload(String topic, PayLoadListener payLoadListener) {
        try {
            IMqttMessageListener iMqttMessageListener = (topic1, message) -> payLoadListener.onMessage(topic1, new String(message.getPayload()));
            this.mqttClient.subscribe(topic, iMqttMessageListener);
        } catch (org.eclipse.paho.client.mqttv3.MqttException e) {
            e.printStackTrace();
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
