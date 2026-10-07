# Shepherd frontend

The citizen, authority and administrator interfaces use neutral backgrounds, light card borders, system fonts, responsive layouts and accessible form labels. A small brand mark and consistent panels provide visual definition. The home page explains emergency alerts, quarter guidance and family groups in plain language, with separate registration paths for citizens and authorities. Colour is limited to emergency and safety states.

## Files

- `src/main/resources/templates/`: the nine Thymeleaf pages.
- `src/main/resources/static/css/shepherd.css`: shared styles and responsive layouts.
- `src/main/resources/static/js/shepherd.js`: quarter search, invite-code copying, keyboard navigation and static-preview handling.
- `src/main/resources/static/js/safety.js`: shared maps, sound and connection status.
- `src/main/resources/static/js/citizen-dashboard.js`: citizen alert requests and household notifications.
- `src/main/resources/static/js/authority-dashboard.js`: initial jurisdiction alerts, incoming notifications and cancellations.

Spring renders the `th:href` and `th:src` attributes using existing routes. Forms retain their server field names and use `th:action` so Thymeleaf can apply Spring's form processing. The current security configuration disables CSRF; the citizen alert script supports a CSRF token if it is enabled later.

## Account validation

Citizen and authority registration require passwords of at least seven characters, reject all-space passwords and enforce BCrypt's 72-byte password limit. Email addresses must have a local part and a domain with a suffix; inputs such as `-1`, `name@host`, and `name@example.com-1` are rejected. Valid hyphens and plus tags, such as `name-1+tag@example.com`, are accepted. This checks address format; it does not verify ownership or whether an inbox exists.

The server trims and lowercases new email addresses and checks for existing accounts across citizens and authorities. Login uses a case-insensitive lookup, including for previously stored addresses. Failed registration displays errors and preserves non-sensitive entries; passwords and identity uploads must be entered again. Existing accounts keep their password hashes; the new length rule applies when registering.

Browser validation provides immediate feedback, while `AccountValidation` enforces the same rules even if browser checks are bypassed. Incorrect login credentials retain the generic error message. Tests under `src/test/java` cover validation, early rejection before file storage, login lookup, password authentication, controller errors and rendered registration pages.

## Behaviour

Quarter search filters the public directory by text. Family groups include a copy-code button, member lists and creation errors. Emergency requests show progress and request errors, prevent repeated clicks while pending, and allow cancellation without acquiring location again. Active citizen alerts survive a page reload. Authorities see existing alerts in their jurisdiction, not only alerts received after opening the page. Cancellation is broadcast to authority and household dashboards; one family member's cancellation does not clear another member's notification. Map tiles include OpenStreetMap attribution. Incoming alert text is inserted with DOM text nodes rather than interpreted as HTML.

Bootstrap, Leaflet, SockJS/STOMP and map tiles require network access. Geolocation needs a secure context or localhost. Audio notifications require the user to enable sound and keep the dashboard open.

## Validation

The nine templates passed checks for local links, form labels, duplicate IDs and inline JavaScript syntax. Browser review covered the desktop home page, sign-in page and all nine pages at a 390-pixel phone viewport, with no horizontal page overflow.

The backend compiled with Java 17. All 31 Java tests and 7 dashboard JavaScript tests passed. A full Spring application-flow test using an isolated H2 database covers all nine pages, registration, login, role restrictions, family creation, uploads, alerts, cancellation and administrator edits. Rendered test-page browser checks at desktop and phone sizes confirmed content, form labels, unique IDs, restored alerts and quarter search. H2 is test-only; PostgreSQL deployment and a real multi-device notification session still require separate verification.

Before release, exercise each role's authenticated flow, including location denial, failed alert requests, cancellation, family notifications, authority notifications and quarter updates.
