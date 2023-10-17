package com.example.web_server_poc.webrtc

import android.content.Context
import com.example.web_server_poc.webserver.Offer
import org.webrtc.AudioTrack


class WebRtcPresenter(
    private val context: Context,
) {

    private val localAudioTrack: AudioTrack
    private val peerConnectionFactory = WebRtcUtils.createFactory(context).apply {
        localAudioTrack = WebRtcUtils.createTrack(this)
    }

    private val peerConnections = mutableListOf<WebRtcPeerConnection>()

    fun createConnection(offer: Offer) {
        val peerConnection = WebRtcPeerConnection(
            context, offer, peerConnectionFactory, localAudioTrack,
            onAnswer = {},
            onConnected = {},
            onDisconnected = {}
        )

        peerConnections.add(peerConnection)
    }


}