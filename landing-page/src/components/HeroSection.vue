<template>
  <section class="hero-section">
    <FloatingShapes :shapeCount="6" />
    <div class="hero-container">
      <h1 class="hero-title slide-up">{{ title }}</h1>
      <p class="hero-subtitle slide-up stagger-1">{{ subtitle }}</p>
      <p v-if="description" class="hero-description slide-up stagger-2">{{ description }}</p>
      <AnimatedButton
        v-if="ctaText && ctaLink"
        :text="ctaText"
        :href="ctaLink"
        icon="download"
        class="slide-up stagger-3"
      />
    </div>
  </section>
</template>

<script setup lang="ts">
import FloatingShapes from './animations/FloatingShapes.vue'
import AnimatedButton from './animations/AnimatedButton.vue'

interface Props {
  title: string
  subtitle: string
  description?: string
  ctaText?: string
  ctaLink?: string
}

defineProps<Props>()
</script>

<style scoped>
.hero-section {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-lg) var(--spacing-md);
  background: linear-gradient(
    180deg,
    #f5f5f7 0%,
    #ffffff 50%,
    #f5f5f7 100%
  );
  position: relative;
  overflow: hidden;
}

.hero-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 20% 20%, rgba(255, 209, 0, 0.08) 0%, transparent 50%),
    radial-gradient(circle at 80% 80%, rgba(0, 0, 0, 0.03) 0%, transparent 50%);
  pointer-events: none;
}

.hero-section::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 800px;
  height: 800px;
  background: radial-gradient(
    circle,
    rgba(255, 209, 0, 0.05) 0%,
    transparent 70%
  );
  border-radius: 50%;
  pointer-events: none;
  animation: pulse 8s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: translate(-50%, -50%) scale(1);
    opacity: 0.5;
  }
  50% {
    transform: translate(-50%, -50%) scale(1.1);
    opacity: 0.8;
  }
}

.hero-container {
  max-width: 900px;
  text-align: center;
  position: relative;
  z-index: 1;
}

.hero-title {
  font-size: 64px;
  font-weight: 700;
  color: #1d1d1f;
  margin-bottom: var(--spacing-md);
  line-height: 1.1;
  letter-spacing: -1.5px;
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'Segoe UI', sans-serif;
  background: linear-gradient(135deg, #1d1d1f 0%, #4a4a4a 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-subtitle {
  font-size: 28px;
  color: #FFD100;
  margin-bottom: var(--spacing-sm);
  font-weight: 600;
  letter-spacing: -0.5px;
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'Segoe UI', sans-serif;
}

.hero-description {
  font-size: 19px;
  color: #6e6e73;
  margin-bottom: var(--spacing-lg);
  line-height: 1.6;
  letter-spacing: -0.3px;
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'Segoe UI', sans-serif;
  max-width: 700px;
  margin-left: auto;
  margin-right: auto;
}

.hero-cta {
  display: inline-block;
  padding: 1rem 2.5rem;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  color: var(--color-text-primary);
  font-size: var(--font-size-lg);
  font-weight: 700;
  border-radius: 50px;
  transition: all var(--transition-base);
  box-shadow: 0 4px 20px rgba(255, 209, 0, 0.3);
}

.hero-cta:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 30px rgba(255, 209, 0, 0.4);
}

.hero-cta:active {
  transform: translateY(-1px);
}

/* Responsive typography */
@media (max-width: 1024px) {
  .hero-title {
    font-size: 52px;
    letter-spacing: -1.2px;
  }
  
  .hero-subtitle {
    font-size: 24px;
  }
  
  .hero-description {
    font-size: 17px;
  }
}

@media (max-width: 768px) {
  .hero-section {
    min-height: 100vh;
    padding: var(--spacing-md) var(--spacing-sm);
  }
  
  .hero-title {
    font-size: 40px;
    letter-spacing: -1px;
  }
  
  .hero-subtitle {
    font-size: 20px;
  }
  
  .hero-description {
    font-size: 16px;
  }
  
  .hero-cta {
    padding: 0.875rem 2rem;
    font-size: var(--font-size-base);
  }
}
</style>
