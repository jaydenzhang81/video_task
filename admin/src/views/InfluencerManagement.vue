<template>
  <div class="influencer-management">
    <div class="page-header">
      <h2>达人管理</h2>
      <div class="header-actions">
        <el-button type="success" @click="showAddTeamDialog = true">
          <el-icon><Plus /></el-icon>
          创建团队
        </el-button>
        <el-button type="primary" @click="showAddInfluencerDialog = true">
          <el-icon><User /></el-icon>
          添加达人
        </el-button>
      </div>
    </div>

    <!-- 达人概览 -->
    <div class="influencer-overview">
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="overview-card">
            <div class="card-icon total">
              <el-icon><User /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-title">总达人数</div>
              <div class="card-value">{{ overviewData.totalInfluencers }}</div>
              <div class="card-change increase">
                <el-icon><ArrowUp /></el-icon>
                +12.5%
              </div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="overview-card">
            <div class="card-icon teams">
              <el-icon><OfficeBuilding /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-title">团队数量</div>
              <div class="card-value">{{ overviewData.totalTeams }}</div>
              <div class="card-change increase">
                <el-icon><ArrowUp /></el-icon>
                +8.3%
              </div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="overview-card">
            <div class="card-icon revenue">
              <el-icon><Money /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-title">总收益</div>
              <div class="card-value">¥{{ overviewData.totalRevenue.toLocaleString() }}</div>
              <div class="card-change increase">
                <el-icon><ArrowUp /></el-icon>
                +15.7%
              </div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="overview-card">
            <div class="card-icon videos">
              <el-icon><VideoCamera /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-title">发布视频</div>
              <div class="card-value">{{ overviewData.totalVideos.toLocaleString() }}</div>
              <div class="card-change increase">
                <el-icon><ArrowUp /></el-icon>
                +22.1%
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 搜索和筛选 -->
    <div class="search-filters">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-input
            v-model="searchForm.influencerName"
            placeholder="请输入达人昵称"
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :span="4">
          <el-select v-model="searchForm.teamId" placeholder="所属团队" clearable>
            <el-option
              v-for="team in teams"
              :key="team.id"
              :label="team.teamName"
              :value="team.id"
            />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="searchForm.status" placeholder="状态" clearable>
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

    <!-- 达人列表 -->
    <div class="influencer-list">
      <el-table :data="influencerList" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="达人信息" min-width="200">
          <template #default="scope">
            <div class="influencer-info">
              <el-avatar :size="50" :src="scope.row.avatar" />
              <div class="info-content">
                <div class="influencer-name">{{ scope.row.influencerName }}</div>
                <div class="user-name">{{ scope.row.userName }}</div>
                <div class="phone">{{ scope.row.phone }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="teamName" label="所属团队" width="150" />
        <el-table-column prop="totalRevenue" label="总收益" width="120">
          <template #default="scope">
            <span class="revenue">¥{{ scope.row.totalRevenue.toLocaleString() }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalVideos" label="发布视频" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'warning'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ctime" label="注册时间" width="180" />
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

    <!-- 添加/编辑达人对话框 -->
    <el-dialog
      v-model="showAddInfluencerDialog"
      :title="isEdit ? '编辑达人' : '添加达人'"
      width="600px"
    >
      <el-form
        ref="influencerFormRef"
        :model="influencerForm"
        :rules="influencerRules"
        label-width="120px"
      >
        <el-form-item label="用户选择" prop="userId">
          <el-select 
            v-model="influencerForm.userId" 
            placeholder="请选择用户"
            filterable
            remote
            :remote-method="searchUsers"
            :loading="userLoading"
          >
            <el-option
              v-for="user in userOptions"
              :key="user.id"
              :label="user.nick"
              :value="user.id"
            >
              <div class="user-option">
                <span>{{ user.nick }}</span>
                <span class="user-phone">{{ user.phone }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="达人昵称" prop="influencerName">
          <el-input v-model="influencerForm.influencerName" placeholder="请输入达人昵称" />
        </el-form-item>
        
        <el-form-item label="所属团队" prop="teamId">
          <el-select v-model="influencerForm.teamId" placeholder="请选择团队">
            <el-option
              v-for="team in teams"
              :key="team.id"
              :label="team.teamName"
              :value="team.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="头像" prop="avatar">
          <el-upload
            class="avatar-uploader"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
            action="/api/upload"
          >
            <img v-if="influencerForm.avatar" :src="influencerForm.avatar" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="influencerForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showAddInfluencerDialog = false">取消</el-button>
          <el-button type="primary" @click="handleInfluencerSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 创建团队对话框 -->
    <el-dialog
      v-model="showAddTeamDialog"
      title="创建团队"
      width="500px"
    >
      <el-form
        ref="teamFormRef"
        :model="teamForm"
        :rules="teamRules"
        label-width="120px"
      >
        <el-form-item label="团队名称" prop="teamName">
          <el-input v-model="teamForm.teamName" placeholder="请输入团队名称" />
        </el-form-item>
        
        <el-form-item label="团队描述" prop="teamDesc">
          <el-input
            v-model="teamForm.teamDesc"
            type="textarea"
            :rows="3"
            placeholder="请输入团队描述"
          />
        </el-form-item>
        
        <el-form-item label="团队负责人" prop="leaderId">
          <el-select v-model="teamForm.leaderId" placeholder="请选择负责人">
            <el-option
              v-for="influencer in influencerOptions"
              :key="influencer.id"
              :label="influencer.influencerName"
              :value="influencer.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showAddTeamDialog = false">取消</el-button>
          <el-button type="primary" @click="handleTeamSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 达人详情对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      title="达人详情"
      width="800px"
    >
      <div v-if="currentInfluencer" class="influencer-detail">
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="detail-section">
              <h3>基本信息</h3>
              <el-descriptions :column="1" border>
                <el-descriptions-item label="达人ID">{{ currentInfluencer.id }}</el-descriptions-item>
                <el-descriptions-item label="达人昵称">{{ currentInfluencer.influencerName }}</el-descriptions-item>
                <el-descriptions-item label="用户名">{{ currentInfluencer.userName }}</el-descriptions-item>
                <el-descriptions-item label="手机号">{{ currentInfluencer.phone }}</el-descriptions-item>
                <el-descriptions-item label="所属团队">{{ currentInfluencer.teamName }}</el-descriptions-item>
                <el-descriptions-item label="状态">
                  <el-tag :type="currentInfluencer.status === 1 ? 'success' : 'warning'">
                    {{ currentInfluencer.status === 1 ? '启用' : '禁用' }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="注册时间">{{ currentInfluencer.ctime }}</el-descriptions-item>
              </el-descriptions>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="detail-section">
              <h3>数据统计</h3>
              <div class="stats-cards">
                <div class="stat-card">
                  <div class="stat-value">¥{{ currentInfluencer.totalRevenue.toLocaleString() }}</div>
                  <div class="stat-label">总收益</div>
                </div>
                <div class="stat-card">
                  <div class="stat-value">{{ currentInfluencer.totalVideos }}</div>
                  <div class="stat-label">发布视频</div>
                </div>
                <div class="stat-card">
                  <div class="stat-value">{{ currentInfluencer.totalLikes }}</div>
                  <div class="stat-label">总点赞</div>
                </div>
                <div class="stat-card">
                  <div class="stat-value">{{ currentInfluencer.totalComments }}</div>
                  <div class="stat-label">总评论</div>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
        
        <div class="detail-section">
          <h3>最近视频</h3>
          <el-table :data="recentVideos" style="width: 100%">
            <el-table-column prop="title" label="视频标题" />
            <el-table-column prop="likeCount" label="点赞数" width="100" />
            <el-table-column prop="commentCount" label="评论数" width="100" />
            <el-table-column prop="viewCount" label="观看数" width="100" />
            <el-table-column prop="ctime" label="发布时间" width="180" />
          </el-table>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Search, 
  Plus, 
  User, 
  OfficeBuilding, 
  Money, 
  VideoCamera, 
  ArrowUp 
} from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const userLoading = ref(false)
const showAddInfluencerDialog = ref(false)
const showAddTeamDialog = ref(false)
const showDetailDialog = ref(false)
const isEdit = ref(false)
const currentInfluencer = ref(null)

// 搜索表单
const searchForm = reactive({
  influencerName: '',
  teamId: '',
  status: '',
  dateRange: []
})

// 达人表单
const influencerForm = reactive({
  id: null,
  userId: '',
  influencerName: '',
  teamId: '',
  avatar: '',
  status: 1
})

// 团队表单
const teamForm = reactive({
  teamName: '',
  teamDesc: '',
  leaderId: ''
})

// 分页
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 数据列表
const influencerList = ref([])
const teams = ref([])
const userOptions = ref([])
const influencerOptions = ref([])
const recentVideos = ref([])

// 概览数据
const overviewData = reactive({
  totalInfluencers: 1542,
  totalTeams: 25,
  totalRevenue: 1250000,
  totalVideos: 15420
})

// 表单验证规则
const influencerRules = {
  userId: [{ required: true, message: '请选择用户', trigger: 'change' }],
  influencerName: [{ required: true, message: '请输入达人昵称', trigger: 'blur' }],
  teamId: [{ required: true, message: '请选择团队', trigger: 'change' }]
}

const teamRules = {
  teamName: [{ required: true, message: '请输入团队名称', trigger: 'blur' }],
  teamDesc: [{ required: true, message: '请输入团队描述', trigger: 'blur' }]
}

// 表单引用
const influencerFormRef = ref()
const teamFormRef = ref()

// 方法
const loadInfluencerList = async () => {
  loading.value = true
  try {
    // TODO: 调用真实API
    // const response = await api.getInfluencerList({
    //   influencerName: searchForm.influencerName,
    //   teamId: searchForm.teamId,
    //   status: searchForm.status,
    //   dateRange: searchForm.dateRange,
    //   page: pagination.current,
    //   size: pagination.size
    // })
    // influencerList.value = response.data.records || []
    // pagination.total = response.data.total || 0
    
    influencerList.value = []
    pagination.total = 0
  } catch (error) {
    ElMessage.error('加载达人列表失败')
  } finally {
    loading.value = false
  }
}

const loadTeams = async () => {
  try {
    // TODO: 调用真实API获取团队列表
    // const response = await api.getTeams()
    // teams.value = response.data || []
    teams.value = []
  } catch (error) {
    console.error('加载团队列表失败:', error)
  }
}

const searchUsers = async (query) => {
  if (query) {
    userLoading.value = true
    try {
      // TODO: 调用真实搜索用户API
      await new Promise(resolve => setTimeout(resolve, 500))
      userOptions.value = [
        { id: 1, nick: '用户001', phone: '138****8888' },
        { id: 2, nick: '用户002', phone: '139****9999' }
      ].filter(user => user.nick.includes(query))
    } finally {
      userLoading.value = false
    }
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadInfluencerList()
}

const resetSearch = () => {
  Object.assign(searchForm, {
    influencerName: '',
    teamId: '',
    status: '',
    dateRange: []
  })
  handleSearch()
}

const handleSizeChange = (size) => {
  pagination.size = size
  loadInfluencerList()
}

const handleCurrentChange = (current) => {
  pagination.current = current
  loadInfluencerList()
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(influencerForm, {
    id: row.id,
    userId: row.userId,
    influencerName: row.influencerName,
    teamId: row.teamId,
    avatar: row.avatar,
    status: row.status
  })
  showAddInfluencerDialog.value = true
}

const handleViewDetail = async (row) => {
  currentInfluencer.value = row
  showDetailDialog.value = true
  
  // 加载最近视频
  recentVideos.value = [
    {
      title: '快!看!美不美~',
      likeCount: 1500,
      commentCount: 250,
      viewCount: 12000,
      ctime: '2024-01-15 10:30:00'
    },
    {
      title: '掉进樱花的世界',
      likeCount: 1200,
      commentCount: 180,
      viewCount: 9800,
      ctime: '2024-01-14 15:20:00'
    }
  ]
}

const handleToggleStatus = async (row) => {
  try {
    const action = row.status === 1 ? '禁用' : '启用'
    await ElMessageBox.confirm(`确定要${action}这个达人吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // TODO: 调用真实API
    ElMessage.success(`${action}成功`)
    loadInfluencerList()
  } catch {
    // 用户取消操作
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这个达人吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // TODO: 调用真实删除API
    ElMessage.success('删除成功')
    loadInfluencerList()
  } catch {
    // 用户取消删除
  }
}

const handleInfluencerSubmit = async () => {
  try {
    await influencerFormRef.value.validate()
    
    // TODO: 调用真实提交API
    ElMessage.success(isEdit.value ? '编辑成功' : '添加成功')
    showAddInfluencerDialog.value = false
    loadInfluencerList()
    
    // 重置表单
    if (!isEdit.value) {
      Object.assign(influencerForm, {
        id: null,
        userId: '',
        influencerName: '',
        teamId: '',
        avatar: '',
        status: 1
      })
    }
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

const handleTeamSubmit = async () => {
  try {
    await teamFormRef.value.validate()
    
    // TODO: 调用真实提交API
    ElMessage.success('团队创建成功')
    showAddTeamDialog.value = false
    loadTeams()
    
    // 重置表单
    Object.assign(teamForm, {
      teamName: '',
      teamDesc: '',
      leaderId: ''
    })
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

const handleAvatarSuccess = (response) => {
  influencerForm.avatar = response.url
}

const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('上传头像只能是图片格式!')
  }
  if (!isLt2M) {
    ElMessage.error('上传头像大小不能超过 2MB!')
  }
  return isImage && isLt2M
}

// 生命周期
onMounted(() => {
  loadInfluencerList()
  loadTeams()
})
</script>

<style scoped>
.influencer-management {
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

.header-actions {
  display: flex;
  gap: 12px;
}

.influencer-overview {
  margin-bottom: 20px;
}

.overview-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 16px;
}

.card-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}

.card-icon.total {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.card-icon.teams {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.card-icon.revenue {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.card-icon.videos {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.card-content {
  flex: 1;
}

.card-title {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.card-value {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.card-change {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #10b981;
}

.search-filters {
  background: white;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.influencer-list {
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

.influencer-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.info-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.influencer-name {
  font-weight: 500;
  font-size: 14px;
}

.user-name {
  font-size: 12px;
  color: #666;
}

.phone {
  font-size: 12px;
  color: #999;
}

.revenue {
  color: #f56c6c;
  font-weight: 600;
}

.user-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-phone {
  font-size: 12px;
  color: #999;
}

.avatar-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 100px;
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-uploader:hover {
  border-color: #409eff;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  text-align: center;
  line-height: 100px;
}

.avatar {
  width: 100px;
  height: 100px;
  display: block;
  object-fit: cover;
}

.influencer-detail {
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
