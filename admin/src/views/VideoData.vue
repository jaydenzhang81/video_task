<template>
  <div class="video-data">
    <!-- 数据统计面板 -->
    <div class="stats-panel">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon total">
                <el-icon><VideoPlay /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ stats.totalVideos }}</div>
                <div class="stat-label">视频总数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon published">
                <el-icon><Check /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ stats.publishedVideos }}</div>
                <div class="stat-label">已发布</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon draft">
                <el-icon><Clock /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ stats.draftVideos }}</div>
                <div class="stat-label">草稿</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon today">
                <el-icon><Calendar /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ stats.todayVideos }}</div>
                <div class="stat-label">今日新增</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 图表展示 -->
    <div class="charts-section">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>视频分类分布</span>
            </template>
            <div class="chart-container">
              <v-chart :option="categoryChartOption" style="height: 300px;" />
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>视频发布趋势</span>
            </template>
            <div class="chart-container">
              <v-chart :option="trendChartOption" style="height: 300px;" />
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 搜索和筛选 -->
    <el-card class="search-card">
      <template #header>
        <div class="search-header">
          <span>视频数据管理</span>
          <div class="search-actions">
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>
              添加视频
            </el-button>
            <el-button @click="handleExport">
              <el-icon><Download /></el-icon>
              导出数据
            </el-button>
            <el-button @click="handleRefresh">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <!-- 高级搜索 -->
      <div class="advanced-search">
        <el-form :model="searchForm" inline>
          <el-form-item label="视频标题">
            <el-input
              v-model="searchForm.title"
              placeholder="请输入视频标题"
              clearable
              style="width: 200px;"
            />
          </el-form-item>
          <el-form-item label="视频类型">
            <el-select v-model="searchForm.videoType" placeholder="选择类型" clearable style="width: 150px;">
              <el-option
                v-for="type in videoTypes"
                :key="type.id"
                :label="type.name"
                :value="type.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="发布状态">
            <el-select v-model="searchForm.status" placeholder="选择状态" clearable style="width: 120px;">
              <el-option label="未发布" :value="0" />
              <el-option label="已发布" :value="1" />
            </el-select>
          </el-form-item>
          <el-form-item label="创建时间">
            <el-date-picker
              v-model="searchForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              style="width: 240px;"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 批量操作 -->
      <div class="batch-actions" v-if="selectedVideos.length > 0">
        <el-alert
          :title="`已选择 ${selectedVideos.length} 个视频`"
          type="info"
          show-icon
          :closable="false"
        >
          <template #default>
            <el-button size="small" type="success" @click="handleBatchPublish">批量发布</el-button>
            <el-button size="small" type="warning" @click="handleBatchUnpublish">批量下架</el-button>
            <el-button size="small" type="danger" @click="handleBatchDelete">批量删除</el-button>
          </template>
        </el-alert>
      </div>

      <!-- 视频列表 -->
      <el-table
        :data="videoList"
        style="width: 100%"
        v-loading="loading"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
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
        <el-table-column prop="title" label="视频标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="videoTypeName" label="类型" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'warning'">
              {{ scope.row.status === 1 ? '已发布' : '未发布' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="播放量" width="100" sortable />
        <el-table-column prop="likeCount" label="点赞数" width="100" sortable />
        <el-table-column prop="commentCount" label="评论数" width="100" sortable />
        <el-table-column prop="ctime" label="创建时间" width="180" sortable />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="handleView(scope.row)">查看</el-button>
            <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="primary" @click="handleTask(scope.row)">任务</el-button>
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
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :before-close="handleDialogClose"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="视频标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入视频标题" />
        </el-form-item>
        <el-form-item label="视频描述" prop="videoDesc">
          <el-input
            v-model="form.videoDesc"
            type="textarea"
            placeholder="请输入视频描述"
            :rows="3"
          />
        </el-form-item>
        <el-form-item label="视频类型" prop="videoType">
          <el-select v-model="form.videoType" placeholder="请选择视频类型" style="width: 100%;">
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
        <el-form-item label="封面地址" prop="cover">
          <el-input v-model="form.cover" placeholder="请输入封面地址" />
        </el-form-item>
        <el-form-item label="发布状态" prop="status">
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

    <!-- 视频详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="视频详情"
      width="800px"
    >
      <div class="video-detail" v-if="currentVideo">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-image
              :src="currentVideo.cover"
              fit="cover"
              style="width: 100%; height: 200px; border-radius: 8px;"
            />
          </el-col>
          <el-col :span="16">
            <h3>{{ currentVideo.title }}</h3>
            <p class="video-desc">{{ currentVideo.videoDesc }}</p>
            <div class="video-stats">
              <el-row :gutter="20">
                <el-col :span="6">
                  <div class="stat-item">
                    <div class="stat-value">{{ currentVideo.viewCount || 0 }}</div>
                    <div class="stat-label">播放量</div>
                  </div>
                </el-col>
                <el-col :span="6">
                  <div class="stat-item">
                    <div class="stat-value">{{ currentVideo.likeCount || 0 }}</div>
                    <div class="stat-label">点赞数</div>
                  </div>
                </el-col>
                <el-col :span="6">
                  <div class="stat-item">
                    <div class="stat-value">{{ currentVideo.commentCount || 0 }}</div>
                    <div class="stat-label">评论数</div>
                  </div>
                </el-col>
                <el-col :span="6">
                  <div class="stat-item">
                    <div class="stat-value">{{ currentVideo.shareCount || 0 }}</div>
                    <div class="stat-label">分享数</div>
                  </div>
                </el-col>
              </el-row>
            </div>
            <div class="video-info">
              <p><strong>类型：</strong>{{ currentVideo.videoTypeName }}</p>
              <p><strong>状态：</strong>
                <el-tag :type="currentVideo.status === 1 ? 'success' : 'warning'">
                  {{ currentVideo.status === 1 ? '已发布' : '未发布' }}
                </el-tag>
              </p>
              <p><strong>创建时间：</strong>{{ formatDate(currentVideo.ctime) }}</p>
              <p><strong>更新时间：</strong>{{ formatDate(currentVideo.utime) }}</p>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart, LineChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import api from '@/utils/api'

use([
  CanvasRenderer,
  PieChart,
  LineChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
])

export default {
  name: 'VideoData',
  components: {
    VChart
  },
  setup() {
    // 响应式数据
    const loading = ref(false)
    const videoList = ref([])
    const videoTypes = ref([])
    const selectedVideos = ref([])
    const dialogVisible = ref(false)
    const detailDialogVisible = ref(false)
    const currentVideo = ref(null)
    const isEdit = ref(false)

    // 统计数据
    const stats = reactive({
      totalVideos: 0,
      publishedVideos: 0,
      draftVideos: 0,
      todayVideos: 0
    })

    // 搜索表单
    const searchForm = reactive({
      title: '',
      videoType: '',
      status: '',
      dateRange: []
    })

    // 分页
    const pagination = reactive({
      current: 1,
      size: 20,
      total: 0
    })

    // 表单数据
    const form = reactive({
      id: null,
      title: '',
      videoDesc: '',
      videoType: '',
      url: '',
      cover: '',
      status: 0
    })

    // 表单验证规则
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

    // 计算属性
    const dialogTitle = computed(() => isEdit.value ? '编辑视频' : '添加视频')

    // 图表配置
    const categoryChartOption = ref({
      title: {
        text: '视频分类分布',
        left: 'center'
      },
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        left: 'left'
      },
      series: [
        {
          name: '视频分类',
          type: 'pie',
          radius: '50%',
          data: []
        }
      ]
    })

    const trendChartOption = ref({
      title: {
        text: '视频发布趋势',
        left: 'center'
      },
      tooltip: {
        trigger: 'axis'
      },
      xAxis: {
        type: 'category',
        data: []
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          name: '发布数量',
          type: 'line',
          data: []
        }
      ]
    })

    // 方法
    const loadData = async () => {
      loading.value = true
      try {
        // 加载视频列表
        const params = {
          page: pagination.current,
          size: pagination.size,
          ...searchForm
        }
        
        if (searchForm.dateRange && searchForm.dateRange.length === 2) {
          params.startDate = searchForm.dateRange[0]
          params.endDate = searchForm.dateRange[1]
        }

        const res = await api.get('/video/list', params)
        if (res.code === 0) {
          videoList.value = res.data.records || res.data || []
          pagination.total = res.data.total || videoList.value.length
        }

        // 加载统计数据
        await loadStats()
        
        // 加载图表数据
        await loadChartData()
        
      } catch (error) {
        console.error('加载数据失败:', error)
        ElMessage.error('加载数据失败')
      } finally {
        loading.value = false
      }
    }

    const loadStats = async () => {
      try {
        const res = await api.get('/video/stats')
        if (res.code === 0) {
          Object.assign(stats, res.data)
        }
      } catch (error) {
        console.error('加载统计数据失败:', error)
      }
    }

    const loadChartData = async () => {
      try {
        // 加载分类分布数据
        const categoryRes = await api.get('/video/category-stats')
        if (categoryRes.code === 0) {
          categoryChartOption.value.series[0].data = categoryRes.data.map(item => ({
            name: item.name,
            value: item.count
          }))
        }

        // 加载趋势数据
        const trendRes = await api.get('/video/trend-stats')
        if (trendRes.code === 0) {
          trendChartOption.value.xAxis.data = trendRes.data.dates
          trendChartOption.value.series[0].data = trendRes.data.counts
        }
      } catch (error) {
        console.error('加载图表数据失败:', error)
      }
    }

    const loadVideoTypes = async () => {
      try {
        const res = await api.get('/video/types')
        if (res.code === 0) {
          videoTypes.value = res.data
        }
      } catch (error) {
        console.error('加载视频类型失败:', error)
      }
    }

    const handleSearch = () => {
      pagination.current = 1
      loadData()
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
      loadData()
    }

    const handleCurrentChange = (current) => {
      pagination.current = current
      loadData()
    }

    const handleSelectionChange = (selection) => {
      selectedVideos.value = selection
    }

    const handleAdd = () => {
      isEdit.value = false
      Object.assign(form, {
        id: null,
        title: '',
        videoDesc: '',
        videoType: '',
        url: '',
        cover: '',
        status: 0
      })
      dialogVisible.value = true
    }

    const handleEdit = (row) => {
      isEdit.value = true
      Object.assign(form, row)
      dialogVisible.value = true
    }

    const handleView = (row) => {
      currentVideo.value = row
      detailDialogVisible.value = true
    }

    const handleDelete = async (row) => {
      try {
        await ElMessageBox.confirm('确定要删除这个视频吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        const res = await api.delete(`/video/${row.id}`)
        if (res.code === 0) {
          ElMessage.success('删除成功')
          loadData()
        } else {
          ElMessage.error(res.message || '删除失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除失败:', error)
          ElMessage.error('删除失败')
        }
      }
    }

    const handleBatchPublish = async () => {
      try {
        await ElMessageBox.confirm(`确定要批量发布选中的 ${selectedVideos.value.length} 个视频吗？`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        const videoIds = selectedVideos.value.map(video => video.id)
        const res = await api.post('/video/batch-publish', { videoIds })
        if (res.code === 0) {
          ElMessage.success('批量发布成功')
          loadData()
        } else {
          ElMessage.error(res.message || '批量发布失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('批量发布失败:', error)
          ElMessage.error('批量发布失败')
        }
      }
    }

         const handleBatchDelete = async () => {
       try {
         await ElMessageBox.confirm(`确定要批量删除选中的 ${selectedVideos.value.length} 个视频吗？`, '提示', {
           confirmButtonText: '确定',
           cancelButtonText: '取消',
           type: 'warning'
         })
         
         const videoIds = selectedVideos.value.map(video => video.id)
         const res = await api.post('/video/batch-delete', { videoIds })
         if (res.code === 0) {
           ElMessage.success('批量删除成功')
           loadData()
         } else {
           ElMessage.error(res.message || '批量删除失败')
         }
       } catch (error) {
         if (error !== 'cancel') {
           console.error('批量删除失败:', error)
           ElMessage.error('批量删除失败')
         }
       }
     },

     const handleBatchUnpublish = async () => {
       try {
         await ElMessageBox.confirm(`确定要批量下架选中的 ${selectedVideos.value.length} 个视频吗？`, '提示', {
           confirmButtonText: '确定',
           cancelButtonText: '取消',
           type: 'warning'
         })
         
         const videoIds = selectedVideos.value.map(video => video.id)
         const res = await api.post('/video/batch-unpublish', { videoIds })
         if (res.code === 0) {
           ElMessage.success('批量下架成功')
           loadData()
         } else {
           ElMessage.error(res.message || '批量下架失败')
         }
       } catch (error) {
         if (error !== 'cancel') {
           console.error('批量下架失败:', error)
           ElMessage.error('批量下架失败')
         }
       }
     },

     const handleTask = (row) => {
       ElMessage.info('任务管理功能开发中...')
     }

    const handleSubmit = async () => {
      try {
        const res = isEdit.value 
          ? await api.put(`/video/${form.id}`, form)
          : await api.post('/video', form)
        
        if (res.code === 0) {
          ElMessage.success(isEdit.value ? '更新成功' : '添加成功')
          dialogVisible.value = false
          loadData()
        } else {
          ElMessage.error(res.message || (isEdit.value ? '更新失败' : '添加失败'))
        }
      } catch (error) {
        console.error('提交失败:', error)
        ElMessage.error(isEdit.value ? '更新失败' : '添加失败')
      }
    }

    const handleExport = () => {
      ElMessage.info('导出功能开发中...')
    }

    const handleRefresh = () => {
      loadData()
    }

    const handleDialogClose = () => {
      dialogVisible.value = false
    }

    const formatDate = (date) => {
      if (!date) return ''
      return new Date(date).toLocaleString()
    }

    // 生命周期
    onMounted(() => {
      loadVideoTypes()
      loadData()
    })

    return {
      loading,
      videoList,
      videoTypes,
      selectedVideos,
      stats,
      searchForm,
      pagination,
      dialogVisible,
      detailDialogVisible,
      currentVideo,
      form,
      rules,
      dialogTitle,
      categoryChartOption,
      trendChartOption,
      handleSearch,
      resetSearch,
      handleSizeChange,
      handleCurrentChange,
      handleSelectionChange,
      handleAdd,
      handleEdit,
      handleView,
      handleDelete,
             handleBatchPublish,
       handleBatchDelete,
       handleBatchUnpublish,
       handleTask,
      handleSubmit,
      handleExport,
      handleRefresh,
      handleDialogClose,
      formatDate
    }
  }
}
</script>

<style scoped>
.video-data {
  padding: 20px;
}

.stats-panel {
  margin-bottom: 20px;
}

.stat-card {
  border: none;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  padding: 20px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
  font-size: 24px;
  color: white;
}

.stat-icon.total {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-icon.published {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-icon.draft {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-icon.today {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.charts-section {
  margin-bottom: 20px;
}

.chart-container {
  height: 300px;
}

.search-card {
  margin-bottom: 20px;
}

.search-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-actions {
  display: flex;
  gap: 10px;
}

.advanced-search {
  margin-bottom: 20px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
}

.batch-actions {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

.video-detail {
  padding: 20px;
}

.video-detail h3 {
  margin: 0 0 10px 0;
  color: #303133;
}

.video-desc {
  color: #606266;
  margin-bottom: 20px;
  line-height: 1.6;
}

.video-stats {
  margin-bottom: 20px;
}

.stat-item {
  text-align: center;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 8px;
}

.stat-item .stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 5px;
}

.stat-item .stat-label {
  font-size: 12px;
  color: #909399;
}

.video-info p {
  margin: 10px 0;
  color: #606266;
}

.video-info strong {
  color: #303133;
}
</style>
