(() => {
  const config = window.shepherdAuthority || {};
  const safety = window.ShepherdSafety;
  const map = safety.createMap();
  const alerts = new Map();
  const list = document.getElementById('alerts');
  function updateCount() {
    document.getElementById('alertsEmpty').hidden = alerts.size > 0;
    document.getElementById('alertCount').textContent = alerts.size;
  }
  function receive(alert, notify = true) {
    if (String(alert.quarterId) !== String(config.quarterId)) return;
    if (alert.cancel) {
      alerts.forEach((entry, key) => {
        if (String(entry.alert.userId) !== String(alert.userId)) return;
        entry.item.remove(); if (entry.marker && map) map.removeLayer(entry.marker); alerts.delete(key);
      });
      updateCount(); return;
    }
    if (!safety.validLocation(alert)) return;
    const key = String(alert.alertId || alert.userId);
    if (alerts.has(key)) return;
    const item = document.createElement('li'); item.className = 'list-group-item';
    item.append(safety.alertDetails(alert, true));
    const marker = map ? L.marker([alert.lat, alert.lng]).addTo(map).bindPopup(safety.alertDetails(alert, true)) : null;
    const locate = document.createElement('button'); locate.type = 'button';
    locate.className = 'btn btn-outline-shepherd btn-sm mt-2'; locate.textContent = 'Show on map';
    locate.disabled = !map;
    locate.addEventListener('click', () => { map.setView([alert.lat, alert.lng], 16); marker.openPopup(); });
    item.append(locate); list.prepend(item); alerts.set(key, {alert, item, marker}); updateCount();
    if (notify) { safety.playSound(); if (map) map.setView([alert.lat, alert.lng], 15); }
  }
  [...(config.alerts || [])].reverse().forEach(alert => receive(alert, false));
  updateCount();
  if (!config.quarterId || document.body.hasAttribute('data-static-preview')) { safety.setConnection('Design preview — sign in as an authority to receive alerts.'); return; }
  safety.connect('/topic/alerts', receive);
})();
