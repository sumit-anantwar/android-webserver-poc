package com.example.web_server_poc.utils

import java.util.concurrent.Executors

private val IO_EXECUTOR = Executors.newSingleThreadExecutor()
fun onIoThread(f: () -> Unit) {
    IO_EXECUTOR.execute(f)
}