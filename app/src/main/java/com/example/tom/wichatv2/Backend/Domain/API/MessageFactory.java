package com.example.tom.wichatv2.Backend.Domain.API;

import java.util.Date;

public interface MessageFactory<T extends Message> {
    T create(String message, User user, Date date, int background);
}
