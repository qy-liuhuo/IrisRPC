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

import io.github.qylh.iris.core.common.msg.MqttRequest;
import io.github.qylh.iris.core.common.msg.MqttResponse;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ClientProxy implements InvocationHandler {
    
    private final Requester requestInvoker;
    
    private final Class<?> serviceClazz;
    
    public ClientProxy(Requester requestInvoker, Class<?> serviceClazz) {
        this.requestInvoker = requestInvoker;
        this.serviceClazz = serviceClazz;
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        MqttRequest request = new MqttRequest();
        request.setServiceName(this.serviceClazz.getSimpleName());
        request.setMethodName(method.getName());
        request.setArgs(args);
        request.setArgsType(method.getParameterTypes());
        MqttResponse response = requestInvoker.request(request);
        if (response == null) {
            return null;
        }
        return response.getData();
    }
    
    public <T> T getProxy() {
        Object o = Proxy.newProxyInstance(serviceClazz.getClassLoader(), new Class[]{serviceClazz}, this);
        return (T) o;
    }
}
