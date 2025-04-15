package io.github.qylh.iris.spring.boot;

import io.github.qylh.iris.common.annotation.IrisService;
import io.github.qylh.iris.common.config.MqttConnectionConfig;
import io.github.qylh.iris.common.execption.MqttClientException;
import io.github.qylh.iris.server.RequestProcessor;
import org.springframework.beans.BeansException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableConfigurationProperties(IrisProperties.class)
public class IrisSpringBootStarterConfiguration implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Bean
    public RequestProcessor requestProcessor(IrisProperties properties) throws MqttClientException {
        MqttConnectionConfig mqttConnectionConfig = MqttConnectionConfig.builder()
                .broker(properties.getBroker())
                .username(properties.getUsername())
                .password(properties.getPassword())
                .clientId(properties.getClientId())
                .connectionTimeout(properties.getConnectionTimeout())
                .keepAliveInterval(properties.getKeepAliveInterval())
                .build();
        Map<String, Object> serviceBeans = applicationContext.getBeansWithAnnotation(IrisService.class);
        RequestProcessor requestProcessor = new RequestProcessor();
        for (Map.Entry<String, Object> entry : serviceBeans.entrySet()) {
            String serviceName = entry.getValue().getClass().getAnnotation(IrisService.class).name();
            if (serviceName == null || serviceName.isEmpty()) {
                throw new RuntimeException("service name is empty");
            }
            requestProcessor.putService(serviceName, entry.getValue());
        }
        requestProcessor.start(serviceBeans.values().stream().map(Object::getClass).collect(Collectors.toList()) , mqttConnectionConfig);
        return requestProcessor;
    }

    @Bean
    public  IrisReferenceBeanPostProcessor irisReferenceBeanPostProcessor(IrisProperties irisProperties) {
        return new IrisReferenceBeanPostProcessor(irisProperties);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
