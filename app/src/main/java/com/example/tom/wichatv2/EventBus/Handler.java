package com.example.tom.wichatv2.EventBus;

public interface Handler {
    void handle(EventType event, Object object);
}
