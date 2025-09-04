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
package io.github.qylh.iris.core.server;

import io.github.qylh.iris.core.common.annotation.IrisApi;
import io.github.qylh.iris.core.common.annotation.IrisService;
import io.github.qylh.iris.core.common.annotation.IrisTool;
import io.github.qylh.iris.core.common.annotation.IrisToolParam;
import io.github.qylh.iris.core.common.constant.Constants;
import io.github.qylh.iris.core.common.execption.MqttClientException;
import io.github.qylh.iris.core.common.msg.MqttRegisterMsg;
import io.github.qylh.iris.core.common.msg.MqttRequest;
import io.github.qylh.iris.core.common.msg.MqttResponse;
import io.github.qylh.iris.core.config.MqttConnectionConfig;
import io.github.qylh.iris.core.mqtt.MqttClient;
import io.github.qylh.iris.core.mqtt.PahoMqttClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class RequestProcessor {
    
    private static final Logger Log = LoggerFactory.getLogger(RequestProcessor.class);
    
    private Map<String, Object> serviceObjects = new HashMap<>();
    
    private MqttClient mqttClient;
    
    private String clientId;
    
    public void start(List<Class<?>> serviceList, MqttConnectionConfig mqttConnectionConfig) throws MqttClientException {
        try {
            mqttClient = new PahoMqttClient();
            mqttClient.connect(mqttConnectionConfig);
            clientId = mqttConnectionConfig.getClientId();
        } catch (MqttClientException e) {
            throw new MqttClientException("Failed to connect to broker ReasonCode is:" + e.getMessage());
        }
        register(serviceList);
    }

    public void start(List<Class<?>> serviceList, MqttClient mqttClient){
        this.mqttClient = mqttClient;
        register(serviceList);
    }
    
    public void putService(String serviceName, Object serviceObject) {
        if (serviceObjects.containsKey(serviceName)) {
            throw new RuntimeException("duplicate service name");
        } else {
            serviceObjects.put(serviceName, serviceObject);
        }
    }
    
    private void register(List<Class<?>> serviceList) {
        for (Class<?> service : serviceList) {
            if (service.isAnnotationPresent(IrisService.class)) {
                String serviceName = service.getAnnotation(IrisService.class).name();
                Method[] methods = service.getMethods();
                // Todo 通过构造器构造对象（依赖问题？）
                try {
                    if (!serviceObjects.containsKey(serviceName)) {
                        serviceObjects.put(serviceName, service.newInstance());
                    }
                } catch (InstantiationException | IllegalAccessException e) {
                    Log.error("Failed to create service object");
                    continue;
                }
                for (Method method : methods) {
                    if (method.isAnnotationPresent(IrisApi.class) || method.getDeclaringClass() == Object.class) {
                        String apiName;
                        if (method.getDeclaringClass() == Object.class) {
                            apiName = method.getName();
                        } else {
                            apiName = method.getAnnotation(IrisApi.class).name();
                            if(method.isAnnotationPresent(IrisTool.class)) {
                                String registerTopic = Constants.MQTT_REGISTER_TOPIC_SUFFIX + serviceName + "/" + apiName;
                                MqttRegisterMsg registerMsg = new MqttRegisterMsg();
                                registerMsg.setQos(2);
                                registerMsg.setServiceName(serviceName);
                                registerMsg.setMethodName(method.getName());
                                registerMsg.setServiceDesc(service.getAnnotation(IrisService.class).desc());
                                registerMsg.setMethodName(method.getName());
                                registerMsg.setMethodDesc(method.getAnnotation(IrisTool.class).desc());
                                // todo 必须把实现的接口放到第一个
                                registerMsg.setInterfaceType(service.getInterfaces()[0]);
                                Parameter[] parameters = method.getParameters();
                                registerMsg.setArgsType(method.getParameterTypes());
                                registerMsg.setArgsDesc(Stream.of(parameters).map(
                                        parameter -> {
                                            if (parameter.isAnnotationPresent(IrisToolParam.class)){
                                                return parameter.getAnnotation(IrisToolParam.class).desc();
                                            }else{
                                                return "";
                                            }
                                        }
                                ).toArray(String[]::new));
                                System.out.println(registerTopic);
                                // 保留消息注册
                                mqttClient.register(registerTopic, registerMsg);
                            }
                        }
                        String fullName = Constants.MQTT_REQUEST_TOPIC_SUFFIX + serviceName + "/" + apiName;
                        mqttClient.subscribe_request(fullName, (topic, message) -> {
                            MqttResponse mqttResponse = new MqttResponse();
                            MqttRequest mqttRequest = (MqttRequest) message;
                            
                            Object[] args = mqttRequest.getArgs();
                            try {
                                Object res = method.invoke(serviceObjects.get(serviceName), args);
                                mqttResponse.setCode(Constants.IRIS_MQTT_SUCCESS);
                                mqttResponse.setData(res);
                                mqttResponse.setResponseId(mqttRequest.getRequestId());
                            } catch (Exception e) {
                                Log.error("Failed to invoke method :" + fullName);
                                mqttResponse.setCode(Constants.IRIS_MQTT_INVOKE_ERROR);
                                mqttResponse.setMsg("Failed to invoke method :" + fullName);
                            } finally {
                                try {
                                    mqttClient.publish(Constants.MQTT_RESPONSE_TOPIC_SUFFIX + message.getClientId(), mqttResponse);
                                } catch (MqttClientException e) {
                                    Log.error("Failed to publish response" + e.getMessage());
                                }
                            }
                        });
                        
                    }
                }
            }
        }
    }
    
}
