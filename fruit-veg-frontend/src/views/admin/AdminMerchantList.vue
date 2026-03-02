<template>
  <div class="admin-merchant-list">
    <el-card class="search-card">
      <el-form :model="searchForm" class="search-form">
        <el-form-item label="商家名称">
          <el-input
            v-model="searchForm.name"
            placeholder="请输入商家名称"
            prefix-icon="Search"
            :clearable="true"
          />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input
            v-model="searchForm.contactName"
            placeholder="请输入联系人"
            prefix-icon="User"
            :clearable="true"
          />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input
            v-model="searchForm.contactPhone"
            placeholder="请输入联系电话"
            prefix-icon="Phone"
            :clearable="true"
          />
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select
            v-model="searchForm.auditStatus"
            placeholder="请选择审核状态"
            prefix-icon="CircleCheck"
            :clearable="true"
            style="width: 120px"
          >
            <el-option label="待审核" :value="0" />
            <el-option label="审核通过" :value="1" />
            <el-option label="审核拒绝" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="merchant-card">
      <el-table
        :data="merchantList"
        style="width: 100%"
        v-loading="loading"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="商家名称" min-width="150" />
        <el-table-column prop="contactName" label="联系人" min-width="100" />
        <el-table-column prop="contactPhone" label="联系电话" min-width="130" />
        <el-table-column prop="address" label="店铺地址" min-width="150" />
        <el-table-column prop="auditStatus" label="审核状态" width="100">
          <template #default="{ row }">
            <el-tag
              :type="
                row.auditStatus === 1
                  ? 'success'
                  : row.auditStatus === 2
                  ? 'danger'
                  : 'warning'
              "
            >
              {{
                row.auditStatus === 1
                  ? '审核通过'
                  : row.auditStatus === 2
                  ? '审核拒绝'
                  : '待审核'
              }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="160" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              :link="true"
              @click="handleAudit(row, 1)"
              v-if="row.auditStatus === 0"
            >
              审核通过
            </el-button>
            <el-button
              link
              :link="true"
              @click="handleAudit(row, 2)"
              v-if="row.auditStatus === 0"
            >
              审核拒绝
            </el-button>
            <el-button link :link="true" @click="handleView(row)">
              查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        class="pagination"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRoute } from 'vue-router'
import { getMerchantList, auditMerchant } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

// 响应式数据
const loading = ref(false)
const route = useRoute()
const merchantList = ref([])
const searchForm = reactive({
  name: '',
  contactName: '',
  contactPhone: '',
  auditStatus: ''
})
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})
const selectedMerchants = ref([])

// 方法
const fetchMerchantList = async () => {
  loading.value = true
  try {
    const params = {
      ...searchForm,
      page: pagination.current,
      pageSize: pagination.pageSize
    }
    const response = await getMerchantList(params)
    merchantList.value = response.records
    pagination.total = response.total
  } catch (error) {
    console.error('获取商家列表失败:', error)
    ElMessage.error('获取商家列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchMerchantList()
}

const handleReset = () => {
  searchForm.name = ''
  searchForm.contactName = ''
  searchForm.contactPhone = ''
  searchForm.auditStatus = ''
  pagination.current = 1
  fetchMerchantList()
}

const handleAudit = async (row, status) => {
  try {
    await auditMerchant(row.id, status)
    row.auditStatus = status
    ElMessage.success(status === 1 ? '商家审核通过' : '商家审核拒绝')
  } catch (error) {
    console.error('商家审核失败:', error)
    ElMessage.error('商家审核失败')
  }
}

const handleView = (row) => {
  console.log('查看商家详情:', row)
  ElMessage.info('查看商家详情功能待实现')
}

const handleSelectionChange = (selection) => {
  selectedMerchants.value = selection
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  fetchMerchantList()
}

const handleCurrentChange = (current) => {
  pagination.current = current
  fetchMerchantList()
}

// 初始化
if (route.path === '/admin/merchant-applications') {
  searchForm.auditStatus = 0
}
fetchMerchantList()
</script>

<style scoped>
.admin-merchant-list {
  padding: 0;
}

.search-card {
  margin-bottom: 20px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: end;
}

.search-form :deep(.el-form-item) {
  margin-bottom: 0;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}
</style>
