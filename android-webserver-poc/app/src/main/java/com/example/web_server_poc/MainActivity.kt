package com.example.web_server_poc

import android.content.Context
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.widget.ImageView
import android.widget.TextView
import com.example.web_server_poc.databinding.ActivityMainBinding
import com.example.web_server_poc.utils.PermissionUtils
import com.example.web_server_poc.utils.onIoThread
import com.example.web_server_poc.utils.onMainThread
import com.example.web_server_poc.webrtc.WebRtcPresenter
import com.example.web_server_poc.webserver.WebServer
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
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
    private lateinit var imageview_QRCode: ImageView
    private lateinit var textview_ListenerCount: TextView

    private var webServer: WebServer? = null

    private val port = 8080
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        textview_IP = binding.textviewIp
        textview_ListenerCount = binding.textviewListenerCount
        imageview_QRCode = binding.imageviewQrcode
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

    private val webRtcPresenterCallback =  object : WebRtcPresenter.Callback {
        override fun onListenerCountChanged(count: Int) {
            onMainThread(this@MainActivity) {
                textview_ListenerCount.setText(count.toString())
            }
        }
    }

    private fun launchServer() {
        val presenter = WebRtcPresenter(this, webRtcPresenterCallback)

        webServer = WebServer(this, presenter, port)
        try {
            webServer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val ipAddress = getLocalIpAddress(this)
        textview_IP.text = String.format("%s:%d", ipAddress, port)
        val streamUrl = "http://$ipAddress:$port"
        try {
            val writer = MultiFormatWriter()
            val bitMatrix = writer.encode(streamUrl, BarcodeFormat.QR_CODE, 800, 800)
            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.createBitmap(bitMatrix)

            imageview_QRCode.setImageBitmap(bitmap)
        } catch (e: Exception) {
            Timber.e(e)
        }
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