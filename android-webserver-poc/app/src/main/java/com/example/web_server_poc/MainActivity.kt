package com.example.web_server_poc

import android.content.Context
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.widget.TextView
import com.example.web_server_poc.databinding.ActivityMainBinding
import com.example.web_server_poc.utils.PermissionUtils
import com.example.web_server_poc.webrtc.WebRtcPresenter
import com.example.web_server_poc.webserver.WebServer
import com.tbruyelle.rxpermissions3.RxPermissions
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MainActivity : AppCompatActivity() {

    private val disposables = CompositeDisposable()

    private lateinit var binding: ActivityMainBinding
    private lateinit var textview_IP: TextView

    private var webServer: WebServer? = null

    private val port = 8080
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        textview_IP = binding.textviewIp
    }

    override fun onResume() {
        super.onResume()

        checkPermissions()
    }

    override fun onDestroy() {
        super.onDestroy()
        webServer?.stop()
        disposables.dispose()
    }

    private fun launchServer() {
        val presenter = WebRtcPresenter(this)

        webServer = WebServer(this, presenter, port)
        try {
            webServer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val ipAddress = getLocalIpAddress(this)
        textview_IP.text = String.format("%s:%d", ipAddress, port)
    }

    private fun checkPermissions() {
        if (PermissionUtils.hasPermissionsGranted(this)) {
            launchServer()
        } else {
            PermissionUtils.requestPermission(this)
                .subscribeBy(
                    onError = Timber::e,
                    onSuccess = {
                        Timber.d("Permissions Granted: $it")
                    }
                ).addTo(disposables)
        }
    }

    private fun getLocalIpAddress(context: Context): String? {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo: WifiInfo = wifiManager.connectionInfo
        val ipAddress = wifiInfo.ipAddress

        // Convert the IP address from an integer to a human-readable format (e.g., "192.168.1.100")
        val ipString = String.format(
            "%d.%d.%d.%d",
            ipAddress and 0xff,
            ipAddress shr 8 and 0xff,
            ipAddress shr 16 and 0xff,
            ipAddress shr 24 and 0xff
        )

        return ipString
    }
}