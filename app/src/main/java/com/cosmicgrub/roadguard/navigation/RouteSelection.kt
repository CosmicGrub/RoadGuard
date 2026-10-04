package com.cosmicgrub.roadguard.navigation

object RouteSelection {
    fun select(routes: List<RouteOption>, requestedId: String?): RouteOption? {
        if (routes.isEmpty()) return null
        return routes.firstOrNull { it.candidate.id == requestedId }
            ?: routes.minByOrNull { it.candidate.durationSeconds }
    }
}
