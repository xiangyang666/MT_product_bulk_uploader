import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useProductStore = defineStore('product', () => {
  // 状态
  const products = ref([])
  const loading = ref(false)
  const error = ref(null)

  // 操作
  const setProducts = (data) => {
    products.value = data
  }

  const setLoading = (status) => {
    loading.value = status
  }

  const setError = (err) => {
    error.value = err
  }

  const clearProducts = () => {
    products.value = []
  }

  return {
    products,
    loading,
    error,
    setProducts,
    setLoading,
    setError,
    clearProducts
  }
})
