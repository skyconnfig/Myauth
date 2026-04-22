/**
 * Vue 3 License 验证工具
 * 用于前端客户端的 License 验证和管理
 */

import axios from 'axios'

// ==================== 配置 ====================

const CONFIG = {
  // API 基础地址
  API_BASE_URL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  
  // License 文件存储位置（localStorage key）
  LICENSE_STORAGE_KEY: 'myauth_license_key',
  
  // License 信息存储位置
  LICENSE_INFO_KEY: 'myauth_license_info',
  
  // 检查间隔（毫秒）- 每小时检查一次
  CHECK_INTERVAL: 3600000
}

// ==================== License 验证类 ====================

class LicenseValidator {
  constructor() {
    this.licenseKey = null
    this.licenseInfo = null
    this.checkTimer = null
  }

  /**
   * 初始化 License 验证器
   */
  async initialize() {
    // 从本地存储读取 License Key
    this.licenseKey = localStorage.getItem(CONFIG.LICENSE_STORAGE_KEY)
    
    if (this.licenseKey) {
      try {
        await this.validateLicense(this.licenseKey)
        console.log('✅ License 验证成功')
        
        // 启动定时检查
        this.startPeriodicCheck()
        
        return true
      } catch (error) {
        console.error('❌ License 验证失败:', error.message)
        this.clearLicense()
        return false
      }
    }
    
    console.warn('⚠️  未找到 License，将以试用模式运行')
    return false
  }

  /**
   * 验证 License Key
   * @param {string} licenseKey - License 密钥
   * @returns {Promise<Object>} License 信息
   */
  async validateLicense(licenseKey) {
    try {
      const response = await axios.post(`${CONFIG.API_BASE_URL}/api/license/validate`, {
        license_key: licenseKey
      })

      if (response.data.code === 200) {
        this.licenseKey = licenseKey
        this.licenseInfo = response.data.data
        
        // 保存到本地存储
        localStorage.setItem(CONFIG.LICENSE_STORAGE_KEY, licenseKey)
        localStorage.setItem(CONFIG.LICENSE_INFO_KEY, JSON.stringify(this.licenseInfo))
        
        return this.licenseInfo
      } else {
        throw new Error(response.data.message || 'License 验证失败')
      }
    } catch (error) {
      if (error.response) {
        throw new Error(error.response.data.message || 'License 验证失败')
      }
      throw error
    }
  }

  /**
   * 激活 License
   * @param {string} licenseKey - License 密钥
   * @returns {Promise<Object>} License 信息
   */
  async activate(licenseKey) {
    try {
      const result = await this.validateLicense(licenseKey)
      
      // 触发事件
      window.dispatchEvent(new CustomEvent('license:activated', {
        detail: result
      }))
      
      return result
    } catch (error) {
      window.dispatchEvent(new CustomEvent('license:error', {
        detail: { message: error.message }
      }))
      throw error
    }
  }

  /**
   * 清除 License
   */
  clearLicense() {
    this.licenseKey = null
    this.licenseInfo = null
    localStorage.removeItem(CONFIG.LICENSE_STORAGE_KEY)
    localStorage.removeItem(CONFIG.LICENSE_INFO_KEY)
    
    // 停止定时检查
    this.stopPeriodicCheck()
    
    // 触发事件
    window.dispatchEvent(new CustomEvent('license:cleared'))
  }

  /**
   * 获取 License 信息
   * @returns {Object|null} License 信息
   */
  getLicenseInfo() {
    if (this.licenseInfo) {
      return this.licenseInfo
    }
    
    // 从本地存储读取
    const infoStr = localStorage.getItem(CONFIG.LICENSE_INFO_KEY)
    if (infoStr) {
      try {
        return JSON.parse(infoStr)
      } catch (e) {
        return null
      }
    }
    
    return null
  }

