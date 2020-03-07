package com.esmirnov.skillboxchat;

import android.util.Log;
import android.util.Pair;

import androidx.core.util.Consumer;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.protocols.IProtocol;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    WebSocketClient client;
    private Map<Long, String> namesMap = new ConcurrentHashMap<>();

    private Consumer<Pair<String, String>> messageConsumer;
    private Consumer<StatusNotification> countConsumer;

    public Server(Consumer<Pair<String, String>> messageConsumer,
                  Consumer<StatusNotification> countConsumer) {
        this.messageConsumer = messageConsumer;
        this.countConsumer = countConsumer;
    }

    public void connect() {
        URI addr;
        try {
            addr = new URI("ws://35.210.129.230:8881/");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        client = new WebSocketClient(addr) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                Log.i("WSSERVER", "Connected to server");
            }

            @Override
            public void onMessage(String encodedMessge) {
                Log.i("WSSERVER", encodedMessge);

                int type = Protocol.getType(encodedMessge);
                switch (type)
                {
                    case Protocol.USER_STATUS:
                        onUserStatus(encodedMessge);
                        break;
                    case Protocol.MESSAGE:
                        onIncomingTextMessage(encodedMessge);
                        break;
                    case Protocol.USER_NAME:
                        break;
                }
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                Log.i("WSSERVER", "onClose" + reason);
            }

            @Override
            public void onError(Exception ex) {
                Log.i("WSSERVER", "onError" + ex.toString());
            }
        };
        client.connect();
    }

    public void disconnect() {
        client.close();
    }

    private void onIncomingTextMessage(String encodedMessage) {
        Protocol.Message message = Protocol.unpackMessage(encodedMessage);
        String senderName = namesMap.get(message.getSender());
        if (senderName == null || senderName.isEmpty())
            senderName = "<unknown>";
        messageConsumer.accept(new Pair<>(message.getEncodedText(), senderName));
    }

    public static class StatusNotification {
        private long onlineCount;
        private String userName;
        private boolean isConnected;

        public StatusNotification(long onlineCount, String userName, boolean isConnected) {
            this.onlineCount = onlineCount;
            this.userName = userName;
            this.isConnected = isConnected;
        }

        public long getOnlineCount() {
            return onlineCount;
        }

        public String getUserName() {
            return userName;
        }

        public boolean isConnected() {
            return isConnected;
        }
    }

    private void onUserStatus(String encodedMessage) {
        Protocol.UserStatus status = Protocol.unpackStatus(encodedMessage);
        Protocol.User user = status.getUser();
        String userName = user.getName();
        if (status.isConnected()) {
            namesMap.put(user.getId(), userName);
        } else {
            namesMap.remove(user.getId());
        }
        countConsumer.accept(new StatusNotification(
                namesMap.size(), userName, status.isConnected()));
    }

    public void sendMessage(String messageText) {
        String json = Protocol.packMessage(new Protocol.Message(messageText));
        if (client != null && client.isOpen())
            client.send(json);
    }

    public void sendName(String name) {
        String json = Protocol.packName(new Protocol.UserName(name));
        if (client != null && client.isOpen())
            client.send(json);
    }
}
