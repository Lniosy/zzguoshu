<template>
  <div class="admin-user-list">
    <el-card class="search-card">
      <el-form :model="searchForm" class="search-form">
        <el-form-item label="用户名">
          <el-input
            v-model="searchForm.username"
            placeholder="请输入用户名"
            prefix-icon="Search"
            :clearable="true"
          />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input
            v-model="searchForm.phone"
            placeholder="请输入手机号"
            prefix-icon="Phone"
            :clearable="true"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择状态"
            prefix-icon="CircleCheck"
            :clearable="true"
            style="width: 120px"
          >
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="角色">
          <el-select
            v-model="searchForm.role"
            placeholder="请选择角色"
            :clearable="true"
            style="width: 140px"
          >
            <el-option label="普通用户" value="USER" />
            <el-option label="商家" value="MERCHANT" />
            <el-option label="子管理员" value="SUB_ADMIN" />
            <el-option label="总管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button type="success" @click="subAdminDialogVisible = true">
            新增子管理员
          </el-button>
          <el-button @click="handleExport">
            导出CSV
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="user-card">
      <el-table
        :data="userList"
        style="width: 100%"
        v-loading="loading"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column prop="phone" label="手机号" min-width="130" />
        <el-table-column prop="gender" label="性别" width="80">
          <template #default="{ row }">
            {{ row.gender === 1 ? '男' : row.gender === 2 ? '女' : '未知' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="role" label="角色" width="110">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'danger' : row.role === 'SUB_ADMIN' ? 'warning' : row.role === 'MERCHANT' ? 'success' : 'info'">
              {{ row.role === 'ADMIN' ? '总管理员' : row.role === 'SUB_ADMIN' ? '子管理员' : row.role === 'MERCHANT' ? '商家' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="160" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              :link="true"
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
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

    <el-dialog v-model="detailDialogVisible" title="用户详情" width="560px">
      <el-descriptions v-loading="detailLoading" :column="2" border>
        <el-descriptions-item label="ID">{{ userDetail.id || '-' }}</el-descriptions-item>
        <el-descriptions-item label="账号">{{ userDetail.username || '-' }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ userDetail.nickname || '-' }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ userDetail.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="角色">{{ userDetail.role || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="userDetail.status === 1 ? 'success' : 'danger'">
            {{ userDetail.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ userDetail.createTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ userDetail.updateTime || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="subAdminDialogVisible" title="新增子管理员" width="520px">
      <el-form :model="subAdminForm" label-width="90px">
        <el-form-item label="账号">
          <el-input v-model="subAdminForm.username" placeholder="请输入登录账号" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="subAdminForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="subAdminForm.nickname" placeholder="请输入显示昵称" />
        </el-form-item>
        <el-form-item label="初始密码">
          <el-input v-model="subAdminForm.password" type="password" show-password placeholder="至少6位" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="subAdminDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitSubAdmin">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { createSubAdmin, exportUsers, getUserDetail, getUserList, toggleUserStatus } from '@/api/admin'
import { ElMessage } from 'element-plus'

// 响应式数据
const loading = ref(false)
const userList = ref([])
const searchForm = reactive({
  username: '',
  phone: '',
  status: '',
  role: ''
})
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})
const selectedUsers = ref([])
const detailDialogVisible = ref(false)
const detailLoading = ref(false)
const userDetail = ref({})
const subAdminDialogVisible = ref(false)
const subAdminForm = reactive({
  username: '',
  phone: '',
  nickname: '',
  password: ''
})

// 方法
const fetchUserList = async () => {
  loading.value = true
  try {
    const params = {
      ...searchForm,
      page: pagination.current,
      pageSize: pagination.pageSize
    }
    const response = await getUserList(params)
    userList.value = response.records
    pagination.total = response.total
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchUserList()
}

const handleReset = () => {
  searchForm.username = ''
  searchForm.phone = ''
  searchForm.status = ''
  searchForm.role = ''
  pagination.current = 1
  fetchUserList()
}

const handleExport = async () => {
  const csv = await exportUsers({ ...searchForm })
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `users-${Date.now()}.csv`
  link.click()
  window.URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
}

const handleToggleStatus = async (row) => {
  if (row.role === 'ADMIN') {
    ElMessage.warning('总管理员账号不可禁用')
    return
  }
  try {
    const status = row.status === 1 ? 0 : 1
    await toggleUserStatus(row.id, status)
    row.status = status
    ElMessage.success(status === 1 ? '用户已启用' : '用户已禁用')
  } catch (error) {
    console.error('切换用户状态失败:', error)
    ElMessage.error('切换用户状态失败')
  }
}

const handleView = async (row) => {
  detailDialogVisible.value = true
  detailLoading.value = true
  try {
    userDetail.value = await getUserDetail(row.id)
  } catch (error) {
    ElMessage.error('获取用户详情失败')
    detailDialogVisible.value = false
  } finally {
    detailLoading.value = false
  }
}

const handleSelectionChange = (selection) => {
  selectedUsers.value = selection
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  fetchUserList()
}

const handleCurrentChange = (current) => {
  pagination.current = current
  fetchUserList()
}

const submitSubAdmin = async () => {
  if (!subAdminForm.username || !subAdminForm.phone || subAdminForm.password.length < 6) {
    ElMessage.warning('请填写完整信息，且密码至少6位')
    return
  }
  try {
    await createSubAdmin({ ...subAdminForm })
    ElMessage.success('子管理员创建成功')
    subAdminDialogVisible.value = false
    subAdminForm.username = ''
    subAdminForm.phone = ''
    subAdminForm.nickname = ''
    subAdminForm.password = ''
    fetchUserList()
  } catch (error) {
    ElMessage.error(error?.message || '创建子管理员失败')
  }
}

// 初始化
fetchUserList()
</script>

<style scoped>
.admin-user-list {
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
