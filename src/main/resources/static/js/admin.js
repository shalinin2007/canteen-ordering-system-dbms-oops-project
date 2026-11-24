/**
 * Campus Canteen Online Ordering System - Admin Operations Logic
 * Handles dashboard statistics, live kitchen order workflow, status updates, and menu CRUD.
 */

let adminRefreshTimer = null;
let currentMenuItems = [];

/**
 * Mobile Sidebar Drawer Toggle
 */
function toggleAdminSidebar() {
  const sidebar = document.getElementById('adminSidebar');
  if (sidebar) {
    sidebar.classList.toggle('open');
  }
}

// Close sidebar when clicking outside on mobile
document.addEventListener('click', (e) => {
  const sidebar = document.getElementById('adminSidebar');
  const toggleBtn = document.querySelector('.btn-sidebar-toggle');
  if (sidebar && sidebar.classList.contains('open')) {
    if (!sidebar.contains(e.target) && (!toggleBtn || !toggleBtn.contains(e.target))) {
      sidebar.classList.remove('open');
    }
  }
});

/**
 * Initialize Admin Dashboard Page
 */
async function initDashboardPage() {
  // Check admin role
  await checkAdminAuth();

  // Load data immediately
  await loadAdminDashboardData();

  // Start 20s auto-refresh
  adminRefreshTimer = setInterval(() => {
    loadAdminDashboardData(true);
  }, 20000);
}

window.addEventListener('beforeunload', () => {
  if (adminRefreshTimer) {
    clearInterval(adminRefreshTimer);
  }
});

/**
 * Load both dashboard stats and live orders
 */
async function loadAdminDashboardData(isSilent = false) {
  try {
    await Promise.all([
      fetchDashboardStats(),
      fetchAdminOrders()
    ]);
  } catch (err) {
    if (!isSilent) {
      console.error('Error refreshing admin dashboard:', err);
    }
  }
}

/**
 * Fetch and render stat cards from GET /api/admin/dashboard
 */
async function fetchDashboardStats() {
  try {
    const stats = await apiCall('/api/admin/dashboard');
    if (stats) {
      const todayOrdersEl = document.getElementById('statTodayOrders');
      const todayRevenueEl = document.getElementById('statTodayRevenue');
      const pendingOrdersEl = document.getElementById('statPendingOrders');
      const totalItemsEl = document.getElementById('statTotalItems');

      if (todayOrdersEl) todayOrdersEl.textContent = stats.todayOrders || 0;
      if (todayRevenueEl) todayRevenueEl.textContent = formatCurrency(stats.todayRevenue || 0);
      if (pendingOrdersEl) pendingOrdersEl.textContent = stats.pendingOrders || 0;
      if (totalItemsEl) totalItemsEl.textContent = stats.totalItems || 0;
    }
  } catch (err) {
    console.warn('Could not fetch stats from /api/admin/dashboard:', err);
  }
}

/**
 * Fetch all orders from GET /api/admin/orders and render table
 */
async function fetchAdminOrders() {
  const tbody = document.getElementById('adminOrdersTbody');
  const countEl = document.getElementById('adminOrderCount');
  if (!tbody) return;

  try {
    const orders = await apiCall('/api/admin/orders');
    const orderList = Array.isArray(orders) ? orders : [];

    if (countEl) countEl.textContent = orderList.length;

    if (orderList.length === 0) {
      tbody.innerHTML = `
        <tr>
          <td colspan="6" style="text-align: center; padding: 30px; color: var(--admin-text-muted);">
            No orders received yet today.
          </td>
        </tr>
      `;
      return;
    }

    // Sort latest first
    orderList.sort((a, b) => new Date(b.createdAt || b.orderDate || 0) - new Date(a.createdAt || a.orderDate || 0));

    tbody.innerHTML = orderList.map(order => {
      const orderId = order.id || order.orderId;
      const token = order.pickupToken || order.tokenNumber || (orderId % 100);
      const studentName = (order.user && order.user.name) || order.userName || order.studentName || 'Student';
      const studentInfo = (order.user && (order.user.collegeId || order.user.phone)) || order.studentPhone || '';
      const total = order.totalAmount || order.total || 0;
      const status = (order.status || 'PENDING').toUpperCase();

      // Parse items
      const items = Array.isArray(order.items) ? order.items : [];
      const itemsSummary = items.map(it => {
        const name = it.foodItemName || it.name || (it.foodItem && it.foodItem.name) || 'Item';
        const qty = it.quantity || 1;
        return `${name} × ${qty}`;
      }).join(', ') || 'No item details';

      return `
        <tr id="admin-order-row-${orderId}">
          <td>
            <div style="font-weight: 800; color: #FFFFFF;">Token #${token}</div>
            <div style="font-size: 0.75rem; color: var(--admin-text-muted);">Order #${orderId}</div>
          </td>
          <td>
            <div style="font-weight: 700; color: #FFFFFF;">${studentName}</div>
            <div style="font-size: 0.74rem; color: var(--admin-text-muted);">${studentInfo}</div>
          </td>
          <td>
            <div style="max-width: 280px; font-size: 0.82rem; line-height: 1.4; color: var(--admin-text);">
              ${itemsSummary}
            </div>
          </td>
          <td>
            <strong style="color: var(--admin-primary); font-family: 'Outfit', sans-serif;">
              ${formatCurrency(total)}
            </strong>
          </td>
          <td>
            <span class="status-pill ${status}">
              ${status}
            </span>
          </td>
          <td>
            <select class="select-status-action" onchange="updateOrderStatus(${orderId}, this.value)">
              <option value="CONFIRMED" ${status === 'CONFIRMED' ? 'selected' : ''}>CONFIRMED</option>
              <option value="PREPARING" ${status === 'PREPARING' ? 'selected' : ''}>PREPARING</option>
              <option value="READY" ${status === 'READY' ? 'selected' : ''}>READY FOR PICKUP</option>
              <option value="DELIVERED" ${status === 'DELIVERED' ? 'selected' : ''}>DELIVERED</option>
              <option value="CANCELLED" ${status === 'CANCELLED' ? 'selected' : ''}>CANCELLED</option>
            </select>
          </td>
        </tr>
      `;
    }).join('');

  } catch (err) {
    console.error('Failed to fetch admin orders:', err);
    tbody.innerHTML = `
      <tr>
        <td colspan="6" style="text-align: center; padding: 24px; color: var(--admin-rose);">
          Error loading orders: ${err.message || 'Server error'}
        </td>
      </tr>
    `;
  }
}

