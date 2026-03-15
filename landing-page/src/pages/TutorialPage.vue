<template>
  <div class="tutorial-page doc-page">
    <div class="doc-container">
      <h1 class="doc-title">使用教程</h1>
      <p class="doc-intro">
        按照以下步骤，快速上手美团商品批量上传系统，轻松管理您的商品数据。
      </p>
      
      <div class="tutorial-steps">
        <div
          v-for="step in tutorialSteps"
          :key="step.id"
          class="tutorial-step"
        >
          <div class="step-header">
            <div class="step-number">{{ step.id }}</div>
            <h2 class="step-title">{{ step.title }}</h2>
          </div>
          <p class="step-description">{{ step.description }}</p>
          <ul class="step-details">
            <li v-for="(detail, index) in step.details" :key="index">
              {{ detail }}
            </li>
          </ul>
          <div v-if="step.image" class="step-image" @click="openImagePreview(step.image, step.title)">
            <img :src="step.image" :alt="step.title" />
            <div class="image-overlay">
              <span class="zoom-icon">+</span>
              <span class="zoom-text">点击查看大图</span>
            </div>
          </div>
        </div>
      </div>
      
      <div class="tutorial-footer">
        <p class="footer-note">
          提示：如果在使用过程中遇到问题，请查看
          <router-link to="/faq" class="inline-link">常见问题</router-link>
          或参考
          <router-link to="/installation" class="inline-link">安装指南</router-link>。
        </p>
      </div>
    </div>
    
    <!-- 图片预览弹窗 -->
    <Teleport to="body">
      <div v-if="previewImage" class="image-preview-modal" @click="closeImagePreview">
        <div class="preview-content" @click.stop>
          <button class="close-button" @click="closeImagePreview">✕</button>
          <img :src="previewImage" :alt="previewTitle" />
          <p class="preview-title">{{ previewTitle }}</p>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

interface TutorialStep {
  id: number
  title: string
  description: string
  details: string[]
  image?: string
}

const previewImage = ref<string | null>(null)
const previewTitle = ref<string>('')

const openImagePreview = (image: string, title: string) => {
  previewImage.value = image
  previewTitle.value = title
  document.body.style.overflow = 'hidden'
}

const closeImagePreview = () => {
  previewImage.value = null
  previewTitle.value = ''
  document.body.style.overflow = ''
}

const tutorialSteps: TutorialStep[] = [
  {
    id: 1,
    title: '准备商品数据',
    description: '准备您的商品信息，可以使用 Excel 或 CSV 格式',
    details: [
      '下载系统提供的模板文件',
      '按照模板格式填写商品信息',
      '确保必填字段完整（商品名称、价格、类目等）',
      '检查数据格式是否符合要求'
    ],
    image: '/tutorial/1.png'
  },
  {
    id: 2,
    title: '导入商品文件',
    description: '将准备好的商品文件导入系统',
    details: [
      '点击"导入商品"按钮',
      '选择您的 Excel 或 CSV 文件',
      '系统会自动识别美团格式',
      '等待文件上传和解析完成'
    ],
    image: '/tutorial/2.png'
  },
  {
    id: 3,
    title: '预览和编辑',
    description: '查看导入的商品数据，进行必要的调整',
    details: [
      '在商品列表中查看所有导入的商品',
      '可以编辑、删除或添加商品',
      '检查商品图片是否正确',
      '验证商品信息的完整性'
    ],
    image: '/tutorial/3.png'
  },
  {
    id: 4,
    title: '批量上传',
    description: '一键将商品批量上传到美团平台',
    details: [
      '点击"批量上传"按钮',
      '系统会自动上传所有商品',
      '查看上传进度和结果',
      '处理上传失败的商品（如有）'
    ],
    image: '/tutorial/4.png'
  }
]
</script>

<style scoped>
.tutorial-page {
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

.tutorial-steps {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xl);
}

