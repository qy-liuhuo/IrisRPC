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
import io.github.qylh.iris.core.common.serializer.JsonSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class MqttResponse extends MqttMsg {
    
    // 响应码
    private int code;
    
    // 响应消息
    private String msg;
    
    // 响应数据
    private Object data;
    
    // 响应id
    private int ResponseId;
    
    public MqttResponse() {
        
    }
    
    public String toString() {
        return JsonSerializer.serialize(this);
    }
    
    public static MqttResponse fromPahoMqttMessage(MqttMessage mqttMessage) throws UnsupportedEncodingException {
        return JsonSerializer.deserialize(new String(mqttMessage.getPayload(), Constants.MQTT_CHARSET_NAME), MqttResponse.class);
    }
    
    public MqttMessage toPahoMqttMessage() {
        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setPayload(JsonSerializer.serialize(this).getBytes());
        mqttMessage.setQos(this.getQos());
        mqttMessage.setId(this.getMessageId());
        return mqttMessage;
    }
}
