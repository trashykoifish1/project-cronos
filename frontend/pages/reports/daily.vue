<template>
  <div class="flex-1 p-6">
    <!-- Page Header -->
    <div class="mb-6">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-app-primary">Daily Reports</h1>
          <p class="text-app-secondary mt-1">
            View detailed breakdown of your daily time tracking
          </p>
        </div>

        <!-- Date Selection -->
        <div class="flex items-center space-x-3">
          <Button
              icon="pi pi-chevron-left"
              size="small"
              severity="secondary"
              @click="previousDay"
              v-tooltip="'Previous Day'"
          />

          <DatePicker
              v-model="selectedDate"
              @date-select="onDateChange"
              showIcon
              dateFormat="yy-mm-dd"
              placeholder="Select Date"
              class="w-48"
          />

          <Button
              icon="pi pi-chevron-right"
              size="small"
              severity="secondary"
              @click="nextDay"
              v-tooltip="'Next Day'"
          />

          <Button
              label="Today"
              size="small"
              severity="secondary"
              @click="goToToday"
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
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="flex items-center justify-center py-12">
      <ProgressSpinner />
      <span class="ml-3 text-app-secondary">Loading daily report...</span>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="bg-app-red-50 border border-app-red-200 rounded-lg p-6 text-center">
      <i class="pi pi-exclamation-triangle text-app-red-600 text-2xl mb-2"></i>
      <h3 class="text-lg font-semibold text-app-red-800 mb-2">Failed to Load Report</h3>
      <p class="text-app-red-600 mb-4">{{ error }}</p>
      <Button
          label="Retry"
          icon="pi pi-refresh"
          @click="fetchDailyReport"
          severity="danger"
          outlined
      />
    </div>

    <!-- Report Content -->
    <div v-else-if="reportData" class="space-y-6">
      <!-- Summary Cards -->
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <Card class="text-center">
          <template #content>
            <div class="text-3xl font-bold text-app-blue-600 mb-2">
              {{ reportData.totalTimeFormatted || '0h 0m' }}
            </div>
            <div class="text-sm text-app-secondary">Total Time Tracked</div>
          </template>
        </Card>

        <Card class="text-center">
          <template #content>
            <div class="text-3xl font-bold text-app-green-600 mb-2">
              {{ reportData.totalEntries || 0 }}
            </div>
            <div class="text-sm text-app-secondary">Time Entries</div>
          </template>
        </Card>

        <Card class="text-center">
          <template #content>
            <div class="text-3xl font-bold text-app-purple-600 mb-2">
              {{ reportData.categoryBreakdowns?.length || 0 }}
            </div>
            <div class="text-sm text-app-secondary">Categories Used</div>
          </template>
        </Card>

        <Card class="text-center">
          <template #content>
            <div class="text-3xl font-bold text-app-orange-600 mb-2">
              {{ reportData.taskBreakdowns?.length || 0 }}
            </div>
            <div class="text-sm text-app-secondary">Tasks Used</div>
          </template>
        </Card>
      </div>

      <!-- Charts Section -->
      <div v-if="reportData" class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
        <!-- Task Breakdown Pie Chart -->
        <Card>
          <template #title>
            <div class="flex items-center">
              <i class="pi pi-chart-pie text-app-purple-600 mr-2"></i>
              Task Distribution
            </div>
          </template>
          <template #content>
            <div v-if="reportData.taskBreakdowns?.length > 0">
              <!-- Fixed container for pie chart -->
              <div class="w-full h-64 relative">
                <canvas ref="taskPieChart"></canvas>
              </div>
              <div class="mt-4 text-center">
                <p class="text-sm text-app-secondary">
                  Time distribution across {{ reportData.taskBreakdowns.length }} tasks
                </p>
              </div>
            </div>
            <div v-else class="text-center py-8 text-app-secondary">
              <i class="pi pi-chart-pie text-3xl mb-2"></i>
              <div>No task data to visualize</div>
            </div>
          </template>
        </Card>

        <!-- Daily Timeline -->
        <Card>
          <template #title>
            <div class="flex items-center">
              <i class="pi pi-clock text-app-blue-600 mr-2"></i>
              Daily Timeline
            </div>
          </template>
          <template #content>
            <div v-if="reportData.timeEntries?.length > 0">
              <div class="space-y-2">
                <!-- Timeline Hours -->
                <div
                    v-for="hour in timelineHours"
                    :key="hour"
                    class="flex items-center space-x-2"
                >
                  <!-- Hour Label -->
                  <div class="w-12 text-xs text-app-secondary font-mono">
                    {{ formatHour(hour) }}
                  </div>

                  <!-- Timeline Bar -->
                  <div class="flex-1 relative h-6 bg-app-tertiary rounded">
                    <div
                        v-for="entry in getEntriesForHour(hour)"
                        :key="entry.id"
                        class="absolute h-full rounded transition-all duration-200 hover:opacity-80 cursor-pointer"
                        :style="{
                  backgroundColor: entry.taskColor,
                  left: getEntryPosition(entry, hour) + '%',
                  width: getEntryWidth(entry, hour) + '%'
                }"
                        :title="`${entry.taskTitle}: ${entry.startTime} - ${entry.endTime}`"
                    ></div>
                  </div>
                </div>
              </div>

              <!-- Timeline Legend -->
              <div class="mt-4 flex flex-wrap gap-2">
                <div
                    v-for="task in reportData.taskBreakdowns"
                    :key="task.taskId"
                    class="flex items-center space-x-1 text-xs"
                >
                  <div
                      class="w-3 h-3 rounded"
                      :style="{ backgroundColor: task.taskColor }"
                  ></div>
                  <span>{{ task.taskTitle }}</span>
                </div>
              </div>
            </div>
            <div v-else class="text-center py-8 text-app-secondary">
              <i class="pi pi-clock text-3xl mb-2"></i>
              <div>No timeline data available</div>
            </div>
          </template>
        </Card>
      </div>

      <!-- Productivity Insights -->
      <Card v-if="reportData" class="mb-6">
        <template #title>
          <div class="flex items-center">
            <i class="pi pi-lightbulb text-yellow-600 mr-2"></i>
            Productivity Insights
          </div>
        </template>
        <template #content>
          <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
            <!-- Peak Hours -->
            <div class="text-center p-4 bg-app-blue-50 rounded-lg">
              <div class="text-2xl font-bold text-app-blue-600 mb-1">
                {{ peakHour }}
              </div>
              <div class="text-sm text-app-blue-800">Peak Hour</div>
              <div class="text-xs text-app-secondary mt-1">
                Most productive time
              </div>
            </div>

            <!-- Average Session -->
            <div class="text-center p-4 bg-app-green-50 rounded-lg">
              <div class="text-2xl font-bold text-app-green-600 mb-1">
                {{ averageSessionLength }}
              </div>
              <div class="text-sm text-app-green-800">Avg Session</div>
              <div class="text-xs text-app-secondary mt-1">
                Average work session
              </div>
            </div>

            <!-- Focus Score -->
            <div class="text-center p-4 bg-app-purple-50 rounded-lg">
              <div class="text-2xl font-bold text-app-purple-600 mb-1">
                {{ focusScore }}%
              </div>
              <div class="text-sm text-app-purple-800">Focus Score</div>
              <div class="text-xs text-app-secondary mt-1">
                Task switching frequency
              </div>
            </div>
          </div>

          <!-- Insights List -->
          <div class="mt-6 space-y-2">
            <div
                v-for="insight in productivityInsights"
                :key="insight.type"
                class="flex items-start p-3 rounded-lg"
                :class="{
          'bg-app-green-50 border border-app-green-200': insight.type === 'positive',
          'bg-app-yellow-50 border border-yellow-200': insight.type === 'suggestion',
          'bg-app-blue-50 border border-app-blue-200': insight.type === 'info'
        }"
            >
              <i
                  class="mt-0.5 mr-3"
                  :class="{
            'pi pi-check-circle text-app-green-600': insight.type === 'positive',
            'pi pi-exclamation-triangle text-yellow-600': insight.type === 'suggestion',
            'pi pi-info-circle text-app-blue-600': insight.type === 'info'
          }"
              ></i>
              <div>
                <div class="font-medium text-app-primary">{{ insight.title }}</div>
                <div class="text-sm text-app-secondary">{{ insight.message }}</div>
              </div>
            </div>
          </div>
        </template>
      </Card>

      <!-- Category Breakdown -->
      <Card>
        <template #title>
          <div class="flex items-center">
            <i class="pi pi-folder text-app-blue-600 mr-2"></i>
            Time by Category
          </div>
        </template>
        <template #content>
          <div v-if="reportData.categoryBreakdowns?.length > 0" class="space-y-3">
            <div
                v-for="category in reportData.categoryBreakdowns"
                :key="category.categoryId"
                class="flex items-center justify-between p-3 bg-app-primary rounded-lg"
            >
              <div class="flex items-center space-x-3">
                <div class="text-lg font-medium text-app-primary">
                  {{ category.categoryTitle }}
                </div>
                <Badge
                    :value="`${category.entryCount} entries`"
                    severity="info"
                />
              </div>
              <div class="text-lg font-semibold text-app-blue-600">
                {{ category.timeFormatted }}
              </div>
            </div>
          </div>
          <div v-else class="text-center py-8 text-app-secondary">
            <i class="pi pi-inbox text-3xl mb-2"></i>
            <div>No categories tracked for this date</div>
          </div>
        </template>
      </Card>

      <!-- Task Breakdown -->
      <Card>
        <template #title>
          <div class="flex items-center">
            <i class="pi pi-bookmark text-app-purple-600 mr-2"></i>
            Time by Task
          </div>
        </template>
        <template #content>
          <div v-if="reportData.taskBreakdowns?.length > 0" class="space-y-3">
            <div
                v-for="task in reportData.taskBreakdowns"
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
                <Badge
                    :value="`${task.entryCount} entries`"
                    severity="info"
                />
              </div>
              <div class="text-lg font-semibold text-app-purple-600">
                {{ task.timeFormatted }}
              </div>
            </div>
          </div>
          <div v-else class="text-center py-8 text-app-secondary">
            <i class="pi pi-bookmark text-3xl mb-2"></i>
            <div>No tasks tracked for this date</div>
          </div>
        </template>
      </Card>

      <!-- Warnings (if any) -->
      <Card v-if="reportData.warnings?.length > 0">
        <template #title>
          <div class="flex items-center">
            <i class="pi pi-exclamation-triangle text-app-orange-600 mr-2"></i>
            Warnings & Insights
          </div>
        </template>
        <template #content>
          <div class="space-y-2">
            <div
                v-for="(warning, index) in reportData.warnings"
                :key="index"
                class="flex items-center p-3 bg-app-orange-50 border border-orange-200 rounded-lg"
            >
              <i class="pi pi-info-circle text-app-orange-600 mr-3"></i>
              <span class="text-app-orange-800">{{ warning }}</span>
            </div>
          </div>
        </template>
      </Card>

      <!-- Raw Time Entries -->
      <Card>
        <template #title>
          <div class="flex items-center justify-between">
            <div class="flex items-center">
              <i class="pi pi-clock text-app-green-600 mr-2"></i>
              Time Entries Detail
            </div>
            <Button
                label="Export CSV"
                icon="pi pi-download"
                size="small"
                @click="exportDay"
                outlined
            />
          </div>
        </template>
        <template #content>
          <div v-if="reportData.timeEntries?.length > 0">
            <DataTable
                :value="reportData.timeEntries"
                stripedRows
                class="p-datatable-sm"
                :paginator="reportData.timeEntries.length > 10"
                :rows="10"
            >
              <Column field="startTime" header="Start" />
              <Column field="endTime" header="End" />
              <Column header="Duration">
                <template #body="slotProps">
                  {{ formatDuration(slotProps.data.durationMinutes) }}
                </template>
              </Column>
              <Column header="Task">
                <template #body="slotProps">
                  <div class="flex items-center space-x-2">
                    <div
                        class="w-3 h-3 rounded-full"
                        :style="{ backgroundColor: slotProps.data.taskColor }"
                    ></div>
                    <span>{{ slotProps.data.taskTitle }}</span>
                  </div>
                </template>
              </Column>
              <Column field="categoryTitle" header="Category" />
              <Column field="description" header="Description">
                <template #body="slotProps">
                  <span class="text-app-secondary">
                    {{ slotProps.data.description || '—' }}
                  </span>
                </template>
              </Column>
            </DataTable>
          </div>
          <div v-else class="text-center py-8 text-app-secondary">
            <i class="pi pi-clock text-3xl mb-2"></i>
            <div>No time entries found for this date</div>
            <p class="text-sm mt-2">
              <NuxtLink to="/" class="text-app-blue-600 hover:underline">
                Go to timesheet to start tracking time
              </NuxtLink>
            </p>
          </div>
        </template>
      </Card>
    </div>

    <!-- No Data State -->
    <div v-else class="text-center py-12">
      <i class="pi pi-calendar text-4xl text-app-secondary mb-4"></i>
      <h3 class="text-lg font-semibold text-app-primary mb-2">No Data Available</h3>
      <p class="text-app-secondary mb-4">
        No time tracking data found for {{ formatDateLong(selectedDate) }}
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
import { format, addDays, subDays } from 'date-fns'
import {useReportsStore} from "~/stores/reports";
import Chart from 'chart.js/auto'

