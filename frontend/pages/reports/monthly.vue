<template>
  <div class="flex-1 p-6">
    <!-- Page Header -->
    <div class="mb-6">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-app-primary">Monthly Overview</h1>
          <p class="text-app-secondary mt-1">
            View your monthly time tracking overview and productivity insights
          </p>
        </div>

        <!-- Month Navigation -->
        <div class="flex items-center space-x-3">
          <Button
              icon="pi pi-chevron-left"
              size="small"
              severity="secondary"
              @click="previousMonth"
              v-tooltip="'Previous Month'"
          />


          <DatePicker
              v-model="selectedDate"
              @date-select="onDateChange"
              view="month"
              dateFormat="MM yy"
              showIcon
              placeholder="Select Month/Year"
              class="w-48"
              v-tooltip="'Select Month and Year'"
          />

          <Button
              icon="pi pi-chevron-right"
              size="small"
              severity="secondary"
              @click="nextMonth"
              v-tooltip="'Next Month'"
          />

          <Button
              label="This Month"
              severity="secondary"
              size="small"
              @click="goToCurrentMonth"
          />

          <Button
              icon="pi pi-refresh"
              size="small"
              severity="secondary"
              @click="forceRefresh"
              v-tooltip="'Refresh Data'"
              :loading="loading"
          />
        </div>
      </div>

      <!-- Month Display -->
      <div class="mt-2">
        <div class="text-lg font-medium text-app-primary">
          {{ currentMonthName }}
        </div>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="flex items-center justify-center py-12">
      <ProgressSpinner />
      <span class="ml-3 text-app-secondary">Loading monthly overview...</span>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="bg-app-red-50 border border-app-red-200 rounded-lg p-6 text-center">
      <i class="pi pi-exclamation-triangle text-app-red-600 text-2xl mb-2"></i>
      <h3 class="text-lg font-semibold text-app-red-800 mb-2">Failed to Load Monthly Overview</h3>
      <p class="text-app-red-600 mb-4">{{ error }}</p>
      <Button
          label="Retry"
          icon="pi pi-refresh"
          @click="fetchMonthlyData"
          severity="danger"
          outlined
      />
    </div>

    <!-- Monthly Content -->
    <div v-else-if="monthlyData" class="space-y-6">
      <!-- Monthly Overview Cards -->
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <Card class="text-center">
          <template #content>
            <div class="text-2xl font-bold text-app-blue-600 mb-2">
              {{ formatTime(monthlyData.statistics?.totalMinutes || 0) }}
            </div>
            <div class="text-sm text-app-secondary">Total Time</div>
          </template>
        </Card>

        <Card class="text-center">
          <template #content>
            <div class="text-2xl font-bold text-app-green-600 mb-2">
              {{ monthlyData.statistics?.daysTracked || 0 }}
            </div>
            <div class="text-sm text-app-secondary">Active Days</div>
          </template>
        </Card>

        <Card class="text-center">
          <template #content>
            <div class="text-2xl font-bold text-app-purple-600 mb-2">
              {{ monthlyData.statistics?.averageDailyHours || '0.0' }}h
            </div>
            <div class="text-sm text-app-secondary">Daily Average</div>
          </template>
        </Card>

        <Card class="text-center">
          <template #content>
            <div class="text-2xl font-bold text-app-orange-600 mb-2">
              {{ monthlyData.statistics?.totalEntries || 0 }}
            </div>
            <div class="text-sm text-app-secondary">Total Entries</div>
          </template>
        </Card>
      </div>

      <!-- Charts Section -->
      <div v-if="monthlyData" class="grid grid-cols-1 lg:grid-cols-1 gap-6 mb-6">
        <!-- Category Distribution Pie Chart -->
        <Card v-if="monthlyData.statistics?.categoryBreakdown?.length">
          <template #title>
            <div class="flex items-center">
              <i class="pi pi-chart-pie text-app-blue-600 mr-2"></i>
              Time by Category
            </div>
          </template>
          <template #content>
            <div class="relative">
              <canvas
                  ref="categoryChart"
                  class="w-full"
                  style="max-height: 300px;"
              ></canvas>
              <div v-if="!monthlyData.statistics.categoryBreakdown?.length"
                   class="absolute inset-0 flex items-center justify-center bg-gray-50 rounded">
                <p class="text-gray-500">No category data available</p>
              </div>
            </div>
          </template>
        </Card>
      </div>

      <!-- Productivity Insights -->
      <Card v-if="monthlyData.insights && Object.keys(monthlyData.insights).length > 0">
        <template #title>
          <div class="flex items-center">
            <i class="pi pi-chart-line text-app-blue-600 mr-2"></i>
            Productivity Insights
          </div>
        </template>
        <template #content>
          <div class="space-y-4">
            <!-- Most Productive Day -->
            <div v-if="monthlyData.insights.mostProductiveDay" class="bg-app-green-50 border border-app-green-200 rounded-lg p-4">
              <div class="flex items-center justify-between">
                <div>
                  <h4 class="font-semibold text-app-green-800">Most Productive Day</h4>
                  <p class="text-app-green-600 text-sm">
                    {{ formatDate(monthlyData.insights.mostProductiveDay.date) }}
                  </p>
                </div>
                <div class="text-right">
                  <div class="text-xl font-bold text-app-green-600">
                    {{ monthlyData.insights.mostProductiveDay.timeFormatted }}
                  </div>
                </div>
              </div>
            </div>

            <!-- Top Tasks by Average Session -->
            <div v-if="monthlyData.insights.topTasksByAvgSession" class="bg-app-blue-50 border border-app-blue-200 rounded-lg p-4">
              <h4 class="font-semibold text-app-blue-800 mb-3">Top Tasks by Average Session Length</h4>
              <div class="space-y-2">
                <div
                    v-for="(minutes, taskName) in monthlyData.insights.topTasksByAvgSession"
                    :key="taskName"
                    class="flex justify-between items-center"
                >
                  <span class="text-app-blue-700">{{ taskName }}</span>
                  <span class="font-medium text-app-blue-600">{{ formatTime(Math.round(minutes)) }}</span>
                </div>
              </div>
            </div>

            <!-- Task Variety Score -->
            <div v-if="monthlyData.insights.taskVarietyScore" class="bg-app-purple-50 border border-app-purple-200 rounded-lg p-4">
              <div class="flex items-center justify-between">
                <div>
                  <h4 class="font-semibold text-app-purple-800">Task Variety</h4>
                  <p class="text-app-purple-600 text-sm">
                    {{ getVarietyDescription(monthlyData.insights.taskVarietyScore) }}
                  </p>
                </div>
                <div class="text-right">
                  <div class="text-xl font-bold text-app-purple-600">
                    {{ monthlyData.insights.taskVarietyScore }}%
                  </div>
                </div>
              </div>
            </div>
          </div>
        </template>
      </Card>

      <!-- Category Breakdown -->
      <Card v-if="monthlyData.statistics?.categoryBreakdown">
        <template #title>
          <div class="flex items-center justify-between">
            <div class="flex items-center">
              <i class="pi pi-tags text-app-green-600 mr-2"></i>
              Category Breakdown
            </div>
            <Button
                label="Export Categories"
                icon="pi pi-download"
                size="small"
                @click="exportCategoryData"
                outlined
            />
          </div>
        </template>
        <template #content>
          <div class="space-y-3">
            <div
                v-for="category in monthlyData.statistics.categoryBreakdown"
                :key="category.categoryTitle"
                class="flex items-center justify-between p-3 bg-app-gray-50 rounded-lg"
            >
              <div class="flex items-center">
                <div>
                  <div class="font-medium text-app-primary">{{ category.categoryTitle }}</div>
                  <div class="text-sm text-app-secondary">{{ category.entryCount }} entries</div>
                </div>
              </div>
              <div class="text-right">
                <div class="font-bold text-app-primary">{{ category.timeFormatted }}</div>
                <div class="text-sm text-app-secondary">{{ calculatePercentage(category.totalMinutes) }}%</div>
              </div>
            </div>
          </div>
        </template>
      </Card>

      <!-- Task Breakdown -->
      <Card v-if="monthlyData.statistics?.taskBreakdown">
        <template #title>
          <div class="flex items-center justify-between">
            <div class="flex items-center">
              <i class="pi pi-list text-app-purple-600 mr-2"></i>
              Top Tasks This Month
            </div>
            <Button
                label="View All Tasks"
                icon="pi pi-external-link"
                size="small"
                @click="showAllTasks = !showAllTasks"
                outlined
            />
          </div>
        </template>
        <template #content>
          <div class="space-y-3">
            <div
                v-for="(task, index) in displayedTasks"
                :key="task.taskTitle"
                class="flex items-center justify-between p-3 bg-app-gray-50 rounded-lg"
            >
              <div class="flex items-center">
                <div class="text-sm font-bold text-app-secondary mr-3 w-6">
                  #{{ index + 1 }}
                </div>
                <div
                    class="w-4 h-4 rounded mr-3"
                    :style="{ backgroundColor: task.taskColor || '#6b7280' }"
                ></div>
                <div>
                  <div class="font-medium text-app-primary">{{ task.taskTitle }}</div>
                  <div class="text-sm text-app-secondary">{{ task.categoryTitle }} • {{ task.entryCount }} entries</div>
                </div>
              </div>
              <div class="text-right">
                <div class="font-bold text-app-primary">{{ task.timeFormatted }}</div>
                <div class="text-sm text-app-secondary">{{ calculatePercentage(task.totalMinutes) }}%</div>
              </div>
            </div>
          </div>
        </template>
      </Card>

      <!-- Monthly Summary -->
      <Card>
        <template #title>
          <div class="flex items-center justify-between">
            <div class="flex items-center">
              <i class="pi pi-calendar text-app-orange-600 mr-2"></i>
              Monthly Summary
            </div>
            <Button
                label="Export Month"
                icon="pi pi-download"
                size="small"
                @click="exportMonthlyData"
                outlined
            />
          </div>
        </template>
        <template #content>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <!-- Time Distribution -->
            <div>
              <h4 class="font-semibold text-app-primary mb-3">Time Distribution</h4>
              <div class="space-y-2">
                <div class="flex justify-between">
                  <span class="text-app-secondary">Weekdays:</span>
                  <span class="font-medium">{{ formatTime(monthlyData.statistics?.weekdayMinutes || 0) }}</span>
                </div>
                <div class="flex justify-between">
                  <span class="text-app-secondary">Weekends:</span>
                  <span class="font-medium">{{ formatTime(monthlyData.statistics?.weekendMinutes || 0) }}</span>
                </div>
                <div class="flex justify-between border-t pt-2">
                  <span class="text-app-secondary font-medium">Total:</span>
                  <span class="font-bold">{{ formatTime(monthlyData.statistics?.totalMinutes || 0) }}</span>
                </div>
              </div>
            </div>

            <!-- Activity Patterns -->
            <div>
              <h4 class="font-semibold text-app-primary mb-3">Activity Patterns</h4>
              <div class="space-y-2">
                <div class="flex justify-between">
                  <span class="text-app-secondary">Most active day:</span>
                  <span class="font-medium">{{ getMostActiveDay() }}</span>
                </div>
                <div class="flex justify-between">
                  <span class="text-app-secondary">Longest session:</span>
                  <span class="font-medium">{{ formatTime(monthlyData.statistics?.longestSession || 0) }}</span>
                </div>
                <div class="flex justify-between">
                  <span class="text-app-secondary">Average session:</span>
                  <span class="font-medium">{{ formatTime(monthlyData.statistics?.averageSession || 0) }}</span>
                </div>
              </div>
            </div>
          </div>
        </template>
      </Card>
    </div>

    <!-- No Data State -->
    <div v-else class="bg-app-gray-50 border border-app-gray-200 rounded-lg p-12 text-center">
      <i class="pi pi-calendar text-app-gray-400 text-4xl mb-4"></i>
      <h3 class="text-lg font-semibold text-app-gray-600 mb-2">No Data Available</h3>
      <p class="text-app-gray-500">No time entries found for {{ currentMonthName }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { format, startOfMonth, endOfMonth, addMonths, subMonths, eachDayOfInterval } from 'date-fns'
import type { CategoryBreakdown, TaskBreakdown, MonthlyReport } from '~/types/api'
import { Chart, registerables } from 'chart.js'

Chart.register(...registerables)

// Page setup
definePageMeta({
  layout: 'default'
})

// Composables
const reportsStore = useReportsStore()
const toast = useToast()

// Reactive state
const selectedDate = ref(new Date())
const showAllTasks = ref(false)

// Chart refs
const categoryChart = ref<HTMLCanvasElement>()

// Chart instances
let categoryChartInstance: Chart | null = null


// Computed properties
const loading = computed(() => reportsStore.loadingMonthly)
const error = computed(() => reportsStore.error)
const monthlyData = computed(() => reportsStore.currentMonthlyReport)

const currentMonthName = computed(() => {
  return format(selectedDate.value, 'MMMM yyyy')
})

const dailyActivityData = computed(() => {
  if (!monthlyData.value?.statistics) return []

  const year = selectedDate.value.getFullYear()
  const month = selectedDate.value.getMonth()
  const monthStart = new Date(year, month, 1)
  const monthEnd = new Date(year, month + 1, 0)

  // Generate all days in the month
  const daysInMonth = eachDayOfInterval({ start: monthStart, end: monthEnd })

  // Create mock daily data (in real app, this would come from backend)
  return daysInMonth.map(day => ({
    date: format(day, 'yyyy-MM-dd'),
    dayName: format(day, 'EEE'),
    dayNumber: day.getDate(),
    // Mock random hours for demonstration - replace with real data
    hours: Math.random() * 8
  }))
})

const displayedTasks = computed(() => {
  if (!monthlyData.value?.statistics?.taskBreakdown) return []
  const tasks: TaskBreakdown[] = monthlyData.value.statistics.taskBreakdown
  return showAllTasks.value ? tasks : tasks.slice(0, 5)
})

// Methods
const formatTime = (minutes: number) => {
  if (!minutes) return '0h 0m'
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  return hours > 0 ? `${hours}h ${mins}m` : `${mins}m`
}

const fetchMonthlyData = async () => {
  try {
    const year = selectedDate.value.getFullYear()
    const month = selectedDate.value.getMonth() + 1
    await reportsStore.fetchMonthlyReport(year, month)
  } catch (err: any) {
    console.error('Monthly report fetch error:', err)
    // Error is handled by the store
  }
}

const onDateChange = () => {
  refreshCurrentMonth()
}

const previousMonth = () => {
  const previous = subMonths(selectedDate.value, 1)
  selectedDate.value = previous
  refreshCurrentMonth()
}

const nextMonth = () => {
  const next = addMonths(selectedDate.value, 1)
  selectedDate.value = next
  refreshCurrentMonth()
}

const goToCurrentMonth = () => {
  selectedDate.value = new Date()
  refreshCurrentMonth()
}

const forceRefresh = async () => {
  try {
    // Clear cache and fetch fresh data
    const year = selectedDate.value.getFullYear()
    const month = selectedDate.value.getMonth() + 1
    const monthKey = `${year}-${String(month).padStart(2, '0')}`
    delete reportsStore.monthlyReports[monthKey]
    await reportsStore.fetchMonthlyReport(year, month)

    toast.add({
      severity: 'success',
      summary: 'Data Refreshed',
      detail: 'Monthly report data has been updated',
      life: 2000
    })
  } catch (error: any) {
    toast.add({
      severity: 'error',
      summary: 'Refresh Failed',
      detail: error.message || 'Failed to refresh monthly data',
      life: 3000
    })
  }
}

// Auto-refresh when navigating to a new month
const refreshCurrentMonth = async () => {
  // Always fetch fresh data when navigating
  const year = selectedDate.value.getFullYear()
  const month = selectedDate.value.getMonth() + 1
  const monthKey = `${year}-${String(month).padStart(2, '0')}`
  delete reportsStore.monthlyReports[monthKey]
  await fetchMonthlyData()
}

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return format(date, 'EEEE, MMMM do')
}

