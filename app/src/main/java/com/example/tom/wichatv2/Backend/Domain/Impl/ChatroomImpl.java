package com.example.tom.wichatv2.Backend.Domain.Impl;

import android.app.Activity;

import com.example.tom.wichatv2.Backend.Domain.API.Chatroom;
import com.example.tom.wichatv2.Backend.Domain.API.User;

import java.util.concurrent.ConcurrentHashMap;

public class ChatroomImpl extends Chatroom {


    public ChatroomImpl(String name, Activity activity) {
        super(name, activity);
        super.userMap = new ConcurrentHashMap<>();
        super.messageMap = new ConcurrentHashMap<>();
    }

    @Override
    public User findUser(String name) {
        User u = new UserImpl("", name);
        super.userMap.put(u, u);
        super.notifyUser();
        return u;
    }

    public String getTitle() {
        if (super.title != null && super.title.length() > 0)
            return super.title;
        else
            return super.name;
    }
}
