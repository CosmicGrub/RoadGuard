package com.cosmicgrub.roadguard.data

import com.cosmicgrub.roadguard.domain.RoadEvent

data class WzdxFeed(val name: String, val url: String)

class WzdxRoadEventProvider(
    private val feeds: List<WzdxFeed>,
    private val http: SimpleHttpClient = SimpleHttpClient(),
    private val clock: () -> Long = System::currentTimeMillis
) : BoundedRoadEventProvider {
    override suspend fun events(bounds: GeoBounds): List<RoadEvent> {
        val now = clock()
        return feeds.flatMap { feed ->
            runCatching { WzdxParser.events(http.get(feed.url), feed.name, now) }
                .getOrElse { emptyList() }
        }.filter { event ->
            event.latitude in bounds.south..bounds.north &&
                event.longitude in bounds.west..bounds.east
        }
    }
}
