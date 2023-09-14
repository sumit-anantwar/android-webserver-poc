package com.example.web_server_poc.webserver

import android.app.Activity
import android.util.Base64
import android.util.Log
import com.example.web_server_poc.utils.JsonSerializer
import fi.iki.elonen.NanoHTTPD
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.serializer
import java.io.IOException
import java.io.InputStream
import kotlin.reflect.KClass

@Serializable
open class JsonMappable {
}
@Serializable
data class Offer(
    val offer: String,
    val candidates: List<String>
) : JsonMappable()

@Suppress("UNCHECKED_CAST")
@OptIn(InternalSerializationApi::class)
inline fun <reified T : JsonMappable> T.toJsonString(): String {
    val serializer = (this::class as KClass<T>).serializer()
    return JsonSerializer.encodeToString(serializer, this)
}


@OptIn(InternalSerializationApi::class)
class WebServer(private val activity: Activity, port: Int) : NanoHTTPD(port) {
    override fun serve(session: IHTTPSession): Response {
        val method = session.method
        var uri = session.uri

        Log.d("TAG", "Calling $method to $uri")

        if (method == Method.POST) {
            val request = mutableMapOf<String, String>()
            session.parseBody(request)

            val requestBody = request["postData"]!!
            val cleanedBody= requestBody.replace("\n", "")
            return when(uri) {
                "/offer" -> handleOffer(cleanedBody)
                else -> newFixedLengthResponse(
                    Response.Status.NOT_FOUND, MIME_PLAINTEXT, "404 Not Found"
                )
            }
        }

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
            uri.endsWith(".svg") -> "image/svg+xml"
            uri.endsWith(".jpg") || uri.endsWith(".jpeg") -> "image/jpeg"
            else -> MIME_PLAINTEXT
        }
    }

    private fun handleOffer(body: String): Response {
        val offer = JsonSerializer.decodeFromString<Offer>(body)

        val responseJson = offer.toJsonString()

        return newFixedLengthResponse(
            Response.Status.OK, "application/json", responseJson
        )
    }
}