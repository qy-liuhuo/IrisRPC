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
import io.github.qylh.iris.core.common.annotation.IrisRPC;
import io.github.qylh.iris.core.config.IrisConfig;
import io.github.qylh.iris.core.config.MqttConnectionConfig;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.ReflectionUtils;

public class IrisReferenceBeanPostProcessor implements BeanPostProcessor {
    
    private final ClientProxyFactory clientProxyFactory;
    
    public IrisReferenceBeanPostProcessor(IrisProperties irisProperties) {
        MqttConnectionConfig mqttConnectionConfig = MqttConnectionConfig.builder()
                .broker(irisProperties.getBroker())
                .username(irisProperties.getUsername())
                .password(irisProperties.getPassword())
                .clientId(irisProperties.getClientId() + "-client")
                .connectionTimeout(irisProperties.getConnectionTimeout())
                .keepAliveInterval(irisProperties.getKeepAliveInterval())
                .build();
        IrisConfig irisConfig = IrisConfig.builder()
                .mqttConnectionConfig(mqttConnectionConfig)
                .timeout(irisProperties.getTimeout())
                .build();
        this.clientProxyFactory = new ClientProxyFactory(irisConfig);
    }
    
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        ReflectionUtils.doWithFields(bean.getClass(), field -> {
            if (field.isAnnotationPresent(IrisRPC.class)) {
                Object serviceProxy = this.clientProxyFactory.getProxy(field.getType());
                field.setAccessible(true);
                field.set(bean, serviceProxy);
            }
        });
        return bean;
    }
}
