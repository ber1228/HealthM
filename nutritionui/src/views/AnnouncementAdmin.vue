<template>
  <div class="announcement-admin">
    <div class="header">
      <h2>公告管理</h2>
      <el-button type="primary" @click="handleAdd">新增公告</el-button>
    </div>

    <el-table :data="list" stripe v-loading="loading">
      <el-table-column type="index" label="#" width="60" />
      <el-table-column prop="title" label="标题" min-width="200" />
      <el-table-column prop="summary" label="摘要" min-width="240" show-overflow-tooltip />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.isPinned" type="warning" size="small">置顶</el-tag>
          <el-tag v-else type="info" size="small">普通</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="publishTime" label="发布时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.publishTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" :type="row.isPinned ? 'warning' : 'success'" @click="togglePin(row)">
            {{ row.isPinned ? '取消置顶' : '置顶' }}
          </el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑公告' : '新增公告'" width="800px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="摘要">
          <el-input v-model="form.summary" type="textarea" :rows="2" placeholder="请输入公告摘要" />
        </el-form-item>
        <el-form-item label="正文内容" required>
          <el-input v-model="form.content" type="textarea" :rows="10" placeholder="请输入公告正文" />
        </el-form-item>
        <el-form-item label="是否置顶">
          <el-switch v-model="form.isPinned" />
        </el-form-item>
        <el-form-item label="发布时间">
          <el-date-picker
            v-model="form.publishTime"
            type="datetime"
            placeholder="选择发布时间"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { announcementsApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

type Announcement = {
  id?: number
  title: string
  summary?: string
  content: string
  isPinned?: boolean
  publishTime?: string
}

const list = ref<Announcement[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const form = ref<Announcement>({
  title: '',
  summary: '',
  content: '',
  isPinned: false
})

async function loadList() {
  loading.value = true
  try {
    const { data } = await announcementsApi.adminList()
    list.value = data || []
  } catch (error: any) {
    ElMessage.error('加载公告列表失败: ' + (error.response?.data || error.message))
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  form.value = {
    title: '',
    summary: '',
    content: '',
    isPinned: false
  }
  dialogVisible.value = true
}

function handleEdit(row: Announcement) {
  form.value = { ...row }
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!form.value.title || !form.value.content) {
    ElMessage.warning('请填写标题和正文内容')
    return
  }
  try {
    if (form.value.id) {
      await announcementsApi.update(form.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await announcementsApi.create(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadList()
  } catch (error: any) {
    ElMessage.error('操作失败: ' + (error.response?.data || error.message))
  }
}

async function handleDelete(row: Announcement) {
  try {
    await ElMessageBox.confirm(
      `确定要删除公告"${row.title}"吗？`,
      '确认删除',
      { type: 'warning' }
    )
    await announcementsApi.delete(row.id!)
    ElMessage.success('删除成功')
    loadList()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败: ' + (error.response?.data || error.message))
    }
  }
}

async function togglePin(row: Announcement) {
  try {
    const newPinned = !row.isPinned
    await announcementsApi.togglePin(row.id!, newPinned)
    ElMessage.success(newPinned ? '已置顶' : '已取消置顶')
    loadList()
  } catch (error: any) {
    ElMessage.error('操作失败: ' + (error.response?.data || error.message))
  }
}

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

onMounted(() => {
  loadList()
})
</script>

<style scoped>
.announcement-admin {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h2 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}
</style>

