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
import com.derosa.progettolam.pojo.UserCorrectlyRemoved
import com.derosa.progettolam.pojo.UserCorrectlySignedUp
import com.derosa.progettolam.pojo.UserCorrectlySignedUpToken
import com.derosa.progettolam.pojo.UserNotAuthorized
import com.derosa.progettolam.retrofit.RetrofitInstance
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader


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
        val user = User("angelicacaneee", "ga")
        val user2 = User("angelicacanee", "ga")
        val token =
            "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE3MTg3MDY2ODh9.HLdcaB-Ox3g7w3_1AIoDRw-xL5yjELsciYXceklS2ec"

        RetrofitInstance.api.auth(user).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val userCorrectlySignedUp = gson.fromJson(
                        response.body()!!.string(),
                        UserCorrectlySignedUp::class.java
                    )
                    //LOGICA DOPO AVER REGISTRATO UTENTE
                    Log.d(
                        "Auth 200",
                        "User correctly signed up: ${userCorrectlySignedUp.username}, ID: ${userCorrectlySignedUp.id}"
                    )
                } else {
                    if (response.code() == 400) {
                        //LOGICA SE L'UTENTE ESISTE GIA'
                        val userAlreadyExists = gson.fromJson(
                            response.errorBody()!!.string(),
                            UserAlreadyExists::class.java
                        )
                        Log.d("Auth 400", "Auth 400 Bad Request: " + userAlreadyExists.detail)
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d("Fail Auth", t.message.toString())
            }
        })

        RetrofitInstance.api.authToken(user.username, user.password)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        val userCorrectlySignedUp = gson.fromJson(
                            response.body()!!.string(),
                            UserCorrectlySignedUpToken::class.java
                        )
                        //LOGICA DOPO AVER REGISTRATO UTENTE
                        Log.d(
                            "Auth Token 200",
                            "User correctly signed up: ${userCorrectlySignedUp.client_id}, ID: ${userCorrectlySignedUp.client_secret}"
                        )
                    } else {
                        if (response.code() == 400) {
                            //LOGICA SE LE CREDENZIALI SONO ERRATE
                            val incorrectCredentials = gson.fromJson(
                                response.errorBody()!!.string(),
                                IncorrectCredentials::class.java
                            )
                            Log.d(
                                "Auth Token 400",
                                "Auth Token 400 Bad Request: " + incorrectCredentials.detail
                            )
                        }
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.d("Fail Auth Token", t.message.toString())
                }
            })

        RetrofitInstance.api.authUnsubscribe(user2).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val userCorrectlyRemoved = gson.fromJson(
                        response.body()!!.string(),
                        UserCorrectlyRemoved::class.java
                    )
                    //LOGICA DOPO AVER ELIMINATO UTENTE
                    Log.d(
                        "Auth Unsubscribe 200",
                        "User correctly removed: " + userCorrectlyRemoved.detail
                    )
                } else {
                    if (response.code() == 400) {
                        //LOGICA SE LE CREDENZIALI SONO ERRATE
                        val incorrectCredentials = gson.fromJson(
                            response.errorBody()!!.string(),
                            IncorrectCredentials::class.java
                        )
                        Log.d(
                            "Auth Unsubscribe 400",
                            "Auth Unsubscribe 400 Bad Request: " + incorrectCredentials.detail
                        )
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d("Fail Auth Unsubscribe", t.message.toString())
            }
        })

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
                    if (response.code() == 401) {
                        //LOGICA SE UTENTE NON AUTORIZZATO
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