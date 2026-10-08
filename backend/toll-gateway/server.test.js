'use strict';
const test=require('node:test');
const assert=require('node:assert/strict');
const http=require('node:http');
const {createHmac}=require('node:crypto');
const {constantTimeTokenMatch,createGatewayHandler}=require('./server');
const token='client-token-0123456789abcdef0123456789abcdef';
const secret='gateway-secret-0123456789abcdef0123456789abcdef';

test('constant-time token helper rejects wrong values',()=>{
  assert.equal(constantTimeTokenMatch(token,token),true);
  assert.equal(constantTimeTokenMatch('wrong',token),false);
});

test('gateway authenticates client and signs exact body for verifier',async()=>{
  const body=JSON.stringify({geometry:[[31,-97],[32,-98]]});
  let calls=0;
  const handler=createGatewayHandler({
    clientToken:token,gatewaySecret:secret,verifierUrl:'http://127.0.0.1:8080/v1/tolls/verify',
    fetchImpl:async(url,init)=>{
      calls++;
      const h=init.headers;
      const expected=createHmac('sha256',secret)
        .update(['POST','/v1/tolls/verify',h['x-roadguard-subject'],h['x-roadguard-timestamp'],h['x-roadguard-nonce'],body].join('\n')).digest('hex');
      assert.equal(h['x-roadguard-signature'],expected);
      assert.equal(init.body,body);
      return {status:200,text:async()=>JSON.stringify({status:'verified_zero'})};
    }
  });
  const server=http.createServer(handler);
  await new Promise(r=>server.listen(0,'127.0.0.1',r));
  try{
    const url=`http://127.0.0.1:${server.address().port}/v1/tolls/verify`;
    assert.equal((await fetch(url,{method:'POST',body})).status,401);
    const ok=await fetch(url,{method:'POST',headers:{authorization:`Bearer ${token}`},body});
    assert.equal(ok.status,200);
    assert.equal((await ok.json()).status,'verified_zero');
    assert.equal(calls,1);
  }finally{server.close();}
});

test('gateway fails closed for unsafe verifier target',async()=>{
  const handler=createGatewayHandler({clientToken:token,gatewaySecret:secret,verifierUrl:'https://evil.example/v1/tolls/verify'});
  const server=http.createServer(handler);
  await new Promise(r=>server.listen(0,'127.0.0.1',r));
  try{
    const response=await fetch(`http://127.0.0.1:${server.address().port}/v1/tolls/verify`,{method:'POST',headers:{authorization:`Bearer ${token}`},body:'{}'});
    assert.equal(response.status,503);
  }finally{server.close();}
});

test('accepts lowercase bearer and preserves split UTF-8 bytes',async()=>{
  const body=Buffer.from(JSON.stringify({geometry:[[31,-97],[32,-98]],note:'é'}));
  let calls=0;
  const handler=createGatewayHandler({clientToken:token,gatewaySecret:secret,verifierUrl:'http://127.0.0.1:8080/v1/tolls/verify',fetchImpl:async(_url,init)=>{
    calls++;
    assert.equal(init.body,body.toString('utf8'));
    const h=init.headers;
    assert.equal(h['x-roadguard-signature'],createHmac('sha256',secret).update(['POST','/v1/tolls/verify',h['x-roadguard-subject'],h['x-roadguard-timestamp'],h['x-roadguard-nonce'],init.body].join('\n')).digest('hex'));
    return {status:200,text:async()=>JSON.stringify({status:'verified_zero'})};
  }});
  const server=http.createServer(handler);
  await new Promise(r=>server.listen(0,'127.0.0.1',r));
  try{
    const split=body.indexOf(0xc3)+1;
    const response=await new Promise((resolve,reject)=>{
      const req=http.request({hostname:'127.0.0.1',port:server.address().port,path:'/v1/tolls/verify',method:'POST',headers:{authorization:'bearer '+token,'content-type':'application/json'}},res=>{res.resume();res.on('end',()=>resolve(res.statusCode));});
      req.on('error',reject);req.write(body.subarray(0,split));req.end(body.subarray(split));
    });
    assert.equal(response,200);assert.equal(calls,1);
  }finally{server.close();}
});

test('IPv6 loopback is accepted but external and credentialed URLs are rejected',async()=>{
  for(const [url,expected] of [['http://[::1]:8080/v1/tolls/verify',200],['http://evil.example/v1/tolls/verify',503],['http://user@127.0.0.1:8080/v1/tolls/verify',503]]){
    const handler=createGatewayHandler({clientToken:token,gatewaySecret:secret,verifierUrl:url,fetchImpl:async()=>({status:200,text:async()=>JSON.stringify({status:'unknown'})})});
    const server=http.createServer(handler);
    await new Promise(r=>server.listen(0,'127.0.0.1',r));
    try{const res=await fetch('http://127.0.0.1:'+server.address().port+'/v1/tolls/verify',{method:'POST',headers:{authorization:'Bearer '+token},body:JSON.stringify({geometry:[[31,-97],[32,-98]]})});assert.equal(res.status,expected);}finally{server.close();}
  }
});
