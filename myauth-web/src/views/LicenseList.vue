<template>
  <div>
    <a-card>
      <div style="margin-bottom: 16px; display: flex; justify-content: space-between;">
        <a-space>
          <a-input
            v-model:value="searchCustomerName"
            placeholder="搜索客户名称"
            style="width: 200px"
            @pressEnter="handleSearch"
          />
          <a-button type="primary" @click="handleSearch">搜索</a-button>
          <a-button @click="handleReset">重置</a-button>
        </a-space>
        <a-button type="primary" @click="goToGenerate">
          <PlusOutlined />
          生成授权
        </a-button>
      </div>

      <a-table
        :columns="columns"
        :data-source="licenseList"
        :pagination="pagination"
        :loading="loading"
        row-key="id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'type'">
            <a-tag :color="getTypeColor(record.type)">
              {{ getTypeText(record.type) }}
            </a-tag>
          </template>
          
          <template v-if="column.key === 'status'">
            <a-switch
              v-model:checked="record.status"
              :checked-value="1"
              :un-checked-value="0"
              @change="handleStatusChange(record)"
            />
          </template>
          
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button size="small" @click="handleView(record)">查看</a-button>
              <a-button size="small" @click="handleRegenerate(record)">延期</a-button>
              <a-popconfirm
                title="确定删除此授权吗？"
                ok-text="确定"
                cancel-text="取消"
                @confirm="handleDelete(record.id)"
              >
                <a-button size="small" danger>删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 查看详情弹窗 -->
    <a-modal
      v-model:open="viewModalVisible"
      title="授权详情"
      :footer="null"
      width="600px"
    >
      <a-descriptions bordered :column="2" v-if="currentLicense">
        <a-descriptions-item label="客户名称">
          {{ currentLicense.customerName }}
        </a-descriptions-item>
        <a-descriptions-item label="授权类型">
          {{ getTypeText(currentLicense.type) }}
        </a-descriptions-item>
        <a-descriptions-item label="过期时间">
          {{ currentLicense.expireTime }}
        </a-descriptions-item>
        <a-descriptions-item label="最大用户数">
          {{ currentLicense.maxUsers }}
        </a-descriptions-item>
        <a-descriptions-item label="机器码">
          {{ currentLicense.machineId || '无限制' }}
        </a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="currentLicense.status === 1 ? 'green' : 'red'">
            {{ currentLicense.status === 1 ? '启用' : '禁用' }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="备注" :span="2">
          {{ currentLicense.remark || '无' }}
        </a-descriptions-item>
        <a-descriptions-item label="授权码" :span="2">
          <a-textarea
            :value="currentLicense.licenseKey"
            :rows="4"
            readonly
          />
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- 延期弹窗 -->
    <a-modal
      v-model:open="regenerateModalVisible"
      title="延期授权"
      @ok="handleRegenerateSubmit"
      :confirmLoading="regenerateLoading"
    >
      <a-form :model="regenerateForm" layout="vertical">
        <a-form-item label="客户名称">
          <a-input :value="regenerateForm.customerName" disabled />
        </a-form-item>
        <a-form-item label="当前过期时间">
          <a-input :value="regenerateForm.oldExpireTime" disabled>
            <template #suffix>
              <a-tag color="orange" v-if="regenerateForm.oldExpireTime">即将到期</a-tag>
            </template>
          </a-input>
        </a-form-item>
        <a-form-item label="新的过期时间" required>
          <a-date-picker
            v-model:value="regenerateForm.expireTime"
            style="width: 100%"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            :disabled-date="disabledDate"
          />
        </a-form-item>
        <a-form-item label="备注">
          <a-textarea v-model:value="regenerateForm.remark" :rows="3" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 延期结果弹窗 -->
    <a-modal
      v-model:open="resultModalVisible"
      title="延期成功"
      :footer="null"
      width="720px"
    >
      <a-result
        status="success"
        title="授权已成功延期"
      >
        <template #subTitle>
          <a-descriptions :column="2" size="small">
            <a-descriptions-item label="客户名称">
              <strong>{{ renewResult.customerName }}</strong>
            </a-descriptions-item>
            <a-descriptions-item label="授权类型">
              <a-tag color="green">{{ renewResult.type }}</a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="原过期时间">
              <span style="color: #999; text-decoration: line-through;">{{ renewResult.oldExpireTime }}</span>
            </a-descriptions-item>
            <a-descriptions-item label="新过期时间">
              <strong style="color: #52c41a; font-size: 16px;">{{ renewResult.newExpireTime }}</strong>
            </a-descriptions-item>
          </a-descriptions>
        </template>

        <template #extra>
          <a-card title="授权文件 (license.key)" size="small" style="text-align: left; margin-top: 16px;">
            <a-textarea
              :value="renewResult.licenseKey"
              :rows="5"
              readonly
              style="font-family: monospace; font-size: 12px;"
            />
            <a-space style="margin-top: 12px;">
              <a-button type="primary" @click="copyLicenseKey">
                <CopyOutlined /> 复制授权码
              </a-button>
              <a-button @click="handleDownloadLicenseKey">
                <DownloadOutlined /> 下载 license.key 文件
              </a-button>
            </a-space>
          </a-card>

          <a-alert
            type="info"
            show-icon
            style="margin-top: 16px; text-align: left;"
          >
            <template #message>
              <strong>客户端/Agent部署步骤</strong>
            </template>
            <template #description>
              <ol style="margin: 4px 0 0 0; padding-left: 20px; line-height: 2;">
                <li>将 <code>license.key</code> 文件放置到程序目录的 <code>license/</code> 文件夹下</li>
                <li>确保 <code>license/public.key</code> 文件存在（公钥不变，无需替换）</li>
                <li>重启目标程序（或使用 <code>java -javaagent:myauth-agent.jar -jar your-app.jar</code> 启动）</li>
                <li>启动日志中会显示新的过期时间，确认延期生效</li>
              </ol>
            </template>
          </a-alert>

          <a-space style="margin-top: 16px; width: 100%; justify-content: center;">
            <a-button type="primary" @click="resultModalVisible = false">
              完成
            </a-button>
          </a-space>
        </template>
      </a-result>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { PlusOutlined, CopyOutlined, DownloadOutlined } from '@ant-design/icons-vue'
import { pageLicenses, updateLicenseStatus, deleteLicense, regenerateLicense, downloadLicenseFile } from '@/api/license'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'

const router = useRouter()
const loading = ref(false)
const licenseList = ref([])
const searchCustomerName = ref('')
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0
})

