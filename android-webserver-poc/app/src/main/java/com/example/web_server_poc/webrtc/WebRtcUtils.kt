package com.example.web_server_poc.webrtc

import android.content.Context
import org.webrtc.AudioTrack
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.audio.JavaAudioDeviceModule

object WebRtcUtils {

    fun createRctConfig(): PeerConnection.RTCConfiguration {
        return PeerConnection.RTCConfiguration(listOf()).apply {
            tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.ENABLED
            bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE
            rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE
            continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
            // Use ECDSA encryption.
            keyType = PeerConnection.KeyType.ECDSA
        }
    }

    fun createFactory(context: Context): PeerConnectionFactory {
        val initializationOptions = PeerConnectionFactory.InitializationOptions.builder(context)
            .setEnableInternalTracer(true)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(initializationOptions)

        val audioDeviceModule = JavaAudioDeviceModule.builder(context)
            .setUseHardwareAcousticEchoCanceler(true)
            .setUseHardwareNoiseSuppressor(true)
            .createAudioDeviceModule()

        val options = PeerConnectionFactory.Options().apply {
            disableNetworkMonitor = true
        }
        return PeerConnectionFactory.builder()
            .setOptions(options)
            .setAudioDeviceModule(audioDeviceModule)
            .createPeerConnectionFactory()
    }

    fun createTrack(factory: PeerConnectionFactory): AudioTrack {
        return factory.createAudioTrack("audio", factory.createAudioSource(MediaConstraints()))
    }
}