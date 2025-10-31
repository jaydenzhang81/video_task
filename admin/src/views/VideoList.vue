<template>
  <div class="video-list">
    <el-card>
      <template #header>
        <div class="header-content">
          <span>视频管理</span>
          <el-button type="primary" @click="handleAdd">添加视频</el-button>
        </div>
      </template>
      
      <el-table :data="videoList" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="videoType" label="类型">
          <template #default="scope">
            {{ getVideoTypeName(scope.row.videoType) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '已发布' : '未发布' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ctime" label="创建时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.ctime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
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
    
    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入视频标题" />
        </el-form-item>
        <el-form-item label="描述" prop="videoDesc">
          <el-input 
            v-model="form.videoDesc" 
            type="textarea" 
            placeholder="请输入视频描述"
            :rows="3"
          />
        </el-form-item>
        <el-form-item label="类型" prop="videoType">
          <el-select v-model="form.videoType" placeholder="请选择视频类型">
            <el-option
              v-for="type in videoTypes"
              :key="type.id"
              :label="type.name"
              :value="type.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="视频地址" prop="url">
          <el-input v-model="form.url" placeholder="请输入视频地址" />
        </el-form-item>
        <el-form-item label="封面" prop="cover">
          <el-input v-model="form.cover" placeholder="请输入封面地址" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="0">未发布</el-radio>
            <el-radio :label="1">已发布</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/utils/api'

export default {
  name: 'VideoList',
  setup() {
    const loading = ref(false)
    const videoList = ref([])
    const videoTypes = ref([])
    const dialogVisible = ref(false)
    const dialogTitle = ref('添加视频')
    const formRef = ref()
    
    const form = reactive({
      id: null,
      title: '',
      videoDesc: '',
      videoType: '',
      url: '',
      cover: '',
      status: 0
    })
    
    const rules = {
      title: [
        { required: true, message: '请输入视频标题', trigger: 'blur' }
      ],
      videoType: [
        { required: true, message: '请选择视频类型', trigger: 'change' }
      ],
      url: [
        { required: true, message: '请输入视频地址', trigger: 'blur' }
      ]
    }
    
    const loadData = async () => {
      loading.value = true
      try {
        const res = await api.video.list({ num: 100 })
        videoList.value = res.data
        
        const typeRes = await api.video.types()
        videoTypes.value = typeRes.data
      } catch (error) {
        console.error('加载数据失败:', error)
      } finally {
        loading.value = false
      }
    }
    
    const getVideoTypeName = (typeId) => {
      const type = videoTypes.value.find(t => t.id === typeId)
      return type ? type.name : '未知'
    }
    
    const formatDate = (dateStr) => {
      return new Date(dateStr).toLocaleString()
    }
    
    const resetForm = () => {
      Object.assign(form, {
        id: null,
        title: '',
        videoDesc: '',
        videoType: '',
        url: '',
        cover: '',
        status: 0
      })
    }
    
    const handleAdd = () => {
      resetForm()
      dialogTitle.value = '添加视频'
      dialogVisible.value = true
    }
    
    const handleEdit = (row) => {
      Object.assign(form, row)
      dialogTitle.value = '编辑视频'
      dialogVisible.value = true
    }
    
    const handleDelete = (row) => {
      ElMessageBox.confirm('确定要删除这个视频吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        ElMessage.success('删除成功')
        loadData()
      })
    }
    
    const handleSubmit = async () => {
      try {
        await formRef.value.validate()
        ElMessage.success('保存成功')
        dialogVisible.value = false
        loadData()
      } catch (error) {
        console.error('保存失败:', error)
      }
    }
    
    onMounted(() => {
      loadData()
    })
    
    return {
      loading,
      videoList,
      videoTypes,
      dialogVisible,
      dialogTitle,
      form,
      rules,
      formRef,
      getVideoTypeName,
      formatDate,
      handleAdd,
      handleEdit,
      handleDelete,
      handleSubmit
    }
  }
}
</script>

<style scoped>
.video-list {
  padding: 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style> 