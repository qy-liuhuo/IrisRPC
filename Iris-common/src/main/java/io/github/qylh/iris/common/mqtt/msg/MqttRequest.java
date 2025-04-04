package io.github.qylh.iris.common.mqtt.msg;

import io.github.qylh.iris.common.constant.Constants;
import io.github.qylh.iris.common.serializer.JsonSerializer;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;

@Getter
@Setter
public class MqttRequest extends MqttMsg{

    private String serviceName;

    private String methodName;

    private Object[] args;

    private Class<?>[] argsType;

    private int requestId;

    public String getTopic(){
        return Constants.MQTT_REQUEST_TOPIC_SUFFIX + serviceName + "/" + methodName;
    }

    public String toString() {
        return JsonSerializer.serialize(this);
    }

    public static MqttRequest fromPahoMqttMessage(MqttMessage mqttMessage) throws UnsupportedEncodingException {
        return JsonSerializer.deserialize(new String(mqttMessage.getPayload(), Constants.MQTT_CHARSET_NAME), MqttRequest.class);
    }

    public MqttMessage toPahoMqttMessage(){
        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setPayload(JsonSerializer.serialize(this).getBytes());
        mqttMessage.setQos(this.getQos());
        mqttMessage.setId(this.getMessageId());
        return mqttMessage;
    }

    public MqttRequest() {
        this.requestId = this.getMessageId();
    }

}