const calculatePercentage = (minutes: number) => {
  if (!monthlyData.value?.statistics?.totalMinutes) return 0
  return Math.round((minutes / monthlyData.value.statistics.totalMinutes) * 100)
}

const getVarietyDescription = (score: number) => {
  if (score >= 80) return 'Excellent task diversity'
  if (score >= 60) return 'Good task variety'
  if (score >= 40) return 'Moderate task focus'
  return 'High task concentration'
}

const getMostActiveDay = () => {
  // This would come from backend analysis, for now return placeholder
  return 'Monday' // Replace with actual data when available
}

const exportCategoryData = () => {
  if (!monthlyData.value?.statistics?.categoryBreakdown?.length) {
    toast.add({
      severity: 'warn',
      summary: 'No Data',
      detail: 'No category data to export',
      life: 3000
    })
    return
  }

  const csvData = monthlyData.value.statistics.categoryBreakdown.map((category: CategoryBreakdown) => ({
    Category: category.categoryTitle,
    'Total Time': category.timeFormatted,
    'Total Minutes': category.totalMinutes,
    'Entry Count': category.entryCount,
    'Percentage': calculatePercentage(category.totalMinutes) + '%'
  }))

  exportToCsv(csvData, `categories-${currentMonthName.value.replace(' ', '-').toLowerCase()}`)
}

