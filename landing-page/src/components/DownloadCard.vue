<template>
  <div class="download-card" :class="platform">
    <div class="card-glow"></div>
    <div class="card-content">
      <div class="platform-icon">
        <img :src="iconPath" :alt="`${platform} logo`" />
      </div>
      <h3 class="platform-name">{{ platformName }}</h3>
      <div class="platform-details">
        <div class="detail-item">
          <svg class="detail-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <rect x="2" y="3" width="20" height="14" rx="2" ry="2"></rect>
            <line x1="8" y1="21" x2="16" y2="21"></line>
            <line x1="12" y1="17" x2="12" y2="21"></line>
          </svg>
          <span class="detail-text">{{ systemRequirement }}</span>
        </div>
        <div class="detail-item">
          <svg class="detail-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"></path>
            <polyline points="3.27 6.96 12 12.01 20.73 6.96"></polyline>
            <line x1="12" y1="22.08" x2="12" y2="12"></line>
          </svg>
          <span class="detail-text">{{ fileSize }}</span>
        </div>
        <div class="detail-item">
          <svg class="detail-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
            <polyline points="22 4 12 14.01 9 11.01"></polyline>
          </svg>
          <span class="detail-text">完全免费</span>
        </div>
      </div>
      <button class="download-button" @click="handleDownload" :disabled="downloading">
        <span class="button-icon">
          <svg v-if="!downloading" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
            <polyline points="7 10 12 15 17 10"></polyline>
            <line x1="12" y1="15" x2="12" y2="3"></line>
          </svg>
          <svg v-else class="spinner" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
            <line x1="12" y1="2" x2="12" y2="6"></line>
            <line x1="12" y1="18" x2="12" y2="22"></line>
            <line x1="4.93" y1="4.93" x2="7.76" y2="7.76"></line>
            <line x1="16.24" y1="16.24" x2="19.07" y2="19.07"></line>
            <line x1="2" y1="12" x2="6" y2="12"></line>
            <line x1="18" y1="12" x2="22" y2="12"></line>
            <line x1="4.93" y1="19.07" x2="7.76" y2="16.24"></line>
            <line x1="16.24" y1="7.76" x2="19.07" y2="4.93"></line>
          </svg>
        </span>
        <span class="button-text">{{ downloading ? '准备下载...' : '立即下载' }}</span>
      </button>
      <div class="version-footer">
        <p class="version-info">版本 {{ version }}</p>
        <p v-if="downloadCount" class="download-count">{{ downloadCount }} 次下载</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { getDownloadUrl } from '@/utils/api'

interface Props {
  platform: 'windows' | 'mac'
  platformName: string
  systemRequirement: string
  fileSize: string
  version: string
  versionId?: number
  releaseNotes?: string
  downloadCount?: number
}

const props = defineProps<Props>()

const iconPath = `/images/${props.platform === 'mac' ? 'apple' : 'windows'}.svg`
const downloading = ref(false)

const handleDownload = async () => {
  if (!props.versionId) {
    alert('暂无可用版本,敬请期待!')
    return
  }
  
  try {
    downloading.value = true
    const url = await getDownloadUrl(props.versionId)
    
    if (url) {
      // 创建隐藏的 a 标签触发下载
      const link = document.createElement('a')
      link.href = url
      link.download = ''
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
    } else {
      alert('获取下载链接失败,请稍后重试')
    }
  } catch (error) {
    console.error('下载失败:', error)
    alert('下载失败,请稍后重试')
  } finally {
    downloading.value = false
  }
}
</script>

<style scoped>
.download-card {
  position: relative;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border-radius: 28px;
  padding: 48px 36px;
  border: 1px solid rgba(255, 255, 255, 0.8);
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.08),
    0 2px 8px rgba(0, 0, 0, 0.04),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}

.download-card::before {
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

.download-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 
    0 20px 60px rgba(0, 0, 0, 0.12),
    0 8px 24px rgba(0, 0, 0, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 1);
}

