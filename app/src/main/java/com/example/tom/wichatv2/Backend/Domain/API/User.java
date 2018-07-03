package com.example.tom.wichatv2.Backend.Domain.API;

import java.io.Serializable;

public abstract class User implements Serializable {
    protected String _lowerNick;
    private String _nick;
    private String _prefix;

    public User(String prefix, String nick) {
        this._prefix = prefix;
        this._nick = nick;
        this._lowerNick = nick.toLowerCase();
    }

    public String getPrefix() {
        return this._prefix;
    }

    public boolean isOp() {
        return this._prefix.indexOf(64) >= 0;
    }

    public boolean hasVoice() {
        return this._prefix.indexOf(43) >= 0;
    }

    public String getNick() {
        return this._nick;
    }

    public String toString() {
        return getPrefix() + getNick();
    }

    public boolean equals(String nick) {
        return nick.toLowerCase().equals(this._lowerNick);
    }

    public boolean equals(Object o) {
        return o instanceof User && ((User) o).getNick().toLowerCase().equals(this._lowerNick);
    }

    public int hashCode() {
        return this._lowerNick.hashCode();
    }
}