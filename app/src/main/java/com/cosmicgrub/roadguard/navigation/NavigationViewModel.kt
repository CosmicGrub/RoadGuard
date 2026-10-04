package com.cosmicgrub.roadguard.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cosmicgrub.roadguard.data.GeoPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NavigationViewModel(private val controller: NavigationController) : ViewModel() {
    private val mutableState = MutableStateFlow(NavigationUiState())
    val state: StateFlow<NavigationUiState> = mutableState.asStateFlow()
    private var searchJob: Job? = null

    fun setOrigin(origin: GeoPoint) {
        mutableState.value = mutableState.value.copy(origin = origin)
    }

    fun queryChanged(query: String) {
        mutableState.value = mutableState.value.copy(destinationQuery = query, error = null)
        searchJob?.cancel()
        if (query.trim().length < 2) {
            mutableState.value = mutableState.value.copy(suggestions = emptyList(), isSearching = false)
            return
        }
        searchJob = viewModelScope.launch {
            delay(250)
            mutableState.value = mutableState.value.copy(isSearching = true)
            runCatching { controller.search(query, mutableState.value.origin) }
                .onSuccess { mutableState.value = mutableState.value.copy(suggestions = it, isSearching = false) }
                .onFailure { mutableState.value = mutableState.value.copy(isSearching = false, error = "Destination search failed") }
        }
    }

    fun chooseDestination(place: PlaceSuggestion) {
        mutableState.value = mutableState.value.copy(
            destination = place,
            destinationQuery = place.name,
            suggestions = emptyList(),
            error = null
        )
        val origin = mutableState.value.origin ?: run {
            mutableState.value = mutableState.value.copy(error = "Current location is required before routing")
            return
        }
        viewModelScope.launch {
            mutableState.value = mutableState.value.copy(isRouting = true)
            runCatching { controller.route(origin, place.point) }
                .onSuccess { routes ->
                    val selected = RouteSelection.select(routes, null)
                    mutableState.value = mutableState.value.copy(
                        routes = routes,
                        selectedRouteId = selected?.candidate?.id,
                        isRouting = false,
                        error = if (routes.isEmpty()) "No verified route satisfies your Route Firewall" else null
                    )
                }
                .onFailure { mutableState.value = mutableState.value.copy(isRouting = false, error = "Route request failed") }
        }
    }

    fun selectRoute(id: String) {
        if (mutableState.value.routes.any { it.candidate.id == id }) {
            mutableState.value = mutableState.value.copy(selectedRouteId = id)
        }
    }
}
