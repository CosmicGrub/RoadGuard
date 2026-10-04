# RoadGuard architecture

## Product invariant
A hard route constraint is never silently downgraded to a preference. If RoadGuard cannot verify a compliant route, it reports that no verified route is available.

## Layers
1. Android client: navigation UX, Route Firewall, GPS and cached trip state.
2. Route orchestration: candidate generation, policy enforcement and independent validation.
3. Event normalization: traffic, weather, construction, closures and provenance-aware reports become RoadEvent records.
4. Provider adapters: routing, NWS weather, WZDx/state DOT construction and public camera registries.
5. Persistence: PostGIS for geospatial state and Redis for expiring live events.

## Trust model
Community reports are observations with confidence and expiry, never guaranteed facts. Public ALPR/camera data carries source and verification timestamps and must never be represented as exhaustive.
