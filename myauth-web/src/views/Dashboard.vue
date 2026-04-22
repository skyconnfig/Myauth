<template>
  <div>
    <a-row :gutter="16">
      <a-col :span="6">
        <a-card>
          <a-statistic title="总授权数" :value="totalLicenses" />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <a-statistic title="活跃授权" :value="activeLicenses" />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <a-statistic title="已过期" :value="expiredLicenses" />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <a-statistic title="即将过期" :value="expiringSoonLicenses" />
        </a-card>
      </a-col>
    </a-row>

    <a-card title="快速操作" style="margin-top: 16px;">
      <a-space>
        <a-button type="primary" @click="goToGenerate">
          <PlusCircleOutlined />
          生成新授权
        </a-button>
        <a-button @click="refreshData">
          <ReloadOutlined />
          刷新数据
        </a-button>
      </a-space>
    </a-card>

    <a-card title="最近授权记录" style="margin-top: 16px;">
      <a-table
        :columns="columns"
        :data-source="recentLicenses"
        :pagination="false"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'green' : 'red'">
              {{ record.status === 1 ? '启用' : '禁用' }}
            </a-tag>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { PlusCircleOutlined, ReloadOutlined } from '@ant-design/icons-vue'
import { listLicenses } from '@/api/license'
import { message } from 'ant-design-vue'

const router = useRouter()
const totalLicenses = ref(0)
const activeLicenses = ref(0)
const expiredLicenses = ref(0)
const expiringSoonLicenses = ref(0)
const recentLicenses = ref([])

const columns = [
  { title: '客户名称', dataIndex: 'customerName', key: 'customerName' },
  { title: '授权类型', dataIndex: 'type', key: 'type' },
  { title: '过期时间', dataIndex: 'expireTime', key: 'expireTime' },
  { title: '状态', key: 'status' }
]

const loadData = async () => {
  try {
    const res = await listLicenses()
    const licenses = res.data || []
    
    totalLicenses.value = licenses.length
    activeLicenses.value = licenses.filter(l => l.status === 1).length
    
    // 计算过期和即将过期的数量
    const now = new Date()
    licenses.forEach(license => {
      const expireDate = new Date(license.expireTime)
      const daysDiff = Math.ceil((expireDate - now) / (1000 * 60 * 60 * 24))
      
      if (daysDiff < 0) {
        expiredLicenses.value++
      } else if (daysDiff <= 30) {
        expiringSoonLicenses.value++
      }
    })
    
    // 最近5条记录
    recentLicenses.value = licenses.slice(0, 5)
  } catch (error) {
    message.error('加载数据失败')
  }
}

const goToGenerate = () => {
  router.push('/generate')
}

const refreshData = () => {
  loadData()
  message.success('数据已刷新')
}

onMounted(() => {
  loadData()
})
</script>
