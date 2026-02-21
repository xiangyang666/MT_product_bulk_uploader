<template>
  <div class="loading-spinner" :style="{ width: `${size}px`, height: `${size}px` }">
    <!-- Spinner type -->
    <svg
      v-if="type === 'spinner'"
      class="spinner"
      viewBox="0 0 50 50"
    >
      <circle
        class="spinner-path"
        cx="25"
        cy="25"
        r="20"
        fill="none"
        :stroke="color"
        stroke-width="4"
      />
    </svg>

    <!-- Pulse type -->
    <svg
      v-else-if="type === 'pulse'"
      class="pulse"
      viewBox="0 0 50 50"
    >
      <circle
        cx="25"
        cy="25"
        r="20"
        :fill="color"
      />
    </svg>

    <!-- Dots type -->
    <div v-else-if="type === 'dots'" class="dots">
      <div class="dot" :style="{ backgroundColor: color }" />
      <div class="dot" :style="{ backgroundColor: color }" />
      <div class="dot" :style="{ backgroundColor: color }" />
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  size?: number
  color?: string
  type?: 'spinner' | 'pulse' | 'dots'
}

withDefaults(defineProps<Props>(), {
  size: 48,
  color: '#FFD100',
  type: 'spinner'
})
</script>

<style scoped>
.loading-spinner {
  display: inline-block;
}

/* Spinner animation */
.spinner {
  animation: rotate 2s linear infinite;
}

.spinner-path {
  stroke-linecap: round;
  stroke-dasharray: 1, 150;
  stroke-dashoffset: 0;
  animation: dash 1.5s ease-in-out infinite;
}

@keyframes rotate {
  100% {
    transform: rotate(360deg);
  }
}

@keyframes dash {
  0% {
    stroke-dasharray: 1, 150;
    stroke-dashoffset: 0;
  }
  50% {
    stroke-dasharray: 90, 150;
    stroke-dashoffset: -35;
  }
  100% {
    stroke-dasharray: 90, 150;
    stroke-dashoffset: -124;
  }
}

/* Pulse animation */
.pulse {
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.5;
    transform: scale(0.8);
  }
}

/* Dots animation */
.dots {
  display: flex;
  gap: 8px;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  animation: dotBounce 1.4s ease-in-out infinite;
}

.dot:nth-child(1) {
  animation-delay: -0.32s;
}

.dot:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes dotBounce {
  0%, 80%, 100% {
    transform: scale(0);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}
</style>
