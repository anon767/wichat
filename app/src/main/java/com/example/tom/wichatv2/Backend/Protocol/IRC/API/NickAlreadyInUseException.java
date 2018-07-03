package com.example.tom.wichatv2.Backend.Protocol.IRC.API;


public class NickAlreadyInUseException extends com.example.tom.wichatv2.Backend.Protocol.IRC.API.IRCException {

    public NickAlreadyInUseException(String e) {
        super(e);
    }
}