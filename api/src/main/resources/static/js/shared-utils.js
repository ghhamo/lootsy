function authGuard() {
  const token = localStorage.getItem('auth.token');
  const expiresAt = Number(localStorage.getItem('auth.expiresAt') || 0);
  if (!token || (expiresAt && Date.now() > expiresAt)) {
    location.replace('/');
    return false;
  }
  return true;
}

window.authFetch = (url, opts = {}) => {
  const token = localStorage.getItem('auth.token');
  return fetch(url, {
    ...opts,
    headers: {
      'Accept': 'application/json',
      ...(opts.headers || {}),
      ...(token ? { 'Authorization': `Bearer ${token}` } : {})
    }
  });
};

async function logout() {
  try {
    await authFetch('/api/logout', { method: 'POST' });
  } catch (e) {
    console.error('Logout error:', e);
  } finally {
    localStorage.removeItem('auth.token');
    localStorage.removeItem('auth.expiresAt');
    location.replace('/');
  }
}


window.CartStore = {
  _cart: null,
  _loading: false,
  _lastFetch: 0,
  _cacheDuration: 5000,
  _subscribers: new Set(),

  subscribe(callback) {
    this._subscribers.add(callback);
    return () => this._subscribers.delete(callback);
  },

  _notify() {
    this._subscribers.forEach(callback => {
      try {
        callback(this._cart);
      } catch (e) {
        console.error('Cart subscriber error:', e);
      }
    });
  },

  async getCart() {
    const now = Date.now();
    
    if (this._cart && (now - this._lastFetch) < this._cacheDuration) {
      return this._cart;
    }

    if (this._loading) {
      while (this._loading) {
        await new Promise(resolve => setTimeout(resolve, 50));
      }
      return this._cart;
    }

    return this._fetchCart();
  },

  async refresh() {
    this._cart = null;
    this._lastFetch = 0;
    return this._fetchCart();
  },

  async _fetchCart() {
    const token = localStorage.getItem('auth.token');
    if (!token) {
      this._cart = null;
      this._notify();
      return null;
    }

    try {
      this._loading = true;
      const r = await authFetch('/api/carts/current');
      
      if (r.ok) {
        const text = await r.text();
        this._cart = text ? JSON.parse(text) : null;
        this._lastFetch = Date.now();
      } else {
        console.log('Cart request failed:', r.status, r.statusText);
        this._cart = null;
      }
    } catch (e) {
      console.error('Error fetching cart:', e);
      this._cart = null;
    } finally {
      this._loading = false;
    }

    this._notify();
    return this._cart;
  },

  async invalidate() {
    setTimeout(() => this.refresh(), 100);
  }
};

async function getCartForUser() {
  return window.CartStore.getCart();
}

window.addToCart = async function(productId, quantity = 1) {
  try {
    const token = localStorage.getItem('auth.token');
    if (!token) {
      location.replace('/');
      return false;
    }

    const res = await authFetch('/api/carts/add', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ productId, quantity })
    });

    if (res.status === 401) {
      location.replace('/');
      return false;
    }

    if (!res.ok) {
      let msg = `Add to cart failed (${res.status})`;
      try {
        const j = await res.json();
        if (j?.message) msg = j.message;
      } catch {}
      throw new Error(msg);
    }

    await window.CartStore.invalidate();
    return true;
  } catch (e) {
    console.error('Add to cart error:', e);
    throw e;
  }
};

window.removeFromCart = async function(productId) {
  try {
    const token = localStorage.getItem('auth.token');
    if (!token) return false;

    const res = await authFetch(`/api/carts/remove/${productId}`, {
      method: 'DELETE'
    });

    if (res.ok) {
      await window.CartStore.invalidate();
      return true;
    }
    return false;
  } catch (e) {
    console.error('Failed to remove item:', e);
    return false;
  }
};


function createMiniCartComponent() {
  return {
    open: false,
    items: [],
    count: 0,
    subtotal: 0,
    unsubscribe: null,

    async init() {
      this.unsubscribe = window.CartStore.subscribe((cart) => {
        this.updateCartDisplay(cart);
      });
      
      const cart = await window.CartStore.getCart();
      this.updateCartDisplay(cart);
    },

    destroy() {
      if (this.unsubscribe) {
        this.unsubscribe();
      }
    },

    toggle() {
      this.open = !this.open;
    },

    updateCartDisplay(cart) {
      if (cart && cart.items) {
        this.items = cart.items.map(item => ({
          id: item.productId,
          name: item.name || 'Product',
          price: parseFloat(item.price) || 0,
          quantity: item.quantity || 1,
          image: item.imageUrl || ''
        }));
        this.count = this.items.reduce((sum, item) => sum + item.quantity, 0);
        this.subtotal = this.items.reduce((sum, item) => sum + (item.price * item.quantity), 0);
      } else {
        this.items = [];
        this.count = 0;
        this.subtotal = 0;
      }
    },

    async remove(productId) {
      await removeFromCart(productId);
    },

    format(price) {
      return '$' + (price || 0).toFixed(2);
    }
  };
}

