package io.github.qylh.common.mqtt;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MqttResponse extends RPCMsg{

    //响应码
    private int code;

    //响应消息
    private String msg;

    //响应数据
    private Object data;

    //响应id
    private Integer ResponseId;

    public MqttResponse(){

    }

}
