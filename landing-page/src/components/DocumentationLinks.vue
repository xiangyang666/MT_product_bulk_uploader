<template>
  <div class="documentation-links">
    <h2 v-if="title" class="docs-title">{{ title }}</h2>
    <div class="accordion">
      <div 
        v-for="(link, index) in links" 
        :key="link.title"
        class="accordion-item"
        :class="{ 'is-active': activeIndex === index }"
      >
        <button 
          class="accordion-header"
          @click="toggleAccordion(index)"
          :aria-expanded="activeIndex === index"
        >
          <div class="header-content">
            <Icon :name="link.icon || 'book'" :size="32" class="header-icon" />
            <h3 class="accordion-title">{{ link.title }}</h3>
          </div>
          <Icon name="arrow-right" :size="20" class="accordion-arrow" :class="{ 'is-open': activeIndex === index }" />
        </button>
        <transition name="accordion">
          <div v-show="activeIndex === index" class="accordion-content">
            <p v-if="link.description" class="content-description">
              {{ link.description }}
            </p>
            <a 
              :href="link.url"
              class="content-link"
              :target="link.external ? '_blank' : '_self'"
              :rel="link.external ? 'noopener noreferrer' : ''"
            >
              查看详情
              <Icon name="arrow-right" :size="16" />
            </a>
          </div>
        </transition>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import Icon from './Icon.vue'

interface DocLink {
  title: string
  url: string
  icon?: 'rocket' | 'book' | 'server' | 'help' | 'code' | 'api' | 'guide' | 'tutorial'
  description?: string
  external?: boolean
}

interface Props {
  links: DocLink[]
  title?: string
}

defineProps<Props>()

const activeIndex = ref<number | null>(0)

const toggleAccordion = (index: number) => {
  activeIndex.value = activeIndex.value === index ? null : index
}
</script>

<style scoped>
.documentation-links {
  padding: var(--spacing-lg) 0;
}

.docs-title {
  text-align: center;
  margin-bottom: var(--spacing-lg);
  color: var(--color-text-primary);
}

.accordion {
  max-width: 800px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.accordion-item {
  background-color: var(--color-bg-secondary);
  border: 2px solid var(--color-border);
  border-radius: var(--radius-md);
  overflow: hidden;
  transition: all var(--transition-base);
}

.accordion-item.is-active {
  border-color: var(--color-primary);
  box-shadow: 0 4px 16px rgba(255, 209, 0, 0.15);
}

.accordion-header {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-md) var(--spacing-lg);
  background: transparent;
  border: none;
  cursor: pointer;
  transition: background-color var(--transition-fast);
  text-align: left;
}

.accordion-header:hover {
  background-color: var(--color-bg-tertiary);
}

.header-content {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.header-icon {
  color: var(--color-primary);
  flex-shrink: 0;
}

.accordion-title {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0;
}

.accordion-arrow {
  color: var(--color-primary);
  transition: transform var(--transition-base);
  flex-shrink: 0;
}

.accordion-arrow.is-open {
  transform: rotate(90deg);
}

.accordion-content {
  padding: 0 var(--spacing-lg) var(--spacing-md) var(--spacing-lg);
  margin-left: calc(32px + var(--spacing-md));
}

.content-description {
  font-size: var(--font-size-base);
  color: var(--color-text-secondary);
  line-height: 1.6;
  margin-bottom: var(--spacing-md);
}

.content-link {
  display: inline-flex;
  align-items: center;
  gap: var(--spacing-xs);
  color: var(--color-primary);
  text-decoration: none;
  font-weight: 600;
  font-size: var(--font-size-base);
  transition: all var(--transition-fast);
}

.content-link:hover {
  gap: var(--spacing-sm);
  text-decoration: underline;
}

.content-link:hover :deep(.icon) {
  transform: translateX(4px);
}

/* Accordion transition */
.accordion-enter-active,
.accordion-leave-active {
  transition: all 0.3s ease;
  overflow: hidden;
}

.accordion-enter-from,
.accordion-leave-to {
  opacity: 0;
  max-height: 0;
}

.accordion-enter-to,
.accordion-leave-from {
  opacity: 1;
  max-height: 200px;
}

/* Responsive */
@media (max-width: 768px) {
  .accordion {
    gap: var(--spacing-xs);
  }
  
  .accordion-header {
    padding: var(--spacing-sm) var(--spacing-md);
  }
  
  .header-content {
    gap: var(--spacing-sm);
  }
  
  .accordion-title {
    font-size: var(--font-size-base);
  }
  
  .accordion-content {
    padding: 0 var(--spacing-md) var(--spacing-sm) var(--spacing-md);
    margin-left: calc(32px + var(--spacing-sm));
  }
  
  .content-description {
    font-size: var(--font-size-sm);
  }
  
  .content-link {
    font-size: var(--font-size-sm);
  }
}
</style>
