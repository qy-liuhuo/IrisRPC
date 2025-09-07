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

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ExampleMcpClientSpringApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ExampleMcpClientSpringApplication.class, args);
    }
    
    @Bean
    public CommandLineRunner commandLineRunner(ChatService chatService) {
        return args -> {
            System.out.println("Input:");
            String input = System.console().readLine();
            while (!input.equalsIgnoreCase("exit")) {
                String response = chatService.getResponse(input);
                System.out.println("Response: " + response);
                System.out.println("Input:");
                input = System.console().readLine();
            }
        };
    }
}
