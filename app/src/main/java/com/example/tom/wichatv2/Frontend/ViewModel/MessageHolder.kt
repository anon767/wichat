package com.example.tom.wichatv2.Frontend.ViewModel

import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.example.tom.wichatv2.Backend.Domain.API.Message
import com.example.tom.wichatv2.R
import java.text.SimpleDateFormat
import java.util.*


open class MessageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private var messageText: TextView = itemView.findViewById(R.id.text_message_body) as TextView
    private var timeText: TextView = itemView.findViewById(R.id.text_message_time) as TextView
    private var nameText: TextView = itemView.findViewById(R.id.text_message_name) as TextView

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    fun bind(message: Message) {
        messageText.text = message.message
        messageText.background = ContextCompat.getDrawable(itemView.context, message.background)
        val df = SimpleDateFormat("HH:mm", Locale.ENGLISH)
        timeText.text = df.format(message.date)
        nameText.text = message.user.nick
    }

}

