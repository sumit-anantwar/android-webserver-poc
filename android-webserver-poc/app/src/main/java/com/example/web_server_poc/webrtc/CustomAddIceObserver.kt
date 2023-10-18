package com.example.web_server_poc.webrtc

import org.webrtc.AddIceObserver
import timber.log.Timber

open class CustomAddIceObserver : AddIceObserver {
    override fun onAddSuccess() {
        Timber.d("Ice Candidate added Successfully")
    }

    override fun onAddFailure(error: String) {
        Timber.e("Failed to add Ice Candidate: $error")
    }
}