package com.cosmicgrub.roadguard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cosmicgrub.roadguard.navigation.NavigationUiState
import com.cosmicgrub.roadguard.navigation.NavigationScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { RoadGuardHome() } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoadGuardHome() {
    var zeroTolls by remember { mutableStateOf(true) }
    var weather by remember { mutableStateOf(true) }
    var construction by remember { mutableStateOf(true) }
    var navigationMode by remember { mutableStateOf(false) }

    Scaffold(topBar = { TopAppBar(title = { Text("RoadGuard") }) }) { padding ->
        Box(Modifier.padding(padding)) {
            if (navigationMode) {
                NavigationScreen(
                    state = NavigationUiState(),
                    onQueryChange = {},
                    onDestination = {},
                    onRouteSelected = {},
                    mapContent = {
                        Box(Modifier.fillMaxSize().padding(20.dp)) {
                            Text("Map surface ready — provider wiring in progress")
                        }
                    }
                )
            } else {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Route Firewall", style = MaterialTheme.typography.headlineMedium)
                    Text("Hard constraints are enforced before a route can be recommended.")
                    FirewallSwitch("Tolls blocked", zeroTolls) { zeroTolls = it }
                    FirewallSwitch("Avoid severe weather", weather) { weather = it }
                    FirewallSwitch("Avoid construction", construction) { construction = it }
                    HorizontalDivider()
                    Button(onClick = { navigationMode = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Choose destination")
                    }
                }
            }
        }
    }
}

@Composable
private fun FirewallSwitch(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
