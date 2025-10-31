<template>
  <div class="system-management">
    <div class="page-header">
      <h2>系统管理</h2>
      <el-button type="primary" @click="showAddAdminDialog = true">
        <el-icon><Plus /></el-icon>
        添加管理员
      </el-button>
    </div>

    <!-- 系统概览 -->
    <div class="system-overview">
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="overview-card">
            <div class="card-icon admins">
              <el-icon><User /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-title">管理员数量</div>
              <div class="card-value">{{ systemData.totalAdmins }}</div>
              <div class="card-change increase">
                <el-icon><ArrowUp /></el-icon>
                +2
              </div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="overview-card">
            <div class="card-icon online">
              <el-icon><Monitor /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-title">在线用户</div>
              <div class="card-value">{{ systemData.onlineUsers }}</div>
              <div class="card-change increase">
                <el-icon><ArrowUp /></el-icon>
                +15
              </div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="overview-card">
            <div class="card-icon storage">
              <el-icon><Folder /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-title">存储使用</div>
              <div class="card-value">{{ systemData.storageUsage }}%</div>
              <div class="card-change warning">
                <el-icon><Warning /></el-icon>
                接近上限
              </div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="overview-card">
            <div class="card-icon uptime">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-title">运行时间</div>
              <div class="card-value">{{ systemData.uptime }}</div>
              <div class="card-change success">
                <el-icon><Check /></el-icon>
                正常
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 管理员列表 -->
    <div class="admin-section">
      <div class="section-header">
        <h3>管理员管理</h3>
      </div>
      
      <!-- 搜索和筛选 -->
      <div class="search-filters">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-input
              v-model="searchForm.username"
              placeholder="请输入用户名"
              clearable
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-col>
          <el-col :span="4">
            <el-select v-model="searchForm.roleId" placeholder="角色" clearable>
              <el-option label="超级管理员" :value="1" />
              <el-option label="普通管理员" :value="2" />
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

      <!-- 管理员列表 -->
      <div class="admin-list">
        <el-table :data="adminList" style="width: 100%" v-loading="loading">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column label="管理员信息" min-width="200">
            <template #default="scope">
              <div class="admin-info">
                <el-avatar :size="50" :src="scope.row.avatar" />
                <div class="info-content">
                  <div class="admin-name">{{ scope.row.realName }}</div>
                  <div class="admin-username">{{ scope.row.username }}</div>
                  <div class="admin-email">{{ scope.row.email }}</div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="roleName" label="角色" width="120">
            <template #default="scope">
              <el-tag :type="scope.row.roleId === 1 ? 'danger' : 'info'">
                {{ scope.row.roleName }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.status === 1 ? 'success' : 'warning'">
                {{ scope.row.status === 1 ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="lastLoginTime" label="最后登录" width="180" />
          <el-table-column prop="lastLoginIp" label="登录IP" width="140" />
          <el-table-column prop="ctime" label="创建时间" width="180" />
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="scope">
              <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button size="small" type="primary" @click="handleResetPassword(scope.row)">重置密码</el-button>
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
    </div>

    <!-- 系统设置 -->
    <div class="system-settings">
      <div class="section-header">
        <h3>系统设置</h3>
      </div>
      
      <el-form
        ref="settingsFormRef"
        :model="settingsForm"
        :rules="settingsRules"
        label-width="120px"
        class="settings-form"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="系统名称" prop="systemName">
              <el-input v-model="settingsForm.systemName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="系统版本" prop="systemVersion">
              <el-input v-model="settingsForm.systemVersion" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="上传文件大小限制" prop="uploadSizeLimit">
              <el-input-number
                v-model="settingsForm.uploadSizeLimit"
                :min="1"
                :max="1000"
              />
              <span class="unit">MB</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="会话超时时间" prop="sessionTimeout">
              <el-input-number
                v-model="settingsForm.sessionTimeout"
                :min="10"
                :max="1440"
              />
              <span class="unit">分钟</span>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="是否开启注册" prop="allowRegister">
              <el-switch v-model="settingsForm.allowRegister" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否开启验证码" prop="enableCaptcha">
              <el-switch v-model="settingsForm.enableCaptcha" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="系统描述" prop="systemDescription">
          <el-input
            v-model="settingsForm.systemDescription"
            type="textarea"
            :rows="3"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSaveSettings">保存设置</el-button>
          <el-button @click="handleResetSettings">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 添加/编辑管理员对话框 -->
    <el-dialog
      v-model="showAddAdminDialog"
      :title="isEdit ? '编辑管理员' : '添加管理员'"
      width="600px"
    >
      <el-form
        ref="adminFormRef"
        :model="adminForm"
        :rules="adminRules"
        label-width="120px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="adminForm.username" placeholder="请输入用户名" />
        </el-form-item>
        
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="adminForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="adminForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="adminForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        
        <el-form-item label="角色" prop="roleId">
          <el-select v-model="adminForm.roleId" placeholder="请选择角色">
            <el-option label="超级管理员" :value="1" />
            <el-option label="普通管理员" :value="2" />
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
            <img v-if="adminForm.avatar" :src="adminForm.avatar" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="adminForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showAddAdminDialog = false">取消</el-button>
          <el-button type="primary" @click="handleAdminSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Plus, 
  Search, 
  User, 
  Monitor, 
  Folder, 
  Clock, 
  ArrowUp, 
  Warning, 
  Check 
} from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const showAddAdminDialog = ref(false)
const isEdit = ref(false)

// 搜索表单
const searchForm = reactive({
  username: '',
  roleId: '',
  status: '',
  dateRange: []
})

// 管理员表单
const adminForm = reactive({
  id: null,
  username: '',
  realName: '',
  phone: '',
  email: '',
  roleId: 2,
  avatar: '',
  status: 1
})

// 系统设置表单
const settingsForm = reactive({
  systemName: '视频任务管理系统',
  systemVersion: 'v1.0.0',
  uploadSizeLimit: 100,
  sessionTimeout: 30,
  allowRegister: true,
  enableCaptcha: true,
  systemDescription: '专业的视频任务管理平台，支持多平台视频发布和达人管理。'
})

// 分页
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 数据列表
const adminList = ref([])

// 系统数据
const systemData = reactive({
  totalAdmins: 15,
  onlineUsers: 8,
  storageUsage: 75,
  uptime: '15天 8小时 32分钟'
})

// 表单验证规则
const adminRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }],
  roleId: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const settingsRules = {
  systemName: [{ required: true, message: '请输入系统名称', trigger: 'blur' }],
  uploadSizeLimit: [{ required: true, message: '请输入上传文件大小限制', trigger: 'blur' }],
  sessionTimeout: [{ required: true, message: '请输入会话超时时间', trigger: 'blur' }]
}

