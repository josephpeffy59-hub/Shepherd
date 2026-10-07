/* Shared map, audio and live-update helpers for the two safety dashboards. */
window.ShepherdSafety = (() => {
  let audio;
  function enableAudio() {
    const Audio = window.AudioContext || window.webkitAudioContext;
    if (!Audio) return false;
    try {
      audio = audio || new Audio();
      if (audio.state === 'suspended') audio.resume().catch(() => {});
      return true;
    } catch { return false; }
  }
  const soundButton = document.getElementById('enableSound');
  if (soundButton) soundButton.addEventListener('click', event => {
    event.preventDefault();
    document.getElementById('soundBanner').textContent = enableAudio()
      ? 'Sound alerts enabled. Keep this page open to receive notifications.'
      : 'Sound is unavailable in this browser. New alerts will still appear on this page.';
  });
  function playSound() {
    if (!audio || audio.state !== 'running') return;
    [880, 660, 880, 660].forEach((frequency, index) => {
      const time = audio.currentTime + index * .25;
      const oscillator = audio.createOscillator();
      const gain = audio.createGain();
      oscillator.frequency.value = frequency;
      gain.gain.setValueAtTime(.18, time);
      gain.gain.exponentialRampToValueAtTime(.001, time + .22);
      oscillator.connect(gain).connect(audio.destination);
      oscillator.start(time); oscillator.stop(time + .22);
    });
    if (navigator.vibrate) navigator.vibrate([200, 100, 200]);
  }
  function createMap() {
    const container = document.getElementById('map');
    if (!window.L) {
      container.textContent = 'The map could not load. Check your internet connection. Location-based alerts can still be sent.';
      container.classList.add('empty-state');
      return null;
    }
    const map = L.map(container).setView([3.848, 11.5021], 12);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
    }).addTo(map);
    return map;
  }
  function validLocation(alert) {
    return Number.isFinite(alert.lat) && Number.isFinite(alert.lng)
      && Math.abs(alert.lat) <= 90 && Math.abs(alert.lng) <= 180;
  }
  function alertDetails(alert, includeId = false) {
    const details = document.createElement('div');
    const name = document.createElement('strong'); name.textContent = alert.userName || 'Citizen';
    const quarter = document.createElement('p'); quarter.className = 'mb-1'; quarter.textContent = alert.quarter || 'Quarter not recorded';
    const when = document.createElement('small');
    const time = alert.timestamp ? new Date(alert.timestamp) : null;
    when.textContent = time && !Number.isNaN(time.getTime()) ? time.toLocaleString() : 'New alert';
    details.append(name, quarter, when);
    if (includeId) {
      const links = document.createElement('div'); links.className = 'mt-2 d-flex gap-3';
      ['idPicture1', 'idPicture2'].forEach((field, index) => {
        if (!alert[field]) return;
        const link = document.createElement('a');
        link.href = '/uploads/' + encodeURIComponent(String(alert[field]));
        link.target = '_blank'; link.rel = 'noopener noreferrer'; link.textContent = 'View ID ' + (index + 1);
        links.append(link);
      });
      details.append(links);
    }
    return details;
  }
  function setConnection(text) {
    const status = document.getElementById('connectionStatus');
    if (status) status.textContent = text;
  }
  function connect(topic, onAlert, onConnected) {
    if (!window.SockJS || !window.Stomp) {
      setConnection('Live updates could not load. Check your connection and reload this page.');
      return;
    }
    let retry;
    function start() {
      setConnection('Connecting to live updates…');
      const socket = new SockJS('/ws');
      const client = Stomp.over(socket); client.debug = null;
      function disconnected() {
        setConnection('Live updates disconnected. Reconnecting…');
        if (!retry) retry = setTimeout(() => { retry = null; start(); }, 5000);
      }
      client.connect({}, () => {
        if (retry) { clearTimeout(retry); retry = null; }
        setConnection('Live updates connected');
        client.subscribe(topic, message => {
          try { onAlert(JSON.parse(message.body)); } catch (error) { console.error('Unable to display an alert.', error); }
        });
        if (onConnected) onConnected();
      }, disconnected);
      socket.addEventListener('close', disconnected);
    }
    start();
  }
  return { createMap, validLocation, alertDetails, playSound, enableAudio, connect, setConnection };
})();