const viewModalVisible = ref(false)
const currentLicense = ref(null)
const regenerateModalVisible = ref(false)
const regenerateLoading = ref(false)
const regenerateForm = ref({
  customerName: '',
  oldExpireTime: '',
  expireTime: null,
  remark: ''
})
const currentRegenerateId = ref(null)

// 延期结果
const resultModalVisible = ref(false)
const renewResult = ref({
  customerName: '',
  type: '',
  oldExpireTime: '',
  newExpireTime: '',
  licenseKey: ''
})

const columns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
  { title: '客户名称', dataIndex: 'customerName', key: 'customerName' },
  { title: '授权类型', key: 'type' },
  { title: '过期时间', dataIndex: 'expireTime', key: 'expireTime' },
  { title: '最大用户数', dataIndex: 'maxUsers', key: 'maxUsers' },
  { title: '状态', key: 'status', width: 100 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 250, fixed: 'right' }
]

const loadData = async () => {
  loading.value = true
  try {
    const res = await pageLicenses({
      current: pagination.value.current,
      size: pagination.value.pageSize,
      customerName: searchCustomerName.value
    })
    
    licenseList.value = res.data.records || []
    pagination.value.total = res.data.total || 0
  } catch (error) {
    message.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.value.current = 1
  loadData()
}

const handleReset = () => {
  searchCustomerName.value = ''
  pagination.value.current = 1
  loadData()
}

const handleTableChange = (pag) => {
  pagination.value.current = pag.current
  pagination.value.pageSize = pag.pageSize
  loadData()
}

const handleStatusChange = async (record) => {
  try {
    await updateLicenseStatus(record.id, record.status)
    message.success('状态更新成功')
  } catch (error) {
    record.status = record.status === 1 ? 0 : 1 // 恢复原状态
  }
}

const handleView = (record) => {
  currentLicense.value = record
  viewModalVisible.value = true
}

const handleRegenerate = (record) => {
  currentRegenerateId.value = record.id
  currentLicense.value = record
  regenerateForm.value = {
    customerName: record.customerName,
    oldExpireTime: record.expireTime,
    expireTime: dayjs().add(1, 'year').format('YYYY-MM-DD'),
    remark: '授权延期'
  }
  regenerateModalVisible.value = true
}

const handleRegenerateSubmit = async () => {
  if (!regenerateForm.value.expireTime) {
    message.warning('请选择新的过期时间')
    return
  }
  
  regenerateLoading.value = true
  try {
    const res = await regenerateLicense(currentRegenerateId.value, {
      expireTime: regenerateForm.value.expireTime,
      remark: regenerateForm.value.remark,
      customerName: currentLicense.value?.customerName,
      type: currentLicense.value?.type,
      maxUsers: currentLicense.value?.maxUsers
    })
    
    // 保存延期结果用于展示
    renewResult.value = {
      customerName: currentLicense.value?.customerName || '',
      type: currentLicense.value?.type || '',
      oldExpireTime: currentLicense.value?.expireTime || '',
      newExpireTime: regenerateForm.value.expireTime,
      licenseKey: res.data.licenseKey
    }
    
    regenerateModalVisible.value = false
    message.success('授权延期成功')
    
    // 打开结果弹窗
    setTimeout(() => {
      resultModalVisible.value = true
    }, 300)
    
    loadData()
  } catch (error) {
    message.error('延期失败: ' + (error.response?.data?.message || error.message))
  } finally {
    regenerateLoading.value = false
  }
}

// 复制授权码
const copyLicenseKey = () => {
  navigator.clipboard.writeText(renewResult.value.licenseKey).then(() => {
    message.success('授权码已复制到剪贴板')
  }).catch(() => {
    message.error('复制失败，请手动选择复制')
  })
}

// 下载 license.key 文件
const handleDownloadLicenseKey = () => {
  downloadLicenseFile(renewResult.value.licenseKey)
  message.success('license.key 文件已下载')
}

// 禁止选择今天之前的日期
const disabledDate = (current) => {
  return current && current < dayjs().startOf('day')
}

const handleDelete = async (id) => {
  try {
    await deleteLicense(id)
    message.success('删除成功')
    loadData()
  } catch (error) {
    message.error('删除失败')
  }
}

const goToGenerate = () => {
  router.push('/generate')
}

const getTypeColor = (type) => {
  const colors = {
    trial: 'default',
    standard: 'blue',
    professional: 'purple',
    enterprise: 'green'
  }
  return colors[type] || 'default'
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

onMounted(() => {
  loadData()
})
</script>
