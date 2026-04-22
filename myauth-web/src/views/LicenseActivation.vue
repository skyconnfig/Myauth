<template>
  <div class="license-activation">
    <a-card title="License 激活" :bordered="false">
      <a-form :model="formState" layout="vertical">
        <a-form-item label="License Key" required>
          <a-textarea
            v-model:value="formState.licenseKey"
            placeholder="请输入 License Key"
            :rows="6"
            :disabled="activating"
          />
        </a-form-item>

        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleActivate" :loading="activating">
              激活 License
            </a-button>
            <a-button @click="handleClear" :disabled="!hasLicense">
              清除 License
            </a-button>
            <a-button @click="handleCheck" :loading="checking">
              手动检查
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <!-- License 信息展示 -->
      <a-divider />
      
      <a-descriptions title="License 信息" bordered :column="2" v-if="licenseInfo">
        <a-descriptions-item label="状态">
          <a-tag :color="getStatusColor(licenseInfo.status)">
            {{ getStatusText(licenseInfo.status) }}
          </a-tag>
        </a-descriptions-item>
        
        <a-descriptions-item label="授权类型">
          <a-tag color="blue">{{ licenseInfo.type }}</a-tag>
        </a-descriptions-item>
        
        <a-descriptions-item label="客户名称">
          {{ licenseInfo.customer_name || '-' }}
        </a-descriptions-item>
        
        <a-descriptions-item label="过期时间">
          {{ licenseInfo.expire_time }}
        </a-descriptions-item>
        
        <a-descriptions-item label="剩余天数">
          <span :class="getRemainingDaysClass(licenseInfo.remaining_days)">
            {{ licenseInfo.remaining_days }} 天
          </span>
        </a-descriptions-item>
        
        <a-descriptions-item label="最大用户数">
          {{ licenseInfo.max_users }}
        </a-descriptions-item>
        
        <a-descriptions-item label="功能模块" :span="2">
          <a-space wrap>
            <a-tag v-for="module in licenseInfo.modules" :key="module">
              {{ module }}
            </a-tag>
          </a-space>
        </a-descriptions-item>
        
        <a-descriptions-item label="机器码" :span="2">
          {{ licenseInfo.machine_id || '未绑定' }}
        </a-descriptions-item>
      </a-descriptions>

      <a-alert
        v-else
        message="试用模式"
        description="当前未激活 License，系统将以试用模式运行（30天限制，功能受限）"
        type="warning"
        show-icon
      />
    </a-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { message } from 'ant-design-vue'
import { licenseValidator } from '@/utils/license'

// 表单状态
const formState = reactive({
  licenseKey: ''
})

// 状态
const activating = ref(false)
const checking = ref(false)
const hasLicense = ref(false)
const licenseInfo = ref(null)

// 初始化
onMounted(() => {
  // 加载 License 信息
  loadLicenseInfo()
  
  // 监听 License 事件
  window.addEventListener('license:activated', handleActivated)
  window.addEventListener('license:error', handleError)
  window.addEventListener('license:cleared', handleCleared)
  window.addEventListener('license:check:success', handleCheckSuccess)
  window.addEventListener('license:check:failed', handleCheckFailed)
})

onUnmounted(() => {
  // 移除事件监听
  window.removeEventListener('license:activated', handleActivated)
  window.removeEventListener('license:error', handleError)
  window.removeEventListener('license:cleared', handleCleared)
  window.removeEventListener('license:check:success', handleCheckSuccess)
  window.removeEventListener('license:check:failed', handleCheckFailed)
})

// 加载 License 信息
const loadLicenseInfo = () => {
  const info = licenseValidator.getLicenseInfo()
  if (info) {
    licenseInfo.value = info
    hasLicense.value = true
  }
}

// 激活 License
const handleActivate = async () => {
  if (!formState.licenseKey.trim()) {
    message.warning('请输入 License Key')
    return
  }

  activating.value = true
  try {
    const result = await licenseValidator.activate(formState.licenseKey.trim())
    message.success('License 激活成功！')
    formState.licenseKey = ''
  } catch (error) {
    message.error(error.message || '激活失败')
  } finally {
    activating.value = false
  }
}

// 清除 License
const handleClear = () => {
  licenseValidator.clearLicense()
  licenseInfo.value = null
  hasLicense.value = false
  message.info('License 已清除')
}

// 手动检查
const handleCheck = async () => {
  checking.value = true
  try {
    const result = await licenseValidator.manualCheck()
    if (result) {
      message.success('License 检查通过')
    } else {
      message.warning('未找到 License')
    }
  } catch (error) {
    message.error(error.message || '检查失败')
  } finally {
    checking.value = false
  }
}

// 事件处理
const handleActivated = (event) => {
  licenseInfo.value = event.detail
  hasLicense.value = true
  message.success('License 已激活')
}

const handleError = (event) => {
  message.error(event.detail.message)
}

const handleCleared = () => {
  licenseInfo.value = null
  hasLicense.value = false
}

const handleCheckSuccess = () => {
  console.log('定时检查成功')
}

const handleCheckFailed = (event) => {
  message.error(`License 检查失败: ${event.detail.message}`)
}

// 获取状态颜色
const getStatusColor = (status) => {
  const colors = {
    valid: 'green',
    expired: 'red',
    trial: 'orange'
  }
  return colors[status] || 'default'
}

// 获取状态文本
const getStatusText = (status) => {
  const texts = {
    valid: '有效',
    expired: '已过期',
    trial: '试用中'
  }
  return texts[status] || status
}

// 获取剩余天数样式类
const getRemainingDaysClass = (days) => {
  if (days < 0) return 'text-danger'
  if (days <= 7) return 'text-warning'
  return 'text-success'
}
</script>

<style scoped>
.license-activation {
  max-width: 800px;
  margin: 0 auto;
}

.text-success {
  color: #52c41a;
  font-weight: bold;
}

.text-warning {
  color: #faad14;
  font-weight: bold;
}

.text-danger {
  color: #ff4d4f;
  font-weight: bold;
}
</style>