function createShippingComponent() {
  return {
    loading: true,
    error: '',
    items: [],
    subtotal: 0,
    unsubscribe: null,

    async init() {
      this.unsubscribe = window.CartStore.subscribe((cart) => {
        this.updateCartDisplay(cart);
        this.loading = false;
      });
      
      try {
        const cart = await window.CartStore.getCart();
        this.updateCartDisplay(cart);
      } catch (e) {
        console.error('Failed to load cart:', e);
        this.error = 'Failed to load cart items. Please try again.';
      } finally {
        this.loading = false;
      }
    },

    destroy() {
      if (this.unsubscribe) {
        this.unsubscribe();
      }
    },

    updateCartDisplay(cart) {
      this.error = '';
      if (cart && cart.items) {
        this.items = cart.items.map(item => ({
          id: item.productId,
          name: item.name || 'Product',
          price: parseFloat(item.price) || 0,
          quantity: item.quantity || 1,
          image: item.imageUrl || ''
        }));
        this.subtotal = this.items.reduce((sum, item) => sum + (item.price * item.quantity), 0);
      } else {
        this.items = [];
        this.subtotal = 0;
      }
    },

    format(price) {
      return '$' + (price || 0).toFixed(2);
    }
  };
}


function doSearch() {
  const searchInput = document.getElementById('searchInput');
  if (!searchInput) return;
  
  const query = searchInput.value.trim();
  if (query) {
    location.assign(`/search?q=${encodeURIComponent(query)}`);
  }
}


function initAccountDropdown(retryCount = 0) {
  const btn = document.getElementById('accountBtn');
  const menu = document.getElementById('accountMenu');
  
  if (!btn || !menu) {
    if (retryCount < 10) {
      setTimeout(() => initAccountDropdown(retryCount + 1), 100);
    }
    return;
  }
  
  const items = Array.from(menu.querySelectorAll('[role="menuitem"]'));

  function openMenu() {
    menu.classList.add('account-menu--open');
    btn.setAttribute('aria-expanded', 'true');
    setTimeout(() => items[0]?.focus(), 0);
  }

  function closeMenu() {
    menu.classList.remove('account-menu--open');
    btn.setAttribute('aria-expanded', 'false');
  }

  function toggleMenu() {
    const isOpen = menu.classList.contains('account-menu--open');
    isOpen ? closeMenu() : openMenu();
  }

  btn.addEventListener('click', (e) => {
    e.stopPropagation();
    toggleMenu();
  });

  document.addEventListener('click', (e) => {
    if (!menu.contains(e.target) && e.target !== btn) {
      closeMenu();
    }
  });

  btn.addEventListener('keydown', (e) => {
    if (e.key === 'Enter' || e.key === ' ') {
      e.preventDefault();
      toggleMenu();
    }
  });

  const logoutBtn = menu.querySelector('.logout');
  if (logoutBtn) {
    logoutBtn.addEventListener('click', (e) => {
      e.preventDefault();
      logout();
    });
  }
}


let componentsRegistered = false;

function registerAlpineComponents() {
  if (componentsRegistered) return;
  
  document.addEventListener('alpine:init', () => {
    Alpine.data('miniCart', () => createMiniCartComponent());
    
    Alpine.data('shipping', () => createShippingComponent());
    
    Alpine.data('productGrid', (url) => ({
      url,
      loading: true,
      error: '',
      products: [],
      
      async init() {
        try {
          const res = await authFetch(this.url);
          if (!res.ok) throw new Error('Failed to load products');
          const data = await res.json();
          const list = Array.isArray(data) ? data : (data.items ?? data.results ?? data.content ?? []);

          this.products = list.map(p => ({
            id: p.id ?? p.productId,
            name: p.name ?? p.title ?? 'Untitled',
            price: p.price != null ? Number(p.price) : null,
            image: p.image ?? p.imageUrl ?? p.thumbnail ?? '',
            category: p.category?.name || ''
          })).filter(p => p.id);
        } catch (e) {
          this.error = e.message || 'Error';
        } finally {
          this.loading = false;
        }
      },
      
      toDetails(id) {
        location.assign(`/details?id=${encodeURIComponent(id)}`);
      },
      
      async addToCart(id) {
        try {
          await addToCart(id, 1);
        } catch (e) {
          alert(e?.message || 'Add to cart failed');
        }
      }
    }));
  });
  
  componentsRegistered = true;
}


function initCommonPageFeatures() {
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
      initAccountDropdown();
    });
  } else {
    initAccountDropdown();
  }
}

initCommonPageFeatures();
