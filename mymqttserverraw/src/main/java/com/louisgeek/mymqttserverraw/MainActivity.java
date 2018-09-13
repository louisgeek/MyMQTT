package com.louisgeek.mymqttserverraw;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.louisgeek.mymqttserverraw.event.MQTTConnectedEvent;
import com.louisgeek.mymqttserverraw.event.MQTTLogEvent;
import com.louisgeek.mymqttserverraw.event.MQTTMsgEvent;
import com.louisgeek.mymqttserverraw.message.MessageService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * 不使用官方
 * org.eclipse.paho.android.service.MqttService
 * 设置里的应用的运行服务中不显示 MqttService
 */
public class MainActivity extends AppCompatActivity {

    private TextView id_tv_log;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("C 设备 不调用官方 MqttService 方式");
        //
        EventBus.getDefault().register(this);
        //
        MessageService.actionStart(this);
//
        id_tv_log=findViewById(R.id.id_tv_log);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSubscribe(MQTTMsgEvent event) {
        id_tv_log.setText(id_tv_log.getText() + "\ntopic：" + event.getTopic() +"msg："+ event.getMsg());

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSubscribe(MQTTLogEvent event) {
        id_tv_log.setText(id_tv_log.getText() + "\n" + event.getLog());

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSubscribe(MQTTConnectedEvent event) {
        id_tv_log.setText(id_tv_log.getText() + "\n" + event.getServerURI()+"connected");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        //
        MessageService.actionStop(this);

    }

}
