package com.useful.server.session;

import io.netty.channel.ChannelHandlerContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {

    private static Map<String, ChannelHandlerContext> sessions = new ConcurrentHashMap<String, ChannelHandlerContext>();

    public static void addOrReplace(String clientId, ChannelHandlerContext context) {
        try {
            if (sessions.containsKey(clientId)) {
                sessions.remove(clientId);
            }

            sessions.put(clientId, context);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void remove(String clientId) {
        sessions.remove(clientId);
    }

    public static int getCount() {
        return sessions.size();
    }

    public static ChannelHandlerContext getClient(String clientId) {
        return sessions.get(clientId);
    }
}
