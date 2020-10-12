package com.useful.server.listener.impl;

import com.useful.common.protobuf.Command;
import com.useful.common.protobuf.Message;
import com.useful.server.listener.ServerListener;
import com.useful.server.session.SessionManager;
import io.netty.channel.ChannelHandlerContext;
import com.google.protobuf.util.JsonFormat;

public class ServerListenerImpl implements ServerListener {

    private final int repeatCount = 500;

    private Thread thread = null;

    public void onClientConnected(final String clientId) {
        final ChannelHandlerContext channelHandlerContext = SessionManager.getClient(clientId);
        thread = new Thread(new Runnable() {
            public void run() {
                for (int idx = 0; idx < repeatCount; idx++) {
                    try {
                        if (channelHandlerContext == null || !channelHandlerContext.channel().isOpen()) {
                            break;
                        }
                        Message.MessageBase message = Message.MessageBase.newBuilder()
                                .setClientId(clientId)
                                .setCommandType(Command.CommandType.ECHO)
                                .setData("TEST MESSAGE-" + idx)
                                .build();
                        channelHandlerContext.writeAndFlush(message);

                        Thread.sleep(100);

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                System.out.println("[" + clientId + "] FINISHED");
            }
        });
        thread.start();
    }

    public void onClientDisconnected(String clientId) {
        try {
            if (thread.isAlive()) {
                thread.interrupt();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void onMessage(String clientId, Message.MessageBase message) {
        try {
            JsonFormat.Printer printer = JsonFormat.printer();
            String json = printer.print(message);
            System.out.println("[" + clientId + "] " + json);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
