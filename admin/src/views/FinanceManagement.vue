<template>
  <div class="finance-management">
    <div class="page-header">
      <h2>财务管理</h2>
      <div class="header-actions">
        <el-button type="success" @click="exportData">
          <el-icon><Download /></el-icon>
          导出数据
        </el-button>
        <el-button type="primary" @click="showWithdrawDialog = true">
          <el-icon><Money /></el-icon>
          批量提现
        </el-button>
      </div>
    </div>

    <!-- 财务概览 -->
    <div class="finance-overview">
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="overview-card">
            <div class="card-icon income">
              <el-icon><TrendCharts /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-title">总收入</div>
              <div class="card-value">¥{{ financeData.totalIncome.toLocaleString() }}</div>
              <div class="card-change increase">
                <el-icon><ArrowUp /></el-icon>
                +12.5%
              </div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="overview-card">
            <div class="card-icon withdraw">
              <el-icon><Money /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-title">已提现</div>
              <div class="card-value">¥{{ financeData.totalWithdrawn.toLocaleString() }}</div>
              <div class="card-change increase">
                <el-icon><ArrowUp /></el-icon>
                +8.3%
              </div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="overview-card">
            <div class="card-icon pending">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-title">待提现</div>
              <div class="card-value">¥{{ financeData.pendingWithdraw.toLocaleString() }}</div>
              <div class="card-change decrease">
                <el-icon><ArrowDown /></el-icon>
                -5.2%
              </div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="overview-card">
            <div class="card-icon users">
              <el-icon><User /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-title">活跃用户</div>
              <div class="card-value">{{ financeData.activeUsers.toLocaleString() }}</div>
              <div class="card-change increase">
                <el-icon><ArrowUp /></el-icon>
                +15.7%
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
            v-model="searchForm.userName"
            placeholder="请输入用户名"
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :span="4">
          <el-select v-model="searchForm.type" placeholder="类型" clearable>
            <el-option label="收入" value="income" />
            <el-option label="提现" value="withdraw" />
            <el-option label="奖励" value="reward" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="searchForm.status" placeholder="状态" clearable>
            <el-option label="成功" value="success" />
            <el-option label="处理中" value="processing" />
            <el-option label="失败" value="failed" />
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

    <!-- 财务记录列表 -->
    <div class="finance-list">
      <el-table :data="financeList" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="userName" label="用户名" width="120" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="scope">
            <el-tag :type="getTypeTagType(scope.row.type)">
              {{ getTypeLabel(scope.row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="120">
          <template #default="scope">
            <span :class="getAmountClass(scope.row.type)">
              {{ getAmountPrefix(scope.row.type) }}¥{{ scope.row.amount.toLocaleString() }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ getStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ctime" label="时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="handleViewDetail(scope.row)">详情</el-button>
            <el-button 
              v-if="scope.row.type === 'withdraw' && scope.row.status === 'processing'"
              size="small" 
              type="success" 
              @click="handleApprove(scope.row)"
            >
              审核
            </el-button>
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

    <!-- 批量提现对话框 -->
    <el-dialog
      v-model="showWithdrawDialog"
      title="批量提现"
      width="600px"
    >
      <el-form
        ref="withdrawFormRef"
        :model="withdrawForm"
        :rules="withdrawRules"
        label-width="120px"
      >
        <el-form-item label="提现用户">
          <el-table
            :data="withdrawUsers"
            style="width: 100%"
            max-height="300"
            @selection-change="handleSelectionChange"
          >
            <el-table-column type="selection" width="55" />
            <el-table-column prop="userName" label="用户名" />
            <el-table-column prop="pendingAmount" label="待提现金额">
              <template #default="scope">
                ¥{{ scope.row.pendingAmount.toLocaleString() }}
              </template>
            </el-table-column>
          </el-table>
        </el-form-item>
        
        <el-form-item label="提现金额" prop="amount">
          <el-input-number
            v-model="withdrawForm.amount"
            :min="0"
            :precision="2"
            :step="0.01"
            style="width: 200px;"
          />
          <span class="unit">元</span>
        </el-form-item>
        
        <el-form-item label="提现方式" prop="method">
          <el-radio-group v-model="withdrawForm.method">
            <el-radio label="alipay">支付宝</el-radio>
            <el-radio label="wechat">微信</el-radio>
            <el-radio label="bank">银行卡</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="withdrawForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showWithdrawDialog = false">取消</el-button>
          <el-button type="primary" @click="handleWithdrawSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      title="财务详情"
      width="600px"
    >
      <div v-if="currentRecord" class="record-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="记录ID">{{ currentRecord.id }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ currentRecord.userName }}</el-descriptions-item>
          <el-descriptions-item label="类型">
            <el-tag :type="getTypeTagType(currentRecord.type)">
              {{ getTypeLabel(currentRecord.type) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="金额">
            <span :class="getAmountClass(currentRecord.type)">
              {{ getAmountPrefix(currentRecord.type) }}¥{{ currentRecord.amount.toLocaleString() }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="描述">{{ currentRecord.description }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusTagType(currentRecord.status)">
              {{ getStatusLabel(currentRecord.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentRecord.ctime }}</el-descriptions-item>
          <el-descriptions-item v-if="currentRecord.utime" label="更新时间">{{ currentRecord.utime }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Search, 
  Download, 
  Money, 
  TrendCharts, 
  Clock, 
  User, 
  ArrowUp, 
  ArrowDown 
} from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const showWithdrawDialog = ref(false)
const showDetailDialog = ref(false)
const currentRecord = ref(null)

// 搜索表单
const searchForm = reactive({
  userName: '',
  type: '',
  status: '',
  dateRange: []
})

// 提现表单
const withdrawForm = reactive({
  amount: 0,
  method: 'alipay',
  remark: ''
})

// 分页
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 数据列表
const financeList = ref([])
const withdrawUsers = ref([])
const selectedUsers = ref([])

// 财务概览数据
const financeData = reactive({
  totalIncome: 1250000,
  totalWithdrawn: 890000,
  pendingWithdraw: 360000,
  activeUsers: 15420
})

// 表单验证规则
const withdrawRules = {
  amount: [{ required: true, message: '请输入提现金额', trigger: 'blur' }],
  method: [{ required: true, message: '请选择提现方式', trigger: 'change' }]
}

// 表单引用
const withdrawFormRef = ref()

// 方法
const loadFinanceList = async () => {
  loading.value = true
  try {
    // TODO: 调用真实API
    // const response = await api.getFinanceList({
    //   userName: searchForm.userName,
    //   type: searchForm.type,
    //   status: searchForm.status,
    //   dateRange: searchForm.dateRange,
    //   page: pagination.current,
    //   size: pagination.size
    // })
    // financeList.value = response.data.records || []
    // pagination.total = response.data.total || 0
    
    financeList.value = []
    pagination.total = 0
  } catch (error) {
    ElMessage.error('加载财务记录失败')
  } finally {
    loading.value = false
  }
}

const loadWithdrawUsers = async () => {
  try {
    // TODO: 调用真实API获取待提现用户列表
    // const response = await api.getWithdrawUsers()
    // withdrawUsers.value = response.data || []
    withdrawUsers.value = []
  } catch (error) {
    console.error('加载待提现用户失败:', error)
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadFinanceList()
}

const resetSearch = () => {
  Object.assign(searchForm, {
    userName: '',
    type: '',
    status: '',
    dateRange: []
  })
  handleSearch()
}

const handleSizeChange = (size) => {
  pagination.size = size
  loadFinanceList()
}

const handleCurrentChange = (current) => {
  pagination.current = current
  loadFinanceList()
}

const handleViewDetail = (row) => {
  currentRecord.value = row
  showDetailDialog.value = true
}

const handleApprove = async (row) => {
  try {
    await ElMessageBox.confirm('确定要审核通过这个提现申请吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // TODO: 调用真实API
    // await api.approveWithdraw(row.id)
    ElMessage.success('审核成功')
    loadFinanceList()
  } catch {
    // 用户取消操作
  }
}

const handleSelectionChange = (selection) => {
  selectedUsers.value = selection
  const totalAmount = selection.reduce((sum, user) => sum + user.pendingAmount, 0)
  withdrawForm.amount = totalAmount
}

const handleWithdrawSubmit = async () => {
  try {
    await withdrawFormRef.value.validate()
    
    if (selectedUsers.value.length === 0) {
      ElMessage.warning('请选择要提现的用户')
      return
    }
    
    // TODO: 调用真实API
    ElMessage.success('批量提现申请已提交')
    showWithdrawDialog.value = false
    loadFinanceList()
    
    // 重置表单
    Object.assign(withdrawForm, {
      amount: 0,
      method: 'alipay',
      remark: ''
    })
    selectedUsers.value = []
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

const exportData = () => {
  ElMessage.success('数据导出成功')
}

// 工具方法
const getTypeTagType = (type) => {
  const types = {
    income: 'success',
    withdraw: 'warning',
    reward: 'info'
  }
  return types[type] || 'info'
}

const getTypeLabel = (type) => {
  const labels = {
    income: '收入',
    withdraw: '提现',
    reward: '奖励'
  }
  return labels[type] || type
}

const getStatusTagType = (status) => {
  const types = {
    success: 'success',
    processing: 'warning',
    failed: 'danger'
  }
  return types[status] || 'info'
}

const getStatusLabel = (status) => {
  const labels = {
    success: '成功',
    processing: '处理中',
    failed: '失败'
  }
  return labels[status] || status
}

const getAmountClass = (type) => {
  return type === 'withdraw' ? 'amount-negative' : 'amount-positive'
}

const getAmountPrefix = (type) => {
  return type === 'withdraw' ? '-' : '+'
}

// 生命周期
onMounted(() => {
  loadFinanceList()
  loadWithdrawUsers()
})
</script>

<style scoped>
.finance-management {
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

.finance-overview {
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

.card-icon.income {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.card-icon.withdraw {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.card-icon.pending {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.card-icon.users {
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

.card-change.decrease {
  color: #ef4444;
}

.search-filters {
  background: white;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.finance-list {
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

.unit {
  margin-left: 8px;
  color: #666;
}

.amount-positive {
  color: #10b981;
  font-weight: 600;
}

.amount-negative {
  color: #ef4444;
  font-weight: 600;
}

.record-detail {
  padding: 20px 0;
}
</style>
