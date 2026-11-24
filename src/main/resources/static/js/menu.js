/**
 * Campus Canteen Online Ordering System - Menu Page Logic
 * Handles menu loading, category filters, veg/non-veg toggles, cart quantity synchronization.
 */

let allFoodItems = [];
let activeCategory = 'ALL';
let activeDietary = 'ALL';
let cartItemQuantities = {}; // Map of foodItemId -> quantity

document.addEventListener('DOMContentLoaded', async () => {
  // 1. Verify user authentication
  await checkAuth();

  // 2. Fetch categories and initial cart state
  await Promise.all([
    loadCategories(),
    refreshCartState()
  ]);

  // 3. Load menu items
  await loadMenuItems();
});

/**
 * Load categories from backend (/api/menu/categories) and render pills
 */
async function loadCategories() {
  const container = document.getElementById('categoryPillList');
  if (!container) return;

  try {
    const categories = await apiCall('/api/menu/categories');
    if (categories && Array.isArray(categories) && categories.length > 0) {
      container.innerHTML = `
        <button class="cat-pill active" data-category="ALL" onclick="selectCategory('ALL', this)">
          <span>✨</span> All Items
        </button>
      `;

      categories.forEach(cat => {
        const catName = typeof cat === 'object' ? cat.name : cat;
        const catId = typeof cat === 'object' ? cat.id : cat;
        const icon = getCategoryIcon(catName);

        const btn = document.createElement('button');
        btn.className = 'cat-pill';
        btn.dataset.category = catName;
        btn.dataset.id = catId;
        btn.onclick = () => selectCategory(catName, btn, catId);
        btn.innerHTML = `<span>${icon}</span> ${catName}`;
        container.appendChild(btn);
      });
    }
  } catch (err) {
    console.warn('Could not load categories from API, using default presets:', err);
  }
}

function getCategoryIcon(name) {
  const n = (name || '').toLowerCase();
  if (n.includes('break')) return '🥞';
  if (n.includes('lunch') || n.includes('meal')) return '🍛';
  if (n.includes('snack') || n.includes('quick')) return '🍟';
  if (n.includes('bev') || n.includes('drink')) return '🥤';
  if (n.includes('dessert') || n.includes('sweet')) return '🍰';
  return '🍴';
}

/**
 * Switch active category filter
 */
function selectCategory(categoryName, element, categoryId = null) {
  activeCategory = categoryName;

  // Highlight pill
  document.querySelectorAll('.cat-pill').forEach(el => el.classList.remove('active'));
  if (element) element.classList.add('active');

  applyFilters();
}

/**
 * Switch active dietary filter (ALL, VEG, NON_VEG)
 */
function setDietaryFilter(diet) {
  activeDietary = diet;

  document.getElementById('filterAll').classList.toggle('active', diet === 'ALL');
  document.getElementById('filterVeg').classList.toggle('active', diet === 'VEG');
  document.getElementById('filterNonVeg').classList.toggle('active', diet === 'NON_VEG');

  applyFilters();
}

/**
 * Search input event handler
 */
function handleMenuSearch() {
  applyFilters();
}

/**
 * Load food items from /api/menu
 */
async function loadMenuItems() {
  const grid = document.getElementById('foodGridContainer');
  try {
    const items = await apiCall('/api/menu');
    allFoodItems = Array.isArray(items) ? items : [];
    applyFilters();
  } catch (err) {
    console.error('Failed to load menu items:', err);
    grid.innerHTML = `
      <div class="empty-state" style="grid-column: 1 / -1;">
        <div class="empty-state-icon">😕</div>
        <h3 class="empty-state-title">Menu Unavailable</h3>
        <p class="empty-state-text">Could not connect to canteen server. Please make sure backend is running.</p>
        <button class="btn-action-primary" onclick="loadMenuItems()">Retry Loading</button>
      </div>
    `;
  }
}

/**
 * Filter items by activeCategory, activeDietary, and search text
 */
function applyFilters() {
  const query = (document.getElementById('menuSearchInput')?.value || '').toLowerCase().trim();

  const filtered = allFoodItems.filter(item => {
    // 1. Dietary match
    if (activeDietary !== 'ALL') {
      const type = (item.itemType || '').toUpperCase();
      if (activeDietary === 'VEG' && type !== 'VEG') return false;
      if (activeDietary === 'NON_VEG' && type !== 'NON_VEG' && type !== 'NON-VEG') return false;
    }

    // 2. Category match
    if (activeCategory !== 'ALL') {
      const cat = (item.categoryName || '').toLowerCase();
      if (!cat.includes(activeCategory.toLowerCase())) return false;
    }

    // 3. Search query match
    if (query) {
      const name = (item.name || '').toLowerCase();
      const desc = (item.description || '').toLowerCase();
      const cat = (item.categoryName || '').toLowerCase();
      if (!name.includes(query) && !desc.includes(query) && !cat.includes(query)) {
        return false;
      }
    }

    return true;
  });

  renderFoodGrid(filtered);
}

