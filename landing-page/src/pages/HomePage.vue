<template>
  <div class="home-page">
    <HeroSection 
      :title="content.hero.title"
      :subtitle="content.hero.subtitle"
      :description="content.hero.description"
      :ctaText="content.hero.ctaText"
      :ctaLink="content.hero.ctaLink"
    />
    
    <section class="content-section features-section">
      <div class="container">
        <ScrollReveal animation="slide-up">
          <h2 class="section-title">核心功能</h2>
          <p class="section-subtitle">强大的功能，让商品管理更简单高效</p>
        </ScrollReveal>
        <FeaturesGrid :features="content.features" />
      </div>
    </section>
    
    <section id="download" class="content-section download-section">
      <div class="container">
        <ScrollReveal animation="slide-up">
          <h2 class="section-title">立即下载</h2>
          <p class="section-subtitle">选择适合您系统的版本,开始高效管理商品</p>
        </ScrollReveal>
        
        <div v-if="loading" class="loading-state">
          <div class="loading-spinner"></div>
          <p>正在加载版本信息...</p>
        </div>
        
        <div v-else class="download-cards">
          <ScrollReveal animation="scale" :delay="0.2">
            <DownloadCard
              platform="windows"
              platformName="Windows"
              systemRequirement="Windows 10/11"
              :fileSize="windowsVersion ? formatFileSize(windowsVersion.fileSize) : '约 50MB'"
              :version="windowsVersion ? `v${windowsVersion.version}` : 'v1.0.0'"
              :versionId="windowsVersion?.id"
              :releaseNotes="windowsVersion?.releaseNotes"
              :downloadCount="windowsVersion?.downloadCount || 0"
            />
          </ScrollReveal>
          <ScrollReveal animation="scale" :delay="0.4">
            <DownloadCard
              platform="mac"
              platformName="macOS"
              systemRequirement="macOS 11+"
              :fileSize="macVersion ? formatFileSize(macVersion.fileSize) : '约 45MB'"
              :version="macVersion ? `v${macVersion.version}` : 'v1.0.0'"
              :versionId="macVersion?.id"
              :releaseNotes="macVersion?.releaseNotes"
              :downloadCount="macVersion?.downloadCount || 0"
            />
          </ScrollReveal>
        </div>
        <ScrollReveal animation="fade" :delay="0.6">
          <div class="download-footer">
            <div class="footer-item">
              <svg class="footer-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
              </svg>
              <span class="footer-text">安全可靠</span>
            </div>
            <div class="footer-item">
              <svg class="footer-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"></polygon>
              </svg>
              <span class="footer-text">快速安装</span>
            </div>
            <div class="footer-item">
              <svg class="footer-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                <polyline points="22 4 12 14.01 9 11.01"></polyline>
              </svg>
              <span class="footer-text">简单易用</span>
            </div>
          </div>
        </ScrollReveal>
      </div>
    </section>
    
    <section class="content-section alt-bg">
      <div class="container">
        <ScrollReveal animation="slide-up">
          <DocumentationLinks 
            title="文档资源"
            :links="content.documentation"
          />
        </ScrollReveal>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import HeroSection from '@/components/HeroSection.vue'
import FeaturesGrid from '@/components/FeaturesGrid.vue'
import DocumentationLinks from '@/components/DocumentationLinks.vue'
import DownloadCard from '@/components/DownloadCard.vue'
import ScrollReveal from '@/components/animations/ScrollReveal.vue'
import { getLatestVersion, formatFileSize, type AppVersion } from '@/utils/api'

const windowsVersion = ref<AppVersion | null>(null)
const macVersion = ref<AppVersion | null>(null)
const loading = ref(true)

onMounted(async () => {
  try {
    // 并行获取两个平台的最新版本
    const [windows, mac] = await Promise.all([
      getLatestVersion('Windows'),
      getLatestVersion('macOS')
    ])
    
    windowsVersion.value = windows
    macVersion.value = mac
  } catch (error) {
    console.error('获取版本信息失败:', error)
  } finally {
    loading.value = false
  }
})

