package io.github.qylh.iris.common.mqtt.msg;

import io.github.qylh.iris.common.constant.Constants;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
public class MqttMsg {

    private static final AtomicInteger MSG_ID_GENERATOR = new AtomicInteger(ThreadLocalRandom.current().nextInt());

    private int qos = Constants.QOS;

    private Integer messageId;

    private String clientId;

    public MqttMsg(){
        this.messageId = MSG_ID_GENERATOR.incrementAndGet();
    }

    public MqttMessage toPahoMqttMessage(){
        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setQos(this.getQos());
        mqttMessage.setId(this.getMessageId());
        return mqttMessage;
    };

}
