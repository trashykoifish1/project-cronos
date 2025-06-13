<template>
  <div class="flex-1 p-6">
    <!-- Page Header -->
    <div class="mb-6">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-app-primary">Statistics</h1>
          <p class="text-app-secondary mt-1">
            Analyze your time tracking patterns and productivity trends
          </p>
        </div>

        <!-- Time Period Selector -->
        <div class="flex items-center space-x-3">
          <div class="flex items-center space-x-2">
            <label class="text-sm font-medium text-app-secondary">Period:</label>
            <Select
                v-model="selectedPeriod"
                @change="onPeriodChange"
                :options="periods"
                option-label="label"
                option-value="value"
                size="small"
            >
            </Select>
          </div>

          <!-- Custom Date Range (only show when custom is selected) -->
          <div v-if="selectedPeriod === 'custom'" class="flex items-center space-x-2">
            <DatePicker
                v-model="customStartDate"
                @date-select="onCustomDateChange"
                showIcon
                dateFormat="yy-mm-dd"
                placeholder="Start Date"
                class="w-40"
                size="small"
            />
            <span class="text-app-secondary">to</span>
            <Calendar
                v-model="customEndDate"
                @date-select="onCustomDateChange"
                showIcon
                dateFormat="yy-mm-dd"
                placeholder="End Date"
                class="w-40"
                size="small"
            />
          </div>

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

      <!-- Period Display -->
      <div class="mt-2">
        <div class="text-lg font-medium text-app-primary">
          {{ currentPeriodDisplay }}
        </div>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="flex items-center justify-center py-12">
      <ProgressSpinner />
      <span class="ml-3 text-app-secondary">Loading statistics...</span>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="bg-app-red-50 border border-app-red-200 rounded-lg p-6 text-center">
      <i class="pi pi-exclamation-triangle text-app-red-600 text-2xl mb-2"></i>
      <h3 class="text-lg font-semibold text-app-red-800 mb-2">Failed to Load Statistics</h3>
      <p class="text-app-red-600 mb-4">{{ error }}</p>
      <Button
          label="Retry"
          icon="pi pi-refresh"
          @click="fetchStatistics"
          severity="danger"
          outlined
      />
    </div>

    <!-- Statistics Content -->
    <div v-else-if="statisticsData" class="space-y-6">
      <!-- Key Metrics Overview -->
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <Card class="text-center">
          <template #content>
            <div class="text-2xl font-bold text-app-blue-600 mb-2">
              {{ formatTime(statisticsData.totalMinutes || 0) }}
            </div>
            <div class="text-sm text-app-secondary">Total Time</div>
          </template>
        </Card>

        <Card class="text-center">
          <template #content>
            <div class="text-2xl font-bold text-app-green-600 mb-2">
              {{ statisticsData.daysTracked || 0 }}
            </div>
            <div class="text-sm text-app-secondary">Active Days</div>
          </template>
        </Card>

        <Card class="text-center">
          <template #content>
            <div class="text-2xl font-bold text-app-purple-600 mb-2">
              {{ statisticsData.averageDailyHours || '0.0' }}h
            </div>
            <div class="text-sm text-app-secondary">Daily Average</div>
          </template>
        </Card>

        <Card class="text-center">
          <template #content>
            <div class="text-2xl font-bold text-app-orange-600 mb-2">
              {{ statisticsData.totalEntries || 0 }}
            </div>
            <div class="text-sm text-app-secondary">Total Entries</div>
          </template>
        </Card>
      </div>

      <!-- Productivity Summary -->
      <Card>
        <template #title>
          <div class="flex items-center justify-between">
            <div class="flex items-center">
              <i class="pi pi-chart-line text-app-blue-600 mr-2"></i>
              Productivity Summary
            </div>
            <Button
                label="Export Data"
                icon="pi pi-download"
                size="small"
                @click="exportStatistics"
                outlined
            />
          </div>
        </template>
        <template #content>
          <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
            <!-- Time Distribution -->
            <div>
              <h4 class="font-semibold text-app-primary mb-3">Time Distribution</h4>
              <div class="space-y-2">
                <div class="flex justify-between">
                  <span class="text-app-secondary">Weekdays:</span>
                  <span class="font-medium">{{ formatTime(statisticsData.weekdayMinutes || 0) }}</span>
                </div>
                <div class="flex justify-between">
                  <span class="text-app-secondary">Weekends:</span>
                  <span class="font-medium">{{ formatTime(statisticsData.weekendMinutes || 0) }}</span>
                </div>
                <div class="flex justify-between border-t pt-2">
                  <span class="text-app-secondary">Consistency:</span>
                  <span class="font-medium">{{ getConsistencyScore() }}%</span>
                </div>
              </div>
            </div>

            <!-- Session Analysis -->
            <div>
              <h4 class="font-semibold text-app-primary mb-3">Session Analysis</h4>
              <div class="space-y-2">
                <div class="flex justify-between">
                  <span class="text-app-secondary">Average session:</span>
                  <span class="font-medium">{{ formatTime(Math.round(statisticsData.averageSession || 0)) }}</span>
                </div>
                <div class="flex justify-between">
                  <span class="text-app-secondary">Longest session:</span>
                  <span class="font-medium">{{ formatTime(statisticsData.longestSession || 0) }}</span>
                </div>
                <div class="flex justify-between border-t pt-2">
                  <span class="text-app-secondary">Focus score:</span>
                  <span class="font-medium">{{ getFocusScore() }}%</span>
                </div>
              </div>
            </div>

            <!-- Activity Patterns -->
            <div>
              <h4 class="font-semibold text-app-primary mb-3">Activity Patterns</h4>
              <div class="space-y-2">
                <div class="flex justify-between">
                  <span class="text-app-secondary">Most productive day:</span>
                  <span class="font-medium">{{ getMostProductiveDay() }}</span>
                </div>
                <div class="flex justify-between">
                  <span class="text-app-secondary">Categories used:</span>
                  <span class="font-medium">{{ statisticsData.categoryBreakdown?.length || 0 }}</span>
                </div>
                <div class="flex justify-between border-t pt-2">
                  <span class="text-app-secondary">Tasks completed:</span>
                  <span class="font-medium">{{ statisticsData.taskBreakdown?.length || 0 }}</span>
                </div>
              </div>
            </div>
          </div>
        </template>
      </Card>

      <!-- Top Categories & Tasks -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <!-- Top Categories -->
        <Card v-if="statisticsData.categoryBreakdown?.length">
          <template #title>
            <div class="flex items-center">
              <i class="pi pi-tags text-app-green-600 mr-2"></i>
              Top Categories
            </div>
          </template>
          <template #content>
            <div class="space-y-3">
              <div
                  v-for="(category, index) in topCategories"
                  :key="category.categoryTitle"
                  class="flex items-center justify-between p-3 bg-app-gray-50 rounded-lg"
              >
                <div class="flex items-center">
                  <div class="text-sm font-bold text-app-secondary mr-3 w-6">
                    #{{ index + 1 }}
                  </div>
                  <div
                      class="w-4 h-4 rounded mr-3"
                      :style="{ backgroundColor: category.color || '#6b7280' }"
                  ></div>
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

        <!-- Top Tasks -->
        <Card v-if="statisticsData.taskBreakdown?.length">
          <template #title>
            <div class="flex items-center">
              <i class="pi pi-list text-app-purple-600 mr-2"></i>
              Top Tasks
            </div>
          </template>
          <template #content>
            <div class="space-y-3">
              <div
                  v-for="(task, index) in topTasks"
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
      </div>
    </div>

    <!-- No Data State -->
    <div v-else class="bg-app-gray-50 border border-app-gray-200 rounded-lg p-12 text-center">
      <i class="pi pi-chart-bar text-app-gray-400 text-4xl mb-4"></i>
      <h3 class="text-lg font-semibold text-app-gray-600 mb-2">No Data Available</h3>
      <p class="text-app-gray-500">No time entries found for the selected period</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { format, subDays, startOfWeek, endOfWeek, startOfMonth, endOfMonth } from 'date-fns'