const content = {
  hero: {
    title: '美团商品批量上传系统',
    subtitle: '高效、智能的商品管理解决方案',
    description: '支持批量导入、自动格式识别、一键上传到美团平台。完全免费,立即下载使用!',
    ctaText: '立即下载',
    ctaLink: '#download'
  },
  
  features: [
    { name: '批量上传商品' },
    { name: '美团格式自动识别' },
    { name: 'Excel 模板生成' },
    { name: '图片批量管理' },
    { name: '操作日志记录' },
    { name: '数据导入导出' },
    { name: '商品批量编辑' },
    { name: '完全免费使用' }
  ],
  
  documentation: [
    { 
      title: '下载软件', 
      url: '#download', 
      icon: 'rocket' as const,
      description: 'Windows 版本,一键安装即可使用'
    },
    { 
      title: '使用教程', 
      url: '/tutorial', 
      icon: 'book' as const,
      description: '详细的图文教程,快速上手'
    },
    { 
      title: '安装指南', 
      url: '/installation', 
      icon: 'server' as const,
      description: '系统要求和安装步骤说明'
    },
    { 
      title: '常见问题', 
      url: '/faq', 
      icon: 'help' as const,
      description: '常见问题解答和故障排除'
    }
  ]
}
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background-color: var(--color-bg-primary);
}

.content-section {
  padding: var(--spacing-xl) var(--spacing-md);
  background-color: var(--color-bg-primary);
}

.content-section.alt-bg {
  background-color: var(--color-bg-secondary);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
}

.section-title {
  text-align: center;
  margin-bottom: var(--spacing-lg);
  color: var(--color-text-primary);
  font-size: var(--font-size-3xl);
  font-weight: 700;
}

.features-section {
  background: linear-gradient(
    180deg,
    #ffffff 0%,
    #f5f5f7 50%,
    #ffffff 100%
  );
  padding: 100px var(--spacing-md) !important;
  position: relative;
  overflow: hidden;
}

.features-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 30% 20%, rgba(255, 209, 0, 0.05) 0%, transparent 50%),
    radial-gradient(circle at 70% 80%, rgba(0, 0, 0, 0.02) 0%, transparent 50%);
  pointer-events: none;
}

.features-section .container {
  position: relative;
  z-index: 1;
}

.download-section {
  background: linear-gradient(
    180deg,
    #f5f5f7 0%,
    #ffffff 50%,
    #f5f5f7 100%
  );
  position: relative;
  overflow: hidden;
  padding: 100px var(--spacing-md) !important;
}

.download-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 20% 30%, rgba(0, 113, 227, 0.08) 0%, transparent 50%),
    radial-gradient(circle at 80% 70%, rgba(0, 0, 0, 0.04) 0%, transparent 50%);
  pointer-events: none;
}

.download-section .container {
  position: relative;
  z-index: 1;
}

.section-subtitle {
  text-align: center;
  color: #6e6e73;
  font-size: 19px;
  font-weight: 400;
  margin-top: 12px;
  margin-bottom: 60px;
  letter-spacing: -0.3px;
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'Segoe UI', sans-serif;
}

.download-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(340px, 1fr));
  gap: 32px;
  max-width: 900px;
  margin: 0 auto 60px;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  gap: 20px;
}

.loading-spinner {
  width: 48px;
  height: 48px;
  border: 4px solid rgba(255, 209, 0, 0.2);
  border-top-color: #FFD100;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.loading-state p {
  font-size: 16px;
  color: #6e6e73;
  font-weight: 500;
  margin: 0;
}

.download-footer {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 48px;
  flex-wrap: wrap;
  padding: 32px;
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  max-width: 700px;
  margin: 0 auto;
}

.footer-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.footer-icon {
  width: 32px;
  height: 32px;
  color: #FFD100;
  stroke-width: 2.5;
}

.footer-text {
  font-size: 15px;
  color: #1d1d1f;
  font-weight: 600;
  letter-spacing: -0.2px;
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'Segoe UI', sans-serif;
}

/* Responsive */
@media (max-width: 768px) {
  .content-section {
    padding: var(--spacing-lg) var(--spacing-sm);
  }
  
  .section-title {
    font-size: var(--font-size-2xl);
  }
  
  .download-section {
    padding: 60px var(--spacing-sm) !important;
  }
  
  .section-subtitle {
    font-size: 17px;
    margin-bottom: 40px;
  }
  
  .download-cards {
    grid-template-columns: 1fr;
    gap: 24px;
    margin-bottom: 40px;
  }
  
  .download-footer {
    gap: 32px;
    padding: 24px;
  }
  
  .footer-icon {
    width: 28px;
    height: 28px;
  }
  
  .footer-text {
    font-size: 14px;
  }
}
</style>
