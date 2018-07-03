package com.example.tom.wichatv2.Backend.Locator.API;

import com.example.tom.wichatv2.Backend.Locator.Impl.WifiConnectionBean;

import java.util.Collection;

public interface ConnectionReceiver {
    public void receiveLocations(Collection<WifiConnectionBean> wifis);
}
