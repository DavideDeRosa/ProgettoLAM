package com.derosa.progettolam.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

object ExtraUtil {
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

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo ?: return false
            return networkInfo.isConnected
        }
    }

    fun isWifiConnected(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            return activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo ?: return false
            return networkInfo.type == ConnectivityManager.TYPE_WIFI
        }
    }
}
