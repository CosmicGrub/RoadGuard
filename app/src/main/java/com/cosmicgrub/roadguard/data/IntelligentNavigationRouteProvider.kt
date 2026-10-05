package com.cosmicgrub.roadguard.data

import com.cosmicgrub.roadguard.domain.RouteFirewall
import com.cosmicgrub.roadguard.domain.RouteIntelligence
import com.cosmicgrub.roadguard.domain.RoutePolicy
import com.cosmicgrub.roadguard.navigation.RouteOption

class IntelligentNavigationRouteProvider(
    private val routing: NavigationRouteProvider,
    private val intelligence: List<BoundedRoadEventProvider>,
    private val clock: () -> Long = System::currentTimeMillis
) : NavigationRouteProvider {
    override suspend fun routes(origin: GeoPoint, destination: GeoPoint, policy: RoutePolicy): List<RouteOption> =
        routing.routes(origin, destination, policy).map { option ->
            val bounds = GeoBounds.around(option.geometry)
            if (bounds == null || intelligence.isEmpty()) return@map option
            val events = intelligence.flatMap { provider ->
                runCatching { provider.events(bounds) }.getOrElse { emptyList() }
            }.distinctBy { it.source + ":" + it.id }
            val summary = RouteIntelligence.summarize(option.geometry, events, clock())
            val candidate = RouteIntelligence.apply(option.candidate, summary)
            option.copy(candidate = candidate, firewall = RouteFirewall.inspect(candidate, policy))
        }
}
