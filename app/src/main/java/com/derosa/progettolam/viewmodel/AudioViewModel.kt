package com.derosa.progettolam.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.derosa.progettolam.pojo.MyAudio
import com.derosa.progettolam.pojo.UserNotAuthorized
import com.derosa.progettolam.retrofit.RetrofitInstance
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AudioViewModel : ViewModel() {

    val gson = Gson()

    fun myAudio(token: String) {
        RetrofitInstance.api.myAudio(token).enqueue(object : Callback<List<MyAudio>> {
            override fun onResponse(call: Call<List<MyAudio>>, response: Response<List<MyAudio>>) {
                if (response.isSuccessful) {
                    val risposta = response.body()
                    if (risposta!!.isNotEmpty()) {
                        for (audio in risposta) {
                            Log.d("My Audio 200", audio.toString())
                        }
                    } else {
                        Log.d("My Audio 200", "size 0")
                    }
                } else {
                    if (response.code() == 401) {   //LOGICA SE UTENTE NON AUTORIZZATO
                        val userNotAuthorized = gson.fromJson(
                            response.errorBody()!!.string(),
                            UserNotAuthorized::class.java
                        )
                        Log.d(
                            "Auth My Audio 401",
                            "Auth My Audio 401 User Not Authorized: " + userNotAuthorized.detail
                        )
                    }
                }
            }

            override fun onFailure(call: Call<List<MyAudio>>, t: Throwable) {
                Log.d("fail", t.message.toString())
            }
        })
    }
}