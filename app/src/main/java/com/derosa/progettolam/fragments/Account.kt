package com.derosa.progettolam.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.derosa.progettolam.R
import com.derosa.progettolam.pojo.ApiResponse
import com.derosa.progettolam.pojo.MyAudioList
import com.derosa.progettolam.pojo.User
import com.derosa.progettolam.retrofit.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Account : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = User(username = "davidevita", password = "gay")
        val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE3MTg2NTA4MzV9.5iz0QakTFYlz55Q0dimgaiCejhEXw_LfhOtHHjEcxis"

        RetrofitInstance.api.auth(user = user).enqueue(object : Callback<ApiResponse>{
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                val apiResponse = response.body()
                Log.d("body", "" + response.code().toString() + "" + response.message())

                when(apiResponse){
                    is ApiResponse.UserCorrectlySignedUp -> {
                        Log.d("success", "UserCorrectlySignedUp")
                    }
                    is ApiResponse.UserCorrectlySignedUpToken -> {
                        Log.d("success", "UserCorrectlySignedUpToken")
                    }
                    is ApiResponse.UserAlreadyExists -> {
                        Log.d("success", "UserAlreadyExists")
                    }
                    is ApiResponse.UserCorrectlyRemoved -> {
                        Log.d("success", "UserCorrectlyRemoved")
                    }
                    is ApiResponse.IncorrectCredentials -> {
                        Log.d("success", "IncorrectCredentials")
                    }

                    else -> {
                        Log.d("null", "response null")
                    }
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.d("fail", t.message.toString())
            }
        })

        RetrofitInstance.api.myAudio(token).enqueue(object : Callback<MyAudioList>{
            override fun onResponse(call: Call<MyAudioList>, response: Response<MyAudioList>) {
                val risposta = response.body()
                Log.d("success", risposta.toString())
            }

            override fun onFailure(call: Call<MyAudioList>, t: Throwable) {
                Log.d("fail", t.message.toString())
            }
        })

    }
}