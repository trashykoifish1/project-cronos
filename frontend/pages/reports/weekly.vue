<template>
  <div class="flex-1 p-6">
    <!-- Page Header -->
    <div class="mb-6">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-app-primary">Weekly Summary</h1>
          <p class="text-app-secondary mt-1">
            View your weekly time tracking overview and daily breakdown
          </p>
        </div>

        <!-- Week Navigation -->
        <div class="flex items-center space-x-3">
          <Button
              icon="pi pi-chevron-left"
              size="small"
              severity="secondary"
              @click="previousWeek"
              v-tooltip="'Previous Week'"
          />

          <Calendar
              v-model="selectedDate"
              @date-select="onDateChange"
              showIcon
              dateFormat="yy-mm-dd"
              placeholder="Select any date in week"
              class="w-48"
          />

          <Button
              icon="pi pi-chevron-right"
              size="small"
              severity="secondary"
              @click="nextWeek"
              v-tooltip="'Next Week'"
          />

          <Button
              label="This Week"
              size="small"
              @click="goToCurrentWeek"
              outlined
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

      <!-- Week Range Display -->
      <div v-if="weekRange" class="mt-2">
        <div class="text-lg font-medium text-app-primary">
          {{ weekRange.start }} - {{ weekRange.end }}
        </div>
        <div class="text-sm text-app-secondary">
          Week {{ weekRange.weekNumber }} of {{ weekRange.year }}
        </div>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="flex items-center justify-center py-12">
      <ProgressSpinner />
      <span class="ml-3 text-app-secondary">Loading weekly summary...</span>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="bg-app-red-50 border border-app-red-200 rounded-lg p-6 text-center">
      <i class="pi pi-exclamation-triangle text-app-red-600 text-2xl mb-2"></i>
      <h3 class="text-lg font-semibold text-app-red-800 mb-2">Failed to Load Weekly Summary</h3>
      <p class="text-app-red-600 mb-4">{{ error }}</p>
      <Button
          label="Retry"
          icon="pi pi-refresh"
          @click="fetchWeeklySummary"
          severity="danger"
          outlined
      />
    </div>

    <!-- Weekly Content -->
    <div v-else-if="weeklyData" class="space-y-6">
      <!-- Weekly Overview Cards -->
      <div class="grid grid-cols-1 md:grid-cols-5 gap-4">
        <Card class="text-center">
          <template #content>
            <div class="text-2xl font-bold text-app-blue-600 mb-2">
              {{ weeklyData.totalTimeFormatted || '0h 0m' }}
            </div>
            <div class="text-sm text-app-secondary">Total Time</div>
          </template>
        </Card>

        <Card class="text-center">
          <template #content>
            <div class="text-2xl font-bold text-app-green-600 mb-2">
              {{ weeklyData.averageDailyHours || '0.0' }}h
            </div>
            <div class="text-sm text-app-secondary">Daily Average</div>
          </template>
        </Card>

        <Card class="text-center">
          <template #content>
            <div class="text-2xl font-bold text-app-purple-600 mb-2">
              {{ activeDaysCount }}
            </div>
            <div class="text-sm text-app-secondary">Active Days</div>
          </template>
        </Card>

        <Card class="text-center">
          <template #content>
            <div class="text-2xl font-bold text-app-orange-600 mb-2">
              {{ totalEntriesCount }}
            </div>
            <div class="text-sm text-app-secondary">Time Entries</div>
          </template>
        </Card>

        <Card class="text-center">
          <template #content>
            <div class="text-2xl font-bold text-indigo-600 mb-2">
              {{ weeklyProductivity }}%
            </div>
            <div class="text-sm text-app-secondary">Weekly Goal</div>
          </template>
        </Card>
      </div>

      <!-- Daily Breakdown -->
      <Card>
        <template #title>
          <div class="flex items-center justify-between">
            <div class="flex items-center">
              <i class="pi pi-calendar text-app-blue-600 mr-2"></i>
              Daily Breakdown
            </div>
            <Button
                label="Export Week"
                icon="pi pi-download"
                size="small"
                @click="exportWeek"
                outlined
            />
          </div>
        </template>
        <template #content>
          <div v-if="weeklyData.dailySummaries?.length > 0">
            <div class="overflow-x-auto">
              <table class="w-full">
                <thead>
                <tr class="border-b border-app-primary">
                  <th class="text-left py-3 px-4 font-medium text-app-primary">Day</th>
                  <th class="text-left py-3 px-4 font-medium text-app-primary">Date</th>
                  <th class="text-left py-3 px-4 font-medium text-app-primary">Total Time</th>
                  <th class="text-left py-3 px-4 font-medium text-app-primary">Entries</th>
                  <th class="text-left py-3 px-4 font-medium text-app-primary">Top Task</th>
                  <th class="text-left py-3 px-4 font-medium text-app-primary">Progress</th>
                  <th class="text-center py-3 px-4 font-medium text-app-primary">Actions</th>
                </tr>
                </thead>
                <tbody>
                <tr
                    v-for="day in weeklyData.dailySummaries"
                    :key="day.date"
                    class="border-b border-app-gray-primary hover:bg-app-tertiary"
                >
                  <td class="py-3 px-4">
                    <div class="font-medium text-app-primary">
                      {{ formatDayName(day.date) }}
                    </div>
                  </td>
                  <td class="py-3 px-4">
                    <div class="text-sm text-app-secondary">
                      {{ formatDate(day.date) }}
                    </div>
                  </td>
                  <td class="py-3 px-4">
                    <div class="font-medium text-app-primary">
                      {{ day.totalTimeFormatted || '0h 0m' }}
                    </div>
                  </td>
                  <td class="py-3 px-4">
                    <Badge
                        :value="day.totalEntries.toString()"
                        :severity="day.totalEntries > 0 ? 'info' : 'secondary'"
                    />
                  </td>
                  <td class="py-3 px-4">
                    <div v-if="getTopTask(day)" class="flex items-center space-x-2">
                      <div
                          class="w-3 h-3 rounded-full"
                          :style="{ backgroundColor: getTopTask(day).taskColor }"
                      ></div>
                      <span class="text-sm text-app-primary">{{ getTopTask(day).taskTitle }}</span>
                    </div>
                    <span v-else class="text-sm text-app-secondary">No tasks</span>
                  </td>
                  <td class="py-3 px-4">
                    <div class="flex items-center space-x-2">
                      <div class="w-16 bg-app-tertiary rounded-full h-2">
                        <div
                            class="bg-app-blue-600 h-2 rounded-full transition-all duration-300"
                            :style="{ width: getDayProgress(day) + '%' }"
                        ></div>
                      </div>
                      <span class="text-xs text-app-secondary">{{ getDayProgress(day) }}%</span>
                    </div>
                  </td>
                  <td class="py-3 px-4 text-center">
                    <Button
                        label="View"
                        icon="pi pi-eye"
                        size="small"
                        severity="secondary"
                        @click="viewDailyReport(day.date)"
                        outlined
                    />
                  </td>
                </tr>
                </tbody>
              </table>
            </div>
          </div>
          <div v-else class="text-center py-8 text-app-secondary">
            <i class="pi pi-calendar text-3xl mb-2"></i>
            <div>No daily data found for this week</div>
          </div>
        </template>
      </Card>

      <!-- Weekly Task Summary -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <!-- Tasks This Week -->
        <Card>
          <template #title>
            <div class="flex items-center">
              <i class="pi pi-bookmark text-app-purple-600 mr-2"></i>
              Tasks This Week
            </div>
          </template>
          <template #content>
            <div v-if="weeklyTaskSummary?.length > 0" class="space-y-3">
              <div
                  v-for="task in weeklyTaskSummary"
                  :key="task.taskId"
                  class="flex items-center justify-between p-3 bg-app-primary rounded-lg"
              >
                <div class="flex items-center space-x-3">
                  <div
                      class="w-4 h-4 rounded-full"
                      :style="{ backgroundColor: task.taskColor }"
                  ></div>
                  <div>
                    <div class="font-medium text-app-primary">{{ task.taskTitle }}</div>
                    <div class="text-sm text-app-secondary">{{ task.categoryTitle }}</div>
                  </div>
                </div>
                <div class="text-right">
                  <div class="font-semibold text-app-purple-600">{{ task.timeFormatted }}</div>
                  <div class="text-xs text-app-secondary">{{ task.percentage }}% of week</div>
                </div>
              </div>
            </div>
            <div v-else class="text-center py-8 text-app-secondary">
              <i class="pi pi-bookmark text-3xl mb-2"></i>
              <div>No tasks tracked this week</div>
            </div>
          </template>
        </Card>

        <!-- Categories This Week -->
        <Card>
          <template #title>
            <div class="flex items-center">
              <i class="pi pi-folder text-app-blue-600 mr-2"></i>
              Categories This Week
            </div>
          </template>
          <template #content>
            <div v-if="weeklyCategorySummary?.length > 0" class="space-y-3">
              <div
                  v-for="category in weeklyCategorySummary"
                  :key="category.categoryId"
                  class="flex items-center justify-between p-3 bg-app-primary rounded-lg"
              >
                <div class="flex items-center space-x-3">
                  <div class="text-lg font-medium text-app-primary">
                    {{ category.categoryTitle }}
                  </div>
                  <Badge
                      :value="`${category.taskCount} tasks`"
                      severity="info"
                  />
                </div>
                <div class="text-right">
                  <div class="font-semibold text-app-blue-600">{{ category.timeFormatted }}</div>
                  <div class="text-xs text-app-secondary">{{ category.percentage }}% of week</div>
                </div>
              </div>
            </div>
            <div v-else class="text-center py-8 text-app-secondary">
              <i class="pi pi-folder text-3xl mb-2"></i>
              <div>No categories tracked this week</div>
            </div>
          </template>
        </Card>
      </div>
    </div>

    <!-- No Data State -->
    <div v-else class="text-center py-12">
      <i class="pi pi-calendar text-4xl text-app-secondary mb-4"></i>
      <h3 class="text-lg font-semibold text-app-primary mb-2">No Data Available</h3>
      <p class="text-app-secondary mb-4">
        No time tracking data found for this week
      </p>
      <Button
          label="Go to Timesheet"
          icon="pi pi-calendar"
          @click="$router.push('/')"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { format, startOfWeek, endOfWeek, addWeeks, subWeeks, parseISO, getWeek, getYear } from 'date-fns'

