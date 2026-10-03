(() => {
  'use strict';
  const search = document.getElementById('quarterSearch');
  if (search) search.addEventListener('input', () => {
    const query = search.value.trim().toLocaleLowerCase();
    const rows = [...document.querySelectorAll('tbody tr')];
    rows.forEach(row => { row.hidden = !row.textContent.toLocaleLowerCase().includes(query); });
    document.getElementById('searchEmpty').hidden = rows.some(row => !row.hidden);
  });
  const copy = document.getElementById('copyInvite');
  if (copy) copy.addEventListener('click', async () => {
    const code = document.getElementById('inviteCode').textContent.trim();
    if (!code) return;
    try {
      await navigator.clipboard.writeText(code); copy.textContent = 'Copied';
      setTimeout(() => { copy.textContent = 'Copy code'; }, 2500);
    } catch {
      copy.textContent = 'Select code to copy';
      const range = document.createRange(); range.selectNodeContents(document.getElementById('inviteCode'));
      const selection = window.getSelection(); selection.removeAllRanges(); selection.addRange(range);
    }
  });
  document.querySelectorAll('input[type="file"]').forEach((input, index) => {
    input.id = input.id || `identity-picture-${index}`;
    if (input.previousElementSibling?.tagName === 'LABEL') input.previousElementSibling.htmlFor = input.id;
  });
  document.querySelectorAll('nav').forEach(nav => nav.setAttribute('aria-label', 'Main navigation'));
  const skip = document.createElement('a'); skip.href = '#main'; skip.textContent = 'Skip to content';
  skip.className = 'visually-hidden-focusable position-absolute bg-white p-3'; skip.style.zIndex = '2000'; document.body.prepend(skip);
  if (!document.getElementById('main')) {
    const content = document.querySelector('body > .container');
    if (content) { content.id = 'main'; content.setAttribute('tabindex', '-1'); }
  }
  if (!document.querySelector('.site-footer')) {
    const footer = document.createElement('footer'); footer.className = 'site-footer';
    const wrap = document.createElement('div'); wrap.className = 'container footer-inner';
    const brand = document.createElement('strong'); brand.textContent = 'Shepherd';
    const place = document.createElement('span'); place.textContent = 'Yaoundé, Cameroon';
    wrap.append(brand, place); footer.append(wrap); document.body.append(footer);
  }
  // Unprocessed Thymeleaf attributes identify a static preview.
  if (document.querySelector('[th\\:if], [th\\:each]')) {
    const banner = document.createElement('p'); banner.className = 'preview-banner';
    banner.textContent = 'Design preview — account data, registration and live alerts require the Shepherd server.';
    document.querySelector('nav').after(banner);
    const emergencyButton = document.getElementById('distressBtn');
    if (emergencyButton) { emergencyButton.disabled = true; emergencyButton.title = 'Live alerts require the Shepherd server.'; }
    document.querySelectorAll('[th\\:if], [th\\:each]').forEach(el => { el.hidden = true; });
    document.querySelectorAll('form').forEach(form => form.addEventListener('submit', event => {
      event.preventDefault(); banner.textContent = 'Connect these templates to the Shepherd server to use this form.';
      banner.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }));
  }
})();
