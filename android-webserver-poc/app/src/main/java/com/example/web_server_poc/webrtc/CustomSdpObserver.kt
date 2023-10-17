package com.example.web_server_poc.webrtc

import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import timber.log.Timber

open class CustomSdpObserver : SdpObserver {
    override fun onCreateSuccess(p0: SessionDescription) {
        Timber.d("SDP Created Successfully")
    }

    override fun onSetSuccess() {
        Timber.d("SDP Set Successfuly")
    }

    override fun onCreateFailure(p0: String?) {
        Timber.e("SDP Creation Failure")
    }

    override fun onSetFailure(p0: String?) {
        Timber.e("SDP Set Failure")
    }
}