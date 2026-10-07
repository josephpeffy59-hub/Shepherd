(() => {
  'use strict';
  document.querySelectorAll('[data-auth-form]').forEach(form => {
    const email = form.querySelector('input[type="email"]');
    const password = form.querySelector('input[autocomplete="new-password"]');
    const emailPattern = /^[A-Za-z0-9]+(?:[._%+\-][A-Za-z0-9]+)*@(?:[A-Za-z0-9](?:[A-Za-z0-9\-]{0,61}[A-Za-z0-9])?\.)+[A-Za-z]{2,63}$/;
    function validateEmail() {
      const value = email.value.trim();
      const at = value.indexOf('@');
      email.setCustomValidity(value && (!emailPattern.test(value) || value.length > 254 || at > 64)
        ? 'Enter a valid email address, such as name@example.com.' : '');
    }
    function validatePassword() {
      const value = password.value;
      password.setCustomValidity(value && (value.length < 7 || !value.trim())
        ? 'Your password must contain at least 7 characters and cannot be only spaces.'
        : new TextEncoder().encode(value).length > 72 ? 'Your password is too long. Please use a shorter password.' : '');
    }
    if (email) {
      email.addEventListener('input', validateEmail);
      email.addEventListener('blur', () => { email.value = email.value.trim(); validateEmail(); });
      validateEmail();
    }
    if (password) {
      password.addEventListener('input', validatePassword);
      validatePassword();
    }
    form.addEventListener('submit', event => {
      if (email) { email.value = email.value.trim(); validateEmail(); }
      if (password) validatePassword();
      if (!form.checkValidity()) { event.preventDefault(); form.reportValidity(); }
    });
  });
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
  if (document.body.hasAttribute('data-static-preview') || document.querySelector('[th\\:if], [th\\:each]')) {
    const banner = document.createElement('p'); banner.className = 'preview-banner';
    banner.textContent = 'Design preview — account data, registration and live alerts require the Shepherd server.';
    document.querySelector('nav').after(banner);
    const emergencyButton = document.getElementById('distressBtn');
    if (emergencyButton) { emergencyButton.disabled = true; emergencyButton.title = 'Live alerts require the Shepherd server.'; }
    document.querySelectorAll('[th\\:if], [th\\:each]').forEach(el => { el.hidden = !el.hasAttribute('data-preview-visible'); });
    document.querySelectorAll('form').forEach(form => form.addEventListener('submit', event => {
      event.preventDefault(); banner.textContent = 'Connect these templates to the Shepherd server to use this form.';
      banner.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }));
  }
})();
