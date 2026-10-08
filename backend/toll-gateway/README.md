# RoadGuard toll gateway (M6)

This is the external boundary in front of the loopback-only toll verifier. It authenticates a bootstrap client token, applies a client quota, and signs the exact request body using the M5 HMAC protocol before forwarding to the local verifier.

## Security boundary

Set separate 32+ byte `ROADGUARD_CLIENT_TOKEN` and `ROADGUARD_GATEWAY_SECRET` secrets. The gateway secret is shared only with the internal verifier. The client token is a **bootstrap mechanism**, not a production multi-user identity system. Do not embed a long-lived shared client token in a public Android release.

The gateway only forwards to loopback HTTP verifier targets and refuses remote targets. Deploy TLS in a trusted reverse proxy/load balancer in front of this process.

## Remaining release blocker

Before multi-user/public release, replace bootstrap bearer-token authentication with a real identity provider (OIDC/JWT with issuer, audience, expiry and key validation), and move quotas to a shared atomic store. TollGuru's licensed live API contract and evidence semantics also remain to be validated independently.
