package io.github.qylh.common.mqtt;

import io.github.qylh.common.constant.Constants;
import io.github.qylh.common.serializer.JsonSerializer;
import lombok.Data;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

@Data
public class MqttMsg {

    private static final AtomicInteger MSG_ID_GENERATOR = new AtomicInteger(ThreadLocalRandom.current().nextInt());

    private RPCMsg RPCMsg;

    private int qos = Constants.QOS;

    private Integer messageId;

    public static void validateQos(int qos) {
        if (qos < 0 || qos > 2) {
            throw new IllegalArgumentException();
        }
    }

    public MqttMsg(){
        this.messageId = MSG_ID_GENERATOR.incrementAndGet();
    }

    public MqttMsg(MqttRequest mqttRequest){
        this.messageId = MSG_ID_GENERATOR.incrementAndGet();
        mqttRequest.setRequestId(this.messageId);
        this.RPCMsg = mqttRequest;
    }

    public MqttMsg(MqttResponse mqttResponse){
        this.messageId = MSG_ID_GENERATOR.incrementAndGet();
        this.RPCMsg = mqttResponse;
    }

    public static MqttMsg fromPahoMqttMessage(MqttMessage mqttMessage) throws UnsupportedEncodingException {
        return JsonSerializer.deserialize(new String(mqttMessage.getPayload(), Constants.MQTT_CHARSET_NAME), MqttMsg.class);
    }

    public MqttMessage toPahoMqttMessage(){
        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setPayload(JsonSerializer.serialize(this).getBytes());
        mqttMessage.setQos(this.qos);
        mqttMessage.setId(this.messageId);
        return mqttMessage;
    }

}
