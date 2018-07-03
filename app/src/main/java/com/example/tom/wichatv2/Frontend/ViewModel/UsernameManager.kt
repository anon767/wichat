package com.example.tom.wichatv2.Frontend.ViewModel

import android.app.Dialog
import android.content.Context
import android.preference.PreferenceManager
import android.widget.Button
import android.widget.EditText
import com.example.tom.wichatv2.R


class UsernameManager {
    private var username: String = ""

    private fun setUsername(context: Context, username: String) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val edit = preferences.edit()
        edit.putString("pref_userName", username)
        edit.apply()
        this.username = username
    }

    private fun getUsername(context: Context): String {
        if (username.isNotEmpty()) return username
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        this.username = preferences.getString("pref_userName", "")
        return this.username
    }

    fun getUsernameOtherwiseAsk(context: Context): String {
        if (!getUsername(context).isNotEmpty())
            askFor(context)
        return this.username
    }

    private fun askFor(context: Context) {
        val myDialog = Dialog(context)
        myDialog.setContentView(R.layout.userpopup)
        myDialog.setCancelable(false)

        val userField = myDialog.findViewById(R.id.userfield) as EditText
        val login = myDialog.findViewById(R.id.loginbutton) as Button
        myDialog.show()
        login.setOnClickListener {
            setUsername(context, userField.text.toString())
            myDialog.dismiss()
        }
    }
}