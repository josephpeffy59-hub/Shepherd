(() => {
  const config = window.shepherdCitizen || {};
  const safety = window.ShepherdSafety;
  const map = safety.createMap();
  const button = document.getElementById('distressBtn');
  const status = document.getElementById('status');
  let active = Boolean(config.active);
  let locationMarker;
  function showLocation(latitude, longitude) {
    if (!map) return;
    if (locationMarker) map.removeLayer(locationMarker);
    locationMarker = L.marker([latitude, longitude]).addTo(map).bindPopup('Your alert location');
    map.setView([latitude, longitude], 15);
  }
  function updateButton() {
    button.textContent = active ? 'Cancel alert' : 'Send emergency alert';
    button.classList.toggle('active', active);
  }
  updateButton();
  if (active) {
    status.textContent = 'You have an active alert. You can cancel it below.';
    if (safety.validLocation({lat: config.latitude, lng: config.longitude})) showLocation(config.latitude, config.longitude);
  }
  if (!config.userId || document.body.hasAttribute('data-static-preview')) {
    button.disabled = true;
    safety.setConnection('Design preview — sign in on the Shepherd server to use live alerts.');
    return;
  }
  button.addEventListener('click', async () => {
    if (button.disabled) return;
    safety.enableAudio();
    button.disabled = true;
    status.textContent = active ? 'Cancelling your alert…' : 'Finding your location…';
    try {
      let payload = {};
      if (!active) {
        if (!navigator.geolocation) throw new Error('This browser does not support location services.');
        const position = await new Promise((resolve, reject) => navigator.geolocation.getCurrentPosition(resolve, reject, {
          enableHighAccuracy: true, timeout: 15000, maximumAge: 0
        }));
        payload = { latitude: position.coords.latitude, longitude: position.coords.longitude };
        showLocation(payload.latitude, payload.longitude);
        status.textContent = 'Sending your alert…';
      }
      const headers = { 'Content-Type': 'application/json' };
      const csrf = document.querySelector('meta[name="_csrf"]');
      const csrfHeader = document.querySelector('meta[name="_csrf_header"]');
      if (csrf && csrfHeader && csrfHeader.content) headers[csrfHeader.content] = csrf.content;
      const response = await fetch(active ? '/api/alert/cancel' : '/api/alert/trigger', {
        method: 'POST', headers, body: JSON.stringify(payload)
      });
      if (response.redirected) throw new Error('Your session has expired. Sign in again before sending an alert.');
      if (!response.ok) throw new Error('Your request was not confirmed. Please try again.');
      if (!active && !response.headers.get('content-type')?.includes('application/json')) throw new Error('Your request was not confirmed. Please sign in again.');
      active = !active; updateButton();
      status.textContent = active ? 'Alert sent to authorities and your family group.' : 'Alert cancelled.';
    } catch (error) {
      status.textContent = error.code === 1 ? 'Allow location access in your browser to send an alert.'
        : error.code === 2 || error.code === 3 ? 'Your location could not be found. Please try again.'
        : error.message || 'Unable to send your alert. Please try again.';
    } finally { button.disabled = false; }
  });
  const familyAlerts = new Map();
  function renderFamily() {
    const container = document.getElementById('familyAlerts'); container.replaceChildren();
    document.getElementById('familyAlertsEmpty').hidden = familyAlerts.size > 0;
    familyAlerts.forEach(({alert}) => {
      const item = document.createElement('div'); item.className = 'alert alert-danger';
      item.append(safety.alertDetails(alert)); container.append(item);
    });
  }
  function receive(alert) {
    if (String(alert.userId) === String(config.userId)) return;
    const key = String(alert.userId);
    const previous = familyAlerts.get(key);
    if (alert.cancel) {
      if (previous?.marker && map) map.removeLayer(previous.marker);
      familyAlerts.delete(key); renderFamily(); return;
    }
    if (!safety.validLocation(alert)) return;
    if (previous?.marker && map) map.removeLayer(previous.marker);
    const marker = map ? L.marker([alert.lat, alert.lng]).addTo(map).bindPopup(safety.alertDetails(alert)) : null;
    familyAlerts.set(key, {alert, marker}); renderFamily(); safety.playSound();
    if (map) map.setView([alert.lat, alert.lng], 15);
  }
  if (config.familyId) safety.connect('/topic/family/' + config.familyId, receive);
  else safety.setConnection('Join a family group to receive household notifications.');
})();
