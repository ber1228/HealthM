<template>
  <div class="user-admin">
    <div class="header">
      <h2>用户管理</h2>
    </div>

    <div class="toolbar">
      <el-input
        v-model="keyword"
        placeholder="搜索用户名/邮箱"
        style="width: 300px;"
        clearable
        @input="doSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-select v-model="roleFilter" placeholder="角色筛选" style="width: 150px; margin-left: 12px;" @change="doSearch">
        <el-option label="全部" value="" />
        <el-option label="管理员" value="ADMIN" />
        <el-option label="普通用户" value="USER" />
      </el-select>
      <el-button type="danger" :disabled="!multipleSelection.length" @click="batchRemove" style="margin-left: 12px;">
        批量删除
      </el-button>
    </div>

    <el-table
      :data="filtered"
      style="width: 100%"
      @selection-change="handleSelectionChange"
      v-loading="loading"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" width="150" />
      <el-table-column prop="email" label="邮箱" width="200" />
      <el-table-column prop="phone" label="手机" width="130" />
      <el-table-column label="角色" width="120">
        <template #default="{ row }">
          <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'primary'">
            {{ row.role === 'ADMIN' ? '管理员' : '普通用户' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="age" label="年龄" width="80" />
      <el-table-column prop="gender" label="性别" width="80">
        <template #default="{ row }">
          {{ row.gender === 1 ? '男' : row.gender === 0 ? '女' : '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="注册时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="editRole(row)">
            {{ row.role === 'ADMIN' ? '取消管理员' : '设为管理员' }}
          </el-button>
          <el-button size="small" type="danger" @click="remove(row)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { usersApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'

type User = {
  id: number
  username: string
  email?: string
  phone?: string
  role: string
  age?: number
  gender?: number
  createdAt?: string
}

const keyword = ref('')
const roleFilter = ref('')
const list = ref<User[]>([])
const multipleSelection = ref<User[]>([])
const loading = ref(false)

const filtered = computed(() => {
  let result = list.value
  const kw = keyword.value.trim().toLowerCase()
  if (kw) {
    result = result.filter(u =>
      u.username.toLowerCase().includes(kw) ||
      (u.email && u.email.toLowerCase().includes(kw))
    )
  }
  if (roleFilter.value) {
    result = result.filter(u => u.role === roleFilter.value)
  }
  return result
})

async function loadList() {
  loading.value = true
  try {
    const { data } = await usersApi.list()
    list.value = data || []
  } catch (error: any) {
    ElMessage.error('加载用户列表失败: ' + (error.response?.data || error.message))
  } finally {
    loading.value = false
  }
}

function doSearch() {
  // filtered computed会自动更新
}

function handleSelectionChange(selection: User[]) {
  multipleSelection.value = selection
}

async function editRole(row: User) {
  const newRole = row.role === 'ADMIN' ? 'USER' : 'ADMIN'
  const roleName = newRole === 'ADMIN' ? '管理员' : '普通用户'
  try {
    await ElMessageBox.confirm(
      `确定要将用户"${row.username}"设置为${roleName}吗？`,
      '确认操作',
      { type: 'warning' }
    )
    await usersApi.updateRole(row.id, newRole)
    ElMessage.success('角色更新成功')
    loadList()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('更新失败: ' + (error.response?.data || error.message))
    }
  }
}

async function remove(row: User) {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户"${row.username}"吗？此操作不可恢复！`,
      '确认删除',
      { type: 'warning' }
    )
    await usersApi.delete(row.id)
    ElMessage.success('删除成功')
    loadList()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败: ' + (error.response?.data?.message || error.message))
    }
  }
}

async function batchRemove() {
  if (!multipleSelection.value.length) return
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${multipleSelection.value.length} 个用户吗？此操作不可恢复！`,
      '确认批量删除',
      { type: 'warning' }
    )
    await Promise.all(multipleSelection.value.map(u => usersApi.delete(u.id)))
    ElMessage.success('批量删除成功')
    multipleSelection.value = []
    loadList()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败: ' + (error.response?.data?.message || error.message))
    }
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
.user-admin {
  padding: 20px;
}

.header {
  margin-bottom: 20px;
}

.header h2 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.toolbar {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}
</style>

