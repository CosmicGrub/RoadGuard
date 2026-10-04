package com.cosmicgrub.roadguard.navigation

import com.cosmicgrub.roadguard.data.GeoPoint
import com.cosmicgrub.roadguard.domain.FirewallCheck
import com.cosmicgrub.roadguard.domain.RouteCandidate

data class PlaceSuggestion(
    val id: String,
    val name: String,
    val subtitle: String,
    val point: GeoPoint
)

data class RouteOption(
    val candidate: RouteCandidate,
    val geometry: List<GeoPoint>,
    val firewall: List<FirewallCheck>,
    val zeroTollVerified: Boolean
)

data class NavigationUiState(
    val destinationQuery: String = "",
    val suggestions: List<PlaceSuggestion> = emptyList(),
    val origin: GeoPoint? = null,
    val destination: PlaceSuggestion? = null,
    val routes: List<RouteOption> = emptyList(),
    val selectedRouteId: String? = null,
    val isSearching: Boolean = false,
    val isRouting: Boolean = false,
    val error: String? = null
)
