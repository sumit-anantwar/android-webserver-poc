package com.example.web_server_poc.webrtc

import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import timber.log.Timber

open class CustomSdpObserver : SdpObserver {
    override fun onCreateSuccess(sdp: SessionDescription) {
        Timber.d("SDP Created Successfully")
    }

    override fun onSetSuccess() {
        Timber.d("SDP Set Successfuly")
    }

    override fun onCreateFailure(error: String) {
        Timber.e("SDP Creation Error: $error")
    }

    override fun onSetFailure(error: String) {
        Timber.e("SDP Set Error: $error")
    }
}