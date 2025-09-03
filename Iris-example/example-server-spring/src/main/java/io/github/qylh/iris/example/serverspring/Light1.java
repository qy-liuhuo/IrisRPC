package io.github.qylh.iris.example.serverspring;

import io.github.qylh.iris.core.common.annotation.IrisApi;
import io.github.qylh.iris.core.common.annotation.IrisService;
import io.github.qylh.iris.core.common.annotation.IrisTool;
import io.github.qylh.iris.core.common.annotation.IrisToolParam;
import io.github.qylh.iris.example.common.LightControl;
import org.springframework.stereotype.Service;

@IrisService(name = "Light_room_1")
@Service
public class Light1 implements LightControl{

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
