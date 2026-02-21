<template>
  <div
    ref="elementRef"
    class="scroll-reveal"
    :class="{
      'is-visible': isVisible,
      [`animation-${animation}`]: true,
      [`delay-${delay}`]: delay > 0
    }"
  >
    <slot />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useScrollAnimation } from '../../composables/useScrollAnimation'

interface Props {
  animation?: 'fade' | 'slide-up' | 'slide-left' | 'slide-right' | 'scale'
  delay?: number
  threshold?: number
}

const props = withDefaults(defineProps<Props>(), {
  animation: 'fade',
  delay: 0,
  threshold: 0.2
})

const elementRef = ref<HTMLElement | null>(null)
const { isVisible } = useScrollAnimation(elementRef, {
  threshold: props.threshold,
  triggerOnce: true
})
</script>

<style scoped>
.scroll-reveal {
  opacity: 0;
  transition: all 0.6s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.scroll-reveal.is-visible {
  opacity: 1;
}

/* Fade animation */
.animation-fade {
  opacity: 0;
}

.animation-fade.is-visible {
  opacity: 1;
}

/* Slide up animation */
.animation-slide-up {
  opacity: 0;
  transform: translateY(40px);
}

.animation-slide-up.is-visible {
  opacity: 1;
  transform: translateY(0);
}

/* Slide left animation */
.animation-slide-left {
  opacity: 0;
  transform: translateX(-40px);
}

.animation-slide-left.is-visible {
  opacity: 1;
  transform: translateX(0);
}

/* Slide right animation */
.animation-slide-right {
  opacity: 0;
  transform: translateX(40px);
}

.animation-slide-right.is-visible {
  opacity: 1;
  transform: translateX(0);
}

/* Scale animation */
.animation-scale {
  opacity: 0;
  transform: scale(0.9);
}

.animation-scale.is-visible {
  opacity: 1;
  transform: scale(1);
}

/* Delay classes */
.delay-1 {
  transition-delay: 100ms;
}

.delay-2 {
  transition-delay: 200ms;
}

.delay-3 {
  transition-delay: 300ms;
}

.delay-4 {
  transition-delay: 400ms;
}

.delay-5 {
  transition-delay: 500ms;
}
</style>
