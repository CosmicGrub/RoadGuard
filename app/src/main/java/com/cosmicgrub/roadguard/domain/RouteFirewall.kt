package com.cosmicgrub.roadguard.domain

enum class FirewallStatus { VERIFIED, BLOCKED, WARNING, NOT_CHECKED }
data class FirewallCheck(val label: String, val status: FirewallStatus, val detail: String)

object RouteFirewall {
    fun inspect(route: RouteCandidate, policy: RoutePolicy): List<FirewallCheck> = buildList {
        add(when {
            policy.tollsAllowed -> FirewallCheck("Tolls", FirewallStatus.NOT_CHECKED, "Allowed by driver")
            route.tollSegments == 0 -> FirewallCheck("Tolls", FirewallStatus.WARNING, "Candidate reports zero; independent verification required")
            else -> FirewallCheck("Tolls", FirewallStatus.BLOCKED, route.tollSegments.toString() + " toll segment(s)")
        })
        add(if (route.hasClosure && policy.avoidClosures)
            FirewallCheck("Closures", FirewallStatus.BLOCKED, "Active closure intersects route")
        else FirewallCheck("Closures", FirewallStatus.VERIFIED, "No blocking closure in candidate"))
        add(if (policy.avoidSevereWeather && route.weatherRisk >= 0.7)
            FirewallCheck("Weather", FirewallStatus.WARNING, "Elevated weather exposure")
        else FirewallCheck("Weather", FirewallStatus.VERIFIED, "Within configured risk threshold"))
    }
}
