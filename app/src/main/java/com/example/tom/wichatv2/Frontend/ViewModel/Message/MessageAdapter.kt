package com.example.tom.wichatv2.Frontend.ViewModel.Message

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.tom.wichatv2.Backend.Domain.API.Chatroom
import com.example.tom.wichatv2.Backend.Domain.API.Message
import com.example.tom.wichatv2.R

class MessageAdapter(private val chatroom: Chatroom, private val defaultMessage: Message) :
        RecyclerView.Adapter<MessageHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): MessageHolder {
        val view: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.chat_message, parent, false)
        return MessageHolder(view)
    }


    override fun onBindViewHolder(holder: MessageHolder, position: Int) {
        holder.bind(chatroom.messageMap.getOrPut(position) { defaultMessage })

    }

    override fun getItemCount() = chatroom.messageMap.size
}