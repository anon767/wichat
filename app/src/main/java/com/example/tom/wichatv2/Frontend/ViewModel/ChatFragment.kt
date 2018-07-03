package com.example.tom.wichatv2.Frontend.ViewModel

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
import com.example.tom.wichatv2.Backend.Domain.Impl.MessageImpl
import com.example.tom.wichatv2.Backend.Domain.Impl.UserImpl
import com.example.tom.wichatv2.Frontend.Home
import com.example.tom.wichatv2.R
import java.util.*

class ChatFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: LinearLayoutManager


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.activity_chat_view, container, false)
        super.onCreate(savedInstanceState)
        val editText = rootView.findViewById<EditText>(R.id.edittext_chatbox)
        val button = rootView.findViewById<Button>(R.id.button_chatbox_send)
        val chatroom = arguments!!.getSerializable("chatroom") as Chatroom

        viewManager = LinearLayoutManager(rootView.context)
        viewAdapter = MessageAdapter(chatroom, MessageImpl(getString(R.string.errorMessage), UserImpl("", getString(R.string.wichatacc)), Date(), R.drawable.rounded_rectangle_grey))
        chatroom.setMessageAdapter(viewAdapter)
        recyclerView = rootView.findViewById<RecyclerView>(R.id.reyclerview_message_list).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
        viewManager.stackFromEnd = true


        button.setOnClickListener {
            val rawMessage = editText.text.toString()
            if (rawMessage.isNotEmpty()) {
                val message = MessageImpl(rawMessage, UserImpl("", getString(R.string.you)), Date(), R.drawable.rounded_rectangle_orange)
                editText.setText("")
                (activity as Home).onSendButtonClicked(message)
            }
        }
        return rootView
    }

    companion object {
        fun newInstance(chatroom: Chatroom): ChatFragment {
            val fragment = ChatFragment()
            val bundle = Bundle()
            bundle.putSerializable("chatroom", chatroom)
            fragment.arguments = bundle
            return fragment
        }
    }

}