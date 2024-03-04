package com.fluto.myassignment.utils

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

object MyPreference {
    private lateinit var prefs: SharedPreferences

    private const val preferenceName = "MyPreference"

    private const val UDER_ID = "userId"
    private const val NICK_NAME = "nickname"
    private const val PROFILE_URL = "profileUrl"
    private const val CHANNEL_TS_MAP = "channelTSMap"
    private const val SP_KEY_CHANNEL_TIMESTAMP = "SP_KEY_CHANNEL_TIMESTAMP"

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

    var channelTSMap: ConcurrentHashMap<String, Long>
        get() {
            val outputMap = ConcurrentHashMap<String, Long>()
            val jsonString =
                prefs.getString(SP_KEY_CHANNEL_TIMESTAMP, JSONObject().toString())
                    ?: return outputMap
            try {
                val jsonObject = JSONObject(jsonString)
                val keysItr = jsonObject.keys()
                while (keysItr.hasNext()) {
                    val key = keysItr.next()
                    val value = jsonObject[key] as Long
                    outputMap[key] = value
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return outputMap
        }
        set(hashMap) = prefs.edit {
            val jsonObject = JSONObject(hashMap as Map<*, *>)
            val jsonString = jsonObject.toString()
            it.remove(SP_KEY_CHANNEL_TIMESTAMP)
            it.putString(SP_KEY_CHANNEL_TIMESTAMP, jsonString)
        }

    fun clear() {
        prefs.edit().clear().apply()
    }

    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = this.edit()
        operation(editor)
        editor.apply()
    }
}