// 表单引用
const adminFormRef = ref()
const settingsFormRef = ref()

// 方法
const loadAdminList = async () => {
  loading.value = true
  try {
    // TODO: 调用真实API
    // const response = await api.getAdminList({
    //   username: searchForm.username,
    //   roleId: searchForm.roleId,
    //   status: searchForm.status,
    //   dateRange: searchForm.dateRange,
    //   page: pagination.current,
    //   size: pagination.size
    // })
    // adminList.value = response.data.records || []
    // pagination.total = response.data.total || 0
    
    adminList.value = []
    pagination.total = 0
  } catch (error) {
    ElMessage.error('加载管理员列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadAdminList()
}

const resetSearch = () => {
  Object.assign(searchForm, {
    username: '',
    roleId: '',
    status: '',
    dateRange: []
  })
  handleSearch()
}

const handleSizeChange = (size) => {
  pagination.size = size
  loadAdminList()
}

const handleCurrentChange = (current) => {
  pagination.current = current
  loadAdminList()
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(adminForm, {
    id: row.id,
    username: row.username,
    realName: row.realName,
    phone: row.phone,
    email: row.email,
    roleId: row.roleId,
    avatar: row.avatar,
    status: row.status
  })
  showAddAdminDialog.value = true
}

const handleToggleStatus = async (row) => {
  try {
    const action = row.status === 1 ? '禁用' : '启用'
    await ElMessageBox.confirm(`确定要${action}这个管理员吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // TODO: 调用真实API
    ElMessage.success(`${action}成功`)
    loadAdminList()
  } catch {
    // 用户取消操作
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这个管理员吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // TODO: 调用真实删除API
    ElMessage.success('删除成功')
    loadAdminList()
  } catch {
    // 用户取消删除
  }
}

const handleResetPassword = async (row) => {
  try {
    await ElMessageBox.confirm('确定要重置该管理员的密码吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // TODO: 调用真实重置密码API
    ElMessage.success('密码重置成功，新密码已发送到邮箱')
  } catch {
    // 用户取消操作
  }
}

const handleAdminSubmit = async () => {
  try {
    await adminFormRef.value.validate()
    
    // TODO: 调用真实提交API
    ElMessage.success(isEdit.value ? '编辑成功' : '添加成功')
    showAddAdminDialog.value = false
    loadAdminList()
    
    // 重置表单
    if (!isEdit.value) {
      Object.assign(adminForm, {
        id: null,
        username: '',
        realName: '',
        phone: '',
        email: '',
        roleId: 2,
        avatar: '',
        status: 1
      })
    }
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

const handleSaveSettings = async () => {
  try {
    await settingsFormRef.value.validate()
    
    // TODO: 调用真实保存设置API
    ElMessage.success('设置保存成功')
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

const handleResetSettings = () => {
  Object.assign(settingsForm, {
    systemName: '视频任务管理系统',
    systemVersion: 'v1.0.0',
    uploadSizeLimit: 100,
    sessionTimeout: 30,
    allowRegister: true,
    enableCaptcha: true,
    systemDescription: '专业的视频任务管理平台，支持多平台视频发布和达人管理。'
  })
  ElMessage.success('设置已重置')
}

const handleAvatarSuccess = (response) => {
  adminForm.avatar = response.url
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
  loadAdminList()
})
</script>

<style scoped>
.system-management {
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

.system-overview {
  margin-bottom: 30px;
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

.card-icon.admins {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.card-icon.online {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.card-icon.storage {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.card-icon.uptime {
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
}

.card-change.increase {
  color: #10b981;
}

.card-change.warning {
  color: #f59e0b;
}

.card-change.success {
  color: #10b981;
}

.admin-section {
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 30px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.section-header {
  margin-bottom: 20px;
}

.section-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.search-filters {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.admin-list {
  margin-top: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.admin-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.info-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.admin-name {
  font-weight: 500;
  font-size: 14px;
}

.admin-username {
  font-size: 12px;
  color: #666;
}

.admin-email {
  font-size: 12px;
  color: #999;
}

.system-settings {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.settings-form {
  max-width: 800px;
}

.unit {
  margin-left: 8px;
  color: #666;
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
</style>
