package io.github.qylh.common.mqtt;

import io.github.qylh.common.serializer.JsonSerializer;
import lombok.Data;

import java.util.Arrays;
import java.util.List;

@Data
public class MqttRequest {

    private String serviceName;

    private String methodName;

    private List<String> args;

    private String clientId;

    public void addArg(Object arg) {
        if (arg instanceof String){
            args.add((String) arg);
        }else{
            args.add(JsonSerializer.serialize(arg));
        }
    }

    public MqttRequest(MqttMsg msg){
        JsonSerializer.deserialize(msg.toString(), MqttRequest.class);
    }
}
