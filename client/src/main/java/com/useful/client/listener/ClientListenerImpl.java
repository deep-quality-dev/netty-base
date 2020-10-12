package com.useful.client.listener;

import com.useful.common.protobuf.Command;
import com.useful.common.protobuf.Message;
import io.netty.channel.ChannelHandlerContext;

public class ClientListenerImpl implements ClientListener {

    public void onConnected(ChannelHandlerContext channelHandlerContext) {
        System.out.println("连接成功");
    }

    public void onDisconnected() {
        System.out.println("断开连接");
    }

    public void onMessage(ChannelHandlerContext channelHandlerContext, Message.MessageBase message) {
        System.out.println(message.getData());

        if (message.getCommandType() == Command.CommandType.ECHO) {
            channelHandlerContext.writeAndFlush(Message.MessageBase.newBuilder()
                    .setClientId(message.getClientId())
                    .setCommandType(Command.CommandType.ECHO_BACK)
                    .setData("[BACK]" + message.getData()).build());
        }
    }
}
