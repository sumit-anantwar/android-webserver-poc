package com.example.web_server_poc.webserver

import android.app.Activity
import fi.iki.elonen.NanoHTTPD
import java.io.IOException
import java.io.InputStream


class WebServer(private val activity: Activity) : NanoHTTPD(8080) {
    override fun serve(session: IHTTPSession): Response {
        var uri: String = session.uri
        try {
            // Remove leading slash to match the file path
            if (uri.startsWith("/")) {
                uri = uri.substring(1)
            }

            if (uri.isEmpty()) {
                uri = "index.html"
            }

            // Load file from assets and serve it
            val inputStream: InputStream = activity.assets.open(uri)
            val mimeType: String = getMimeTypeForF(uri)
            return newChunkedResponse(Response.Status.OK, mimeType, inputStream)
        } catch (e: IOException) {
            e.printStackTrace()
            return newFixedLengthResponse(
                Response.Status.NOT_FOUND, MIME_PLAINTEXT, "404 Not Found"
            )
        }
    }

    private fun getMimeTypeForF(uri: String): String {
        return when {
            uri.endsWith(".html") -> "text/html"
            uri.endsWith(".css") -> "text/css"
            uri.endsWith(".js") -> "application/javascript"
            uri.endsWith(".png") -> "image/png"
            uri.endsWith(".jpg") || uri.endsWith(".jpeg") -> "image/jpeg"
            else -> MIME_PLAINTEXT
        }
    }
}