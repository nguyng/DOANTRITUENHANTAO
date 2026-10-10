/* ==========================================================================
   MedCore HMS — Utilities & shared helpers
   Namespace toàn cục: window.HMS
   ========================================================================== */
window.HMS = window.HMS || {};

(function (H) {
  'use strict';

  /* ---------- Seeded random (mock data ổn định giữa các lần tải) ---------- */
  function mulberry32(seed) {
    return function () {
      seed |= 0; seed = (seed + 0x6D2B79F5) | 0;
      let t = Math.imul(seed ^ (seed >>> 15), 1 | seed);
      t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t;
      return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
    };
  }
  const rnd = mulberry32(20261004);
  const R = {
    next: rnd,
    int: (a, b) => Math.floor(rnd() * (b - a + 1)) + a,
    pick: (arr) => arr[Math.floor(rnd() * arr.length)],
    chance: (p) => rnd() < p,
    weighted: (pairs) => { // [[value, weight], ...]
      const total = pairs.reduce((s, p) => s + p[1], 0);
      let r = rnd() * total;
      for (const [v, w] of pairs) { if ((r -= w) <= 0) return v; }
      return pairs[pairs.length - 1][0];
    },
    shuffle: (arr) => { const a = arr.slice(); for (let i = a.length - 1; i > 0; i--) { const j = Math.floor(rnd() * (i + 1)); [a[i], a[j]] = [a[j], a[i]]; } return a; },
    series: (n, base, variance, trend = 0) => Array.from({ length: n }, (_, i) => Math.max(0, Math.round(base + trend * i + (rnd() - .5) * variance * 2)))
  };
  H.rand = R;

  /* ---------- Formatters ---------- */
  const nf = new Intl.NumberFormat('vi-VN');
  const U = {
    num: (n) => nf.format(Math.round(n)),
    money: (n) => nf.format(Math.round(n)) + ' ₫',
    moneyShort: (n) => {
      if (n >= 1e9) return (n / 1e9).toFixed(n >= 1e10 ? 0 : 2).replace('.', ',') + ' tỷ';
      if (n >= 1e6) return (n / 1e6).toFixed(n >= 1e8 ? 0 : 1).replace('.', ',') + ' tr';
      if (n >= 1e3) return Math.round(n / 1e3) + 'k';
      return String(n);
    },
    pct: (n, d = 1) => (n > 0 ? '+' : '') + n.toFixed(d).replace('.', ',') + '%',
    pad: (n, l = 2) => String(n).padStart(l, '0'),
    date: (d) => { d = new Date(d); return `${U.pad(d.getDate())}/${U.pad(d.getMonth() + 1)}/${d.getFullYear()}`; },
    dateShort: (d) => { d = new Date(d); return `${U.pad(d.getDate())}/${U.pad(d.getMonth() + 1)}`; },
    time: (d) => { d = new Date(d); return `${U.pad(d.getHours())}:${U.pad(d.getMinutes())}`; },
    dateTime: (d) => `${U.time(d)} · ${U.date(d)}`,
    weekday: (d) => ['Chủ nhật', 'Thứ 2', 'Thứ 3', 'Thứ 4', 'Thứ 5', 'Thứ 6', 'Thứ 7'][new Date(d).getDay()],
    weekdayShort: (d) => ['CN', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7'][new Date(d).getDay()],
    monthName: (m) => 'Tháng ' + (m + 1),
    longDate: (d) => { d = new Date(d); return `${U.weekday(d)}, ${d.getDate()} tháng ${d.getMonth() + 1}, ${d.getFullYear()}`; },
    ago: (d) => {
      const s = Math.round((Date.now() - new Date(d).getTime()) / 1000);
      if (s < 60) return 'Vừa xong';
      if (s < 3600) return Math.floor(s / 60) + ' phút trước';
      if (s < 86400) return Math.floor(s / 3600) + ' giờ trước';
      if (s < 86400 * 7) return Math.floor(s / 86400) + ' ngày trước';
      return U.date(d);
    },
    addDays: (d, n) => { const x = new Date(d); x.setDate(x.getDate() + n); return x; },
    addMinutes: (d, n) => new Date(new Date(d).getTime() + n * 60000),
    sameDay: (a, b) => { a = new Date(a); b = new Date(b); return a.getFullYear() === b.getFullYear() && a.getMonth() === b.getMonth() && a.getDate() === b.getDate(); },
    startOfDay: (d) => { const x = new Date(d); x.setHours(0, 0, 0, 0); return x; },
    startOfWeek: (d) => { const x = U.startOfDay(d); const day = (x.getDay() + 6) % 7; x.setDate(x.getDate() - day); return x; },
    ageFrom: (dob) => { const d = new Date(dob), n = new Date(); let a = n.getFullYear() - d.getFullYear(); if (n < new Date(n.getFullYear(), d.getMonth(), d.getDate())) a--; return a; },

    esc: (s) => String(s ?? '').replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c])),
    norm: (s) => String(s ?? '').normalize('NFD').replace(/[\u0300-\u036f]/g, '').replace(/đ/g, 'd').replace(/Đ/g, 'D').toLowerCase(),
    highlight: (text, q) => {
      if (!q) return U.esc(text);
      const t = String(text), nt = U.norm(t), nq = U.norm(q).trim();
      const i = nt.indexOf(nq);
      if (i < 0 || !nq) return U.esc(t);
      return U.esc(t.slice(0, i)) + '<mark>' + U.esc(t.slice(i, i + nq.length)) + '</mark>' + U.esc(t.slice(i + nq.length));
    },
    debounce: (fn, ms = 200) => { let t; return (...a) => { clearTimeout(t); t = setTimeout(() => fn(...a), ms); }; },
    uid: (p = 'id') => p + '-' + Math.random().toString(36).slice(2, 9),
    clamp: (n, a, b) => Math.min(b, Math.max(a, n)),
    sum: (arr, f = (x) => x) => arr.reduce((s, x) => s + f(x), 0),
    groupBy: (arr, f) => arr.reduce((m, x) => { const k = typeof f === 'function' ? f(x) : x[f]; (m[k] = m[k] || []).push(x); return m; }, {}),
    countBy: (arr, f) => arr.reduce((m, x) => { const k = typeof f === 'function' ? f(x) : x[f]; m[k] = (m[k] || 0) + 1; return m; }, {}),
    hash: (s) => { let h = 0; for (let i = 0; i < s.length; i++) h = (h * 31 + s.charCodeAt(i)) | 0; return Math.abs(h); },
    $: (sel, root = document) => root.querySelector(sel),
    $$: (sel, root = document) => Array.from(root.querySelectorAll(sel)),
    sleep: (ms) => new Promise((r) => setTimeout(r, ms))
  };
  H.u = U;

  /* ---------- Icon helper ---------- */
  H.ic = (name, cls = '') => `<i data-lucide="${name}"${cls ? ` class="${cls}"` : ''}></i>`;
  H.refreshIcons = (root) => { if (window.lucide) window.lucide.createIcons({ attrs: {}, nameAttr: 'data-lucide', root: root || document }); };

  /* ---------- Avatar ---------- */
  const AV_GRADS = [
    ['#1E6FF0', '#0FB5C9'], ['#7C5CFC', '#4F8BFA'], ['#12B886', '#0FB5C9'], ['#F59F0B', '#EF4565'],
    ['#EF4565', '#7C5CFC'], ['#0B93A6', '#12B886'], ['#3B82F6', '#7C5CFC'], ['#14B8A6', '#3B82F6'],
    ['#F97316', '#F59F0B'], ['#EC4899', '#F97316']
  ];
  H.initials = (name) => {
    const parts = String(name).replace(/^(PGS\.TS\.BS\.|TS\.BS\.|ThS\.BS\.|BSCKII\.|BSCKI\.|BS\.|ĐD\.|KTV\.|DS\.)\s*/i, '').trim().split(/\s+/);
    if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase();
    return (parts[parts.length - 2][0] + parts[parts.length - 1][0]).toUpperCase();
  };
  H.avatar = (name, opts = {}) => {
    const g = AV_GRADS[U.hash(name || '?') % AV_GRADS.length];
    const size = opts.size ? ' ' + opts.size : '';
    const sq = opts.square ? ' sq' : '';
    const pres = opts.presence ? `<span class="presence ${opts.presence}"></span>` : '';
    return `<span class="avatar${size}${sq}" style="--av:linear-gradient(135deg,${g[0]},${g[1]})" aria-hidden="true">${U.esc(H.initials(name))}${pres}</span>`;
  };

  /* ---------- Status dictionary (một nguồn duy nhất cho label + màu) ---------- */
  H.STATUS = {
    patient: {
      treatment: { label: 'Đang điều trị', c: 'primary' },
      waiting: { label: 'Chờ khám', c: 'warning' },
      observation: { label: 'Theo dõi', c: 'purple' },
      emergency: { label: 'Cấp cứu', c: 'danger', pulse: true },
      discharged: { label: 'Đã xuất viện', c: 'success' }
    },
    appt: {
      confirmed: { label: 'Đã xác nhận', c: 'primary' },
      pending: { label: 'Chờ xác nhận', c: 'warning' },
      completed: { label: 'Hoàn thành', c: 'success' },
      cancelled: { label: 'Đã hủy', c: 'slate' },
      emergency: { label: 'Khẩn cấp', c: 'danger', pulse: true }
    },
    bed: {
      available: { label: 'Trống', c: 'success' },
      occupied: { label: 'Đang sử dụng', c: 'primary' },
      reserved: { label: 'Đã đặt trước', c: 'warning' },
      maintenance: { label: 'Bảo trì', c: 'slate' }
    },
    lab: {
      waiting: { label: 'Chờ xử lý', c: 'warning' },
      processing: { label: 'Đang xử lý', c: 'info', pulse: true },
      completed: { label: 'Hoàn thành', c: 'success' },
      critical: { label: 'Nguy hiểm', c: 'danger', pulse: true }
    },
    priority: {
      routine: { label: 'Thường', c: 'slate' },
      urgent: { label: 'Khẩn', c: 'warning' },
      stat: { label: 'Cấp cứu', c: 'danger' }
    },
    stock: {
      ok: { label: 'Còn hàng', c: 'success' },
      low: { label: 'Sắp hết', c: 'warning' },
      out: { label: 'Hết hàng', c: 'danger' },
      expiring: { label: 'Sắp hết hạn', c: 'purple' }
    },
    invoice: {
      paid: { label: 'Đã thanh toán', c: 'success' },
      pending: { label: 'Chờ thanh toán', c: 'warning' },
      overdue: { label: 'Quá hạn', c: 'danger' },
      refunded: { label: 'Đã hoàn tiền', c: 'slate' }
    },
    claim: {
      approved: { label: 'Đã duyệt', c: 'success' },
      pending: { label: 'Đang thẩm định', c: 'warning' },
      rejected: { label: 'Từ chối', c: 'danger' },
      submitted: { label: 'Đã gửi', c: 'info' }
    },
    equipment: {
      active: { label: 'Đang hoạt động', c: 'success' },
      ready: { label: 'Sẵn sàng', c: 'primary' },
      maintenance: { label: 'Đang bảo trì', c: 'warning' },
      broken: { label: 'Hỏng', c: 'danger' }
    },
    rx: {
      dispensed: { label: 'Đã cấp phát', c: 'success' },
      pending: { label: 'Chờ cấp phát', c: 'warning' },
      cancelled: { label: 'Đã hủy', c: 'slate' }
    },
    imaging: {
      scheduled: { label: 'Đã lên lịch', c: 'slate' },
      acquired: { label: 'Đã chụp', c: 'info' },
      reported: { label: 'Đã đọc kết quả', c: 'success' },
      urgent: { label: 'Cần đọc gấp', c: 'danger', pulse: true }
    },
    staff: {
      onduty: { label: 'Đang trực', c: 'success' },
      off: { label: 'Ngoài ca', c: 'slate' },
      leave: { label: 'Nghỉ phép', c: 'warning' }
    },
    doctor: {
      online: { label: 'Online', c: 'success' },
      busy: { label: 'Đang khám', c: 'warning' },
      offline: { label: 'Offline', c: 'slate' }
    },
    audit: {
      success: { label: 'Thành công', c: 'success' },
      failed: { label: 'Thất bại', c: 'danger' },
      warning: { label: 'Cảnh báo', c: 'warning' }
    },
    user: {
      active: { label: 'Hoạt động', c: 'success' },
      locked: { label: 'Đã khóa', c: 'danger' },
      invited: { label: 'Chờ kích hoạt', c: 'warning' }
    },
    esi: {
      1: { label: 'ESI 1 · Hồi sức', c: 'danger', pulse: true },
      2: { label: 'ESI 2 · Nguy kịch', c: 'danger' },
      3: { label: 'ESI 3 · Khẩn cấp', c: 'warning' },
      4: { label: 'ESI 4 · Ít khẩn', c: 'info' },
      5: { label: 'ESI 5 · Không khẩn', c: 'success' }
    }
  };
  H.badge = (group, key, opts = {}) => {
    const s = (H.STATUS[group] || {})[key] || { label: key, c: 'slate' };
    const cls = ['badge', 'c-' + s.c, opts.noDot ? '' : 'dot', s.pulse ? 'pulse' : '', opts.sm ? 'sm' : '', opts.solid ? 'solid' : ''].filter(Boolean).join(' ');
    return `<span class="${cls}">${U.esc(opts.label || s.label)}</span>`;
  };
  H.statusColor = (group, key) => ((H.STATUS[group] || {})[key] || { c: 'slate' }).c;

  H.trend = (v) => {
    const cls = v > 0 ? 'up' : v < 0 ? 'down' : 'flat';
    const icon = v > 0 ? 'trending-up' : v < 0 ? 'trending-down' : 'minus';
    return `<span class="trend ${cls}">${H.ic(icon)}${U.pct(v)}</span>`;
  };
  H.stars = (rating) => {
    let s = '<span class="stars" aria-label="' + rating + ' sao">';
    for (let i = 1; i <= 5; i++) s += `<svg viewBox="0 0 24 24" class="${rating >= i - .3 ? '' : 'off'}"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01z"/></svg>`;
    return s + '</span>';
  };
  H.copyId = (id) => `<button type="button" class="copy-id" data-copy="${U.esc(id)}" title="Sao chép mã">${U.esc(id)}${H.ic('copy')}</button>`;
  H.meter = (pct, color) => `<div class="meter"><span style="width:${U.clamp(pct, 0, 100)}%;${color ? '--c:' + color : ''}"></span></div>`;

  /* ---------- Small css-var color resolver for charts ---------- */
  H.cssVar = (name) => getComputedStyle(document.documentElement).getPropertyValue(name).trim();
  H.COLORS = {
    primary: '#1E6FF0', accent: '#0FB5C9', success: '#12B886', warning: '#F59F0B',
    danger: '#EF4565', info: '#3B82F6', purple: '#7C5CFC', slate: '#94A3B8',
    palette: ['#1E6FF0', '#0FB5C9', '#7C5CFC', '#12B886', '#F59F0B', '#EF4565', '#3B82F6', '#14B8A6', '#F97316', '#EC4899', '#64748B', '#8B5CF6']
  };
})(window.HMS);
