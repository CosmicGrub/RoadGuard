# RoadGuard roadmap

## M0 — Foundation
- Android Compose application shell
- RoutePolicy and RouteCandidate domain model
- RoadEvent normalization model
- hard toll and closure constraints
- independent TollValidator boundary
- CI unit test + debug APK build

## M1 — Navigable map
- production map SDK adapter
- destination search
- device location permission flow
- candidate route rendering
- turn-by-turn navigation
- route lifecycle and rerouting

## M2 — Live intelligence
- NWS alert adapter and route/weather intersection
- WZDx and state DOT construction ingestion
- traffic and closure adapters
- provenance, confidence and event expiration
- independent toll facility validation

## M3 — Community intelligence
- driver hazard reports
- confirmation/decay reputation model
- recent police-presence observations
- abuse controls and moderation
- public/documented ALPR registry with source and freshness

## M4 — Competitive layer
- Route Firewall verification UI
- predictive weather corridor
- trip cost and fuel model
- offline resilience
- Android Auto
- fleet/API surface
