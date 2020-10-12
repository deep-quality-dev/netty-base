package com.useful.client.handler;

import com.useful.client.listener.ClientListener;
import com.useful.client.typeClient.TCPClient;
import com.useful.common.protobuf.Command;
import com.useful.common.protobuf.Message;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.EventLoopGroup;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import org.springframework.beans.factory.annotation.Value;

import java.util.concurrent.TimeUnit;

public class HeartbeatHandler extends ChannelInboundHandlerAdapter {

    @Value("${client.reconnectTimeout}")
    private int reconnectTimeout;

    private static final int MAX_UN_REC_PONG_TIMES = 3;

    private long startPingTime;

    private final AttributeKey<Integer> pingKey = AttributeKey.valueOf("attributeKeyPingCount");

    private TCPClient tcpClient;

    private ClientListener clientListener;

    public HeartbeatHandler(TCPClient tcpClient, ClientListener clientListener) {
        this.tcpClient = tcpClient;
        this.clientListener = clientListener;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        super.channelRead(ctx, msg);

        final Message.MessageBase message = (Message.MessageBase) msg;
//        System.out.println("[" + ctx.channel().id().asLongText() + "] clientId = " + message.getClientId() +", message = " + message.getData());

        Attribute<Integer> pingAttr = ctx.attr(pingKey);
        pingAttr.set(new Integer(0));

        if (message.getCommandType() == Command.CommandType.PONG) {
            long elapsed = System.currentTimeMillis() - startPingTime;
            System.out.println(tcpClient.getClientId() + ", PING = " + elapsed + "ms");

        } else {
            if (ctx.channel().isOpen()) {
                ctx.fireChannelRead(msg);
            }
        }

        ReferenceCountUtil.release(msg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (this.clientListener != null)
            this.clientListener.onDisconnected();

        final EventLoopGroup eventLoop = ctx.channel().eventLoop();
        ctx.channel().eventLoop().schedule(new Runnable() {
            public void run() {
                tcpClient.doConnect(new Bootstrap(), eventLoop);
            }
        }, reconnectTimeout, TimeUnit.SECONDS);
        super.channelInactive(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);

        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            String type = "";
            if (idleStateEvent.state() == IdleState.READER_IDLE) {
                type = "Reader idle";
            } else if (idleStateEvent.state() == IdleState.WRITER_IDLE) {
                type = "Writer idle";
            } else if (idleStateEvent.state() == IdleState.ALL_IDLE) {
                type = "All idle";
            }

            if (idleStateEvent.state() == IdleState.READER_IDLE) {
                Attribute<Integer> pingAttr = ctx.attr(pingKey);
                Integer pingCount = pingAttr.get();
                if (pingCount == null) {
                    pingCount = new Integer(1);
                } else {
                    pingCount++;
                }
                pingAttr.set(pingCount);

                if (pingCount > MAX_UN_REC_PONG_TIMES) {
                    ctx.channel().close();
                    System.out.println("心跳超时，断开连接");

                } else {
                    sendPingMessage(ctx);
                }
            }

            System.out.println(ctx.channel().remoteAddress()+", 超时类型：" + type);
        }
    }

    private void sendPingMessage(ChannelHandlerContext ctx) {
        startPingTime = System.currentTimeMillis();
        ctx.writeAndFlush(Message.MessageBase.newBuilder()
                .setClientId(tcpClient.getClientId())
                .setCommandType(Command.CommandType.PING)
                .setData(String.valueOf(System.currentTimeMillis())));
    }
}
