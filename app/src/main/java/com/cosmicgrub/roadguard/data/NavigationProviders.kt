package com.cosmicgrub.roadguard.data

import com.cosmicgrub.roadguard.navigation.PlaceSuggestion
import com.cosmicgrub.roadguard.navigation.RouteOption
import com.cosmicgrub.roadguard.domain.RoutePolicy

interface PlaceSearchProvider {
    suspend fun search(query: String, near: GeoPoint?): List<PlaceSuggestion>
}

interface NavigationRouteProvider {
    suspend fun routes(origin: GeoPoint, destination: GeoPoint, policy: RoutePolicy): List<RouteOption>
}

interface MapSurface {
    fun showUser(point: GeoPoint)
    fun showRoutes(routes: List<RouteOption>, selectedRouteId: String?)
    fun fitRoute(route: RouteOption)
}
