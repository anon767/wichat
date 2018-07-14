package com.example.tom.wichatv2.EventBus;

import java.util.ArrayList;

public class EventBus {
    private static EventBus instance;

    private EventBus() {
    }

    private ArrayList<Handler> handlerList = new ArrayList<>();

    public void addHandler(Handler handler) {
        this.handlerList.add(handler);
    }

    public void emit(EventType event, Object object) {
        this.handlerList.forEach(handler -> handler.handle(event, object));
    }

    public static synchronized EventBus getInstance() {
        if (EventBus.instance == null) {
            EventBus.instance = new EventBus();
        }
        return EventBus.instance;
    }
}
