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
package io.github.qylh.iris.core.common.msg;

import io.github.qylh.iris.core.common.constant.Constants;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
public class MqttMsg {
    
    private static final AtomicInteger MSG_ID_GENERATOR = new AtomicInteger(ThreadLocalRandom.current().nextInt());
    
    private int qos = Constants.QOS;
    
    private Integer messageId;
    
    private String clientId;
    
    public MqttMsg() {
        this.messageId = MSG_ID_GENERATOR.incrementAndGet();
    }
    
    public MqttMessage toPahoMqttMessage() {
        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setQos(this.getQos());
        mqttMessage.setId(this.getMessageId());
        return mqttMessage;
    };
    
}
