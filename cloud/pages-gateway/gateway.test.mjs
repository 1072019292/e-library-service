import assert from 'node:assert/strict';
import test from 'node:test';
import gateway, { forwardRequest } from './public/_worker.js';

const env = { JAVA_ORIGIN: 'https://library-test.onrender.com' };

test('the Workers entrypoint does not mistake ExecutionContext for fetch', async t => {
  t.mock.method(globalThis, 'fetch', async request => {
    assert.equal(new URL(request.url).hostname, 'library-test.onrender.com');
    return new Response('runtime entry works');
  });
  const response = await gateway.fetch(new Request('https://library.pages.dev/'), env,
    { waitUntil() {} });
  assert.equal(await response.text(), 'runtime entry works');
});

test('missing or unsafe configuration never transmits authorization', async () => {
  for (const value of ['', 'http://library-test.onrender.com',
    'https://onrender.com.evil.example', 'https://user:pass@library-test.onrender.com',
    'https://library-test.onrender.com/path', 'https://library-test.onrender.com?x=1']) {
    const response = await forwardRequest(new Request('https://library.pages.dev/api/me/loans', {
      headers: { Authorization: 'Basic review-only' },
    }), { JAVA_ORIGIN: value }, () => assert.fail('must not contact an upstream'));
    assert.equal(response.status, 503);
    assert.equal((await response.json()).code, 'ORIGIN_NOT_CONFIGURED');
  }
});

test('path, query and explicit authorization reach only the configured origin', async () => {
  const response = await forwardRequest(new Request('https://library.pages.dev/api/books?q=Java&page=1', {
    headers: { Authorization: 'Basic review-only', Forwarded: 'host=evil.example',
      'X-Forwarded-Host': 'evil.example' },
  }), env, async request => {
    assert.equal(request.url, 'https://library-test.onrender.com/api/books?q=Java&page=1');
    assert.equal(request.headers.get('Authorization'), 'Basic review-only');
    assert.equal(request.headers.get('Forwarded'), null);
    assert.equal(request.headers.get('X-Forwarded-Host'), 'library.pages.dev');
    assert.equal(request.redirect, 'manual');
    return new Response('{"items":[]}', { headers: {
      'Content-Type': 'application/json', 'Content-Security-Policy': "default-src 'self'",
    } });
  });
  assert.equal(response.headers.get('Cache-Control'), 'no-store');
  assert.equal(response.headers.get('Content-Security-Policy'), "default-src 'self'");
});

test('a path resembling a network URL cannot turn the gateway into an open proxy', async () => {
  await forwardRequest(new Request('https://library.pages.dev//evil.example/api'), env, async request => {
    assert.equal(new URL(request.url).hostname, 'library-test.onrender.com');
    assert.equal(new URL(request.url).pathname, '//evil.example/api');
    return new Response('ok');
  });
});

test('writes preserve body and method and are never retried after a transport error', async () => {
  let attempts = 0;
  const response = await forwardRequest(new Request('https://library.pages.dev/api/loans', {
    method: 'POST', headers: { 'Content-Type': 'application/json' }, body: '{"bookId":2}',
  }), env, async request => {
    attempts++;
    assert.equal(request.method, 'POST');
    assert.equal(await request.text(), '{"bookId":2}');
    throw new Error('simulated uncertain network outcome');
  });
  assert.equal(attempts, 1);
  assert.equal(response.status, 503);
  assert.equal((await response.json()).code, 'UPSTREAM_UNAVAILABLE');
});

test('status and same-origin redirects stay on the public Pages entry', async () => {
  const response = await forwardRequest(new Request('https://library.pages.dev/'), env,
    async () => new Response(null, { status: 302, headers: {
      Location: 'https://library-test.onrender.com/index.html?view=catalog',
    } }));
  assert.equal(response.status, 302);
  assert.equal(response.headers.get('Location'), 'https://library.pages.dev/index.html?view=catalog');
});

test('upstream business conflicts are returned without replacement or retry', async () => {
  const response = await forwardRequest(new Request('https://library.pages.dev/api/loans'), env,
    async () => new Response('{"code":"NO_LICENSE_AVAILABLE"}', { status: 409,
      headers: { 'Content-Type': 'application/problem+json', 'Cache-Control': 'public' } }));
  assert.equal(response.status, 409);
  assert.equal((await response.json()).code, 'NO_LICENSE_AVAILABLE');
  assert.equal(response.headers.get('Cache-Control'), 'no-store');
});
