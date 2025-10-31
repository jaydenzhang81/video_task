<template>
  <div class="video-management">
    <div class="page-header">
      <h2>视频管理</h2>
      <el-button type="primary" @click="showAddDialog = true">
        <el-icon><Plus /></el-icon>
        添加视频
      </el-button>
    </div>

    <!-- 搜索和筛选 -->
    <div class="search-filters">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-input
            v-model="searchForm.title"
            placeholder="请输入视频标题"
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :span="4">
          <el-select v-model="searchForm.videoType" placeholder="视频类型" clearable>
            <el-option
              v-for="type in videoTypes"
              :key="type.id"
              :label="type.name"
              :value="type.id"
            />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="searchForm.status" placeholder="发布状态" clearable>
            <el-option label="未发布" :value="0" />
            <el-option label="已发布" :value="1" />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-col>
      </el-row>
    </div>

    <!-- 视频列表 -->
    <div class="video-list">
      <el-table :data="videoList" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="封面" width="120">
          <template #default="scope">
            <el-image
              :src="scope.row.cover"
              :preview-src-list="[scope.row.cover]"
              fit="cover"
              style="width: 80px; height: 60px; border-radius: 4px;"
            />
          </template>
        </el-table-column>
        <el-table-column prop="title" label="视频标题" min-width="200" />
        <el-table-column prop="videoTypeName" label="类型" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'warning'">
              {{ scope.row.status === 1 ? '已发布' : '未发布' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ctime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="primary" @click="handleTask(scope.row)">任务设置</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 添加/编辑视频对话框 -->
    <el-dialog
      v-model="showAddDialog"
      :title="isEdit ? '编辑视频' : '添加视频'"
      width="600px"
    >
      <el-form
        ref="videoFormRef"
        :model="videoForm"
        :rules="videoRules"
        label-width="100px"
      >
        <el-form-item label="视频标题" prop="title">
          <el-input v-model="videoForm.title" placeholder="请输入视频标题" />
        </el-form-item>
        
        <el-form-item label="视频描述" prop="videoDesc">
          <el-input
            v-model="videoForm.videoDesc"
            type="textarea"
            :rows="3"
            placeholder="请输入视频描述"
          />
        </el-form-item>
        
        <el-form-item label="视频类型" prop="videoType">
          <el-select v-model="videoForm.videoType" placeholder="请选择视频类型">
            <el-option
              v-for="type in videoTypes"
              :key="type.id"
              :label="type.name"
              :value="type.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="视频封面" prop="cover">
          <el-upload
            class="cover-uploader"
            :show-file-list="false"
            :on-success="handleCoverSuccess"
            :before-upload="beforeCoverUpload"
            action="/api/upload"
          >
            <img v-if="videoForm.cover" :src="videoForm.cover" class="cover" />
            <el-icon v-else class="cover-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        
        <el-form-item label="视频文件" prop="url">
          <el-upload
            class="video-uploader"
            :show-file-list="false"
            :on-success="handleVideoSuccess"
            :before-upload="beforeVideoUpload"
            action="/api/upload"
          >
            <el-button type="primary">选择视频文件</el-button>
          </el-upload>
          <div v-if="videoForm.url" class="video-preview">
            已选择: {{ videoForm.url }}
          </div>
        </el-form-item>
        
        <el-form-item label="发布状态" prop="status">
          <el-radio-group v-model="videoForm.status">
            <el-radio :label="0">未发布</el-radio>
            <el-radio :label="1">已发布</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showAddDialog = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 任务设置对话框 -->
    <el-dialog
      v-model="showTaskDialog"
      title="任务设置"
      width="800px"
    >
      <el-form
        ref="taskFormRef"
        :model="taskForm"
        :rules="taskRules"
        label-width="120px"
      >
        <el-form-item label="视频标题">
          <span>{{ currentVideo?.title }}</span>
        </el-form-item>
        
        <el-form-item label="平台设置">
          <el-checkbox-group v-model="taskForm.platforms">
            <el-checkbox
              v-for="platform in platforms"
              :key="platform.id"
              :label="platform.id"
            >
              {{ platform.name }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        
        <el-form-item label="奖励金额" prop="rewardAmount">
          <el-input-number
            v-model="taskForm.rewardAmount"
            :min="0"
            :precision="2"
            :step="0.01"
          />
          <span class="unit">元</span>
        </el-form-item>
        
        <el-form-item label="任务描述" prop="taskDesc">
          <el-input
            v-model="taskForm.taskDesc"
            type="textarea"
            :rows="3"
            placeholder="请输入任务描述"
          />
        </el-form-item>
        
        <el-form-item label="任务时间" prop="timeRange">
          <el-date-picker
            v-model="taskForm.timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showTaskDialog = false">取消</el-button>
          <el-button type="primary" @click="handleTaskSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const showAddDialog = ref(false)
const showTaskDialog = ref(false)
const isEdit = ref(false)
const currentVideo = ref(null)

// 搜索表单
const searchForm = reactive({
  title: '',
  videoType: '',
  status: '',
  dateRange: []
})

// 视频表单
const videoForm = reactive({
  id: null,
  title: '',
  videoDesc: '',
  videoType: '',
  cover: '',
  url: '',
  status: 0
})

// 任务表单
const taskForm = reactive({
  platforms: [],
  rewardAmount: 0,
  taskDesc: '',
  timeRange: []
})

// 分页
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 数据列表
const videoList = ref([])
const videoTypes = ref([
  { id: 1, name: '带货视频' },
  { id: 2, name: '种草短视频' }
])
const platforms = ref([
  { id: 1, name: '抖音' },
  { id: 2, name: '快手' },
  { id: 3, name: '小红书' }
])

// 表单验证规则
const videoRules = {
  title: [{ required: true, message: '请输入视频标题', trigger: 'blur' }],
  videoType: [{ required: true, message: '请选择视频类型', trigger: 'change' }],
  url: [{ required: true, message: '请上传视频文件', trigger: 'change' }]
}

const taskRules = {
  platforms: [{ required: true, message: '请选择平台', trigger: 'change' }],
  rewardAmount: [{ required: true, message: '请输入奖励金额', trigger: 'blur' }],
  taskDesc: [{ required: true, message: '请输入任务描述', trigger: 'blur' }],
  timeRange: [{ required: true, message: '请选择任务时间', trigger: 'change' }]
}

// 表单引用
const videoFormRef = ref()
const taskFormRef = ref()

// 方法
const loadVideoList = async () => {
  loading.value = true
  try {
    // TODO: 调用真实API
    // const response = await api.getVideoList({
    //   title: searchForm.title,
    //   videoType: searchForm.videoType,
    //   status: searchForm.status,
    //   dateRange: searchForm.dateRange,
    //   page: pagination.current,
    //   size: pagination.size
    // })
    // videoList.value = response.data.records || []
    // pagination.total = response.data.total || 0
    
    videoList.value = []
    pagination.total = 0
  } catch (error) {
    ElMessage.error('加载视频列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadVideoList()
}

const resetSearch = () => {
  Object.assign(searchForm, {
    title: '',
    videoType: '',
    status: '',
    dateRange: []
  })
  handleSearch()
}

const handleSizeChange = (size) => {
  pagination.size = size
  loadVideoList()
}

const handleCurrentChange = (current) => {
  pagination.current = current
  loadVideoList()
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(videoForm, row)
  showAddDialog.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这个视频吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // TODO: 调用真实删除API
    ElMessage.success('删除成功')
    loadVideoList()
  } catch {
    // 用户取消删除
  }
}

const handleTask = (row) => {
  currentVideo.value = row
  showTaskDialog.value = true
}

const handleSubmit = async () => {
  try {
    await videoFormRef.value.validate()
    
    // TODO: 调用真实提交API
    ElMessage.success(isEdit.value ? '编辑成功' : '添加成功')
    showAddDialog.value = false
    loadVideoList()
    
    // 重置表单
    if (!isEdit.value) {
      Object.assign(videoForm, {
        id: null,
        title: '',
        videoDesc: '',
        videoType: '',
        cover: '',
        url: '',
        status: 0
      })
    }
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

const handleTaskSubmit = async () => {
  try {
    await taskFormRef.value.validate()
    
    // TODO: 调用真实提交API
    ElMessage.success('任务设置成功')
    showTaskDialog.value = false
    
    // 重置表单
    Object.assign(taskForm, {
      platforms: [],
      rewardAmount: 0,
      taskDesc: '',
      timeRange: []
    })
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

const handleCoverSuccess = (response) => {
  videoForm.cover = response.url
}

const beforeCoverUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('上传封面只能是图片格式!')
  }
  if (!isLt2M) {
    ElMessage.error('上传封面大小不能超过 2MB!')
  }
  return isImage && isLt2M
}

const handleVideoSuccess = (response) => {
  videoForm.url = response.url
}

const beforeVideoUpload = (file) => {
  const isVideo = file.type.startsWith('video/')
  const isLt100M = file.size / 1024 / 1024 < 100

  if (!isVideo) {
    ElMessage.error('上传文件只能是视频格式!')
  }
  if (!isLt100M) {
    ElMessage.error('上传视频大小不能超过 100MB!')
  }
  return isVideo && isLt100M
}

// 生命周期
onMounted(() => {
  loadVideoList()
})
</script>

<style scoped>
.video-management {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.search-filters {
  background: white;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.video-list {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.cover-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 178px;
  height: 178px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-uploader:hover {
  border-color: #409eff;
}

.cover-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  text-align: center;
  line-height: 178px;
}

.cover {
  width: 178px;
  height: 178px;
  display: block;
  object-fit: cover;
}

.video-preview {
  margin-top: 10px;
  padding: 8px;
  background: #f5f5f5;
  border-radius: 4px;
  font-size: 12px;
  color: #666;
}

.unit {
  margin-left: 8px;
  color: #666;
}
</style>
