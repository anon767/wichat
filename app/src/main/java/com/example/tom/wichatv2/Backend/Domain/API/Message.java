package com.example.tom.wichatv2.Backend.Domain.API;

import java.io.Serializable;
import java.util.Date;

public abstract class Message implements Serializable {
    protected String message;
    protected Date date;
    protected User user;
    protected int background;

    public int getBackground() {
        return background;
    }

    public void setColor(int background) {
        this.background = background;
    }

    public User getUser() {
        return this.user;
    }

    public String getMessage() {
        return this.message;
    }

    public Date getDate() {
        return this.date;
    }
}
