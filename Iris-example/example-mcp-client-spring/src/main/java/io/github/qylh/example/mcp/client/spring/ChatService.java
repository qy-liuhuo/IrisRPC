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
package io.github.qylh.example.mcp.client.spring;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class ChatService {
    
    @Autowired
    private ChatClient chatClient;
    
    public String getResponse(String msg) {
        // ChatClient.CallResponseSpec responseSpec = chatClient.prompt()
        // .system("你是会使用工具的人工智能助手，请帮助用户解决问题")
        // .user(msg)
        // .call();
        // System.out.println(responseSpec.chatResponse());
        // return responseSpec.content();
        return chatClient.prompt().system("你是会使用工具的人工智能助手，请帮助用户解决问题").user(msg).call().content();
    }
    public Flux<String> getStreamResponse(String msg) {
        return chatClient.prompt().user(msg).stream().content();
    }
    
}
