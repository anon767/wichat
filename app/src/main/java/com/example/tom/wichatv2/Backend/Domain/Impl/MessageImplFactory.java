package com.example.tom.wichatv2.Backend.Domain.Impl;

import com.example.tom.wichatv2.Backend.Domain.API.Message;
import com.example.tom.wichatv2.Backend.Domain.API.MessageFactory;
import com.example.tom.wichatv2.Backend.Domain.API.User;

import java.util.Date;

public class MessageImplFactory implements MessageFactory {
    @Override
    public Message create(String message, User user, Date date, int background) {
        return new MessageImpl(message, user, date, background);
    }
}
