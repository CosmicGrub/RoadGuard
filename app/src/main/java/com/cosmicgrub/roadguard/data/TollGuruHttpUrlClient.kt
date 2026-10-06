package com.cosmicgrub.roadguard.data

import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class TollGuruHttpUrlClient : TollGuruHttpClient {
    override suspend fun verifyPolyline(url: String, apiKey: String, polyline: String): String =
        withContext(Dispatchers.IO) {
            val connection = URL(url).openConnection() as HttpURLConnection
            try {
                connection.requestMethod = "POST"
                connection.connectTimeout = 10_000
                connection.readTimeout = 20_000
                connection.doOutput = true
                connection.setRequestProperty("Accept", "application/json")
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("x-api-key", apiKey)

                val payload = JSONObject()
                    .put("mapProvider", "here")
                    .put("polyline", polyline)
                    .put("vehicle", JSONObject().put("type", "2AxlesAuto"))
                    .toString()
                connection.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }

                val code = connection.responseCode
                val stream = if (code in 200..299) connection.inputStream else connection.errorStream
                val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
                if (code !in 200..299) throw ProviderHttpException(code, body.take(512))
                body
            } finally {
                connection.disconnect()
            }
        }
}
