package com.example.tom.wichatv2

import com.example.tom.wichatv2.Backend.Domain.API.Chatroom
import com.example.tom.wichatv2.Backend.Domain.Impl.ChatroomImpl
import com.example.tom.wichatv2.Backend.Domain.Impl.MessageImpl
import com.example.tom.wichatv2.Backend.Domain.Impl.UserImpl
import com.example.tom.wichatv2.Backend.Domain.Impl.UserImplFactory
import com.example.tom.wichatv2.Backend.Protocol.IRC.Impl.IRCClientImpl
import org.junit.Test

import java.util.*


class IRCUnitTest {
    @Test
    fun connect_working() {
        MyAssertions.assertDoesNotThrow(fun() {
            val chatroom: Chatroom = ChatroomImpl("test", null)
            val ircclient = IRCClientImpl(UserImplFactory(), chatroom)
            ircclient.connect("127.0.0.1", 6667, "test")
            ircclient.join()
            ircclient.sendMessage(MessageImpl("yo", UserImpl("", "tom"), Date(), 0))
        })
    }
}
