<template>
  <a
    :href="href"
    class="animated-button"
    :class="[`variant-${variant}`, { 'is-loading': loading }]"
    @click="handleClick"
    @mouseenter="isHovered = true"
    @mouseleave="isHovered = false"
  >
    <span class="button-content">
      <slot name="icon">
        <svg
          v-if="icon"
          class="button-icon"
          :class="{ 'icon-animated': isHovered }"
          width="24"
          height="24"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
        >
          <path v-if="icon === 'download'" d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M7 10l5 5 5-5M12 15V3" />
          <path v-else-if="icon === 'arrow-right'" d="M5 12h14M12 5l7 7-7 7" />
          <circle v-else-if="icon === 'play'" cx="12" cy="12" r="10" />
        </svg>
      </slot>
      <span class="button-text">{{ text }}</span>
    </span>

    <!-- Ripple effect container -->
    <span
      v-for="ripple in ripples"
      :key="ripple.id"
      class="ripple-effect"
      :style="{
        left: `${ripple.x}px`,
        top: `${ripple.y}px`
      }"
    />

    <!-- Animated background -->
    <span class="button-bg" />
  </a>
</template>

<script setup lang="ts">
import { ref } from 'vue'

interface Props {
  text: string
  href?: string
  icon?: 'download' | 'arrow-right' | 'play'
  variant?: 'primary' | 'secondary'
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  href: '#',
  variant: 'primary',
  loading: false
})

const emit = defineEmits<{
  click: [event: MouseEvent]
}>()

interface Ripple {
  id: number
  x: number
  y: number
}

const isHovered = ref(false)
const ripples = ref<Ripple[]>([])
let rippleId = 0

const handleClick = (event: MouseEvent) => {
  // 创建涟漪效果
  const button = event.currentTarget as HTMLElement
  const rect = button.getBoundingClientRect()
  
  // 支持触摸事件
  let x, y
  if ('touches' in event && (event as any).touches.length > 0) {
    x = (event as any).touches[0].clientX - rect.left
    y = (event as any).touches[0].clientY - rect.top
  } else {
    x = event.clientX - rect.left
    y = event.clientY - rect.top
  }

  const newRipple: Ripple = {
    id: rippleId++,
    x,
    y
  }

  ripples.value.push(newRipple)

  // 300ms 后移除涟漪
  setTimeout(() => {
    ripples.value = ripples.value.filter(r => r.id !== newRipple.id)
  }, 600)

  emit('click', event)
}
</script>

<style scoped>
.animated-button {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: 1rem 2.5rem;
  font-size: var(--font-size-lg);
  font-weight: 700;
  border-radius: 50px;
  overflow: hidden;
  cursor: pointer;
  text-decoration: none;
  transition: all var(--transition-base);
  will-change: transform;
}

.button-content {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.button-icon {
  transition: transform var(--transition-base);
}

.icon-animated {
  animation: iconBounce 0.6s ease-in-out;
}

@keyframes iconBounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-4px);
  }
}

.button-text {
  position: relative;
}

/* Primary variant */
.variant-primary {
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  color: #000;
  box-shadow: 0 4px 20px rgba(255, 209, 0, 0.3);
}

.variant-primary:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 30px rgba(255, 209, 0, 0.4);
}

.variant-primary:active {
  transform: translateY(-1px);
}

/* Secondary variant */
.variant-secondary {
  background: transparent;
  color: var(--color-primary);
  border: 2px solid var(--color-primary);
}

.variant-secondary:hover {
  background: rgba(255, 209, 0, 0.1);
  transform: translateY(-2px);
}

/* Animated background */
.button-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    45deg,
    transparent 30%,
    rgba(255, 255, 255, 0.3) 50%,
    transparent 70%
  );
  background-size: 200% 200%;
  opacity: 0;
  transition: opacity var(--transition-base);
  z-index: 1;
}

.animated-button:hover .button-bg {
  opacity: 1;
  animation: shimmerMove 1.5s ease-in-out infinite;
}

@keyframes shimmerMove {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

/* Ripple effect */
.ripple-effect {
  position: absolute;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.6);
  transform: translate(-50%, -50%) scale(0);
  animation: rippleAnimation 0.6s ease-out;
  pointer-events: none;
  z-index: 1;
}

@keyframes rippleAnimation {
  to {
    transform: translate(-50%, -50%) scale(20);
    opacity: 0;
  }
}

/* Loading state */
.is-loading {
  pointer-events: none;
  opacity: 0.7;
}

.is-loading .button-icon {
  animation: rotate 1s linear infinite;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* Responsive */
@media (max-width: 768px) {
  .animated-button {
    padding: 0.875rem 2rem;
    font-size: var(--font-size-base);
  }
}
</style>
