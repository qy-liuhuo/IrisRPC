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
package io.github.qylh.iris.core.mqtt;

import io.github.qylh.iris.core.common.msg.MqttRegisterMsg;
import io.github.qylh.iris.core.config.MqttConnectionConfig;
import io.github.qylh.iris.core.common.execption.MqttClientException;
import io.github.qylh.iris.core.listener.MqttMsgListener;
import io.github.qylh.iris.core.listener.PayLoadListener;
import io.github.qylh.iris.core.common.msg.MqttMsg;

public abstract class MqttClient {

    private String clientId;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public abstract void connect(MqttConnectionConfig mqttConnectionConfig) throws MqttClientException;
    public abstract void publish(String topic, MqttMsg mqttMsg) throws MqttClientException;
    public abstract void publish(String[] topic, MqttMsg mqttMsg) throws MqttClientException;
    public abstract void publish(String topic, String msg, int qos) throws MqttClientException;
    public abstract void publish(String[] topic, String msg, int qos) throws MqttClientException;
    
    public abstract void subscribe_request(String topic, MqttMsgListener mqttMsgListener);
    public abstract void subscribe_request(String[] topics, MqttMsgListener mqttMsgListener);
    
    public abstract void subscribe_response(String topic, MqttMsgListener mqttMsgListener);
    public abstract void subscribe_response(String[] topics, MqttMsgListener mqttMsgListener);

    public abstract void subscribe_register(String topic, MqttMsgListener mqttMsgListener);

    public abstract void subscribe_payload(String topic, PayLoadListener payLoadListener);
    
    public abstract void unsubscribe(String topic);
    public abstract void unsubscribe(String[] topics);
    public abstract void disconnect();
    public abstract void register(String topic, MqttRegisterMsg mqttRegisterMsg);
    public abstract boolean isConnect();
}
