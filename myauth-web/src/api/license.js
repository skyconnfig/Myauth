import request from '@/utils/request'

// ==================== 认证接口 ====================

// 用户登录
export const login = (data) => {
  return request.post('/auth/login', data)
}

// 用户注册
export const register = (data) => {
  return request.post('/auth/register', data)
}

// 获取用户信息
export const getUserInfo = () => {
  return request.get('/auth/userinfo')
}

// 修改密码
export const changePassword = (data) => {
  return request.post('/auth/change-password', data)
}

// ==================== License接口 ====================

// 生成密钥对
export const generateKeyPair = () => {
  return request.post('/license/generate-keypair')
}

// 获取公钥
export const getPublicKey = () => {
  return request.get('/license/public-key')
}

// 生成License
export const generateLicense = (data) => {
  return request.post('/license/generate', data)
}

// 分页查询License列表
export const pageLicenses = (params) => {
  return request.get('/license/page', { params })
}

// 获取所有License列表
export const listLicenses = () => {
  return request.get('/license/list')
}

// 根据ID查询License
export const getLicenseById = (id) => {
  return request.get(`/license/${id}`)
}

// 更新License状态
export const updateLicenseStatus = (id, status) => {
  return request.put(`/license/${id}/status`, null, { params: { status } })
}

// 删除License
export const deleteLicense = (id) => {
  return request.delete(`/license/${id}`)
}

// 重新生成License（延期）
export const regenerateLicense = (id, data) => {
  return request.post(`/license/${id}/regenerate`, data)
}
