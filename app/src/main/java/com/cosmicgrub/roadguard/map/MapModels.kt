package com.cosmicgrub.roadguard.map

import com.cosmicgrub.roadguard.data.GeoPoint
import com.cosmicgrub.roadguard.navigation.RouteOption

data class MapViewport(
    val center: GeoPoint = GeoPoint(39.5, -98.35),
    val zoom: Double = 3.5,
    val bearing: Double = 0.0,
    val pitch: Double = 0.0
)

data class MapUiModel(
    val viewport: MapViewport = MapViewport(),
    val userLocation: GeoPoint? = null,
    val routes: List<RouteOption> = emptyList(),
    val selectedRouteId: String? = null
)

interface MapRenderer {
    fun render(model: MapUiModel)
}
