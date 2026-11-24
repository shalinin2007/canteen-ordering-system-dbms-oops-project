module.exports = async function handler(req, res) {
  const backendUrl = process.env.BACKEND_URL;
  if (!backendUrl) {
    res.status(503).json({ error: 'The canteen backend is not configured.' });
    return;
  }

  let target;
  try {
    target = new URL(req.url, backendUrl);
    if (target.origin !== new URL(backendUrl).origin) {
      res.status(400).json({ error: 'Invalid API path.' });
      return;
    }
  } catch (error) {
    console.error('Invalid BACKEND_URL configuration:', error);
    res.status(500).json({ error: 'The canteen backend is misconfigured.' });
    return;
  }

  const headers = new Headers();
  for (const name of ['accept', 'content-type', 'cookie', 'authorization']) {
    const value = req.headers[name];
    if (typeof value === 'string') {
      headers.set(name, value);
    }
  }

  const options = { method: req.method, headers, redirect: 'manual' };
  if (req.method !== 'GET' && req.method !== 'HEAD' && req.body != null) {
    options.body = Buffer.isBuffer(req.body)
      ? req.body
      : typeof req.body === 'string'
        ? req.body
        : JSON.stringify(req.body);
  }

  try {
    const upstream = await fetch(target, options);
    for (const name of ['content-type', 'content-disposition', 'cache-control', 'location']) {
      const value = upstream.headers.get(name);
      if (value) {
        res.setHeader(name, value);
      }
    }

    const cookies = upstream.headers.getSetCookie
      ? upstream.headers.getSetCookie()
      : [upstream.headers.get('set-cookie')].filter(Boolean);
    if (cookies.length > 0) {
      res.setHeader('set-cookie', cookies);
    }

    res.statusCode = upstream.status;
    res.end(Buffer.from(await upstream.arrayBuffer()));
  } catch (error) {
    console.error('Canteen backend request failed:', error);
    res.status(502).json({ error: 'The canteen backend is unavailable.' });
  }
};
