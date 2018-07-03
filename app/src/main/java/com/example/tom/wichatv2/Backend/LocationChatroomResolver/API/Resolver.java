package com.example.tom.wichatv2.Backend.LocationChatroomResolver.API;


import com.example.tom.wichatv2.Backend.Domain.API.Chatroom;
import com.example.tom.wichatv2.Backend.Locator.API.Location;

import java.util.Collection;

public interface Resolver {
    Chatroom getChatroom(Location location);

    void sendLocations(Collection<Location> locations);
}