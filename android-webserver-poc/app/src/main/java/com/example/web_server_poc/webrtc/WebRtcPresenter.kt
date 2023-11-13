package com.example.web_server_poc.webrtc

import android.content.Context
import com.example.web_server_poc.webserver.SdpPacket
import org.webrtc.AudioTrack
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class WebRtcPresenter(
    private val context: Context,
    private val callback: Callback
) {

    interface Callback {
        fun onListenerCountChanged(count: Int)
    }

    private val peerConnectionFactory = WebRtcUtils.createFactory(context)
    private val localAudioTrack = WebRtcUtils.createTrack(peerConnectionFactory)

    private val peerConnections = mutableListOf<WebRtcPeerConnection>()

    private val connectionStatusCallback = object : WebRtcPeerConnection.StatusCallback {
        override fun onDisconnected(connection: WebRtcPeerConnection) {
            Timber.d("Peer Disconnected")
            peerConnections.remove(connection)
            callback.onListenerCountChanged(peerConnections.count())
        }
    }

    suspend fun createConnection(offer: SdpPacket): SdpPacket {
        val answer = suspendCoroutine { continuation ->
            val peerConnection = WebRtcPeerConnection(
                context, offer, peerConnectionFactory, localAudioTrack,
                statusCallback = connectionStatusCallback,
                onAnswer = {
                    continuation.resume(it)
                },
            )

            peerConnections.add(peerConnection)
            callback.onListenerCountChanged(peerConnections.count())
        }

        return answer
    }


}