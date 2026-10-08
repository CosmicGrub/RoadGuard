package com.cosmicgrub.roadguard

import android.os.Bundle
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.cosmicgrub.roadguard.data.RoadGuardProviderFactory
import com.cosmicgrub.roadguard.location.LocationAccess
import com.cosmicgrub.roadguard.location.LocationPermissionGate
import com.cosmicgrub.roadguard.location.locationAccess
import com.cosmicgrub.roadguard.location.rememberDriverLocation
import com.cosmicgrub.roadguard.map.RoadGuardMap
import com.cosmicgrub.roadguard.navigation.NavigationRuntime
import com.cosmicgrub.roadguard.navigation.NavigationScreen
import com.cosmicgrub.roadguard.navigation.NavigationViewModel

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
    val context = LocalContext.current
    var locationAccess by remember { mutableStateOf(context.locationAccess()) }
    val provider = remember { RoadGuardProviderFactory.here(BuildConfig.HERE_API_KEY) }
    // The gateway credential is provisioned at runtime, never in BuildConfig or APK resources.
    val gatewayTokenProvider: suspend () -> String? = {
        context.getSharedPreferences("roadguard_gateway_auth", Context.MODE_PRIVATE)
            .getString("access_token", null)
    }
    val navViewModel: NavigationViewModel? = remember(provider) {
        provider?.let { NavigationViewModel(NavigationRuntime(it, BuildConfig.TOLL_VERIFICATION_ENDPOINT, gatewayTokenProvider).controller) }
    }
    val navState = navViewModel?.state?.collectAsState()?.value
    val driverLocation = rememberDriverLocation(locationAccess != LocationAccess.NONE)
    LaunchedEffect(driverLocation, navViewModel) {
        driverLocation?.point?.let { navViewModel?.setOrigin(it) }
    }

    val locationRequest = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants -> locationAccess = LocationPermissionGate.access(grants) }

    Scaffold(topBar = { TopAppBar(title = { Text("RoadGuard") }) }) { padding ->
        Box(Modifier.padding(padding)) {
            if (navigationMode) {
                if (navViewModel == null || navState == null) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Live navigation provider is not configured.")
                        Text("Set HERE_API_KEY in local.properties or the build environment.")
                    }
                } else NavigationScreen(
                    state = navState,
                    onQueryChange = navViewModel::queryChanged,
                    onDestination = navViewModel::chooseDestination,
                    onRouteSelected = navViewModel::selectRoute,
                    mapContent = {
                        Box(Modifier.fillMaxSize()) {
                            RoadGuardMap(
                                state = navState,
                                driverLocation = driverLocation,
                                modifier = Modifier.fillMaxSize()
                            )
                            if (locationAccess == LocationAccess.NONE) {
                                Button(
                                    onClick = { locationRequest.launch(LocationPermissionGate.foregroundPermissions) },
                                    modifier = Modifier.padding(20.dp)
                                ) { Text("Enable current location") }
                            }
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
