package com.useful.client.listener;

import com.useful.common.protobuf.Message;

import io.netty.channel.ChannelHandlerContext;

public interface ClientListener {

    void onConnected(ChannelHandlerContext channelHandlerContext);

    void onDisconnected();

    void onMessage(ChannelHandlerContext channelHandlerContext, Message.MessageBase message);
}
