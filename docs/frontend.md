# Shepherd frontend

The citizen, authority and administrator interfaces use white backgrounds, neutral text, simple borders, system fonts, responsive layouts and accessible form labels. The home page explains emergency alerts, quarter guidance and family groups in plain language, with separate registration paths for citizens and authorities. Decorative illustrations, gradients, slogan sections and custom font downloads have been removed. Colour is limited to emergency and safety states.

## Files

- `src/main/resources/templates/`: the nine Thymeleaf pages.
- `src/main/resources/static/css/shepherd.css`: shared styles and responsive layouts.
- `src/main/resources/static/js/shepherd.js`: quarter search, invite-code copying, keyboard navigation and static-preview handling.

Spring renders the `th:href` and `th:src` attributes using existing routes. Forms retain their server field names and use `th:action` so Thymeleaf can apply Spring's form processing. The current security configuration disables CSRF; the citizen alert script supports a CSRF token if it is enabled later.

## Behaviour

Quarter search filters the rendered directory by text. Family groups include a copy-code button. Emergency requests show progress and request errors, prevent repeated clicks while pending, and allow cancellation without acquiring location again. Map tiles include OpenStreetMap attribution. Incoming alert names are escaped before insertion into map popups and notification markup.

Bootstrap, Leaflet, SockJS/STOMP and map tiles require network access. Geolocation needs a secure context or localhost. Audio notifications require the user to enable sound and keep the dashboard open.

## Validation

The nine templates passed checks for local links, form labels, duplicate IDs and inline JavaScript syntax. Browser review covered the desktop home page, sign-in page and all nine pages at a 390-pixel phone viewport, with no horizontal page overflow.

End-to-end registration, uploads, authenticated alerts and administrator updates require the running Spring application and its configured database. Maven was unavailable in the environment used for the redesign, so a backend build was not run.

Before release, exercise each role's authenticated flow, including location denial, failed alert requests, cancellation, family notifications, authority notifications and quarter updates.
