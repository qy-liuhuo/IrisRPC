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
package io.github.qylh.iris.spring.boot;

import io.github.qylh.iris.core.common.annotation.IrisService;
import io.github.qylh.iris.core.mqtt.MqttClient;
import io.github.qylh.iris.core.server.RequestProcessor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;
import java.util.stream.Collectors;

public class RequestProcessorGenerator implements ApplicationContextAware, InitializingBean {
    
    private ApplicationContext applicationContext;
    
    private final MqttClient mqttClient;
    
    public RequestProcessorGenerator(MqttClient mqttClient) {
        this.mqttClient = mqttClient;
    }
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, Object> serviceBeans = applicationContext.getBeansWithAnnotation(IrisService.class);
        RequestProcessor requestProcessor = new RequestProcessor();
        for (Map.Entry<String, Object> entry : serviceBeans.entrySet()) {
            String serviceName = entry.getValue().getClass().getAnnotation(IrisService.class).name();
            if (serviceName == null || serviceName.isEmpty()) {
                throw new RuntimeException("service name is empty");
            }
            requestProcessor.putService(serviceName, entry.getValue());
        }
        requestProcessor.start(serviceBeans.values().stream().map(Object::getClass).collect(Collectors.toList()), mqttClient);
    }
}
