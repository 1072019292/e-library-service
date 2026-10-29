# E-Library product

<!-- impeccable:product-schema 1 -->

## Platform

web

## Users

Readers browse a small digital catalog, inspect a book, sign in, borrow a reading license, and return their loans. A hiring reviewer also needs to demonstrate the complete workflow quickly. The user requested a beautiful, usable Chinese frontend and delegated the design decisions.

## Product Purpose

Provide an actual browser interface for the five implemented library operations. All books, availability and loans come from the Java API. Successful interaction must be reflected by persisted backend state.

## Operating Context

The existing Java 17 Spring Boot application serves one origin at port 8080. The frontend ships inside the JAR and needs no second development server. Core UI, font, icons and original fallback covers are local. Optional publisher-cover thumbnails reference Open Library directly following its guidance; unavailable or low-resolution images do not block any task. Desktop and mobile layouts are required.

## Capabilities and Constraints

The API offers 32 sourced sample titles in 6 editorial categories, paged keyword/category/availability filters, sorting, curated featured titles, rich details, borrowing, returning, current and returned loans, and private saved books. HTTP Basic identifies the reader. Browser credentials stay in page memory with explicit authorization headers and no storage. Public accounts exist only in the demo profile. Content files, a full reader, due dates, ratings and personalised recommendations are not implemented or invented.

## Evidence on Hand

The sample catalog contains 32 real technical titles with checked bibliographic sources, original Chinese descriptions, audience notes and reading highlights. Thirty have source cover references; original local typography is the universal fallback. Availability is a configured sample concurrent-license count. Backend transaction/ownership and library-feature tests exist. The browser workflow has been exercised against persisted records, and the root serves the complete interface.

## Product Principles

- Make browsing and borrowing clear within the first viewport.
- Show real loading, error, empty, unavailable and success states.
- Keep catalog content public and private loans tied to the current reader.
- Preserve existing API behavior and package the UI with the service.
- Treat unknown request outcomes as a reason to reconcile state, not blindly repeat a write.
