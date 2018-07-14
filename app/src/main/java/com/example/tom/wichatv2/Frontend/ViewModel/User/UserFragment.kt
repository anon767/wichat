package com.example.tom.wichatv2.Frontend.ViewModel.User

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.tom.wichatv2.Backend.Domain.API.Chatroom
import com.example.tom.wichatv2.Backend.Domain.API.Message
import com.example.tom.wichatv2.Backend.Domain.API.User
import com.example.tom.wichatv2.EventBus.EventBus
import com.example.tom.wichatv2.EventBus.EventType
import com.example.tom.wichatv2.EventBus.Handler
import com.example.tom.wichatv2.R

class UserFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var chatroom: Chatroom
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.activity_user_view, container, false)
        super.onCreate(savedInstanceState)
        EventBus.getInstance().addHandler(FrontEndHandler())
        chatroom = arguments!!.getSerializable("chatroom") as Chatroom
        viewManager = LinearLayoutManager(rootView.context)
        viewAdapter = UserAdapter(chatroom)
        recyclerView = rootView.findViewById<RecyclerView>(R.id.recycler_view_user).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
        return rootView
    }

    fun newUser(user: User) {
        this.chatroom.addUser(user)
        this.viewAdapter.notifyDataSetChanged()
    }

    private fun goneUser(user: User) {
        this.chatroom.removeUser(user)
        this.viewAdapter.notifyDataSetChanged()
    }

    inner class FrontEndHandler : Handler {
        override fun handle(event: EventType?, `object`: Any?) {
            when (event) {
                EventType.NEWUSER -> newUser(`object` as User)
                EventType.GONEUSER -> goneUser(`object` as User)
                EventType.NOTIFYUSER -> viewAdapter.notifyDataSetChanged()
                else -> {
                }
            }
        }
    }


    companion object {
        fun newInstance(chatroom: Chatroom): UserFragment {
            val fragment = UserFragment()
            val args = Bundle()
            args.putSerializable("chatroom", chatroom)
            fragment.setArguments(args)
            return fragment
        }
    }

}