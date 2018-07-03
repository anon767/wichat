package com.example.tom.wichatv2.Backend.Locator.Impl;


import android.net.wifi.WifiManager;

import com.example.tom.wichatv2.Backend.Locator.API.ConnectionReceiver;

public class WifiLocator {

    private WifiReceiver receiverWifi;
    private WifiManager wifiManager;

    public WifiLocator(WifiManager wifiManager) {
        this.wifiManager = wifiManager;

    }

    public WifiReceiver getReceiver(ConnectionReceiver receiver) {
        if (this.receiverWifi == null) {
            this.receiverWifi = new WifiReceiver(this.wifiManager, receiver);
        }
        return this.receiverWifi;
    }

    public void startScan() throws NoWifiException {
        if (this.receiverWifi == null || this.wifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLED) {
            throw new NoWifiException();
        }
        this.wifiManager.startScan();
    }



    /*
    public WifiReceiver initWifis() throws NoWifiException {


        //getApplicationContext().registerReceiver(this.receiverWifi, new IntentFilter("android.net.wifi.SCAN_RESULTS"));
        return this.receiverWifi;
    }
    public void getWifis(ArrayList<WifiConnectionBean> wifis) {
        String wifistring = "";
        Iterator it = wifis.iterator();
        while (it.hasNext()) {
            wifistring = wifistring + ((WifiConnectionBean) it.next()).getId().hashCode() + "|";
        }
        this.lastWifis = wifistring;
        sendrequest("");
    }
*/
}