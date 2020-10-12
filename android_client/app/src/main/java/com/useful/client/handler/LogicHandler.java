package com.useful.client.handler;

import com.useful.client.listener.ClientListener;
import com.useful.client.typeClient.TCPClient;
import com.useful.common.protobuf.Command;
import com.useful.common.protobuf.Message;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class LogicHandler extends ChannelInboundHandlerAdapter {

    private TCPClient tcpClient;

    private ClientListener clientListener;

    public LogicHandler(TCPClient tcpClient, ClientListener clientListener) {
        this.tcpClient = tcpClient;
        this.clientListener = clientListener;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

        Message.MessageBase message = Message.MessageBase.newBuilder()
                .setClientId(tcpClient.getClientId())
                .setCommandType(Command.CommandType.AUTH)
                .setData("AUTH")
                .build();

        ctx.writeAndFlush(message);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);

        final Message.MessageBase message = (Message.MessageBase) msg;

        if (message.getCommandType() == Command.CommandType.AUTH_BACK) {
            // start to send logic message
            ctx.writeAndFlush(Message.MessageBase.newBuilder()
                    .setClientId(tcpClient.getClientId())
                    .setCommandType(Command.CommandType.ECHO)
                    .setData("FIRST MESSAGE"));

            if (this.clientListener != null)
                this.clientListener.onConnected(ctx);

        } else {
            if (this.clientListener != null)
                this.clientListener.onMessage(ctx, message);
        }
    }
}
