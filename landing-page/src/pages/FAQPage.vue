<template>
  <div class="faq-page doc-page">
    <div class="doc-container">
      <h1 class="doc-title">常见问题</h1>
      <p class="doc-intro">
        这里整理了用户最常遇到的问题和解决方案。如果您的问题未在此列出，请联系技术支持。
      </p>
      
      <!-- 按类别分组的 FAQ -->
      <div
        v-for="category in faqCategories"
        :key="category"
        class="faq-category"
      >
        <h2 class="category-title">{{ category }}</h2>
        <div class="faq-list">
          <div
            v-for="item in getFAQsByCategory(category)"
            :key="item.id"
            class="faq-item"
          >
            <button
              class="faq-question"
              :class="{ 'is-open': openItems.includes(item.id) }"
              @click="toggleItem(item.id)"
              :aria-expanded="openItems.includes(item.id)"
            >
              <span class="question-text">{{ item.question }}</span>
              <span class="question-icon">
                {{ openItems.includes(item.id) ? '−' : '+' }}
              </span>
            </button>
            <transition name="faq-answer">
              <div v-if="openItems.includes(item.id)" class="faq-answer">
                <p>{{ item.answer }}</p>
              </div>
            </transition>
          </div>
        </div>
      </div>
      
      <!-- 联系支持 -->
      <div class="support-section">
        <h2 class="section-title">还有其他问题？</h2>
        <p class="support-text">
          如果以上内容没有解决您的问题，欢迎查看
          <router-link to="/tutorial" class="inline-link">使用教程</router-link>
          或
          <router-link to="/installation" class="inline-link">安装指南</router-link>
          获取更多帮助。
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

interface FAQItem {
  id: number
  category: string
  question: string
  answer: string
}

const faqCategories = ['安装问题', '使用问题', '错误排查', '其他']

const faqItems: FAQItem[] = [
  {
    id: 1,
    category: '安装问题',
    question: '安装时提示缺少依赖怎么办？',
    answer: '本系统已打包所有依赖，无需额外安装。如果仍有问题，请确保您的 Windows 系统已更新到最新版本，并检查是否有杀毒软件阻止了安装程序。'
  },
  {
    id: 2,
    category: '安装问题',
    question: '安装后无法启动应用？',
    answer: '请检查：1) 是否以管理员权限运行 2) 防火墙是否阻止了应用 3) 系统是否满足最低要求（Windows 10 及以上）。如果问题仍然存在，请尝试重新安装。'
  },
  {
    id: 3,
    category: '使用问题',
    question: '支持哪些文件格式？',
    answer: '系统支持 Excel (.xlsx, .xls) 和 CSV (.csv) 格式。推荐使用 Excel 格式以获得最佳兼容性。文件大小建议不超过 10MB，单次导入商品数量建议不超过 5000 个。'
  },
  {
    id: 4,
    category: '使用问题',
    question: '如何批量上传商品？',
    answer: '首先导入商品文件，系统会自动识别格式。然后在商品列表页面点击"批量上传"按钮即可。上传过程中请保持网络连接稳定，不要关闭应用。'
  },
  {
    id: 5,
    category: '使用问题',
    question: '可以同时上传多个文件吗？',
    answer: '目前系统支持单个文件导入。如果需要导入多个文件，请先合并为一个文件，或者分批次导入。系统会保留之前导入的商品数据。'
  },
  {
    id: 6,
    category: '错误排查',
    question: '上传失败怎么办？',
    answer: '请检查：1) 网络连接是否正常 2) 美团 API 配置是否正确 3) 商品数据是否符合要求 4) 是否超过了美团平台的限制。详细错误信息可在日志页面查看。'
  },
  {
    id: 7,
    category: '错误排查',
    question: '导入的商品数据不显示？',
    answer: '请确保文件格式正确，必填字段完整。可以下载系统提供的模板文件作为参考。如果使用的是自定义格式，请确保列名与系统要求一致。'
  },
  {
    id: 8,
    category: '错误排查',
    question: '为什么有些商品上传失败？',
    answer: '可能的原因包括：1) 商品信息不完整 2) 图片链接无效 3) 价格格式错误 4) 类目 ID 不正确。系统会在上传结果中显示失败原因，您可以根据提示修改后重新上传。'
  },
  {
    id: 9,
    category: '其他',
    question: '系统是否收费？',
    answer: '本系统完全免费，无需注册，下载即可使用。我们不会收取任何费用，也不会在未来转为付费模式。'
  },
  {
    id: 10,
    category: '其他',
    question: '数据安全吗？',
    answer: '您的数据完全存储在本地，我们不会上传或保存您的商品信息。系统只在您主动上传时才会将数据发送到美团平台。'
  },
  {
    id: 11,
    category: '其他',
    question: '支持 Mac 或 Linux 系统吗？',
    answer: '目前仅支持 Windows 系统。我们正在评估对其他操作系统的支持，未来可能会推出 Mac 和 Linux 版本。'
  }
]

