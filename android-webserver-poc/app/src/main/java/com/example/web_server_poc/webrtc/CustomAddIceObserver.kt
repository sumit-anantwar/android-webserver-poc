package com.example.web_server_poc.webrtc

import timber.log.Timber

open class CustomAddIceObserver  {
    fun onAddSuccess() {
        Timber.d("Ice Candidate added Successfully")
    }

    fun onAddFailure(error: String) {
        Timber.e("Failed to add Ice Candidate: $error")
    }
}