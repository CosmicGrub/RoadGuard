package com.cosmicgrub.roadguard.navigation

import com.cosmicgrub.roadguard.data.ExternalNavigationAdapter
import com.cosmicgrub.roadguard.domain.RoutePolicy

class NavigationRuntime(
    provider: ExternalNavigationAdapter,
    policy: RoutePolicy = RoutePolicy()
) {
    val controller = NavigationController(
        places = provider,
        routing = provider,
        policy = policy
    )
}