// Stores
const reportsStore = useReportsStore()

// Composables
const toast = useToast()
const { formatDuration } = useTime()

// Reactive state
const selectedDate = ref(new Date())

// Computed properties from store
const weeklyData = computed(() => reportsStore.currentWeeklyReport)
const loading = computed(() => reportsStore.loadingWeekly)
const error = computed(() => reportsStore.error)

// Week calculations
const weekRange = computed(() => {
  if (!selectedDate.value) return null

  const start = startOfWeek(selectedDate.value, { weekStartsOn: 1 }) // Monday start
  const end = endOfWeek(selectedDate.value, { weekStartsOn: 1 })

  return {
    start: format(start, 'MMM dd'),
    end: format(end, 'MMM dd, yyyy'),
    weekNumber: getWeek(selectedDate.value),
    year: getYear(selectedDate.value)
  }
})

// Weekly statistics
const activeDaysCount = computed(() => {
  if (!weeklyData.value?.dailySummaries) return 0
  return weeklyData.value.dailySummaries.filter(day => day.totalEntries > 0).length
})

const totalEntriesCount = computed(() => {
  if (!weeklyData.value?.dailySummaries) return 0
  return weeklyData.value.dailySummaries.reduce((sum, day) => sum + day.totalEntries, 0)
})

