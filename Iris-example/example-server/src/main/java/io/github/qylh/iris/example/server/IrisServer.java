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
package io.github.qylh.iris.example.server;

import io.github.qylh.iris.core.common.execption.MqttClientException;
import io.github.qylh.iris.core.config.MqttConnectionConfig;
import io.github.qylh.iris.core.server.RequestProcessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class IrisServer {
    
    public static void main(String[] args) throws MqttClientException, IOException {
        RequestProcessor requestProcessor = new RequestProcessor();
        List<Class<?>> classList = new ArrayList<>();
        classList.add(TestServiceImpl.class);
        MqttConnectionConfig mqttConnectionConfig = MqttConnectionConfig.builder()
                .broker("tcp://localhost:1883")
                .connectionTimeout(10)
                .keepAliveInterval(60)
                .clientId("IrisServer")
                .build();
        requestProcessor.start(classList, mqttConnectionConfig);
        System.in.read();
    }
}