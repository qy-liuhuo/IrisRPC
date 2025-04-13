package io.github.qylh.iris.spring.boot;

import io.github.qylh.iris.client.ClientProxy;
import io.github.qylh.iris.common.annotation.IrisRPC;
import io.github.qylh.iris.common.config.MqttConnectionConfig;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.ReflectionUtils;

public class IrisReferenceBeanPostProcessor implements BeanPostProcessor {

    private final ClientProxy clientProxy;

    public IrisReferenceBeanPostProcessor(IrisProperties irisProperties) {
        MqttConnectionConfig mqttConnectionConfig = MqttConnectionConfig.builder()
                .broker(irisProperties.getBroker())
                .username(irisProperties.getUsername())
                .password(irisProperties.getPassword())
                .clientId(irisProperties.getClientId() + "-client")
                .connectionTimeout(irisProperties.getConnectionTimeout())
                .keepAliveInterval(irisProperties.getKeepAliveInterval())
                .build();
        clientProxy = new ClientProxy(mqttConnectionConfig);
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        ReflectionUtils.doWithFields(bean.getClass(), field -> {
            if (field.isAnnotationPresent(IrisRPC.class)) {
                Object serviceProxy = clientProxy.getProxy(field.getType());
                field.setAccessible(true);
                field.set(bean, serviceProxy);
            }

        });
        return bean;
    }
}
