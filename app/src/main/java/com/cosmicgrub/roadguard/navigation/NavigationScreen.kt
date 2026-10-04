package com.cosmicgrub.roadguard.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NavigationScreen(
    state: NavigationUiState,
    onQueryChange: (String) -> Unit,
    onDestination: (PlaceSuggestion) -> Unit,
    onRouteSelected: (String) -> Unit,
    mapContent: @Composable () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        Box(Modifier.weight(1f).fillMaxWidth()) { mapContent() }

        OutlinedTextField(
            value = state.destinationQuery,
            onValueChange = onQueryChange,
            label = { Text("Where to?") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        )

        if (state.isSearching || state.isRouting) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }

        state.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 12.dp))
        }

        if (state.suggestions.isNotEmpty()) {
            LazyColumn(Modifier.heightIn(max = 220.dp)) {
                items(state.suggestions, key = { it.id }) { place ->
                    ListItem(
                        headlineContent = { Text(place.name) },
                        supportingContent = { Text(place.subtitle) },
                        modifier = Modifier.clickable { onDestination(place) }
                    )
                }
            }
        }

        if (state.routes.isNotEmpty()) {
            LazyColumn(Modifier.heightIn(max = 260.dp)) {
                items(state.routes, key = { it.candidate.id }) { option ->
                    val card = RoutePresentation.card(option)
                    val selected = state.selectedRouteId == card.id
                    ListItem(
                        headlineContent = { Text("${card.durationMinutes} min • ${"%.1f".format(card.distanceMiles)} mi") },
                        supportingContent = {
                            Text(if (card.verifiedZeroToll) "✓ $0 tolls verified • ${card.warnings} warning(s)"
                            else "Toll verification incomplete")
                        },
                        trailingContent = { if (selected) Text("SELECTED") },
                        modifier = Modifier.clickable { onRouteSelected(card.id) }
                    )
                }
            }
        }
    }
}
