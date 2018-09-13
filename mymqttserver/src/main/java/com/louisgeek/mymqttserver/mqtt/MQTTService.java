package com.louisgeek.mymqttserver.mqtt;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.louisgeek.mymqttserver.event.MQTTConnectedEvent;
import com.louisgeek.mymqttserver.event.MQTTLogEvent;
import com.louisgeek.mymqttserver.event.MQTTMsgEvent;
import com.louisgeek.mymqttserver.tool.DeviceTool;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by louisgeek on 2018/8/31.
 */
public class MQTTService extends Service {

    private static final String TAG = MQTTService.class.getSimpleName();
    ///////////////apache activemq
    private String serviceUri = "tcp://10.10.2.39:1884";
    private String userName = "admin";
    private String passWord = "admin";
    private static String[] myServerTopic = {"entopic", "中文topic"};

    ///////////////apache activemq apollo
//    private String mServiceUri = "tcp://0.0.0.0:61613";
//    private String mUsername = "admin";
//    private String mPassword = "password";

    @Override
    public void onCreate() {
        super.onCreate();
        //
        MQTTManager.getInstance().init(this, serviceUri, DeviceTool.getSerial(), userName, passWord, myServerTopic);
        MQTTManager.getInstance().setOnMessageStateListener(new MQTTManager.OnMessageStateListener() {
            @Override
            public void onMessageArrived(String topic, String msg) {
                Log.d(TAG, "onMessageArrived: android 接收信息:" + topic + msg);
                EventBus.getDefault().post(new MQTTMsgEvent(topic, msg));
            }

            @Override
            public void onConnectComplete(boolean reconnect, String serverURI) {
                EventBus.getDefault().post(new MQTTConnectedEvent(serverURI));
            }

            @Override
            public void onShowLog(String log) {
                EventBus.getDefault().post(new MQTTLogEvent(log));
            }

            @Override
            public void onConnectSuccess(String serverURI) {
                EventBus.getDefault().post(new MQTTConnectedEvent(serverURI));
            }

            @Override
            public void onConnectFailure(Throwable exception) {
                Log.e(TAG, "onConnectFailure: ", exception);
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MQTTManager.getInstance().connect();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        MQTTManager.getInstance().release();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    // 服务启动
    public static void actionStart(Context context) {
        Intent intent = new Intent(context, MQTTService.class);
        context.startService(intent);
    }

    // 服务停止
    public static void actionStop(Context context) {
        Intent intent = new Intent(context, MQTTService.class);
        context.stopService(intent);
    }
}
