package com.cosmicgrub.roadguard.data

object HerePolicy {
    private val tollViolationCodes = setOf("avoidTollRoad", "violatedAvoidTollRoad")

    fun providerReportsToll(noticeCodes: Collection<String>): Boolean =
        noticeCodes.any { code ->
            code in tollViolationCodes ||
                (code.contains("toll", ignoreCase = true) && code.contains("avoid", ignoreCase = true))
        }
}
