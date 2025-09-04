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
package io.github.qylh.iris.example.serverspring;

import io.github.qylh.iris.core.common.annotation.IrisApi;
import io.github.qylh.iris.core.common.annotation.IrisService;
import io.github.qylh.iris.core.common.annotation.IrisTool;
import io.github.qylh.iris.core.common.annotation.IrisToolParam;
import io.github.qylh.iris.example.common.LightControl;
import org.springframework.stereotype.Service;

@IrisService(name = "Light_room_1")
@Service
public class Light1 implements LightControl {
    
    public static volatile boolean status = false;
    
    public static volatile int brightness = 0;
    
    @Override
    @IrisTool(desc = "灯光是否开启")
    @IrisApi(name = "isOpen")
    public Boolean isOpen() {
        return status;
    }
    
    @Override
    @IrisTool(desc = "打开灯光")
    @IrisApi(name = "openLight")
    public Boolean openLight() {
        status = true;
        return true;
    }
    
    @Override
    @IrisTool(desc = "关闭灯光")
    @IrisApi(name = "closeLight")
    public Boolean closeLight() {
        status = false;
        return true;
    }
    
    @Override
    @IrisTool(desc = "设置亮度，1-10")
    @IrisApi(name = "setBrightness")
    public Integer setBrightness(@IrisToolParam(desc = "灯光亮度，范围1-10") Integer i) {
        brightness = i;
        return brightness;
    }
    
    @Override
    @IrisTool(desc = "获取灯光亮度")
    @IrisApi(name = "getBrightness")
    public Integer getBrightness() {
        return brightness;
    }
}
