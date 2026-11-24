/**
 * Campus Canteen Online Ordering System - Cart Page Logic
 * Handles cart display, item removal, quantity updates, order placement, and UPI payment.
 */

let cartItems = [];
let upiInfo = { upiId: 'canteen@sbi', name: 'Campus Canteen' };

document.addEventListener('DOMContentLoaded', async () => {
  // Check auth
  await checkAuth();

  // Load cart items & UPI info
  await Promise.all([
    loadCart(),
    loadUpiInfo()
  ]);
});

/**
 * Fetch cart items from GET /api/cart
 */
async function loadCart() {
  const container = document.getElementById('cartItemsContainer');
  const summarySection = document.getElementById('billSummarySection');
  const floatingBar = document.getElementById('checkoutFloatingBar');
  const clearBtn = document.getElementById('btnClearCart');

  try {
    const data = await apiCall('/api/cart');
    cartItems = Array.isArray(data) ? data : [];

    await updateCartCountBadge();

    if (cartItems.length === 0) {
      if (clearBtn) clearBtn.style.display = 'none';
      if (summarySection) summarySection.style.display = 'none';
      if (floatingBar) floatingBar.style.display = 'none';

      container.innerHTML = `
        <div class="empty-state">
          <div class="empty-state-icon">🛒</div>
          <h3 class="empty-state-title">Your Cart is Empty</h3>
          <p class="empty-state-text">Looks like you haven't added anything to your tray yet.</p>
          <a href="menu.html" class="btn-action-primary">
            <span>Explore Menu</span> ➔
          </a>
        </div>
      `;
      return;
    }

    if (clearBtn) clearBtn.style.display = 'inline-block';
    if (summarySection) summarySection.style.display = 'block';
    if (floatingBar) floatingBar.style.display = 'flex';

    renderCartItems();
    updateBillSummary();
  } catch (err) {
    console.error('Failed to load cart:', err);
    container.innerHTML = `
      <div class="empty-state">
        <div class="empty-state-icon">⚠️</div>
        <h3 class="empty-state-title">Unable to Load Cart</h3>
        <p class="empty-state-text">${err.message || 'Please check your connection and try again.'}</p>
        <button class="btn-action-primary" onclick="loadCart()">Retry</button>
      </div>
    `;
  }
}

/**
 * Render cart items cards
 */
function renderCartItems() {
  const container = document.getElementById('cartItemsContainer');

  container.innerHTML = cartItems.map(item => {
    const foodId = item.foodItemId || item.id;
    const name = item.foodItemName || item.name || 'Food Item';
    const type = item.itemType || 'VEG';
    const typeClass = getFoodTypeGradientClass(type);
    const emoji = getFoodEmoji(name, type);
    const unitPrice = item.price || 0;
    const qty = item.quantity || 1;
    const subtotal = item.subtotal || (unitPrice * qty);

    return `
      <div class="cart-item-card" id="cart-row-${foodId}">
        <div class="cart-item-thumb ${typeClass}">
          <span>${emoji}</span>
        </div>

        <div class="cart-item-info">
          <h4 class="cart-item-name">${name}</h4>
          <span class="cart-item-price">${formatCurrency(unitPrice)} each</span>
          <div class="cart-item-subtotal">${formatCurrency(subtotal)}</div>
        </div>

        <div class="cart-item-actions">
          <button class="btn-remove-item" onclick="removeCartItem(${foodId})" title="Remove item">
            🗑️
          </button>
          <div class="quantity-stepper">
            <button class="stepper-btn" onclick="modifyQuantity(${foodId}, ${qty - 1})">-</button>
            <span class="stepper-val">${qty}</span>
            <button class="stepper-btn" onclick="modifyQuantity(${foodId}, ${qty + 1})">+</button>
          </div>
        </div>
      </div>
    `;
  }).join('');
}

/**
 * Update the bill calculations
 */
function updateBillSummary() {
  let totalItemsCount = 0;
  let itemsTotalAmount = 0;

  cartItems.forEach(item => {
    const qty = item.quantity || 1;
    const price = item.price || 0;
    const sub = item.subtotal !== undefined ? item.subtotal : (price * qty);

    totalItemsCount += qty;
    itemsTotalAmount += sub;
  });

  const summaryItemCount = document.getElementById('summaryItemCount');
  const summaryItemsTotal = document.getElementById('summaryItemsTotal');
  const summaryFinalTotal = document.getElementById('summaryFinalTotal');
  const checkoutBarTotal = document.getElementById('checkoutBarTotal');

  if (summaryItemCount) summaryItemCount.textContent = totalItemsCount;
  if (summaryItemsTotal) summaryItemsTotal.textContent = formatCurrency(itemsTotalAmount);
  if (summaryFinalTotal) summaryFinalTotal.textContent = formatCurrency(itemsTotalAmount);
  if (checkoutBarTotal) checkoutBarTotal.textContent = formatCurrency(itemsTotalAmount);
}

