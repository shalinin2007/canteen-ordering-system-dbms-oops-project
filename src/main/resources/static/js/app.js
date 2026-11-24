/**
 * Campus Canteen Online Ordering System - Core Shared Utilities
 * Handles API calls, authentication checks, toast notifications, and helpers.
 */

const API_BASE = ''; // Same origin (localhost:8080)

/**
 * Perform an API call with automatic JSON headers, credentials, and error handling
 */
async function apiCall(url, method = 'GET', body = null) {
  const options = {
    method: method.toUpperCase(),
    headers: {
      'Accept': 'application/json'
    },
    credentials: 'include' // Allow session cookie passing
  };

  if (body) {
    options.headers['Content-Type'] = 'application/json';
    options.body = JSON.stringify(body);
  }

  try {
    const response = await fetch(`${API_BASE}${url}`, options);

    // Handle 401 Unauthorized
    if (response.status === 401) {
      sessionStorage.removeItem('canteen_user');
      // If not already on index.html, redirect
      if (!window.location.pathname.endsWith('index.html') && window.location.pathname !== '/') {
        showToast('Session expired. Please log in again.', 'warning');
        setTimeout(() => {
          window.location.href = '/index.html';
        }, 1200);
      }
      throw new Error('Unauthorized');
    }

    // Try parsing JSON if content exists
    const contentType = response.headers.get('content-type');
    let data = null;
    if (contentType && contentType.includes('application/json')) {
      data = await response.json();
    } else {
      const text = await response.text();
      try {
        data = text ? JSON.parse(text) : null;
      } catch (e) {
        data = text;
      }
    }

    if (!response.ok) {
      const errorMsg = (data && (data.message || data.error)) || `Error ${response.status}: ${response.statusText}`;
      throw new Error(errorMsg);
    }

    return data;
  } catch (error) {
    console.error(`API Error on [${method}] ${url}:`, error);
    throw error;
  }
}

/**
 * Verify user authentication. If not authenticated, redirect to login page.
 * If user info exists, caches it in sessionStorage and returns it.
 */
async function checkAuth(requiredRole = null) {
  try {
    const user = await apiCall('/api/auth/me');
    if (!user || !(user.userId || user.id)) {
      sessionStorage.removeItem('canteen_user');
      window.location.href = '/index.html';
      return null;
    }

    sessionStorage.setItem('canteen_user', JSON.stringify(user));

    if (requiredRole && user.role !== requiredRole) {
      showToast('Access denied: Unauthorized role', 'error');
      setTimeout(() => {
        window.location.href = user.dashboardUrl || (user.role === 'ADMIN' ? '/admin/dashboard.html' : '/menu.html');
      }, 1000);
      return null;
    }

    renderHeaderUser(user);
    return user;
  } catch (err) {
    sessionStorage.removeItem('canteen_user');
    window.location.href = '/index.html';
    return null;
  }
}

/**
 * Check Admin authentication. Redirects if not logged in or role is not ADMIN.
 */
async function checkAdminAuth() {
  return await checkAuth('ADMIN');
}

/**
 * Render user name / greeting in header if elements exist
 */
function renderHeaderUser(user) {
  const nameEl = document.getElementById('user-display-name');
  if (nameEl && user && user.name) {
    nameEl.textContent = user.name.split(' ')[0];
  }
}

/**
 * Logout the current user and redirect to login
 */
async function logoutUser() {
  try {
    await apiCall('/api/auth/logout', 'POST');
  } catch (err) {
    console.warn('Logout request completed with warning:', err);
  } finally {
    sessionStorage.removeItem('canteen_user');
    showToast('Logged out successfully', 'info');
    setTimeout(() => {
      window.location.href = '/index.html';
    }, 600);
  }
}

/**
 * Display toast notification with smooth enter/exit animations
 * Types: 'success' | 'error' | 'warning' | 'info'
 */
