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
package io.github.qylh.iris.core.client;

import io.github.qylh.iris.core.config.IrisConfig;
import io.github.qylh.iris.core.common.constant.Constants;
import io.github.qylh.iris.core.common.execption.MqttClientException;
import io.github.qylh.iris.core.mqtt.MqttClient;
import io.github.qylh.iris.core.mqtt.PahoMqttClient;
import io.github.qylh.iris.core.common.msg.MqttRequest;
import io.github.qylh.iris.core.common.msg.MqttResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadPoolExecutor;

public class Requester {
    
    private static final Logger logger = LoggerFactory.getLogger(Requester.class);

    private MqttClient mqttClient;
    
    private IrisConfig config;
    
    private static final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
            10,
            20,
            60,
            java.util.concurrent.TimeUnit.SECONDS,
            new java.util.concurrent.ArrayBlockingQueue<>(1000));
    
    private String clientId;
    
    public Requester(IrisConfig config) {
        this.config = config;
        this.mqttClient = new PahoMqttClient();
        this.mqttClient.setClientId(this.config.getMqttConnectionConfig().getClientId());
        try {
            this.mqttClient.connect(config.getMqttConnectionConfig());
        } catch (MqttClientException e) {
            logger.error("Failed to connect to broker ReasonCode is:" + e.getMessage());
        }
    }

    public Requester(MqttClient mqttClient) {
        this.mqttClient = mqttClient;
    }
    
    public void start() {
        clientId = mqttClient.getClientId();
        // 接收回调消息
        mqttClient.subscribe_response(Constants.MQTT_RESPONSE_TOPIC_SUFFIX + clientId, (topic, message) -> {
            threadPoolExecutor.submit(() -> {
                MqttResponse mqttResponse = (MqttResponse) message;
                int responseId = mqttResponse.getResponseId();
                RPCCall rpcCall = RPCCall.getRPCCall(responseId);
                rpcCall.complete(mqttResponse);
            });
        });
//        mqttClient.subscribe_register(Constants.MQTT_REGISTER_TOPIC_SUFFIX + "#", (topic, message) -> {
//            System.out.println(message);
//        });

    }
    
    public MqttResponse request(MqttRequest request) {
        try {
            mqttClient.publish(request.getTopic(), request);
        } catch (MqttClientException e) {
            logger.error("Failed to invoke remote method ReasonCode is:" + e.getMessage());
            return null;
        }
        RPCCall rpcCall = RPCCall.makeRPCCall(request.getRequestId());
        try {
            // todo 设置超时时间
            return rpcCall.get();
        } catch (Exception e) {
            logger.error("Failed to get response ReasonCode is:" + e.getMessage());
            return null;
        } finally {
            RPCCall.removeRPCCall(request.getRequestId());
        }
    }
    
}
