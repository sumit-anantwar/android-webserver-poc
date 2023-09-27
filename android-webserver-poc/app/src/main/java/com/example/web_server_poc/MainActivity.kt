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
import com.example.web_server_poc.webserver.WebServer

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var textview_IP: TextView

    private var webServer: WebServer? = null

    private val port = 8080
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        textview_IP = binding.textviewIp

        webServer = WebServer(this, port)
        try {
            webServer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val ipAddress = getLocalIpAddress(this)
        textview_IP.text = String.format("%s:%d", ipAddress, port)
    }

    override fun onDestroy() {
        super.onDestroy()
        webServer?.stop()
    }

    fun getLocalIpAddress(context: Context): String? {
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