<template>
  <div class="admin-product-list">
    <el-card class="search-card">
      <el-form :model="searchForm" class="search-form">
        <el-form-item label="商品名称">
          <el-input
            v-model="searchForm.name"
            placeholder="请输入商品名称"
            prefix-icon="Search"
            :clearable="true"
          />
        </el-form-item>
        <el-form-item label="商品分类">
          <el-select
            v-model="searchForm.categoryId"
            placeholder="请选择商品分类"
            prefix-icon="Folder"
            :clearable="true"
            style="width: 120px"
          >
            <el-option
              v-for="category in categoryList"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
          </el-select>
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

    <el-card class="product-card">
      <el-table
        :data="productList"
        style="width: 100%"
        v-loading="loading"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="商品名称" min-width="150">
          <template #default="{ row }">
            <div class="product-info">
              <el-image
                :src="row.coverImage"
                :preview-src-list="row.images ? row.images.split(',') : []"
                style="width: 40px; height: 40px; border-radius: 4px"
                fit="cover"
              />
              <span class="product-name">{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="商品分类" min-width="100" />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">
            ¥{{ (row.price || 0).toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column prop="sales" label="销量" width="80" />
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
import { getProductList, auditProduct } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

// 响应式数据
const loading = ref(false)
const productList = ref([])
const categoryList = ref([
  { id: 1, name: '水果' },
  { id: 2, name: '蔬菜' },
  { id: 3, name: '坚果' },
  { id: 4, name: '肉类' },
  { id: 5, name: '海鲜' }
])
const searchForm = reactive({
  name: '',
  categoryId: '',
  auditStatus: ''
})
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})
const selectedProducts = ref([])

// 方法
const fetchProductList = async () => {
  loading.value = true
  try {
    const params = {
      ...searchForm,
      page: pagination.current,
      pageSize: pagination.pageSize
    }
    const response = await getProductList(params)
    productList.value = response.records
    pagination.total = response.total
  } catch (error) {
    console.error('获取商品列表失败:', error)
    ElMessage.error('获取商品列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchProductList()
}

const handleReset = () => {
  searchForm.name = ''
  searchForm.categoryId = ''
  searchForm.auditStatus = ''
  pagination.current = 1
  fetchProductList()
}

const handleAudit = async (row, status) => {
  try {
    await auditProduct(row.id, status)
    row.auditStatus = status
    ElMessage.success(status === 1 ? '商品审核通过' : '商品审核拒绝')
  } catch (error) {
    console.error('商品审核失败:', error)
    ElMessage.error('商品审核失败')
  }
}

const handleView = (row) => {
  console.log('查看商品详情:', row)
  ElMessage.info('查看商品详情功能待实现')
}

const handleSelectionChange = (selection) => {
  selectedProducts.value = selection
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  fetchProductList()
}

const handleCurrentChange = (current) => {
  pagination.current = current
  fetchProductList()
}

// 初始化
fetchProductList()
</script>

<style scoped>
.admin-product-list {
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

.product-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.product-name {
  flex: 1;
  min-width: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}
</style>