/**
 * Update order status via PUT /api/admin/orders/{id}/status
 */
async function updateOrderStatus(orderId, newStatus) {
  try {
    await apiCall(`/api/admin/orders/${orderId}/status`, 'PUT', { status: newStatus });
    showToast(`Order #${orderId} marked as ${newStatus} ✅`, 'success');
    
    // Refresh stats and orders
    await loadAdminDashboardData(true);
  } catch (err) {
    showToast(`Failed to update status: ${err.message}`, 'error');
  }
}

/* ==========================================================================
   Admin Menu Management Logic (admin/menu.html)
   ========================================================================== */

/**
 * Initialize Admin Menu Page
 */
async function initMenuPage() {
  await checkAdminAuth();
  await loadAdminMenuItems();
}

/**
 * Fetch all menu items from GET /api/admin/menu
 */
async function loadAdminMenuItems() {
  const tbody = document.getElementById('adminMenuTbody');
  const countEl = document.getElementById('adminMenuTotalCount');
  if (!tbody) return;

  try {
    const items = await apiCall('/api/admin/menu');
    currentMenuItems = Array.isArray(items) ? items : [];

    if (countEl) countEl.textContent = currentMenuItems.length;

    if (currentMenuItems.length === 0) {
      tbody.innerHTML = `
        <tr>
          <td colspan="7" style="text-align: center; padding: 36px; color: var(--admin-text-muted);">
            No menu items found. Click "+ Add Food Item" to create one.
          </td>
        </tr>
      `;
      return;
    }

    tbody.innerHTML = currentMenuItems.map(item => {
      const emoji = getFoodEmoji(item.name, item.itemType);
      const type = (item.itemType || 'VEG').toUpperCase();
      const isAvailable = item.available !== false;

      let typeBadge = `<span style="color: #22C55E; font-weight: 700;">🟢 Veg</span>`;
      if (type === 'NON_VEG' || type === 'NON-VEG') {
        typeBadge = `<span style="color: #EF4444; font-weight: 700;">🔴 Non-Veg</span>`;
      } else if (type === 'BEVERAGE') {
        typeBadge = `<span style="color: #0EA5E9; font-weight: 700;">🔵 Beverage</span>`;
      }

      return `
        <tr id="admin-menu-item-${item.id}">
          <td>
            <div style="display: flex; align-items: center; gap: 10px;">
              <span style="font-size: 1.5rem;">${emoji}</span>
              <div>
                <strong style="color: #FFFFFF;">${item.name}</strong>
                <div style="font-size: 0.74rem; color: var(--admin-text-muted); max-width: 220px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">
                  ${item.description || 'No description'}
                </div>
              </div>
            </div>
          </td>
          <td>${item.categoryName || 'General'}</td>
          <td>${typeBadge}</td>
          <td><strong>${formatCurrency(item.price)}</strong></td>
          <td>${item.preparationTime ? `${item.preparationTime}m` : '10m'}</td>
          <td>
            <button onclick="toggleItemAvailability(${item.id}, ${!isAvailable})" 
                    style="padding: 4px 10px; border-radius: 99px; font-size: 0.72rem; font-weight: 800; cursor: pointer; ${isAvailable ? 'background: rgba(16, 185, 129, 0.15); color: #34D399;' : 'background: rgba(239, 68, 68, 0.15); color: #F87171;'}">
              ${isAvailable ? 'In Stock' : 'Sold Out'}
            </button>
          </td>
          <td>
            <button class="btn-action-edit" onclick="openEditMenuItemModal(${item.id})">Edit</button>
            <button class="btn-action-delete" onclick="deleteMenuItem(${item.id})">Delete</button>
          </td>
        </tr>
      `;
    }).join('');

  } catch (err) {
    console.error('Failed to load admin menu:', err);
    tbody.innerHTML = `
      <tr>
        <td colspan="7" style="text-align: center; padding: 24px; color: var(--admin-rose);">
          Error loading menu: ${err.message || 'Server error'}
        </td>
      </tr>
    `;
  }
}

