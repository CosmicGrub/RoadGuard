# M1 Navigation

## Acceptance target
A driver can grant foreground location, see their position, search for a destination, receive alternative route candidates, and select a route only after RoadGuard's policy layer accepts it.

When tolls are blocked, a provider's zero-toll claim is insufficient by itself. The route remains ineligible until the independent validation layer returns verified zero toll.

## Provider boundary
Map rendering, place search, route generation, live traffic and toll verification remain separate adapters. The intended first renderer is MapLibre-compatible, but no domain or navigation policy code imports a map SDK.

## Next integration
1. MapLibre Android renderer.
2. Android fused/location-manager source.
3. production geocoding adapter.
4. production candidate-routing adapter.
5. route polyline display and camera fitting.
6. reroute trigger and maneuver guidance.
