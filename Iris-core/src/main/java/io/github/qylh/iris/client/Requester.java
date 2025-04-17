package io.github.qylh.iris.client;

import io.github.qylh.iris.core.config.IrisConfig;
import io.github.qylh.iris.core.constant.Constants;
import io.github.qylh.iris.core.execption.MqttClientException;
import io.github.qylh.iris.core.mqtt.MqttClient;
import io.github.qylh.iris.core.mqtt.PahoMqttClient;
import io.github.qylh.iris.core.msg.MqttRequest;
import io.github.qylh.iris.core.msg.MqttResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadPoolExecutor;

public class Requester {
    private static final Logger logger = LoggerFactory.getLogger(Requester.class);
    private final MqttClient mqttClient = new PahoMqttClient();

    private final IrisConfig config;

    private static final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
            10,
            20,
            60,
            java.util.concurrent.TimeUnit.SECONDS,
            new java.util.concurrent.ArrayBlockingQueue<>(1000)
    );

    private String clientId;

    public Requester(IrisConfig config) {
        this.config = config;
    }

    public void start(){
        try {
            mqttClient.connect(config.getMqttConnectionConfig());
            clientId = config.getMqttConnectionConfig().getClientId();
            mqttClient.subscribe_response(Constants.MQTT_RESPONSE_TOPIC_SUFFIX + clientId, (topic, message) -> {
                threadPoolExecutor.submit(() -> {
                    MqttResponse mqttResponse = (MqttResponse) message;
                    int responseId = mqttResponse.getResponseId();
                    RPCCall rpcCall = RPCCall.getRPCCall(responseId);
                    rpcCall.complete(mqttResponse);
                });
            });
        } catch (MqttClientException e) {
            logger.error("Failed to connect to broker ReasonCode is:" + e.getMessage());
        }
    }

    public MqttResponse request(MqttRequest request) {
        try {
            mqttClient.publish(request.getTopic(), request);
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
        }finally {
            RPCCall.removeRPCCall(request.getRequestId());
        }
    }


}
