<template>
  <div class="page">
    <el-card>
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="搜索名称/分类" clearable style="max-width: 240px" />
        <div class="ops">
          <el-button type="primary" @click="openForm()">新增食物</el-button>
          <el-button :disabled="!multipleSelection.length" @click="batchRemove">批量删除</el-button>
        </div>
      </div>
      <el-table :data="filtered" @selection-change="handleSelectionChange" border>
        <el-table-column type="selection" width="48" />
        <el-table-column type="index" label="#" width="60" />
        <el-table-column label="图片" width="84">
          <template #default="{ row }">
            <el-avatar :size="40" :src="thumb(row)" />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="名称" min-width="140" />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="calories" label="热量(kcal)" width="120" />
        <el-table-column label="核心营养" min-width="220">
          <template #default="{ row }">
            蛋白 {{ row.protein ?? '-' }}g · 碳水 {{ row.carbs ?? '-' }}g · 脂肪 {{ row.fat ?? '-' }}g
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.availability === 'disabled' ? 'danger' : 'success'" effect="plain">
              {{ row.availability === 'disabled' ? '停用' : '启用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openForm(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
            <el-button size="small" @click="preview(row)">预览</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 表单弹窗（基础信息 + 营养配置） -->
    <el-dialog v-model="visible" :title="form.id ? '编辑食物' : '新增食物'" width="680px">
      <el-form :model="form" label-width="96px">
        <div class="section">基础信息</div>
        <el-form-item label="名称" required>
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="图片">
          <el-upload
            class="uploader"
            :action="''"
            :auto-upload="false"
            :on-change="handleImageChange"
            :show-file-list="false"
            accept="image/*"
          >
            <img v-if="form.imageUrl" :src="form.imageUrl" class="preview-img" />
            <el-button v-else>选择图片并上传</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="分类" required>
          <el-select v-model="form.category" placeholder="选择分类">
            <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.enabled" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.desc" type="textarea" :rows="3" />
        </el-form-item>

        <div class="section">营养配置</div>
        <el-row :gutter="8">
          <el-col :span="8" v-for="n in nutrients" :key="n.key">
            <el-form-item :label="n.label">
              <el-input v-model.number="form[n.key]" placeholder="数值" />
            </el-form-item>
          </el-col>
        </el-row>
        <div style="display:flex; gap:8px">
          <el-button @click="autoFill">按分类智能填充</el-button>
          <el-button type="primary" @click="submit">提交</el-button>
        </div>
      </el-form>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { foodsApi, uploadApi } from '@/api'
import { ElMessageBox, ElMessage } from 'element-plus'

function getCategoryIcon(category?: string) {
  const map: Record<string, string> = {
    '谷物': '/images/categories/grains.svg',
    '蛋白质': '/images/categories/protein.svg',
    '蔬菜': '/images/categories/vegetable.svg',
    '水果': '/images/categories/fruit.svg',
    '奶制品': '/images/categories/dairy.svg',
    '豆制品': '/images/categories/bean.svg',
    '坚果': '/images/categories/nuts.svg'
  }
  return map[category || ''] || '/images/categories/fallback.svg'
}

type Food = any
const keyword = ref('')
const list = ref<Food[]>([])
const multipleSelection = ref<Food[]>([])

const categories = ['谷物', '蛋白质', '蔬菜', '水果', '奶制品', '豆制品', '坚果']
const nutrients = [
  { key: 'calories', label: '热量(kcal)' },
  { key: 'protein', label: '蛋白质(g)' },
  { key: 'carbs', label: '碳水(g)' },
  { key: 'fat', label: '脂肪(g)' },
  { key: 'fiber', label: '纤维(g)' },
  { key: 'calcium', label: '钙(mg)' },
  { key: 'iron', label: '铁(mg)' },
  { key: 'vitc', label: '维C(mg)' },
  { key: 'potassium', label: '钾(mg)' },
  { key: 'sodium', label: '钠(mg)' },
]

const filtered = computed(() => {
  const kw = keyword.value.trim()
  if (!kw) return list.value
  return list.value.filter(x => (x.name?.includes(kw) || x.category?.includes(kw)))
})

function thumb(row: Food) {
  if (row.imageUrl) return row.imageUrl
  return getCategoryIcon(row.category)
}

function handleSelectionChange(rows: Food[]) { multipleSelection.value = rows }

const visible = ref(false)
const form = ref<any>({ enabled: true })

function openForm(row?: Food) {
  form.value = row ? { ...row, imageUrl: row.imageUrl || '', enabled: row.availability !== 'disabled' } : { enabled: true, imageUrl: '' }
  visible.value = true
}

async function handleImageChange(file: any) {
  try {
    if (!file || !file.raw) { ElMessage.error('未选择文件'); return }
    const fd = new FormData()
    fd.append('file', file.raw)
    const { data } = await uploadApi.image(fd)
    form.value.imageUrl = data.url
    ElMessage.success('上传成功')
  } catch (e:any) {
    ElMessage.error('上传失败: ' + (e.response?.data || e.message))
  }
}

function autoFill() {
  if (!form.value.category) return
  const presets: Record<string, any> = {
    '蛋白质': { protein: 20, fat: 10, calories: 200 },
    '蔬菜': { carbs: 8, fiber: 3, calories: 40 },
    '谷物': { carbs: 25, calories: 120 },
  }
  Object.assign(form.value, presets[form.value.category] || {})
}

async function submit() {
  if (!form.value.name || !form.value.category) {
    ElMessage.error('请完整填写必填项（名称、分类）')
    return
  }
  try {
    const payload = {
      name: form.value.name,
      category: form.value.category,
      imageUrl: form.value.imageUrl || null,
      calories: form.value.calories || null,
      protein: form.value.protein || null,
      carbs: form.value.carbs || null,
      fat: form.value.fat || null,
      fiber: form.value.fiber || null,
      calcium: form.value.calcium || null,
      iron: form.value.iron || null,
      vitc: form.value.vitc || null,
      potassium: form.value.potassium || null,
      sodium: form.value.sodium || null,
      season: form.value.season || null,
      availability: form.value.enabled ? 'enabled' : 'disabled'
    }
    if (form.value.id) {
      await foodsApi.update(form.value.id, payload)
      ElMessage.success('更新成功')
    } else {
      await foodsApi.create(payload)
      ElMessage.success('创建成功')
    }
    visible.value = false
    loadList()
  } catch (error: any) {
    ElMessage.error('操作失败: ' + (error.response?.data?.message || error.message))
  }
}

async function remove(row: Food) {
  await ElMessageBox.confirm(`确定删除"${row.name}"吗？`, '删除确认', { type: 'warning' })
  try {
    await foodsApi.delete(row.id)
    ElMessage.success('删除成功')
    loadList()
  } catch (error: any) {
    ElMessage.error('删除失败: ' + (error.response?.data?.message || error.message))
  }
}

async function batchRemove() {
  await ElMessageBox.confirm(`确定删除选中的 ${multipleSelection.value.length} 项吗？`, '批量删除确认', { type: 'warning' })
  try {
    for (const item of multipleSelection.value) {
      await foodsApi.delete(item.id)
    }
    ElMessage.success('批量删除成功')
    multipleSelection.value = []
    loadList()
  } catch (error: any) {
    ElMessage.error('批量删除失败: ' + (error.response?.data?.message || error.message))
  }
}

function preview(row: Food) {
  window.open(`/recipes/${row.id}`, '_blank')
}

async function loadList() {
  try {
    const { data } = await foodsApi.adminList()
    list.value = data
  } catch (error: any) {
    ElMessage.error('加载失败: ' + (error.response?.data?.message || error.message))
  }
}

onMounted(() => {
  loadList()
})
</script>

<style scoped>
.page { padding: 20px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.ops { display: flex; gap: 8px; }
.section { font-weight: 700; color: #1f2937; margin: 8px 0; }
.uploader { width: 120px; height: 120px; border: 1px dashed #d9d9d9; border-radius: 8px; cursor: pointer; display: flex; align-items: center; justify-content: center; }
.preview-img { width: 120px; height: 120px; object-fit: cover; border-radius: 8px; }
.upload-icon { font-size: 28px; color: #8c8c8c; }
</style>


