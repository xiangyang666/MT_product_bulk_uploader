import { ref, onMounted, onUnmounted, type Ref } from 'vue'

interface UseScrollAnimationOptions {
  threshold?: number
  rootMargin?: string
  triggerOnce?: boolean
}

export function useScrollAnimation(
  elementRef: Ref<HTMLElement | null>,
  options: UseScrollAnimationOptions = {}
) {
  const {
    threshold = 0.2,
    rootMargin = '50px',
    triggerOnce = true
  } = options

  const isVisible = ref(false)
  const hasTriggered = ref(false)
  let observer: IntersectionObserver | null = null

  onMounted(() => {
    if (!elementRef.value) return

    observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            isVisible.value = true
            
            if (triggerOnce && !hasTriggered.value) {
              hasTriggered.value = true
              // 一旦触发就停止观察
              if (observer && entry.target) {
                observer.unobserve(entry.target)
              }
            }
          } else if (!triggerOnce) {
            isVisible.value = false
          }
        })
      },
      {
        threshold,
        rootMargin
      }
    )

    observer.observe(elementRef.value)
  })

  onUnmounted(() => {
    if (observer) {
      observer.disconnect()
    }
  })

  return {
    isVisible,
    hasTriggered
  }
}
