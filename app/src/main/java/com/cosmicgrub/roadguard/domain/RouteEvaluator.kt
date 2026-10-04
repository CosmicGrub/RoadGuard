package com.cosmicgrub.roadguard.domain

object RouteEvaluator {
    fun score(route: RouteCandidate, policy: RoutePolicy): Double {
        if (!policy.tollsAllowed && route.tollSegments > 0) return Double.POSITIVE_INFINITY
        if (policy.avoidClosures && route.hasClosure) return Double.POSITIVE_INFINITY

        var score = route.durationSeconds
        score += route.trafficDelaySeconds * policy.trafficWeight
        if (policy.avoidConstruction) score += route.constructionDelaySeconds * policy.constructionWeight
        if (policy.avoidSevereWeather) score += route.weatherRisk.coerceIn(0.0, 1.0) * policy.weatherWeight * 1000.0
        return score
    }

    fun best(routes: List<RouteCandidate>, policy: RoutePolicy): RouteCandidate? =
        routes.minByOrNull { score(it, policy) }?.takeIf { score(it, policy).isFinite() }
}
