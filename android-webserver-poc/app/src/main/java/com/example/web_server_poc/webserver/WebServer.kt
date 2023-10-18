package com.example.web_server_poc.webserver

import android.app.Activity
import android.util.Base64
import android.util.Log
import com.example.web_server_poc.utils.JsonSerializer
import com.example.web_server_poc.webrtc.WebRtcPresenter
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.Response
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.serializer
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import timber.log.Timber
import java.io.IOException
import java.io.InputStream
import kotlin.reflect.KClass

@Serializable
open class JsonMappable {
}

@Serializable
data class SDP(
    val type: String,
    val sdp: String,
) : JsonMappable()

@Serializable
data class Candidate(
    val candidate: String,
    val sdpMid: String,
    val sdpMLineIndex: Int,
    val usernameFragment: String,
) : JsonMappable()

@Serializable
data class SdpPacket(
    val sdp: SDP,
    val icecandidates: List<Candidate>
) : JsonMappable() {
    fun sdp(): SessionDescription {
        return SessionDescription(
            SessionDescription.Type.OFFER,
            sdp.sdp
        )
    }

    fun candidates(): List<IceCandidate> {
        return icecandidates.map {
            IceCandidate(
                it.sdpMid,
                it.sdpMLineIndex,
                it.candidate
            )
        }
    }
}

@Serializable
data class Answer(
    val answer: SDP,
    val candidates: List<String>
) : JsonMappable()

@Suppress("UNCHECKED_CAST")
@OptIn(InternalSerializationApi::class)
inline fun <reified T : JsonMappable> T.toJsonString(): String {
    val serializer = (this::class as KClass<T>).serializer()
    return JsonSerializer.encodeToString(serializer, this)
}


@OptIn(InternalSerializationApi::class)
class WebServer(
    private val activity: Activity,
    private val webRtcPresenter: WebRtcPresenter,
    port: Int
) : NanoHTTPD(port) {
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
                else -> fixedLengthResponseWithCors(
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
            return chunkedResponseWithCors(
                Response.Status.OK, mimeType, inputStream
            ).also(Response::addCors)
        } catch (e: IOException) {
            e.printStackTrace()
            return fixedLengthResponseWithCors(
                Response.Status.NOT_FOUND, MIME_PLAINTEXT, "404 Not Found"
            ).also(Response::addCors)
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
        val offer = JsonSerializer.decodeFromString<SdpPacket>(body)
        Timber.d(offer.toString())

        runBlocking {
            launch {
                val packet = webRtcPresenter.createConnection(offer)

            }
        }



        val answerSDP = SDP(
            type = "answer",
            sdp = offer.sdp().description
        )

//        val answer = Answer(
//            answer = answerSDP,
//            candidates = offer.candidates
//        )

        val responseJson = answerSDP.toJsonString()

        return fixedLengthResponseWithCors(
            Response.Status.OK, "application/json", responseJson
        )
    }

    private fun fixedLengthResponseWithCors(status: Response.IStatus, mimeType: String, txt: String): Response {
        return newFixedLengthResponse(status, mimeType, txt)
            .also(Response::addCors)
    }

    private fun chunkedResponseWithCors(status: Response.IStatus, mimeType: String, data: InputStream): Response {
        return newChunkedResponse(status, mimeType, data)
            .also(Response::addCors)
    }
}

fun Response.addCors() {
    this.addHeader("Access-Control-Allow-Methods", "DELETE, GET, POST, PUT");
    this.addHeader("Access-Control-Allow-Origin",  "*");
    this.addHeader("Access-Control-Allow-Headers", "X-Requested-With");
}