// Composables
const toast = useToast()
const { formatDuration } = useTime()
const route = useRoute()  // Add this
const router = useRouter()  // Add this

// Reactive state
const selectedDate = ref(new Date())
const taskPieChart = ref<HTMLCanvasElement>()
let taskPieChartInstance: Chart | null = null
// Add the reports store
const reportsStore = useReportsStore()

// Use computed properties from store
const reportData = computed(() => reportsStore.currentDailyReport)
const loading = computed(() => reportsStore.loadingDaily)
const error = computed(() => reportsStore.error)
// Timeline hours (7 AM to 6 PM)
const timelineHours = computed(() => {
  const hours = []
  for (let i = 7; i <= 18; i++) {
    hours.push(i)
  }
  return hours
})

// Peak hour calculation
const peakHour = computed(() => {
  if (!reportData.value?.timeEntries?.length) return 'N/A'

  const hourlyMinutes: Record<number, number> = {}

  reportData.value.timeEntries.forEach(entry => {
    const startHour = parseInt(entry.startTime.split(':')[0])
    const endHour = parseInt(entry.endTime.split(':')[0])

    for (let hour = startHour; hour <= endHour; hour++) {
      if (!hourlyMinutes[hour]) hourlyMinutes[hour] = 0

      if (hour === startHour && hour === endHour) {
        hourlyMinutes[hour] += entry.durationMinutes
      } else if (hour === startHour) {
        const minutesInHour = 60 - parseInt(entry.startTime.split(':')[1])
        hourlyMinutes[hour] += minutesInHour
      } else if (hour === endHour) {
        const minutesInHour = parseInt(entry.endTime.split(':')[1])
        hourlyMinutes[hour] += minutesInHour
      } else {
        hourlyMinutes[hour] += 60
      }
    }
  })

  const maxHour = Object.entries(hourlyMinutes).reduce((a, b) =>
      hourlyMinutes[parseInt(a[0])] > hourlyMinutes[parseInt(b[0])] ? a : b
  )

  return `${maxHour[0]}:00`
})