const weeklyProductivity = computed(() => {
  if (!weeklyData.value) return 0
  const weeklyGoal = 40 * 60 // 40 hours in minutes
  return Math.min(Math.round((weeklyData.value.totalMinutes / weeklyGoal) * 100), 100)
})

// Task and category summaries
const weeklyTaskSummary = computed(() => {
  if (!weeklyData.value?.dailySummaries) return []

  const taskTotals: Record<string, any> = {}
  let totalWeekMinutes = 0

  weeklyData.value.dailySummaries.forEach(day => {
    if (day.taskBreakdowns) {
      day.taskBreakdowns.forEach(task => {
        if (!taskTotals[task.taskId]) {
          taskTotals[task.taskId] = {
            taskId: task.taskId,
            taskTitle: task.taskTitle,
            taskColor: task.taskColor,
            categoryTitle: task.categoryTitle,
            totalMinutes: 0
          }
        }
        taskTotals[task.taskId].totalMinutes += task.totalMinutes
        totalWeekMinutes += task.totalMinutes
      })
    }
  })

  return Object.values(taskTotals)
      .map((task: any) => ({
        ...task,
        timeFormatted: formatDuration(task.totalMinutes),
        percentage: totalWeekMinutes > 0 ? Math.round((task.totalMinutes / totalWeekMinutes) * 100) : 0
      }))
      .sort((a, b) => b.totalMinutes - a.totalMinutes)
})

const weeklyCategorySummary = computed(() => {
  if (!weeklyData.value?.dailySummaries) return []

  const categoryTotals: Record<string, any> = {}
  let totalWeekMinutes = 0

  weeklyData.value.dailySummaries.forEach(day => {
    if (day.categoryBreakdowns) {
      day.categoryBreakdowns.forEach(category => {
        if (!categoryTotals[category.categoryId]) {
          categoryTotals[category.categoryId] = {
            categoryId: category.categoryId,
            categoryTitle: category.categoryTitle,
            totalMinutes: 0,
            taskCount: 0
          }
        }
        categoryTotals[category.categoryId].totalMinutes += category.totalMinutes
        totalWeekMinutes += category.totalMinutes
      })
    }

    // Count unique tasks per category
    if (day.taskBreakdowns) {
      const tasksByCategory: Record<string, Set<string>> = {}
      day.taskBreakdowns.forEach(task => {
        if (!tasksByCategory[task.categoryTitle]) {
          tasksByCategory[task.categoryTitle] = new Set()
        }
        tasksByCategory[task.categoryTitle].add(task.taskId.toString())
      })

      Object.entries(tasksByCategory).forEach(([categoryTitle, taskIds]) => {
        const category = Object.values(categoryTotals).find((c: any) => c.categoryTitle === categoryTitle)
        if (category) {
          category.taskCount = Math.max(category.taskCount, taskIds.size)
        }
      })
    }
  })

  return Object.values(categoryTotals)
      .map((category: any) => ({
        ...category,
        timeFormatted: formatDuration(category.totalMinutes),
        percentage: totalWeekMinutes > 0 ? Math.round((category.totalMinutes / totalWeekMinutes) * 100) : 0
      }))
      .sort((a, b) => b.totalMinutes - a.totalMinutes)
})

