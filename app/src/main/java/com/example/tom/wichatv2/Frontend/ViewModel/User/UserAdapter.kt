package com.example.tom.wichatv2.Frontend.ViewModel.User

import android.annotation.TargetApi
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.tom.wichatv2.Backend.Domain.API.Chatroom
import com.example.tom.wichatv2.Backend.Domain.API.User
import com.example.tom.wichatv2.R


class UserAdapter(private val chatroom: Chatroom) :
        RecyclerView.Adapter<UserHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): UserHolder {
        val view: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.user_item, parent, false)
        return UserHolder(view)
    }

    @TargetApi(Build.VERSION_CODES.N)
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onBindViewHolder(holder: UserHolder, position: Int) {
        val user: Pair<User, User>? = chatroom.userMap.toList()[position]
        if (user !== null)
            holder.bind(user.second)
    }

    override fun getItemCount() = chatroom.userMap.size
}