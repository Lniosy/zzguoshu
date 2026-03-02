<template>
  <div class="admin-content-manage">
    <el-tabs>
      <el-tab-pane label="轮播图管理">
        <el-card>
          <template #header>
            <div class="head-row">
              <h3>首页轮播图</h3>
              <el-button type="primary" @click="openBannerDialog()">新增轮播</el-button>
            </div>
          </template>
          <el-table :data="banners" v-loading="loadingBanner">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="title" label="标题" min-width="180" />
            <el-table-column label="预览" width="120">
              <template #default="{ row }">
                <el-image :src="row.imageUrl" fit="cover" style="width:90px;height:56px;border-radius:6px" />
              </template>
            </el-table-column>
            <el-table-column prop="targetUrl" label="跳转链接" min-width="180" />
            <el-table-column label="操作" width="160" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openBannerDialog(row)">编辑</el-button>
                <el-button link type="danger" @click="removeBanner(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="公告资讯管理">
        <el-card>
          <template #header>
            <div class="head-row">
              <h3>公告与行业资讯</h3>
              <el-button type="primary" @click="openNoticeDialog()">发布公告</el-button>
            </div>
          </template>
          <el-table :data="notices" v-loading="loadingNotice">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="type" label="类型" width="110" />
            <el-table-column prop="title" label="标题" min-width="220" />
            <el-table-column prop="content" label="正文" min-width="260" show-overflow-tooltip />
            <el-table-column prop="publishTime" label="发布时间" width="170" />
            <el-table-column label="操作" width="160" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openNoticeDialog(row)">编辑</el-button>
                <el-button link type="danger" @click="removeNotice(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="bannerVisible" :title="bannerForm.id ? '编辑轮播' : '新增轮播'" width="520px">
      <el-form :model="bannerForm" label-width="84px">
        <el-form-item label="标题"><el-input v-model="bannerForm.title" /></el-form-item>
        <el-form-item label="图片URL"><el-input v-model="bannerForm.imageUrl" /></el-form-item>
        <el-form-item label="跳转链接"><el-input v-model="bannerForm.targetUrl" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bannerVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitBanner">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="noticeVisible" :title="noticeForm.id ? '编辑公告' : '发布公告'" width="520px">
      <el-form :model="noticeForm" label-width="84px">
        <el-form-item label="类型">
          <el-select v-model="noticeForm.type" style="width:100%">
            <el-option label="平台公告" value="平台公告" />
            <el-option label="行业资讯" value="行业资讯" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题"><el-input v-model="noticeForm.title" maxlength="120" /></el-form-item>
        <el-form-item label="正文">
          <el-input v-model="noticeForm.content" type="textarea" :rows="4" maxlength="1000" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="noticeVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitNotice">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  deleteAdminBanner,
  deleteAdminNotice,
  getAdminBanners,
  getAdminNotices,
  saveAdminBanner,
  saveAdminNotice
} from '@/api/admin'

const banners = ref([])
const notices = ref([])
const loadingBanner = ref(false)
const loadingNotice = ref(false)
const saving = ref(false)

const bannerVisible = ref(false)
const noticeVisible = ref(false)

const bannerForm = ref({ id: null, title: '', imageUrl: '', targetUrl: '/' })
const noticeForm = ref({ id: null, type: '平台公告', title: '', content: '' })

const fetchBanners = async () => {
  loadingBanner.value = true
  try {
    const data = await getAdminBanners({ page: 1, pageSize: 100 })
    banners.value = data.records || []
  } finally {
    loadingBanner.value = false
  }
}

const fetchNotices = async () => {
  loadingNotice.value = true
  try {
    const data = await getAdminNotices({ page: 1, pageSize: 100 })
    notices.value = data.records || []
  } finally {
    loadingNotice.value = false
  }
}

const openBannerDialog = (row = null) => {
  bannerForm.value = row
    ? { id: row.id, title: row.title, imageUrl: row.imageUrl, targetUrl: row.targetUrl }
    : { id: null, title: '', imageUrl: '', targetUrl: '/' }
  bannerVisible.value = true
}

const openNoticeDialog = (row = null) => {
  noticeForm.value = row
    ? { id: row.id, type: row.type, title: row.title, content: row.content || '' }
    : { id: null, type: '平台公告', title: '', content: '' }
  noticeVisible.value = true
}

const submitBanner = async () => {
  if (!bannerForm.value.title.trim()) return ElMessage.warning('请输入标题')
  if (!bannerForm.value.imageUrl.trim()) return ElMessage.warning('请输入图片URL')
  saving.value = true
  try {
    await saveAdminBanner(bannerForm.value)
    ElMessage.success('保存成功')
    bannerVisible.value = false
    fetchBanners()
  } finally {
    saving.value = false
  }
}

const submitNotice = async () => {
  if (!noticeForm.value.title.trim()) return ElMessage.warning('请输入标题')
  if (!noticeForm.value.content.trim()) return ElMessage.warning('请输入正文')
  saving.value = true
  try {
    await saveAdminNotice(noticeForm.value)
    ElMessage.success('保存成功')
    noticeVisible.value = false
    fetchNotices()
  } finally {
    saving.value = false
  }
}

const removeBanner = async (row) => {
  await ElMessageBox.confirm(`确认删除轮播“${row.title}”？`, '删除确认', { type: 'warning' })
  await deleteAdminBanner(row.id)
  ElMessage.success('已删除')
  fetchBanners()
}

const removeNotice = async (row) => {
  await ElMessageBox.confirm(`确认删除公告“${row.title}”？`, '删除确认', { type: 'warning' })
  await deleteAdminNotice(row.id)
  ElMessage.success('已删除')
  fetchNotices()
}

fetchBanners()
fetchNotices()
</script>

<style scoped>
.head-row { display:flex; align-items:center; justify-content:space-between; }
.head-row h3 { margin:0; }
</style>
