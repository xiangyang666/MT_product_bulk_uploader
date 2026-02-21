<template>
  <div class="code-snippet">
    <div v-if="title" class="code-header">
      <span class="code-title">{{ title }}</span>
      <button 
        class="copy-button" 
        @click="copyCode"
        :aria-label="copied ? '已复制' : '复制代码'"
      >
        {{ copied ? '✓ 已复制' : '复制' }}
      </button>
    </div>
    <pre 
      class="code-block" 
      :class="getLanguageClass(language)"
    ><code v-html="highlightedCode"></code></pre>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { highlightCode, getLanguageClass, copyToClipboard } from '@/utils/highlight'

interface Props {
  code: string
  language?: string
  title?: string
  showLineNumbers?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  language: 'javascript',
  showLineNumbers: true
})

const copied = ref(false)

const highlightedCode = computed(() => {
  return highlightCode(props.code, props.language)
})

const copyCode = async () => {
  const success = await copyToClipboard(props.code)
  if (success) {
    copied.value = true
    setTimeout(() => {
      copied.value = false
    }, 2000)
  }
}
</script>

<style scoped>
.code-snippet {
  background-color: var(--color-bg-secondary);
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-lg);
  margin: var(--spacing-md) 0;
  border: 1px solid var(--color-border);
}

.code-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-sm) var(--spacing-md);
  background-color: var(--color-bg-tertiary);
  border-bottom: 1px solid var(--color-border);
}

.code-title {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  font-weight: 600;
}

.copy-button {
  padding: 0.5rem 1rem;
  background-color: var(--color-bg-secondary);
  color: var(--color-primary-dark);
  font-size: var(--font-size-sm);
  font-weight: 600;
  border: 1px solid var(--color-primary);
  border-radius: 20px;
  transition: all var(--transition-fast);
  cursor: pointer;
}

.copy-button:hover {
  background-color: var(--color-primary);
  color: var(--color-text-primary);
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.code-block {
  padding: var(--spacing-md);
  margin: 0;
  overflow-x: auto;
  font-family: var(--font-family-code);
  font-size: var(--font-size-sm);
  line-height: 1.6;
  color: var(--color-text-primary);
  background-color: #FAFBFC;
}

.code-block code {
  font-family: inherit;
  font-size: inherit;
}

/* Syntax highlighting styles - Light theme */
.code-block :deep(.token.comment),
.code-block :deep(.token.prolog),
.code-block :deep(.token.doctype),
.code-block :deep(.token.cdata) {
  color: var(--color-syntax-comment);
}

.code-block :deep(.token.punctuation) {
  color: var(--color-text-secondary);
}

.code-block :deep(.token.property),
.code-block :deep(.token.tag),
.code-block :deep(.token.boolean),
.code-block :deep(.token.number),
.code-block :deep(.token.constant),
.code-block :deep(.token.symbol),
.code-block :deep(.token.deleted) {
  color: var(--color-syntax-number);
}

.code-block :deep(.token.selector),
.code-block :deep(.token.attr-name),
.code-block :deep(.token.string),
.code-block :deep(.token.char),
.code-block :deep(.token.builtin),
.code-block :deep(.token.inserted) {
  color: var(--color-syntax-string);
}

.code-block :deep(.token.operator),
.code-block :deep(.token.entity),
.code-block :deep(.token.url),
.code-block :deep(.language-css .token.string),
.code-block :deep(.style .token.string) {
  color: var(--color-text-primary);
}

.code-block :deep(.token.atrule),
.code-block :deep(.token.attr-value),
.code-block :deep(.token.keyword) {
  color: var(--color-syntax-keyword);
}

.code-block :deep(.token.function),
.code-block :deep(.token.class-name) {
  color: var(--color-syntax-function);
}

.code-block :deep(.token.regex),
.code-block :deep(.token.important),
.code-block :deep(.token.variable) {
  color: var(--color-primary-dark);
}

/* Scrollbar for code block */
.code-block::-webkit-scrollbar {
  height: 8px;
}

.code-block::-webkit-scrollbar-track {
  background: var(--color-bg-tertiary);
}

.code-block::-webkit-scrollbar-thumb {
  background: var(--color-border);
  border-radius: var(--radius-sm);
}

.code-block::-webkit-scrollbar-thumb:hover {
  background: var(--color-primary);
}

/* Responsive */
@media (max-width: 768px) {
  .code-header {
    padding: var(--spacing-xs) var(--spacing-sm);
  }
  
  .code-block {
    padding: var(--spacing-sm);
    font-size: 0.8rem;
  }
  
  .copy-button {
    padding: 0.375rem 0.75rem;
    font-size: 0.75rem;
  }
}
</style>
