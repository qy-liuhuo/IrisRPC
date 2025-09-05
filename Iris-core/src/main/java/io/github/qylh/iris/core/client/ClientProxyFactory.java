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
import io.github.qylh.iris.core.mqtt.MqttClient;

public class ClientProxyFactory {
    
    // todo clientProxy 单例模式
    private final Requester requestInvoker;
    
    public ClientProxyFactory(IrisConfig config) {
        this.requestInvoker = new Requester(config);
        this.requestInvoker.start();
    }
    
    public ClientProxyFactory(MqttClient mqttClient) {
        this.requestInvoker = new Requester(mqttClient);
        this.requestInvoker.start();
    }
    
    public <T> T getProxy(Class<?> serviceClazz) {
        return new ClientProxy(this.requestInvoker, serviceClazz).getProxy();
    }
}
