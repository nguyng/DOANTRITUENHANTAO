/* ==========================================================================
   MedCore HMS — UI primitives
   toast · modal · confirm · drawer · dropdown · tooltip · tabs · segmented
   copy-id · ripple · form validation · empty states · skeleton · count-up
   ========================================================================== */
(function (H) {
  'use strict';
  const U = H.u, ic = H.ic;
  const UI = {};

  /* ---------------- Toast ---------------- */
  const TOAST_META = { success: ['circle-check', 'success'], error: ['circle-x', 'danger'], warning: ['triangle-alert', 'warning'], info: ['info', 'primary'] };
  UI.toast = (title, msg = '', type = 'success', ms = 4200) => {
    let stack = U.$('.toast-stack');
    if (!stack) { stack = document.createElement('div'); stack.className = 'toast-stack'; stack.setAttribute('aria-live', 'polite'); document.body.appendChild(stack); }
    const [icon, c] = TOAST_META[type] || TOAST_META.info;
    const el = document.createElement('div');
    el.className = `toast c-${c}`;
    el.setAttribute('role', 'status');
    el.innerHTML = `<div class="ichip c-${c}">${ic(icon)}</div>
      <div class="flex-1"><div class="toast-title">${U.esc(title)}</div>${msg ? `<div class="toast-msg">${msg}</div>` : ''}</div>
      <button class="toast-close" aria-label="Đóng">${ic('x')}</button>
      <div class="toast-bar" style="animation-duration:${ms}ms"></div>`;
    stack.appendChild(el);
    H.refreshIcons(el);
    const close = () => { if (el.classList.contains('leaving')) return; el.classList.add('leaving'); setTimeout(() => el.remove(), 280); };
    let timer = setTimeout(close, ms);
    el.addEventListener('mouseenter', () => { clearTimeout(timer); el.querySelector('.toast-bar').style.animationPlayState = 'paused'; });
    el.addEventListener('mouseleave', () => { timer = setTimeout(close, 1600); el.querySelector('.toast-bar').style.animationPlayState = 'running'; });
    el.querySelector('.toast-close').onclick = close;
    const all = stack.querySelectorAll('.toast:not(.leaving)');
    if (all.length > 4) all[0].querySelector('.toast-close').click();
    return close;
  };

  /* ---------------- Modal ---------------- */
  const openStack = [];
  UI.modal = ({ title = '', sub = '', icon = '', iconColor = 'primary', body = '', foot = '', size = '', onMount, onClose, className = '', closable = true, raw = false } = {}) => {
    const root = document.createElement('div');
    root.className = 'modal-root';
    const headHtml = raw ? '' : `<div class="modal-head">
        ${icon ? `<div class="ichip c-${iconColor}">${ic(icon)}</div>` : ''}
        <div class="flex-1"><div class="modal-title" id="${U.uid('mt')}">${title}</div>${sub ? `<div class="modal-sub">${sub}</div>` : ''}</div>
        ${closable ? `<button class="icon-btn modal-close" data-close aria-label="Đóng">${ic('x')}</button>` : ''}
      </div>`;
    root.innerHTML = `<div class="modal-backdrop" ${closable ? 'data-close' : ''}></div>
      <div class="modal ${size} ${className}" role="dialog" aria-modal="true">
        ${headHtml}
        ${raw ? body : `<div class="modal-body">${body}</div>`}
        ${foot ? `<div class="modal-foot">${foot}</div>` : ''}
      </div>`;
    document.body.appendChild(root);
    document.body.style.overflow = 'hidden';
    H.refreshIcons(root);
    const api = {
      el: root, modal: root.querySelector('.modal'),
      close: (result) => {
        if (root.classList.contains('closing')) return;
        root.classList.add('closing');
        const i = openStack.indexOf(api); if (i >= 0) openStack.splice(i, 1);
        setTimeout(() => { root.remove(); if (!openStack.length) document.body.style.overflow = ''; }, 200);
        onClose && onClose(result);
      },
      $: (s) => root.querySelector(s), $$: (s) => U.$$(s, root)
    };
    openStack.push(api);
    root.addEventListener('click', (e) => { if (e.target.closest('[data-close]')) api.close(); });
    const first = root.querySelector('input:not([type=hidden]):not([type=checkbox]), select, textarea');
    setTimeout(() => (first || root.querySelector('.modal')).focus?.(), 60);
    onMount && onMount(api);
    return api;
  };
  document.addEventListener('keydown', (e) => { if (e.key === 'Escape' && openStack.length) openStack[openStack.length - 1].close(); });

  UI.confirm = ({ title = 'Bạn chắc chắn?', message = '', confirmText = 'Xác nhận', cancelText = 'Hủy', danger = false, icon, requireText } = {}) => new Promise((resolve) => {
    let ok = false;
    const m = UI.modal({
      size: 'sm', raw: true,
      body: `<div class="modal-body" style="padding-top:26px">
        <div class="confirm-ic ichip c-${danger ? 'danger' : 'primary'}">${ic(icon || (danger ? 'triangle-alert' : 'circle-help'))}</div>
        <div class="modal-title">${title}</div>
        <div class="modal-sub" style="margin-top:6px;line-height:1.55">${message}</div>
        ${requireText ? `<div class="field mt-4"><label class="label">Nhập <b class="mono">${U.esc(requireText)}</b> để xác nhận</label><input class="input" data-req autocomplete="off"></div>` : ''}
      </div>`,
      foot: `<button class="btn btn-secondary" data-close>${cancelText}</button><button class="btn ${danger ? 'btn-danger' : 'btn-primary'}" data-ok ${requireText ? 'disabled' : ''}><span>${confirmText}</span></button>`,
      onClose: () => resolve(ok)
    });
    const okBtn = m.$('[data-ok]');
    if (requireText) m.$('[data-req]').addEventListener('input', (e) => { okBtn.disabled = e.target.value.trim() !== requireText; });
    okBtn.addEventListener('click', () => { ok = true; m.close(true); });
  });

  UI.success = (title, message = '', ms = 1700) => {
    const m = UI.modal({
      size: 'sm', raw: true,
      body: `<div class="modal-body text-center" style="padding:34px 24px 30px">
        <div class="success-check"><svg viewBox="0 0 52 52" fill="none" stroke-width="3.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="26" cy="26" r="24"/><path d="M15 27l7 7 15-15"/></svg></div>
        <div class="modal-title mt-4">${title}</div>${message ? `<div class="modal-sub mt-1">${message}</div>` : ''}</div>`
    });
    setTimeout(() => m.close(), ms);
    return m;
  };

  /* ---------------- Drawer ---------------- */
  UI.drawer = ({ title = '', sub = '', body = '', foot = '', onMount, width } = {}) => {
    const root = document.createElement('div');
    root.className = 'drawer-root';
    root.innerHTML = `<div class="modal-backdrop" data-close></div>
      <aside class="drawer" role="dialog" aria-modal="true" ${width ? `style="width:min(${width}px,100%)"` : ''}>
        <div class="modal-head"><div class="flex-1"><div class="modal-title">${title}</div>${sub ? `<div class="modal-sub">${sub}</div>` : ''}</div><button class="icon-btn modal-close" data-close aria-label="Đóng">${ic('x')}</button></div>
        <div class="modal-body flex-1">${body}</div>${foot ? `<div class="modal-foot" style="border-radius:0">${foot}</div>` : ''}
      </aside>`;
    document.body.appendChild(root);
    H.refreshIcons(root);
    const api = { el: root, $: (s) => root.querySelector(s), close: () => { root.classList.add('closing'); const i = openStack.indexOf(api); if (i >= 0) openStack.splice(i, 1); setTimeout(() => root.remove(), 240); } };
    openStack.push(api);
    root.addEventListener('click', (e) => { if (e.target.closest('[data-close]')) api.close(); });
    onMount && onMount(api);
    return api;
  };

  /* ---------------- Dropdown (global delegation) ---------------- */
  UI.closeDropdowns = (except) => U.$$('.dropdown.open').forEach((d) => { if (d !== except) { d.classList.remove('open'); d.querySelector('[data-dd-toggle]')?.setAttribute('aria-expanded', 'false'); } });
  document.addEventListener('click', (e) => {
    const t = e.target.closest('[data-dd-toggle]');
    if (t) {
      const dd = t.closest('.dropdown');
      const willOpen = !dd.classList.contains('open');
      UI.closeDropdowns(dd);
      dd.classList.toggle('open', willOpen);
      t.setAttribute('aria-expanded', String(willOpen));
      if (willOpen) dd.dispatchEvent(new CustomEvent('dd:open', { bubbles: true }));
      e.stopPropagation();
      return;
    }
    const inMenu = e.target.closest('.dropdown .menu');
    if (inMenu && e.target.closest('[data-dd-close], .menu-item')) { UI.closeDropdowns(); return; }
    if (!inMenu) UI.closeDropdowns();
  });

  /* ---------------- Tooltip ---------------- */
  const tip = document.createElement('div');
  tip.className = 'tooltip'; tip.setAttribute('role', 'tooltip');
  document.addEventListener('DOMContentLoaded', () => document.body.appendChild(tip));
  let tipTarget = null;
  const showTip = (el) => {
    const text = el.getAttribute('data-tip');
    if (!text) return;
    if (el.hasAttribute('data-tip-collapsed') && !document.documentElement.classList.contains('sb-collapsed')) return;
    if (el.hasAttribute('data-tip-collapsed') && window.innerWidth <= 768) return;
    tipTarget = el;
    tip.textContent = text;
    const r = el.getBoundingClientRect();
    const pos = el.getAttribute('data-tip-pos') || (el.hasAttribute('data-tip-collapsed') ? 'right' : 'top');
    tip.className = 'tooltip ' + pos;
    tip.style.left = '0px'; tip.style.top = '0px';
    const tr = tip.getBoundingClientRect();
    if (pos === 'right') { tip.style.left = r.right + 12 + 'px'; tip.style.top = r.top + r.height / 2 - tr.height / 2 + 'px'; }
    else { tip.style.left = U.clamp(r.left + r.width / 2 - tr.width / 2, 8, innerWidth - tr.width - 8) + 'px'; tip.style.top = r.top - tr.height - 8 + 'px'; }
    requestAnimationFrame(() => tip.classList.add('show'));
  };
  document.addEventListener('mouseover', (e) => { const el = e.target.closest('[data-tip]'); if (el && el !== tipTarget) showTip(el); else if (!el && tipTarget) { tip.classList.remove('show'); tipTarget = null; } });
  document.addEventListener('focusin', (e) => { const el = e.target.closest('[data-tip]'); if (el) showTip(el); });
  document.addEventListener('focusout', () => { tip.classList.remove('show'); tipTarget = null; });
  window.addEventListener('scroll', () => { tip.classList.remove('show'); tipTarget = null; }, true);

  /* ---------------- Tabs with animated ink ---------------- */
  UI.tabs = (container, onChange) => {
    const tabsEl = container.matches('.tabs') ? container : container.querySelector('.tabs');
    if (!tabsEl) return;
    let ink = tabsEl.querySelector('.tab-ink');
    if (!ink) { ink = document.createElement('span'); ink.className = 'tab-ink'; tabsEl.appendChild(ink); }
    const move = () => { const a = tabsEl.querySelector('.tab.active'); if (a) { ink.style.left = a.offsetLeft + 'px'; ink.style.width = a.offsetWidth + 'px'; } };
    tabsEl.addEventListener('click', (e) => {
      const t = e.target.closest('.tab'); if (!t || t.classList.contains('active')) return;
      tabsEl.querySelectorAll('.tab').forEach((x) => { x.classList.toggle('active', x === t); x.setAttribute('aria-selected', String(x === t)); });
      move();
      t.scrollIntoView({ behavior: 'smooth', inline: 'nearest', block: 'nearest' });
      onChange && onChange(t.dataset.tab, t);
    });
    requestAnimationFrame(move); setTimeout(move, 300);
    window.addEventListener('resize', move);
    return { move };
  };

  /* ---------------- Segmented control ---------------- */
  UI.segmented = (el, onChange) => {
    if (!el) return;
    let thumb = el.querySelector('.seg-thumb');
    if (!thumb) { thumb = document.createElement('span'); thumb.className = 'seg-thumb'; el.prepend(thumb); }
    const move = () => { const a = el.querySelector('button.active'); if (a) { thumb.style.left = a.offsetLeft + 'px'; thumb.style.width = a.offsetWidth + 'px'; } };
    el.addEventListener('click', (e) => {
      const b = e.target.closest('button'); if (!b || b.classList.contains('active')) return;
      el.querySelectorAll('button').forEach((x) => x.classList.toggle('active', x === b));
      move(); onChange && onChange(b.dataset.v, b);
    });
    requestAnimationFrame(move); setTimeout(move, 250);
  };
  UI.segHtml = (opts, active, cls = '') => `<div class="segmented ${cls}" role="tablist">${opts.map(([v, l]) => `<button type="button" data-v="${v}" class="${v === active ? 'active' : ''}">${l}</button>`).join('')}</div>`;
  UI.RANGE_OPTS = [['day', 'Ngày'], ['week', 'Tuần'], ['month', 'Tháng'], ['year', 'Năm']];

  /* ---------------- Copy ID ---------------- */
  document.addEventListener('click', async (e) => {
    const b = e.target.closest('[data-copy]'); if (!b) return;
    e.stopPropagation();
    const v = b.getAttribute('data-copy');
    try { await navigator.clipboard.writeText(v); } catch (_) { const t = document.createElement('textarea'); t.value = v; document.body.appendChild(t); t.select(); document.execCommand('copy'); t.remove(); }
    b.classList.add('copied');
    const svg = b.querySelector('svg'); if (svg) { svg.outerHTML = ic('check'); H.refreshIcons(b); }
    UI.toast('Đã sao chép', `<span class="mono">${U.esc(v)}</span> đã được sao chép vào clipboard.`, 'info', 2200);
    setTimeout(() => { b.classList.remove('copied'); const s = b.querySelector('svg'); if (s) { s.outerHTML = ic('copy'); H.refreshIcons(b); } }, 1600);
  });

  /* ---------------- Ripple on buttons ---------------- */
  document.addEventListener('pointerdown', (e) => {
    const b = e.target.closest('.btn'); if (!b || b.disabled) return;
    const r = b.getBoundingClientRect(), s = Math.max(r.width, r.height);
    const sp = document.createElement('span'); sp.className = 'ripple';
    sp.style.cssText = `width:${s}px;height:${s}px;left:${e.clientX - r.left - s / 2}px;top:${e.clientY - r.top - s / 2}px`;
    b.appendChild(sp); setTimeout(() => sp.remove(), 600);
  });

  /* ---------------- Button loading ---------------- */
  UI.btnLoading = (btn, on = true) => {
    if (!btn) return;
    if (on) { btn.classList.add('loading'); if (!btn.querySelector('.spinner')) btn.insertAdjacentHTML('beforeend', '<span class="spinner"></span>'); btn.disabled = true; }
    else { btn.classList.remove('loading'); btn.querySelector('.spinner')?.remove(); btn.disabled = false; }
  };

  /* ---------------- Form validation ---------------- */
  // Rule attrs trên input: required, data-rule="email|phone|min:3|number|date-future", data-label
  const RULES = {
    email: (v) => /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(v) || 'Email không hợp lệ',
    phone: (v) => /^0\d{9,10}$/.test(v.replace(/\s/g, '')) || 'Số điện thoại gồm 10–11 chữ số, bắt đầu bằng 0',
    number: (v) => !isNaN(+v) || 'Phải là số',
    bhyt: (v) => !v || /^[A-Z]{2}\d{13}$/.test(v.replace(/\s/g, '').toUpperCase()) || 'Mã BHYT gồm 2 chữ cái + 13 chữ số',
    'date-future': (v) => !v || U.startOfDay(new Date(v)) >= U.startOfDay(new Date()) || 'Ngày phải từ hôm nay trở đi'
  };
  UI.validateField = (input) => {
    const field = input.closest('.field'); if (!field) return true;
    const v = (input.value || '').trim();
    let msg = '';
    if (input.hasAttribute('required') && !v) msg = 'Vui lòng nhập ' + (input.dataset.label || 'trường này').toLowerCase();
    else if (v && input.dataset.rule) {
      for (const rule of input.dataset.rule.split('|')) {
        const [name, arg] = rule.split(':');
        if (name === 'min') { if (v.length < +arg) { msg = `Tối thiểu ${arg} ký tự`; break; } continue; }
        const res = RULES[name] ? RULES[name](v) : true;
        if (res !== true) { msg = res; break; }
      }
    }
    let m = field.querySelector('.field-msg');
    if (!m) { m = document.createElement('div'); m.className = 'field-msg'; field.appendChild(m); }
    field.classList.toggle('has-error', !!msg);
    field.classList.toggle('has-success', !msg && !!v && input.dataset.rule !== undefined);
    if (msg) { m.innerHTML = ic('circle-alert') + U.esc(msg); H.refreshIcons(m); }
    else if (field.classList.contains('has-success')) { m.innerHTML = ic('circle-check') + 'Hợp lệ'; H.refreshIcons(m); }
    else m.innerHTML = m.dataset.hint || '';
    return !msg;
  };
  UI.validateForm = (form) => {
    let ok = true, first = null;
    U.$$('input, select, textarea', form).forEach((i) => { if (!UI.validateField(i)) { ok = false; first = first || i; } });
    if (first) { const f = first.closest('.field'); f.classList.remove('shake'); void f.offsetWidth; f.classList.add('shake'); first.focus(); }
    return ok;
  };
  UI.bindValidation = (form) => {
    form.addEventListener('blur', (e) => { if (e.target.matches('input, select, textarea')) UI.validateField(e.target); }, true);
    form.addEventListener('input', (e) => { if (e.target.closest('.field.has-error')) UI.validateField(e.target); });
  };
  UI.field = ({ name, label, type = 'text', required, rule, placeholder = '', value = '', options, full, hint = '', icon, rows }) => {
    const id = U.uid('f');
    const attrs = `id="${id}" name="${name}" ${required ? 'required' : ''} ${rule ? `data-rule="${rule}"` : ''} data-label="${U.esc(label)}" placeholder="${U.esc(placeholder)}"`;
    let control;
    if (type === 'select') control = `<select class="select" ${attrs}>${placeholder ? `<option value="">${U.esc(placeholder)}</option>` : ''}${options.map((o) => { const [v, l] = Array.isArray(o) ? o : [o, o]; return `<option value="${U.esc(v)}" ${String(v) === String(value) ? 'selected' : ''}>${U.esc(l)}</option>`; }).join('')}</select>`;
    else if (type === 'textarea') control = `<textarea class="textarea" ${attrs} rows="${rows || 3}">${U.esc(value)}</textarea>`;
    else control = `<input class="input" type="${type}" value="${U.esc(value)}" ${attrs}>`;
    if (icon) control = `<div class="input-wrap">${ic(icon)}${control}</div>`;
    return `<div class="field ${full ? 'full' : ''}"><label class="label" for="${id}">${U.esc(label)}${required ? '<span class="req">*</span>' : ''}</label>${control}<div class="field-msg" data-hint="${U.esc(hint)}">${U.esc(hint)}</div></div>`;
  };

  /* ---------------- Empty states (SVG minh họa theo theme) ---------------- */
  const ART = {
    patients: `<circle cx="75" cy="60" r="44" fill="var(--primary-soft)"/><circle cx="75" cy="48" r="14" fill="none" stroke="var(--primary)" stroke-width="3"/><path d="M50 88c4-14 14-20 25-20s21 6 25 20" fill="none" stroke="var(--primary)" stroke-width="3" stroke-linecap="round"/><circle cx="114" cy="30" r="10" fill="var(--surface)" stroke="var(--accent)" stroke-width="2.5"/><path d="M110 30h8M114 26v8" stroke="var(--accent)" stroke-width="2.5" stroke-linecap="round"/>`,
    calendar: `<circle cx="75" cy="60" r="44" fill="var(--primary-soft)"/><rect x="46" y="34" width="58" height="52" rx="9" fill="var(--surface)" stroke="var(--primary)" stroke-width="3"/><path d="M46 50h58M60 28v12M90 28v12" stroke="var(--primary)" stroke-width="3" stroke-linecap="round"/><circle cx="62" cy="64" r="3" fill="var(--text-3)"/><circle cx="75" cy="64" r="3" fill="var(--text-3)"/><circle cx="88" cy="64" r="3" fill="var(--accent)"/><circle cx="62" cy="76" r="3" fill="var(--text-3)"/>`,
    lab: `<circle cx="75" cy="60" r="44" fill="var(--primary-soft)"/><path d="M66 30v22L50 82a6 6 0 0 0 5 9h40a6 6 0 0 0 5-9L84 52V30" fill="var(--surface)" stroke="var(--primary)" stroke-width="3" stroke-linejoin="round"/><path d="M62 30h26" stroke="var(--primary)" stroke-width="3" stroke-linecap="round"/><path d="M56 74h38l5 8a3 3 0 0 1-3 4H54a3 3 0 0 1-3-4z" fill="var(--accent)" opacity=".35"/><circle cx="70" cy="66" r="3" fill="var(--accent)"/><circle cx="80" cy="60" r="2" fill="var(--accent)"/>`,
    bell: `<circle cx="75" cy="60" r="44" fill="var(--primary-soft)"/><path d="M57 72V58a18 18 0 0 1 36 0v14l5 6H52z" fill="var(--surface)" stroke="var(--primary)" stroke-width="3" stroke-linejoin="round"/><path d="M69 84a6 6 0 0 0 12 0" stroke="var(--primary)" stroke-width="3" stroke-linecap="round" fill="none"/><path d="M104 36l6-6M108 46h8M98 28v-8" stroke="var(--accent)" stroke-width="2.5" stroke-linecap="round"/>`,
    data: `<circle cx="75" cy="60" r="44" fill="var(--primary-soft)"/><rect x="48" y="34" width="54" height="56" rx="8" fill="var(--surface)" stroke="var(--primary)" stroke-width="3"/><path d="M58 76V64M70 76V54M82 76V60M94 76V48" stroke="var(--accent)" stroke-width="5" stroke-linecap="round" opacity=".7"/><path d="M58 46h22" stroke="var(--text-3)" stroke-width="3" stroke-linecap="round"/>`,
    search: `<circle cx="75" cy="60" r="44" fill="var(--primary-soft)"/><circle cx="70" cy="56" r="18" fill="var(--surface)" stroke="var(--primary)" stroke-width="3"/><path d="M83 69l14 14" stroke="var(--primary)" stroke-width="4" stroke-linecap="round"/><path d="M63 52c2-4 6-6 10-5" stroke="var(--accent)" stroke-width="2.5" stroke-linecap="round" fill="none"/>`
  };
  UI.empty = ({ art = 'data', title = 'Không có dữ liệu', desc = '', action = '' } = {}) =>
    `<div class="empty"><svg class="empty-art" viewBox="0 0 150 120" fill="none" aria-hidden="true">${ART[art] || ART.data}<ellipse cx="75" cy="112" rx="36" ry="4" fill="var(--border)"/></svg><div class="empty-title">${title}</div>${desc ? `<div class="empty-desc">${desc}</div>` : ''}${action}</div>`;

  /* ---------------- Skeleton helpers ---------------- */
  UI.skelRows = (n = 6, cols = 6) => Array.from({ length: n }, () => `<tr>${Array.from({ length: cols }, (_, i) => `<td>${i === 0 ? '<div class="flex items-center gap-3"><div class="skel skel-circle" style="width:34px;height:34px"></div><div class="flex-1"><div class="skel skel-line" style="width:70%"></div><div class="skel skel-line" style="width:40%;height:9px"></div></div></div>' : `<div class="skel skel-line" style="width:${40 + ((i * 17) % 50)}%"></div>`}</td>`).join('')}</tr>`).join('');
  UI.skelCards = (n = 4, h = 120) => Array.from({ length: n }, () => `<div class="card card-pad"><div class="flex justify-between"><div style="flex:1"><div class="skel skel-line" style="width:50%"></div><div class="skel" style="height:28px;width:60%;margin-top:12px"></div></div><div class="skel" style="width:44px;height:44px;border-radius:13px"></div></div><div class="skel" style="height:${h - 80}px;margin-top:16px"></div></div>`).join('');

  /* ---------------- Count-up animation ---------------- */
  UI.countUp = (root = document) => {
    U.$$('[data-count]', root).forEach((el) => {
      const target = parseFloat(el.dataset.count), dur = 1100, fmt = el.dataset.fmt;
      const start = performance.now();
      const render = (v) => { el.textContent = fmt === 'money' ? U.moneyShort(v) : fmt === 'pct' ? v.toFixed(1).replace('.', ',') + '%' : U.num(v); };
      const step = (t) => { const p = Math.min(1, (t - start) / dur); const e = 1 - Math.pow(1 - p, 4); render(target * e); if (p < 1) requestAnimationFrame(step); else render(target); };
      requestAnimationFrame(step);
    });
  };

  /* ---------------- CSV / Print export ---------------- */
  UI.exportCSV = (filename, headers, rows) => {
    const esc = (v) => `"${String(v ?? '').replace(/"/g, '""')}"`;
    const csv = '\uFEFF' + [headers.map(esc).join(','), ...rows.map((r) => r.map(esc).join(','))].join('\n');
    const a = document.createElement('a');
    a.href = URL.createObjectURL(new Blob([csv], { type: 'text/csv;charset=utf-8' }));
    a.download = filename.endsWith('.csv') ? filename : filename + '.csv';
    a.click(); setTimeout(() => URL.revokeObjectURL(a.href), 1000);
    UI.toast('Xuất file thành công', `Đã tải xuống <b>${U.esc(a.download)}</b> (${rows.length} dòng).`, 'success');
  };
  UI.fakeExport = async (btn, label = 'PDF') => {
    UI.btnLoading(btn, true);
    await U.sleep(1100);
    UI.btnLoading(btn, false);
    if (label === 'In') { window.print(); return; }
    UI.toast(`Đã tạo báo cáo ${label}`, 'File đang được tải xuống. Bản sao đã lưu trong mục Báo cáo.', 'success');
  };

  H.ui = UI;
})(window.HMS);
