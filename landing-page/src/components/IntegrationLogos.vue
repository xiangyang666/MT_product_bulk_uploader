<template>
  <div class="integration-logos">
    <h2 v-if="title" class="logos-title">{{ title }}</h2>
    <div class="logos-grid">
      <div 
        v-for="integration in integrations" 
        :key="integration.name"
        class="logo-item hover-scale"
      >
        <img 
          :src="integration.logo" 
          :alt="`${integration.name} logo`"
          class="logo-image"
          loading="lazy"
        />
        <span class="logo-name">{{ integration.name }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Integration {
  name: string
  logo: string
}

interface Props {
  integrations: Integration[]
  title?: string
}

defineProps<Props>()
</script>

<style scoped>
.integration-logos {
  padding: var(--spacing-lg) 0;
}

.logos-title {
  text-align: center;
  margin-bottom: var(--spacing-lg);
  color: var(--color-text-primary);
}

.logos-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: var(--spacing-lg);
  max-width: 1200px;
  margin: 0 auto;
}

.logo-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-md);
  background-color: var(--color-bg-secondary);
  border-radius: var(--radius-md);
  transition: all var(--transition-base);
  cursor: pointer;
  min-height: 150px;
}

.logo-item:hover {
  background-color: var(--color-bg-tertiary);
  box-shadow: 0 4px 12px rgba(255, 209, 0, 0.2);
}

.logo-image {
  width: 80px;
  height: 80px;
  object-fit: contain;
  filter: grayscale(100%) brightness(1.5);
  transition: filter var(--transition-base);
  margin-bottom: var(--spacing-sm);
}

.logo-item:hover .logo-image {
  filter: grayscale(0%) brightness(1);
}

.logo-name {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  text-align: center;
  font-weight: 500;
  transition: color var(--transition-fast);
}

.logo-item:hover .logo-name {
  color: var(--color-primary);
}

/* Responsive */
@media (max-width: 1024px) {
  .logos-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: var(--spacing-md);
  }
}

@media (max-width: 768px) {
  .logos-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: var(--spacing-sm);
  }
  
  .logo-item {
    padding: var(--spacing-sm);
    min-height: 120px;
  }
  
  .logo-image {
    width: 60px;
    height: 60px;
  }
  
  .logo-name {
    font-size: var(--font-size-xs);
  }
}

@media (max-width: 480px) {
  .logos-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
