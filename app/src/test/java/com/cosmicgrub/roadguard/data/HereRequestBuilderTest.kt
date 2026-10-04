package com.cosmicgrub.roadguard.data

import org.junit.Assert.*
import org.junit.Test

class HereRequestBuilderTest {
    private val config = ProviderConfig("https://discover.search.hereapi.com","https://router.hereapi.com","secret")

    @Test fun discoverIncludesLocationContextAndEncodedQuery() {
        val url = HereRequestBuilder.discover(config, "Fort Hood TX", GeoPoint(31.1, -97.7))
        assertTrue(url.contains("q=Fort+Hood+TX"))
        assertTrue(url.contains("at=31.1,-97.7"))
    }

    @Test fun tollBlockedRouteExplicitlyRequestsTollAvoidance() {
        val url = HereRequestBuilder.route(config, GeoPoint(31.0,-97.0), GeoPoint(32.0,-96.0), true)
        assertTrue(url.contains("avoid%5Bfeatures%5D=tollRoad"))
        assertTrue(url.contains("return=polyline,summary,notices"))
    }

    @Test fun tollAllowedRouteDoesNotRequestTollAvoidance() {
        val url = HereRequestBuilder.route(config, GeoPoint(31.0,-97.0), GeoPoint(32.0,-96.0), false)
        assertFalse(url.contains("tollRoad"))
    }
}
