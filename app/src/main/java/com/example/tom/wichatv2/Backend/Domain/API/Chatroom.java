package com.example.tom.wichatv2.Backend.Domain.API;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Chatroom implements Serializable {
    protected ConcurrentHashMap<User, User> userMap;
    protected ConcurrentHashMap<Integer, Message> messageMap;
    protected String name;
    protected String title = "";

    public Chatroom(String name) {
        this.name = name;
    }
    public void setTitle(String title) {
        this.title = title;
    }


    public String getName() {
        return this.name;
    }

    public void addMessage(Message message) {
        this.messageMap.put(this.messageMap.size(), message);
    }

    public void addUser(User user) {
        this.userMap.put(user, user);
    }

    public Map<User, User> getUserMap() {
        return userMap;
    }

    public Map<Integer, Message> getMessageMap() {
        return messageMap;
    }

    public User findUser(String name) {
        return null;
    }

    public void removeUser(User user) {
        userMap.remove(user);
    }

    public abstract String getTitle();
}
