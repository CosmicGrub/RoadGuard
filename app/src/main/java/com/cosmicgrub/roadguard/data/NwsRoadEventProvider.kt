package com.cosmicgrub.roadguard.data

import com.cosmicgrub.roadguard.domain.RoadEvent
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class NwsRoadEventProvider(
    private val http: SimpleHttpClient = SimpleHttpClient(),
    private val clock: () -> Long = System::currentTimeMillis,
    private val userAgent: String = "RoadGuard"
) : BoundedRoadEventProvider {
    override suspend fun events(bounds: GeoBounds): List<RoadEvent> {
        val url = "https://api.weather.gov/alerts/active?status=actual&message_type=alert,update" +
            "&point=" + enc((bounds.south + bounds.north) / 2.0) + "%2C" + enc((bounds.west + bounds.east) / 2.0)
        return runCatching {
            NwsAlertParser.events(http.get(url), "National Weather Service", clock())
        }.getOrElse { emptyList() }.filter {
            it.latitude in bounds.south..bounds.north && it.longitude in bounds.west..bounds.east
        }
    }

    private fun enc(value: Double) = URLEncoder.encode(value.toString(), StandardCharsets.UTF_8.toString())
}
