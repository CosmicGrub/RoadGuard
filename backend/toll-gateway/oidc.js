'use strict';
const { createPublicKey, verify } = require('node:crypto');

function b64json(part) {
  return JSON.parse(Buffer.from(part, 'base64url').toString('utf8'));
}

function createOidcValidator({issuer,audience,fetchImpl=fetch,clock=()=>Date.now()}) {
  if (!/^https:\/\//.test(issuer) || !audience) throw new Error('OIDC issuer and audience required');
  const base=issuer.replace(/\/$/,'');
  let cache=null;
  return async function validate(token) {
    if (typeof token !== 'string' || token.length > 8192) return null;
    const parts=token.split('.');
    if(parts.length!==3) return null;
    let header,payload;
    try {header=b64json(parts[0]);payload=b64json(parts[1]);}catch{return null;}
    if(header.alg!=='RS256' || typeof header.kid!=='string' || !header.kid || header.kid.length>256) return null;
    const now=Math.floor(clock()/1000);
    if(payload.iss!==base || !(payload.aud===audience || (Array.isArray(payload.aud)&&payload.aud.includes(audience))) ||
       !Number.isInteger(payload.exp) || payload.exp<=now ||
       !Number.isInteger(payload.iat) || payload.iat>now+60 ||
       typeof payload.sub!=='string' || !/^[A-Za-z0-9_-]{1,128}$/.test(payload.sub)) return null;
    if(Array.isArray(payload.aud)&&payload.aud.length>1&&payload.azp!==audience) return null;
    if(!cache || cache.expires<=clock() || !cache.keys.some(k=>k.kid===header.kid)){
      const configUrl=base+'/.well-known/openid-configuration';
      const conf=await fetchImpl(configUrl,{redirect:'error',signal:AbortSignal.timeout(5000)});
      if(!conf.ok) return null;
      const discovery=await conf.json();
      const jwksUrl=new URL(discovery.jwks_uri);
      if(jwksUrl.protocol!=='https:' || jwksUrl.origin!==new URL(base).origin) return null;
      const response=await fetchImpl(jwksUrl.href,{redirect:'error',signal:AbortSignal.timeout(5000)});
      if(!response.ok) return null;
      const keys=(await response.json()).keys;
      if(!Array.isArray(keys)) return null;
      cache={keys,expires:clock()+300000};
    }
    const jwk=cache.keys.find(k=>k.kid===header.kid&&k.kty==='RSA'&&k.alg==='RS256'&&k.use==='sig');
    if(!jwk) return null;
    try {
      const key=createPublicKey({key:jwk,format:'jwk'});
      if(!verify('RSA-SHA256',Buffer.from(parts[0]+'.'+parts[1]),key,Buffer.from(parts[2],'base64url'))) return null;
    }catch{return null;}
    return payload.sub;
  };
}
module.exports={createOidcValidator};
