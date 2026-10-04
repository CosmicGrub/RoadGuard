# Data-source policy

RoadGuard separates map rendering, routing and road intelligence so no single vendor becomes the source of truth.

## Location
Android foreground location is sufficient for the initial interactive navigation experience. The app must remain functional with approximate access where practical and request precise access only in context when navigation requires it. Background navigation will be added with an explicit location foreground service rather than silently requesting permanent background access.

## Basemap
Map rendering is behind a provider boundary. MapLibre is a leading candidate because it keeps rendering open and decoupled from routing/data vendors. A production tile/style source still needs to be selected.

## Routing
Candidate routes may come from one or more routing engines. Hard constraints are revalidated by RoadGuard rather than trusted solely from a provider response.

## Weather
Official NWS alerts will form the first U.S. severe-weather authority layer. Forecast/radar providers can augment this for predictive corridor analysis.

## Construction
WZDx and state DOT feeds will be normalized into RoadEvent records. Coverage varies by jurisdiction, so source and freshness are retained.

## Police presence
Community reports are time-decaying observations. RoadGuard must not claim an officer is present solely because of a report.

## ALPR/cameras
Only publicly documented or community-observed camera information is represented. Every record carries provenance, confidence and freshness; the product must not imply the layer contains every camera.
