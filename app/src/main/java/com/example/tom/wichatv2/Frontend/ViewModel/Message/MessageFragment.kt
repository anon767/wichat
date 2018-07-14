package com.example.tom.wichatv2.Frontend.ViewModel.Message

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.example.tom.wichatv2.Backend.Domain.API.Chatroom
import com.example.tom.wichatv2.Backend.Domain.API.Message
import com.example.tom.wichatv2.Backend.Domain.Impl.MessageImpl
import com.example.tom.wichatv2.Backend.Domain.Impl.UserImpl
import com.example.tom.wichatv2.EventBus.EventBus
import com.example.tom.wichatv2.EventBus.EventType
import com.example.tom.wichatv2.EventBus.Handler
import com.example.tom.wichatv2.Frontend.ViewModel.MessageType
import com.example.tom.wichatv2.R
import java.util.*

class MessageFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: LinearLayoutManager
    private lateinit var chatroom: Chatroom

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.activity_chat_view, container, false)
        super.onCreate(savedInstanceState)
        val editText = rootView.findViewById<EditText>(R.id.edittext_chatbox)
        val button = rootView.findViewById<Button>(R.id.button_chatbox_send)
        chatroom = arguments!!.getSerializable("chatroom") as Chatroom
        EventBus.getInstance().addHandler(FrontEndHandler())
        viewManager = LinearLayoutManager(rootView.context)
        viewAdapter = MessageAdapter(chatroom, MessageImpl(getString(R.string.errorMessage), UserImpl("", getString(R.string.wichatacc)), Date(), MessageType.SYSTEM.ressource))
        recyclerView = rootView.findViewById<RecyclerView>(R.id.reyclerview_message_list).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
        viewManager.stackFromEnd = true


        button.setOnClickListener {
            val rawMessage = editText.text.toString()
            if (rawMessage.isNotEmpty()) {
                val message = MessageImpl(rawMessage, UserImpl("", getString(R.string.you)), Date(), MessageType.YOU.ressource)
                editText.setText("")
                EventBus.getInstance().emit(EventType.SENDMESSAGE, message)
                EventBus.getInstance().emit(EventType.RECEIVEMESSAGE, message)
            }
        }
        return rootView
    }

    private fun newMessage(message: Message) {
        chatroom.addMessage(message)
        this.viewAdapter.notifyDataSetChanged()
        recyclerView.smoothScrollToPosition(viewAdapter.itemCount - 1)
    }



    inner class FrontEndHandler : Handler {
        override fun handle(event: EventType?, `object`: Any?) {
            when (event) {
                EventType.RECEIVEMESSAGE -> newMessage(`object` as Message)
                else -> {
                }
            }
        }
    }


    companion object {
        fun newInstance(chatroom: Chatroom): MessageFragment {
            val fragment = MessageFragment()
            val bundle = Bundle()
            bundle.putSerializable("chatroom", chatroom)
            fragment.arguments = bundle
            return fragment
        }
    }

}