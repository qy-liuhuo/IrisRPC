package io.github.qylh.client;

import io.github.qylh.common.constant.Constants;
import io.github.qylh.common.mqtt.*;
import io.github.qylh.common.mqtt.client.MqttClient;
import io.github.qylh.common.mqtt.client.PahoMqttClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Requester {
    private static final Logger logger = LoggerFactory.getLogger(Requester.class);
    private final MqttClient mqttClient = new PahoMqttClient();

    private String clientId;

    public void init(MqttConnectionConfig mqttConnectionConfig){
        try {
            mqttClient.connect(mqttConnectionConfig);
            clientId = mqttConnectionConfig.getClientId();
            //TODO 线程池优化
            mqttClient.subscribe(Constants.MQTT_RESPONSE_TOPIC_SUFFIX + clientId, (topic, message) -> {
                int messageId = ((MqttRequest)message.getRPCMsg()).getRequestId();
                MqttResponse response = (MqttResponse) message.getRPCMsg();
                RPCCall rpcCall = RPCCall.getRPCCall(messageId);
                rpcCall.complete(response);
            });
        } catch (MqttClientException e) {
            logger.error("Failed to connect to broker ReasonCode is:" + e.getMessage());
        }
    }

    public MqttResponse request(MqttRequest request) {
        MqttMsg mqttMsg = new MqttMsg();
        request.setRequestId(mqttMsg.getMessageId());
        mqttMsg.setRPCMsg(request);
        try {
            mqttClient.publish(request.getTopic(), mqttMsg);
        }catch (MqttClientException e){
            logger.error("Failed to invoke remote method ReasonCode is:" + e.getMessage());
            return null;
        }
        RPCCall rpcCall = RPCCall.makeRPCCall(request.getRequestId());
        try{
            return rpcCall.get();
        } catch (Exception e) {
            logger.error("Failed to get response ReasonCode is:" + e.getMessage());
            return null;
        }
    }


}
