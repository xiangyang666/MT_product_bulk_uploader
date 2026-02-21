<template>
  <div class="floating-shapes">
    <svg
      v-for="shape in shapes"
      :key="shape.id"
      class="floating-shape"
      :class="`float-${shape.speed}`"
      :style="{
        left: `${shape.x}%`,
        top: `${shape.y}%`,
        width: `${shape.size}px`,
        height: `${shape.size}px`,
        animationDelay: `${shape.delay}s`,
        opacity: shape.opacity,
        filter: `blur(${shape.blur}px)`,
        transform: `translateY(${parallaxOffset * shape.parallaxSpeed}px)`
      }"
      viewBox="0 0 200 200"
    >
      <defs>
        <linearGradient :id="`gradient-${shape.id}`" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" :stop-color="shape.color" />
          <stop offset="100%" :stop-color="shape.colorEnd" />
        </linearGradient>
      </defs>

      <!-- Circle -->
      <circle
        v-if="shape.type === 'circle'"
        cx="100"
        cy="100"
        r="80"
        :fill="`url(#gradient-${shape.id})`"
      />

      <!-- Triangle -->
      <polygon
        v-else-if="shape.type === 'triangle'"
        points="100,20 20,180 180,180"
        :fill="`url(#gradient-${shape.id})`"
      />

      <!-- Square -->
      <rect
        v-else-if="shape.type === 'square'"
        x="30"
        y="30"
        width="140"
        height="140"
        rx="20"
        :fill="`url(#gradient-${shape.id})`"
      />

      <!-- Blob (organic shape) -->
      <path
        v-else-if="shape.type === 'blob'"
        d="M100,20 C140,20 180,60 180,100 C180,140 140,180 100,180 C60,180 20,140 20,100 C20,60 60,20 100,20 Z"
        :fill="`url(#gradient-${shape.id})`"
      />
    </svg>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useParallax } from '../../composables/useParallax'

interface Props {
  shapeCount?: number
  colors?: string[]
  speed?: 'slow' | 'medium' | 'fast'
}

const props = withDefaults(defineProps<Props>(), {
  shapeCount: 5,
  colors: () => ['#FFD100', '#FFDC33', '#E6BC00'],
  speed: 'medium'
})

interface Shape {
  id: string
  type: 'circle' | 'triangle' | 'square' | 'blob'
  x: number
  y: number
  size: number
  color: string
  colorEnd: string
  speed: 'slow' | 'medium' | 'fast'
  delay: number
  opacity: number
  blur: number
  parallaxSpeed: number
}

const shapes = ref<Shape[]>([])
const { offset: parallaxOffset } = useParallax({ speed: 0.3 })

const shapeTypes: Shape['type'][] = ['circle', 'triangle', 'square', 'blob']
const speedOptions: Shape['speed'][] = ['slow', 'medium', 'fast']

const generateShapes = () => {
  const generated: Shape[] = []

  for (let i = 0; i < props.shapeCount; i++) {
    const colorIndex = Math.floor(Math.random() * props.colors.length)
    const colorEndIndex = (colorIndex + 1) % props.colors.length

    generated.push({
      id: `shape-${i}`,
      type: shapeTypes[Math.floor(Math.random() * shapeTypes.length)]!,
      x: Math.random() * 100,
      y: Math.random() * 100,
      size: 100 + Math.random() * 200,
      color: props.colors[colorIndex]!,
      colorEnd: props.colors[colorEndIndex]!,
      speed: speedOptions[Math.floor(Math.random() * speedOptions.length)]!,
      delay: Math.random() * 5,
      opacity: 0.15 + Math.random() * 0.15,
      blur: Math.random() * 30,
      parallaxSpeed: 0.1 + Math.random() * 0.3
    })
  }

  shapes.value = generated
}

onMounted(() => {
  generateShapes()
})
</script>

<style scoped>
.floating-shapes {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  pointer-events: none;
  z-index: 0;
}

.floating-shape {
  position: absolute;
  will-change: transform;
  transition: transform 0.1s ease-out;
}

.float-slow {
  animation: floatSlow 12s ease-in-out infinite;
}

.float-medium {
  animation: floatMedium 8s ease-in-out infinite;
}

.float-fast {
  animation: float 6s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(5deg);
  }
}

@keyframes floatMedium {
  0%, 100% {
    transform: translate(0, 0) rotate(0deg);
  }
  50% {
    transform: translate(-15px, -20px) rotate(-5deg);
  }
}

@keyframes floatSlow {
  0%, 100% {
    transform: translate(0, 0) rotate(0deg);
  }
  33% {
    transform: translate(10px, -15px) rotate(3deg);
  }
  66% {
    transform: translate(-10px, -10px) rotate(-3deg);
  }
}
</style>
