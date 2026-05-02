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
import io.github.qylh.iris.core.common.constant.Constants;
import io.github.qylh.iris.core.common.execption.MqttClientException;
import io.github.qylh.iris.core.common.msg.MqttRegisterMsg;
import io.github.qylh.iris.core.common.serializer.JsonSerializer;
import io.github.qylh.iris.core.config.MqttConnectionConfig;
import io.github.qylh.iris.core.mqtt.MqttClient;
import io.github.qylh.iris.core.mqtt.PahoMqttClient;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.spec.McpSchema;
import javax.annotation.PostConstruct;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IrisMCPToolRegister implements ApplicationContextAware {
    
    private static final Logger log = LoggerFactory.getLogger(IrisMCPToolRegister.class);
    
    private ApplicationContext applicationContext;
    
    private static Map<String, McpServerFeatures.SyncToolSpecification> syncToolSpecifications = new ConcurrentHashMap<>();
    
    private final MqttClient mqttClient;
    
    private final ClientProxyFactory clientProxyFactory;
    
    @Autowired(required = false)
    private McpSyncServer mcpSyncServer;
    
    public IrisMCPToolRegister(IrisProperties properties, ClientProxyFactory clientProxyFactory) throws MqttClientException {
        MqttConnectionConfig mqttConnectionConfig = MqttConnectionConfig.builder()
                .broker(properties.getBroker())
                .username(properties.getUsername())
                .password(properties.getPassword())
                .clientId(properties.getClientId() + "-registerListener")
                .connectionTimeout(properties.getConnectionTimeout())
                .keepAliveInterval(properties.getKeepAliveInterval())
                .cleanSession(true)
                .build();
        this.mqttClient = new PahoMqttClient();
        try {
            this.mqttClient.connect(mqttConnectionConfig);
        } catch (MqttClientException e) {
            throw new RuntimeException("mqtt client connect error", e);
        }
        this.clientProxyFactory = clientProxyFactory;
    }
    
    @PostConstruct
    public void init() {
        startListen();
    }
    
    public void startListen() {
        mqttClient.subscribe_register(Constants.MQTT_REGISTER_TOPIC_SUFFIX + "#", (topic, message) -> {
            try {
                MqttRegisterMsg msg = MqttRegisterMsg.fromPahoMqttMessage(message.toPahoMqttMessage());
                buildSyncToolSpecification(msg);
            } catch (UnsupportedEncodingException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        });
        
    }
    
    private void buildSyncToolSpecification(MqttRegisterMsg msg) throws NoSuchMethodException {
        if (mcpSyncServer == null) {
            log.warn("McpSyncServer not available, skip tool registration for: {}", msg.getServiceName());
            return;
        }
        String serviceName = msg.getServiceName();
        Class<?> interfaceType = msg.getInterfaceType();
        Object serviceProxy = clientProxyFactory.getProxy(interfaceType);
        Class<?>[] argTypes = msg.getArgsType();
        Method method = interfaceType.getMethod(msg.getMethodName(), argTypes);
        // 构造 tool
        String toolName = serviceName + "-" + msg.getMethodName();
        String toolDesc = msg.getServiceDesc() + msg.getMethodDesc();
        Map<String, Object> properties = new HashMap<>();
        List<String> required = new ArrayList<>();
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Map<String, Object> param = new HashMap<>();
            param.put("type", toJsonType(argTypes[i]));
            param.put("description", msg.getArgsDesc()[i]);
            // 使用参数名（需 -parameters 编译），降级为 param_{i}
            String paramName = parameters[i].getName();
            if (paramName.startsWith("arg")) {
                paramName = "param" + i;
            }
            properties.put(paramName, param);
            required.add("true");
        }
        McpSchema.JsonSchema toolSchema = new McpSchema.JsonSchema(
                "object",
                properties,
                required,
                false);
        McpSchema.Tool Tool = new McpSchema.Tool(toolName, toolDesc, toolSchema);
        McpServerFeatures.SyncToolSpecification syncToolSpecification = new McpServerFeatures.SyncToolSpecification(
                Tool,
                (exchange, arguments) -> {
                    Object[] args = new Object[parameters.length];
                    for (int i = 0; i < parameters.length; i++) {
                        String key = parameters[i].getName();
                        if (key.startsWith("arg")) {
                            key = "param" + i;
                        }
                        args[i] = arguments.get(key);
                    }
                    try {
                        Object res = method.invoke(serviceProxy, args);
                        McpSchema.TextContent resContent = new McpSchema.TextContent(JsonSerializer.serialize(res));
                        return new McpSchema.CallToolResult(List.of(resContent), false);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        return new McpSchema.CallToolResult(null, true);
                    }
                });
        
        syncToolSpecifications.put(toolName, syncToolSpecification);
        mcpSyncServer.addTool(syncToolSpecification);
    }
    
    private Map<String, McpServerFeatures.SyncToolSpecification> getSyncToolSpecifications() {
        return syncToolSpecifications;
    }
    
    private static String toJsonType(Class<?> javaType) {
        if (javaType == Boolean.class || javaType == boolean.class) {
            return "boolean";
        } else if (javaType == Integer.class || javaType == int.class
                || javaType == Long.class || javaType == long.class) {
            return "integer";
        } else if (javaType == Double.class || javaType == double.class
                || javaType == Float.class || javaType == float.class) {
            return "number";
        } else if (javaType == String.class) {
            return "string";
        }
        return "string";
    }
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
