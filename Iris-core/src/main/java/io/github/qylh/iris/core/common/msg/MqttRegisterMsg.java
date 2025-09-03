package io.github.qylh.iris.core.common.msg;


import io.github.qylh.iris.core.common.constant.Constants;
import io.github.qylh.iris.core.common.serializer.JsonSerializer;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;

@Getter
@Setter
public class MqttRegisterMsg extends MqttMessage {

    private String serviceName;

    private String methodName;

    private String serviceDesc;

    private String methodDesc;

    private Object[] args;

    private Class<?>[] argsType;

    private String[] argsDesc;

    private int messageId;

    public String toString() {
        return JsonSerializer.serialize(this);
    }

    public static MqttRegisterMsg fromPahoMqttMessage(MqttMessage mqttMessage) throws UnsupportedEncodingException {
        return JsonSerializer.deserialize(new String(mqttMessage.getPayload(), Constants.MQTT_CHARSET_NAME), MqttRegisterMsg.class);
    }

    public MqttMessage toPahoMqttMessage() {
        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setPayload(JsonSerializer.serialize(this).getBytes());
        mqttMessage.setQos(this.getQos());
        mqttMessage.setId(this.getMessageId());
        return mqttMessage;
    }
}
