package com.example.web_server_poc.webrtc

import android.content.Context
import com.example.web_server_poc.webserver.Candidate
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
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpReceiver
import org.webrtc.SessionDescription
import timber.log.Timber
import java.lang.Exception
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

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

    private var localPeer: PeerConnection? = null

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
            }
        )

        Observables.combineLatest(answerPublisher, candidatePublisher)
            .debounce(3, TimeUnit.SECONDS)
            .subscribeBy(
                onError = Timber::e,
                onNext = {
                    Timber.d("Response Packet Complete: $it")
                }
            ).addTo(disposables)

        runBlocking {
            launch {
                localPeer?.setRemoteDescription(CustomSdpObserver(), offer.sdp())
                val candidates = offer.candidates()
                Timber.d("Number of candidates: ${candidates.count()}")
                for (candidate in candidates) {
                    localPeer?.addIceCandidate(candidate, CustomAddIceObserver())
                    delay(1000)
                }
            }
        }

//        localPeer?.createAnswer()
        Timber.d("Method complete")
    }

    // region === PRIVATE ===

    private fun addStreamToLocalPeer(peerConnection: PeerConnection) {
        val stream = peerConnectionFactory.createLocalMediaStream("stream")
        stream.addTrack(audioTrack)
        peerConnection.addTrack(stream.audioTracks[0])
    }

    // endregion

}