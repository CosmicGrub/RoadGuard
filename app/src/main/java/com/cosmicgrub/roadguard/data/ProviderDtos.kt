package com.cosmicgrub.roadguard.data

data class ProviderPlace(
    val id: String,
    val name: String,
    val label: String,
    val latitude: Double,
    val longitude: Double
)

data class ProviderRoute(
    val id: String,
    val distanceMeters: Double,
    val durationSeconds: Double,
    val geometry: List<GeoPoint>,
    val providerReportsToll: Boolean
)
