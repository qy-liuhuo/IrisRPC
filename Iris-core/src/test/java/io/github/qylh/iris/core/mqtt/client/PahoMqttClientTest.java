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
package io.github.qylh.iris.core.mqtt.client;

import io.github.qylh.iris.core.config.MqttConnectionConfig;
import io.github.qylh.iris.core.common.execption.MqttClientException;
import io.github.qylh.iris.core.listener.PayLoadListener;

import io.github.qylh.iris.core.env.MQTTContainerTestEnv;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class PahoMqttClientTest extends MQTTContainerTestEnv {
    
    private MqttConnectionConfig mqttConnectionConfig;
    
    private MqttClient pahoMqttClient;
    
    @Before
    public void beforeRun() {
        mqttConnectionConfig = MqttConnectionConfig.builder()
                .broker("tcp://" + mqttBrokerContainer.getHost() + ":" + mqttBrokerContainer.getMappedPort(1883))
                .connectionTimeout(10)
                .keepAliveInterval(60)
                .clientId("PahoMqttClientTest").build();
        pahoMqttClient = new PahoMqttClient();
        try {
            pahoMqttClient.connect(mqttConnectionConfig);
        } catch (MqttClientException exception) {
            System.out.println(exception.getMessage());
        }
    }
    
    @After
    public void afterRun() {
        pahoMqttClient.disconnect();
    }
    
    @Test
    public void testPubAndSub() {
        List<String> sendMsg = new ArrayList<>();
        List<String> receivedMsg = new ArrayList<>();
        pahoMqttClient.subscribe_payload("test", new PayLoadListener() {
            
            @Override
            public void onMessage(String topic, String message) {
                System.out.println("Received message: " + message);
                receivedMsg.add(String.valueOf(message));
            }
        });
        try {
            for (int i = 0; i < 10; i++) {
                sendMsg.add("Hello, " + i);
                pahoMqttClient.publish("test", "Hello, " + i, 2);
            }
            Thread.sleep(2);
        } catch (MqttClientException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        assert sendMsg.size() == receivedMsg.size();
        for (int i = 0; i < sendMsg.size(); i++) {
            Assert.assertEquals(sendMsg.get(i), receivedMsg.get(i));
        }
    }
    
}