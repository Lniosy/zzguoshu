<template>
  <div class="favorite-page">
    <div class="page-header">
      <h2>我的收藏</h2>
      <el-button link @click="navigate('/user')">返回用户中心</el-button>
    </div>

    <el-card>
      <el-table :data="list" v-loading="loading">
        <el-table-column label="商品" min-width="220">
          <template #default="{ row }">
            <div class="p-row">
              <el-image :src="row.image" fit="cover" style="width:64px;height:64px;border-radius:8px" />
              <div>
                <div class="name">{{ row.name }}</div>
                <div class="desc">{{ row.description }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="120">
          <template #default="{ row }">¥{{ Number(row.price).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="navigate(`/products/${row.id}`)">查看详情</el-button>
            <el-button link type="danger" @click="remove(row)">取消收藏</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && list.length === 0" description="暂无收藏商品" />
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAppNavigation } from '@/composables/useAppNavigation'
import { ElMessage } from 'element-plus'
import { getFavoriteList, removeFavorite } from '@/api/favorite'

const router = useRouter()
const { navigate } = useAppNavigation()
const list = ref([])
const loading = ref(false)

const fetchList = async () => {
  loading.value = true
  try {
    list.value = await getFavoriteList()
  } finally {
    loading.value = false
  }
}

const remove = async (row) => {
  await removeFavorite(row.id)
  ElMessage.success('已取消收藏')
  fetchList()
}

fetchList()
</script>

<style scoped>
.favorite-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px; }
.page-header h2 { margin: 0; }
.p-row { display: flex; align-items: center; gap: 10px; }
.name { font-weight: 600; }
.desc { color: #5f7469; font-size: 13px; }
</style>
