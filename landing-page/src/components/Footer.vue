<template>
  <footer class="footer">
    <div class="footer-content">
      <div class="footer-top">
        <div class="footer-section">
          <h3 class="footer-title">美团商品批量上传系统</h3>
          <p class="footer-description">
            高效、智能的商品管理解决方案<br>
            让商品上传变得简单快捷
          </p>
        </div>
        
        <div class="footer-section">
          <h4 class="section-title">快速链接</h4>
          <ul class="footer-links">
            <li><router-link to="/">首页</router-link></li>
            <li><router-link to="/tutorial">使用教程</router-link></li>
            <li><router-link to="/installation">安装指南</router-link></li>
            <li><router-link to="/faq">常见问题</router-link></li>
          </ul>
        </div>
      </div>
      
      <div class="footer-divider"></div>
      
      <div class="footer-bottom">
        <p class="copyright">
          © {{ currentYear }} 
          <span class="company-name">{{ companyInfo?.companyName || '云创智链科技' }}</span>
          版权所有
        </p>
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { getCompanyInfo, type CompanyInfo } from '@/utils/api'

const companyInfo = ref<CompanyInfo | null>(null)
const currentYear = computed(() => new Date().getFullYear())

onMounted(async () => {
  companyInfo.value = await getCompanyInfo()
})
</script>

<style scoped>
.footer {
  background: linear-gradient(180deg, #1d1d1f 0%, #000000 100%);
  color: #f5f5f7;
  padding: 60px 24px 32px;
  margin-top: auto;
  position: relative;
  overflow: hidden;
}

.footer::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.1) 50%,
    transparent
  );
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
}

.footer-top {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 48px;
  margin-bottom: 48px;
}

.footer-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.footer-title {
  font-size: 20px;
  font-weight: 700;
  color: #f5f5f7;
  margin: 0;
  letter-spacing: -0.3px;
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'Segoe UI', sans-serif;
}

.footer-description {
  font-size: 15px;
  color: #a1a1a6;
  line-height: 1.6;
  margin: 0;
  letter-spacing: -0.2px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #f5f5f7;
  margin: 0;
  letter-spacing: -0.2px;
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'Segoe UI', sans-serif;
}

.footer-links {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.footer-links a {
  color: #a1a1a6;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.3s ease;
  letter-spacing: -0.1px;
}

.footer-links a:hover {
  color: #FFD100;
}

.footer-divider {
  height: 1px;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.1) 50%,
    transparent
  );
  margin-bottom: 32px;
}

.footer-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.copyright {
  font-size: 13px;
  color: #86868b;
  margin: 0;
  letter-spacing: -0.1px;
}

.company-name {
  color: #f5f5f7;
  font-weight: 600;
  margin: 0 4px;
}

.icp {
  font-size: 13px;
  color: #86868b;
  margin: 0;
  letter-spacing: -0.1px;
}

/* Responsive */
@media (max-width: 968px) {
  .footer-top {
    grid-template-columns: 1fr;
    gap: 32px;
  }
}

@media (max-width: 640px) {
  .footer {
    padding: 40px 20px 24px;
  }
  
  .footer-top {
    grid-template-columns: 1fr;
    gap: 32px;
    margin-bottom: 32px;
  }
  
  .footer-bottom {
    flex-direction: column;
    align-items: center;
    text-align: center;
    gap: 12px;
  }
  
  .footer-title {
    font-size: 18px;
  }
  
  .footer-description {
    font-size: 14px;
  }
}
</style>
