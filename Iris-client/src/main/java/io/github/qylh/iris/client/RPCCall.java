package io.github.qylh.iris.client;

import io.github.qylh.iris.common.msg.MqttResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class RPCCall extends CompletableFuture<MqttResponse> {
    private final int requestId;

    private static ConcurrentHashMap<Integer, RPCCall> calls = new ConcurrentHashMap<>();

    private RPCCall(int requestId){
        this.requestId = requestId;
        calls.put(requestId, this);
    }

    public static RPCCall makeRPCCall(int requestId){
        return new RPCCall(requestId);
    }

    public static RPCCall getRPCCall(int requestId){
        return calls.get(requestId);
    }

    public static void removeRPCCall(int requestId){
        calls.remove(requestId);
    }
}
