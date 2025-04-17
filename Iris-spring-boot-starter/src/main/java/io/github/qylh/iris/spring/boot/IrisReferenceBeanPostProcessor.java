package io.github.qylh.iris.spring.boot;

import io.github.qylh.iris.client.ClientProxyFactory;
import io.github.qylh.iris.core.annotation.IrisRPC;
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
