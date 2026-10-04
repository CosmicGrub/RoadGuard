package com.cosmicgrub.roadguard.map

import com.cosmicgrub.roadguard.data.GeoPoint
import com.cosmicgrub.roadguard.navigation.RouteOption

data class RouteLine(val id: String, val points: List<GeoPoint>, val selected: Boolean)

object RouteGeometry {
    fun lines(routes: List<RouteOption>, selectedId: String?): List<RouteLine> =
        routes.filter { it.geometry.size >= 2 }.map {
            RouteLine(it.candidate.id, it.geometry, it.candidate.id == selectedId)
        }
}
