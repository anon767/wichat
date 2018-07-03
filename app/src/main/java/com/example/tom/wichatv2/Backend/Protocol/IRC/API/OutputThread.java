package com.example.tom.wichatv2.Backend.Protocol.IRC.API;


import android.util.Log;

import java.io.BufferedWriter;

public class OutputThread extends Thread {
    private com.example.tom.wichatv2.Backend.Protocol.IRC.API.IRCClient _bot;
    private Queue _outQueue;

    OutputThread(com.example.tom.wichatv2.Backend.Protocol.IRC.API.IRCClient bot, com.example.tom.wichatv2.Backend.Protocol.IRC.API.Queue outQueue) {
        this._bot = bot;
        this._outQueue = outQueue;
        setName(getClass() + "-Thread");
    }

    static void sendRawLine(com.example.tom.wichatv2.Backend.Protocol.IRC.API.IRCClient bot, BufferedWriter bwriter, String line) {
        if (line.length() > bot.getMaxLineLength() - 2) {
            line = line.substring(0, bot.getMaxLineLength() - 2);
        }
        synchronized (bwriter) {
            try {
                bwriter.write(line + "\r\n");
                bwriter.flush();
            } catch (Exception e) {
                Log.d("OutputThread", "couldnt send line " + line);
            }
        }
    }

    public void run() {
        boolean running = true;
        while (running) {
            try {
                Thread.sleep(this._bot.getMessageDelay());
                String line = (String) this._outQueue.next();
                if (line != null) {
                    this._bot.sendRawLine(line);
                } else {
                    running = false;
                }
            } catch (Exception e) {
                running = false;
            }
        }
    }
}