.tutorial-step {
  background-color: var(--color-bg-secondary);
  border-radius: var(--radius-md);
  padding: var(--spacing-lg);
  border: 1px solid var(--color-border);
  transition: all 0.3s ease;
}

.tutorial-step:hover {
  border-color: var(--color-primary);
  box-shadow: 0 4px 12px rgba(255, 209, 0, 0.1);
}

.step-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-md);
}

.step-number {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  color: var(--color-text-primary);
  font-size: var(--font-size-xl);
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.step-title {
  font-size: var(--font-size-2xl);
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0;
}

.step-description {
  font-size: var(--font-size-base);
  color: var(--color-text-secondary);
  line-height: 1.6;
  margin-bottom: var(--spacing-md);
}

.step-details {
  list-style: none;
  padding: 0;
  margin: 0 0 var(--spacing-md) 0;
}

.step-details li {
  padding: var(--spacing-sm) 0;
  padding-left: var(--spacing-lg);
  position: relative;
  color: var(--color-text-secondary);
  line-height: 1.6;
}

.step-details li::before {
  content: '√';
  position: absolute;
  left: 0;
  color: var(--color-primary);
  font-weight: 700;
}

.step-image {
  margin-top: var(--spacing-md);
  border-radius: var(--radius-sm);
  overflow: hidden;
  border: 1px solid var(--color-border);
  position: relative;
  cursor: pointer;
  transition: all 0.3s ease;
}

.step-image:hover {
  border-color: var(--color-primary);
  box-shadow: 0 4px 12px rgba(255, 209, 0, 0.2);
}

.step-image:hover .image-overlay {
  opacity: 1;
}

.step-image img {
  width: 100%;
  height: auto;
  display: block;
  transition: transform 0.3s ease;
}

.step-image:hover img {
  transform: scale(1.02);
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-xs);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.zoom-icon {
  font-size: var(--font-size-3xl);
}

.zoom-text {
  color: white;
  font-size: var(--font-size-sm);
  font-weight: 500;
}

/* 图片预览弹窗 */
.image-preview-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  padding: var(--spacing-lg);
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.preview-content {
  position: relative;
  max-width: 90vw;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-md);
  animation: zoomIn 0.3s ease;
}

@keyframes zoomIn {
  from {
    transform: scale(0.9);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}

.preview-content img {
  max-width: 100%;
  max-height: 80vh;
  object-fit: contain;
  border-radius: var(--radius-md);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
}

.preview-title {
  color: white;
  font-size: var(--font-size-lg);
  font-weight: 500;
  text-align: center;
  margin: 0;
}

.close-button {
  position: absolute;
  top: -50px;
  right: 0;
  background: rgba(255, 255, 255, 0.2);
  border: 2px solid white;
  color: white;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  font-size: var(--font-size-xl);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.close-button:hover {
  background: white;
  color: var(--color-text-primary);
  transform: rotate(90deg);
}

.tutorial-footer {
  margin-top: var(--spacing-xl);
  padding: var(--spacing-lg);
  background-color: rgba(255, 209, 0, 0.1);
  border-radius: var(--radius-md);
  border-top: 4px solid var(--color-primary);
}

.footer-note {
  color: var(--color-text-secondary);
  line-height: 1.6;
  margin: 0;
  text-align: center;
}

.inline-link {
  color: var(--color-primary);
  text-decoration: none;
  font-weight: 500;
  transition: color 0.2s ease;
}

.inline-link:hover {
  color: var(--color-primary-dark);
  text-decoration: underline;
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
  
  .tutorial-step {
    padding: var(--spacing-md);
  }
  
  .step-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-sm);
  }
  
  .step-number {
    width: 40px;
    height: 40px;
    font-size: var(--font-size-lg);
  }
  
  .step-title {
    font-size: var(--font-size-xl);
  }
  
  .step-image {
    border-radius: var(--radius-xs);
  }
  
  .step-image img {
    border-radius: var(--radius-xs);
  }
}
</style>
