package io.github.qylh.common.mqtt;

import io.github.qylh.common.constant.Constants;
import io.github.qylh.common.serializer.JsonSerializer;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MqttRequest {

    private String serviceName;

    private String methodName;

    private Object[] args;

    private Class<?>[] argsType;

    private String clientId;

    public String getTopic(){
        return Constants.MQTT_REQUEST_TOPIC_SUFFIX + serviceName + "/" + methodName;
    }

    public MqttRequest(MqttMsg msg){
        JsonSerializer.deserialize(msg.toString(), MqttRequest.class);
    }

}
