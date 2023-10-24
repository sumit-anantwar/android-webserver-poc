package com.example.web_server_poc.webrtc

import android.content.Context
import com.example.web_server_poc.utils.onIoThread
import com.example.web_server_poc.webserver.Candidate
import com.example.web_server_poc.webserver.SDP
import com.example.web_server_poc.webserver.SdpPacket
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.Observables
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.webrtc.AudioTrack
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpReceiver
import org.webrtc.SessionDescription
import timber.log.Timber
import java.util.concurrent.TimeUnit

class WebRtcPeerConnection(
    context: Context,
    private val offer: SdpPacket,
    private val peerConnectionFactory: PeerConnectionFactory,
    private val audioTrack: AudioTrack,
    private val statusCallback: StatusCallback,
    private val onAnswer: (SdpPacket) -> Unit,
) {
    interface StatusCallback {
        fun onDisconnected(connection: WebRtcPeerConnection)
    }

    private val disposables = CompositeDisposable()

    private val candidates = mutableListOf<IceCandidate>()

    private val localPeer: PeerConnection
    private var broadcastingDataChannel: DataChannel? = null

    private val answerPublisher = PublishSubject.create<SessionDescription>()
    private val candidatePublisher = PublishSubject.create<List<IceCandidate>>()

    init {
        localPeer = peerConnectionFactory.createPeerConnection(
            WebRtcUtils.createRctConfig(),
            object : CustomPeerConnectionObserver() {
                override fun onIceCandidate(iceCandidate: IceCandidate) {
                    super.onIceCandidate(iceCandidate)

                    candidates.add(iceCandidate)
                    candidatePublisher.onNext(candidates)
                }

                override fun onAddTrack(
                    rtpReceiver: RtpReceiver,
                    mediaStreams: Array<MediaStream>,
                ) {
                    super.onAddTrack(rtpReceiver, mediaStreams)
                    mediaStreams[0].audioTracks[0].setEnabled(false)
                }

                override fun onIceConnectionChange(iceConnectionState: PeerConnection.IceConnectionState) {
                    super.onIceConnectionChange(iceConnectionState)
                    if (iceConnectionState == PeerConnection.IceConnectionState.FAILED) {
                        statusCallback.onDisconnected(this@WebRtcPeerConnection)
                    }
                }

                override fun onDataChannel(dataChannel: DataChannel) {
                    super.onDataChannel(dataChannel)
                    broadcastingDataChannel = dataChannel
                }
            }
        ) ?: throw IllegalStateException("Can't create peerconnection")

        // data channel for messaging
        setupDataChannel(localPeer)
        addStreamToLocalPeer(localPeer)

        val answerObservable = answerPublisher
            .map { it.toSdp() }

        val candidateObservable = candidatePublisher
            .map {
                it.map { it.toCandidate() }
            }

        Observables.combineLatest(answerObservable, candidateObservable)
            .debounce(3, TimeUnit.SECONDS)
            .firstElement()
            .map {
                SdpPacket(
                    sdp = it.first, icecandidates = it.second
                )
            }
            .subscribeBy(
                onError = Timber::e,
                onSuccess = {
                    onAnswer(it)
                }
            ).addTo(disposables)

        runBlocking {
            launch {
                localPeer.setRemoteDescription(CustomSdpObserver(), offer.sdp())
                val candidates = offer.candidates()
                Timber.d("Number of candidates: ${candidates.count()}")
                for (candidate in candidates) {
                    localPeer.addIceCandidate(candidate)
                    delay(300)
                }
            }
        }

        createAnswer(localPeer)
        Timber.d("Method complete")
    }

    // region === PRIVATE ===

    private fun setupDataChannel(peerConnection: PeerConnection) {
        val init = DataChannel.Init().apply { id = 0 }
        val dataChannel = peerConnection.createDataChannel("dataChannel", init)
        dataChannel.registerObserver(object : DataChannel.Observer {
            override fun onBufferedAmountChange(l: Long) {}
            override fun onStateChange() {
            }

            override fun onMessage(buffer: DataChannel.Buffer) {
                Timber.d("Daya Channel Message Received")
            }
        })
    }

    private fun createAnswer(peerConnection: PeerConnection) {
            val sdpConstraints = MediaConstraints()
            sdpConstraints.mandatory.add(
                MediaConstraints.KeyValuePair(
                    "OfferToReceiveAudio", "true"
                )
            )
            peerConnection.createAnswer(object : CustomSdpObserver() {
                override fun onCreateSuccess(sdp: SessionDescription) {
                    super.onCreateSuccess(sdp)
                    peerConnection.setLocalDescription(object : CustomSdpObserver() {
                        override fun onSetSuccess() {
                            super.onSetSuccess()
                            answerPublisher.onNext(sdp)
                        }
                    }, sdp)
                }
            }, sdpConstraints)
    }

    private fun addStreamToLocalPeer(peerConnection: PeerConnection) {
        val stream = peerConnectionFactory.createLocalMediaStream("stream")
        stream.addTrack(audioTrack)
        peerConnection.addTrack(stream.audioTracks[0])
    }

    // endregion

}

fun SessionDescription.toSdp(): SDP {
    return SDP(
        type = this.type.canonicalForm(),
        sdp = this.description
    )
}

fun IceCandidate.toCandidate(): Candidate {
    return Candidate(
        candidate = this.sdp,
        sdpMid = this.sdpMid,
        sdpMLineIndex = this.sdpMLineIndex,
    )
}