const exportMonthlyData = () => {
  if (!monthlyData.value?.statistics) {
    toast.add({
      severity: 'warn',
      summary: 'No Data',
      detail: 'No monthly data to export',
      life: 3000
    })
    return
  }

  const monthlyStats = [{
    Month: currentMonthName.value,
    'Total Time': formatTime(monthlyData.value.statistics.totalMinutes || 0),
    'Total Minutes': monthlyData.value.statistics.totalMinutes || 0,
    'Active Days': monthlyData.value.statistics.daysTracked || 0,
    'Total Entries': monthlyData.value.statistics.totalEntries || 0,
    'Average Daily Hours': monthlyData.value.statistics.averageDailyHours || 0,
    'Most Productive Day': monthlyData.value.insights?.mostProductiveDay?.date || 'N/A'
  }]

  exportToCsv(monthlyStats, `monthly-summary-${currentMonthName.value.replace(' ', '-').toLowerCase()}`)
}

const exportToCsv = (data: any[], filename: string) => {
  const csvContent = [
    Object.keys(data[0]).join(','),
    ...data.map(row => Object.values(row).map(val => `"${val}"`).join(','))
  ].join('\n')

  const blob = new Blob([csvContent], { type: 'text/csv' })
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `${filename}.csv`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)

  toast.add({
    severity: 'success',
    summary: 'Export Complete',
    detail: `${filename}.csv has been downloaded`,
    life: 3000
  })
}

