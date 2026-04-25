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
            :disabled="route.path === '/admin/merchant-applications'"
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

    <!-- 商家详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="商家详情"
      width="760px"
      destroy-on-close
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item label="商家ID">{{ currentMerchant.id }}</el-descriptions-item>
        <el-descriptions-item label="商家名称">{{ currentMerchant.name }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ currentMerchant.contactName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentMerchant.contactPhone }}</el-descriptions-item>
        <el-descriptions-item label="店铺地址">{{ currentMerchant.address }}</el-descriptions-item>
        <el-descriptions-item label="审核状态">
          <el-tag :type="currentMerchant.auditStatus === 1 ? 'success' : currentMerchant.auditStatus === 2 ? 'danger' : 'warning'">
            {{ currentMerchant.auditStatus === 1 ? '审核通过' : currentMerchant.auditStatus === 2 ? '审核拒绝' : '待审核' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentMerchant.createTime }}</el-descriptions-item>
        <el-descriptions-item label="店铺描述" v-if="currentMerchant.shopDescription || currentMerchant.description">
          {{ currentMerchant.shopDescription || currentMerchant.description }}
        </el-descriptions-item>
      </el-descriptions>

      <div class="qualification-section">
        <div class="section-title">入驻资质材料</div>
        <el-empty v-if="!qualificationMaterials.length" description="暂无上传资质" />
        <div v-else class="qualification-grid">
          <div v-for="item in qualificationMaterials" :key="item.name" class="qualification-card">
            <div class="qualification-name">{{ item.name }}</div>
            <el-image
              v-if="isImageMaterial(item)"
              :src="item.url"
              :preview-src-list="imageMaterialUrls"
              fit="cover"
              class="qualification-image"
              preview-teleported
            />
            <el-link v-else type="primary" :href="item.url" target="_blank">查看 PDF 材料</el-link>
          </div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, ref, reactive, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getMerchantList, getMerchantDetail, auditMerchant } from '@/api/admin'
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
const detailDialogVisible = ref(false)
const currentMerchant = ref({})
const isImageMaterial = (item) => {
  const url = String(item?.url || '').toLowerCase()
  return item?.type === 'image' || url.startsWith('data:image/') || /\.(png|jpe?g|webp|gif)(\?|$)/.test(url)
}
const qualificationMaterials = computed(() => {
  const list = currentMerchant.value.qualificationMaterials
  if (Array.isArray(list) && list.length) {
    return list.filter(item => item?.url)
  }
  return [
    { name: '营业执照', url: currentMerchant.value.businessLicense },
    { name: '食品经营许可证', url: currentMerchant.value.foodLicense },
    { name: '法人身份证', url: currentMerchant.value.idCardImage }
  ].filter(item => item.url)
})
const imageMaterialUrls = computed(() => qualificationMaterials.value
  .filter(isImageMaterial)
  .map(item => item.url))

// 监听路由变化，解决组件复用冲突
const initFromRoute = () => {
  if (route.path === '/admin/merchant-applications') {
    searchForm.auditStatus = 0
  } else {
    searchForm.auditStatus = ''
  }
}

watch(() => route.path, () => {
  initFromRoute()
  handleSearch()
})

// 方法
const fetchMerchantList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.current,
      pageSize: pagination.pageSize
    }
    if (searchForm.name) params.name = searchForm.name;
    if (searchForm.contactName) params.contactName = searchForm.contactName;
    if (searchForm.contactPhone) params.contactPhone = searchForm.contactPhone;
    if (searchForm.auditStatus !== '') params.auditStatus = searchForm.auditStatus;

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
  initFromRoute()
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

const handleView = async (row) => {
  try {
    currentMerchant.value = await getMerchantDetail(row.id)
  } catch (error) {
    currentMerchant.value = { ...row }
  }
  detailDialogVisible.value = true
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
onMounted(() => {
  initFromRoute()
  fetchMerchantList()
})
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

.qualification-section {
  margin-top: 18px;
}

.section-title {
  margin-bottom: 12px;
  font-weight: 700;
  color: #244034;
}

.qualification-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 14px;
}

.qualification-card {
  padding: 12px;
  border: 1px solid #e1ece5;
  border-radius: 10px;
  background: #f8fbf9;
}

.qualification-name {
  margin-bottom: 8px;
  font-size: 13px;
  font-weight: 600;
  color: #4b685b;
}

.qualification-image {
  width: 100%;
  height: 132px;
  border-radius: 8px;
  background: #eef5f1;
}
</style>
