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

    public RequestProcessorGenerator(MqttClient mqttClient){
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
