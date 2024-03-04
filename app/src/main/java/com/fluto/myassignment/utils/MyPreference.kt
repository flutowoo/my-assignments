package com.fluto.myassignment.utils

import android.content.Context
import android.content.SharedPreferences

object MyPreference {
    private lateinit var prefs: SharedPreferences

    private const val preferenceName = "MyPreference"

    private const val UDER_ID = "userId"
    private const val NICK_NAME = "nickname"
    private const val PROFILE_URL = "profileUrl"

    fun init(context: Context) {
        prefs = context.getSharedPreferences(preferenceName, 0)
    }

    var userId: String
        get() = prefs.getString(UDER_ID, "") ?: ""
        set(value) = prefs.edit().putString(UDER_ID, value).apply()

    var nickname: String
        get() = prefs.getString(NICK_NAME, "") ?: ""
        set(value) = prefs.edit().putString(NICK_NAME, value).apply()

    var profileUrl: String
        get() = prefs.getString(PROFILE_URL, "") ?: ""
        set(value) = prefs.edit().putString(PROFILE_URL, value).apply()
}