import type { CategoryBreakdown, TaskBreakdown } from '~/types/api'

// Page setup
definePageMeta({
  layout: 'default'
})

// Composables
const reportsStore = useReportsStore()
const toast = useToast()

// Reactive state
const selectedPeriod = ref('last30')
const customStartDate = ref(new Date())
const customEndDate = ref(new Date())

// Computed properties
const loading = computed(() => reportsStore.loadingStatistics)
const error = computed(() => reportsStore.error)
const statisticsData = computed(() => reportsStore.statistics)

const periods = computed(() => [
  { value: 'last7', label: 'Last 7 Days' },
  { value: 'last30', label: 'Last 30 Days' },
  { value: 'last90', label: 'Last 90 Days' },
  { value: 'thisWeek', label: 'This Week' },
  { value: 'thisMonth', label: 'This Month' },
  { value: 'custom', label: 'Custom Range' }
])

const currentPeriodDisplay = computed(() => {
  const period = periods.value.find(p => p.value === selectedPeriod.value)
  if (selectedPeriod.value === 'custom') {
    return `${format(customStartDate.value, 'MMM d, yyyy')} - ${format(customEndDate.value, 'MMM d, yyyy')}`
  }
  return period?.label || 'Unknown Period'
})

const topCategories = computed(() => {
  if (!statisticsData.value?.categoryBreakdown) return []
  return statisticsData.value.categoryBreakdown.slice(0, 5)
})

const topTasks = computed(() => {
  if (!statisticsData.value?.taskBreakdown) return []
  return statisticsData.value.taskBreakdown.slice(0, 5)
})

// Methods
const formatTime = (minutes: number) => {
  if (!minutes) return '0h 0m'
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  return hours > 0 ? `${hours}h ${mins}m` : `${mins}m`
}

const calculatePercentage = (minutes: number) => {
  if (!statisticsData.value?.totalMinutes) return 0
  return Math.round((minutes / statisticsData.value.totalMinutes) * 100)
}

