<template>
  <svg
    class="icon"
    :width="size"
    :height="size"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    stroke-width="2"
    stroke-linecap="round"
    stroke-linejoin="round"
  >
    <!-- Rocket -->
    <g v-if="name === 'rocket'">
      <path d="M4.5 16.5c-1.5 1.25-2 5-2 5s3.75-.5 5-2c.625-.75 1-1.5 1-2.5 0-1-.75-1.375-1.5-1.375-.75 0-1.75.375-2.5 1.375z" />
      <path d="m12 15-3-3a22 22 0 0 1 2-3.95A12.88 12.88 0 0 1 22 2c0 2.72-.78 7.5-6 11a22.35 22.35 0 0 1-4 2z" />
      <path d="M9 12H4s.55-3.03 2-4c1.62-1.08 5 0 5 0" />
      <path d="M12 15v5s3.03-.55 4-2c1.08-1.62 0-5 0-5" />
    </g>

    <!-- Book -->
    <g v-else-if="name === 'book'">
      <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" />
      <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z" />
    </g>

    <!-- Server -->
    <g v-else-if="name === 'server'">
      <rect x="2" y="2" width="20" height="8" rx="2" ry="2" />
      <rect x="2" y="14" width="20" height="8" rx="2" ry="2" />
      <line x1="6" y1="6" x2="6.01" y2="6" />
      <line x1="6" y1="18" x2="6.01" y2="18" />
    </g>

    <!-- Help -->
    <g v-else-if="name === 'help'">
      <circle cx="12" cy="12" r="10" />
      <path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3" />
      <line x1="12" y1="17" x2="12.01" y2="17" />
    </g>

    <!-- Code -->
    <g v-else-if="name === 'code'">
      <polyline points="16 18 22 12 16 6" />
      <polyline points="8 6 2 12 8 18" />
    </g>

    <!-- API / Zap -->
    <g v-else-if="name === 'api'">
      <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2" />
    </g>

    <!-- Guide / BookOpen -->
    <g v-else-if="name === 'guide'">
      <path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z" />
      <path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z" />
    </g>

    <!-- Tutorial / GraduationCap -->
    <g v-else-if="name === 'tutorial'">
      <path d="M22 10v6M2 10l10-5 10 5-10 5z" />
      <path d="M6 12v5c3 3 9 3 12 0v-5" />
    </g>

    <!-- Download -->
    <g v-else-if="name === 'download'">
      <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
      <polyline points="7 10 12 15 17 10" />
      <line x1="12" y1="15" x2="12" y2="3" />
    </g>

    <!-- Arrow Right -->
    <g v-else-if="name === 'arrow-right'">
      <line x1="5" y1="12" x2="19" y2="12" />
      <polyline points="12 5 19 12 12 19" />
    </g>

    <!-- Play -->
    <g v-else-if="name === 'play'">
      <circle cx="12" cy="12" r="10" />
      <polygon points="10 8 16 12 10 16 10 8" />
    </g>

    <!-- Default: File -->
    <g v-else>
      <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
      <polyline points="14 2 14 8 20 8" />
      <line x1="16" y1="13" x2="8" y2="13" />
      <line x1="16" y1="17" x2="8" y2="17" />
      <polyline points="10 9 9 9 8 9" />
    </g>
  </svg>
</template>

<script setup lang="ts">
interface Props {
  name: 'rocket' | 'book' | 'server' | 'help' | 'code' | 'api' | 'guide' | 'tutorial' | 'download' | 'arrow-right' | 'play'
  size?: number | string
}

withDefaults(defineProps<Props>(), {
  size: 24
})
</script>

<style scoped>
.icon {
  display: inline-block;
  vertical-align: middle;
  flex-shrink: 0;
}
</style>