  /**
   * 检查 License 是否有效
   * @returns {boolean} 是否有效
   */
  isValid() {
    const info = this.getLicenseInfo()
    if (!info) return false
    
    // 检查状态
    if (info.status !== 'valid') return false
    
    // 检查剩余天数
    if (info.remaining_days < 0) return false
    
    return true
  }

  /**
   * 检查是否为试用模式
   * @returns {boolean} 是否为试用模式
   */
  isTrialMode() {
    const info = this.getLicenseInfo()
    return !info || info.status === 'trial'
  }

  /**
   * 检查授权类型
   * @param {string[]} requiredTypes - 需要的授权类型列表
   * @returns {boolean} 是否符合要求
   */
  checkType(requiredTypes) {
    const info = this.getLicenseInfo()
    if (!info) return false
    
    return requiredTypes.includes(info.type)
  }

  /**
   * 检查功能模块权限
   * @param {string} moduleName - 模块名称
   * @returns {boolean} 是否有权限
   */
  hasModulePermission(moduleName) {
    const info = this.getLicenseInfo()
    if (!info) return false
    
    // 试用模式只有 basic 模块
    if (info.status === 'trial') {
      return moduleName === 'basic'
    }
    
    return info.modules && info.modules.includes(moduleName)
  }

  /**
   * 检查用户数量限制
   * @param {number} currentUserCount - 当前用户数
   * @returns {boolean} 是否在限制内
   */
  checkUserLimit(currentUserCount) {
    const info = this.getLicenseInfo()
    if (!info) return false
    
    return currentUserCount <= info.max_users
  }

  /**
   * 获取剩余天数
   * @returns {number} 剩余天数
   */
  getRemainingDays() {
    const info = this.getLicenseInfo()
    if (!info) return 0
    
    return info.remaining_days || 0
  }

  /**
   * 启动定时检查
   */
  startPeriodicCheck() {
    this.stopPeriodicCheck() // 先停止已有的定时器
    
    this.checkTimer = setInterval(async () => {
      try {
        if (this.licenseKey) {
          await this.validateLicense(this.licenseKey)
          console.log('✅ 定时 License 检查通过')
          
          // 触发事件
          window.dispatchEvent(new CustomEvent('license:check:success'))
        }
      } catch (error) {
        console.error('❌ 定时 License 检查失败:', error.message)
        
        // 触发事件
        window.dispatchEvent(new CustomEvent('license:check:failed', {
          detail: { message: error.message }
        }))
        
        // 如果验证失败，清除 License
        this.clearLicense()
      }
    }, CONFIG.CHECK_INTERVAL)
  }

  /**
   * 停止定时检查
   */
  stopPeriodicCheck() {
    if (this.checkTimer) {
      clearInterval(this.checkTimer)
      this.checkTimer = null
    }
  }

  /**
   * 手动检查 License
   */
  async manualCheck() {
    if (this.licenseKey) {
      return await this.validateLicense(this.licenseKey)
    }
    return null
  }
}

// ==================== 创建单例 ====================

const licenseValidator = new LicenseValidator()

// ==================== Vue 插件 ====================

/**
 * Vue 3 插件
 */
export default {
  install: (app, options = {}) => {
    // 合并配置
    Object.assign(CONFIG, options)
    
    // 提供全局属性
    app.config.globalProperties.$license = licenseValidator
    
    // 提供组合式 API
    app.provide('license', licenseValidator)
    
    // 全局指令：需要 License 验证的元素
    app.directive('license', {
      mounted(el, binding) {
        const requiredType = binding.value
        
        if (requiredType) {
          // 检查授权类型
          if (!licenseValidator.checkType([requiredType])) {
            el.style.display = 'none'
          }
        } else {
          // 检查是否有有效 License
          if (!licenseValidator.isValid()) {
            el.style.display = 'none'
          }
        }
      }
    })
  }
}

// ==================== 导出 ====================

export { licenseValidator, LicenseValidator, CONFIG }
