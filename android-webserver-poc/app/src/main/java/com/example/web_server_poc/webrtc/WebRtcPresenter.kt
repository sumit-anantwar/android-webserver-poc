package com.example.web_server_poc.webrtc

import android.content.Context
import com.example.web_server_poc.webserver.SdpPacket
import org.webrtc.AudioTrack
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class WebRtcPresenter(
    private val context: Context,
) {

    private val localAudioTrack: AudioTrack
    private val peerConnectionFactory = WebRtcUtils.createFactory(context).apply {
        localAudioTrack = WebRtcUtils.createTrack(this)
    }

    private val peerConnections = mutableListOf<WebRtcPeerConnection>()

    private val connectionStatusCallback = object : WebRtcPeerConnection.StatusCallback {
        override fun onDisconnected(connection: WebRtcPeerConnection) {
            Timber.d("Peer Disconnected")
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
        }

        return answer
    }


}