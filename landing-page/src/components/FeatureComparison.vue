<template>
  <div class="feature-comparison">
    <h2 v-if="title" class="comparison-title">{{ title }}</h2>
    <div class="features-list">
      <div 
        v-for="(feature, index) in features" 
        :key="index"
        class="feature-item"
      >
        <span class="check-icon" aria-label="支持">✓</span>
        <span class="feature-name">{{ feature.name }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Feature {
  name: string
  [key: string]: any
}

interface Props {
  features: Feature[]
  columns: string[]
  title?: string
}

defineProps<Props>()
</script>

<style scoped>
.feature-comparison {
  padding: var(--spacing-lg) 0;
}

.comparison-title {
  text-align: center;
  margin-bottom: var(--spacing-lg);
  color: var(--color-text-primary);
  font-weight: 700;
}

.features-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: var(--spacing-md);
  max-width: 1000px;
  margin: 0 auto;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-md);
  background-color: var(--color-bg-secondary);
  border-radius: 16px;
  transition: all var(--transition-base);
  border: 2px solid var(--color-border);
}

.feature-item:hover {
  border-color: var(--color-primary);
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.check-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  color: var(--color-text-primary);
  font-weight: bold;
  font-size: var(--font-size-base);
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(255, 209, 0, 0.3);
}

.feature-name {
  color: var(--color-text-primary);
  font-weight: 600;
  font-size: var(--font-size-base);
}

/* Responsive */
@media (max-width: 768px) {
  .features-list {
    grid-template-columns: 1fr;
    gap: var(--spacing-sm);
  }
  
  .feature-item {
    padding: var(--spacing-sm) var(--spacing-md);
  }
  
  .check-icon {
    width: 28px;
    height: 28px;
    font-size: var(--font-size-sm);
  }
  
  .feature-name {
    font-size: var(--font-size-sm);
  }
}
</style>
