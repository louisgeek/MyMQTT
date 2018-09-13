package com.louisgeek.mymqtt;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.louisgeek.mymqtt.event.MQTTConnectedEvent;
import com.louisgeek.mymqtt.event.MQTTLogEvent;
import com.louisgeek.mymqtt.event.MQTTMsgEvent;
import com.louisgeek.mymqtt.mqtt.MessageManager;
import com.louisgeek.mymqtt.mqtt.MessageService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainActivity extends AppCompatActivity {
    private TextView id_tv_log;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("A 设备");
        //
        EventBus.getDefault().register(this);
        //
        MessageService.actionStart(this);
//
        findViewById(R.id.id_btn_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageManager.getInstance().publish("entopic", "设备 A 发送消息：你好！");
            }
        });
        id_tv_log = findViewById(R.id.id_tv_log);

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
