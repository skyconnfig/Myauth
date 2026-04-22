<template>
  <a-config-provider
    :theme="{
      token: {
        colorPrimary: '#111827',
        colorPrimaryHover: '#000000',
        colorTextBase: '#1F2937',
        colorTextSecondary: '#6B7280',
        colorBorder: '#E5E7EB'
      }
    }"
  >
    <router-view />
  </a-config-provider>
</template>

<script setup>
</script>

<style>
/* 全局样式优化 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial,
    'Noto Sans', sans-serif, 'Apple Color Emoji', 'Segoe UI Emoji', 'Segoe UI Symbol',
    'Noto Color Emoji';
  background-color: #f5f5f5;
}

#app {
  width: 100%;
  height: 100vh;
}
</style>
