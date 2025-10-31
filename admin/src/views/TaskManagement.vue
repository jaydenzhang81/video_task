<template>
  <div class="task-management">
    <div class="page-header">
      <h2>任务管理</h2>
      <el-button type="primary" @click="showAddDialog = true">
        <el-icon><Plus /></el-icon>
        创建任务
      </el-button>
    </div>

    <!-- 搜索和筛选 -->
    <div class="search-filters">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-input
            v-model="searchForm.videoTitle"
            placeholder="请输入视频标题"
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :span="4">
          <el-select v-model="searchForm.platformId" placeholder="平台" clearable>
            <el-option
              v-for="platform in platforms"
              :key="platform.id"
              :label="platform.name"
              :value="platform.id"
            />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="searchForm.status" placeholder="任务状态" clearable>
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
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

    <!-- 任务列表 -->
    <div class="task-list">
      <el-table :data="taskList" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="视频信息" min-width="200">
          <template #default="scope">
            <div class="video-info">
              <el-image
                :src="scope.row.videoCover"
                :preview-src-list="[scope.row.videoCover]"
                fit="cover"
                style="width: 60px; height: 40px; border-radius: 4px; margin-right: 12px;"
              />
              <div>
                <div class="video-title">{{ scope.row.videoTitle }}</div>
                <div class="video-desc">{{ scope.row.videoDesc }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="platformName" label="平台" width="100" />
        <el-table-column prop="rewardAmount" label="奖励金额" width="120">
          <template #default="scope">
            <span class="reward-amount">¥{{ scope.row.rewardAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="taskDesc" label="任务描述" min-width="200" />
        <el-table-column label="任务时间" width="200">
          <template #default="scope">
            <div>{{ scope.row.startTime }}</div>
            <div>{{ scope.row.endTime }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'warning'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="统计" width="150">
          <template #default="scope">
            <div class="task-stats">
              <div>参与: {{ scope.row.participantCount }}</div>
              <div>完成: {{ scope.row.completedCount }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="primary" @click="handleViewDetail(scope.row)">详情</el-button>
            <el-button 
              size="small" 
              :type="scope.row.status === 1 ? 'warning' : 'success'"
              @click="handleToggleStatus(scope.row)"
            >
              {{ scope.row.status === 1 ? '禁用' : '启用' }}
            </el-button>
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

    <!-- 添加/编辑任务对话框 -->
    <el-dialog
      v-model="showAddDialog"
      :title="isEdit ? '编辑任务' : '创建任务'"
      width="800px"
    >
      <el-form
        ref="taskFormRef"
        :model="taskForm"
        :rules="taskRules"
        label-width="120px"
      >
        <el-form-item label="选择视频" prop="videoId">
          <el-select 
            v-model="taskForm.videoId" 
            placeholder="请选择视频"
            filterable
            remote
            :remote-method="searchVideos"
            :loading="videoLoading"
          >
            <el-option
              v-for="video in videoOptions"
              :key="video.id"
              :label="video.title"
              :value="video.id"
            >
              <div class="video-option">
                <el-image
                  :src="video.cover"
                  fit="cover"
                  style="width: 40px; height: 30px; border-radius: 4px; margin-right: 8px;"
                />
                <span>{{ video.title }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="平台设置" prop="platformId">
          <el-select v-model="taskForm.platformId" placeholder="请选择平台">
            <el-option
              v-for="platform in platforms"
              :key="platform.id"
              :label="platform.name"
              :value="platform.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="奖励金额" prop="rewardAmount">
          <el-input-number
            v-model="taskForm.rewardAmount"
            :min="0"
            :precision="2"
            :step="0.01"
            style="width: 200px;"
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
            style="width: 100%;"
          />
        </el-form-item>
        
        <el-form-item label="任务状态" prop="status">
          <el-radio-group v-model="taskForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
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

    <!-- 任务详情对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      title="任务详情"
      width="1000px"
    >
      <div v-if="currentTask" class="task-detail">
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="detail-section">
              <h3>基本信息</h3>
              <el-descriptions :column="1" border>
                <el-descriptions-item label="任务ID">{{ currentTask.id }}</el-descriptions-item>
                <el-descriptions-item label="视频标题">{{ currentTask.videoTitle }}</el-descriptions-item>
                <el-descriptions-item label="平台">{{ currentTask.platformName }}</el-descriptions-item>
                <el-descriptions-item label="奖励金额">¥{{ currentTask.rewardAmount }}</el-descriptions-item>
                <el-descriptions-item label="任务描述">{{ currentTask.taskDesc }}</el-descriptions-item>
                <el-descriptions-item label="开始时间">{{ currentTask.startTime }}</el-descriptions-item>
                <el-descriptions-item label="结束时间">{{ currentTask.endTime }}</el-descriptions-item>
                <el-descriptions-item label="状态">
                  <el-tag :type="currentTask.status === 1 ? 'success' : 'warning'">
                    {{ currentTask.status === 1 ? '启用' : '禁用' }}
                  </el-tag>
                </el-descriptions-item>
              </el-descriptions>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="detail-section">
              <h3>参与统计</h3>
              <div class="stats-cards">
                <div class="stat-card">
                  <div class="stat-value">{{ currentTask.participantCount }}</div>
                  <div class="stat-label">参与人数</div>
                </div>
                <div class="stat-card">
                  <div class="stat-value">{{ currentTask.completedCount }}</div>
                  <div class="stat-label">完成人数</div>
                </div>
                <div class="stat-card">
                  <div class="stat-value">{{ currentTask.successRate }}%</div>
                  <div class="stat-label">成功率</div>
                </div>
                <div class="stat-card">
                  <div class="stat-value">¥{{ currentTask.totalReward }}</div>
                  <div class="stat-label">总奖励</div>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
        
        <div class="detail-section">
          <h3>参与记录</h3>
          <el-table :data="participantList" style="width: 100%">
            <el-table-column prop="userId" label="用户ID" width="100" />
            <el-table-column prop="userName" label="用户名" width="150" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.status === 1 ? 'success' : 'warning'">
                  {{ scope.row.status === 1 ? '完成' : '进行中' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="likeCount" label="点赞数" width="100" />
            <el-table-column prop="commentCount" label="评论数" width="100" />
            <el-table-column prop="viewCount" label="观看数" width="100" />
            <el-table-column prop="rewardAmount" label="获得奖励" width="120">
              <template #default="scope">
                ¥{{ scope.row.rewardAmount }}
              </template>
            </el-table-column>
            <el-table-column prop="ctime" label="参与时间" width="180" />
          </el-table>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const videoLoading = ref(false)
const showAddDialog = ref(false)
const showDetailDialog = ref(false)
const isEdit = ref(false)
const currentTask = ref(null)

// 搜索表单
const searchForm = reactive({
  videoTitle: '',
  platformId: '',
  status: '',
  dateRange: []
})

// 任务表单
const taskForm = reactive({
  id: null,
  videoId: '',
  platformId: '',
  rewardAmount: 0,
  taskDesc: '',
  timeRange: [],
  status: 1
})

// 分页
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 数据列表
const taskList = ref([])
const videoOptions = ref([])
const participantList = ref([])
const platforms = ref([
  { id: 1, name: '抖音' },
  { id: 2, name: '快手' },
  { id: 3, name: '小红书' }
])

// 表单验证规则
const taskRules = {
  videoId: [{ required: true, message: '请选择视频', trigger: 'change' }],
  platformId: [{ required: true, message: '请选择平台', trigger: 'change' }],
  rewardAmount: [{ required: true, message: '请输入奖励金额', trigger: 'blur' }],
  taskDesc: [{ required: true, message: '请输入任务描述', trigger: 'blur' }],
  timeRange: [{ required: true, message: '请选择任务时间', trigger: 'change' }]
}

// 表单引用
const taskFormRef = ref()

// 方法
const loadTaskList = async () => {
  loading.value = true
  try {
    // TODO: 调用真实API
    // const response = await api.getTaskList({
    //   videoTitle: searchForm.videoTitle,
    //   platformId: searchForm.platformId,
    //   status: searchForm.status,
    //   dateRange: searchForm.dateRange,
    //   page: pagination.current,
    //   size: pagination.size
    // })
    // taskList.value = response.data.records || []
    // pagination.total = response.data.total || 0
    
    taskList.value = []
    pagination.total = 0
  } catch (error) {
    ElMessage.error('加载任务列表失败')
  } finally {
    loading.value = false
  }
}

const searchVideos = async (query) => {
  if (query) {
    videoLoading.value = true
    try {
      // TODO: 调用真实搜索视频API
      await new Promise(resolve => setTimeout(resolve, 500))
      videoOptions.value = [
        { id: 1, title: '快!看!美不美~', cover: 'https://via.placeholder.com/300x200' },
        { id: 2, title: '掉进樱花的世界', cover: 'https://via.placeholder.com/300x200' }
      ].filter(video => video.title.includes(query))
    } finally {
      videoLoading.value = false
    }
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadTaskList()
}

const resetSearch = () => {
  Object.assign(searchForm, {
    videoTitle: '',
    platformId: '',
    status: '',
    dateRange: []
  })
  handleSearch()
}

const handleSizeChange = (size) => {
  pagination.size = size
  loadTaskList()
}

const handleCurrentChange = (current) => {
  pagination.current = current
  loadTaskList()
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(taskForm, {
    id: row.id,
    videoId: row.videoId,
    platformId: row.platformId,
    rewardAmount: row.rewardAmount,
    taskDesc: row.taskDesc,
    timeRange: [row.startTime, row.endTime],
    status: row.status
  })
  showAddDialog.value = true
}

const handleViewDetail = async (row) => {
  currentTask.value = row
  showDetailDialog.value = true
  
  // 加载参与记录
  participantList.value = [
    {
      userId: 1,
      userName: '用户001',
      status: 1,
      likeCount: 150,
      commentCount: 25,
      viewCount: 1200,
      rewardAmount: 50.00,
      ctime: '2024-01-15 10:30:00'
    },
    {
      userId: 2,
      userName: '用户002',
      status: 0,
      likeCount: 80,
      commentCount: 15,
      viewCount: 800,
      rewardAmount: 0.00,
      ctime: '2024-01-15 11:20:00'
    }
  ]
}

const handleToggleStatus = async (row) => {
  try {
    const action = row.status === 1 ? '禁用' : '启用'
    await ElMessageBox.confirm(`确定要${action}这个任务吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // TODO: 调用真实API
    ElMessage.success(`${action}成功`)
    loadTaskList()
  } catch {
    // 用户取消操作
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这个任务吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // TODO: 调用真实删除API
    ElMessage.success('删除成功')
    loadTaskList()
  } catch {
    // 用户取消删除
  }
}

const handleSubmit = async () => {
  try {
    await taskFormRef.value.validate()
    
    // TODO: 调用真实提交API
    ElMessage.success(isEdit.value ? '编辑成功' : '创建成功')
    showAddDialog.value = false
    loadTaskList()
    
    // 重置表单
    if (!isEdit.value) {
      Object.assign(taskForm, {
        id: null,
        videoId: '',
        platformId: '',
        rewardAmount: 0,
        taskDesc: '',
        timeRange: [],
        status: 1
      })
    }
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

// 生命周期
onMounted(() => {
  loadTaskList()
})
</script>

<style scoped>
.task-management {
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

.task-list {
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

.video-info {
  display: flex;
  align-items: center;
}

.video-title {
  font-weight: 500;
  margin-bottom: 4px;
}

.video-desc {
  font-size: 12px;
  color: #666;
}

.reward-amount {
  color: #f56c6c;
  font-weight: 600;
}

.task-stats {
  font-size: 12px;
  color: #666;
}

.unit {
  margin-left: 8px;
  color: #666;
}

.video-option {
  display: flex;
  align-items: center;
}

.task-detail {
  padding: 20px 0;
}

.detail-section {
  margin-bottom: 30px;
}

.detail-section h3 {
  margin-bottom: 16px;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.stat-card {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 8px;
  text-align: center;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #3b82f6;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 12px;
  color: #666;
}
</style>
