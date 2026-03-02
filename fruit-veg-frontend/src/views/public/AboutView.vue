<template>
  <div class="about">
    <el-container>
      <el-main class="about-main">
        <div class="about-content">
          <h1>关于我们</h1>
          <div class="about-section">
            <h2>公司简介</h2>
            <p>
              果蔬销售系统是一家专业的在线果蔬购买平台，致力于为用户提供新鲜、优质的果蔬产品和便捷的购物体验。
              我们与多家优质农场和供应商合作，确保每一件产品都经过严格的质量检测，让您买得放心，吃得健康。
            </p>
          </div>
          <div class="about-section">
            <h2>我们的使命</h2>
            <p>
              我们的使命是通过互联网技术，连接优质农产品生产者和消费者，减少中间环节，提高农产品流通效率，
              让更多人享受到新鲜、健康、安全的农产品。
            </p>
          </div>
          <div class="about-section">
            <h2>产品特色</h2>
            <el-row :gutter="20">
              <el-col :span="8">
                <div class="feature-item">
                  <el-icon :size="48" class="feature-icon">
                    <Goods />
                  </el-icon>
                  <h3>新鲜直达</h3>
                  <p>每日新鲜采摘，快速配送</p>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="feature-item">
                  <el-icon :size="48" class="feature-icon">
                    <Star />
                  </el-icon>
                  <h3>品质保证</h3>
                  <p>严格的质量检测，放心购买</p>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="feature-item">
                  <el-icon :size="48" class="feature-icon">
                    <Van />
                  </el-icon>
                  <h3>快速配送</h3>
                  <p>专业配送团队，及时送达</p>
                </div>
              </el-col>
            </el-row>
          </div>
          <div class="about-section">
            <h2>联系方式</h2>
            <div class="contact-info">
              <el-row :gutter="20">
                <el-col :span="8">
                  <div class="contact-item">
                    <el-icon :size="24" class="contact-icon">
                      <Phone />
                    </el-icon>
                    <div class="contact-content">
                      <h4>客服电话</h4>
                      <p>400-123-4567</p>
                    </div>
                  </div>
                </el-col>
                <el-col :span="8">
                  <div class="contact-item">
                    <el-icon :size="24" class="contact-icon">
                      <Message />
                    </el-icon>
                    <div class="contact-content">
                      <h4>电子邮箱</h4>
                      <p>info@fruits-vegetables.com</p>
                    </div>
                  </div>
                </el-col>
                <el-col :span="8">
                  <div class="contact-item">
                    <el-icon :size="24" class="contact-icon">
                      <Position />
                    </el-icon>
                    <div class="contact-content">
                      <h4>公司地址</h4>
                      <p>北京市朝阳区建国路88号</p>
                    </div>
                  </div>
                </el-col>
              </el-row>
            </div>
          </div>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { Goods, Star, Van, Phone, Message, Position } from '@element-plus/icons-vue'
</script>

<style scoped>
.about {
  min-height: 100vh;
}

.about-main {
  background: transparent;
  padding: 20px;
}

.about-content {
  max-width: 1200px;
  margin: 0 auto;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid #dbe8df;
  padding: 40px;
  border-radius: 16px;
  box-shadow: 0 12px 32px rgba(20, 60, 44, 0.09);
}

.about-content h1 {
  margin: 0 0 30px 0;
  font-size: 38px;
  color: #1f3a2c;
  text-align: center;
}

.about-section {
  margin-bottom: 40px;
}

.about-section h2 {
  margin: 0 0 20px 0;
  font-size: 24px;
  color: #234435;
}

.about-section p {
  margin: 0 0 10px 0;
  line-height: 1.8;
  color: #5a6d63;
}

.feature-item {
  text-align: center;
  padding: 20px 16px;
  border: 1px solid #e2ebe4;
  border-radius: 14px;
  background: linear-gradient(180deg, #ffffff, #f6fbf8);
}

.feature-icon {
  color: #2a7f5f;
  margin-bottom: 10px;
}

.feature-item h3 {
  margin: 10px 0;
  font-size: 18px;
  color: #234334;
}

.feature-item p {
  color: #63766d;
  font-size: 14px;
}

.contact-info {
  margin-top: 20px;
}

.contact-item {
  display: flex;
  align-items: center;
  padding: 20px;
  background: #f7fbf8;
  border: 1px solid #e1ebe4;
  border-radius: 12px;
}

.contact-icon {
  color: #2a7f5f;
  margin-right: 15px;
}

.contact-content h4 {
  margin: 0 0 5px 0;
  font-size: 16px;
  color: #2b4437;
}

.contact-content p {
  margin: 0;
  color: #62766d;
  font-size: 14px;
}
</style>
