package com.derosa.progettolam.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.derosa.progettolam.R
import com.derosa.progettolam.pojo.IncorrectCredentials
import com.derosa.progettolam.pojo.MyAudio
import com.derosa.progettolam.pojo.User
import com.derosa.progettolam.pojo.UserAlreadyExists
import com.derosa.progettolam.pojo.UserCorrectlySignedUp
import com.derosa.progettolam.pojo.UserCorrectlySignedUpToken
import com.derosa.progettolam.retrofit.RetrofitInstance
import com.google.gson.Gson
import okhttp3.ResponseBody
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

        val gson = Gson()
        val user = User(username = "angelica", password = "gay")
        val token =
            "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE3MTg2NjczNTJ9.2FkjStdekRpGRSqs_omquezlO1oWltrGCL28_E9GZQU"

        RetrofitInstance.api.auth(user).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                val responseBody = response.body()
                Log.d("Body auth", ""+ response.code() + response.message())

                if (responseBody != null) {
                    val responseBody = response.body()!!.string()
                    try {
                        when (response.code()) {
                            200 -> {
                                val userCorrectlySignedUp = gson.fromJson(
                                    responseBody,
                                    UserCorrectlySignedUp::class.java
                                )
                                Log.d(
                                    "Auth 200",
                                    "User correctly signed up: ${userCorrectlySignedUp.username}, ID: ${userCorrectlySignedUp.id}"
                                )
                            }

                            400 -> {
                                val userAlreadyExists = gson.fromJson(
                                    responseBody,
                                    UserAlreadyExists::class.java
                                )
                                Log.d(
                                    "Auth 400",
                                    "User correctly signed up: ${userAlreadyExists.detail}"
                                )
                            }

                            else -> {
                                Log.e(
                                    "API Error",
                                    "Unexpected response code: ${response.code()}"
                                )
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("API Error", "Failed to parse response: ${e.message}")
                    }
                } else {
                    Log.e("API Error", "Response body is null")
                }


            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d("fail", t.message.toString())
            }
        })

        RetrofitInstance.api.authToken(user.username, user.password)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {

                    val responseBody = response.body()!!.string()

                    if (responseBody != null) {
                        try {
                            when (response.code()) {
                                200 -> {
                                    val userCorrectlySignedUp = gson.fromJson(
                                        responseBody,
                                        UserCorrectlySignedUpToken::class.java
                                    )
                                    Log.d(
                                        "AuthToken 200",
                                        "User correctly signed up: ${userCorrectlySignedUp.client_id}, ID: ${userCorrectlySignedUp.client_secret}"
                                    )
                                }

                                400 -> {
                                    val incorrectCredentials = gson.fromJson(
                                        responseBody,
                                        IncorrectCredentials::class.java
                                    )
                                    Log.d(
                                        "AuthToken 400",
                                        "Incorrect credentials: ${incorrectCredentials.detail}"
                                    )
                                }

                                else -> {
                                    Log.e(
                                        "API Error",
                                        "Unexpected response code: ${response.code()}"
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("API Error", "Failed to parse response: ${e.message}")
                        }
                    } else {
                        Log.e("API Error", "Response body is null")
                    }

                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.d("fail", t.message.toString())
                }
            })

        RetrofitInstance.api.myAudio(token).enqueue(object : Callback<List<MyAudio>> {
            override fun onResponse(call: Call<List<MyAudio>>, response: Response<List<MyAudio>>) {
                if (response.isSuccessful) {
                    val risposta = response.body()
                    if (risposta!!.isNotEmpty()) {
                        for (audio in risposta) {
                            Log.d("Audio", audio.toString())
                        }
                    } else {
                        Log.d("Audio", "size 0")
                    }
                } else {
                    if (response.code() == 401) {
                        Log.d("Error", "" + response.code() + response.message())
                    }
                }
            }

            override fun onFailure(call: Call<List<MyAudio>>, t: Throwable) {
                Log.d("fail", t.message.toString())
            }
        })
    }
}