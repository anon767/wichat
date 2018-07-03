package com.example.tom.wichatv2.Backend.Domain.Impl;

import com.example.tom.wichatv2.Backend.Domain.API.User;
import com.example.tom.wichatv2.Backend.Domain.API.UserFactory;

    public class UserImplFactory implements UserFactory {
        @Override
        public User create(String prefix, String nick) {
            return new UserImpl(prefix, nick);
        }
    }
