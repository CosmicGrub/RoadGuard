package com.cosmicgrub.roadguard.data

import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

data class RoadGuardTollVerifierConfig(
    val endpoint: String,
    val accessTokenProvider: suspend () -> String? = { null }
) {
    init {
        require(endpoint.startsWith("https://")) { "Toll verification endpoint must use HTTPS" }
    }
}

class RoadGuardBackendTollEvidence(
    private val config: RoadGuardTollVerifierConfig,
    private val http: RoadGuardBackendTollClient = RoadGuardBackendTollClient()
) : IndependentTollEvidence {
    override suspend fun verify(geometry: List<GeoPoint>): TollVerification {
        if (geometry.size < 2) return TollVerification.Unknown("Route geometry is incomplete")
        return parse(http.verify(config.endpoint, geometry, config.accessTokenProvider()))
    }

    internal fun parse(json: String): TollVerification {
        val root = JSONObject(json)
        return when (root.optString("status")) {
            "verified_zero" -> TollVerification.VerifiedZero
            "toll_detected" -> TollVerification.TollDetected(
                root.optString("reason", "Independent toll evidence detected tolls")
            )
            else -> TollVerification.Unknown(
                root.optString("reason", "Independent toll evidence is unavailable")
            )
        }
    }
}

open class RoadGuardBackendTollClient {
    open suspend fun verify(endpoint: String, geometry: List<GeoPoint>, accessToken: String? = null): String =
        withContext(Dispatchers.IO) {
            val connection = URL(endpoint).openConnection() as HttpURLConnection
            try {
                connection.requestMethod = "POST"
                connection.connectTimeout = 7_500
                connection.readTimeout = 12_500
                connection.doOutput = true
                connection.setRequestProperty("Accept", "application/json")
                connection.setRequestProperty("Content-Type", "application/json")
                if (!accessToken.isNullOrBlank()) {
                    require(!accessToken.contains("\\r") && !accessToken.contains("\\n")) { "Invalid authentication token" }
                    connection.setRequestProperty("Authorization", "Bearer $accessToken")
                }

                val points = JSONArray()
                geometry.forEach { point ->
                    points.put(JSONArray().put(point.latitude).put(point.longitude))
                }
                val payload = JSONObject().put("geometry", points).toString()
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
