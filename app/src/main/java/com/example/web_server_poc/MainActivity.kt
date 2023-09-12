package com.example.web_server_poc

import android.content.Context
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import com.example.web_server_poc.databinding.ActivityMainBinding
import com.example.web_server_poc.webserver.WebServer

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var webView: WebView

    private var webServer: WebServer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        webView = binding.webView

        webServer = WebServer(this)
        try {
            webServer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        webView.loadUrl("http://localhost:8080/index.html")

        val ipAddress = getLocalIpAddress(this)
        Log.d("TAG", ipAddress ?: "IP ADDRESS NOT FOUND")
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