package com.louisgeek.mymqttserverraw.message;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.louisgeek.mymqttserverraw.event.MQTTConnectedEvent;
import com.louisgeek.mymqttserverraw.event.MQTTLogEvent;
import com.louisgeek.mymqttserverraw.event.MQTTMsgEvent;
import com.louisgeek.mymqttserverraw.tool.DeviceTool;
import com.louisgeek.mymqttserverraw.tool.UIToast;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by louisgeek on 2018/8/31.
 */
public class MessageService extends Service {

    private static final String TAG = MessageService.class.getSimpleName();
    //    private String serviceUri = "tcp://0.0.0.0:61616";
    private String serviceUri = "tcp://10.10.2.39:1884";
//    private String serviceUri = "tcp://10.0.38.83:1883";
    private String userName = "admin";
    private String passWord = "admin";
    private static String[] myServerTopic = {"entopic", "中文topic"};

    ///////////////apache apollo
//    private String mServiceUri = "tcp://0.0.0.0:61613";
//    private String mUsername = "admin";
//    private String mPassword = "password";
    Context mContext;
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        //
        MessageManager.getInstance().init(this, serviceUri, DeviceTool.getSerial(), userName, passWord, myServerTopic);
        MessageManager.getInstance().setOnMessageStateListener(new MessageManager.OnMessageStateListener() {
            @Override
            public void onMessageArrived(String topic, String msg) {
                Log.d(TAG, "onMessageArrived: android 接收信息:" + topic + msg);
                EventBus.getDefault().post(new MQTTMsgEvent(topic, msg));
                UIToast.show(mContext,msg);
            }
            @Override
            public void onShowLog(String log) {
                EventBus.getDefault().post(new MQTTLogEvent(log));
                UIToast.show(mContext,log);
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
        MessageManager.getInstance().connect();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        MessageManager.getInstance().release();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    // 服务启动
    public static void actionStart(Context context) {
        Intent intent = new Intent(context, MessageService.class);
        context.startService(intent);
    }
    // 服务停止
    public static void actionStop(Context context) {
        Intent intent = new Intent(context, MessageService.class);
        context.stopService(intent);
    }
}