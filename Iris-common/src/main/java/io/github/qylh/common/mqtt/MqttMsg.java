package io.github.qylh.common.mqtt;

import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MqttMsg {
    private boolean mutable = true;
    private byte[] payload;
    private int qos = 1;
    private boolean retained = false;
    private boolean dup = false;
    private int messageId;

    public static void validateQos(int qos) {
        if (qos < 0 || qos > 2) {
            throw new IllegalArgumentException();
        }
    }

    public MqttMsg(MqttMessage mqttMessage){
        this.setDuplicate(mqttMessage.isDuplicate());
        this.setPayload(mqttMessage.getPayload());
        this.setQos(mqttMessage.getQos());
        this.setRetained(mqttMessage.isRetained());
        this.setId(mqttMessage.getId());
        this.setMutable(false);
    }

    public MqttMsg() {
        this.setPayload(new byte[0]);
    }

    public MqttMsg(byte[] payload) {
        this.setPayload(payload);
    }

    public byte[] getPayload() {
        return this.payload;
    }

    public void clearPayload() {
        this.checkMutable();
        this.payload = new byte[0];
    }

    public void setPayload(byte[] payload) {
        this.checkMutable();
        if (payload == null) {
            throw new NullPointerException();
        } else {
            this.payload = (byte[])payload.clone();
        }
    }

    public boolean isRetained() {
        return this.retained;
    }

    public void setRetained(boolean retained) {
        this.checkMutable();
        this.retained = retained;
    }

    public int getQos() {
        return this.qos;
    }

    public void setQos(int qos) {
        this.checkMutable();
        validateQos(qos);
        this.qos = qos;
    }

    public String toString() {
        return new String(this.payload);
    }

    protected void setMutable(boolean mutable) {
        this.mutable = mutable;
    }

    protected void checkMutable() throws IllegalStateException {
        if (!this.mutable) {
            throw new IllegalStateException();
        }
    }

    protected void setDuplicate(boolean dup) {
        this.dup = dup;
    }

    public boolean isDuplicate() {
        return this.dup;
    }

    public void setId(int messageId) {
        this.messageId = messageId;
    }

    public int getId() {
        return this.messageId;
    }

    public String toString(String charset) {
        try {
            return new String(this.payload, charset);
        } catch (Exception e) {
            return new String(this.payload);
        }
    }

}
