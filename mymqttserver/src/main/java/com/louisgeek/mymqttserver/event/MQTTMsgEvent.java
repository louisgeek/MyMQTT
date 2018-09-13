package com.louisgeek.mymqttserver.event;

/**
 * Created by louisgeek on 2018/8/31.
 */
public class MQTTMsgEvent {
    private String topic;

    public MQTTMsgEvent(String topic, String msg) {
        this.topic = topic;
        this.msg = msg;
    }

    private String msg;

    public String getMsg() {
        return msg;
    }

    public String getTopic() {
        return topic;
    }

}