// Average session length
const averageSessionLength = computed(() => {
  if (!reportData.value?.timeEntries?.length) return 'N/A'

  const totalMinutes = reportData.value.timeEntries.reduce((sum, entry) => sum + entry.durationMinutes, 0)
  const avgMinutes = totalMinutes / reportData.value.timeEntries.length

  return formatDuration(Math.round(avgMinutes))
})

// Focus score (based on task switching)
const focusScore = computed(() => {
  if (!reportData.value?.timeEntries?.length) return 0

  const entries = [...reportData.value.timeEntries].sort((a, b) => a.startTime.localeCompare(b.startTime))
  let switches = 0

  for (let i = 1; i < entries.length; i++) {
    if (entries[i].taskId !== entries[i - 1].taskId) {
      switches++
    }
  }

  // Higher score = fewer switches = better focus
  const maxPossibleSwitches = entries.length - 1
  if (maxPossibleSwitches === 0) return 100

  const focusPercentage = Math.max(0, 100 - (switches / maxPossibleSwitches) * 100)
  return Math.round(focusPercentage)
})

// Productivity insights
const productivityInsights = computed(() => {
  if (!reportData.value) return []

  const insights = []
  const totalHours = reportData.value.totalMinutes / 60

  // Long day insight
  if (totalHours >= 8) {
    insights.push({
      type: 'positive',
      title: 'Great Work Day!',
      message: `You tracked ${totalHours.toFixed(1)} hours today. Excellent productivity!`
    })
  } else if (totalHours >= 6) {
    insights.push({
      type: 'info',
      title: 'Solid Day',
      message: `${totalHours.toFixed(1)} hours tracked. You're making good progress.`
    })
  }

  // Focus insight
  if (focusScore.value >= 80) {
    insights.push({
      type: 'positive',
      title: 'High Focus',
      message: 'You maintained great focus with minimal task switching.'
    })
  } else if (focusScore.value < 50) {
    insights.push({
      type: 'suggestion',
      title: 'Consider Longer Sessions',
      message: 'Try working in longer blocks to improve focus and reduce context switching.'
    })
  }

  // Task diversity
  const taskCount = reportData.value.taskBreakdowns?.length || 0
  if (taskCount === 1) {
    insights.push({
      type: 'info',
      title: 'Single Task Focus',
      message: 'You focused on one task today. Great for deep work!'
    })
  } else if (taskCount > 5) {
    insights.push({
      type: 'suggestion',
      title: 'Many Tasks',
      message: 'Consider grouping similar tasks together for better efficiency.'
    })
  }

  return insights
})

