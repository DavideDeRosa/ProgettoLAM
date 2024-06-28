package com.derosa.progettolam.util

import android.content.Context

object SharedPrefUtil {
    private const val PREF_NAME = "my_app_preferences"
    private const val KEY_TOKEN = "token"
    private const val KEY_USERNAME = "username"

    private fun getSharedPreferences(context: Context) =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun saveTokenAndUsername(token: String, username: String, context: Context) {
        val sharedPref = getSharedPreferences(context)
        with(sharedPref.edit()) {
            putString(KEY_TOKEN, token)
            putString(KEY_USERNAME, username)
            apply()
        }
    }

    fun getTokenAndUsername(context: Context): Pair<String?, String?> {
        val sharedPref = getSharedPreferences(context)
        val token = sharedPref.getString(KEY_TOKEN, null)
        val username = sharedPref.getString(KEY_USERNAME, null)
        return Pair(token, username)
    }

    fun clearTokenAndUsername(context: Context) {
        val sharedPref = getSharedPreferences(context)
        with(sharedPref.edit()) {
            remove(KEY_TOKEN)
            remove(KEY_USERNAME)
            apply()
        }
    }
}
