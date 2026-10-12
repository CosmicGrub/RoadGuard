'use strict';
const test=require('node:test');
const assert=require('node:assert/strict');
const {generateKeyPairSync,sign}=require('node:crypto');
const {createOidcValidator}=require('./oidc');
const {privateKey,publicKey}=generateKeyPairSync('rsa',{modulusLength:2048});
const jwk={...publicKey.export({format:'jwk'}),kid:'test-key',alg:'RS256',use:'sig'};
const issuer='https://id.example.test';
const audience='roadguard-mobile';
const now=1_800_000_000;
function jwt(payload,header={alg:'RS256',kid:'test-key',typ:'JWT'}){
 const a=Buffer.from(JSON.stringify(header)).toString('base64url');
 const b=Buffer.from(JSON.stringify(payload)).toString('base64url');
 return a+'.'+b+'.'+sign('RSA-SHA256',Buffer.from(a+'.'+b),privateKey).toString('base64url');
}
function claims(overrides={}){return {iss:issuer,aud:audience,sub:'driver_123',iat:now-10,exp:now+300,...overrides};}
function setup(discoveryIssuer=issuer){
 let calls=0;
 const fetchImpl=async url=>{
  calls++;
  if(url===issuer+'/.well-known/openid-configuration')return {ok:true,json:async()=>({issuer:discoveryIssuer,jwks_uri:issuer+'/keys'})};
  if(url===issuer+'/keys')return {ok:true,json:async()=>({keys:[jwk]})};
  throw Error('unexpected URL '+url);
 };
 return {validate:createOidcValidator({issuer,audience,fetchImpl,clock:()=>now*1000}),getCalls:()=>calls};
}
test('accepts correctly signed and scoped JWT and caches keys',async()=>{
 const s=setup();
 assert.equal(await s.validate(jwt(claims())),'driver_123');
 assert.equal(await s.validate(jwt(claims())),'driver_123');
 assert.equal(s.getCalls(),2);
});
test('rejects invalid issuer, audience, expiry, not-before, subject and algorithm',async()=>{
 const s=setup();
 for(const c of [claims({iss:'https://evil.test'}),claims({aud:'wrong'}),claims({exp:now}),claims({nbf:now+120}),claims({sub:'bad subject'})])
   assert.equal(await s.validate(jwt(c)),null);
 assert.equal(await s.validate(jwt(claims(),{alg:'HS256',kid:'test-key'})),null);
});
test('rejects tampering, untrusted discovery issuer, and unknown signing key',async()=>{
 const s=setup();
 const token=jwt(claims());
 assert.equal(await s.validate(token.slice(0,-2)+'aa'),null);
 assert.equal(await setup('https://evil.test').validate(token),null);
 assert.equal(await setup().validate(jwt(claims(),{alg:'RS256',kid:'missing'})),null);
});
