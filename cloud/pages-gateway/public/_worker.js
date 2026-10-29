function problem(status, code, detail) {
  return new Response(JSON.stringify({
    type: `urn:elibrary:gateway:${code.toLowerCase().replaceAll('_', '-')}`,
    title: 'Library service temporarily unavailable',
    status,
    detail,
    code,
  }), {
    status,
    headers: {
      'Content-Type': 'application/problem+json; charset=utf-8',
      'Cache-Control': 'no-store',
      'Retry-After': '30',
      'X-Content-Type-Options': 'nosniff',
    },
  });
}

function configuredOrigin(value) {
  try {
    const origin = new URL(value);
    if (origin.protocol !== 'https:' || !origin.hostname.endsWith('.onrender.com')
        || origin.username || origin.password || origin.port
        || origin.pathname !== '/' || origin.search || origin.hash) return null;
    return origin;
  } catch {
    return null;
  }
}

// The only upstream is operator configuration. A request never selects a
// destination. Authorization goes only to the configured library service.
export async function forwardRequest(request, env, send = fetch) {
  const origin = configuredOrigin(env?.JAVA_ORIGIN);
  if (!origin) return problem(503, 'ORIGIN_NOT_CONFIGURED',
    'The review service has not been connected yet.');

  const visitor = new URL(request.url);
  // Assigning pathname, rather than resolving it as a relative URL, prevents
  // a path beginning with // from replacing the fixed upstream hostname.
  const target = new URL(origin);
  target.pathname = visitor.pathname;
  target.search = visitor.search;
  const headers = new Headers(request.headers);
  for (const name of ['host', 'forwarded', 'x-forwarded-host',
    'x-forwarded-proto', 'x-forwarded-port']) headers.delete(name);
  headers.set('X-Forwarded-Proto', 'https');
  headers.set('X-Forwarded-Host', visitor.host);

  let response;
  try {
    response = await send(new Request(target, {
      method: request.method,
      headers,
      body: ['GET', 'HEAD'].includes(request.method) ? undefined : request.body,
      redirect: 'manual',
      duplex: 'half',
    }));
  } catch {
    // Never automatically retry a borrow/return after an uncertain outcome.
    return problem(503, 'UPSTREAM_UNAVAILABLE',
      'The review service is starting or temporarily unreachable. Please try again shortly.');
  }

  if (visitor.pathname.startsWith('/api/')
      && (response.headers.get('Content-Type') || '').includes('text/html')) {
    return problem(503, 'SERVICE_WARMING',
      'The free review service is waking up. Please wait a few minutes and reload.');
  }

  const resultHeaders = new Headers(response.headers);
  resultHeaders.set('Cache-Control', 'no-store');
  resultHeaders.set('X-Library-Gateway', 'pages');
  const location = resultHeaders.get('Location');
  if (location) {
    try {
      const destination = new URL(location, target);
      if (destination.origin === origin.origin) {
        destination.protocol = visitor.protocol;
        destination.host = visitor.host;
        resultHeaders.set('Location', destination.toString());
      }
    } catch { /* Preserve a malformed upstream header without following it. */ }
  }
  return new Response(response.body, {
    status: response.status,
    statusText: response.statusText,
    headers: resultHeaders,
  });
}

const contentPolicy = "default-src 'self'; script-src 'self'; style-src 'self'; "
  + "img-src 'self' data: https://covers.openlibrary.org; font-src 'self'; "
  + "connect-src 'self'; base-uri 'self'; form-action 'self'; "
  + "frame-ancestors 'none'; object-src 'none'";

export default {
  async fetch(request, env) {
    const { pathname } = new URL(request.url);
    if (pathname === '/api' || pathname.startsWith('/api/')
        || pathname === '/actuator' || pathname.startsWith('/actuator/')) {
      // ExecutionContext is not the injectable test transport.
      return forwardRequest(request, env);
    }
    // Readers can load the interface while the free Java instance wakes.
    // Catalog and account state always come from the Java API.
    const response = await env.ASSETS.fetch(request);
    const result = new Response(response.body, response);
    result.headers.set('Content-Security-Policy', contentPolicy);
    result.headers.set('X-Content-Type-Options', 'nosniff');
    result.headers.set('X-Frame-Options', 'DENY');
    result.headers.set('Referrer-Policy', 'strict-origin-when-cross-origin');
    result.headers.set('Cache-Control', pathname.startsWith('/assets/')
      ? 'public, max-age=300' : 'no-store');
    result.headers.set('X-Library-Gateway', 'pages-assets');
    return result;
  },
};