// Chart creation methods
const createCategoryChart = () => {
  if (!categoryChart.value || !monthlyData.value?.statistics?.categoryBreakdown?.length) return

  // Destroy existing chart
  if (categoryChartInstance) {
    categoryChartInstance.destroy()
  }

  const ctx = categoryChart.value.getContext('2d')
  if (!ctx) return

  const categories = monthlyData.value.statistics.categoryBreakdown

  categoryChartInstance = new Chart(ctx, {
    type: 'pie',
    data: {
      labels: categories.map(cat => cat.categoryTitle),
      datasets: [{
        data: categories.map(cat => cat.totalMinutes),
        backgroundColor: categories.map(cat => cat.color || '#' + Math.floor(Math.random() * 16777215).toString(16)),
        borderWidth: 2,
        borderColor: '#ffffff'
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: 'bottom',
          labels: {
            padding: 15,
            usePointStyle: true,
            font: {
              size: 12
            }
          }
        },
        tooltip: {
          callbacks: {
            label: (context) => {
              const label = context.label || ''
              const minutes = context.parsed as number
              const percentage = calculatePercentage(minutes)
              return `${label}: ${formatTime(minutes)} (${percentage}%)`
            }
          }
        }
      }
    }
  })
}

// Update charts when data changes
const updateCharts = () => {
  nextTick(() => {
    createCategoryChart()
  })
}

// Watch for data changes and update charts
watch(() => monthlyData.value, () => {
  updateCharts()
}, { deep: true })

// Initialize on mount
onMounted(() => {
  fetchMonthlyData().then(() => {
    updateCharts()
  })
})

onUnmounted(() => {
  if (categoryChartInstance) {
    categoryChartInstance.destroy()
  }
})
</script>