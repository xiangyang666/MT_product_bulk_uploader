import { ref, onMounted, onUnmounted } from 'vue'

interface UseParallaxOptions {
  speed?: number
  direction?: 'vertical' | 'horizontal'
}

export function useParallax(options: UseParallaxOptions = {}) {
  const {
    speed = 0.5,
    direction = 'vertical'
  } = options

  const offset = ref(0)
  let ticking = false

  const updateParallax = () => {
    const scrolled = window.scrollY
    
    if (direction === 'vertical') {
      offset.value = scrolled * speed
    } else {
      offset.value = scrolled * speed
    }
    
    ticking = false
  }

  const handleScroll = () => {
    if (!ticking) {
      requestAnimationFrame(updateParallax)
      ticking = true
    }
  }

  onMounted(() => {
    window.addEventListener('scroll', handleScroll, { passive: true })
    updateParallax()
  })

  onUnmounted(() => {
    window.removeEventListener('scroll', handleScroll)
  })

  return {
    offset
  }
}