/**
 * Toggle availability of a food item
 */
async function toggleItemAvailability(itemId, newAvailableStatus) {
  try {
    const item = currentMenuItems.find(i => i.id === itemId);
    if (!item) return;

    const payload = {
      ...item,
      available: newAvailableStatus
    };

    await apiCall(`/api/admin/menu/${itemId}`, 'PUT', payload);
    showToast(`Updated item availability`, 'success');
    await loadAdminMenuItems();
  } catch (err) {
    showToast(`Failed to update status: ${err.message}`, 'error');
  }
}

/**
 * Open Modal to Add New Food Item
 */
function openAddMenuItemModal() {
  document.getElementById('itemEditId').value = '';
  document.getElementById('menuModalTitle').textContent = 'Add New Food Item';
  document.getElementById('itemName').value = '';
  document.getElementById('itemDescription').value = '';
  document.getElementById('itemPrice').value = '';
  document.getElementById('itemPrepTime').value = '10';
  document.getElementById('itemCategory').value = 'Breakfast';
  document.getElementById('itemType').value = 'VEG';
  document.getElementById('itemAvailable').checked = true;

  const overlay = document.getElementById('menuModalOverlay');
  if (overlay) overlay.classList.add('active');
}

/**
 * Open Modal to Edit Existing Food Item
 */
function openEditMenuItemModal(itemId) {
  const item = currentMenuItems.find(i => i.id === itemId);
  if (!item) return;

  document.getElementById('itemEditId').value = item.id;
  document.getElementById('menuModalTitle').textContent = `Edit: ${item.name}`;
  document.getElementById('itemName').value = item.name || '';
  document.getElementById('itemDescription').value = item.description || '';
  document.getElementById('itemPrice').value = item.price || '';
  document.getElementById('itemPrepTime').value = item.preparationTime || 10;
  
  if (item.categoryName) {
    document.getElementById('itemCategory').value = item.categoryName;
  }
  if (item.itemType) {
    document.getElementById('itemType').value = item.itemType;
  }
  document.getElementById('itemAvailable').checked = item.available !== false;

  const overlay = document.getElementById('menuModalOverlay');
  if (overlay) overlay.classList.add('active');
}

/**
 * Close Modal
 */
function closeMenuItemModal() {
  const overlay = document.getElementById('menuModalOverlay');
  if (overlay) overlay.classList.remove('active');
}

/**
 * Handle Add/Edit Form Submit
 */
async function handleMenuItemFormSubmit(e) {
  e.preventDefault();

  const editId = document.getElementById('itemEditId').value;
  const name = document.getElementById('itemName').value.trim();
  const description = document.getElementById('itemDescription').value.trim();
  const price = parseFloat(document.getElementById('itemPrice').value);
  const preparationTime = parseInt(document.getElementById('itemPrepTime').value, 10) || 10;
  const categoryName = document.getElementById('itemCategory').value;
  const itemType = document.getElementById('itemType').value;
  const available = document.getElementById('itemAvailable').checked;

  const payload = {
    name,
    description,
    price,
    preparationTime,
    categoryName,
    itemType,
    available
  };

  const btn = document.getElementById('btnSaveItem');
  btn.disabled = true;
  btn.textContent = 'Saving...';

  try {
    if (editId) {
      // PUT /api/admin/menu/{id}
      await apiCall(`/api/admin/menu/${editId}`, 'PUT', payload);
      showToast('Menu item updated successfully! 🍲', 'success');
    } else {
      // POST /api/admin/menu
      await apiCall('/api/admin/menu', 'POST', payload);
      showToast('New menu item created! 🎉', 'success');
    }

    closeMenuItemModal();
    await loadAdminMenuItems();
  } catch (err) {
    showToast(`Error: ${err.message}`, 'error');
  } finally {
    btn.disabled = false;
    btn.textContent = 'Save Item ➔';
  }
}

/**
 * Delete Menu Item
 */
async function deleteMenuItem(itemId) {
  const item = currentMenuItems.find(i => i.id === itemId);
  const name = item ? item.name : 'this item';

  if (!confirm(`Are you sure you want to delete "${name}" from the canteen menu?`)) {
    return;
  }

  try {
    await apiCall(`/api/admin/menu/${itemId}`, 'DELETE');
    showToast(`"${name}" deleted from menu`, 'info');
    await loadAdminMenuItems();
  } catch (err) {
    showToast(`Failed to delete item: ${err.message}`, 'error');
  }
}
