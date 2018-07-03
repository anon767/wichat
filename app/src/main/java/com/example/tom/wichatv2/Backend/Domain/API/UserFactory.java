package com.example.tom.wichatv2.Backend.Domain.API;

public interface UserFactory<T extends User> {
    T create(String prefix, String nick);
}