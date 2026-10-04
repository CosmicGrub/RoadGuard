package com.cosmicgrub.roadguard.data

class HereExternalNavigationService(
    private val config: ProviderConfig,
    private val http: SimpleHttpClient = SimpleHttpClient()
) : ExternalNavigationService {
    override suspend fun search(query: String, near: GeoPoint?): List<ProviderPlace> {
        val anchor = requireNotNull(near) { "HERE Discover requires location context" }
        return HereJsonParser.places(http.get(HereRequestBuilder.discover(config, query, anchor)))
    }

    override suspend fun routes(
        origin: GeoPoint,
        destination: GeoPoint,
        avoidTolls: Boolean
    ): List<ProviderRoute> =
        HereJsonParser.routeSections(
            http.get(HereRequestBuilder.route(config, origin, destination, avoidTolls))
        ).map {
            ProviderRoute(
                id = it.id,
                distanceMeters = it.distanceMeters,
                durationSeconds = it.durationSeconds,
                geometry = it.geometry,
                providerReportsToll = HerePolicy.providerReportsToll(it.noticeCodes)
            )
        }
}
