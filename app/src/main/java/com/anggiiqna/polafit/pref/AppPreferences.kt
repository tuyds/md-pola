package com.anggiiqna.polafit.pref

import android.content.Context
import android.content.SharedPreferences

class AppPreferences(private val context: Context) {

    private val pref: SharedPreferences = context.getSharedPreferences("app_pref", Context.MODE_PRIVATE)
    private val edit: SharedPreferences.Editor = pref.edit()

    fun saveToken(token: String) {
        edit.putString("token", token)
        edit.apply()
    }

    fun getUserId(id: String): String {
        return pref.getString("id", "") ?: ""
    }

    fun getToken(): String {
        return pref.getString("token", "") ?: ""
    }
}