.download-card.windows:hover .card-glow {
  opacity: 1;
  background: radial-gradient(
    circle at center,
    rgba(0, 120, 215, 0.15) 0%,
    transparent 70%
  );
}

.download-card.mac:hover .card-glow {
  opacity: 1;
  background: radial-gradient(
    circle at center,
    rgba(0, 0, 0, 0.08) 0%,
    transparent 70%
  );
}

.card-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 120%;
  height: 120%;
  opacity: 0;
  transition: opacity 0.4s ease;
  pointer-events: none;
}

.card-content {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
}

.platform-icon {
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f5f7 0%, #ffffff 100%);
  border-radius: 20px;
  box-shadow: 
    0 4px 16px rgba(0, 0, 0, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);
  transition: transform 0.3s ease;
}

.download-card:hover .platform-icon {
  transform: scale(1.1) rotateY(10deg);
}

.platform-icon img {
  width: 48px;
  height: 48px;
  object-fit: contain;
}

.download-card.windows .platform-icon img {
  color: #0078d7;
  filter: brightness(0) saturate(100%) invert(38%) sepia(98%) saturate(1789%) hue-rotate(188deg) brightness(95%) contrast(101%);
}

.download-card.mac .platform-icon img {
  color: #000000;
  filter: brightness(0) saturate(100%);
}

.platform-name {
  font-size: 28px;
  font-weight: 700;
  color: #1d1d1f;
  margin: 0;
  letter-spacing: -0.5px;
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'Segoe UI', sans-serif;
}

.platform-details {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
}

.detail-item:hover {
  background: rgba(255, 255, 255, 0.8);
  transform: translateX(4px);
}

.detail-icon {
  width: 20px;
  height: 20px;
  color: #6e6e73;
  flex-shrink: 0;
}

.detail-text {
  font-size: 15px;
  color: #6e6e73;
  font-weight: 500;
  letter-spacing: -0.2px;
}

.download-button {
  width: 100%;
  padding: 16px 32px;
  background: linear-gradient(180deg, #FFD100 0%, #FFC300 100%);
  color: #1d1d1f;
  border: none;
  border-radius: 14px;
  font-size: 17px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 
    0 4px 16px rgba(255, 209, 0, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
  letter-spacing: -0.2px;
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'Segoe UI', sans-serif;
}

.download-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.download-button:not(:disabled):hover {
  background: linear-gradient(180deg, #FFC300 0%, #FFD100 100%);
  transform: translateY(-2px);
  box-shadow: 
    0 8px 24px rgba(255, 209, 0, 0.5),
    inset 0 1px 0 rgba(255, 255, 255, 0.4);
}

.download-button:not(:disabled):active {
  transform: translateY(0);
  box-shadow: 
    0 2px 8px rgba(255, 209, 0, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

.button-icon {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.button-icon svg:not(.spinner) {
  width: 100%;
  height: 100%;
  animation: bounce 2s infinite;
}

.button-icon .spinner {
  width: 100%;
  height: 100%;
  animation: spin 1s linear infinite;
}

@keyframes bounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-4px);
  }
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.button-text {
  line-height: 1;
}

.version-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  gap: 12px;
}

.version-info {
  font-size: 13px;
  color: #86868b;
  margin: 0;
  font-weight: 500;
  letter-spacing: -0.1px;
}

.download-count {
  font-size: 12px;
  color: #a1a1a6;
  margin: 0;
  font-weight: 500;
  letter-spacing: -0.1px;
}

/* Responsive */
@media (max-width: 768px) {
  .download-card {
    padding: 36px 24px;
    border-radius: 24px;
  }
  
  .platform-icon {
    width: 64px;
    height: 64px;
  }
  
  .platform-icon img {
    width: 40px;
    height: 40px;
  }
  
  .platform-name {
    font-size: 24px;
  }
  
  .detail-text {
    font-size: 14px;
  }
  
  .download-button {
    padding: 14px 28px;
    font-size: 16px;
  }
}
</style>

