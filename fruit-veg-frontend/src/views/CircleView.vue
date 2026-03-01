<template>
  <div class="circle-page">
    <div class="page-header">
      <h2>果蔬圈</h2>
      <div>
        <el-button link @click="navigate('/')">返回首页</el-button>
        <el-button v-if="isLoggedIn" type="primary" plain @click="navigate('/merchant/shop')">发布动态</el-button>
      </div>
    </div>
    <div class="tabs-wrap">
      <el-radio-group v-model="feedMode" @change="handleFeedModeChange">
        <el-radio-button value="recommend">推荐动态</el-radio-button>
        <el-radio-button value="followed">关注动态</el-radio-button>
      </el-radio-group>
    </div>

    <el-row :gutter="16">
      <el-col :span="16">
        <el-card v-for="post in list" :key="post.id" class="post-card">
          <template #header>
            <div class="post-head">
              <div class="merchant-entry" @click="navigate(`/store/${post.merchantId}`)">
                <el-avatar :size="38" :src="post.merchantAvatar">{{ post.merchantName?.[0] || '店' }}</el-avatar>
                <div>
                <h3>{{ post.title }}</h3>
                <small>{{ post.merchantName }} · {{ post.createTime }}</small>
                </div>
              </div>
              <div class="head-right">
                <el-tag effect="plain">{{ post.viewCount }} 浏览</el-tag>
                <el-button v-if="isLoggedIn" size="small" :type="post.isFollowed ? 'info' : 'primary'" plain @click="toggleFollow(post)">
                  {{ post.isFollowed ? '已关注' : '关注商家' }}
                </el-button>
              </div>
            </div>
          </template>
          <p class="post-content">{{ post.content }}</p>
          <div class="post-images">
            <el-image v-for="img in post.images" :key="img" :src="img" fit="cover" class="img" preview-teleported />
          </div>
          <div class="post-actions">
            <el-button @click="onLike(post)">👍 点赞 {{ post.likeCount }}</el-button>
            <el-button @click="openComment(post)">💬 评论 {{ post.commentCount }}</el-button>
            <el-button type="primary" @click="toProduct(post)">去购买</el-button>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card>
          <template #header><h3>热门话题</h3></template>
          <el-tag v-for="tag in tags" :key="tag" class="topic"># {{ tag }}</el-tag>
        </el-card>
        <el-card v-if="isLoggedIn" class="follow-card">
          <template #header><h3>我关注的商家</h3></template>
          <el-empty v-if="!followedMerchants.length" description="还没有关注商家" />
          <div v-for="merchant in followedMerchants" :key="merchant.id" class="follow-item">
            <div class="name" @click="navigate(`/store/${merchant.id}`)">{{ merchant.shopName || merchant.name }}</div>
            <el-button size="small" link type="danger" @click="removeFollowedMerchant(merchant.id)">取消关注</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="commentVisible" title="评论互动" width="520px">
      <div v-if="activePost">
        <p class="comment-title">{{ activePost.title }}</p>
        <el-timeline>
          <el-timeline-item v-for="c in activePost.comments || []" :key="c.id" :timestamp="c.createTime">
            <b>{{ c.nickname }}</b>：{{ c.content }}
          </el-timeline-item>
        </el-timeline>
        <el-input v-model="commentText" type="textarea" :rows="3" maxlength="200" show-word-limit placeholder="写下你的看法..." />
      </div>
      <template #footer>
        <el-button @click="commentVisible = false">取消</el-button>
        <el-button type="primary" @click="submitComment">发布评论</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAppNavigation } from '@/composables/useAppNavigation'
import { ElMessage } from 'element-plus'
import {
  commentCircle,
  followMerchant,
  getCircleDetail,
  getCircleList,
  getFollowedMerchants,
  likeCircle,
  unfollowMerchant
} from '@/api/circle'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const { navigate } = useAppNavigation()
const userStore = useUserStore()
const isLoggedIn = computed(() => userStore.getIsLoggedIn)

const list = ref([])
const followedMerchants = ref([])
const feedMode = ref('recommend')
const tags = ['当季鲜采', '有机种植', '冷链配送', '农残检测', '基地直发']
const commentVisible = ref(false)
const activePost = ref(null)
const commentText = ref('')

const ensureLogin = () => {
  if (isLoggedIn.value) return true
  ElMessage.warning('登录后可点赞和评论')
  navigate('/login')
  return false
}

const fetchList = async () => {
  const data = await getCircleList({ page: 1, size: 20, followedOnly: feedMode.value === 'followed' })
  list.value = data.records || []
}

const fetchFollowedMerchants = async () => {
  if (!isLoggedIn.value) return
  followedMerchants.value = await getFollowedMerchants()
}

const onLike = async (post) => {
  if (!ensureLogin()) return
  await likeCircle(post.id)
  post.likeCount += 1
  ElMessage.success('点赞成功')
}

const openComment = async (post) => {
  if (!ensureLogin()) return
  activePost.value = await getCircleDetail(post.id)
  commentText.value = ''
  commentVisible.value = true
}

const submitComment = async () => {
  if (!commentText.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  await commentCircle(activePost.value.id, { content: commentText.value.trim() })
  ElMessage.success('评论成功')
  commentVisible.value = false
  fetchList()
}

const toProduct = (post) => {
  const pid = post.productIds?.[0]
  if (pid) navigate(`/products/${pid}`)
}

const handleFeedModeChange = async () => {
  if (feedMode.value === 'followed' && !ensureLogin()) {
    feedMode.value = 'recommend'
    return
  }
  await fetchList()
}

const toggleFollow = async (post) => {
  if (!ensureLogin()) return
  if (post.isFollowed) {
    await unfollowMerchant(post.merchantId)
    post.isFollowed = false
    ElMessage.success('已取消关注')
  } else {
    await followMerchant(post.merchantId)
    post.isFollowed = true
    ElMessage.success('关注成功')
  }
  await fetchFollowedMerchants()
}

const removeFollowedMerchant = async (merchantId) => {
  await unfollowMerchant(merchantId)
  ElMessage.success('已取消关注')
  await Promise.all([fetchFollowedMerchants(), fetchList()])
}

onMounted(async () => {
  await fetchList()
  await fetchFollowedMerchants()
})
</script>

<style scoped>
.circle-page { padding: 20px; }
.tabs-wrap {
  margin-bottom: 12px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.page-header h2 { margin: 0; }
.post-card { margin-bottom: 14px; }
.post-head { display: flex; justify-content: space-between; align-items: center; }
.merchant-entry { display: flex; align-items: center; gap: 10px; cursor: pointer; }
.head-right { display: flex; gap: 8px; align-items: center; }
.post-head h3 { margin: 0 0 4px; }
.post-content { color: #4c6358; line-height: 1.7; }
.post-images { display: flex; gap: 10px; margin: 12px 0; flex-wrap: wrap; }
.img { width: 130px; height: 100px; border-radius: 8px; overflow: hidden; }
.post-actions { display: flex; gap: 8px; flex-wrap: wrap; }
.topic { margin: 0 8px 8px 0; }
.comment-title { margin-top: 0; font-weight: 600; }
.follow-card {
  margin-top: 12px;
}
.follow-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #eef0f5;
}
.follow-item:last-child {
  border-bottom: none;
}
.follow-item .name {
  color: #2f3d33;
  font-weight: 500;
  cursor: pointer;
}
</style>
