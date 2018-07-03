package com.example.tom.wichatv2.Frontend.ViewModel

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.tom.wichatv2.Backend.Domain.API.Chatroom
import com.example.tom.wichatv2.R

class UserFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.activity_user_view, container, false)
        super.onCreate(savedInstanceState)

        val chatroom = arguments!!.getSerializable("chatroom") as Chatroom
        viewManager = LinearLayoutManager(rootView.context)
        viewAdapter = UserAdapter(chatroom)
        chatroom.setUserAdapter(viewAdapter)
        recyclerView = rootView.findViewById<RecyclerView>(R.id.recycler_view_user).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
        return rootView
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