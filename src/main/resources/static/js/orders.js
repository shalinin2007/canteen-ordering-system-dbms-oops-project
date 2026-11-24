/**
 * Campus Canteen Online Ordering System - Orders Page Logic
 * Handles order history fetching, active order live status, and 30-sec polling.
 */

let ordersRefreshTimer = null;

document.addEventListener('DOMContentLoaded', async () => {
  // Check student auth
  await checkAuth();

  // Initial load
  await loadMyOrders();
  await updateCartCountBadge();

  // Auto-refresh every 30 seconds for live counter updates
  ordersRefreshTimer = setInterval(() => {
    loadMyOrders(true); // background silent refresh
  }, 30000);
});

window.addEventListener('beforeunload', () => {
  if (ordersRefreshTimer) {
    clearInterval(ordersRefreshTimer);
  }
});

/**
 * Load student orders from GET /api/orders/my
 */
async function loadMyOrders(isSilent = false) {
  const container = document.getElementById('ordersFeedContainer');
  const bannerContainer = document.getElementById('activeOrderLiveBanner');

  try {
    const orders = await apiCall('/api/orders/my');
    const orderList = Array.isArray(orders) ? orders : [];

    if (orderList.length === 0) {
      if (bannerContainer) bannerContainer.style.display = 'none';
      container.innerHTML = `
        <div class="empty-state">
          <div class="empty-state-icon">🧾</div>
          <h3 class="empty-state-title">No Orders Placed Yet</h3>
          <p class="empty-state-text">Your hot meal or beverage is just a tap away. Order from our fresh menu!</p>
          <a href="menu.html" class="btn-action-primary">
            <span>Browse Today's Menu</span> ➔
          </a>
        </div>
      `;
      return;
    }

    // Sort by latest order first if not already sorted
    orderList.sort((a, b) => new Date(b.createdAt || b.orderDate || 0) - new Date(a.createdAt || a.orderDate || 0));

    // Check for active orders (PENDING, CONFIRMED, PREPARING, READY)
    renderActiveOrderBanner(orderList);

    // Render order list
    container.innerHTML = orderList.map(order => {
      const orderId = order.id || order.orderId;
      const token = order.pickupToken || order.tokenNumber || (orderId % 100);
      const status = (order.status || 'PENDING').toUpperCase();
      const statusClass = getStatusBadgeClass(status);
      const total = order.totalAmount || order.total || 0;
      const dateStr = formatDate(order.createdAt || order.orderDate || new Date().toISOString());

      // Parse items
      const items = Array.isArray(order.items) ? order.items : [];

      const itemsHtml = items.map(it => {
        const itemName = it.foodItemName || it.name || (it.foodItem && it.foodItem.name) || 'Item';
        const qty = it.quantity || 1;
        const itemPrice = it.price || (it.subtotal ? it.subtotal / qty : 0);
        return `
          <div class="order-item-row">
            <span class="order-item-name">${itemName} <span class="order-item-qty">×${qty}</span></span>
            <span class="order-item-price">${formatCurrency(itemPrice * qty)}</span>
          </div>
        `;
      }).join('');

      return `
        <div class="order-card" id="order-${orderId}">
          <div class="order-card-header">
            <div class="order-id-token">
              <span class="token-pill">Token #${token}</span>
              <span class="order-id-label">#${orderId}</span>
            </div>
            <span class="order-date">${dateStr}</span>
          </div>

          <div class="order-items-snippet">
            ${itemsHtml || '<span style="color: var(--text-subtle); font-size: 0.82rem;">Items details loading...</span>'}
          </div>

          <div class="order-card-footer">
            <div class="order-total-val">${formatCurrency(total)}</div>
            <div class="status-badge ${statusClass}">
              <span>${getStatusIcon(status)}</span>
              <span>${status}</span>
            </div>
          </div>
        </div>
      `;
    }).join('');

  } catch (err) {
    if (!isSilent) {
      console.error('Failed to load orders:', err);
      container.innerHTML = `
        <div class="empty-state">
          <div class="empty-state-icon">⚠️</div>
          <h3 class="empty-state-title">Unable to Fetch Orders</h3>
          <p class="empty-state-text">${err.message || 'Please check your connection and retry.'}</p>
          <button class="btn-action-primary" onclick="loadMyOrders()">Retry</button>
        </div>
      `;
    }
  }
}

/**
 * Display active order banner if student has orders in progress
 */
function renderActiveOrderBanner(orders) {
  const banner = document.getElementById('activeOrderLiveBanner');
  if (!banner) return;

  const active = orders.find(o => {
    const s = (o.status || '').toUpperCase();
    return s === 'PREPARING' || s === 'CONFIRMED' || s === 'READY' || s === 'PENDING';
  });

  if (!active) {
    banner.style.display = 'none';
    return;
  }

  const s = (active.status || '').toUpperCase();
  const token = active.pickupToken || active.tokenNumber || (active.id % 100);

  let bannerText = '';
  if (s === 'READY') {
    bannerText = `
      <div class="active-order-banner" style="background: #F0FDF4; border-color: #86EFAC;">
        <span style="font-size: 1.5rem;">🔔</span>
        <div class="active-banner-text">
          <div class="active-banner-title" style="color: #166534;">Token #${token} is READY for Pickup!</div>
          <div class="active-banner-sub" style="color: #15803D;">Please head to the canteen counter now.</div>
        </div>
      </div>
    `;
  } else if (s === 'PREPARING') {
    bannerText = `
      <div class="active-order-banner">
        <div class="live-pulse-indicator"></div>
        <div class="active-banner-text">
          <div class="active-banner-title">⏳ Token #${token} is Being Prepared...</div>
          <div class="active-banner-sub">Chefs are preparing your fresh order right now.</div>
        </div>
      </div>
    `;
  } else {
    bannerText = `
      <div class="active-order-banner" style="background: #EFF6FF; border-color: #93C5FD;">
        <span style="font-size: 1.4rem;">🧾</span>
        <div class="active-banner-text">
          <div class="active-banner-title" style="color: #1E40AF;">Token #${token}: Order ${s}</div>
          <div class="active-banner-sub" style="color: #2563EB;">Your order will be queued for preparation shortly.</div>
        </div>
      </div>
    `;
  }

  banner.innerHTML = bannerText;
  banner.style.display = 'block';
}

function getStatusIcon(status) {
  switch (status) {
    case 'PENDING': return '⏳';
    case 'CONFIRMED': return '👍';
    case 'PREPARING': return '🍳';
    case 'READY': return '🔔';
    case 'DELIVERED': return '✅';
    case 'CANCELLED': return '❌';
    default: return '⏳';
  }
}