// Methods
const onDateChange = async (event: any) => {
  selectedDate.value = event
  await fetchWeeklySummary()
}

const previousWeek = async () => {
  const newDate = subWeeks(selectedDate.value, 1)
  selectedDate.value = newDate
  await fetchWeeklySummary()
}

const nextWeek = async () => {
  const newDate = addWeeks(selectedDate.value, 1)
  selectedDate.value = newDate
  await fetchWeeklySummary()
}

const goToCurrentWeek = async () => {
  const today = new Date()
  selectedDate.value = today
  await fetchWeeklySummary()
}

const fetchWeeklySummary = async () => {
  try {
    const dateString = format(selectedDate.value, 'yyyy-MM-dd')
    await reportsStore.fetchWeeklyReport(dateString)
  } catch (err: any) {
    console.error('Weekly summary fetch error:', err)
  }
}

const forceRefresh = async () => {
  try {
    const dateString = format(selectedDate.value, 'yyyy-MM-dd')
    reportsStore.invalidateWeekly(dateString)
    await reportsStore.fetchWeeklyReport(dateString)

    toast.add({
      severity: 'success',
      summary: 'Data Refreshed',
      detail: 'Weekly summary has been updated',
      life: 2000
    })
  } catch (error: any) {
    toast.add({
      severity: 'error',
      summary: 'Refresh Failed',
      detail: error.message || 'Failed to refresh data',
      life: 3000
    })
  }
}

// Helper methods
const formatDayName = (dateString: string) => {
  return format(parseISO(dateString), 'EEEE')
}

const formatDate = (dateString: string) => {
  return format(parseISO(dateString), 'MMM dd')
}

const getTopTask = (day: any) => {
  if (!day.taskBreakdowns?.length) return null
  return day.taskBreakdowns.reduce((prev: any, current: any) =>
      prev.totalMinutes > current.totalMinutes ? prev : current
  )
}

const getDayProgress = (day: any) => {
  const dailyGoal = 8 * 60 // 8 hours in minutes
  return Math.min(Math.round((day.totalMinutes / dailyGoal) * 100), 100)
}

const viewDailyReport = (date: string) => {
  navigateTo(`/reports/daily?date=${date}`)
}

const exportWeek = () => {
  if (!weeklyData.value?.dailySummaries?.length) {
    toast.add({
      severity: 'warn',
      summary: 'No Data',
      detail: 'No weekly data to export',
      life: 3000
    })
    return
  }

  const csvData: any[] = []

  weeklyData.value.dailySummaries.forEach(day => {
    if (day.timeEntries?.length) {
      day.timeEntries.forEach(entry => {
        csvData.push({
          Date: entry.entryDate,
          Day: formatDayName(entry.entryDate),
          'Start Time': entry.startTime,
          'End Time': entry.endTime,
          'Duration (minutes)': entry.durationMinutes,
          'Duration (formatted)': formatDuration(entry.durationMinutes),
          Task: entry.taskTitle,
          Category: entry.categoryTitle,
          Description: entry.description || ''
        })
      })
    }
  })

  if (csvData.length === 0) {
    toast.add({
      severity: 'warn',
      summary: 'No Entries',
      detail: 'No time entries found in this week',
      life: 3000
    })
    return
  }

  const csvContent = [
    Object.keys(csvData[0]).join(','),
    ...csvData.map(row => Object.values(row).map(val => `"${val}"`).join(','))
  ].join('\n')

  const blob = new Blob([csvContent], { type: 'text/csv' })
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `weekly-report-${format(selectedDate.value, 'yyyy-MM-dd')}.csv`
  link.click()
  window.URL.revokeObjectURL(url)

  toast.add({
    severity: 'success',
    summary: 'Export Complete',
    detail: `Exported ${csvData.length} time entries`,
    life: 3000
  })
}

// Initialize
onMounted(async () => {
  await fetchWeeklySummary()
})

// Set page title
useHead({
  title: 'Weekly Summary - Time Tracker'
})
</script>