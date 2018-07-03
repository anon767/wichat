package com.example.tom.wichatv2.Backend.Locator.Impl;

import com.example.tom.wichatv2.Backend.Locator.API.Location;

public class WifiConnectionBean implements Location {
    String connectionName;
    String description;
    String id;
    String level;

    WifiConnectionBean() {
    }

    public String getConnectionName() {
        return this.connectionName;
    }

    public String getId() {
        return this.id;
    }

    public void setConnectionName(String connectionName) {
        this.connectionName = connectionName;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLevel() {
        return this.level;
    }

    public void setLevel(String level) {
        this.level = level;
    }
}