/**
 * Render the filtered food items into grid
 */
function renderFoodGrid(items) {
  const grid = document.getElementById('foodGridContainer');
  const countLabel = document.getElementById('menuItemCount');

  if (countLabel) {
    countLabel.textContent = `${items.length} item${items.length === 1 ? '' : 's'}`;
  }

  if (items.length === 0) {
    grid.innerHTML = `
      <div class="empty-state" style="grid-column: 1 / -1;">
        <div class="empty-state-icon">🔍</div>
        <h3 class="empty-state-title">No Food Items Found</h3>
        <p class="empty-state-text">Try switching your category or dietary filter to see more options.</p>
      </div>
    `;
    return;
  }

  grid.innerHTML = items.map(item => {
    const isVeg = (item.itemType || '').toUpperCase() === 'VEG';
    const isBeverage = (item.itemType || '').toUpperCase() === 'BEVERAGE' || (item.itemType || '').toUpperCase() === 'DRINK';
    const typeClass = getFoodTypeGradientClass(item.itemType);
    const emoji = getFoodEmoji(item.name, item.itemType);
    const currentQty = cartItemQuantities[item.id] || 0;
    const isAvailable = item.available !== false;

    // Action button or stepper
    let actionHtml = '';
    if (!isAvailable) {
      actionHtml = `<span class="btn-add-cart unavailable">Sold Out</span>`;
    } else if (currentQty > 0) {
      actionHtml = `
        <div class="quantity-stepper">
          <button class="stepper-btn" onclick="updateCartQuantity(${item.id}, ${currentQty - 1})">-</button>
          <span class="stepper-val">${currentQty}</span>
          <button class="stepper-btn" onclick="updateCartQuantity(${item.id}, ${currentQty + 1})">+</button>
        </div>
      `;
    } else {
      actionHtml = `
        <button class="btn-add-cart" onclick="addToCart(${item.id})">
          <span>+</span> Add
        </button>
      `;
    }

    const typeDotClass = isVeg ? 'veg' : (isBeverage ? 'beverage' : 'non-veg');
    const prepTimeDisplay = item.preparationTime ? `⏱️ ${item.preparationTime}m` : '⏱️ 10m';

    return `
      <div class="food-card" data-id="${item.id}">
        <!-- Gradient Food Thumb -->
        <div class="food-thumb ${typeClass}">
          <span class="food-thumb-emoji">${emoji}</span>
          <span class="prep-time-badge">${prepTimeDisplay}</span>
          <div class="type-indicator-badge" title="${item.itemType}">
            <div class="type-dot ${typeDotClass}"></div>
          </div>
        </div>

        <!-- Food Details -->
        <div class="food-details">
          <span class="food-category-tag">${item.categoryName || 'Canteen Special'}</span>
          <h3 class="food-title" title="${item.name}">${item.name}</h3>
          <p class="food-desc" title="${item.description || ''}">${item.description || 'Freshly prepared at the campus counter.'}</p>

          <div class="food-footer">
            <span class="food-price">${formatCurrency(item.price)}</span>
            <div id="cart-btn-slot-${item.id}">
              ${actionHtml}
            </div>
          </div>
        </div>
      </div>
    `;
  }).join('');
}

/**
 * Fetch cart from backend to sync quantity counters
 */
async function refreshCartState() {
  try {
    const cartItems = await apiCall('/api/cart');
    cartItemQuantities = {};

    if (Array.isArray(cartItems)) {
      cartItems.forEach(ci => {
        const id = ci.foodItemId || ci.foodItem?.id || ci.id;
        if (id) {
          cartItemQuantities[id] = ci.quantity;
        }
      });
    }

    await updateCartCountBadge();
  } catch (err) {
    console.warn('Could not sync cart state:', err);
  }
}

/**
 * Add item to cart (calls POST /api/cart/add)
 */
async function addToCart(foodItemId) {
  try {
    await apiCall('/api/cart/add', 'POST', { foodItemId, quantity: 1 });
    cartItemQuantities[foodItemId] = 1;
    showToast('Added to cart! 🛒', 'success');
    await updateCartCountBadge();
    applyFilters();
  } catch (err) {
    showToast(err.message || 'Could not add item to cart', 'error');
  }
}

/**
 * Update quantity of item in cart (PUT /api/cart/update or DELETE /api/cart/remove)
 */
async function updateCartQuantity(foodItemId, newQuantity) {
  try {
    if (newQuantity <= 0) {
      await apiCall(`/api/cart/remove/${foodItemId}`, 'DELETE');
      delete cartItemQuantities[foodItemId];
      showToast('Item removed from cart', 'info');
    } else {
      await apiCall('/api/cart/update', 'PUT', { foodItemId, quantity: newQuantity });
      cartItemQuantities[foodItemId] = newQuantity;
    }

    await updateCartCountBadge();
    applyFilters();
  } catch (err) {
    showToast(err.message || 'Failed to update cart', 'error');
  }
}
