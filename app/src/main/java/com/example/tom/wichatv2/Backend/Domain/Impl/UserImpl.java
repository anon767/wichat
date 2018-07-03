package com.example.tom.wichatv2.Backend.Domain.Impl;

import com.example.tom.wichatv2.Backend.Domain.API.User;

public class UserImpl extends User {
    public UserImpl(String prefix, String nick) {
        super(prefix, nick);
    }

    @Override
    public boolean equals(String nick) {
        return nick.toLowerCase().equals(this._lowerNick);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof User && ((User) o).getNick().toLowerCase().equals(this._lowerNick);
    }

    @Override
    public int hashCode() {
        return this._lowerNick.hashCode();
    }
}
