package io.github.qylh.iris.example.common;

public interface LightControl {

    public Boolean isOpen();

    public Boolean openLight();

    public Boolean closeLight();

    public Integer setBrightness(Integer brightness);

    public Integer getBrightness();
}
