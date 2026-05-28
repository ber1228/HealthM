<template>
  <div class="page">
    <el-row :gutter="16">
      <el-col :span="16">
        <el-card>
          <div class="title">{{ data.title }}</div>
          <div class="meta">发布时间：{{ time }} · 发布者：系统</div>
          <el-divider />
          <div class="content" v-html="data.content"></div>
          <el-divider />
          <div class="footer-meta">发布于：{{ time }} · 置顶：{{ data.isPinned ? '是' : '否' }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card v-if="toc.length > 0">
          <div class="toc-title">目录</div>
          <el-divider />
          <el-timeline>
            <el-timeline-item v-for="(h,i) in toc" :key="i" :timestamp="''">
              <div class="toc-item">{{ h }}</div>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { announcementsApi } from '@/api'

const route = useRoute()
const data = ref<any>({ title: '', publishTime: '', content: '', isPinned: false })

const time = computed(() => {
  if (!data.value.publishTime) return ''
  const date = new Date(data.value.publishTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit'
  })
})

const toc = computed(() => {
  const content = data.value.content || ''
  const headings: string[] = []
  const lines = content.split('\n')
  for (const line of lines) {
    if (line.trim().length > 0 && line.trim().length < 50) {
      headings.push(line.trim())
      if (headings.length >= 8) break
    }
  }
  return headings
})

onMounted(async () => {
  const id = Number(route.params.id)
  try {
    const { data: res } = await announcementsApi.getById(id)
    data.value = res
    if (data.value.content) data.value.content = data.value.content.replace(/\n/g, '<br>')
    // 登录后标记已读
    if (localStorage.getItem('token')) {
      try { await announcementsApi.markRead(id) } catch {}
    }
  } catch (error: any) {
    console.error('加载公告详情失败:', error)
  }
})
</script>

<style scoped>
.page { padding: 20px; }
.title { font-weight: 800; font-size: 20px; color: #111827; }
.meta { color: #6b7280; margin-top: 4px; }
.content :deep(h3){ margin: 12px 0 6px; color: #1f2937; }
.toc-title { font-weight: 700; color: #1f2937; }
.toc-item { color: #334155; }
.footer-meta { color: #64748b; font-size: 12px; }
</style>


