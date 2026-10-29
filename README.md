# E-Library Service

A Java 17 library application with a responsive Chinese browser interface and a REST API for browsing, borrowing and returning digital books. The service models a limited number of concurrent reading licenses per title and keeps loan history after return.

The implementation focuses on a complete lending workflow, explicit business rules, database consistency and straightforward review. It is a single Spring Boot application with feature-oriented packages, not a distributed system.

See [design decisions and diagrams](docs/design.md) for the model, transaction sequence, alternatives and failure boundaries.

## Quick start

Requirements: a JDK 17 installation, `JAVA_HOME`, and internet access for the first Maven dependency download. Neither a global Maven installation nor Docker is required for the default run.

From this directory, on Windows PowerShell:

```powershell
.\mvnw.cmd --batch-mode --no-transfer-progress verify
java -jar target/e-library-service-1.0.0.jar
```

On Linux or macOS:

```bash
./mvnw --batch-mode --no-transfer-progress verify
java -jar target/e-library-service-1.0.0.jar
```

The default `demo` profile binds to `127.0.0.1:8080`. Open `http://127.0.0.1:8080/` for the library interface: search the catalog, open book details, sign in, borrow, view current loans and return books. The Java process must remain running. Start the application again after a computer restart.

The HTML/CSS/JavaScript, font, licensed icons and original fallback cover compositions are included in the JAR. No Node.js runtime or separate frontend server is needed to use it. Optional publisher-cover images reference Open Library directly according to its usage guidance; failed or low-resolution scans retain the local fallback. All core tasks work without that image service. The interface supports desktop/mobile, system dark mode, keyboard focus and reduced motion. See `src/main/resources/static` and `DESIGN.md`.

Browser credentials are held only in page memory, cleared on logout/refresh and sent through explicit Authorization headers with `credentials: omit`. A failed or timed-out write is not automatically repeated; the interface reconciles current loans and availability. Dynamic book and user text is inserted through DOM text nodes, and a Content Security Policy restricts assets and connections to the same origin.

It creates a file database at `./data/elibrary.mv.db`, 32 real technical titles in six editorial categories, and two **public demonstration accounts**:

| Username | Password |
| --- | --- |
| `alice` | `alice-demo-pass` |
| `bob` | `bob-demo-pass` |

Passwords are stored as BCrypt hashes. These are deliberately published review credentials, not deployment secrets. Stop with Ctrl+C. Restart from the same working directory to retain loans. Flyway applies each migration once; restarting does not reset the catalog or loan history.

