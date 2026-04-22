<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h1>MyAuth</h1>
        <p>授权管理系统</p>
      </div>
      
      <a-form
        :model="form"
        @finish="handleLogin"
        class="login-form"
      >
        <a-form-item
          name="username"
          :rules="[{ required: true, message: '请输入用户名' }]"
        >
          <a-input
            v-model:value="form.username"
            placeholder="用户名"
            size="large"
          >
            <template #prefix>
              <UserOutlined />
            </template>
          </a-input>
        </a-form-item>

        <a-form-item
          name="password"
          :rules="[{ required: true, message: '请输入密码' }]"
        >
          <a-input-password
            v-model:value="form.password"
            placeholder="密码"
            size="large"
          >
            <template #prefix>
              <LockOutlined />
            </template>
          </a-input-password>
        </a-form-item>

        <a-form-item>
          <a-button
            type="primary"
            html-type="submit"
            size="large"
            block
            :loading="loading"
          >
            登录
          </a-button>
        </a-form-item>
      </a-form>

      <div class="login-tip">
        <p>默认账号：admin / admin123</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue'
import { login } from '@/api/license'
import { message } from 'ant-design-vue'

const router = useRouter()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const handleLogin = async () => {
  loading.value = true
  try {
    const res = await login(form)
    
    // 保存token和用户信息
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('username', res.data.username)
    localStorage.setItem('realName', res.data.realName)
    localStorage.setItem('userId', res.data.userId)
    
    message.success('登录成功')
    
    // 跳转到首页
    router.push('/dashboard')
  } catch (error) {
    message.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #111827 0%, #1F2937 100%);
}

.login-box {
  width: 420px;
  padding: 48px 40px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-header h1 {
  margin: 0;
  font-size: 32px;
  font-weight: 700;
  color: #111827;
  letter-spacing: -0.5px;
}

.login-header p {
  margin: 8px 0 0;
  color: #6B7280;
  font-size: 14px;
}

.login-form {
  margin-top: 24px;
}

.login-tip {
  text-align: center;
  margin-top: 20px;
  padding: 12px;
  background: #f9fafb;
  border-radius: 6px;
  color: #6B7280;
  font-size: 13px;
}

.login-tip p {
  margin: 0;
}
</style>
