package com.example.tom.wichatv2.Backend.Locator.Impl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.example.tom.wichatv2.Backend.Locator.API.ConnectionReceiver;

import java.util.HashSet;
import java.util.List;

class WifiReceiver extends BroadcastReceiver {
    HashSet<WifiConnectionBean> m4MessagesList;
    WifiManager mainWifi;
    ConnectionReceiver receiver;

    public WifiReceiver(WifiManager mainWifi, ConnectionReceiver receiver) {
        this.mainWifi = mainWifi;
        this.receiver = receiver;
        this.m4MessagesList = new HashSet<>();
    }

    @RequiresApi(Build.VERSION_CODES.N)
    public void onReceive(Context c, Intent intent) {
        List<ScanResult> wifiList = this.mainWifi.getScanResults();
        wifiList.forEach((scanResult) -> {
            WifiConnectionBean bean = new WifiConnectionBean();
            bean.setConnectionName(scanResult.SSID);
            bean.setDescription(scanResult.capabilities);
            bean.setId(scanResult.BSSID);
            bean.setLevel(String.valueOf(scanResult.level));
            this.m4MessagesList.add(bean);
        });

        receiver.receiveLocations(this.m4MessagesList);
    }
}