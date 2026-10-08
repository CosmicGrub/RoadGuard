'use strict';

const http = require('node:http');
const { createHmac, randomBytes, timingSafeEqual } = require('node:crypto');

const MAX_BYTES = 100_000;

function constantTimeTokenMatch(actual, expected) {
  if (typeof actual !== 'string' || typeof expected !== 'string') return false;
  const a = Buffer.from(actual), b = Buffer.from(expected);
  return a.length === b.length && timingSafeEqual(a, b);
}

function createGatewayHandler({
  clientToken, gatewaySecret, verifierUrl, fetchImpl = fetch,
  maxPerClientPerMinute = 10
}) {
  const quotas = new Map();
  const configured = typeof clientToken === 'string' && Buffer.byteLength(clientToken) >= 32 &&
    typeof gatewaySecret === 'string' && Buffer.byteLength(gatewaySecret) >= 32 &&
    (() => { try { const u = new URL(verifierUrl); return u.protocol === 'http:' && ['127.0.0.1','[::1]'].includes(u.hostname) && !u.username && !u.password && u.pathname === '/v1/tolls/verify' && !u.search && !u.hash; } catch { return false; } })();

  return async (req, res) => {
    const reply = (code, body) => {
      res.writeHead(code, {'content-type':'application/json','cache-control':'no-store'});
      res.end(JSON.stringify(body));
    };
    if (req.method !== 'POST' || req.url !== '/v1/tolls/verify')
      return reply(404, {status:'unknown', reason:'Not found'});
    if (!configured) return reply(503, {status:'unknown', reason:'Gateway not configured'});

    const auth = req.headers.authorization;
    if (typeof auth !== 'string' || !/^Bearer [^\s]+$/i.test(auth) ||
        !constantTimeTokenMatch(auth.slice(7), clientToken))
      return reply(401, {status:'unknown', reason:'Authentication required'});

    // M6 bootstrap identity: a deployment-issued token maps to one stable subject.
    // Replace this single-client bootstrap with OIDC/JWT validation before multi-user release.
    const subject = 'bootstrap-client';
    const now = Date.now();
    for (const [k,v] of quotas) if (v.expires <= now) quotas.delete(k);
    const quota = quotas.get(subject) || {count:0, expires:now+60_000};
    quota.count++; quotas.set(subject, quota);
    if (quota.count > maxPerClientPerMinute)
      return reply(429, {status:'unknown', reason:'Client quota exceeded'});

    const chunks = [];
    let size = 0;
    try {
      for await (const chunk of req) {
        size += chunk.length;
        if (size > MAX_BYTES)
          return reply(413, {status:'unknown', reason:'Payload too large'});
        chunks.push(chunk);
      }
      const raw = Buffer.concat(chunks).toString('utf8');
      let parsed;
      try { parsed = JSON.parse(raw); } catch { return reply(400, {status:'unknown', reason:'Invalid JSON'}); }
      if (!parsed || typeof parsed !== 'object' || !Array.isArray(parsed.geometry))
        return reply(400, {status:'unknown', reason:'Invalid request'});

      const timestamp = String(Date.now());
      const nonce = randomBytes(16).toString('hex');
      const signature = createHmac('sha256', gatewaySecret)
        .update(['POST','/v1/tolls/verify',subject,timestamp,nonce,raw].join('\n')).digest('hex');
      const upstream = await fetchImpl(verifierUrl, {
        method:'POST', redirect:'error',
        headers:{'content-type':'application/json','x-roadguard-subject':subject,
          'x-roadguard-timestamp':timestamp,'x-roadguard-nonce':nonce,
          'x-roadguard-signature':signature},
        body:raw, signal:AbortSignal.timeout(15_000)
      });
      const text = await upstream.text();
      res.writeHead(upstream.status, {'content-type':'application/json','cache-control':'no-store'});
      res.end(text);
    } catch {
      return reply(502, {status:'unknown', reason:'Verifier unavailable'});
    }
  };
}

if (require.main === module) {
  const port=Number(process.env.GATEWAY_PORT||8443);
  const verifierUrl=process.env.ROADGUARD_VERIFIER_URL||'http://127.0.0.1:8080/v1/tolls/verify';
  http.createServer(createGatewayHandler({
    clientToken:process.env.ROADGUARD_CLIENT_TOKEN,
    gatewaySecret:process.env.ROADGUARD_GATEWAY_SECRET,
    verifierUrl
  })).listen(port,'127.0.0.1',()=>console.log('RoadGuard gateway listening on loopback',port));
}

module.exports={constantTimeTokenMatch,createGatewayHandler};
