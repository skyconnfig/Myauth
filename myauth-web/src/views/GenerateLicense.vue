<template>
  <div>
    <a-card title="生成新授权">
      <a-form
        :model="form"
        layout="vertical"
        style="max-width: 800px"
      >
        <a-form-item label="客户名称" required>
          <a-input v-model:value="form.customerName" placeholder="请输入客户名称" />
        </a-form-item>

        <a-form-item label="授权类型" required>
          <a-select v-model:value="form.type" placeholder="请选择授权类型">
            <a-select-option value="trial">试用版</a-select-option>
            <a-select-option value="standard">标准版</a-select-option>
            <a-select-option value="professional">专业版</a-select-option>
            <a-select-option value="enterprise">企业版</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="过期时间" required>
          <a-date-picker
            v-model:value="form.expireTime"
            style="width: 100%"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            placeholder="请选择过期时间"
          />
        </a-form-item>

        <a-form-item label="最大用户数">
          <a-input-number
            v-model:value="form.maxUsers"
            :min="1"
            :max="10000"
            style="width: 100%"
            placeholder="请输入最大用户数"
          />
        </a-form-item>

        <a-form-item label="机器码（可选）">
          <a-input
            v-model:value="form.machineId"
            placeholder="留空表示不绑定设备"
          />
          <div style="color: #999; font-size: 12px; margin-top: 4px;">
            如果填写，此授权将只能在该设备上使用
          </div>
        </a-form-item>

        <a-form-item label="功能模块（可选）">
          <a-textarea
            v-model:value="form.modules"
            :rows="3"
            placeholder='JSON数组格式，如: ["module1", "module2"]'
          />
        </a-form-item>

        <a-form-item label="备注">
          <a-textarea v-model:value="form.remark" :rows="3" placeholder="请输入备注" />
        </a-form-item>

        <a-form-item>
          <a-space>
            <a-button type="primary" size="large" @click="handleSubmit" :loading="loading">
              生成授权
            </a-button>
            <a-button size="large" @click="handleReset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 生成结果 -->
    <a-card v-if="resultVisible" title="生成成功" style="margin-top: 16px;">
      <a-alert
        message="授权码已生成，请妥善保存！"
        type="success"
        show-icon
        style="margin-bottom: 16px"
      />
      
      <a-descriptions bordered :column="1">
        <a-descriptions-item label="客户名称">
          {{ result.customerName }}
        </a-descriptions-item>
        <a-descriptions-item label="授权类型">
          {{ getTypeText(result.type) }}
        </a-descriptions-item>
        <a-descriptions-item label="过期时间">
          {{ result.expireTime }}
        </a-descriptions-item>
        <a-descriptions-item label="授权码">
          <a-textarea
            :value="result.licenseKey"
            :rows="6"
            readonly
          />
          <a-button size="small" style="margin-top: 8px;" @click="copyLicenseKey">
            复制授权码
          </a-button>
        </a-descriptions-item>
      </a-descriptions>
    </a-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { generateLicense } from '@/api/license'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'

const form = reactive({
  customerName: '',
  type: 'professional',
  expireTime: dayjs().add(1, 'year').format('YYYY-MM-DD'),
  maxUsers: 100,
  machineId: '',
  modules: '',
  remark: ''
})

const loading = ref(false)
const resultVisible = ref(false)
const result = ref(null)

const handleSubmit = async () => {
  // 验证必填项
  if (!form.customerName) {
    message.warning('请输入客户名称')
    return
  }
  if (!form.type) {
    message.warning('请选择授权类型')
    return
  }
  if (!form.expireTime) {
    message.warning('请选择过期时间')
    return
  }

  loading.value = true
  try {
    const res = await generateLicense(form)
    
    result.value = res.data
    resultVisible.value = true
    
    message.success('授权生成成功')
    
    // 滚动到结果区域
    setTimeout(() => {
      window.scrollTo({ top: document.body.scrollHeight, behavior: 'smooth' })
    }, 100)
  } catch (error) {
    message.error('授权生成失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  form.customerName = ''
  form.type = 'professional'
  form.expireTime = dayjs().add(1, 'year').format('YYYY-MM-DD')
  form.maxUsers = 100
  form.machineId = ''
  form.modules = ''
  form.remark = ''
  resultVisible.value = false
  result.value = null
}

const copyLicenseKey = () => {
  navigator.clipboard.writeText(result.value.licenseKey).then(() => {
    message.success('授权码已复制到剪贴板')
  }).catch(() => {
    message.error('复制失败')
  })
}

const getTypeText = (type) => {
  const texts = {
    trial: '试用版',
    standard: '标准版',
    professional: '专业版',
    enterprise: '企业版'
  }
  return texts[type] || type
}
</script>
