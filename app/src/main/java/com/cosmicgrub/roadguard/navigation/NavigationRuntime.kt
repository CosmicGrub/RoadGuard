package com.cosmicgrub.roadguard.navigation

import com.cosmicgrub.roadguard.data.ExternalNavigationAdapter
import com.cosmicgrub.roadguard.data.IndependentTollRouteProvider
import com.cosmicgrub.roadguard.data.NavigationRouteProvider
import com.cosmicgrub.roadguard.data.RoadGuardBackendTollEvidence
import com.cosmicgrub.roadguard.data.RoadGuardTollVerifierConfig
import com.cosmicgrub.roadguard.domain.RoutePolicy

class NavigationRuntime(
    provider: ExternalNavigationAdapter,
    tollVerificationEndpoint: String? = null,
    tollAccessTokenProvider: suspend () -> String? = { null },
    policy: RoutePolicy = RoutePolicy()
) {
    private val routing: NavigationRouteProvider =
        if (tollVerificationEndpoint.isNullOrBlank()) provider
        else IndependentTollRouteProvider(
            provider,
            RoadGuardBackendTollEvidence(RoadGuardTollVerifierConfig(tollVerificationEndpoint, tollAccessTokenProvider))
        )

    val controller = NavigationController(
        places = provider,
        routing = routing,
        policy = policy
    )
}
