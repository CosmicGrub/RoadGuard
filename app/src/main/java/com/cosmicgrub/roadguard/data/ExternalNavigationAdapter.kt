package com.cosmicgrub.roadguard.data

import com.cosmicgrub.roadguard.domain.RouteCandidate
import com.cosmicgrub.roadguard.domain.RouteFirewall
import com.cosmicgrub.roadguard.domain.RoutePolicy
import com.cosmicgrub.roadguard.navigation.PlaceSuggestion
import com.cosmicgrub.roadguard.navigation.RouteOption

interface ExternalNavigationService {
    suspend fun search(query: String, near: GeoPoint?): List<ProviderPlace>
    suspend fun routes(origin: GeoPoint, destination: GeoPoint, avoidTolls: Boolean): List<ProviderRoute>
}

class ExternalNavigationAdapter(private val service: ExternalNavigationService) :
    PlaceSearchProvider, NavigationRouteProvider {

    override suspend fun search(query: String, near: GeoPoint?): List<PlaceSuggestion> =
        service.search(query, near).map {
            PlaceSuggestion(it.id, it.name, it.label, GeoPoint(it.latitude, it.longitude))
        }

    override suspend fun routes(origin: GeoPoint, destination: GeoPoint, policy: RoutePolicy): List<RouteOption> =
        service.routes(origin, destination, avoidTolls = !policy.tollsAllowed).map { route ->
            val candidate = RouteCandidate(
                id = route.id,
                distanceMeters = route.distanceMeters,
                durationSeconds = route.durationSeconds,
                tollSegments = if (route.providerReportsToll) 1 else 0,
                hasClosure = false,
                constructionDelaySeconds = 0,
                trafficDelaySeconds = 0,
                weatherRisk = 0.0
            )
            RouteOption(
                candidate = candidate,
                geometry = route.geometry,
                firewall = RouteFirewall.inspect(candidate, policy),
                zeroTollVerified = false
            )
        }
}