const openItems = ref<number[]>([])

const getFAQsByCategory = (category: string) => {
  return faqItems.filter(item => item.category === category)
}

const toggleItem = (id: number) => {
  const index = openItems.value.indexOf(id)
  if (index > -1) {
    openItems.value.splice(index, 1)
  } else {
    openItems.value.push(id)
  }
}
</script>

<style scoped>
.faq-page {
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

.faq-category {
  margin-bottom: var(--spacing-xl);
}

.category-title {
  font-size: var(--font-size-2xl);
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: var(--spacing-lg);
  padding-bottom: var(--spacing-sm);
  border-bottom: 2px solid var(--color-primary);
}

.faq-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.faq-item {
  background-color: var(--color-bg-secondary);
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
  overflow: hidden;
  transition: all 0.3s ease;
}

.faq-item:hover {
  border-color: var(--color-primary);
}

.faq-question {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-md) var(--spacing-lg);
  background: transparent;
  border: none;
  cursor: pointer;
  text-align: left;
  transition: all 0.3s ease;
}

.faq-question:hover {
  background-color: rgba(255, 209, 0, 0.05);
}

.faq-question.is-open {
  background-color: rgba(255, 209, 0, 0.1);
}

.question-text {
  flex: 1;
  font-size: var(--font-size-base);
  font-weight: 500;
  color: var(--color-text-primary);
  line-height: 1.6;
}

.question-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--color-primary);
  color: var(--color-text-primary);
  border-radius: 50%;
  font-size: var(--font-size-xl);
  font-weight: 700;
  flex-shrink: 0;
  transition: transform 0.3s ease;
}

.faq-question.is-open .question-icon {
  transform: rotate(180deg);
}

.faq-answer {
  padding: 0 var(--spacing-lg) var(--spacing-md) var(--spacing-lg);
  overflow: hidden;
}

.faq-answer p {
  color: var(--color-text-secondary);
  line-height: 1.8;
  margin: 0;
}

/* FAQ 答案过渡动画 */
.faq-answer-enter-active,
.faq-answer-leave-active {
  transition: all 0.3s ease;
}

.faq-answer-enter-from,
.faq-answer-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* 支持区域 */
.support-section {
  margin-top: var(--spacing-xl);
  padding: var(--spacing-xl);
  background: linear-gradient(135deg, rgba(255, 209, 0, 0.1) 0%, rgba(255, 209, 0, 0.05) 100%);
  border-radius: var(--radius-md);
  border: 2px solid var(--color-primary);
  text-align: center;
}

.section-title {
  font-size: var(--font-size-2xl);
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: var(--spacing-md);
}

.support-text {
  color: var(--color-text-secondary);
  line-height: 1.8;
  margin: 0;
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
  
  .category-title {
    font-size: var(--font-size-xl);
  }
  
  .faq-question {
    padding: var(--spacing-sm) var(--spacing-md);
  }
  
  .question-text {
    font-size: var(--font-size-sm);
  }
  
  .question-icon {
    width: 28px;
    height: 28px;
    font-size: var(--font-size-lg);
  }
  
  .faq-answer {
    padding: 0 var(--spacing-md) var(--spacing-sm) var(--spacing-md);
  }
  
  .faq-answer p {
    font-size: var(--font-size-sm);
  }
  
  .support-section {
    padding: var(--spacing-md);
  }
  
  .section-title {
    font-size: var(--font-size-xl);
  }
}
</style>
