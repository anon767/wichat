package com.example.tom.wichatv2.Backend.Domain.Impl;

import com.example.tom.wichatv2.Backend.Domain.API.Message;
import com.example.tom.wichatv2.Backend.Domain.API.User;

import java.util.Date;

public class MessageImpl extends Message {
    public MessageImpl(String message, User user, Date date, int background) {
        super.message = message;
        super.user = user;
        super.date = date;
        super.background = background;
    }

}
