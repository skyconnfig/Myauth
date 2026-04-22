<template>
  <div>
    <a-card title="密钥管理">
      <a-alert
        message="重要提示"
        description="请妥善保管您的密钥对！私钥用于生成授权码，公钥用于客户端验证。如果丢失，需要重新生成并通知所有客户更新。"
        type="warning"
        show-icon
        style="margin-bottom: 16px"
      />

      <a-space direction="vertical" style="width: 100%;" :size="16">
        <!-- 生成密钥对 -->
        <a-card size="small" title="生成新密钥对">
          <p style="color: #666; margin-bottom: 12px;">
            生成新的RSA密钥对（2048位）。生成后会自动保存到服务器。
          </p>
          <a-button type="primary" danger @click="handleGenerateKeyPair" :loading="generating">
            生成密钥对
          </a-button>
        </a-card>

        <!-- 公钥显示 -->
        <a-card size="small" title="公钥（用于客户端验证）">
          <p style="color: #666; margin-bottom: 12px;">
            将此公钥提供给客户，用于验证授权码的有效性。
          </p>
          <a-textarea
            v-model:value="publicKey"
            :rows="6"
            readonly
            placeholder="点击“获取公钥”按钮查看公钥"
          />
          <a-space style="margin-top: 12px;">
            <a-button @click="loadPublicKey" :loading="loading">
              获取公钥
            </a-button>
            <a-button @click="copyPublicKey" :disabled="!publicKey">
              复制公钥
            </a-button>
            <a-button @click="downloadPublicKey" :disabled="!publicKey">
              下载公钥文件
            </a-button>
          </a-space>
        </a-card>

        <!-- 使用说明 -->
        <a-card size="small" title="客户端集成说明">
          <a-steps :current="-1" direction="vertical" size="small">
            <a-step
              title="步骤1：获取公钥"
              description="点击上方“获取公钥”按钮，复制公钥内容"
            />
            <a-step
              title="步骤2：配置客户端"
              description="在客户端应用的 application.yml 中配置 license.public-key"
            />
            <a-step
              title="步骤3：放置授权文件"
              description="将生成的授权码保存为 license/license.key 文件"
            />
            <a-step
              title="步骤4：启动应用"
              description="客户端启动时会自动验证授权码的有效性"
            />
          </a-steps>

          <a-divider />

          <h4>配置文件示例：</h4>
          <a-card size="small" style="background: #f5f5f5;">
            <pre style="margin: 0; font-size: 12px;">
license:
  public-key: "{{ publicKey || '在此粘贴公钥' }}"
  license-file: license/license.key
            </pre>
          </a-card>
        </a-card>
      </a-space>
    </a-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { generateKeyPair, getPublicKey } from '@/api/license'
import { message } from 'ant-design-vue'

const publicKey = ref('')
const loading = ref(false)
const generating = ref(false)

const handleGenerateKeyPair = async () => {
  if (!confirm('确定要生成新的密钥对吗？这将使旧的授权码失效！')) {
    return
  }

  generating.value = true
  try {
    await generateKeyPair()
    message.success('密钥对生成成功')
    // 自动加载新公钥
    await loadPublicKey()
  } catch (error) {
    message.error('密钥对生成失败')
  } finally {
    generating.value = false
  }
}

const loadPublicKey = async () => {
  loading.value = true
  try {
    const res = await getPublicKey()
    publicKey.value = res.data
    message.success('公钥加载成功')
  } catch (error) {
    message.error('公钥加载失败')
  } finally {
    loading.value = false
  }
}

const copyPublicKey = () => {
  navigator.clipboard.writeText(publicKey.value).then(() => {
    message.success('公钥已复制到剪贴板')
  }).catch(() => {
    message.error('复制失败')
  })
}

const downloadPublicKey = () => {
  const blob = new Blob([publicKey.value], { type: 'text/plain' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'public.key'
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
  message.success('公钥文件已下载')
}
</script>
