package com.useful.client.typeClient;

import com.useful.client.handler.HeartbeatHandler;
import com.useful.client.handler.LogicHandler;
import com.useful.client.listener.ClientListener;
import com.useful.client.properties.PropertyUtils;
import com.useful.common.protobuf.Message;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

public class TCPClient {

    private String host;

    private int port;

    private int READER_IDLE_TIME_SECONDS = 10;

    private int WRITER_IDLE_TIME_SECONDS = 0;

    private int ALL_IDLE_TIME_SECONDS = 0;

    private int reconnectTimeout = 3;

    private String clientId;

    private EventLoopGroup eventLoopGroup = new NioEventLoopGroup();

    private ClientListener clientListener;

    public TCPClient(String clientId) {
        this.clientId = clientId;

        this.host = PropertyUtils.getInstance().getString("tcp.host");
        this.port = PropertyUtils.getInstance().getInteger("tcp.port");

        this.READER_IDLE_TIME_SECONDS = PropertyUtils.getInstance().getInteger("socket.READER_IDLE_TIME_SECONDS");
        this.WRITER_IDLE_TIME_SECONDS = PropertyUtils.getInstance().getInteger("socket.WRITER_IDLE_TIME_SECONDS");
        this.ALL_IDLE_TIME_SECONDS = PropertyUtils.getInstance().getInteger("socket.ALL_IDLE_TIME_SECONDS");

        this.reconnectTimeout = PropertyUtils.getInstance().getInteger("client.reconnectTimeout");
    }

    public String getClientId() {
        return clientId;
    }

    public void start(ClientListener clientListener) {
        this.clientListener = clientListener;
        doConnect(new Bootstrap(), eventLoopGroup);
    }

    public void doConnect(final Bootstrap bootstrap, final EventLoopGroup nioEventLoopGroup) {
        try {
            bootstrap.group(nioEventLoopGroup);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    ChannelPipeline channelPipeline = socketChannel.pipeline();

                    channelPipeline.addLast("idleStateHandler", new IdleStateHandler(READER_IDLE_TIME_SECONDS
                            , WRITER_IDLE_TIME_SECONDS, ALL_IDLE_TIME_SECONDS, TimeUnit.SECONDS));

                    channelPipeline.addLast(new ProtobufVarint32FrameDecoder());
                    channelPipeline.addLast(new ProtobufDecoder(Message.MessageBase.getDefaultInstance()));

                    channelPipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
                    channelPipeline.addLast(new ProtobufEncoder());

                    channelPipeline.addLast("idleTimeoutHandler", new HeartbeatHandler(TCPClient.this, TCPClient.this.clientListener));
                    channelPipeline.addLast("clientHandler", new LogicHandler(TCPClient.this, TCPClient.this.clientListener));
                }
            });
            bootstrap.remoteAddress(host, port);
            ChannelFuture channelFuture = bootstrap.connect().addListener(new ChannelFutureListener() {
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (!channelFuture.isSuccess()) {
                        System.out.println("[" + getClientId() + "] 连接服务器失败，5秒后重新连接！");
                        channelFuture.channel().eventLoop().schedule(new Runnable() {
                            public void run() {
                                doConnect(new Bootstrap(), nioEventLoopGroup);
                            }
                        }, reconnectTimeout, TimeUnit.SECONDS);
                    }
                }
            });

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
