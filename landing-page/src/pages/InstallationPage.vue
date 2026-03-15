<template>
  <div class="installation-page doc-page">
    <div class="doc-container">
      <h1 class="doc-title">安装指南</h1>
      <p class="doc-intro">
        在开始使用美团商品批量上传系统之前，请确保您的系统满足以下要求，并按照步骤完成安装。
      </p>
      
      <!-- 系统要求 -->
      <section class="requirements-section">
        <h2 class="section-title">系统要求</h2>
        <div class="requirements-grid">
          <div
            v-for="requirement in systemRequirements"
            :key="requirement.category"
            class="requirement-card"
          >
            <h3 class="requirement-category">{{ requirement.category }}</h3>
            <ul class="requirement-list">
              <li v-for="(item, index) in requirement.items" :key="index">
                {{ item }}
              </li>
            </ul>
          </div>
        </div>
      </section>
      
      <!-- 安装步骤 -->
      <section class="installation-section">
        <h2 class="section-title">安装步骤</h2>
        <div class="installation-steps">
          <div
            v-for="step in installationSteps"
            :key="step.id"
            class="installation-step"
          >
            <div class="step-header">
              <div class="step-number">{{ step.id }}</div>
              <h3 class="step-title">{{ step.title }}</h3>
            </div>
            <p class="step-description">{{ step.description }}</p>
            <div v-if="step.commands && step.commands.length > 0" class="step-commands">
              <code v-for="(cmd, index) in step.commands" :key="index" class="command">
                {{ cmd }}
              </code>
            </div>
          </div>
        </div>
      </section>
      
      <!-- 下载链接 -->
      <section class="download-section">
        <h2 class="section-title">下载安装包</h2>
        <div class="download-box">
          <div class="download-info">
            <h3 class="download-title">美团商品批量上传系统 v1.0</h3>
            <p class="download-meta">
              <span class="meta-item">
                <Icon name="package" :size="16" />
                文件大小：约 50MB
              </span>
              <span class="meta-item">
                <Icon name="monitor" :size="16" />
                适用系统：Windows 10/11
              </span>
              <span class="meta-item">
                <Icon name="gift" :size="16" />
                完全免费
              </span>
            </p>
          </div>
          <router-link to="/#download" class="download-button">
            立即下载
          </router-link>
        </div>
      </section>
      
      <!-- 首次配置 -->
      <section class="config-section">
        <h2 class="section-title">首次配置</h2>
        <div class="config-content">
          <p class="config-description">
            安装完成后，首次启动应用时需要进行基本配置：
          </p>
          <ol class="config-list">
            <li>启动应用程序</li>
            <li>系统会自动打开配置向导</li>
            <li>按照提示完成基本设置</li>
            <li>配置完成后即可开始使用</li>
          </ol>
          <div class="config-note">
            <p>
              <Icon name="lightbulb" :size="16" class="note-icon" />
              <strong>提示：</strong>如果需要修改配置，可以在系统设置中随时调整。
            </p>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import Icon from '@/components/Icon.vue'

interface SystemRequirement {
  category: string
  items: string[]
}

interface InstallationStep {
  id: number
  title: string
  description: string
  commands?: string[]
}

const systemRequirements: SystemRequirement[] = [
  {
    category: '操作系统',
    items: [
      'Windows 10 或更高版本',
      'Windows 11 (推荐)',
      '64 位操作系统'
    ]
  },
  {
    category: '硬件要求',
    items: [
      '至少 4GB 内存',
      '至少 500MB 可用磁盘空间',
      '稳定的网络连接'
    ]
  },
  {
    category: '软件依赖',
    items: [
      '无需额外安装',
      '所有依赖已打包',
      '开箱即用'
    ]
  }
]

const installationSteps: InstallationStep[] = [
  {
    id: 1,
    title: '下载安装包',
    description: '从官网下载最新版本的安装包，确保下载完整且未损坏。'
  },
  {
    id: 2,
    title: '运行安装程序',
    description: '双击下载的安装包，按照安装向导的提示完成安装。选择合适的安装路径。'
  },
  {
    id: 3,
    title: '等待安装完成',
    description: '安装程序会自动完成所有必要的配置，通常需要 1-2 分钟。'
  },
  {
    id: 4,
    title: '启动应用',
    description: '安装完成后，可以从桌面快捷方式或开始菜单启动应用程序。'
  }
]
</script>

<style scoped>
.installation-page {
  flex: 1;
}

.doc-container {
  max-width: 900px;
  margin: 0 auto;
  padding: var(--spacing-xl) var(--spacing-md);
}

.doc-title {
  font-size: var(--font-size-3xl);
  font-weight: 700;
  color: var(--color-text-primary);
  margin-bottom: var(--spacing-md);
}

