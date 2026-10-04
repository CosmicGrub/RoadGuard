package com.cosmicgrub.roadguard.data

data class GeocodingRequest(val query: String, val near: GeoPoint?)
data class RoutingRequest(val origin: GeoPoint, val destination: GeoPoint, val avoidTolls: Boolean)

interface ProviderHttpClient {
    suspend fun geocode(request: GeocodingRequest): String
    suspend fun route(request: RoutingRequest): String
}
