package com.louisgeek.mymqttserver.mqtt;

import android.content.Context;
import android.util.Log;

import com.louisgeek.mymqttserver.tool.NetTool;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by louisgeek on 2018/9/3.
 * 使用 MqttAndroidClient
 * 需要设置
 * <service android:name="org.eclipse.paho.android.service.MqttService" />
 */
public class MQTTManager {
    private static final String TAG = "MQTTManager";

    public static MQTTManager getInstance() {
        return Inner.INSTANCE;
    }

    private static class Inner {
        private static final MQTTManager INSTANCE = new MQTTManager();
    }

    //////////////////
    private MqttAndroidClient mMqttAndroidClient;
    private MqttConnectOptions mMqttConnectOptions;
    private Context mContext;

    private int mQos = 2;
    //Object 可以为空 单纯的为了去重复
    private Map<String, Object> mSubscribeTopicMap = new HashMap<>();

    public void init(Context context, String serverURI, String clientId, String username, String password, String[] subscribeTopicArr) {
        Log.e(TAG, "init: ");
        mContext = context.getApplicationContext();
        for (String subscribeTopic : subscribeTopicArr) {
            mSubscribeTopicMap.put(subscribeTopic, null);
        }
        //serviceUri 服务器地址（协议+地址+端口号）
//        mMqttAndroidClient = new MqttAndroidClient(context, serviceUri, clientId);
        mMqttAndroidClient = new MqttAndroidClient(context, serverURI, clientId, new MemoryPersistence());
        // 设置 MqttCallbackExtended 包含重连
//        mMqttAndroidClient.setCallback(new MqttCallback() {
        mMqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                //也可以直接在这里统一订阅消息
                if (reconnect) {
                    //重连 需要重新订阅
                    subscribe();
                }
                Log.e(TAG, "connectComplete: reconnect:" + reconnect);
                if (mOnMessageStateListener != null) {
                    mOnMessageStateListener.onConnectComplete(reconnect, serverURI);
                }
                if (mOnMessageStateListener != null) {
                    mOnMessageStateListener.onShowLog(serverURI + "connectComplete" + reconnect);
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                Log.e(TAG, "connectionLost: ", cause);
                if (mOnMessageStateListener != null) {
                    mOnMessageStateListener.onShowLog("connectionLost");
                }
            }

            @Override
            public void messageArrived(final String topic, MqttMessage message) throws Exception {
                final String msg = new String(message.getPayload());
                if (mOnMessageStateListener != null) {
                    mOnMessageStateListener.onMessageArrived(topic, msg);
                }
                Log.i(TAG, topic + "messageArrived:" + msg);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                //服务器成功delivery消息
                Log.i(TAG, "deliveryComplete:");
                if (mOnMessageStateListener != null) {
                    mOnMessageStateListener.onShowLog("deliveryComplete");
                }
            }
        });
        /////////
        mMqttConnectOptions = new MqttConnectOptions();
        // 清除以前的Session缓存
        mMqttConnectOptions.setCleanSession(true);
        // 设置超时时间，单位：秒
        mMqttConnectOptions.setConnectionTimeout(10);
        // 心跳包发送间隔，单位：秒
        mMqttConnectOptions.setKeepAliveInterval(20);
        // 用户名
        mMqttConnectOptions.setUserName(username);
        // 密码
        mMqttConnectOptions.setPassword(password != null ? password.toCharArray() : null);
        //
        mMqttConnectOptions.setAutomaticReconnect(true);
        //允许同时发送几条消息 默认10
//        mMqttConnectOptions.setMaxInflight(10);
        // last will message
        //setWill方法，如果项目中需要知道客户端是否掉线可以调用该方法。设置最终端口的通知消息
