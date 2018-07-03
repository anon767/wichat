package com.example.tom.wichatv2.Frontend.ViewModel.User

import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.example.tom.wichatv2.Backend.Domain.API.User
import com.example.tom.wichatv2.R


open class UserHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private var userTextView: TextView = itemView.findViewById(R.id.row_item) as TextView
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    fun bind(user: User) {
        userTextView.text = String.format("%s%s", user.prefix, user.nick)
    }

}


