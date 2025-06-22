let products = [];
let cart = [];
let isLoggedIn = false;

const loginSection = document.getElementById('loginSection');
const catalogSection = document.getElementById('catalogSection');
const checkoutSection = document.getElementById('checkoutSection');
const loginForm = document.getElementById('loginForm');
const productsGrid = document.getElementById('productsGrid');
const cartItems = document.getElementById('cartItems');
const cartTotal = document.getElementById('cartTotal');
const checkoutBtn = document.getElementById('checkoutBtn');
const loadingProducts = document.getElementById('loadingProducts');

function loadCartFromStorage() {
    try {
        const savedCart = localStorage.getItem('mercadinho_cart');
        if (savedCart) {
            cart = JSON.parse(savedCart);
        }
    } catch (error) {
        console.log('localStorage não disponível ou erro ao carregar carrinho:', error);
        cart = [];
    }
}


function saveCartToStorage() {
    try {
        localStorage.setItem('mercadinho_cart', JSON.stringify(cart));
    } catch (error) {
        console.log('localStorage não disponível:', error);
    }
}

async function fetchProducts() {
    try {
        loadingProducts.style.display = 'block';
        const response = await fetch('https://fakestoreapi.com/products');
        
        if (!response.ok) {
            throw new Error('Erro ao buscar produtos da API');
        }
        
        products = await response.json();
        loadingProducts.style.display = 'none';
        loadProducts();
    } catch (error) {
        console.error('Erro ao carregar produtos:', error);
        loadingProducts.innerHTML = `
            <div class="error-message">
                ❌ Erro ao carregar produtos da API<br>
                <small>Verifique sua conexão com a internet</small>
            </div>
        `;
    }
}

loginForm.addEventListener('submit', handleLogin);
checkoutBtn.addEventListener('click', showCheckout);
document.getElementById('backToCart').addEventListener('click', backToCart);
document.getElementById('checkoutForm').addEventListener('submit', handleCheckout);

function handleLogin(e) {
    e.preventDefault();
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    
    if (username && password) {
        isLoggedIn = true;
        showCatalog();
    } else {
        alert('Por favor, preencha todos os campos');
    }
}

function showCatalog() {
    loginSection.style.display = 'none';
    catalogSection.style.display = 'block';
    loadCartFromStorage(); 
    fetchProducts(); 
    updateCart(); 
}

function loadProducts() {
    productsGrid.innerHTML = '';
    products.forEach(product => {
        const productCard = document.createElement('div');
        productCard.className = 'product-card';
        productCard.innerHTML = `
            <div class="product-image">
                <img src="${product.image}" alt="${product.title}" onerror="this.src='data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgZmlsbD0iI2YwZjBmMCIvPjx0ZXh0IHg9IjUwIiB5PSI1NSIgZm9udC1mYW1pbHk9IkFyaWFsIiBmb250LXNpemU9IjE0IiBmaWxsPSIjOTk5IiB0ZXh0LWFuY2hvcj0ibWlkZGxlIj7wn5OKIE5vIEltYWdlPC90ZXh0Pjwvc3ZnPg=='">
            </div>
            <div class="product-title">${product.title}</div>
            <div class="product-description">${product.description}</div>
            <div class="product-price">R$ ${product.price.toFixed(2).replace('.', ',')}</div>
            <button class="add-to-cart" onclick="addToCart(${product.id})">
                🔥 Adicionar ao Carrinho 🔥
            </button>
        `;
        productsGrid.appendChild(productCard);
    });
}

function addToCart(productId) {
    const product = products.find(p => p.id === productId);
    const existingItem = cart.find(item => item.id === productId);
    
    if (existingItem) {
        existingItem.quantity += 1;
    } else {
        cart.push({...product, quantity: 1});
    }
    
    saveCartToStorage(); 
    updateCart();
}

function removeFromCart(productId) {
    cart = cart.filter(item => item.id !== productId);
    saveCartToStorage();
    updateCart();
}

function updateCart() {
    if (cart.length === 0) {
        cartItems.innerHTML = '<p>Seu carrinho está vazio - Adicione alguns produtos! 🛍️</p>';
        cartTotal.innerHTML = 'Total: R$ 0,00';
        checkoutBtn.disabled = true;
        return;
    }

    let html = '';
    let total = 0;

    cart.forEach(item => {
        const itemTotal = item.price * item.quantity;
        total += itemTotal;
        
        html += `
            <div class="cart-item">
                <div>
                    <strong>${item.title}</strong><br>
                    <small>Quantidade: ${item.quantity} | Preço: R$ ${item.price.toFixed(2).replace('.', ',')}</small>
                </div>
                <div>
                    <strong>R$ ${itemTotal.toFixed(2).replace('.', ',')}</strong>
                    <button class="remove-btn" onclick="removeFromCart(${item.id})">Remover</button>
                </div>
            </div>
        `;
    });

    cartItems.innerHTML = html;
    cartTotal.innerHTML = `Total: R$ ${total.toFixed(2).replace('.', ',')}`;
    checkoutBtn.disabled = false;
}

function showCheckout() {
    catalogSection.style.display = 'none';
    checkoutSection.style.display = 'block';
    
    let total = cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);
    let summaryHtml = '<h3>Resumo do Pedido</h3>';
    
    cart.forEach(item => {
        summaryHtml += `
            <div class="cart-item">
                <span>${item.title} (${item.quantity}x)</span>
                <span>R$ ${(item.price * item.quantity).toFixed(2).replace('.', ',')}</span>
            </div>
        `;
    });
    
    summaryHtml += `<div class="cart-total">Total: R$ ${total.toFixed(2).replace('.', ',')}</div>`;
    document.getElementById('orderSummary').innerHTML = summaryHtml;
}

function backToCart() {
    checkoutSection.style.display = 'none';
    catalogSection.style.display = 'block';
}

function handleCheckout(e) {
    e.preventDefault();
    
    const formData = new FormData(e.target);
    const orderData = {
        items: cart,
        customer: {
            name: formData.get('fullName'),
            address: formData.get('address'),
            phone: formData.get('phone')
        },
        payment: formData.get('paymentMethod'),
        total: cart.reduce((sum, item) => sum + (item.price * item.quantity), 0)
    };

    console.log('Pedido realizado:', orderData);
    
    cart = [];
    saveCartToStorage();
    
    checkoutSection.innerHTML = `
        <div class="success-message">
            <h2>✅ Pedido Confirmado!</h2>
            <p>Seu pedido foi realizado com sucesso!</p>
            <p><strong>Total:</strong> R$ ${orderData.total.toFixed(2).replace('.', ',')}</p>
            <p>Você receberá uma confirmação por email em breve.</p>
            <button class="btn" onclick="startOver()">Fazer Nova Compra</button>
        </div>
    `;
}

function startOver() {
    cart = [];
    saveCartToStorage();
    checkoutSection.style.display = 'none';
    catalogSection.style.display = 'block';
    updateCart();
}