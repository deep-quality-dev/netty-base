package com.useful.client.listener;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.useful.common.protobuf.Command;
import com.useful.common.protobuf.Message;

import io.netty.channel.ChannelHandlerContext;

public class ClientListenerImpl implements ClientListener {

    private final String TAG = "netty";

    private Context context;

    public ClientListenerImpl(Context context) {
        this.context = context;
    }

    @Override
    public void onConnected(ChannelHandlerContext channelHandlerContext) {
        Log.i(TAG, "连接成功");
    }

    @Override
    public void onDisconnected() {
        Log.i(TAG, "断开连接");
    }

    @Override
    public void onMessage(ChannelHandlerContext channelHandlerContext, Message.MessageBase message) {
        Log.i(TAG, message.getData());

        if (message.getCommandType() == Command.CommandType.ECHO) {
            channelHandlerContext.writeAndFlush(Message.MessageBase.newBuilder()
                    .setClientId(message.getClientId())
                    .setCommandType(Command.CommandType.ECHO_BACK)
                    .setData("[BACK]" + message.getData()).build());
        }
    }
}
