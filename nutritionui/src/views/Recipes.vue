<template>
  <div class="recipes-page">
    <div class="hero">
      <div class="container">
        <h1>菜谱搜索</h1>
        <div class="search-bar">
          <input
            v-model="keyword"
            type="text"
            placeholder="搜索菜谱/食材/做法…"
            @keyup.enter="doSearch"
          />
          <button @click="doSearch">搜索</button>
        </div>

        <div class="filters">
          <div class="chips">
            <button
              v-for="cat in categories"
              :key="cat"
              :class="['chip', selectedCategory === cat && 'active']"
              @click="selectCategory(cat)"
            >{{ cat }}</button>
          </div>
          <div class="selects">
            <select v-model="difficulty">
              <option value="">难度</option>
              <option value="简单">简单</option>
              <option value="中等">中等</option>
              <option value="困难">困难</option>
            </select>
            <select v-model="taste">
              <option value="">口味</option>
              <option value="清淡">清淡</option>
              <option value="微辣">微辣</option>
              <option value="重口">重口</option>
            </select>
            <input type="number" v-model.number="minDuration" placeholder="最短(分)" style="width:110px" />
            <input type="number" v-model.number="maxDuration" placeholder="最长(分)" style="width:110px" />
            <button @click="doSearch">筛选</button>
          </div>
        </div>
      </div>
    </div>

    <div class="container">
      <div class="grid">
        <div v-for="item in results" :key="item.id" class="card" @click="goDetail(item)">
          <div class="thumb" :style="getThumbStyle(item)"></div>
          <div class="body">
            <h3 class="title">{{ item.name }}</h3>
            <div class="meta">
              <span class="author">作者：{{ item.author || '佚名' }}</span>
              <span class="kcal">分类：{{ item.category || '-' }}</span>
            </div>
            <div class="tags">
              <span class="tag">时长 {{ item.durationMinutes ?? '-' }} 分</span>
              <span class="tag">难度 {{ item.difficulty || '-' }}</span>
              <span class="tag">口味 {{ item.taste || '-' }}</span>
            </div>
          </div>
        </div>
      </div>

      <div v-if="loading" class="empty">加载中…</div>
      <div v-else-if="!results.length" class="empty">未找到匹配菜谱</div>
      <div class="pager" v-if="total > size">
        <button :disabled="page===1" @click="changePage(page-1)">上一页</button>
        <span class="pg-info">{{ page }} / {{ Math.ceil(total/size) }}</span>
        <button :disabled="page>=Math.ceil(total/size)" @click="changePage(page+1)">下一页</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { recipesApi } from '@/api'

type Recipe = {
  id: number
  name: string
  author?: string
  category?: string
  durationMinutes?: number
  difficulty?: string
  taste?: string
  coverImageUrl?: string
}

const router = useRouter()
const keyword = ref('')
const results = ref<Recipe[]>([])
const loading = ref(false)
const page = ref(1)
const size = ref(12)
const total = ref(0)

const categories = ['全部', '家常菜', '快手菜', '减脂餐', '健身餐', '早餐', '午餐', '晚餐', '甜品']
const selectedCategory = ref('全部')
const difficulty = ref('')
const taste = ref('')
const minDuration = ref<number | undefined>(undefined)
const maxDuration = ref<number | undefined>(undefined)

async function fetchPage() {
  loading.value = true
  try {
    const params: any = { page: page.value, size: size.value }
    if (selectedCategory.value !== '全部') params.category = selectedCategory.value
    if (keyword.value.trim()) params.keyword = keyword.value.trim()
    if (difficulty.value) params.difficulty = difficulty.value
    if (taste.value) params.taste = taste.value
    if (minDuration.value != null) params.minDuration = minDuration.value
    if (maxDuration.value != null) params.maxDuration = maxDuration.value
    const { data } = await recipesApi.page(params)
    results.value = data.items
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function getThumbStyle(item: Recipe) {
  if (item.coverImageUrl) {
    return {
      backgroundImage: `url(${item.coverImageUrl})`,
      backgroundSize: 'cover',
      backgroundPosition: 'center'
    }
  }
  return {
    background: 'linear-gradient(135deg, #c7d2fe, #e0e7ff)'
  }
}

function selectCategory(cat: string) {
  selectedCategory.value = cat
  page.value = 1
  fetchPage()
}

async function doSearch() {
  page.value = 1
  fetchPage()
}

function changePage(p: number) { page.value = p; fetchPage() }

function goDetail(item: Recipe) { router.push(`/recipes/${item.id}`) }

onMounted(() => { fetchPage() })
</script>

<style scoped>
.recipes-page { background: #f8fafc; min-height: 100%; }
.container { max-width: 1080px; margin: 0 auto; padding: 0 16px; }
.hero { background: linear-gradient(135deg, rgba(129,140,248,.15), rgba(248,250,252,1)); padding: 32px 0 16px; }
.hero h1 { margin: 0 0 16px; font-size: 28px; color: #1e293b; font-weight: 700; }
.search-bar { display: flex; gap: 8px; }
.search-bar input { flex: 1; padding: 10px 12px; border: 1px solid #e2e8f0; border-radius: 10px; outline: none; }
.search-bar button { padding: 10px 16px; border: none; border-radius: 10px; background: #4f46e5; color: #fff; cursor: pointer; }
.filters { display: grid; grid-template-columns: 1fr auto; gap: 12px; align-items: center; margin-top: 14px; }
.chips { display: flex; overflow-x: auto; gap: 8px; padding-bottom: 4px; }
.chip { padding: 8px 12px; border-radius: 999px; background: #eef2ff; color: #4f46e5; border: 1px solid transparent; cursor: pointer; white-space: nowrap; }
.chip.active { background: #4f46e5; color: #fff; }
.selects { display: flex; gap: 8px; }
.selects input, .selects select, .selects button { height: 36px; border: 1px solid #e2e8f0; border-radius: 8px; padding: 0 10px; background: #fff; }
.grid { display: grid; grid-template-columns: repeat(1, 1fr); gap: 12px; margin: 16px 0 24px; }
@media (min-width: 640px) { .grid { grid-template-columns: repeat(2, 1fr); } }
@media (min-width: 960px) { .grid { grid-template-columns: repeat(3, 1fr); } }
.card { background: #fff; border-radius: 14px; overflow: hidden; box-shadow: 0 4px 18px rgba(0,0,0,.06); transition: transform .2s ease, box-shadow .2s ease; }
.card:hover { transform: translateY(-2px); box-shadow: 0 8px 26px rgba(0,0,0,.08); }
.thumb { height: 160px; background: #e5e7eb; }
.body { padding: 12px; }
.title { margin: 0 0 6px; font-size: 16px; color: #1f2937; }
.meta { display: flex; gap: 10px; color: #6b7280; font-size: 12px; }
.tags { display: flex; flex-wrap: wrap; gap: 6px; margin-top: 8px; }
.tag { background: #f3f4f6; color: #374151; border-radius: 999px; padding: 4px 8px; font-size: 12px; }
.empty { text-align: center; color: #6b7280; padding: 24px 0; }
.pager { display: flex; gap: 8px; justify-content: center; align-items: center; padding-bottom: 24px; }
.pager button { padding: 6px 10px; border: 1px solid #e5e7eb; border-radius: 8px; background: #fff; cursor: pointer; }
.pager button:disabled { opacity: .5; cursor: not-allowed; }
.pg-info { color: #6b7280; }
</style>