//              options.setWill(topic, "close".getBytes(), mQos, true);
        //
        connect();

    }

    public void publish(String publishTopic, String msg) {
        //是否保留最后的断开连接信息
        boolean retained = false;
        try {
            mMqttAndroidClient.publish(publishTopic, msg.getBytes(), mQos, retained);
        } catch (MqttException e) {
            e.printStackTrace();
            if (mOnMessageStateListener != null) {
                mOnMessageStateListener.onShowLog(e.getMessage());
            }
            Log.e(TAG, "error:" + e.getMessage());
        }
    }

    private void subscribe() {
        if (mSubscribeTopicMap.isEmpty()) {
            return;
        }
        //Qos=0时，报文最多发送一次，有可能丢失
        //Qos=1时，报文至少发送一次，有可能重复
        //Qos=2时，报文只发送一次，并且确保消息只到达一次。
        int[] qosArr = new int[mSubscribeTopicMap.size()];
        //赋值
        Arrays.fill(qosArr, 2);
        String[] subscribeTopicArr = new String[mSubscribeTopicMap.size()];
        //赋值
        mSubscribeTopicMap.keySet().toArray(subscribeTopicArr);
        try {
            mMqttAndroidClient.subscribe(subscribeTopicArr, qosArr);

        } catch (MqttException e) {
            e.printStackTrace();
            if (mOnMessageStateListener != null) {
                mOnMessageStateListener.onShowLog(e.getMessage());
            }
        }
    }

    public void unsubscribe(String topic) {
        if (mSubscribeTopicMap.isEmpty()) {
            return;
        }
        mSubscribeTopicMap.remove(topic);
        try {
            mMqttAndroidClient.unsubscribe(topic);
        } catch (MqttException e) {
            e.printStackTrace();
            if (mOnMessageStateListener != null) {
                mOnMessageStateListener.onShowLog(e.getMessage());
            }
            Log.e(TAG, "error:" + e.getMessage());
        }
    }

    public void unsubscribe() {
        if (mSubscribeTopicMap.isEmpty()) {
            return;
        }
        String[] subscribeTopicArr = new String[mSubscribeTopicMap.size()];
        //赋值
        mSubscribeTopicMap.keySet().toArray(subscribeTopicArr);
        try {
            mMqttAndroidClient.unsubscribe(subscribeTopicArr);
        } catch (MqttException e) {
            e.printStackTrace();
            if (mOnMessageStateListener != null) {
                mOnMessageStateListener.onShowLog(e.getMessage());
            }
            Log.e(TAG, "error:" + e.getMessage());
        }
    }

    public void connect() {
        if (!NetTool.isNetConnected(mContext)) {
            return;
        }
        if (!mMqttAndroidClient.isConnected()) {
            //
            try {
                mMqttAndroidClient.connect(mMqttConnectOptions, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.d(TAG, "onSuccess: ");
                        //成功连接以后开始订阅 注意也可以都在  MqttCallbackExtended 的 connectComplete  中订阅
                        subscribe();
                        if (mOnMessageStateListener != null) {
                            mOnMessageStateListener.onShowLog("onSuccess");
                        }
                        if (mOnMessageStateListener != null) {
                            String serverURI = asyncActionToken.getClient().getServerURI();
                            mOnMessageStateListener.onConnectSuccess(serverURI);

                        }
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Log.e(TAG, "onFailure: ", exception);
                        if (mOnMessageStateListener != null) {
                            mOnMessageStateListener.onShowLog("onFailure");
                        }
                        if (mOnMessageStateListener != null) {
                            mOnMessageStateListener.onConnectFailure(exception);
                        }

                    }
                });
                //
            } catch (MqttException e) {
                e.printStackTrace();
                if (mOnMessageStateListener != null) {
                    mOnMessageStateListener.onShowLog(e.getMessage());
                }
                Log.e(TAG, "error:" + e.getMessage());
            }
        }
    }


    public void release() {
        mMqttAndroidClient.unregisterResources();
        mMqttAndroidClient.close();
        mMqttAndroidClient = null;
        Log.d(TAG, "release: ");
    }

    public void releaseOld() {
        try {
            mMqttAndroidClient.unregisterResources();
            mMqttAndroidClient.close();
            mMqttAndroidClient.disconnect();
            mMqttAndroidClient = null;
        } catch (MqttException e) {
            e.printStackTrace();
            if (mOnMessageStateListener != null) {
                mOnMessageStateListener.onShowLog(e.getMessage());
            }
        }
    }

    private OnMessageStateListener mOnMessageStateListener;

    public void setOnMessageStateListener(OnMessageStateListener onMessageStateListener) {
        mOnMessageStateListener = onMessageStateListener;
    }

    public interface OnMessageStateListener {
        void onMessageArrived(String topic, String msg);

        void onConnectComplete(boolean reconnect, String serverURI);

        void onShowLog(String log);

        void onConnectSuccess(String serverURI);

        void onConnectFailure(Throwable exception);
    }
}
