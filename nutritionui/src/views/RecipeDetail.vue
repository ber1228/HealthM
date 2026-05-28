<template>
  <div class="page">
    <el-skeleton :loading="loading" animated>
      <template #template>
        <el-skeleton-item variant="image" style="width: 100%; height: 220px; border-radius: 12px" />
        <div style="padding: 12px 0">
          <el-skeleton-item variant="h1" style="width: 40%" />
          <el-skeleton-item variant="text" style="width: 60%" />
        </div>
      </template>
      <template #default>
        <el-card class="hero">
          <div class="cover" :style="coverStyle"></div>
          <div class="head">
            <h1>{{ recipe.name }}</h1>
            <div class="meta">分类：{{ recipe.category || '其他' }} · 约{{ recipe.durationMinutes ?? '-' }}分钟 · {{ recipe.difficulty || '-' }} · 口味 {{ recipe.taste || '-' }}</div>
            <div class="tags">
              <el-tag>热量 {{ recipe.calories ?? '-' }} kcal/份</el-tag>
              <el-tag type="info">作者 {{ recipe.author || '佚名' }}</el-tag>
            </div>
          </div>
        </el-card>

        <el-row :gutter="16" style="margin-top: 12px">
          <el-col :span="16">
            <el-card>
              <div class="section-title">步骤教程</div>
              <div class="steps">
                <div v-for="(s,i) in steps" :key="i" class="step">
                  <div class="idx">{{ i + 1 }}</div>
                  <div class="body">
                    <div class="sd" :class="{ key: s.isKey }">{{ s.description }}</div>
                    <img v-if="s.imageUrl" :src="s.imageUrl" class="step-img" />
                  </div>
                </div>
              </div>
            </el-card>
            <el-card style="margin-top: 12px">
              <div class="section-title">评论</div>
              <div v-if="!comments.length"><el-empty description="暂未有评论" /></div>
              <div v-else style="display:grid; gap:10px">
                <div v-for="c in comments" :key="c.id" class="comment-item">
                  <div class="comment-meta">{{ c.username || '用户' }} · {{ formatTime(c.createdAt) }}</div>
                  <div class="comment-content">{{ c.content }}</div>
                </div>
              </div>
              <div v-if="isLoggedIn" style="display:flex; gap:8px; margin-top:10px">
                <el-input v-model="newComment" placeholder="说点什么…" />
                <el-button type="primary" @click="sendComment" :loading="sending">发表评论</el-button>
              </div>
              <div v-else class="login-tip">登录后可以发表评论</div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card>
              <div class="section-title">食材清单</div>
              <div class="ingredients">
                <div class="ig-title">主料</div>
                <div class="ig-item" v-for="(m,i) in mains" :key="'m'+i">{{ m.name }} {{ m.amount || '' }}</div>
                <div class="ig-title" style="margin-top:8px">辅料</div>
                <div class="ig-item" v-for="(a,i) in auxes" :key="'a'+i">{{ a.name }} {{ a.amount || '' }}</div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </template>
    </el-skeleton>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { recipesApi } from '@/api'

const route = useRoute()
const id = Number(route.params.id)
const loading = ref(true)

const isLoggedIn = !!localStorage.getItem('token')

const recipe = ref<any>({})
const mains = ref<any[]>([])
const auxes = ref<any[]>([])
const steps = ref<any[]>([])
const comments = ref<any[]>([])

const coverStyle = computed(() => {
  if (recipe.value?.coverImageUrl) {
    return { backgroundImage: `url(${recipe.value.coverImageUrl})`, backgroundSize: 'cover', backgroundPosition: 'center' }
  }
  return { background: '#e5e7eb' }
})

const newComment = ref('')
const sending = ref(false)

function formatTime(ts?: string) {
  if (!ts) return ''
  const d = new Date(ts)
  return d.toLocaleString('zh-CN', { year:'numeric', month:'2-digit', day:'2-digit', hour:'2-digit', minute:'2-digit' })
}

async function sendComment() {
  if (!newComment.value.trim()) return
  sending.value = true
  try {
    await recipesApi.comment(id, newComment.value.trim())
    newComment.value = ''
    await loadDetail()
  } catch (e:any) {
    // ignore
  } finally {
    sending.value = false
  }
}

async function loadDetail() {
  try {
    const { data } = await recipesApi.detail(id)
    recipe.value = data.recipe || {}
    const ings = data.ingredients || []
    mains.value = ings.filter((i:any)=>i.type==='main')
    auxes.value = ings.filter((i:any)=>i.type==='aux')
    steps.value = data.steps || []
    comments.value = data.comments || []
  } finally {
    loading.value = false
  }
}

onMounted(() => { loadDetail() })
</script>

<style scoped>
.page { padding: 20px; }
.hero { overflow: hidden; }
.cover { height: 220px; border-radius: 12px; background-size: cover; background-position: center; }
.head { padding: 10px 0; }
.head h1 { margin: 0 0 6px; font-size: 22px; font-weight: 800; color: #111827; }
.meta { color: #6b7280; }
.tags { display: flex; flex-wrap: wrap; gap: 6px; margin-top: 8px; }
.section-title { font-weight: 700; color: #1f2937; margin-bottom: 8px; }
.steps { display: grid; gap: 10px; }
.step { display: grid; grid-template-columns: 32px 1fr; gap: 10px; background: #f8fafc; border: 1px solid #e5e7eb; border-radius: 12px; padding: 10px; }
.idx { width: 32px; height: 32px; border-radius: 50%; background: #4f46e5; color: #fff; display: flex; align-items: center; justify-content: center; font-weight: 700; }
.sd { color: #334155; }
.sd.key { color: #ef4444; font-weight: 700; }
.step-img { width: 100%; height: auto; display: block; border-radius: 8px; }
.ingredients { display: grid; gap: 6px; }
.ig-title { font-weight: 700; color: #1f2937; }
.ig-item { color: #334155; }
.comment-item { border:1px solid #e5e7eb; border-radius:10px; padding:8px 10px; }
.comment-meta { color:#475569; font-size:12px; }
.comment-content { margin-top:6px; }
.login-tip { color:#6b7280; margin-top:8px; font-size:12px; }
</style>


