<template>
  <div class="reminders-container">
    <el-card>
      <template #header>
        <div class="header">
          <h2>提醒设置</h2>
          <el-button type="primary" @click="openCreate">新建提醒</el-button>
        </div>
      </template>

      <el-table :data="items" border>
        <el-table-column prop="reminderType" label="类型" width="120">
          <template #default="{ row }">
            {{ typeLabel(row.reminderType) }}
          </template>
        </el-table-column>
        <el-table-column prop="reminderTime" label="时间" width="140">
          <template #default="{ row }">
            {{ formatTime(row.reminderTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="daysOfWeek" label="重复(周)" min-width="160">
          <template #default="{ row }">
            {{ formatDays(row.daysOfWeek) }}
          </template>
        </el-table-column>
        <el-table-column label="启用" width="100">
          <template #default="{ row }">
            <el-switch v-model="row.isEnabled" @change="toggleEnabled(row)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="remove(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showDialog" :title="dialogTitle" width="500px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="类型">
          <el-select v-model="form.reminderType" placeholder="请选择类型">
            <el-option label="饮食提醒" value="meal" />
            <el-option label="营养提醒" value="nutrition" />
            <el-option label="目标提醒" value="target" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间">
          <el-time-picker
            v-model="formTime"
            placeholder="选择时间"
            format="HH:mm"
            value-format="HH:mm"
          />
        </el-form-item>
        <el-form-item label="重复(周)">
          <el-checkbox-group v-model="formDays">
            <el-checkbox v-for="d in weekOptions" :key="d.value" :label="d.value">{{ d.label }}</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { remindersApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

type Reminder = {
  id: number
  reminderType: 'meal' | 'nutrition' | 'target'
  reminderTime: string
  daysOfWeek?: string
  isEnabled: boolean
}

const items = ref<Reminder[]>([])
const showDialog = ref(false)
const dialogTitle = ref('新建提醒')
const editingId = ref<number | null>(null)

const form = ref<{ reminderType: 'meal' | 'nutrition' | 'target'; reminderTime: string; daysOfWeek?: string }>({
  reminderType: 'meal',
  reminderTime: '08:00',
  daysOfWeek: '1,2,3,4,5'
})
const formTime = ref('08:00')
const formDays = ref<string[]>(['1','2','3','4','5'])

const weekOptions = [
  { value: '1', label: '一' },
  { value: '2', label: '二' },
  { value: '3', label: '三' },
  { value: '4', label: '四' },
  { value: '5', label: '五' },
  { value: '6', label: '六' },
  { value: '7', label: '日' }
]

const load = async () => {
  const res = await remindersApi.list()
  items.value = res.data
}

const openCreate = () => {
  dialogTitle.value = '新建提醒'
  editingId.value = null
  form.value = { reminderType: 'meal', reminderTime: '08:00', daysOfWeek: '1,2,3,4,5' }
  formTime.value = '08:00'
  formDays.value = ['1','2','3','4','5']
  showDialog.value = true
}

const openEdit = (row: Reminder) => {
  dialogTitle.value = '编辑提醒'
  editingId.value = row.id
  form.value = { reminderType: row.reminderType, reminderTime: row.reminderTime?.substring(0,5) || '08:00', daysOfWeek: row.daysOfWeek }
  formTime.value = form.value.reminderTime
  formDays.value = (row.daysOfWeek ? row.daysOfWeek.split(',') : [])
  showDialog.value = true
}

const submit = async () => {
  try {
    const payload = {
      reminderType: form.value.reminderType,
      reminderTime: formTime.value,
      daysOfWeek: formDays.value.join(',')
    }
    if (editingId.value) {
      await remindersApi.update(editingId.value, { reminderTime: payload.reminderTime, daysOfWeek: payload.daysOfWeek })
      ElMessage.success('更新成功')
    } else {
      await remindersApi.create(payload)
      ElMessage.success('创建成功')
    }
    showDialog.value = false
    await load()
  } catch (e: any) {
    ElMessage.error(e.response?.data || '操作失败')
  }
}

const remove = async (id: number) => {
  try {
    await ElMessageBox.confirm('确认删除该提醒？', '提示', { type: 'warning' })
    await remindersApi.remove(id)
    ElMessage.success('删除成功')
    await load()
  } catch (e) {}
}

const toggleEnabled = async (row: Reminder) => {
  try {
    await remindersApi.update(row.id, { isEnabled: row.isEnabled })
    ElMessage.success('已更新')
  } catch (e) {
    ElMessage.error('更新失败')
    row.isEnabled = !row.isEnabled
  }
}

const typeLabel = (t: string) => ({ meal: '饮食提醒', nutrition: '营养提醒', target: '目标提醒' } as any)[t] || t
const formatDays = (s?: string) => (s ? s.split(',').map(n => '一二三四五六日'[Number(n)-1]).join('、') : '每天')
const formatTime = (t?: string) => (t ? t.substring(0,5) : '-')

onMounted(load)
</script>

<style scoped>
.reminders-container { padding: 20px; }
.header { display: flex; align-items: center; justify-content: space-between; }
</style>