// Methods
const formatDateLong = (date: Date) => {
  return format(date, 'EEEE, MMMM do, yyyy')
}

const onDateChange = async (event: any) => {
  selectedDate.value = event
  const dateString = format(event, 'yyyy-MM-dd')

  // Update URL to reflect the new date
  updateURLWithDate(event)

  await reportsStore.fetchDailyReport(dateString)
}

const previousDay = async () => {
  const newDate = subDays(selectedDate.value, 1)
  selectedDate.value = newDate

  // Update URL
  updateURLWithDate(newDate)

  await fetchDailyReport()
}

const nextDay = async () => {
  const newDate = addDays(selectedDate.value, 1)
  selectedDate.value = newDate

  // Update URL
  updateURLWithDate(newDate)

  await fetchDailyReport()
}

const goToToday = async () => {
  const today = new Date()
  selectedDate.value = today

  // Update URL
  updateURLWithDate(today)

  await fetchDailyReport()
}

const fetchDailyReport = async () => {
  try {
    const dateString = format(selectedDate.value, 'yyyy-MM-dd')
    await reportsStore.fetchDailyReport(dateString)
  } catch (err: any) {
    console.error('Daily report fetch error:', err)
    // Error is handled by the store
  }
}


const exportDay = () => {
  const dateString = format(selectedDate.value, 'yyyy-MM-dd')

  if (!reportData.value?.timeEntries?.length) {
    toast.add({
      severity: 'warn',
      summary: 'No Data',
      detail: 'No time entries to export for this date',
      life: 3000
    })
    return
  }

  const csvData = reportData.value.timeEntries.map(entry => ({
    Date: dateString,
    'Start Time': entry.startTime,
    'End Time': entry.endTime,
    'Duration (minutes)': entry.durationMinutes,
    'Duration (formatted)': formatDuration(entry.durationMinutes),
    Task: entry.taskTitle,
    Category: entry.categoryTitle,
    Description: entry.description || ''
  }))

  const csvContent = [
    Object.keys(csvData[0]).join(','),
    ...csvData.map(row => Object.values(row).map(val => `"${val}"`).join(','))
  ].join('\n')

  const blob = new Blob([csvContent], { type: 'text/csv' })
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `daily-report-${dateString}.csv`
  link.click()
  window.URL.revokeObjectURL(url)

  toast.add({
    severity: 'success',
    summary: 'Export Complete',
    detail: `Exported ${csvData.length} time entries`,
    life: 3000
  })
}

