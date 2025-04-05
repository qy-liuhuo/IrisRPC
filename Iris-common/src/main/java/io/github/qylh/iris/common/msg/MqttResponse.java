package io.github.qylh.iris.common.msg;

import io.github.qylh.iris.common.constant.Constants;
import io.github.qylh.iris.common.serializer.JsonSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class MqttResponse extends MqttMsg{

    //响应码
    private int code;

    //响应消息
    private String msg;

    //响应数据
    private Object data;

    //响应id
    private int ResponseId;

    public MqttResponse() {

    }

    public String toString() {
        return JsonSerializer.serialize(this);
    }

    public static MqttResponse fromPahoMqttMessage(MqttMessage mqttMessage) throws UnsupportedEncodingException {
        return JsonSerializer.deserialize(new String(mqttMessage.getPayload(), Constants.MQTT_CHARSET_NAME), MqttResponse.class);
    }

    public MqttMessage toPahoMqttMessage(){
        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setPayload(JsonSerializer.serialize(this).getBytes());
        mqttMessage.setQos(this.getQos());
        mqttMessage.setId(this.getMessageId());
        return mqttMessage;
    }
}
