'use strict';

const http = require('node:http');

const MAX_BYTES = 100_000;
const MAX_POINTS = 5_000;
const DEFAULT_TIMEOUT_MS = 12_000;

function encodePolyline(points) {
  let lastLat = 0, lastLon = 0, encoded = '';
  const encode = (delta) => {
    let value = delta < 0 ? ~(delta << 1) : delta << 1;
    while (value >= 0x20) {
      encoded += String.fromCharCode((0x20 | (value & 0x1f)) + 63);
      value >>>= 5;
    }
    encoded += String.fromCharCode(value + 63);
  };
  for (const [lat, lon] of points) {
    const latitude = Math.round(lat * 1e5);
    const longitude = Math.round(lon * 1e5);
    encode(latitude - lastLat);
    encode(longitude - lastLon);
    lastLat = latitude;
    lastLon = longitude;
  }
  return encoded;
}

function validGeometry(points) {
  return Array.isArray(points) && points.length >= 2 && points.length <= MAX_POINTS &&
    points.every(p => Array.isArray(p) && p.length === 2 &&
      p.every(Number.isFinite) && Math.abs(p[0]) <= 90 && Math.abs(p[1]) <= 180);
}

function interpretVendorResponse(payload) {
  if (!payload || typeof payload !== 'object') return { status: 'unknown', reason: 'Missing evidence' };
  // Do not treat missing, malformed, or ambiguous fields as zero-toll proof.
  const route = payload.route ?? payload.summary?.route;
  if (!route || typeof route.hasTolls !== 'boolean')
    return { status: 'unknown', reason: 'Vendor toll coverage not established' };
  return route.hasTolls
    ? { status: 'toll_detected', reason: 'Independent provider detected tolls' }
    : { status: 'verified_zero' };
}

function createHandler({ apiKey, fetchImpl = fetch, vendorUrl, maxRequestsPerMinute = 30 }) {
  const counters = new Map();
  return async (req, res) => {
    const reply = (code, body) => {
      res.writeHead(code, { 'content-type': 'application/json', 'cache-control': 'no-store' });
      res.end(JSON.stringify(body));
    };
    if (req.method !== 'POST' || req.url !== '/v1/tolls/verify')
      return reply(404, { status: 'unknown', reason: 'Not found' });
    if (!apiKey || !vendorUrl) return reply(503, { status: 'unknown', reason: 'Verifier not configured' });

    // Per-address limiter is a defense-in-depth guard, not a substitute for
    // authentication and quota enforcement at the deployment gateway.
    const now = Date.now();
    const ip = req.socket.remoteAddress || 'unknown';
    for (const [key, bucket] of counters) if (bucket.expires <= now) counters.delete(key);
    const bucket = counters.get(ip) || { count: 0, expires: now + 60_000 };
    bucket.count++;
    counters.set(ip, bucket);
    if (bucket.count > maxRequestsPerMinute)
      return reply(429, { status: 'unknown', reason: 'Rate limit exceeded' });

    let raw = '';
    try {
      for await (const chunk of req) {
        raw += chunk;
        if (Buffer.byteLength(raw) > MAX_BYTES) return reply(413, { status: 'unknown', reason: 'Payload too large' });
      }
      const geometry = JSON.parse(raw).geometry;
      if (!validGeometry(geometry)) return reply(400, { status: 'unknown', reason: 'Invalid route geometry' });
      const response = await fetchImpl(vendorUrl, {
        method: 'POST',
        headers: { 'content-type': 'application/json', 'x-api-key': apiKey },
        body: JSON.stringify({
          mapProvider: 'here',
          polyline: encodePolyline(geometry),
          vehicle: { type: '2AxlesAuto' }
        }),
        signal: AbortSignal.timeout(DEFAULT_TIMEOUT_MS)
      });
      if (!response.ok) return reply(502, { status: 'unknown', reason: 'Independent provider unavailable' });
      const payload = await response.json();
      return reply(200, interpretVendorResponse(payload));
    } catch {
      return reply(502, { status: 'unknown', reason: 'Independent verification failed' });
    }
  };
}

if (require.main === module) {
  const key = process.env.TOLLGURU_API_KEY;
  const vendorUrl = process.env.TOLLGURU_ENDPOINT ||
    'https://apis.tollguru.com/toll/v2/complete-polyline-from-mapping-service';
  const port = Number(process.env.PORT || 8080);
  const server = http.createServer(createHandler({ apiKey: key, vendorUrl }));
  server.listen(port, '127.0.0.1', () => console.log('RoadGuard verifier listening on loopback', port));
}

module.exports = { encodePolyline, validGeometry, interpretVendorResponse, createHandler };
