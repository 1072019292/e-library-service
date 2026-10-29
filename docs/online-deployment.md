# Online review deployment

The Java application remains the sole owner of authentication, catalog queries,
loans, bookmarks, transactions and database migrations. Cloudflare Pages serves
the same HTML/assets included in the JAR and forwards API requests to the Java
application; it does not replace the backend with browser data.

## Layout

```text
Browser -> HTTPS Cloudflare Pages HTML/assets
                 -> API gateway -> HTTPS Render Java service
                                        -> private Render PostgreSQL
```

`render.yaml` defines a free Docker web service and a separate free PostgreSQL 16
database in Singapore. Database credentials are injected using Render database
bindings, never committed to Git. External PostgreSQL access is disabled through
`ipAllowList: []`. The application uses TLS to connect over Render's private
network. The `render` profile binds the application to `0.0.0.0:$PORT` and keeps a
small connection pool. The explicitly selected `demo` profile creates the public
review accounts and catalog in this dedicated database.

## Deploy the Java service

1. Publish this repository to the reviewer's accessible GitHub account.
2. In Render, create a Blueprint from this repository and its `main` branch.
3. Review that **both resources use Free** before creating them. No paid disk is
   needed: all state is in PostgreSQL.
4. Wait for the database and service to become healthy. The readiness path is
   `/actuator/health/readiness`; it includes a database connectivity check.
5. Record the actual service URL and database expiration shown by Render.

The Blueprint configures subsequent automatic application deployments to wait for
GitHub checks. Automatic repository notifications require a Git provider
connection in Render; a deployment created from the public repository URL can be
updated with the Blueprint's **Manual sync** action without granting access to
other repositories.
Render builds the existing multi-stage Dockerfile remotely; local Docker is not
required. Do not place secrets in a Dockerfile, build argument or public URL.

## Connect the Pages entry

Set `JAVA_ORIGIN` in `cloud/pages-gateway/wrangler.jsonc` to the actual HTTPS
`onrender.com` service origin, with no path, query, credentials or port. An unset
or unsafe origin deliberately fails closed. Build the asset bundle using an
explicit allowlist, which excludes local scans and runtime data. From
`cloud/pages-gateway`, use Node.js and the account's authenticated Wrangler:

```text
node build.mjs
wrangler pages project create lizhi-e-library --production-branch main
wrangler pages deploy ./dist --project-name lizhi-e-library --branch main
```

Create the Pages project once. Reuse it on updates. Preserve the deployment ID and
returned URL rather than guessing a live address. The gateway forwards each API
request once, including explicit authentication and JSON writes, to one configured
service. It preserves application security headers and error responses, does not
cache private data, and keeps application redirects on the Pages entry. It never
automatically repeats a potentially successful write after a network failure.

Run its dependency-free tests with:

```text
node --test cloud/pages-gateway/gateway.test.mjs
```

## Review limits and acceptance

Render's free web service sleeps after 15 minutes without requests. Waking it can
take a minute or longer; the first observed Java startup took several minutes on
the free instance. The interface loads from Pages independently and explains
temporary API failures, without resending uncertain writes. The free PostgreSQL database expires **30 days after
creation**, and Render does not offer free database backups. Record the actual
expiration; export or migrate the review database before then if it is still
needed. The free service filesystem is ephemeral, so H2 file mode is unsuitable
for this deployment. No keep-alive scheduler is included.

The two demo accounts are intentionally public and share review state. Use no real
personal or confidential data. This is a recruitment review environment, not a
production service or a guarantee of access from every ISP.

Before handing off the URL, check the root page and its assets, book browsing,
sign-in, borrow/return, private shelves, and retention after a service restart.
For Mainland China access, verify TLS over the physical network with HTTP proxies
disabled, inspect the edge trace for `loc=CN` and `warp=off`, and run a real API
workflow through the same public entry. A proxy-assisted success is not evidence
of direct Mainland access. Retain the dated acceptance receipt outside this repo.

References:
- [Render free service limits](https://render.com/docs/free)
- [Render Blueprint specification](https://render.com/docs/blueprint-spec)
- [Docker deployments on Render](https://render.com/docs/docker)
- [Cloudflare Pages advanced mode](https://developers.cloudflare.com/pages/functions/advanced-mode/)
