package com.cosmicgrub.roadguard.data

import org.junit.Assert.*
import org.junit.Test

class ProviderConfigTest {
    @Test fun missingKeyKeepsProviderUnconfigured() {
        val c = ProviderConfig("https://geo.example/", "https://route.example/")
        assertFalse(c.configured)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsInsecureEndpoint() {
        ProviderConfig("http://geo.example/", "https://route.example/")
    }
}
