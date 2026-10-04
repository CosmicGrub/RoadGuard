package com.cosmicgrub.roadguard.data

object RoadGuardProviderFactory {
    fun here(apiKey: String?): ExternalNavigationAdapter? {
        if (apiKey.isNullOrBlank()) return null
        val config = ProviderConfig(
            geocodingBaseUrl = "https://discover.search.hereapi.com",
            routingBaseUrl = "https://router.hereapi.com",
            apiKey = apiKey
        )
        return ExternalNavigationAdapter(HereExternalNavigationService(config))
    }
}
