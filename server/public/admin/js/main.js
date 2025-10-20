const gatewayTableBody = document.getElementById('gatewayTableBody');
const emptyState = document.getElementById('emptyState');
const modal = document.getElementById('modal');
const modalTitle = document.getElementById('modalTitle');
const modalBody = document.getElementById('modalBody');
const toast = document.getElementById('toast');
const configFormTemplate = document.getElementById('configFormTemplate');

const ADMIN_API_BASE = '/admin/payment-gateways';

async function fetchGateways() {
  const response = await fetch(ADMIN_API_BASE);
  if (!response.ok) {
    throw new Error('Failed to load gateways');
  }
  return response.json();
}

function renderGateways(gateways) {
  gatewayTableBody.innerHTML = '';
  if (!gateways.length) {
    emptyState.hidden = false;
    return;
  }
  emptyState.hidden = true;
  const isMobile = matchMedia('(max-width: 720px)').matches;

  gateways.forEach((gateway) => {
    if (isMobile) {
      gatewayTableBody.appendChild(renderGatewayCard(gateway));
    } else {
      gatewayTableBody.appendChild(renderGatewayRow(gateway));
    }
  });
}

function renderGatewayRow(gateway) {
  const tr = document.createElement('tr');
  tr.innerHTML = `
    <td>
      <div class="gateway-name">
        <strong>${gateway.displayName}</strong>
        <div class="gateway-meta">${gateway.supportedActions.join(', ')}</div>
      </div>
    </td>
    <td>
      <span class="status-pill" data-enabled="${gateway.enabled}">
        ${gateway.enabled ? 'Enabled' : 'Disabled'}
      </span>
    </td>
    <td>
      <div class="action-group">
        <button class="btn" data-action="toggle">${gateway.enabled ? 'Disable' : 'Enable'}</button>
        <button class="btn" data-action="configure">Configure</button>
        <button class="btn" data-action="test">Test Connection</button>
      </div>
    </td>
  `;

  tr.querySelector('[data-action="toggle"]').addEventListener('click', () => toggleGateway(gateway));
  tr.querySelector('[data-action="configure"]').addEventListener('click', () => openConfigModal(gateway));
  tr.querySelector('[data-action="test"]').addEventListener('click', () => testGateway(gateway));

  return tr;
}

function renderGatewayCard(gateway) {
  const wrapper = document.createElement('div');
  wrapper.className = 'gateway-card';
  wrapper.innerHTML = `
    <header>
      <div>
        <strong>${gateway.displayName}</strong>
        <div class="gateway-meta">${gateway.supportedActions.join(', ')}</div>
      </div>
      <span class="status-pill" data-enabled="${gateway.enabled}">
        ${gateway.enabled ? 'Enabled' : 'Disabled'}
      </span>
    </header>
    <div class="action-group">
      <button class="btn" data-action="toggle">${gateway.enabled ? 'Disable' : 'Enable'}</button>
      <button class="btn" data-action="configure">Configure</button>
      <button class="btn" data-action="test">Test Connection</button>
    </div>
  `;

  wrapper.querySelector('[data-action="toggle"]').addEventListener('click', () => toggleGateway(gateway));
  wrapper.querySelector('[data-action="configure"]').addEventListener('click', () => openConfigModal(gateway));
  wrapper.querySelector('[data-action="test"]').addEventListener('click', () => testGateway(gateway));

  return wrapper;
}

async function toggleGateway(gateway) {
  try {
    const response = await fetch(`${ADMIN_API_BASE}/${gateway.id}/toggle`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ enabled: !gateway.enabled })
    });
    if (!response.ok) {
      throw new Error('Unable to toggle gateway');
    }
    showToast(`${gateway.displayName} ${gateway.enabled ? 'disabled' : 'enabled'} successfully`);
    await refresh();
  } catch (error) {
    showToast(error.message, 'error');
  }
}

function openConfigModal(gateway) {
  modalTitle.textContent = `Configure ${gateway.displayName}`;
  const formFragment = configFormTemplate.content.cloneNode(true);
  const form = formFragment.querySelector('form');
  const fieldsWrapper = form.querySelector('.form-fields');

  if (!gateway.configFields.length) {
    const notice = document.createElement('p');
    notice.textContent = 'This gateway does not expose configurable fields.';
    fieldsWrapper.appendChild(notice);
  } else {
    gateway.configFields.forEach((field) => {
      const label = document.createElement('label');
      label.innerHTML = `
        <span>${field.label}</span>
        <input type="${field.secret ? 'password' : 'text'}" name="${field.key}" placeholder="${field.secret ? '********' : ''}" value="" autocomplete="off" />
      `;
      if (gateway.config && gateway.config[field.key]) {
        label.querySelector('input').placeholder = gateway.config[field.key];
      }
      fieldsWrapper.appendChild(label);
    });
  }

  form.addEventListener('submit', (event) => {
    event.preventDefault();
    const data = new FormData(form);
    const configPayload = {};
    for (const [key, value] of data.entries()) {
      if (value) {
        configPayload[key] = value;
      }
    }
    saveGatewayConfig(gateway, configPayload);
  });

  form.querySelector('[data-action="cancel"]').addEventListener('click', closeModal);

  modalBody.innerHTML = '';
  modalBody.appendChild(formFragment);
  openModal();
}

function openModal() {
  modal.hidden = false;
}

function closeModal() {
  modal.hidden = true;
  modalBody.innerHTML = '';
}

modal.addEventListener('click', (event) => {
  if (event.target.dataset.action === 'close' || event.target === modal) {
    closeModal();
  }
});

async function saveGatewayConfig(gateway, config) {
  try {
    const response = await fetch(`${ADMIN_API_BASE}/${gateway.id}/config`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ config })
    });
    if (!response.ok) {
      throw new Error('Failed to save configuration');
    }
    closeModal();
    showToast('Configuration saved successfully');
    await refresh();
  } catch (error) {
    showToast(error.message, 'error');
  }
}

async function testGateway(gateway) {
  try {
    const response = await fetch(`${ADMIN_API_BASE}/${gateway.id}/test`, {
      method: 'POST'
    });
    if (!response.ok) {
      const payload = await response.json().catch(() => ({}));
      throw new Error(payload.message || 'Gateway test failed');
    }
    showToast('Connection test completed successfully');
  } catch (error) {
    showToast(error.message, 'error');
  }
}

function showToast(message, variant = 'default') {
  toast.textContent = message;
  toast.dataset.variant = variant;
  toast.hidden = false;
  clearTimeout(showToast.timeoutId);
  showToast.timeoutId = setTimeout(() => {
    toast.hidden = true;
  }, 4000);
}

async function refresh() {
  const { gateways } = await fetchGateways();
  renderGateways(gateways);
}

refresh().catch((error) => {
  showToast(error.message, 'error');
});
