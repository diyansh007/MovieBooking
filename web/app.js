const $ = selector => document.querySelector(selector);
let token = localStorage.getItem('movieBookingToken');
const page = document.body.dataset.page;

async function request(url, options = {}) {
  const response = await fetch(url, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { 'X-Session-Token': token } : {}),
      ...(options.headers || {})
    }
  });
  const data = await response.json();
  if (response.status === 401) {
    localStorage.removeItem('movieBookingToken');
    token = null;
    if (page !== 'login') {
      location.href = 'login.html?required=1';
    }
  }
  if (!response.ok) throw new Error(data.error || 'Something went wrong.');
  return data;
}

function requireLogin() {
  if (!token) {
    location.href = 'login.html?required=1';
    return false;
  }
  return true;
}

function error(message) {
  const el = $('#formError');
  if (el) el.textContent = message;
}

function formatCurrency(value) {
  return `₹ ${(Number(value) || 0).toFixed(2)}`;
}

async function refreshPricing(form) {
  const title = form.title.value;
  const format = form.format.value;
  const seats = Number(form.seats.value || 1);
  const strategy = form.strategy.value;
  try {
    const data = await request('/api/pricing', {
      method: 'POST',
      body: JSON.stringify({ title, format, seats, strategy })
    });
    $('#bookingBase').textContent = formatCurrency(data.base);
    $('#bookingAdjustment').textContent = formatCurrency(data.adjustment);
    $('#bookingPrice').textContent = formatCurrency(data.amount);
  } catch (err) {
    $('#bookingPrice').textContent = formatCurrency(0);
    error(err.message);
  }
}

if (page === 'home') $('#sessionName').textContent = token ? 'Signed in' : 'Guest';

if (page === 'login') {
  if (token) location.href = 'index.html';
  $('#loginForm').onsubmit = async event => {
    event.preventDefault();
    error('');
    try {
      const data = await request('/api/login', { method: 'POST', body: JSON.stringify(Object.fromEntries(new FormData(event.target))) });
      token = data.token;
      localStorage.setItem('movieBookingToken', token);
      location.href = 'index.html';
    } catch (err) {
      error(err.message);
    }
  };
  if (location.search.includes('required')) error('Please login first to continue.');
}

if (page === 'book' && requireLogin()) {
  const form = $('#bookingForm');
  const savedDraft = localStorage.getItem('movieBookingDraft');
  if (savedDraft) {
    try {
      const draft = JSON.parse(savedDraft);
      form.title.value = draft.movie || form.title.value;
      form.format.value = draft.format || form.format.value;
      form.seats.value = draft.seats || 1;
      form.strategy.value = draft.strategy || 'standard';
      const notice = $('#draftNotice');
      notice.textContent = `Book Again draft from booking #${draft.sourceBookingId} is ready. Review or change it before confirming.`;
      notice.hidden = false;
      localStorage.removeItem('movieBookingDraft');
    } catch (err) {
      localStorage.removeItem('movieBookingDraft');
      error('The saved booking draft could not be loaded.');
    }
  }
  form.oninput = () => refreshPricing(form);
  form.onchange = () => refreshPricing(form);
  form.onsubmit = async event => {
    event.preventDefault();
    error('');
    try {
      const payload = Object.fromEntries(new FormData(form));
      const data = await request('/api/bookings', { method: 'POST', body: JSON.stringify(payload) });
      alert(data.message || 'Booking created.');
      location.href = 'bookings.html';
    } catch (err) {
      error(err.message);
    }
  };
  $('#saveDraftButton').onclick = async () => {
    try {
      const payload = Object.fromEntries(new FormData(form));
      const data = await request('/api/drafts/save', { method: 'POST', body: JSON.stringify(payload) });
      alert('Draft saved for this session.');
      console.log(data);
    } catch (err) {
      error(err.message);
    }
  };
  $('#undoDraftButton').onclick = async () => {
    try {
      const data = await request('/api/drafts/undo', { method: 'POST' });
      if (data.movie) {
        form.title.value = data.movie;
        form.format.value = data.format || '2D';
        form.seats.value = data.seats || 1;
        form.strategy.value = data.strategy || 'standard';
        refreshPricing(form);
        alert('Last draft state restored.');
      } else {
        alert(data.error || 'No draft to undo.');
      }
    } catch (err) {
      error(err.message);
    }
  };
  refreshPricing(form);
}

if (page === 'bookings' && requireLogin()) {
  request('/api/bookings').then(data => {
    $('#bookingList').innerHTML = data.bookings.length
      ? `<div class="table-head"><span>ID</span><span>Movie</span><span>Format</span><span>Seats</span><span>Amount</span><span>Strategy</span><span>Status</span><span>Action</span></div>${data.bookings.map(b => `<article class="table-row"><span>#${b.id}</span><span>${b.title}</span><span>${b.format}</span><span>${b.seats}</span><span>₹ ${Number(b.amount).toFixed(2)}</span><span>${b.strategy || 'standard'}</span><span class="status ${b.status.toLowerCase()}">${b.status}</span><span><button class="small-button" data-book-again="${b.id}">Book Again</button></span></article>`).join('')}`
      : '<p class="empty">No booking history found.</p>';
    document.querySelectorAll('[data-book-again]').forEach(button => {
      button.onclick = async () => {
        try {
          const draft = await request(`/api/bookings/${button.dataset.bookAgain}/draft`, { method: 'POST' });
          localStorage.setItem('movieBookingDraft', JSON.stringify(draft));
          location.href = 'book.html';
        } catch (err) {
          alert(err.message);
        }
      };
    });
  }).catch(err => {
    $('#bookingList').innerHTML = `<p class="form-error">${err.message}</p>`;
  });
}

if (page === 'cancel' && requireLogin()) $('#cancelForm').onsubmit = async event => { event.preventDefault(); error(''); const id = new FormData(event.target).get('bookingId'); try { const data = await request(`/api/bookings/${id}/cancel`, { method: 'POST' }); alert(data.message); location.href = 'bookings.html'; } catch (err) { error(err.message); } };

if (page === 'exit') $('#exitButton').onclick = async () => { if (token) { try { await request('/api/logout', { method: 'POST' }); } catch (_) {} } localStorage.removeItem('movieBookingToken'); token = null; $('#exitMessage').textContent = 'You have been signed out successfully. Thank you for using Movie Booking.'; };

if (page === 'terminal' && requireLogin()) { const output = $('#terminalOutput'); const input = $('#terminalInput'); const form = $('#terminalForm'); const start = $('#startTerminal'); let timer; const show = data => { output.textContent = data.output; output.scrollTop = output.scrollHeight; input.disabled = !data.running; form.querySelector('button').disabled = !data.running; if (!data.running) clearInterval(timer); }; const refresh = async () => { try { show(await request('/api/terminal/output')); } catch (_) {} }; start.onclick = async () => { clearInterval(timer); start.disabled = true; start.textContent = 'Starting…'; try { show(await request('/api/terminal/start', { method: 'POST' })); timer = setInterval(refresh, 500); start.textContent = 'Restart terminal'; start.disabled = false; } catch (err) { output.textContent = err.message; start.disabled = false; start.textContent = 'Start terminal'; } }; form.onsubmit = async event => { event.preventDefault(); if (!input.value.trim()) return; const value = input.value; input.value = ''; try { show(await request('/api/terminal/input', { method: 'POST', body: JSON.stringify({ input: value }) })); } catch (err) { output.textContent += `\n${err.message}`; } }; }
