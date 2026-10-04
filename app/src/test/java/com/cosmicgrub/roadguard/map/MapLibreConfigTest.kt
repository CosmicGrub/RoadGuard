package com.cosmicgrub.roadguard.map

import org.junit.Assert.assertEquals
import org.junit.Test

class MapLibreConfigTest {
    @Test fun acceptsHttpsStyle() {
        assertEquals("https://example.com/style.json", MapLibreConfig("https://example.com/style.json").styleUri)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsInsecureStyle() { MapLibreConfig("http://example.com/style.json") }
}
