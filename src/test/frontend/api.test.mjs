import test from 'node:test';
import assert from 'node:assert/strict';
import { ApiError, errorMessage, makeAuthorization, request } from '../../main/resources/static/assets/api.mjs';

test('authorization encodes ASCII and Unicode credentials as UTF-8', () => {
  assert.equal(makeAuthorization('alice', 'alice-demo-pass'), 'Basic ' + Buffer.from('alice:alice-demo-pass').toString('base64'));
  assert.equal(makeAuthorization('读者', '密码'), 'Basic ' + Buffer.from('读者:密码').toString('base64'));
});

test('public reads omit ambient credentials and do not send authorization', async t => {
  let options;
  t.mock.method(globalThis, 'fetch', async (path, input) => {
    assert.equal(path, '/api/books'); options = input;
    return { ok: true, json: async () => ({ items: [] }) };
  });
  assert.deepEqual(await request('/api/books'), { items: [] });
  assert.equal(options.credentials, 'omit');
  assert.equal(options.cache, 'no-store');
  assert.equal(options.headers.Authorization, undefined);
});

test('borrowing sends explicit authorization and JSON', async t => {
  let options;
  t.mock.method(globalThis, 'fetch', async (path, input) => {
    options = input; return { ok: true, json: async () => ({ id: 7 }) };
  });
  await request('/api/loans', { method: 'POST', authorization: 'Basic demo', body: { bookId: 2 } });
  assert.equal(options.headers.Authorization, 'Basic demo');
  assert.equal(options.headers['Content-Type'], 'application/json');
  assert.equal(options.body, '{"bookId":2}');
  assert.equal(options.credentials, 'omit');
});

test('business conflicts are confirmed failures with meaningful feedback', async t => {
  t.mock.method(globalThis, 'fetch', async () => ({ ok: false, status: 409, json: async () => ({ code: 'ALREADY_BORROWED' }) }));
  await assert.rejects(request('/api/loans', { method: 'POST', body: { bookId: 2 } }), error => {
    assert.ok(error instanceof ApiError); assert.equal(error.code, 'ALREADY_BORROWED');
    assert.equal(error.ambiguous, false); return true;
  });
});

test('network failure after a write remains ambiguous and never triggers automatic resend', async t => {
  let calls = 0;
  t.mock.method(globalThis, 'fetch', async () => { calls++; throw new TypeError('network'); });
  await assert.rejects(request('/api/loans', { method: 'POST', body: { bookId: 1 } }), error => {
    assert.equal(error.ambiguous, true); assert.equal(error.code, 'CONNECTION_ERROR'); return true;
  });
  assert.equal(calls, 1);
});

test('network failure of a read is not an ambiguous mutation', async t => {
  t.mock.method(globalThis, 'fetch', async () => { throw new TypeError('network'); });
  await assert.rejects(request('/api/books'), error => { assert.equal(error.ambiguous, false); return true; });
});

test('unreadable successful write response still requires reconciliation', async t => {
  t.mock.method(globalThis, 'fetch', async () => ({ ok: true, status: 201, json: async () => { throw new SyntaxError('invalid JSON'); } }));
  await assert.rejects(request('/api/loans', { method: 'POST', body: { bookId: 1 } }), error => {
    assert.equal(error.code, 'INVALID_RESPONSE'); assert.equal(error.ambiguous, true); return true;
  });
});

test('unrecognised error codes cannot inherit object prototype values', () => {
  for (const code of ['__proto__', 'constructor', 'toString']) {
    assert.equal(typeof errorMessage(code), 'string');
    assert.equal(errorMessage(code), '操作暂时未完成，请刷新后核对。');
  }
});

test('removing a saved book accepts a confirmed empty 204 response', async t => {
  t.mock.method(globalThis, 'fetch', async () => ({ ok: true, status: 204, json: async () => { throw new Error('must not parse'); } }));
  assert.equal(await request('/api/me/favorites/2', { method: 'DELETE', authorization: 'Basic demo' }), null);
});
