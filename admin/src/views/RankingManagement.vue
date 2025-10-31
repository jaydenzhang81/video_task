<template>
  <div class="ranking-management">
    <div class="page-header">
      <h2>榜单管理</h2>
      <div class="header-actions">
        <el-button type="success" @click="exportData">
          <el-icon><Download /></el-icon>
          导出数据
        </el-button>
        <el-button type="primary" @click="refreshRanking">
          <el-icon><Refresh /></el-icon>
          刷新榜单
        </el-button>
      </div>
    </div>

    <!-- 榜单筛选 -->
    <div class="ranking-filters">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-radio-group v-model="rankingType" @change="handleRankingTypeChange">
            <el-radio-button label="total">总榜单</el-radio-button>
            <el-radio-button label="monthly">月榜单</el-radio-button>
            <el-radio-button label="weekly">周榜单</el-radio-button>
          </el-radio-group>
        </el-col>
        <el-col :span="4">
          <el-select v-model="platformFilter" placeholder="平台筛选" clearable>
            <el-option label="全部平台" value="" />
            <el-option label="抖音" value="douyin" />
            <el-option label="快手" value="kuaishou" />
            <el-option label="小红书" value="xiaohongshu" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="teamFilter" placeholder="团队筛选" clearable>
            <el-option label="全部团队" value="" />
            <el-option
              v-for="team in teams"
              :key="team.id"
              :label="team.teamName"
              :value="team.id"
            />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            @change="handleDateChange"
          />
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-col>
      </el-row>
    </div>

    <!-- 榜单统计 -->
    <div class="ranking-stats">
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon total">
              <el-icon><Trophy /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-title">上榜达人</div>
              <div class="stat-value">{{ stats.totalInfluencers }}</div>
              <div class="stat-change increase">
                <el-icon><ArrowUp /></el-icon>
                +{{ stats.influencerGrowth }}%
              </div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon revenue">
              <el-icon><Money /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-title">总奖励金额</div>
              <div class="stat-value">¥{{ stats.totalReward.toLocaleString() }}</div>
              <div class="stat-change increase">
                <el-icon><ArrowUp /></el-icon>
                +{{ stats.rewardGrowth }}%
              </div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon videos">
              <el-icon><VideoCamera /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-title">发布视频</div>
              <div class="stat-value">{{ stats.totalVideos.toLocaleString() }}</div>
              <div class="stat-change increase">
                <el-icon><ArrowUp /></el-icon>
                +{{ stats.videoGrowth }}%
              </div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon engagement">
              <el-icon><Star /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-title">平均互动率</div>
              <div class="stat-value">{{ stats.avgEngagement }}%</div>
              <div class="stat-change increase">
                <el-icon><ArrowUp /></el-icon>
                +{{ stats.engagementGrowth }}%
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 榜单列表 -->
    <div class="ranking-list">
      <el-table :data="rankingList" style="width: 100%" v-loading="loading">
        <el-table-column prop="rank" label="排名" width="80">
          <template #default="scope">
            <div class="rank-badge" :class="getRankClass(scope.row.rank)">
              {{ scope.row.rank }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="达人信息" min-width="250">
          <template #default="scope">
            <div class="influencer-info">
              <el-avatar :size="50" :src="scope.row.avatar" />
              <div class="info-content">
                <div class="influencer-name">{{ scope.row.influencerName }}</div>
                <div class="team-name">{{ scope.row.teamName }}</div>
                <div class="platform-tags">
                  <el-tag 
                    v-for="platform in scope.row.platforms" 
                    :key="platform"
                    size="small"
                    :type="getPlatformTagType(platform)"
                  >
                    {{ getPlatformLabel(platform) }}
                  </el-tag>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="rewardAmount" label="奖励金额" width="120">
          <template #default="scope">
            <span class="reward-amount">¥{{ scope.row.rewardAmount.toLocaleString() }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="videosPosted" label="发布视频" width="120" />
        <el-table-column prop="totalLikes" label="总点赞" width="120">
          <template #default="scope">
            {{ scope.row.totalLikes.toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column prop="totalComments" label="总评论" width="120">
          <template #default="scope">
            {{ scope.row.totalComments.toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column prop="engagementRate" label="互动率" width="100">
          <template #default="scope">
            <span class="engagement-rate">{{ scope.row.engagementRate }}%</span>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="综合评分" width="120">
          <template #default="scope">
            <el-rate 
              v-model="scope.row.score" 
              disabled 
              show-score 
              text-color="#ff9900"
              score-template="{value}"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="handleViewDetail(scope.row)">详情</el-button>
            <el-button size="small" type="primary" @click="handleViewVideos(scope.row)">视频</el-button>
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
                <el-descriptions-item label="排名">{{ currentInfluencer.rank }}</el-descriptions-item>
                <el-descriptions-item label="达人昵称">{{ currentInfluencer.influencerName }}</el-descriptions-item>
                <el-descriptions-item label="所属团队">{{ currentInfluencer.teamName }}</el-descriptions-item>
                <el-descriptions-item label="奖励金额">¥{{ currentInfluencer.rewardAmount.toLocaleString() }}</el-descriptions-item>
                <el-descriptions-item label="发布视频">{{ currentInfluencer.videosPosted }}</el-descriptions-item>
                <el-descriptions-item label="综合评分">{{ currentInfluencer.score }}</el-descriptions-item>
              </el-descriptions>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="detail-section">
              <h3>数据统计</h3>
              <div class="stats-cards">
                <div class="stat-card">
                  <div class="stat-value">{{ currentInfluencer.totalLikes.toLocaleString() }}</div>
                  <div class="stat-label">总点赞</div>
                </div>
                <div class="stat-card">
                  <div class="stat-value">{{ currentInfluencer.totalComments.toLocaleString() }}</div>
                  <div class="stat-label">总评论</div>
                </div>
                <div class="stat-card">
                  <div class="stat-value">{{ currentInfluencer.engagementRate }}%</div>
                  <div class="stat-label">互动率</div>
                </div>
                <div class="stat-card">
                  <div class="stat-value">{{ currentInfluencer.totalViews.toLocaleString() }}</div>
                  <div class="stat-label">总观看</div>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
        
        <div class="detail-section">
          <h3>平台表现</h3>
          <el-table :data="platformPerformance" style="width: 100%">
            <el-table-column prop="platform" label="平台" />
            <el-table-column prop="videos" label="视频数" />
            <el-table-column prop="likes" label="点赞数" />
            <el-table-column prop="comments" label="评论数" />
            <el-table-column prop="views" label="观看数" />
            <el-table-column prop="engagement" label="互动率" />
          </el-table>
        </div>
      </div>
    </el-dialog>

    <!-- 视频列表对话框 -->
    <el-dialog
      v-model="showVideosDialog"
      title="达人视频"
      width="1000px"
    >
      <div v-if="currentInfluencer" class="videos-list">
        <el-table :data="videosList" style="width: 100%">
          <el-table-column label="视频" min-width="200">
            <template #default="scope">
              <div class="video-info">
                <el-image
                  :src="scope.row.cover"
                  :preview-src-list="[scope.row.cover]"
                  fit="cover"
                  style="width: 80px; height: 60px; border-radius: 4px; margin-right: 12px;"
                />
                <div>
                  <div class="video-title">{{ scope.row.title }}</div>
                  <div class="video-desc">{{ scope.row.desc }}</div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="platform" label="平台" width="100" />
          <el-table-column prop="likeCount" label="点赞" width="100" />
          <el-table-column prop="commentCount" label="评论" width="100" />
          <el-table-column prop="viewCount" label="观看" width="100" />
          <el-table-column prop="ctime" label="发布时间" width="180" />
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Download, 
  Refresh, 
  Trophy, 
  Money, 
  VideoCamera, 
  Star, 
  ArrowUp 
} from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const showDetailDialog = ref(false)
const showVideosDialog = ref(false)
const currentInfluencer = ref(null)

// 筛选条件
const rankingType = ref('total')
const platformFilter = ref('')
const teamFilter = ref('')
const dateRange = ref([])

// 分页
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 数据列表
const rankingList = ref([])
const teams = ref([])
const platformPerformance = ref([])
const videosList = ref([])

// 统计数据
const stats = reactive({
  totalInfluencers: 1542,
  influencerGrowth: 12.5,
  totalReward: 1250000,
  rewardGrowth: 8.3,
  totalVideos: 15420,
  videoGrowth: 22.1,
  avgEngagement: 15.7,
  engagementGrowth: 5.2
})

// 方法
const loadRankingList = async () => {
  loading.value = true
  try {
    // TODO: 调用真实API
    // const response = await api.getRankingList({
    //   rankingType: rankingType.value,
    //   platformFilter: platformFilter.value,
    //   teamFilter: teamFilter.value,
    //   dateRange: dateRange.value,
    //   page: pagination.current,
    //   size: pagination.size
    // })
    // rankingList.value = response.data.records || []
    // pagination.total = response.data.total || 0
    
    rankingList.value = []
    pagination.total = 0
  } catch (error) {
    ElMessage.error('加载榜单失败')
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

const handleRankingTypeChange = () => {
  loadRankingList()
}

const handleDateChange = () => {
  loadRankingList()
}

const handleSearch = () => {
  pagination.current = 1
  loadRankingList()
}

const resetSearch = () => {
  rankingType.value = 'total'
  platformFilter.value = ''
  teamFilter.value = ''
  dateRange.value = []
  handleSearch()
}

const handleSizeChange = (size) => {
  pagination.size = size
  loadRankingList()
}

const handleCurrentChange = (current) => {
  pagination.current = current
  loadRankingList()
}

const handleViewDetail = async (row) => {
  currentInfluencer.value = row
  showDetailDialog.value = true
  
  // 加载平台表现数据
  platformPerformance.value = [
    {
      platform: '抖音',
      videos: 300,
      likes: 80000,
      comments: 5000,
      views: 1500000,
      engagement: '20.5%'
    },
    {
      platform: '快手',
      videos: 255,
      likes: 45000,
      comments: 3500,
      views: 1000000,
      engagement: '16.8%'
    }
  ]
}

const handleViewVideos = async (row) => {
  currentInfluencer.value = row
  showVideosDialog.value = true
  
  // 加载视频列表
  videosList.value = [
    {
      title: '快!看!美不美~',
      desc: '这是一个美丽的视频',
      cover: 'https://via.placeholder.com/300x200',
      platform: '抖音',
      likeCount: 1500,
      commentCount: 250,
      viewCount: 12000,
      ctime: '2024-01-15 10:30:00'
    },
    {
      title: '掉进樱花的世界',
      desc: '樱花盛开的美景',
      cover: 'https://via.placeholder.com/300x200',
      platform: '快手',
      likeCount: 1200,
      commentCount: 180,
      viewCount: 9800,
      ctime: '2024-01-14 15:20:00'
    }
  ]
}

const refreshRanking = () => {
  ElMessage.success('榜单刷新成功')
  loadRankingList()
}

const exportData = () => {
  ElMessage.success('数据导出成功')
}

// 工具方法
const getRankClass = (rank) => {
  if (rank === 1) return 'rank-gold'
  if (rank === 2) return 'rank-silver'
  if (rank === 3) return 'rank-bronze'
  return 'rank-normal'
}

const getPlatformTagType = (platform) => {
  const types = {
    douyin: 'danger',
    kuaishou: 'warning',
    xiaohongshu: 'success'
  }
  return types[platform] || 'info'
}

const getPlatformLabel = (platform) => {
  const labels = {
    douyin: '抖音',
    kuaishou: '快手',
    xiaohongshu: '小红书'
  }
  return labels[platform] || platform
}

// 生命周期
onMounted(() => {
  loadRankingList()
  loadTeams()
})
</script>

<style scoped>
.ranking-management {
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

.ranking-filters {
  background: white;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.ranking-stats {
  margin-bottom: 20px;
}

.stat-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}

.stat-icon.total {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-icon.revenue {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-icon.videos {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-icon.engagement {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-content {
  flex: 1;
}

.stat-title {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.stat-change {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #10b981;
}

.ranking-list {
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

.rank-badge {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  color: white;
}

.rank-gold {
  background: linear-gradient(135deg, #ffd700 0%, #ffed4e 100%);
}

.rank-silver {
  background: linear-gradient(135deg, #c0c0c0 0%, #e5e5e5 100%);
  color: #333;
}

.rank-bronze {
  background: linear-gradient(135deg, #cd7f32 0%, #daa520 100%);
}

.rank-normal {
  background: #f5f5f5;
  color: #666;
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

.team-name {
  font-size: 12px;
  color: #666;
}

.platform-tags {
  display: flex;
  gap: 4px;
}

.reward-amount {
  color: #f56c6c;
  font-weight: 600;
}

.engagement-rate {
  color: #10b981;
  font-weight: 600;
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

.videos-list {
  padding: 20px 0;
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
</style>
