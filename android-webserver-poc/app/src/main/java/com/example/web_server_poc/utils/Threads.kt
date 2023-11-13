package com.example.web_server_poc.utils

import android.content.Context
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors

private val IO_EXECUTOR = Executors.newSingleThreadExecutor()
fun onIoThread(f: () -> Unit) {
    IO_EXECUTOR.execute(f)
}

fun onMainThread(context: Context, f: () -> Unit) {
    ContextCompat.getMainExecutor(context).execute(f)
}