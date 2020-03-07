package com.esmirnov.skillboxchat;

import com.google.gson.Gson;

public class Protocol {
    public static final int USER_STATUS = 1;
    public static final int MESSAGE = 2;
    public static final int USER_NAME = 3;

    static class User {
        private long id;
        private String name;

        public User() {
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    static class UserStatus {
        private User user;
        private boolean connected;

        public UserStatus() {
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }

        public boolean isConnected() {
            return connected;
        }

        public void setConnected(boolean connected) {
            this.connected = connected;
        }
    }

    static class Message {
        private long sender;
        private long receiver;
        private String encodedText;

        public Message(String encodedText) {
            this.encodedText = encodedText;
        }

        public long getSender() {
            return sender;
        }

        public void setSender(long sender) {
            this.sender = sender;
        }

        public long getReceiver() {
            return receiver;
        }

        public void setReceiver(long receiver) {
            this.receiver = receiver;
        }

        public String getEncodedText() {
            return encodedText;
        }

        public void setEncodedText(String encodedText) {
            this.encodedText = encodedText;
        }
    }

    static class UserName {
        private String name;

        public UserName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name.replace("\n", "").replace("\r", "");
        }
    }

    public static int getType(String json) {
        if (json == null || json.length() == 0)
            return -1;
        return Integer.parseInt(json.substring(0, 1));
    }

    public static UserStatus unpackStatus(String json) {
        if (json == null || json.isEmpty())
            return null;
        Gson g = new Gson();
        return g.fromJson(json.substring(1), UserStatus.class);
    }

    public static Message unpackMessage(String json) {
        if (json == null || json.isEmpty())
            return null;        Gson g = new Gson();
        return g.fromJson(json.substring(1), Message.class);
    }

    public static String packName(UserName name) {
        Gson g = new Gson();
        return USER_NAME + g.toJson(name);
    }

    public static String packMessage(Message message) {
        Gson g = new Gson();
        return MESSAGE + g.toJson(message);
    }
}