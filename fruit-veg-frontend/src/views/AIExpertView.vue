<template>
  <div class="ai-page">
    <div class="hero">
      <div>
        <div class="hero-title-line">
          <h1>AI 果蔬专家</h1>
          <el-tag type="success" effect="dark" round>已接入 DeepSeek</el-tag>
        </div>
        <p>支持多轮上下文、会话管理与历史回看（模型：DeepSeek-V3）</p>
      </div>
      <el-button class="back-btn" @click="navigate('/')">返回首页</el-button>
    </div>

    <div class="ai-layout">
      <aside class="left-pane">
        <div class="left-head">
          <h3>历史会话</h3>
          <el-button type="primary" plain size="small" @click="handleCreateSession">新建对话</el-button>
        </div>

        <div v-if="!sessions.length" class="history-empty">暂无历史会话</div>

        <div v-else class="session-list">
          <div
            v-for="session in sessions"
            :key="session.id"
            class="session-item"
            :class="{ active: currentSessionId === session.id }"
            @click="selectSession(session)"
          >
            <div class="session-main">
              <div class="title">{{ session.title || '新对话' }}</div>
              <div class="meta">{{ session.lastTime || '-' }} · {{ session.count || 0 }} 条</div>
            </div>
            <el-button
              text
              class="delete-btn"
              @click.stop="handleDeleteSession(session)"
            >
              删除
            </el-button>
          </div>
        </div>
      </aside>

      <main class="chat-pane">
        <div ref="chatListRef" class="chat-list">
          <div v-if="!messages.length" class="empty-state">
            <h4>开始咨询</h4>
            <p>例如：菠菜怎么存放更久？草莓怎么挑选更甜？</p>
          </div>

          <template v-for="(item, idx) in messages" :key="item.id || idx">
            <div class="qa-block">
              <div class="msg-row user">
                <div class="msg user-msg">{{ item.question }}</div>
              </div>
              <div class="msg-row ai">
                <div class="msg ai-msg">{{ item.answer }}</div>
                <span class="msg-time">{{ item.createTime }}</span>
              </div>
            </div>
          </template>

          <div v-if="loading" class="msg-row ai">
            <div class="msg ai-msg typing">
              <span></span><span></span><span></span>
            </div>
          </div>
        </div>

        <div class="composer">
          <el-input
            v-model="question"
            type="textarea"
            :rows="2"
            maxlength="300"
            show-word-limit
            placeholder="请输入果蔬相关问题..."
            @keydown.enter.exact.prevent="submit"
          />
          <div class="composer-actions">
            <el-button @click="question = ''">清空</el-button>
            <el-button type="primary" :loading="loading" @click="submit">发送</el-button>
          </div>
          <div class="composer-quick">
            <span class="label">快捷提问</span>
            <el-button
              v-for="q in faq"
              :key="q"
              class="q-btn"
              text
              @click="quickAsk(q)"
            >
              {{ q }}
            </el-button>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { nextTick, onMounted, ref } from 'vue'
import { useAppNavigation } from '@/composables/useAppNavigation'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  askAi,
  createAiSession,
  deleteAiSession,
  getAiFaq,
  getAiHistory,
  getAiSessions,
  switchAiSession
} from '@/api/ai'

const { navigate } = useAppNavigation()

const faq = ref([])
const sessions = ref([])
const currentSessionId = ref(null)
const messages = ref([])
const question = ref('')
const loading = ref(false)
const chatListRef = ref(null)

const scrollToBottom = async () => {
  await nextTick()
  if (chatListRef.value) {
    chatListRef.value.scrollTop = chatListRef.value.scrollHeight
  }
}

const refreshSessions = async () => {
  sessions.value = await getAiSessions()
  const active = sessions.value.find(s => s.active)
  if (active) {
    currentSessionId.value = active.id
    return
  }
  if (sessions.value.length) {
    currentSessionId.value = sessions.value[0].id
  }
}

const loadHistory = async (sessionId = currentSessionId.value) => {
  if (!sessionId) {
    messages.value = []
    return
  }
  messages.value = await getAiHistory(sessionId)
  await scrollToBottom()
}

const loadAll = async () => {
  const faqData = await getAiFaq()
  faq.value = faqData || []
  await refreshSessions()
  if (!currentSessionId.value) {
    const created = await createAiSession()
    currentSessionId.value = created.id
    await refreshSessions()
  }
  await loadHistory(currentSessionId.value)
}

const selectSession = async (session) => {
  if (!session?.id || loading.value) return
  await switchAiSession(session.id)
  currentSessionId.value = session.id
  await refreshSessions()
  await loadHistory(session.id)
}

const handleCreateSession = async () => {
  if (loading.value) return
  const created = await createAiSession()
  currentSessionId.value = created.id
  question.value = ''
  messages.value = []
  await refreshSessions()
}

const handleDeleteSession = async (session) => {
  try {
    await ElMessageBox.confirm('确定删除该历史会话吗？删除后不可恢复。', '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
    await deleteAiSession(session.id)
    ElMessage.success('会话已删除')
    await refreshSessions()
    await loadHistory(currentSessionId.value)
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error?.message || '删除会话失败')
    }
  }
}

