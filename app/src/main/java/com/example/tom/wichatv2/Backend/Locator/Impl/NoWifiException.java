package com.example.tom.wichatv2.Backend.Locator.Impl;

public class NoWifiException extends Exception {
    public NoWifiException(){
        super("No Wifi Connection");
    }
}
