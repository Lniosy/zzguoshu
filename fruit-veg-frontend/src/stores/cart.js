import { defineStore } from 'pinia'

export const useCartStore = defineStore('cart', {
  state: () => ({
    cartItems: (() => {
      try {
        const raw = localStorage.getItem('cartItems')
        return raw ? JSON.parse(raw) : []
      } catch (e) {
        return []
      }
    })()
  }),
  getters: {
    getCartItems: (state) => state.cartItems,
    getCartCount: (state) => state.cartItems.reduce((count, item) => count + item.quantity, 0),
    getCartTotal: (state) => state.cartItems.reduce((total, item) => total + (item.price * item.quantity), 0)
  },
  actions: {
    persist() {
      localStorage.setItem('cartItems', JSON.stringify(this.cartItems))
    },
    addToCart(product, quantity = 1) {
      const existingItem = this.cartItems.find(item => item.id === product.id)
      if (existingItem) {
        existingItem.quantity += quantity
      } else {
        this.cartItems.push({
          ...product,
          quantity: quantity
        })
      }
      this.persist()
    },
    removeFromCart(productId) {
      this.cartItems = this.cartItems.filter(item => item.id !== productId)
      this.persist()
    },
    updateQuantity(productId, quantity) {
      const item = this.cartItems.find(item => item.id === productId)
      if (item) {
        item.quantity = quantity
        if (item.quantity <= 0) {
          this.removeFromCart(productId)
        }
      }
      this.persist()
    },
    clearCart() {
      this.cartItems = []
      this.persist()
    }
  }
})
