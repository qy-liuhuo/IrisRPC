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