/**
 * Modify quantity of an item (+ / -)
 */
async function modifyQuantity(foodItemId, newQty) {
  try {
    if (newQty <= 0) {
      await removeCartItem(foodItemId);
    } else {
      await apiCall('/api/cart/update', 'PUT', { foodItemId, quantity: newQty });
      await loadCart();
    }
  } catch (err) {
    showToast(err.message || 'Failed to update quantity', 'error');
  }
}

/**
 * Remove an item completely from cart
 */
async function removeCartItem(foodItemId) {
  try {
    await apiCall(`/api/cart/remove/${foodItemId}`, 'DELETE');
    showToast('Item removed from cart', 'info');
    await loadCart();
  } catch (err) {
    showToast(err.message || 'Failed to remove item', 'error');
  }
}

/**
 * Clear the entire cart
 */
async function clearEntireCart() {
  if (!confirm('Are you sure you want to clear your entire cart?')) return;

  try {
    await apiCall('/api/cart/clear', 'DELETE');
    showToast('Cart cleared', 'info');
    await loadCart();
  } catch (err) {
    showToast(err.message || 'Failed to clear cart', 'error');
  }
}

/**
 * Fetch UPI info from /api/payment/upi-info
 */
async function loadUpiInfo() {
  try {
    const data = await apiCall('/api/payment/upi-info');
    if (data && data.upiId) {
      upiInfo = data;
      const el = document.getElementById('upiIdDisplay');
      if (el) el.textContent = data.upiId;
    }
  } catch (err) {
    console.warn('Could not fetch upi-info from server, using default:', err);
  }
}

/**
 * Copy UPI ID to clipboard
 */
function copyUpiId() {
  const upiId = upiInfo.upiId || 'canteen@sbi';
  navigator.clipboard.writeText(upiId).then(() => {
    showToast('UPI ID copied to clipboard! 📋', 'success');
  }).catch(() => {
    showToast(`UPI ID: ${upiId}`, 'info');
  });
}

/**
 * Modal visibility
 */
function openPaymentModal() {
  if (cartItems.length === 0) {
    showToast('Your cart is empty', 'warning');
    return;
  }
  const modal = document.getElementById('paymentModalOverlay');
  if (modal) modal.classList.add('active');
}

function closePaymentModal() {
  const modal = document.getElementById('paymentModalOverlay');
  if (modal) modal.classList.remove('active');
}

/**
 * Process order placement and UPI confirmation
 * Flow:
 * 1. POST /api/orders/place with { paymentMethod: 'UPI' }
 * 2. POST /api/payment/confirm with { orderId, transactionId }
 */
async function processOrderPayment() {
  const txnInput = document.getElementById('upiTxnId');
  const txnId = txnInput ? txnInput.value.trim() : '';
  const btn = document.getElementById('btnConfirmPayment');

  if (!txnId) {
    showToast('Please enter your UPI Transaction / Ref ID', 'warning');
    if (txnInput) txnInput.focus();
    return;
  }

  btn.disabled = true;
  btn.innerHTML = '<span>Processing Order...</span> ⏳';

  try {
    // 1. Place Order
    const placeRes = await apiCall('/api/orders/place', 'POST', { paymentMethod: 'UPI' });
    const orderId = placeRes && (placeRes.orderId || placeRes.id);

    if (!orderId) {
      throw new Error('Order placement did not return an order ID');
    }

    // 2. Confirm Payment
    await apiCall('/api/payment/confirm', 'POST', {
      orderId: orderId,
      transactionId: txnId
    });

    showToast('Order confirmed! Token generated 🎉', 'success');
    closePaymentModal();

    setTimeout(() => {
      window.location.href = '/orders.html';
    }, 1000);
  } catch (err) {
    showToast(err.message || 'Payment verification failed. Please try again.', 'error');
    btn.disabled = false;
    btn.innerHTML = '<span>Confirm Payment & Place Order</span> ➔';
  }
}
