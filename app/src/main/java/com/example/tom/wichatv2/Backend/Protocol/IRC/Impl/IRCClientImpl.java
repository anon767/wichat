package com.example.tom.wichatv2.Backend.Protocol.IRC.Impl;

import com.example.tom.wichatv2.Backend.Domain.API.Chatroom;
import com.example.tom.wichatv2.Backend.Domain.API.Message;
import com.example.tom.wichatv2.Backend.Domain.API.MessageFactory;
import com.example.tom.wichatv2.Backend.Domain.API.User;
import com.example.tom.wichatv2.Backend.Domain.API.UserFactory;
import com.example.tom.wichatv2.Backend.Domain.Impl.MessageImpl;
import com.example.tom.wichatv2.Backend.Domain.Impl.UserImpl;
import com.example.tom.wichatv2.Backend.Protocol.API.Client;
import com.example.tom.wichatv2.Backend.Protocol.IRC.API.IRCClient;
import com.example.tom.wichatv2.Backend.Protocol.IRC.API.IRCException;
import com.example.tom.wichatv2.Backend.Protocol.IRC.API.NickAlreadyInUseException;
import com.example.tom.wichatv2.EventBus.EventBus;
import com.example.tom.wichatv2.EventBus.EventType;
import com.example.tom.wichatv2.EventBus.Handler;
import com.example.tom.wichatv2.Frontend.ViewModel.MessageType;
import com.example.tom.wichatv2.R;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class IRCClientImpl extends IRCClient implements Client {

    private MessageFactory messageFactory;

    public IRCClientImpl(UserFactory userFactory, MessageFactory messageFactory, Chatroom chatroom) {
        super(userFactory, chatroom);
        this.messageFactory = messageFactory;
        EventBus.getInstance().addHandler(this);
    }


    public void connect(String host, int port, String username) throws IOException, IRCException {
        try {
            super.connect(host, port, username);
        } catch (NickAlreadyInUseException e) {
            super.connect(host, port, String.format("%s%d", username, (int) (Math.random() * 50 + 1)));
        }
    }


    public void join() {
        super.joinChannel("#" + super.chatroom.getName());
    }

    public void sendMessage(Message message) {
        super.sendMessage("#" + super.chatroom.getName(), message.getMessage());
    }

    @Override
    public void onUserList(String channel, User[] users) {
        Arrays.stream(users).forEach((user) -> chatroom.addUser(user));
        EventBus.getInstance().emit(EventType.NOTIFYUSER, null);
    }


    @Override
    public void onMessage(Date evDate, String channel, String sender, String login, String hostname, String message) {
        super.onMessage(evDate, channel, sender, login, hostname, message);
        Message messageObject = messageFactory.create(message, chatroom.findUser(sender), new Date(), MessageType.FOREIGN.getRessource());
        EventBus.getInstance().emit(EventType.RECEIVEMESSAGE, messageObject);
    }

    @Override
    public void onPrivateMessage(Date evDate, String sender, String login, String hostname, String target, String message) {
        super.onPrivateMessage(evDate, sender, login, hostname, target, message);
    }

    @Override
    public void onJoin(String channel, String sender, String login, String hostname) {
        super.onJoin(channel, sender, login, hostname);
        EventBus.getInstance().emit(EventType.NEWUSER, userFactory.create("", sender));

    }

    @Override
    public void onPart(String channel, String sender, String login, String hostname) {
        super.onPart(channel, sender, login, hostname);
        EventBus.getInstance().emit(EventType.GONEUSER, userFactory.create("", sender));
    }

    @Override
    public void onNickChange(String oldNick, String login, String hostname, String newNick) {
        super.onNickChange(oldNick, login, hostname, newNick);
        EventBus.getInstance().emit(EventType.GONEUSER, userFactory.create("", oldNick));
        EventBus.getInstance().emit(EventType.NEWUSER, userFactory.create("", newNick));
    }

    @Override
    public void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason) {
        super.onKick(channel, kickerNick, kickerLogin, kickerHostname, recipientNick, reason);
    }

    @Override
    public void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason) {
        super.onQuit(sourceNick, sourceLogin, sourceHostname, reason);
    }

    @Override
    public void onTopic(String channel, String topic, String setBy, long date, boolean changed) {
        super.onTopic(channel, topic, setBy, date, changed);
        EventBus.getInstance().emit(EventType.RECEIVEMESSAGE, messageFactory.create(String.format("%s hat den Channelnamen zu %s geändert", setBy, topic), chatroom.findUser(setBy), new Date(), MessageType.SYSTEM.getRessource()));
        EventBus.getInstance().emit(EventType.CHANNELNAME, topic);
    }

    @Override
    public void onChannelInfo(String channel, int userCount, String topic) {
        super.onChannelInfo(channel, userCount, topic);
        EventBus.getInstance().emit(EventType.CHANNELNAME, topic);

    }

    @Override
    public void onInvite(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String channel) {
        super.onInvite(targetNick, sourceNick, sourceLogin, sourceHostname, channel);
    }

    @Override
    public void onConnect() {
        super.onConnect();
        EventBus.getInstance().emit(EventType.CONNECTED, null);
    }

    @Override
    public void handle(EventType event, Object object) {
        switch (event) {
            case SENDMESSAGE:
                this.sendMessage((Message) object);
        }
    }
}
