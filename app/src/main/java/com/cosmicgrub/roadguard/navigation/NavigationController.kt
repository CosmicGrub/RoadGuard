package com.cosmicgrub.roadguard.navigation

import com.cosmicgrub.roadguard.data.GeoPoint
import com.cosmicgrub.roadguard.data.NavigationRouteProvider
import com.cosmicgrub.roadguard.data.PlaceSearchProvider
import com.cosmicgrub.roadguard.domain.FirewallStatus
import com.cosmicgrub.roadguard.domain.RoutePolicy

class NavigationController(
    private val places: PlaceSearchProvider,
    private val routing: NavigationRouteProvider,
    private val policy: RoutePolicy = RoutePolicy()
) {
    suspend fun search(query: String, near: GeoPoint?): List<PlaceSuggestion> {
        if (query.trim().length < 2) return emptyList()
        return places.search(query.trim(), near)
    }

    suspend fun route(origin: GeoPoint, destination: GeoPoint): List<RouteOption> =
        routing.routes(origin, destination, policy)
            .filter { option ->
                option.firewall.none { it.status == FirewallStatus.BLOCKED } &&
                    (policy.tollsAllowed || option.zeroTollVerified)
            }
            .sortedBy { it.candidate.durationSeconds }
}
