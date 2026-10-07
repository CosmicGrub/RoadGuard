# RoadGuard independent toll verifier (M4)

This service keeps the TollGuru credential on the server, never in the Android APK.

## Local development

Requires Node.js 22+. Run `node --test backend/toll-verifier/server.test.js` from the repository root.

Set `TOLLGURU_API_KEY` in the **server environment only**. Optional variables:
`PORT` (default 8080), `TOLLGURU_ENDPOINT` (defaults to TollGuru's polyline endpoint).

The process deliberately binds to **127.0.0.1**. Put it behind a TLS-terminating gateway that enforces authenticated clients, abuse protection, request quotas, and monitoring before any public deployment. Do **not** expose the process directly to the internet. The Android `TOLL_VERIFICATION_ENDPOINT` must be the gateway's HTTPS URL ending in `/v1/tolls/verify`.

## Contract

POST JSON: `{"geometry":[[latitude,longitude],[latitude,longitude]]}`.

Response: `{"status":"verified_zero"|"toll_detected"|"unknown","reason":"optional"}`.

The service fails closed for unavailable or ambiguous evidence, HTTP failures, and invalid geometry. Never infer verified zero tolls from an absent field.

**Integration blocker:** The TollGuru request and `route.hasTolls` response shape still require validation against the current official TollGuru API contract and real licensed sandbox fixtures. The implementation intentionally refuses ambiguous responses. Do not deploy or claim verified zero-toll routes until the vendor contract, route coverage, and accuracy have been independently validated.

The built-in 30-request/minute limit is a **backend-wide emergency cap** when traffic is forwarded through a loopback gateway (all requests share its source IP). It is **not** a per-user limit. Production operators must configure an appropriate capacity and enforce authenticated per-user quotas at the trusted gateway to protect chargeable upstream calls. The upstream URL must be HTTPS and redirects are refused.

## M5 authenticated gateway contract

Set a random 32+ byte `ROADGUARD_GATEWAY_SECRET` in both the trusted gateway and this service. Never ship it in the Android application. Every request must contain `x-roadguard-subject` (stable authenticated user ID, ASCII letters/digits/underscore/hyphen), `x-roadguard-timestamp` (13-digit Unix milliseconds), `x-roadguard-nonce` (32 lowercase hex characters), and `x-roadguard-signature` (lowercase hex HMAC-SHA256). Signature input is the UTF-8 concatenation of `POST`, `/v1/tolls/verify`, subject, timestamp, nonce, and the **exact unmodified JSON request body**, separated by LF characters. Requests older than 60 seconds and repeated nonces are rejected. Per-subject quotas default to 10/minute, with a separate backend-wide emergency limit of 30/minute.

**Operational limits:** Quotas and replay caches are in memory per process. Before horizontal scaling, implement a shared atomic store and centralized budget limits. The gateway must authenticate callers, sign requests, and enforce its own abuse controls. The Android client cannot directly call this authenticated internal service. No public gateway has been implemented or deployed by this change.
