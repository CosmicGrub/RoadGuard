package com.cosmicgrub.roadguard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { RoadGuardHome() } }
    }
}

@Composable
private fun RoadGuardHome() {
    var zeroTolls by remember { mutableStateOf(true) }
    var weather by remember { mutableStateOf(true) }
    var construction by remember { mutableStateOf(true) }

    Scaffold(topBar = { TopAppBar(title = { Text("RoadGuard") }) }) { padding ->
        Column(Modifier.padding(padding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Route Firewall", style = MaterialTheme.typography.headlineMedium)
            Text("Hard constraints are enforced before a route can be recommended.")
            FirewallSwitch("Tolls blocked", zeroTolls) { zeroTolls = it }
            FirewallSwitch("Avoid severe weather", weather) { weather = it }
            FirewallSwitch("Avoid construction", construction) { construction = it }
            HorizontalDivider()
            Text("Live map and routing provider adapters are the next integration layer.")
            Button(onClick = { }, modifier = Modifier.fillMaxWidth()) { Text("Choose destination") }
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
