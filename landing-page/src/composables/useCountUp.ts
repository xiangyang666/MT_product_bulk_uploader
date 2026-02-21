import { ref } from 'vue'

interface UseCountUpOptions {
  duration?: number
  startValue?: number
  easing?: (t: number) => number
}

// 缓动函数：ease-out
const easeOutQuad = (t: number): number => {
  return t * (2 - t)
}

export function useCountUp(
  targetValue: number,
  options: UseCountUpOptions = {}
) {
  const {
    duration = 2000,
    startValue = 0,
    easing = easeOutQuad
  } = options

  const currentValue = ref(startValue)
  const isAnimating = ref(false)

  const start = () => {
    if (isAnimating.value) return

    isAnimating.value = true
    const startTime = performance.now()
    const valueRange = targetValue - startValue

    const animate = (currentTime: number) => {
      const elapsed = currentTime - startTime
      const progress = Math.min(elapsed / duration, 1)
      const easedProgress = easing(progress)

      currentValue.value = startValue + valueRange * easedProgress

      if (progress < 1) {
        requestAnimationFrame(animate)
      } else {
        currentValue.value = targetValue
        isAnimating.value = false
      }
    }

    requestAnimationFrame(animate)
  }

  const reset = () => {
    currentValue.value = startValue
    isAnimating.value = false
  }

  return {
    currentValue,
    isAnimating,
    start,
    reset
  }
}
