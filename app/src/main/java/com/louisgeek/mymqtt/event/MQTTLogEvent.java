package com.louisgeek.mymqtt.event;

/**
 * Created by louisgeek on 2018/8/31.
 */
public class MQTTLogEvent {
    private String log;
    public MQTTLogEvent(String log) {
        this.log = log;
    }
    public String getLog() {
        return log;
    }

}
