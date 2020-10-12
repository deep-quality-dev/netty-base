package com.useful.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.useful.client.R;
import com.useful.client.listener.ClientListenerImpl;
import com.useful.client.properties.PropertyUtils;
import com.useful.client.typeClient.TCPClient;

/**
 * Description:
 * User: chenzheng
 * Date: 2016/12/9 0009
 * Time: 18:01
 */
public class MinaTestActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "netty";

    private TextView save_btn, txt_version;

    private EditText ip_tv , port_tv;

    private MessageBroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_mina_test);

        new PropertyUtils(this.getApplication());

        initView();
    }

    private void initView() {
        txt_version = (TextView) this.findViewById(R.id.message_version);
        txt_version.setText("v1.0 netty client on android");

        ip_tv = (EditText) this.findViewById(R.id.ip_tv);
        port_tv = (EditText) this.findViewById(R.id.port_tv);

        save_btn = (TextView) this.findViewById(R.id.save_btn);
        save_btn.setOnClickListener(this);

        ip_tv.setInputType(EditorInfo.TYPE_CLASS_PHONE);
        port_tv.setInputType(EditorInfo.TYPE_CLASS_PHONE);

        String host = "", port = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            host = PropertyUtils.getInstance().getString("tcp.host");
            port = PropertyUtils.getInstance().getString("tcp.port");

        } else {
            SharedPreferences ipport = getSharedPreferences("ipport", Activity.MODE_WORLD_READABLE);
            host = ipport.getString("host", PropertyUtils.getInstance().getString("tcp.host"));
            port = ipport.getString("port", PropertyUtils.getInstance().getString("tcp.port"));
        }

        ip_tv.setText(host);
        port_tv.setText(port);

        Log.i(TAG,"Loading ip and port: " + host + ", " + port);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.save_btn:
                Log.i(TAG,"Setting ip and port");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Toast.makeText(MinaTestActivity.this.getApplicationContext(), "无法修改", Toast.LENGTH_LONG).show();

                } else {
                    String host = ip_tv.getText().toString();
                    Integer port = Integer.valueOf(port_tv.getText().toString());

                    SharedPreferences ipport = getSharedPreferences("ipport", Activity.MODE_WORLD_READABLE);
                    SharedPreferences.Editor editor = ipport.edit();

                    editor.putString("host", host).commit();
                    editor.putString("port", ""+port).commit();
                    editor.apply();

                    Log.i(TAG, "Host = " + host + ", port = " + port);
                    Toast.makeText(MinaTestActivity.this.getApplicationContext(), "修改成功", Toast.LENGTH_LONG).show();
                }

                {
                    TCPClient tcpClient = new TCPClient("test");
                    tcpClient.start(new ClientListenerImpl(this));
                }
                break;
        }
    }
    //启动服务
    private void startServiceTv() {
        Log.e(TAG, "Start service");
    }

    private void unregisterBroadcast(){
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        stopService(new Intent(this, MinaService.class));
        unregisterBroadcast();
        Log.e(TAG, "OnDestroy, reset isStart as false");
    }

    private class MessageBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

//            receive_tv.setText(intent.getStringExtra("message"));
        }
    }
}
