<template>
  <div class="features-grid">
    <div
      v-for="(feature, index) in features"
      :key="index"
      class="feature-card"
      :style="{ animationDelay: `${index * 0.1}s` }"
    >
      <div class="feature-icon">
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
          <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
          <polyline points="22 4 12 14.01 9 11.01"></polyline>
        </svg>
      </div>
      <h3 class="feature-title">{{ feature.name }}</h3>
      <div class="feature-glow"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Feature {
  name: string
}

interface Props {
  features: Feature[]
}

defineProps<Props>()
</script>

<style scoped>
.features-grid {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 24px;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px 0;
}

.feature-card {
  flex: 0 0 calc(25% - 18px);
  max-width: calc(25% - 18px);
  min-width: 260px;
}

.feature-card {
  position: relative;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border-radius: 20px;
  padding: 32px 24px;
  border: 1px solid rgba(255, 255, 255, 0.8);
  box-shadow: 
    0 4px 16px rgba(0, 0, 0, 0.06),
    0 1px 4px rgba(0, 0, 0, 0.04),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  animation: fadeInUp 0.6s ease-out backwards;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.feature-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.8) 50%,
    transparent
  );
}

.feature-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 
    0 12px 32px rgba(255, 209, 0, 0.2),
    0 4px 16px rgba(0, 0, 0, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 1);
  border-color: rgba(255, 209, 0, 0.3);
}

.feature-card:hover .feature-glow {
  opacity: 1;
}

.feature-card:hover .feature-icon {
  transform: scale(1.1) rotateY(10deg);
  background: linear-gradient(135deg, #FFD100 0%, #FFC300 100%);
}

.feature-card:hover .feature-icon svg {
  color: #1d1d1f;
}

.feature-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 150%;
  height: 150%;
  background: radial-gradient(
    circle at center,
    rgba(255, 209, 0, 0.15) 0%,
    transparent 70%
  );
  opacity: 0;
  transition: opacity 0.4s ease;
  pointer-events: none;
}

.feature-icon {
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f5f7 0%, #ffffff 100%);
  border-radius: 16px;
  box-shadow: 
    0 4px 12px rgba(0, 0, 0, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);
  transition: all 0.3s ease;
  position: relative;
  z-index: 1;
}

.feature-icon svg {
  width: 32px;
  height: 32px;
  color: #FFD100;
  transition: color 0.3s ease;
}

.feature-title {
  font-size: 18px;
  font-weight: 600;
  color: #1d1d1f;
  margin: 0;
  text-align: center;
  letter-spacing: -0.3px;
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'Segoe UI', sans-serif;
  position: relative;
  z-index: 1;
}

/* Responsive */
@media (max-width: 968px) {
  .feature-card {
    flex: 0 0 calc(50% - 12px);
    max-width: calc(50% - 12px);
    min-width: 240px;
  }
}

@media (max-width: 768px) {
  .features-grid {
    gap: 16px;
  }
  
  .feature-card {
    flex: 0 0 calc(50% - 8px);
    max-width: calc(50% - 8px);
    min-width: 200px;
    padding: 24px 20px;
    border-radius: 16px;
  }
  
  .feature-icon {
    width: 56px;
    height: 56px;
  }
  
  .feature-icon svg {
    width: 28px;
    height: 28px;
  }
  
  .feature-title {
    font-size: 16px;
  }
}

@media (max-width: 480px) {
  .feature-card {
    flex: 0 0 100%;
    max-width: 100%;
    min-width: auto;
  }
}
</style>

