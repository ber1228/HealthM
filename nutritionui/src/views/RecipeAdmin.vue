<template>
  <div class="page">
    <el-card>
      <div class="toolbar">
        <div style="display:flex; gap:8px; align-items:center; flex-wrap: wrap;">
          <el-input v-model="keyword" placeholder="搜索名称/作者" clearable style="width: 220px" />
          <el-select v-model="filters.category" placeholder="分类" clearable style="width: 120px">
            <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
          </el-select>
          <el-select v-model="filters.difficulty" placeholder="难度" clearable style="width: 120px">
            <el-option label="简单" value="简单" /><el-option label="中等" value="中等" /><el-option label="困难" value="困难" />
          </el-select>
          <el-select v-model="filters.taste" placeholder="口味" clearable style="width: 120px">
            <el-option label="清淡" value="清淡" /><el-option label="微辣" value="微辣" /><el-option label="重口" value="重口" />
          </el-select>
          <el-select v-model="filters.published" placeholder="发布状态" clearable style="width: 140px">
            <el-option label="已发布" :value="true" /><el-option label="未发布" :value="false" />
          </el-select>
          <el-button type="primary" @click="loadPage(1)">查询</el-button>
        </div>
        <div class="ops">
          <el-button type="primary" @click="openWizard()">新增菜谱</el-button>
          <el-button :disabled="!multipleSelection.length" @click="batchRemove">批量删除</el-button>
        </div>
      </div>

      <el-table :data="list" @selection-change="onSelChange" border v-loading="loading">
        <el-table-column type="selection" width="48" />
        <el-table-column label="封面" width="92">
          <template #default="{ row }">
            <el-avatar :size="48" :src="row.coverImageUrl" />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="名称" min-width="180" />
        <el-table-column prop="author" label="作者" width="120" />
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column prop="durationMinutes" label="时长(分钟)" width="110" />
        <el-table-column prop="difficulty" label="难度" width="90" />
        <el-table-column prop="taste" label="口味" width="90" />
        <el-table-column label="发布" width="100">
          <template #default="{ row }">
            <el-tag :type="row.published ? 'success' : 'info'">{{ row.published ? '已发布' : '未发布' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="340" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="preview(row)">预览</el-button>
            <el-button size="small" @click="openWizard(row)">编辑</el-button>
            <el-button size="small" :type="row.published?'warning':'success'" @click="toggle(row)">{{ row.published?'下架':'上架' }}</el-button>
            <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="display:flex; justify-content:flex-end; margin-top:10px;">
        <el-pagination background layout="prev, pager, next, jumper" :total="total" :page-size="size" :current-page="page" @current-change="loadPage" />
      </div>
    </el-card>

    <!-- 新增/编辑分步（基础信息/食材配置/步骤教程/营养摘要） -->
    <el-dialog v-model="visible" :title="wizardTitle" width="820px">
      <el-steps :active="step" align-center finish-status="success" style="margin-bottom:12px">
        <el-step title="基础信息" /><el-step title="食材配置" /><el-step title="步骤教程" /><el-step title="营养摘要" />
      </el-steps>

      <div v-show="step===0">
        <el-form :model="form" label-width="100px">
          <el-form-item label="名称" required><el-input v-model="form.name" /></el-form-item>
          <el-form-item label="作者"><el-input v-model="form.author" /></el-form-item>
          <el-form-item label="分类"><el-select v-model="form.category" filterable clearable style="width: 200px"><el-option v-for="c in categories" :key="c" :label="c" :value="c" /></el-select></el-form-item>
          <el-form-item label="时长(分钟)"><el-input v-model.number="form.durationMinutes" type="number" min="0" /></el-form-item>
          <el-form-item label="难度"><el-select v-model="form.difficulty" style="width: 200px"><el-option label="简单" value="简单" /><el-option label="中等" value="中等" /><el-option label="困难" value="困难" /></el-select></el-form-item>
          <el-form-item label="口味"><el-select v-model="form.taste" style="width: 200px"><el-option label="清淡" value="清淡" /><el-option label="微辣" value="微辣" /><el-option label="重口" value="重口" /></el-select></el-form-item>
          <el-form-item label="热量(每份)"><el-input v-model="form.calories" type="number" /></el-form-item>
          <el-form-item label="封面图" required>
            <div style="display:flex; gap:12px; align-items:center;">
              <el-upload :auto-upload="false" :show-file-list="false" accept="image/*" @change="onCoverChange">
                <img v-if="form.coverImageUrl" :src="form.coverImageUrl" style="width:84px;height:84px;border-radius:8px;object-fit:cover;border:1px solid #e5e7eb" />
                <el-button v-else>选择图片并上传</el-button>
              </el-upload>
            </div>
          </el-form-item>
          <el-form-item label="发布"><el-switch v-model="form.published" /></el-form-item>
        </el-form>
      </div>

      <div v-show="step===1">
        <div style="display:grid; grid-template-columns: 1fr 1fr; gap:16px;">
          <div>
            <div style="font-weight:700;margin-bottom:6px">主料</div>
            <div v-for="(ing,i) in mains" :key="'m'+i" style="display:flex; gap:6px; margin-bottom:6px;">
              <el-input v-model="ing.name" placeholder="食材名" style="width: 160px" />
              <el-input v-model="ing.amount" placeholder="用量" style="width: 120px" />
              <el-button text type="danger" @click="mains.splice(i,1)">移除</el-button>
            </div>
            <el-button text @click="mains.push({name:'',amount:''})">+ 添加主料</el-button>
          </div>
          <div>
            <div style="font-weight:700;margin-bottom:6px">辅料</div>
            <div v-for="(ing,i) in auxes" :key="'a'+i" style="display:flex; gap:6px; margin-bottom:6px;">
              <el-input v-model="ing.name" placeholder="食材名" style="width: 160px" />
              <el-input v-model="ing.amount" placeholder="用量" style="width: 120px" />
              <el-button text type="danger" @click="auxes.splice(i,1)">移除</el-button>
            </div>
            <el-button text @click="auxes.push({name:'',amount:''})">+ 添加辅料</el-button>
          </div>
        </div>
      </div>

      <div v-show="step===2">
        <div v-for="(s,i) in steps" :key="'s'+i" style="display:grid; grid-template-columns: 60px 1fr 160px 80px; gap:8px; align-items:center; margin-bottom:8px;">
          <el-input v-model.number="s.stepOrder" type="number" placeholder="#" />
          <el-input v-model="s.description" type="textarea" :rows="2" placeholder="步骤描述" />
          <div style="display:flex; gap:6px; align-items:center;">
            <el-upload :auto-upload="false" :show-file-list="false" accept="image/*" @change="(f:any)=>onStepImageChange(i,f)">
              <img v-if="s.imageUrl" :src="s.imageUrl" style="width:48px;height:48px;border-radius:6px;object-fit:cover;border:1px solid #e5e7eb" />
              <el-button v-else>步骤图</el-button>
            </el-upload>
            <el-input v-model="s.imageUrl" placeholder="图片URL" />
          </div>
          <el-checkbox v-model="s.isKey">关键</el-checkbox>
        </div>
        <el-button text @click="steps.push({ stepOrder: steps.length+1, description:'', imageUrl:'', isKey:false })">+ 添加步骤</el-button>
      </div>

      <div v-show="step===3">
        <el-alert type="info" show-icon :closable="false" title="营养摘要可根据食材自动计算后微调（当前先手工录入可选）" />
        <div style="margin-top:8px;display:flex;justify-content:space-between;">
          <el-button @click="step--">上一步</el-button>
          <el-button type="primary" @click="submit()" :loading="saving">提交</el-button>
        </div>
      </div>

      <template #footer>
        <div v-if="step<3" style="display:flex;justify-content:space-between;width:100%">
          <div>
            <el-button :disabled="step===0" @click="step--">上一步</el-button>
          </div>
          <div>
            <el-button type="primary" @click="step++">下一步</el-button>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { recipesApi, uploadApi } from '@/api'

const categories = ['家常菜', '快手菜', '减脂餐', '健身餐', '早餐', '午餐', '晚餐', '甜品']

const keyword = ref('')
const filters = ref<any>({ category: '', difficulty: '', taste: '', published: undefined })
const page = ref(1)
const size = ref(10)
const total = ref(0)
const list = ref<any[]>([])
const loading = ref(false)
let multipleSelection = ref<any[]>([])

function onSelChange(rows: any[]) { multipleSelection.value = rows }

async function loadPage(p?: number) {
  if (p) page.value = p
  loading.value = true
  try {
    const params: any = { page: page.value, size: size.value }
    if (keyword.value.trim()) params.keyword = keyword.value.trim()
    if (filters.value.category) params.category = filters.value.category
    if (filters.value.difficulty) params.difficulty = filters.value.difficulty
    if (filters.value.taste) params.taste = filters.value.taste
    if (filters.value.published !== undefined) params.published = filters.value.published
    const { data } = await recipesApi.adminPage(params)
    list.value = data.items || []
    total.value = data.total || 0
  } catch (e:any) {
    ElMessage.error('加载失败: ' + (e.response?.data || e.message))
  } finally {
    loading.value = false
  }
}

function preview(row: any) { window.open('/recipes/' + row.id, '_blank') }

async function toggle(row: any) {
  try {
    await recipesApi.publish(row.id, !row.published)
    ElMessage.success((!row.published ? '上架' : '下架') + '成功')
    loadPage()
  } catch (e:any) {
    ElMessage.error('操作失败: ' + (e.response?.data || e.message))
  }
}

async function remove(row: any) {
  if (row.published) { ElMessage.warning('已发布需先下架再删除'); return }
  try {
    await ElMessageBox.confirm(`确定删除“${row.name}”吗？`, '删除确认', { type: 'warning' })
    await recipesApi.delete(row.id)
    ElMessage.success('已删除')
    loadPage()
  } catch (e:any) {
    if (e !== 'cancel') ElMessage.error('删除失败: ' + (e.response?.data || e.message))
  }
}

async function batchRemove() {
  if (!multipleSelection.value.length) return
  try {
    await ElMessageBox.confirm(`确定删除选中的 ${multipleSelection.value.length} 项吗？`, '批量删除确认', { type: 'warning' })
    await Promise.all(multipleSelection.value.filter((r:any)=>!r.published).map((r:any)=>recipesApi.delete(r.id)))
    ElMessage.success('批量删除完成（已发布的已跳过）')
    multipleSelection.value = []
    loadPage()
  } catch (e:any) {
    if (e !== 'cancel') ElMessage.error('批量删除失败: ' + (e.response?.data || e.message))
  }
}

const visible = ref(false)
const step = ref(0)
const form = ref<any>({})
const mains = ref<{name:string;amount:string}[]>([])
const auxes = ref<{name:string;amount:string}[]>([])
const steps = ref<any[]>([])
const saving = ref(false)
const wizardTitle = computed(() => form.value.id ? '编辑菜谱' : '新增菜谱')

function openWizard(row?: any) {
  if (row) {
    // 编辑时先加载详情
    loadDetail(row.id).then((detail:any) => {
      const r = detail.recipe
      form.value = { ...r }
      mains.value = (detail.ingredients || []).filter((i:any)=>i.type==='main').map((i:any)=>({ name:i.name, amount:i.amount }))
      auxes.value = (detail.ingredients || []).filter((i:any)=>i.type==='aux').map((i:any)=>({ name:i.name, amount:i.amount }))
      steps.value = (detail.steps || []).map((s:any)=>({ stepOrder:s.stepOrder, description:s.description, imageUrl:s.imageUrl, isKey:s.isKey }))
      step.value = 0
      visible.value = true
    })
  } else {
    form.value = { name:'', author:'', category:'', durationMinutes: null, difficulty:'', taste:'', calories: null, coverImageUrl:'', published: false }
    mains.value = []
    auxes.value = []
    steps.value = []
    step.value = 0
    visible.value = true
  }
}

async function loadDetail(id: number) {
  const { data } = await recipesApi.detail(id)
  return data
}

async function onCoverChange(file:any) {
  try {
    if (!file || !file.raw) { ElMessage.error('未选择文件'); return }
    const fd = new FormData()
    fd.append('file', file.raw)
    const { data } = await uploadApi.image(fd)
    form.value.coverImageUrl = data.url
    ElMessage.success('上传成功')
  } catch (e:any) {
    ElMessage.error('上传失败: ' + (e.response?.data || e.message))
  }
}

async function onStepImageChange(idx:number, file:any) {
  try {
    if (!file || !file.raw) { ElMessage.error('未选择文件'); return }
    const fd = new FormData()
    fd.append('file', file.raw)
    const { data } = await uploadApi.image(fd)
    steps.value[idx].imageUrl = data.url
    ElMessage.success('上传成功')
  } catch (e:any) {
    ElMessage.error('上传失败: ' + (e.response?.data || e.message))
  }
}

async function submit() {
  if (!form.value.name) { ElMessage.error('请完善基础信息'); return }
  saving.value = true
  try {
    const payload:any = { ...form.value, mains: mains.value, auxes: auxes.value, steps: steps.value }
    if (form.value.id) {
      await recipesApi.update(form.value.id, payload)
      ElMessage.success('更新成功')
    } else {
      await recipesApi.create(payload)
      ElMessage.success('创建成功')
    }
    visible.value = false
    loadPage()
  } catch (e:any) {
    ElMessage.error('保存失败: ' + (e.response?.data || e.message))
  } finally {
    saving.value = false
  }
}

onMounted(() => { loadPage(1) })
</script>

<style scoped>
.page { padding: 20px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; gap: 8px; flex-wrap: wrap; }
.ops { display: flex; gap: 8px; }
</style>


