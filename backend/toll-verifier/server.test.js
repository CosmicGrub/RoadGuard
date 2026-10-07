'use strict';

const test = require('node:test');
const assert = require('node:assert/strict');
const http = require('node:http');
const { encodePolyline, validGeometry, interpretVendorResponse, createHandler } = require('./server');

test('Google polyline example', () => {
  assert.equal(encodePolyline([[38.5, -120.2], [40.7, -120.95], [43.252, -126.453]]),
    '_p~iF~ps|U_ulLnnqC_mqNvxq`@');
});

test('geometry rejects invalid and oversized inputs', () => {
  assert.equal(validGeometry([[0, 0], [1, 1]]), true);
  assert.equal(validGeometry([[91, 0], [1, 1]]), false);
  assert.equal(validGeometry([[0, Infinity], [1, 1]]), false);
  assert.equal(validGeometry(Array(5001).fill([0, 0])), false);
});

test('only explicit boolean evidence can verify zero tolls', () => {
  assert.deepEqual(interpretVendorResponse({ route: { hasTolls: false } }), { status: 'verified_zero' });
  assert.equal(interpretVendorResponse({ route: { hasTolls: true } }).status, 'toll_detected');
  assert.equal(interpretVendorResponse({ route: { hasTolls: 'false' } }).status, 'unknown');
  assert.equal(interpretVendorResponse({ route: {} }).status, 'unknown');
  assert.equal(interpretVendorResponse({}).status, 'unknown');
});

test('HTTP boundary does not expose vendor key', async () => {
  let calls = 0;
  const handler = createHandler({
    apiKey: 'secret-test-key',
    vendorUrl: 'https://vendor.example/verify',
    fetchImpl: async (url, init) => {
      calls++;
      assert.equal(init.headers['x-api-key'], 'secret-test-key');
      return { ok: true, json: async () => ({ route: { hasTolls: false } }) };
    }
  });
  const server = http.createServer(handler);
  await new Promise(resolve => server.listen(0, '127.0.0.1', resolve));
  try {
    const response = await fetch(`http://127.0.0.1:${server.address().port}/v1/tolls/verify`, {
      method: 'POST', headers: { 'content-type': 'application/json' },
      body: JSON.stringify({ geometry: [[31, -97], [31.1, -97.1]] })
    });
    const text = await response.text();
    assert.equal(response.status, 200);
    assert.equal(JSON.parse(text).status, 'verified_zero');
    assert.equal(text.includes('secret-test-key'), false);
    assert.equal(calls, 1);
  } finally {
    server.close();
  }
});