const createTaskPieChart = () => {
  if (!taskPieChart.value || !reportData.value?.taskBreakdowns?.length) return

  // Destroy existing chart
  if (taskPieChartInstance) {
    taskPieChartInstance.destroy()
  }

  const ctx = taskPieChart.value.getContext('2d')
  if (!ctx) return

  taskPieChartInstance = new Chart(ctx, {
    type: 'pie',
    data: {
      labels: reportData.value.taskBreakdowns.map(task => task.taskTitle),
      datasets: [{
        data: reportData.value.taskBreakdowns.map(task => task.totalMinutes),
        backgroundColor: reportData.value.taskBreakdowns.map(task => task.taskColor),
        borderWidth: 2,
        borderColor: '#fff'
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false, // This allows it to fill the container
      plugins: {
        legend: {
          position: 'bottom',
          labels: {
            padding: 10,
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
              const value = context.parsed
              const formatted = formatDuration(value)
              return `${label}: ${formatted}`
            }
          }
        }
      },
      layout: {
        padding: 10
      }
    }
  })
}

const forceRefresh = async () => {
  try {
    // Clear cache and fetch fresh data
    const dateString = format(selectedDate.value, 'yyyy-MM-dd')
    reportsStore.invalidateDaily(dateString)
    await reportsStore.fetchDailyReport(dateString)

    toast.add({
      severity: 'success',
      summary: 'Data Refreshed',
      detail: 'Report data has been updated',
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

const formatHour = (hour: number) => {
  return `${hour.toString().padStart(2, '0')}:00`
}

const getEntriesForHour = (hour: number) => {
  if (!reportData.value?.timeEntries) return []

  return reportData.value.timeEntries.filter(entry => {
    const startHour = parseInt(entry.startTime.split(':')[0])
    const endHour = parseInt(entry.endTime.split(':')[0])
    return hour >= startHour && hour <= endHour
  })
}

const getEntryPosition = (entry: any, hour: number) => {
  const startHour = parseInt(entry.startTime.split(':')[0])
  const startMinute = parseInt(entry.startTime.split(':')[1])

  if (hour > startHour) return 0
  return (startMinute / 60) * 100
}

const getEntryWidth = (entry: any, hour: number) => {
  const startHour = parseInt(entry.startTime.split(':')[0])
  const startMinute = parseInt(entry.startTime.split(':')[1])
  const endHour = parseInt(entry.endTime.split(':')[0])
  const endMinute = parseInt(entry.endTime.split(':')[1])

  if (hour < startHour || hour > endHour) return 0

  let startPos = 0
  let endPos = 100

  if (hour === startHour) {
    startPos = (startMinute / 60) * 100
  }

  if (hour === endHour) {
    endPos = (endMinute / 60) * 100
  }

  return Math.max(0, endPos - startPos)
}

// URL parameter handling
const initializeDateFromURL = () => {
  const urlDate = route.query.date as string

  if (urlDate) {
    try {
      // Validate the date format (YYYY-MM-DD)
      const dateRegex = /^\d{4}-\d{2}-\d{2}$/
      if (dateRegex.test(urlDate)) {
        const parsedDate = new Date(urlDate + 'T00:00:00')

        // Check if it's a valid date
        if (!isNaN(parsedDate.getTime())) {
          selectedDate.value = parsedDate

          toast.add({
            severity: 'info',
            summary: 'Date Loaded',
            detail: `Showing report for ${format(parsedDate, 'MMMM do, yyyy')}`,
            life: 3000
          })
          return
        }
      }

      // If we get here, the date was invalid
      toast.add({
        severity: 'warn',
        summary: 'Invalid Date',
        detail: 'URL date parameter was invalid, showing today instead',
        life: 3000
      })
    } catch (error) {
      toast.add({
        severity: 'warn',
        summary: 'Date Error',
        detail: 'Could not parse URL date, showing today instead',
        life: 3000
      })
    }
  }

  // If no URL date or invalid date, use today (existing behavior)
  if (!route.query.date) {
    selectedDate.value = new Date()
  }
}

const updateURLWithDate = (date: Date) => {
  const dateString = format(date, 'yyyy-MM-dd')

  // Only update URL if the date parameter is different
  if (route.query.date !== dateString) {
    router.replace({
      query: { ...route.query, date: dateString }
    })
  }
}

watch(() => reportData.value, async (newData) => {
  if (newData) {
    // Wait for DOM to update
    await nextTick()

    // Small delay to ensure containers are properly sized
    setTimeout(() => {
      createTaskPieChart()
    }, 100)
  }
}, { immediate: true })

watch(() => route.query.date, (newDate) => {
  if (newDate && typeof newDate === 'string') {
    try {
      const dateRegex = /^\d{4}-\d{2}-\d{2}$/
      if (dateRegex.test(newDate)) {
        const parsedDate = new Date(newDate + 'T00:00:00')
        if (!isNaN(parsedDate.getTime()) &&
            format(parsedDate, 'yyyy-MM-dd') !== format(selectedDate.value, 'yyyy-MM-dd')) {
          selectedDate.value = parsedDate
          fetchDailyReport()
        }
      }
    } catch (error) {
      console.warn('Invalid date in URL:', newDate)
    }
  }
})

// Initialize
onMounted(async () => {
  initializeDateFromURL()
  await fetchDailyReport()
  await nextTick(() => {
    createTaskPieChart()
  })
})

onUnmounted(() => {
  if (taskPieChartInstance) {
    taskPieChartInstance.destroy()
  }
})

// Set page title
useHead({
  title: 'Daily Reports - Project Cronos'
})
</script>

<style scoped>
/* Chart container styling */
.chart-container {
  position: relative;
  height: 256px; /* h-64 equivalent */
  width: 100%;
  overflow: hidden;
}

/* Ensure canvas doesn't overflow */
canvas {
  max-width: 100% !important;
  max-height: 100% !important;
}

/* Card content padding adjustment for charts */
:deep(.p-card-content) {
  overflow: hidden;
}
</style>