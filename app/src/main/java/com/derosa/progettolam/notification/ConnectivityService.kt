package com.derosa.progettolam.notification

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.IBinder
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.derosa.progettolam.MyApp
import com.derosa.progettolam.db.AudioDatabase
import com.derosa.progettolam.util.DataSingleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ConnectivityService : Service() {

    private lateinit var connectivityReceiver: BroadcastReceiver
    private lateinit var audioDatabase: AudioDatabase
    private var size = 0
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        audioDatabase = AudioDatabase.getInstance(this)

        connectivityReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == ConnectivityManager.CONNECTIVITY_ACTION) {
                    val connectivityManager =
                        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                    val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo

                    if (networkInfo != null && networkInfo.type == ConnectivityManager.TYPE_WIFI && networkInfo.isConnected) {
                        if (!MyApp.isAppInForeground && DataSingleton.username != null) {
                            serviceScope.launch {
                                withContext(Dispatchers.IO) {
                                    size = audioDatabase.uploadDataDao()
                                        .getAllUploadByUsernameInt(DataSingleton.username!!).size
                                }

                                if (size > 0) {
                                    val workRequest =
                                        OneTimeWorkRequest.Builder(WifiStateWorker::class.java)
                                            .build()
                                    WorkManager.getInstance(context).enqueue(workRequest)

                                    size = 0
                                }
                            }
                        }
                    }
                }
            }
        }

        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(connectivityReceiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(connectivityReceiver)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
