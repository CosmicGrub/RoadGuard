package com.cosmicgrub.roadguard.data

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class RoadGuardProviderFactoryTest {
    @Test fun missingKeyDisablesLiveProvider() {
        assertNull(RoadGuardProviderFactory.here(null))
        assertNull(RoadGuardProviderFactory.here("   "))
    }

    @Test fun keyCreatesProviderWithoutNetworkCall() {
        assertNotNull(RoadGuardProviderFactory.here("test-key"))
    }
}
