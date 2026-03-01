import { defineStore } from 'pinia'

export const useCartStore = defineStore('cart', {
  state: () => ({
    cartItems: []
  }),
  getters: {
    getCartItems: (state) => state.cartItems,
    getCartCount: (state) => state.cartItems.reduce((count, item) => count + item.quantity, 0),
    getCartTotal: (state) => state.cartItems.reduce((total, item) => total + (item.price * item.quantity), 0)
  },
  actions: {
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
    },
    removeFromCart(productId) {
      this.cartItems = this.cartItems.filter(item => item.id !== productId)
    },
    updateQuantity(productId, quantity) {
      const item = this.cartItems.find(item => item.id === productId)
      if (item) {
        item.quantity = quantity
        if (item.quantity <= 0) {
          this.removeFromCart(productId)
        }
      }
    },
    clearCart() {
      this.cartItems = []
    }
  }
})
