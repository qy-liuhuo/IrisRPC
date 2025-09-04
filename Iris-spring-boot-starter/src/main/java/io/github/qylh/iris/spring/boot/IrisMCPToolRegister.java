package io.github.qylh.iris.spring.boot;


import io.github.qylh.iris.core.client.ClientProxyFactory;
import io.github.qylh.iris.core.common.constant.Constants;
import io.github.qylh.iris.core.common.execption.MqttClientException;
import io.github.qylh.iris.core.common.msg.MqttRegisterMsg;
import io.github.qylh.iris.core.common.serializer.JsonSerializer;
import io.github.qylh.iris.core.mqtt.MqttClient;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

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

    private ApplicationContext applicationContext;

    private static Map<String, McpServerFeatures.SyncToolSpecification> syncToolSpecifications = new ConcurrentHashMap<>();

    private final MqttClient mqttClient;

    private final ClientProxyFactory clientProxyFactory;

    public IrisMCPToolRegister(MqttClient mqttClient,ClientProxyFactory clientProxyFactory) throws MqttClientException {
        this.mqttClient = mqttClient;
        this.clientProxyFactory = new ClientProxyFactory(mqttClient);
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
        String serviceName = msg.getServiceName();
        Class<?> interfaceType = msg.getInterfaceType();
        Object serviceProxy = clientProxyFactory.getProxy(interfaceType);
//        Object serviceProxy = applicationContext.getBean(serviceName);
        Method method = serviceProxy.getClass().getMethod(msg.getMethodName());
        // 构造 tool
        String toolName = serviceName+"-"+ msg.getMethodName();
        String toolDesc = msg.getServiceDesc() + msg.getMethodDesc();
        Map<String, Object> properties = new HashMap<>();
        List<String> required = new ArrayList<>();
        for(int i = 0; i < msg.getArgsType().length; i++) {
            Map<String, Object> param = new HashMap<>();
            param.put("type", msg.getArgsType()[i].getSimpleName());
            param.put("desc", msg.getArgsDesc()[i]);
            properties.put(method.getParameters()[i].getName(), param);
            required.add("true");
        }
        McpSchema.JsonSchema toolSchema = new McpSchema.JsonSchema(
                "object",
                properties,
                required,
                false
        );
        McpSchema.Tool Tool = new McpSchema.Tool(toolName, toolDesc, toolSchema);
        McpServerFeatures.SyncToolSpecification syncToolSpecification = new McpServerFeatures.SyncToolSpecification(
                Tool,
                (exchange, arguments)->{
                    Parameter[] methodParameters = method.getParameters();
                    Object[] args = new Object[methodParameters.length];
                    for (int i = 0; i < methodParameters.length; i++) {
                        args[i] = arguments.get(methodParameters[i].getName());
                    }
                    try {
                        Object res = method.invoke(serviceProxy, args);
                        McpSchema.TextContent resContent = new McpSchema.TextContent(JsonSerializer.serialize(res));
                        return new McpSchema.CallToolResult(List.of(resContent), false);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        return new McpSchema.CallToolResult(null, true);
                    }
                }
        );
        syncToolSpecifications.put(toolName, syncToolSpecification);
    }

    private Map<String, McpServerFeatures.SyncToolSpecification> getSyncToolSpecifications() {
        return syncToolSpecifications;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
