package com.cosmicgrub.roadguard.data

import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SimpleHttpClient {
    suspend fun get(url: String): String = withContext(Dispatchers.IO) {
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 10_000
            connection.readTimeout = 15_000
            connection.setRequestProperty("Accept", "application/json")
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

class ProviderHttpException(val statusCode: Int, message: String) :
    RuntimeException("Provider HTTP $statusCode: $message")