const submit = async () => {
  const q = question.value.trim()
  if (!q) {
    ElMessage.warning('请输入问题')
    return
  }
  if (loading.value) return

  if (!currentSessionId.value) {
    const created = await createAiSession()
    currentSessionId.value = created.id
  }

  loading.value = true
  try {
    const data = await askAi(q, currentSessionId.value)
    messages.value.push(data)
    question.value = ''
    await refreshSessions()
    await scrollToBottom()
  } catch (error) {
    ElMessage.error('AI暂时不可用，请稍后重试')
  } finally {
    loading.value = false
  }
}

const quickAsk = async (q) => {
  if (loading.value) return
  question.value = q
  await submit()
}

onMounted(loadAll)
</script>

<style scoped>
.ai-page {
  min-height: calc(100vh - 80px);
  padding: 20px;
  background:
    radial-gradient(1000px 500px at 0% -10%, rgba(44, 127, 92, 0.18), transparent 60%),
    radial-gradient(1000px 500px at 100% 0%, rgba(219, 156, 73, 0.15), transparent 60%),
    #f6f8f4;
}

.hero {
  max-width: 1180px;
  margin: 0 auto 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.hero h1 {
  margin: 0;
  color: #1f3d2e;
  font-size: 30px;
  font-family: 'Noto Serif SC', serif;
}

.hero-title-line {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.hero p {
  margin: 6px 0 0;
  color: #4f6d5f;
}

.ai-layout {
  max-width: 1180px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 14px;
}

.left-pane,
.chat-pane {
  background: rgba(255, 255, 255, 0.88);
  border: 1px solid #dfe7de;
  border-radius: 16px;
  box-shadow: 0 10px 25px rgba(38, 63, 43, 0.08);
}

.left-pane {
  height: 72vh;
  overflow: auto;
  padding: 14px;
}

.left-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.left-head h3 {
  margin: 0;
  color: #2d4638;
}

.history-empty {
  color: #7a8f83;
  font-size: 13px;
  padding: 8px;
}

.session-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.session-item {
  display: flex;
  align-items: center;
  gap: 10px;
  border: 1px solid #e3ece4;
  border-radius: 10px;
  background: #fff;
  padding: 10px 12px;
  cursor: pointer;
  transition: border-color 0.2s ease, background-color 0.2s ease;
}

.session-item:hover {
  border-color: #b8d3bf;
  background: #f8fcf9;
}

.session-item.active {
  border-color: #4a996e;
  background: #edf8f1;
}

.session-main {
  flex: 1;
  min-width: 0;
}

.session-main .title {
  color: #2a4638;
  font-size: 14px;
  line-height: 1.4;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.session-main .meta {
  color: #7f9187;
  font-size: 12px;
}

.delete-btn {
  color: #a96b6b;
  opacity: 0.78;
}

.session-item:hover .delete-btn {
  opacity: 1;
}

.chat-pane {
  height: 72vh;
  display: flex;
  flex-direction: column;
  padding: 14px 14px 12px;
}

.chat-list {
  flex: 1;
  overflow-y: auto;
  padding: 6px 4px;
  max-height: 100%;
}

.empty-state {
  height: 100%;
  min-height: 300px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #6b7f74;
  text-align: center;
}

.empty-state h4 {
  margin: 0 0 8px;
  color: #2d4738;
}

.qa-block {
  margin-bottom: 8px;
}

.msg-row {
  display: flex;
  margin-bottom: 12px;
}

.msg-row.user {
  justify-content: flex-end;
}

.msg-row.ai {
  justify-content: flex-start;
  flex-direction: column;
  align-items: flex-start;
}

.msg {
  max-width: min(78%, 700px);
  padding: 10px 14px;
  border-radius: 14px;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
}

.user-msg {
  background: linear-gradient(120deg, #2a7f5f, #4a996e);
  color: #fff;
  border-bottom-right-radius: 4px;
}

.ai-msg {
  background: #f1f6f2;
  color: #294337;
  border: 1px solid #d8e5db;
  border-bottom-left-radius: 4px;
}

.msg-time {
  font-size: 12px;
  color: #7f9187;
  margin-top: 4px;
  margin-left: 4px;
}

.typing span {
  display: inline-block;
  width: 6px;
  height: 6px;
  margin-right: 6px;
  border-radius: 50%;
  background: #78a087;
  animation: typing 1s infinite ease-in-out;
}

.typing span:nth-child(2) {
  animation-delay: 0.15s;
}

.typing span:nth-child(3) {
  animation-delay: 0.3s;
}

@keyframes typing {
  0%,
  100% { transform: translateY(0); opacity: 0.4; }
  50% { transform: translateY(-5px); opacity: 1; }
}

.composer {
  padding-top: 10px;
  border-top: 1px dashed #d8e2d8;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0), #fff 34%);
}

.composer-actions {
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.composer-quick {
  margin-top: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.composer-quick .label {
  color: #678072;
  font-size: 13px;
  margin-right: 4px;
}

.q-btn {
  width: auto;
  justify-content: center;
  color: #355647;
  border-radius: 10px;
  border: 1px solid #dbe8de;
  background: #f7fbf8;
  min-height: 32px;
}

.q-btn:hover {
  background: #edf6ef;
}

@media (max-width: 980px) {
  .ai-layout {
    grid-template-columns: 1fr;
  }

  .left-pane {
    order: 2;
    height: 34vh;
  }

  .chat-pane {
    order: 1;
    height: 60vh;
  }

  .msg {
    max-width: 92%;
  }
}
</style>