The default file URL sets `WRITE_DELAY=0` to flush committed H2 records without its default 500ms delay. A subprocess regression test commits a record and halts the writer immediately; JAR-level acceptance also interrupts and restarts the application after saving a bookmark. This trades some local write throughput for clearer restart behavior. It is not a claim about hardware power-loss safety; see [H2 WRITE_DELAY](https://h2database.com/html/commands.html#set_write_delay).

The catalog includes original Chinese introductions, audience notes, reading highlights and bibliographic links. See [catalog sources](docs/catalog-sources.md) and `catalog/books.json`. Work-level source records can aggregate editions, so exact editions, publication years, page counts and ratings are not asserted. Reading-license capacities are sample business assumptions. Demo migration V6 enriches the original books without replacing their IDs, capacities or loan history; it inserts the remaining titles only when absent.

`python tools/generate_catalog.py` optionally checks that the versioned V6 seed matches the committed catalog and source records, entirely offline. This is a maintenance helper, not a startup requirement. After changing catalog data for a deployed database, generate a **new** migration using `--output <new-file>`; the helper refuses to overwrite an existing file.

Check readiness:

```bash
curl http://127.0.0.1:8080/actuator/health/readiness
```

## Try the workflow

The following commands use curl. In PowerShell use `curl.exe` to avoid the Windows PowerShell alias. Alternatively, run the PowerShell example below or use [requests.http](requests.http).

```bash
# Public catalog and detail
curl 'http://127.0.0.1:8080/api/books?q=java&page=0&size=20'
curl http://127.0.0.1:8080/api/books/2

# Borrow as Alice; note the returned loan id
curl -i -u alice:alice-demo-pass -H 'Content-Type: application/json' \
  -d '{"bookId":2}' http://127.0.0.1:8080/api/loans

# Alice's current loans
curl -u alice:alice-demo-pass http://127.0.0.1:8080/api/me/loans

# Replace 1 with the id returned by the borrowing request
curl -X PUT -u alice:alice-demo-pass http://127.0.0.1:8080/api/loans/1/return
```

PowerShell, including automatic capture of the actual loan id:

```powershell
$base = 'http://127.0.0.1:8080'
$token = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes('alice:alice-demo-pass'))
$headers = @{ Authorization = "Basic $token" }
Invoke-RestMethod "$base/api/books?q=java"
$loan = Invoke-RestMethod "$base/api/loans" -Method Post -Headers $headers `
  -ContentType 'application/json' -Body '{"bookId":2}'
Invoke-RestMethod "$base/api/me/loans" -Headers $headers
Invoke-RestMethod "$base/api/loans/$($loan.id)/return" -Method Put -Headers $headers
```

Book 2 has one license. A second active loan by Alice returns `ALREADY_BORROWED`; a concurrent attempt by Bob returns `NO_LICENSE_AVAILABLE`. After return, Bob can borrow it. Replaying Alice's old return request does not release Bob's license.

## API contract

The full contract, including schemas, authentication and errors, is in [docs/openapi.yaml](docs/openapi.yaml).

| Method | Path | Authentication | Result |
| --- | --- | --- | --- |
| GET | `/api/books` | Public | Paged catalog and current availability |
| GET | `/api/books/{bookId}` | Public | Book detail and current availability |
| POST | `/api/loans` | HTTP Basic | Create a loan from `{"bookId":2}`; `201` |
| PUT | `/api/loans/{loanId}/return` | HTTP Basic | Return an owned loan; `200` |
| GET | `/api/me/loans` | HTTP Basic | Paged current loans for the authenticated reader |
| GET | `/api/books/categories` | Public | Real category facets and counts |
| GET | `/api/me/loans?status=returned` | HTTP Basic | The reader's retained return history |
| GET | `/api/me/library` | HTTP Basic | Current/returned/saved counts for the reader |
| GET | `/api/me/favorites` | HTTP Basic | Paged saved books |
| PUT | `/api/me/favorites/{bookId}` | HTTP Basic | Save idempotently; consumes no reading license |
| DELETE | `/api/me/favorites/{bookId}` | HTTP Basic | Remove an owned bookmark idempotently; `204` |

Pagination is zero-based, defaults to `page=0&size=20`, and accepts sizes 1–100. Catalog order is ascending book id. Current loans use descending borrowing time, then descending id. Search matches title or author case-insensitively; `%`, `_` and `!` are literal characters. An out-of-range page returns an empty `items` array.

Catalog search also covers descriptions, categories, audience and highlights. Combine `category`, `availableOnly=true`, and `featuredOnly=true` as needed. `sort=catalog` preserves the default; `title` orders by case-insensitive title and `recent` uses descending catalog id. History uses return time then id descending. Saved books use save time then id descending. `status` defaults to `active`; only `active` and `returned` are accepted.

Offsets greater than JPA's signed 32-bit limit are rejected with `400`, rather than overflowing into a server error.

Responses use explicit DTOs. Page responses have `items`, `page`, `size`, `totalElements` and `totalPages`. Timestamps are UTC ISO 8601 values, stored to microsecond precision. `returnedAt` is null for an active loan.

Errors use `application/problem+json` with `type`, `title`, `status`, `detail`, `instance`, and stable `code` fields:

```json
{
  "type": "urn:elibrary:error:no-license-available",
  "title": "Conflict",
  "status": 409,
  "detail": "All licenses for this book are currently borrowed.",
  "instance": "/api/loans",
  "code": "NO_LICENSE_AVAILABLE"
}
```

| Status | Code | Meaning |
| --- | --- | --- |
| 400 | `INVALID_REQUEST` | Invalid parameters, identifiers, or JSON; validation errors may include `errors` |
| 401 | `UNAUTHORIZED` | Missing or incorrect credentials; includes a Basic challenge |
| 403 | `FORBIDDEN` | Access to an unexposed resource is denied |
| 404 | `BOOK_NOT_FOUND`, `LOAN_NOT_FOUND`, `NOT_FOUND` | Missing or invisible resource |
| 409 | `ALREADY_BORROWED`, `NO_LICENSE_AVAILABLE` | Lending rule conflict |
| 415 | `UNSUPPORTED_MEDIA_TYPE` | Borrowing requires JSON |
| 503 | `BOOK_BUSY` | A database lock wait timed out; `Retry-After: 1` |
| 500 | `INTERNAL_ERROR` | Unexpected failure; details are logged server-side |

Request bodies cannot select another user. Unknown properties, fractional book ids, malformed JSON and trailing JSON are rejected. Both a missing loan and another reader's loan return the same `LOAN_NOT_FOUND` detail. No password hashes or internal entities appear in responses.

## Domain and consistency

`Book` owns a fixed positive license capacity. `User` owns an identity and password hash. `Loan` references both and records borrowing and return times. Returning updates the loan; it does not delete it. There is no arbitrary per-reader total loan limit.

`Favorite` is a separate reader/book relationship with a database unique constraint. Saving or removing it never borrows or returns a license. Writes take the same book lock before checking/updating, making retries and concurrent saves deterministic without introducing an inverse lock order. This deliberately reuses a small, proven protocol; very high bookmark volume would merit a less contended, database-specific upsert path.

Every lending mutation follows one lock order inside a service transaction:

1. Identify and lock the book with `PESSIMISTIC_WRITE`.
2. For borrowing, check the reader's active loan and count all active loans for the book.
3. Insert the loan, or mark an owned loan as returned.
4. Commit, then release the database lock.

Availability is derived as `totalLicenses - activeLoanCount`. There is no separately updated stock counter. Catalog projection queries calculate availability without one additional query per book. Current-loan queries fetch their book details in the same query.

Returns first look up only an owned loan's book id, then lock the book, then load the loan. Loading the loan before waiting for the lock could leave stale state in JPA's first-level cache. Repeated returns preserve the original timestamp and never affect a newer loan.

The database additionally enforces foreign keys, positive license capacity, valid return time, and one active loan per reader/book. PostgreSQL uses a partial unique index. H2 uses a generated nullable key with a unique index. The capacity invariant still requires **every writer** to follow the book-lock protocol; a future administrative writer must use the same boundary.

This serializes writes for a single title. Other titles lock different rows. The trade-off is simple, predictable consistency at modest scale; a popular title can become a contention point. Lock waits are bounded to three seconds. Availability shown by a GET is a snapshot, and borrowing always rechecks it within the transaction.

## Database profiles and deployment

The default H2 file database provides a quick review path and persistence across restarts. It is intended for a single local process. The `postgres` profile uses PostgreSQL and does **not** seed public demo accounts. Flyway owns schema changes and Hibernate only validates them; `ddl-auto=update` is not used.

Run against a dedicated PostgreSQL 16 database:

```powershell
$env:DB_URL = 'jdbc:postgresql://127.0.0.1:5432/elibrary'
$env:DB_USERNAME = 'elibrary'
$env:DB_PASSWORD = '<your database password>'
java -jar target/e-library-service-1.0.0.jar --spring.profiles.active=postgres
```

For a **fresh, disposable review database** only, use `--spring.profiles.active=postgres,demo` to explicitly opt into demo data. Flyway retains migration history: disabling the demo profile later does not remove accounts already inserted. Use a separate deployment database rather than reusing a demo database.

Container configuration is provided in [Dockerfile](Dockerfile) and [compose.yml](compose.yml). Set `DB_PASSWORD` in the shell or an ignored `.env`, then run:

```bash
docker compose up --build
```

For an explicit local demonstration with PostgreSQL and public demo accounts:

```bash
docker compose -f compose.yml -f compose.demo.yml up --build
```

The runtime container uses a non-root user. Only the application port is published, bound to host loopback; PostgreSQL is internal to the Compose network. Database files use a named volume. The app waits for database health, supports graceful shutdown, and exposes liveness/readiness endpoints. Readiness includes a database check; health details remain hidden. Stop with `docker compose down`; it preserves database volumes.

HTTP Basic is deliberately limited to this review-oriented API. Browser sign-in verifies credentials through the current-loans endpoint; it creates no cookie/session or password-management endpoint. Writes use JSON POST, PUT or DELETE with explicit authorization; form submissions cannot perform these operations and credentialed cross-origin access is not enabled. A real deployment needs TLS, managed credentials or an identity provider, authorization policy, monitoring and tested backup/restore procedures.

## Tests and CI

```powershell
# Default database path: H2
.\mvnw.cmd --batch-mode --no-transfer-progress verify

# Run the same integration and concurrency suite on native PostgreSQL 16
.\mvnw.cmd --batch-mode --no-transfer-progress -Ppostgres-test verify

# Optional frontend request tests (Node.js 18+; Node is not an app runtime requirement)
node --test src/test/frontend/api.test.mjs
```

The PostgreSQL test profile starts a dedicated, loopback-only native server at a random port using Zonky Embedded Postgres. It needs no Docker or existing database service. Test-only native binaries are not included in the application JAR. The supplied binaries support x86-64 Windows/Linux; the native test server cannot run as root on Linux.

On Windows, `initdb` needs an ASCII runtime path. By default, native test data and binaries live under the JVM temporary directory in `elibrary-postgres-tests`; data is retained after the server stops. If that parent path contains non-ASCII characters, pass `'-Delibrary.pg.test.root=D:/your-ascii-test-directory'` to Maven. This option does not change the source directory or system environment variables.

The tests cover the workflow, authentication, ownership, validation, paging, keyword/facet/availability filters, sorting, error shape, constraints, lock-timeout recovery and old-return replay. They also verify private saved/history data, bookmark idempotence and concurrent saves. Concurrent tests call transactional services in independent threads without an enclosing test transaction and inspect committed state. Frontend request tests cover explicit credentials, safe error mapping and ambiguous writes without automatic resend.

JUnit XML/text reports are under `target/surefire-reports` and `target/postgres-test-reports`. CI is configured for H2 on Linux/Windows and PostgreSQL on Linux. Repository configuration alone does not establish that hosted CI or a container deployment has run.

## Deliberate scope and next steps

The five requested operations and their browser workflows are implemented. Content-file distribution, administration, reservations, due dates, renewal and fines are outside this version. Splitting this workflow into microservices or adding Redis/Kafka/gRPC would add operational and consistency costs without serving a current requirement.

Categories, richer discovery, saved books and retained history are bounded additions to the requested workflow. They add useful reader behavior while preserving the original API defaults and transaction rules. A full-text reader/DRM system is not implied by the metadata and license model.

Before expanding the system, prioritize identity lifecycle, real authorization and catalog administration, PostgreSQL operational hardening, a measured load test, database role separation, and deployment/back-up validation. Rate limiting, caching, asynchronous notifications and service extraction should follow measured needs. The current automated tests establish functional and database behavior; they are not a production availability or throughput benchmark.
