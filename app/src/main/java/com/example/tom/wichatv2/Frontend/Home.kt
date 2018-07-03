package com.example.tom.wichatv2.Frontend

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.AppCompatActivity
import com.example.tom.wichatv2.Backend.Domain.API.Chatroom
import com.example.tom.wichatv2.Backend.Domain.API.Message
import com.example.tom.wichatv2.Backend.Domain.Impl.ChatroomImpl
import com.example.tom.wichatv2.Backend.Domain.Impl.MessageImplFactory
import com.example.tom.wichatv2.Backend.Domain.Impl.UserImplFactory
import com.example.tom.wichatv2.Backend.Protocol.IRC.Impl.IRCClientImpl
import com.example.tom.wichatv2.Frontend.ViewModel.Message.MessageFragment
import com.example.tom.wichatv2.Frontend.ViewModel.User.UserFragment
import com.example.tom.wichatv2.Frontend.ViewModel.UsernameManager
import com.example.tom.wichatv2.R
import kotlinx.android.synthetic.main.activity_home.*

class Home : AppCompatActivity() {
    var paused: Boolean = false
    private lateinit var client: IRCClientImpl
    var chatroom: Chatroom = ChatroomImpl("lel", this)
    var usernameManager: UsernameManager = UsernameManager()
    private lateinit var clientThread: ClientThread
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        startClientThread()
        val fragmentAdapter = MyPagerAdapter(supportFragmentManager)
        viewpager_main.adapter = fragmentAdapter
        tabs_main.setupWithViewPager(viewpager_main)

    }

    private fun startClientThread() {
        val username = usernameManager.getUsernameOtherwiseAsk(this)
        client = IRCClientImpl(UserImplFactory(), MessageImplFactory(), chatroom)
        clientThread = ClientThread(client, getString(R.string.ircserver), 6697, username)
        clientThread.start()
    }

    override fun onPause() {
        super.onPause()
        paused = true
    }

    override fun onResume() {
        super.onResume()
        if (paused && !client.isConnected) {
            paused = false
            startClientThread()
        }
    }

    fun onSendButtonClicked(message: Message) {
        client.sendMessage(message)
        chatroom.addMessage(message)
        chatroom.notifyMessage()
    }

    class ClientThread(private val client: IRCClientImpl, private val host: String, private val port: Int, private val username: String) : Thread() {
        override fun run() {
            client.connect(host, port, username)
        }
    }

    inner class MyPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> {
                    MessageFragment.newInstance(chatroom)
                }
                else -> {
                    UserFragment.newInstance(chatroom)
                }
            }
        }

        override fun getCount(): Int {
            return 2
        }

        override fun getPageTitle(position: Int): CharSequence {
            return when (position) {
                0 -> chatroom.title
                else -> {
                    return "Online User"
                }
            }
        }
    }


}
