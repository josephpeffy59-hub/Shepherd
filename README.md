# Shepherd

Community safety application for Yaoundé, built with Java 17, Spring Boot, Thymeleaf, Spring Security, PostgreSQL, Leaflet and SockJS/STOMP.

## Current update

This repository contains the complete updated application code, including the frontend and the backend changes that support it:

- Nine pages with restrained styling, complete navigation, explanatory content, empty states and consistent footers.
- Citizen and authority registration with passwords of at least seven characters, email-format checks, duplicate-email checks and useful errors. Login looks up email addresses case-insensitively.
- Citizen dashboards that restore active alerts after a reload and handle location denial, failed requests and expired sessions.
- Authority dashboards that display existing alerts for their jurisdiction, receive new alerts and remove cancelled alerts.
- Family creation and invite-code copying, a searchable public quarter guide, administrator authority/alert records and quarter-update confirmation.
- Shared dashboard JavaScript and automated Java and JavaScript tests.

## Source locations

| Area | Location |
| --- | --- |
| Pages | `src/main/resources/templates/` |
| Styles | `src/main/resources/static/css/shepherd.css` |
| Browser behavior | `src/main/resources/static/js/` |
| Controllers, services, models and repositories | `src/main/java/com/crowdguard/` |
| Java tests | `src/test/java/` |
| Dashboard JavaScript tests | `src/test/js/dashboards.test.cjs` |

## Run locally

Use Java 17 and Maven. Configure `JDBC_DATABASE_URL`, `DB_USER` and `DB_PASSWORD` for your PostgreSQL database, then run:

```sh
mvn spring-boot:run
```

The default HTTP port is 8080; `PORT` can override it. Uploaded identity pictures use the directory configured by `crowdguard.upload-dir`.

Maps and the externally hosted browser libraries require network access. Browser location services require HTTPS or localhost. Notification sound requires user interaction and an open dashboard.

## Verification

```sh
mvn test
node --test src/test/js/dashboards.test.cjs
```

The update passed 31 Java tests and 7 JavaScript tests. The full application-flow test uses an isolated H2 database and covers all nine rendered pages, registration, login, role restrictions, family creation, uploads, alerts, cancellation and administrator edits. Desktop and 390-pixel phone browser checks covered rendered test pages, including restored alerts and quarter search, with no page overflow, missing form labels, duplicate IDs or uncaught JavaScript errors.

H2 is test-only; production continues to use PostgreSQL. These checks do not replace verification against the deployed PostgreSQL instance or a live multi-device notification session.

Additional implementation notes are in [docs/frontend.md](docs/frontend.md).
