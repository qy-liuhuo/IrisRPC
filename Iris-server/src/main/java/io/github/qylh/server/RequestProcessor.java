package io.github.qylh.server;

import io.github.qylh.common.annotation.Api;
import io.github.qylh.common.annotation.IrisService;
import io.github.qylh.common.constant.Constants;
import io.github.qylh.common.mqtt.*;
import io.github.qylh.common.mqtt.client.MqttClient;
import io.github.qylh.common.mqtt.client.PahoMqttClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestProcessor {

    private static final Logger Log = LoggerFactory.getLogger(RequestProcessor.class);

    private Map<String, Method> apis = new HashMap<>();

    private Map<String, Object> serviceObjects = new HashMap<>();

    private final MqttClient mqttClient = new PahoMqttClient();

    private String clientId;

    public void start(List<Class<?>> serviceList, MqttConnectionConfig mqttConnectionConfig) throws MqttClientException {
        try {
            mqttClient.connect(mqttConnectionConfig);
            clientId = mqttConnectionConfig.getClientId();
        } catch (MqttClientException e) {
            throw new MqttClientException("Failed to connect to broker ReasonCode is:" + e.getMessage());
        }
        register(serviceList);
    }

    private void register(List<Class<?>> serviceList){
        for (Class<?> service : serviceList) {
            if (service.isAnnotationPresent(IrisService.class)) {
                String serviceName = service.getAnnotation(IrisService.class).name();
                Method[] methods = service.getMethods();
                //Todo 通过构造器构造对象（依赖问题？）
                try{
                    serviceObjects.put(serviceName, service.newInstance());
                }catch (InstantiationException | IllegalAccessException e){
                    Log.error("Failed to create service object");
                    continue;
                }
                for (Method method : methods) {
                    if (method.isAnnotationPresent(Api.class)) {
                        String apiName = method.getAnnotation(Api.class).name();
                        String fullName = serviceName + "/" + apiName;
                        //register service
                        if (apis.containsKey(fullName)) {
                            throw new RuntimeException("duplicate api name");
                        }else{
                            apis.put(fullName, method);
                            mqttClient.subscribe(fullName, (topic, message) -> {
                                MqttResponse mqttResponse = new MqttResponse();
                                MqttRequest mqttRequest = (MqttRequest) message.getRPCMsg();
                                Object[] args = mqttRequest.getArgs();
                                try {
                                    Object res = method.invoke(serviceObjects.get(serviceName), args);
                                    mqttResponse.setCode(Constants.IRIS_MQTT_SUCCESS);
                                    mqttResponse.setData(res);
                                } catch (Exception e) {
                                    Log.error("Failed to invoke method :" + fullName);
                                    mqttResponse.setCode(Constants.IRIS_MQTT_INVOKE_ERROR);
                                    mqttResponse.setMsg("Failed to invoke method :" + fullName);
                                }finally {
                                    try {
                                        MqttMsg mqttMsg = new MqttMsg(mqttResponse);
                                        mqttClient.publish(Constants.MQTT_RESPONSE_TOPIC_SUFFIX + clientId, mqttMsg);
                                    }catch (MqttClientException e){
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

    private Method getMethod(String apiName){
        if (!apis.containsKey(apiName)) {
            throw new RuntimeException("api not found");
        }
        return apis.get(apiName);
    }
}
