package com.cosmicgrub.roadguard.map

data class MapLibreConfig(val styleUri: String, val attributionRequired: Boolean = true) {
    init { require(styleUri.startsWith("https://")) { "Map style must use HTTPS" } }
}
