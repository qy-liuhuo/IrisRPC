/*
 *    Licensed to the Apache Software Foundation (ASF) under one
 *    or more contributor license agreements.  See the NOTICE file
 *    distributed with this work for additional information
 *    regarding copyright ownership.  The ASF licenses this file
 *    to you under the Apache License, Version 2.0 (the
 *    "License"); you may not use this file except in compliance
 *    with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing,
 *    software distributed under the License is distributed on an
 *    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *    KIND, either express or implied.  See the License for the
 *    specific language governing permissions and limitations
 *    under the License.
 */
package io.github.qylh.iris.core.client;

import io.github.qylh.iris.core.common.msg.MqttResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class RPCCall extends CompletableFuture<MqttResponse> {
    
    private final int requestId;
    
    private static ConcurrentHashMap<Integer, RPCCall> calls = new ConcurrentHashMap<>();
    
    private RPCCall(int requestId) {
        this.requestId = requestId;
        calls.put(requestId, this);
    }
    
    public static RPCCall makeRPCCall(int requestId) {
        return new RPCCall(requestId);
    }
    
    public static RPCCall getRPCCall(int requestId) {
        return calls.get(requestId);
    }
    
    public static void removeRPCCall(int requestId) {
        calls.remove(requestId);
    }
}
