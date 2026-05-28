<template>
  <div class="page">
    <el-card>
      <div class="header">
        <div class="title">公告</div>
        <el-input v-model="keyword" placeholder="搜索公告标题/摘要" clearable style="max-width: 260px" />
      </div>
      <el-table :data="filtered" stripe v-loading="loading">
        <el-table-column type="index" label="#" width="60" />
        <el-table-column label="标题">
          <template #default="{ row }">
            <span class="link" :class="{ bold: !row.read }" @click="goDetail(row)">
              <el-tag v-if="row.isPinned" size="small" type="warning" effect="plain">置顶</el-tag>
              {{ row.title }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="summary" label="摘要" min-width="240" show-overflow-tooltip />
        <el-table-column prop="publishTime" label="时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.publishTime) }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { announcementsApi } from '@/api'

type Ann = { id: number; title: string; summary?: string; publishTime?: string; isPinned?: boolean; read?: boolean }
const router = useRouter()
const keyword = ref('')
const list = ref<Ann[]>([])
const loading = ref(false)

async function loadList() {
  loading.value = true
  try {
    const { data } = await announcementsApi.list()
    list.value = data || []
  } catch (error: any) {
    console.error('加载公告列表失败:', error)
    list.value = []
  } finally {
    loading.value = false
  }
}

const filtered = computed(() => {
  const kw = keyword.value.trim()
  let result = list.value
  if (kw) {
    result = result.filter(x => x.title.includes(kw) || (x.summary && x.summary.includes(kw)))
  }
  return result.sort(sorter)
})

function sorter(a: Ann, b: Ann) {
  if ((a.isPinned ? 1 : 0) !== (b.isPinned ? 1 : 0)) {
    return (b.isPinned ? 1 : 0) - (a.isPinned ? 1 : 0)
  }
  const timeA = a.publishTime || ''
  const timeB = b.publishTime || ''
  return timeA < timeB ? 1 : -1
}

function goDetail(item: Ann) { router.push(`/announcements/${item.id}`) }

function formatDate(dateStr?: string) {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => { loadList() })
</script>

<style scoped>
.page { padding: 20px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.title { font-weight: 700; color: #1f2937; }
.link { color: #334155; cursor: pointer; }
.link.bold { font-weight: 700; }
</style>


