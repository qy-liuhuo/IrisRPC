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

import io.github.qylh.iris.core.client.ClientProxyFactory;
import io.github.qylh.iris.core.common.execption.MqttClientException;
import io.github.qylh.iris.core.config.MqttConnectionConfig;
import io.github.qylh.iris.core.mqtt.MqttClient;
import io.github.qylh.iris.core.mqtt.PahoMqttClient;
import org.springframework.beans.BeansException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(IrisProperties.class)
public class IrisSpringBootStarterConfiguration implements ApplicationContextAware {
    
    private ApplicationContext applicationContext;
    
    @Bean
    public MqttClient mqttClient(IrisProperties properties) {
        MqttConnectionConfig mqttConnectionConfig = MqttConnectionConfig.builder()
                .broker(properties.getBroker())
                .username(properties.getUsername())
                .password(properties.getPassword())
                .clientId(properties.getClientId())
                .connectionTimeout(properties.getConnectionTimeout())
                .keepAliveInterval(properties.getKeepAliveInterval())
                .cleanSession(true)
                .build();
        MqttClient mqttClient = new PahoMqttClient();
        mqttClient.setClientId(mqttConnectionConfig.getClientId());
        try {
            mqttClient.connect(mqttConnectionConfig);
        } catch (MqttClientException e) {
            throw new RuntimeException("mqtt client connect error", e);
        }
        return mqttClient;
    }
    
    @Bean
    public ClientProxyFactory clientProxyFactory(MqttClient mqttClient) {
        return new ClientProxyFactory(mqttClient);
    }
    
    // @Bean
    // public RequestProcessor requestProcessor(IrisProperties properties) throws MqttClientException {
    // MqttConnectionConfig mqttConnectionConfig = MqttConnectionConfig.builder()
    // .broker(properties.getBroker())
    // .username(properties.getUsername())
    // .password(properties.getPassword())
    // .clientId(properties.getClientId())
    // .connectionTimeout(properties.getConnectionTimeout())
    // .keepAliveInterval(properties.getKeepAliveInterval())
    // .build();
    // Map<String, Object> serviceBeans = applicationContext.getBeansWithAnnotation(IrisService.class);
    // RequestProcessor requestProcessor = new RequestProcessor();
    // for (Map.Entry<String, Object> entry : serviceBeans.entrySet()) {
    // String serviceName = entry.getValue().getClass().getAnnotation(IrisService.class).name();
    // if (serviceName == null || serviceName.isEmpty()) {
    // throw new RuntimeException("service name is empty");
    // }
    // requestProcessor.putService(serviceName, entry.getValue());
    // }
    // requestProcessor.start(serviceBeans.values().stream().map(Object::getClass).collect(Collectors.toList()), mqttConnectionConfig);
    // return requestProcessor;
    // }
    
    // @Bean
    // public IrisReferenceBeanPostProcessor irisReferenceBeanPostProcessor(IrisProperties irisProperties) {
    // return new IrisReferenceBeanPostProcessor(irisProperties);
    // }
    //
    // @Bean
    // public IrisMCPToolRegister irisMCPToolRegister(IrisProperties irisProperties) throws MqttClientException {
    // return new IrisMCPToolRegister(irisProperties);
    // }
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
