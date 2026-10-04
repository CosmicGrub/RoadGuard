package com.cosmicgrub.roadguard.data

data class ProviderConfig(
    val geocodingBaseUrl: String,
    val routingBaseUrl: String,
    val apiKey: String? = null
) {
    init {
        require(geocodingBaseUrl.startsWith("https://"))
        require(routingBaseUrl.startsWith("https://"))
    }

    val configured: Boolean get() = !apiKey.isNullOrBlank()
}