const getConsistencyScore = () => {
  if (!statisticsData.value) return 0
  const totalDays = getPeriodDays()
  const activeDays = statisticsData.value.daysTracked || 0
  return Math.round((activeDays / totalDays) * 100)
}

const getFocusScore = () => {
  if (!statisticsData.value?.averageSession) return 0
  // Score based on average session length (higher is better, max 100)
  const avgMinutes = statisticsData.value.averageSession
  return Math.min(Math.round((avgMinutes / 120) * 100), 100) // 2 hours = 100%
}

const getMostProductiveDay = () => {
  // This would come from backend analysis, for now return placeholder
  return 'Monday' // Replace with actual data when available
}

const getPeriodDays = () => {
  switch (selectedPeriod.value) {
    case 'last7': return 7
    case 'last30': return 30
    case 'last90': return 90
    case 'thisWeek': return 7
    case 'thisMonth': return new Date().getDate()
    case 'custom':
      const diffTime = Math.abs(customEndDate.value.getTime() - customStartDate.value.getTime())
      return Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1
    default: return 30
  }
}

const getDateRange = () => {
  const today = new Date()

  switch (selectedPeriod.value) {
    case 'last7':
      return {
        startDate: format(subDays(today, 6), 'yyyy-MM-dd'),
        endDate: format(today, 'yyyy-MM-dd')
      }
    case 'last30':
      return {
        startDate: format(subDays(today, 29), 'yyyy-MM-dd'),
        endDate: format(today, 'yyyy-MM-dd')
      }
    case 'last90':
      return {
        startDate: format(subDays(today, 89), 'yyyy-MM-dd'),
        endDate: format(today, 'yyyy-MM-dd')
      }
    case 'thisWeek':
      return {
        startDate: format(startOfWeek(today, { weekStartsOn: 1 }), 'yyyy-MM-dd'),
        endDate: format(endOfWeek(today, { weekStartsOn: 1 }), 'yyyy-MM-dd')
      }
    case 'thisMonth':
      return {
        startDate: format(startOfMonth(today), 'yyyy-MM-dd'),
        endDate: format(endOfMonth(today), 'yyyy-MM-dd')
      }
    case 'custom':
      return {
        startDate: format(customStartDate.value, 'yyyy-MM-dd'),
        endDate: format(customEndDate.value, 'yyyy-MM-dd')
      }
    default:
      return {
        startDate: format(subDays(today, 29), 'yyyy-MM-dd'),
        endDate: format(today, 'yyyy-MM-dd')
      }
  }
}

const fetchStatistics = async () => {
  try {
    const { startDate, endDate } = getDateRange()
    await reportsStore.fetchStatistics(startDate, endDate)
  } catch (err: any) {
    console.error('Statistics fetch error:', err)
    // Error is handled by the store
  }
}

const onPeriodChange = () => {
  fetchStatistics()
}

const onCustomDateChange = () => {
  if (selectedPeriod.value === 'custom') {
    fetchStatistics()
  }
}

const forceRefresh = async () => {
  try {
    // Clear cached statistics and refetch
    reportsStore.statistics = null
    await fetchStatistics()

    toast.add({
      severity: 'success',
      summary: 'Data Refreshed',
      detail: 'Statistics have been updated',
      life: 2000
    })
  } catch (error: any) {
    toast.add({
      severity: 'error',
      summary: 'Refresh Failed',
      detail: error.message || 'Failed to refresh statistics',
      life: 3000
    })
  }
}

const exportStatistics = () => {
  if (!statisticsData.value) {
    toast.add({
      severity: 'warn',
      summary: 'No Data',
      detail: 'No statistics data to export',
      life: 3000
    })
    return
  }

  const exportData = [{
    Period: currentPeriodDisplay.value,
    'Total Time': formatTime(statisticsData.value.totalMinutes || 0),
    'Total Minutes': statisticsData.value.totalMinutes || 0,
    'Active Days': statisticsData.value.daysTracked || 0,
    'Total Entries': statisticsData.value.totalEntries || 0,
    'Average Daily Hours': statisticsData.value.averageDailyHours || 0,
    'Average Session': formatTime(Math.round(statisticsData.value.averageSession || 0)),
    'Longest Session': formatTime(statisticsData.value.longestSession || 0),
    'Consistency Score': getConsistencyScore() + '%',
    'Focus Score': getFocusScore() + '%'
  }]

  const csvContent = [
    Object.keys(exportData[0]).join(','),
    ...exportData.map(row => Object.values(row).map(val => `"${val}"`).join(','))
  ].join('\n')

  const blob = new Blob([csvContent], { type: 'text/csv' })
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `statistics-${selectedPeriod.value}-${format(new Date(), 'yyyy-MM-dd')}.csv`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)

  toast.add({
    severity: 'success',
    summary: 'Export Complete',
    detail: 'Statistics have been downloaded',
    life: 3000
  })
}

// Initialize on mount
onMounted(() => {
  fetchStatistics()
})
</script>