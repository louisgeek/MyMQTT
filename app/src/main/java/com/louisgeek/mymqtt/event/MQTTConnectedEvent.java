package com.louisgeek.mymqtt.event;

/**
 * Created by louisgeek on 2018/9/5.
 */
public class MQTTConnectedEvent {

    public MQTTConnectedEvent(String serverURI) {
        this.serverURI = serverURI;
    }

    private String serverURI;

    public String getServerURI() {
        return serverURI;
    }

}
