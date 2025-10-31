<template>
  <div class="user-list">
    <el-card>
      <template #header>
        <span>用户管理</span>
      </template>
      
      <el-table :data="userList" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column prop="nick" label="昵称" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ctime" label="注册时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.ctime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button 
              size="small" 
              :type="scope.row.status === 1 ? 'warning' : 'success'"
              @click="handleToggleStatus(scope.row)"
            >
              {{ scope.row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button 
              size="small" 
              type="danger" 
              @click="handleDelete(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/utils/api'

export default {
  name: 'UserList',
  setup() {
    const loading = ref(false)
    const userList = ref([])
    
    const loadData = async () => {
      loading.value = true
      try {
        // TODO: 调用真实API
        // const response = await api.getUserList()
        // userList.value = response.data || []
        userList.value = []
      } catch (error) {
        console.error('加载数据失败:', error)
      } finally {
        loading.value = false
      }
    }
    
    const getStatusType = (status) => {
      switch (status) {
        case 1: return 'success'
        case 0: return 'warning'
        case -1: return 'danger'
        default: return 'info'
      }
    }
    
    const getStatusText = (status) => {
      switch (status) {
        case 1: return '正常'
        case 0: return '禁用'
        case -1: return '删除'
        default: return '未知'
      }
    }
    
    const formatDate = (dateStr) => {
      return new Date(dateStr).toLocaleString()
    }
    
    const handleToggleStatus = (row) => {
      const action = row.status === 1 ? '禁用' : '启用'
      ElMessageBox.confirm(`确定要${action}这个用户吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        row.status = row.status === 1 ? 0 : 1
        ElMessage.success(`${action}成功`)
      })
    }
    
    const handleDelete = (row) => {
      ElMessageBox.confirm('确定要删除这个用户吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        row.status = -1
        ElMessage.success('删除成功')
      })
    }
    
    onMounted(() => {
      loadData()
    })
    
    return {
      loading,
      userList,
      getStatusType,
      getStatusText,
      formatDate,
      handleToggleStatus,
      handleDelete
    }
  }
}
</script>

<style scoped>
.user-list {
  padding: 20px;
}
</style> 