function showToast(message, type = 'info') {
  let container = document.getElementById('toast-container');
  if (!container) {
    container = document.createElement('div');
    container.id = 'toast-container';
    document.body.appendChild(container);
  }

  const icons = {
    success: '✅',
    error: '❌',
    warning: '⚠️',
    info: 'ℹ️'
  };

  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  toast.innerHTML = `
    <span>${icons[type] || 'ℹ️'}</span>
    <span>${message}</span>
  `;

  container.appendChild(toast);

  setTimeout(() => {
    toast.classList.add('fade-out');
    setTimeout(() => {
      if (toast.parentElement) toast.remove();
    }, 280);
  }, 3200);
}

/**
 * Format currency in Indian Rupees: ₹XX.00
 */
function formatCurrency(amount) {
  const num = Number(amount) || 0;
  return `₹${num.toFixed(2)}`;
}

/**
 * Format date string into human readable string
 */
function formatDate(dateString) {
  if (!dateString) return '';
  const date = new Date(dateString);
  if (isNaN(date.getTime())) return dateString;

  const now = new Date();
  const isToday = now.toDateString() === date.toDateString();

  const timeOptions = { hour: '2-digit', minute: '2-digit', hour12: true };
  const formattedTime = date.toLocaleTimeString('en-US', timeOptions);

  if (isToday) {
    return `Today, ${formattedTime}`;
  }

  const dateOptions = { month: 'short', day: 'numeric' };
  return `${date.toLocaleDateString('en-US', dateOptions)}, ${formattedTime}`;
}

/**
 * Returns CSS badge class for order status
 */
function getStatusBadgeClass(status) {
  const normalized = (status || '').toUpperCase();
  switch (normalized) {
    case 'PENDING': return 'status-pending';
    case 'CONFIRMED': return 'status-confirmed';
    case 'PREPARING': return 'status-preparing';
    case 'READY': return 'status-ready';
    case 'DELIVERED': return 'status-delivered';
    case 'CANCELLED': return 'status-cancelled';
    default: return 'status-pending';
  }
}

/**
 * Fetch and update global cart badge counter
 */
async function updateCartCountBadge() {
  try {
    const data = await apiCall('/api/cart/count');
    const count = data && typeof data.count === 'number' ? data.count : 0;
    
    const badgeHeaders = document.querySelectorAll('.cart-badge-count, .nav-cart-badge');
    badgeHeaders.forEach(el => {
      el.textContent = count;
      if (count > 0) {
        el.classList.add('active');
      } else {
        el.classList.remove('active');
      }
    });
    return count;
  } catch (err) {
    // If not logged in or backend unavailable, silently ignore
    return 0;
  }
}

/**
 * Returns appropriate gradient class or style for food item type
 */
function getFoodTypeGradientClass(itemType) {
  const t = (itemType || '').toUpperCase();
  if (t === 'VEG') return 'veg';
  if (t === 'NON_VEG' || t === 'NON-VEG') return 'non-veg';
  if (t === 'BEVERAGE' || t === 'DRINK') return 'beverage';
  return 'veg';
}

/**
 * Returns food emoji based on item name and category
 */
function getFoodEmoji(name, itemType) {
  const lower = (name || '').toLowerCase();
  const t = (itemType || '').toUpperCase();

  if (lower.includes('burger')) return '🍔';
  if (lower.includes('pizza')) return '🍕';
  if (lower.includes('dosa') || lower.includes('idli')) return '🥞';
  if (lower.includes('samosa') || lower.includes('roll')) return '🥟';
  if (lower.includes('biryani') || lower.includes('rice') || lower.includes('fried rice')) return '🍚';
  if (lower.includes('sandwich')) return '🥪';
  if (lower.includes('noodles') || lower.includes('maggi') || lower.includes('pasta')) return '🍜';
  if (lower.includes('coffee') || lower.includes('tea') || lower.includes('chai')) return '☕';
  if (lower.includes('juice') || lower.includes('shake') || lower.includes('smoothie')) return '🥤';
  if (lower.includes('ice cream') || lower.includes('cake') || lower.includes('pastry')) return '🍰';
  if (lower.includes('chicken') || lower.includes('mutton') || lower.includes('fish')) return '🍗';
  
  if (t === 'BEVERAGE') return '🥤';
  if (t === 'NON_VEG') return '🍗';
  return '🍲';
}
