# Vercel and Railway deployment

The static frontend is deployed to Vercel. The Spring Boot API and MySQL database run on Railway. The Vercel function in `api/` proxies `/api/*` requests to the backend so the existing session-cookie login continues to use one browser origin.

## Railway

1. Create a Railway project from this repository and add a MySQL service.
2. Deploy the application service from the repository root; Railway uses `Dockerfile` and `railway.json`.
3. Add references to the MySQL service's `MYSQLHOST`, `MYSQLPORT`, `MYSQLDATABASE`, `MYSQLUSER`, and `MYSQLPASSWORD` variables to the application service. The `railway` Spring profile uses these values; do not commit database credentials.
4. Generate a public domain for the application service and wait for its `/` health check to pass.

## Vercel

1. Import the same repository into Vercel, using the repository root as the project root.
2. Set the `BACKEND_URL` environment variable to the Railway application URL (for example, `https://your-service.up.railway.app`), with no `/api` suffix.
3. Deploy. Vercel uses `vercel.json` and `deploy/vercel-build.js` to publish the static pages and the API proxy. The frontend and API proxy remain on the same origin; Vercel forwards API calls to Railway.

The Java service requires a reachable Railway MySQL service and all five database variables before it can become healthy. Deployment requires a Git repository connected to both platforms and access to their accounts.
