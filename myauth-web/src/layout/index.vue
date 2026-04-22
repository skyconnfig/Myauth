<template>
  <a-layout style="min-height: 100vh">
    <!-- 左侧边栏 -->
    <a-layout-sider 
      v-model:collapsed="collapsed" 
      collapsible
      :width="220"
      :collapsed-width="80"
      theme="dark"
      style="box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);"
    >
      <div class="logo">
        <h2 v-if="!collapsed" class="logo-text">
          MyAuth
        </h2>
        <h2 v-else class="logo-text-short">MA</h2>
      </div>
      
      <a-menu
        v-model:selectedKeys="selectedKeys"
        theme="dark"
        mode="inline"
        @click="handleMenuClick"
      >
        <a-menu-item key="/dashboard">
          <template #icon>
            <DashboardOutlined />
          </template>
          <span>仪表盘</span>
        </a-menu-item>
        <a-menu-item key="/licenses">
          <template #icon>
            <KeyOutlined />
          </template>
          <span>授权管理</span>
        </a-menu-item>
        <a-menu-item key="/generate">
          <template #icon>
            <PlusCircleOutlined />
          </template>
          <span>生成授权</span>
        </a-menu-item>
        <a-menu-item key="/keys">
          <template #icon>
            <SafetyCertificateOutlined />
          </template>
          <span>密钥管理</span>
        </a-menu-item>
      </a-menu>
    </a-layout-sider>
    
    <!-- 右侧内容区 -->
    <a-layout class="right-layout">
      <!-- 顶部导航栏 -->
      <a-layout-header class="header">
        <div class="header-left">
          <h2 class="page-title">{{ currentTitle }}</h2>
        </div>
        
        <div class="header-right">
          <a-dropdown>
            <a class="user-info" @click.prevent>
              <UserOutlined class="user-icon" />
              <span class="user-name">{{ realName || username }}</span>
              <DownOutlined class="dropdown-icon" />
            </a>
            <template #overlay>
              <a-menu>
                <a-menu-item key="logout" @click="handleLogout">
                  <LogoutOutlined />
                  退出登录
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
      </a-layout-header>
      
      <!-- 主要内容区 -->
      <a-layout-content class="content">
        <div class="content-wrapper">
          <router-view />
        </div>
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { DashboardOutlined, KeyOutlined, PlusCircleOutlined, SafetyCertificateOutlined, UserOutlined, DownOutlined, LogoutOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'

const router = useRouter()
const route = useRoute()
const collapsed = ref(false)
const selectedKeys = ref([route.path])
const username = ref(localStorage.getItem('username') || '')
const realName = ref(localStorage.getItem('realName') || '')

const currentTitle = computed(() => {
  return route.meta.title || 'MyAuth 授权管理系统'
})

const handleMenuClick = ({ key }) => {
  router.push(key)
  selectedKeys.value = [key]
}

const handleLogout = () => {
  // 清除本地存储
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  localStorage.removeItem('realName')
  localStorage.removeItem('userId')
  
  message.success('已退出登录')
  
  // 跳转到登录页
  router.push('/login')
}

onMounted(() => {
  // 确保用户信息最新
  username.value = localStorage.getItem('username') || ''
  realName.value = localStorage.getItem('realName') || ''
})
</script>

<style scoped>
/* Logo 区域 */
.logo {
  height: 64px;
  background: rgba(255, 255, 255, 0.1);
  margin: 16px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.logo-text {
  color: white;
  font-size: 20px;
  font-weight: 600;
  margin: 0;
  letter-spacing: 1px;
}

.logo-text-short {
  color: white;
  font-size: 18px;
  font-weight: 600;
  margin: 0;
}

/* 右侧布局 */
.right-layout {
  background: #f5f5f5;
}

/* 顶部导航栏 */
.header {
  background: #ffffff;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  height: 64px;
  line-height: 64px;
}

.header-left {
  display: flex;
  align-items: center;
}

.page-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1F2937;
}

.header-right {
  display: flex;
  align-items: center;
}

/* 用户信息 */
.user-info {
  color: #1F2937;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 6px;
  transition: all 0.3s;
}

.user-info:hover {
  background: #f5f5f5;
  color: #111827;
}

.user-icon {
  font-size: 16px;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
}

.dropdown-icon {
  font-size: 12px;
}

/* 内容区域 */
.content {
  margin: 16px;
  overflow: initial;
}

.content-wrapper {
  padding: 24px;
  background: #ffffff;
  min-height: calc(100vh - 96px);
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.06);
}
</style>
