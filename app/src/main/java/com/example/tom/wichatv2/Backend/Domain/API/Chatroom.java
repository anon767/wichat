package com.example.tom.wichatv2.Backend.Domain.API;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;

import com.example.tom.wichatv2.R;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Chatroom implements Serializable {
    private transient Activity activity;
    protected ConcurrentHashMap<User, User> userMap;
    protected ConcurrentHashMap<Integer, Message> messageMap;
    protected String name;
    private transient RecyclerView.Adapter userAdapter;
    private transient RecyclerView.Adapter messageAdapter;
    protected String title = "";

    public Chatroom(String name, Activity activity) {
        this.name = name;
        this.activity = activity;

    }

    public void setUserAdapter(RecyclerView.Adapter userAdapter) {

        this.userAdapter = userAdapter;
    }

    public void setMessageAdapter(RecyclerView.Adapter messageAdapter) {
        this.messageAdapter = messageAdapter;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void notifyMessage() {
        if (this.messageAdapter == null) return;
        activity.runOnUiThread(() -> messageAdapter.notifyDataSetChanged());
        RecyclerView messageRecyclerView = activity.findViewById(R.id.reyclerview_message_list);
        messageRecyclerView.smoothScrollToPosition(messageMap.size() - 1);
    }

    public void notifyUser() {
        if (this.userAdapter == null) return;
        activity.runOnUiThread(() -> userAdapter.notifyDataSetChanged());
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