.doc-intro {
  font-size: var(--font-size-lg);
  color: var(--color-text-secondary);
  line-height: 1.8;
  margin-bottom: var(--spacing-xl);
}

.section-title {
  font-size: var(--font-size-2xl);
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: var(--spacing-lg);
  padding-bottom: var(--spacing-sm);
  border-bottom: 2px solid var(--color-primary);
}

/* 系统要求 */
.requirements-section {
  margin-bottom: var(--spacing-xl);
}

.requirements-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: var(--spacing-md);
}

.requirement-card {
  background-color: var(--color-bg-secondary);
  border-radius: var(--radius-md);
  padding: var(--spacing-lg);
  border: 1px solid var(--color-border);
}

.requirement-category {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--color-primary);
  margin-bottom: var(--spacing-md);
}

.requirement-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.requirement-list li {
  padding: var(--spacing-xs) 0;
  padding-left: var(--spacing-md);
  position: relative;
  color: var(--color-text-secondary);
  line-height: 1.6;
}

.requirement-list li::before {
  content: '•';
  position: absolute;
  left: 0;
  color: var(--color-primary);
  font-weight: 700;
}

/* 安装步骤 */
.installation-section {
  margin-bottom: var(--spacing-xl);
}

.installation-steps {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.installation-step {
  background-color: var(--color-bg-secondary);
  border-radius: var(--radius-md);
  padding: var(--spacing-lg);
  border: 1px solid var(--color-border);
}

.step-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-sm);
}

.step-number {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  color: var(--color-text-primary);
  font-size: var(--font-size-lg);
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.step-title {
  font-size: var(--font-size-xl);
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0;
}

.step-description {
  color: var(--color-text-secondary);
  line-height: 1.6;
  margin: 0;
}

.step-commands {
  margin-top: var(--spacing-md);
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
}

.command {
  background-color: var(--color-bg-tertiary);
  padding: var(--spacing-sm) var(--spacing-md);
  border-radius: var(--radius-sm);
  font-family: 'Courier New', monospace;
  font-size: var(--font-size-sm);
  color: var(--color-text-primary);
  border: 1px solid var(--color-border);
}

/* 下载区域 */
.download-section {
  margin-bottom: var(--spacing-xl);
}

.download-box {
  background: linear-gradient(135deg, rgba(255, 209, 0, 0.1) 0%, rgba(255, 209, 0, 0.05) 100%);
  border: 2px solid var(--color-primary);
  border-radius: var(--radius-md);
  padding: var(--spacing-xl);
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--spacing-lg);
}

.download-info {
  flex: 1;
}

.download-title {
  font-size: var(--font-size-xl);
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: var(--spacing-sm);
}

.download-meta {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-md);
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
}

.download-button {
  padding: var(--spacing-md) var(--spacing-xl);
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  color: var(--color-text-primary);
  text-decoration: none;
  font-weight: 600;
  border-radius: var(--radius-md);
  transition: all 0.3s ease;
  white-space: nowrap;
}

.download-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 209, 0, 0.3);
}

/* 配置区域 */
.config-section {
  margin-bottom: var(--spacing-xl);
}

.config-content {
  background-color: var(--color-bg-secondary);
  border-radius: var(--radius-md);
  padding: var(--spacing-lg);
  border: 1px solid var(--color-border);
}

.config-description {
  color: var(--color-text-secondary);
  line-height: 1.6;
  margin-bottom: var(--spacing-md);
}

.config-list {
  padding-left: var(--spacing-lg);
  margin: 0 0 var(--spacing-md) 0;
}

.config-list li {
  padding: var(--spacing-xs) 0;
  color: var(--color-text-secondary);
  line-height: 1.6;
}

.config-note {
  background-color: rgba(255, 209, 0, 0.1);
  border-left: 4px solid var(--color-primary);
  padding: var(--spacing-md);
  border-radius: var(--radius-sm);
}

.config-note p {
  margin: 0;
  color: var(--color-text-secondary);
  line-height: 1.6;
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
}

.note-icon {
  color: var(--color-primary);
  flex-shrink: 0;
}

/* 响应式 */
@media (max-width: 768px) {
  .doc-container {
    padding: var(--spacing-lg) var(--spacing-sm);
  }
  
  .doc-title {
    font-size: var(--font-size-2xl);
  }
  
  .doc-intro {
    font-size: var(--font-size-base);
  }
  
  .requirements-grid {
    grid-template-columns: 1fr;
  }
  
  .download-box {
    flex-direction: column;
    align-items: stretch;
    padding: var(--spacing-md);
  }
  
  .download-button {
    text-align: center;
  }
  
  .download-meta {
    flex-direction: column;
    gap: var(--spacing-xs);
  }
}
</style>
