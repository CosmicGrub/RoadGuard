package com.cosmicgrub.roadguard.navigation

import com.cosmicgrub.roadguard.data.ExternalNavigationAdapter
import com.cosmicgrub.roadguard.data.IndependentTollRouteProvider
import com.cosmicgrub.roadguard.data.NavigationRouteProvider
import com.cosmicgrub.roadguard.data.TollGuruConfig
import com.cosmicgrub.roadguard.data.TollGuruEvidence
import com.cosmicgrub.roadguard.data.TollGuruHttpUrlClient
import com.cosmicgrub.roadguard.domain.RoutePolicy

class NavigationRuntime(
    provider: ExternalNavigationAdapter,
    tollGuruApiKey: String? = null,
    policy: RoutePolicy = RoutePolicy()
) {
    private val routing: NavigationRouteProvider =
        if (tollGuruApiKey.isNullOrBlank()) {
            provider
        } else {
            IndependentTollRouteProvider(
                provider,
                TollGuruEvidence(TollGuruConfig(tollGuruApiKey), TollGuruHttpUrlClient())
            )
        }

    val controller = NavigationController(
        places = provider,
        routing = routing,
        policy = policy
    )
}
