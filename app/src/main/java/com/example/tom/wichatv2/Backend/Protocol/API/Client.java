package com.example.tom.wichatv2.Backend.Protocol.API;

import com.example.tom.wichatv2.Backend.Domain.API.Message;
import com.example.tom.wichatv2.Backend.Domain.API.User;
import com.example.tom.wichatv2.EventBus.Handler;

import java.util.Date;

public interface Client extends Handler {
    void connect(String host, int port, String username) throws Exception;

    boolean isConnected();

    void join();

    void onMessage(Date evDate, String channel, String sender, String login, String hostname, String message);

    void onPrivateMessage(Date evDate, String sender, String login, String hostname, String target, String message);

    void onJoin(String channel, String sender, String login, String hostname);

    void onPart(String channel, String sender, String login, String hostname);

    void onNickChange(String oldNick, String login, String hostname, String newNick);

    void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason);

    void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason);

    void onTopic(String channel, String topic, String setBy, long date, boolean changed);

    void onChannelInfo(String channel, int userCount, String topic);

    void onInvite(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String channel);

    void sendMessage(Message message);

    void onUserList(String channel, User[] users);
}
