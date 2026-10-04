package com.cosmicgrub.roadguard.data

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object HereRequestBuilder {
    private fun enc(value: String) = URLEncoder.encode(value, StandardCharsets.UTF_8.toString())

    fun discover(config: ProviderConfig, query: String, near: GeoPoint): String =
        config.geocodingBaseUrl.trimEnd('/') + "/v1/discover?q=" + enc(query) +
            "&at=" + near.latitude + "," + near.longitude +
            "&limit=8&apiKey=" + enc(requireNotNull(config.apiKey))

    fun route(config: ProviderConfig, origin: GeoPoint, destination: GeoPoint, avoidTolls: Boolean): String {
        val avoid = if (avoidTolls) "&avoid%5Bfeatures%5D=tollRoad" else ""
        return config.routingBaseUrl.trimEnd('/') + "/v8/routes" +
            "?transportMode=car&origin=" + origin.latitude + "," + origin.longitude +
            "&destination=" + destination.latitude + "," + destination.longitude +
            "&alternatives=3&return=polyline,summary,notices" + avoid +
            "&apiKey=" + enc(requireNotNull(config.apiKey))
    }
}
