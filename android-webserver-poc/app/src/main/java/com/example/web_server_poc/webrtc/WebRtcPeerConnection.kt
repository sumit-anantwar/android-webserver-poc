package com.example.web_server_poc.webrtc

import android.content.Context
import com.example.web_server_poc.webserver.Answer
import com.example.web_server_poc.webserver.Offer
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.Observables
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.subjects.PublishSubject
import org.webrtc.AudioTrack
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpReceiver
import org.webrtc.SessionDescription
import timber.log.Timber
import java.util.concurrent.TimeUnit

class WebRtcPeerConnection(
    context: Context,
    private val offer: Offer,
    private val peerConnectionFactory: PeerConnectionFactory,
    private val audioTrack: AudioTrack,
    private val onAnswer: (Answer) -> Unit,
    private val onConnected: () -> Unit,
    private val onDisconnected: () -> Unit,
) {
    private val disposables = CompositeDisposable()

    private val candidates = mutableListOf<IceCandidate>()

    private var localPeer: PeerConnection? = null

    private val answerPublisher = PublishSubject.create<String>()
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
                        onDisconnected()
//                        listener.onConnectionLost(this@WebRtcPresenterHandler)
                    }
                }
            }
        )

        Observables.combineLatest(answerPublisher, candidatePublisher)
            .debounce(3, TimeUnit.SECONDS)
            .subscribeBy(
                onError = Timber::e,
                onNext = {
                    Timber.d(it.toString())
                }
            ).addTo(disposables)

        localPeer?.setRemoteDescription(CustomSdpObserver(), offer.sdp())
//        offer.candidates.forEach {
//            IceCandidate()
//            localPeer?.addIceCandidate()
//        }
    }

    // region === PRIVATE ===

    private fun addStreamToLocalPeer(peerConnection: PeerConnection) {
        val stream = peerConnectionFactory.createLocalMediaStream("stream")
        stream.addTrack(audioTrack)
        peerConnection.addTrack(stream.audioTracks[0])
    }

    // endregion

}