<template>
    <!-- Main Content Area -->
    <div class="flex flex-1 overflow-hidden">
      <!-- Task Sidebar -->
      <div class="w-80 flex-shrink-0 bg-white border-r border-gray-200">
        <TaskSidebar />
      </div>

      <!-- Timesheet Area -->
      <div class="flex-1 flex flex-col">
        <!-- Current Day Header -->
        <div class="bg-white border-b border-gray-200 p-6">
          <div class="flex items-center justify-between">
            <div>
              <div class="pt-2 space-x-2">
                <Button
                    icon="pi pi-chevron-left"
                    size="large"
                    severity="secondary"
                    @click="previousDay"
                    v-tooltip="'Previous Day'"
                    class="w-8 h-12"
                    variant="text"
                />

                <DatePicker
                    v-model="selectedDate"
                    @date-select="onDateChange"
                    showIcon
                    :showButtonBar="true"
                    dateFormat="DD, MM dth, yy"
                    placeholder="Select Date"
                    size="large"
                    input-class="font-semibold"
                />

                <Button
                    icon="pi pi-chevron-right"
                    size="large"
                    severity="secondary"
                    @click="nextDay"
                    v-tooltip="'Next Day'"
                    class="w-8 h-12"
                    variant="text"
                />
              </div>
              <p class="text-gray-600 mt-1">
                Track your time by selecting a task and painting time slots
              </p>
            </div>


            <!-- Quick Actions -->
            <div class="flex items-center space-x-3">
              <Button
                  @click="showQuickAdd = true"
                  label="Quick Add"
                  icon="pi pi-plus"
                  size="small"
                  severity="info"
              />
              <Button
                  @click="exportToday"
                  label="Export"
                  icon="pi pi-download"
                  size="small"
                  severity="secondary"
              />
              <Button
                  @click="refreshTimesheet"
                  icon="pi pi-refresh"
                  size="small"
                  severity="secondary"
                  :loading="timesheetStore.loading"
                  v-tooltip="'Refresh'"
              />
            </div>
          </div>

          <!-- Daily Statistics -->
          <div class="mt-6 grid grid-cols-4 gap-4">
            <div class="bg-blue-50 rounded-lg p-4 text-center">
              <div class="text-2xl font-bold text-blue-600">
                {{ formatDuration(timesheetStore.totalTrackedMinutes) }}
              </div>
              <div class="text-sm text-blue-800">Total Tracked</div>
            </div>

            <div class="bg-green-50 rounded-lg p-4 text-center">
              <div class="text-2xl font-bold text-green-600">
                {{ timesheetStore.timeEntries.length }}
              </div>
              <div class="text-sm text-green-800">Time Entries</div>
            </div>

            <div class="bg-purple-50 rounded-lg p-4 text-center">
              <div class="text-2xl font-bold text-purple-600">
                {{ uniqueTasksCount }}
              </div>
              <div class="text-sm text-purple-800">Tasks Used</div>
            </div>

            <div class="bg-orange-50 rounded-lg p-4 text-center">
              <div class="text-2xl font-bold text-orange-600">
                {{ completionPercentage }}%
              </div>
              <div class="text-sm text-orange-800">Day Progress</div>
            </div>
          </div>
        </div>

        <!-- Timesheet Grid -->
        <div class="flex-1 overflow-auto p-6">
          <div class="bg-white rounded-lg border border-gray-200 p-4">
            <TimesheetGrid />
          </div>
        </div>

        <!-- Time Breakdown Footer -->
        <div class="bg-white border-t border-gray-200 p-4">
          <div class="flex items-center justify-between">
            <!-- Time Breakdown by Task -->
            <div class="flex items-center space-x-4">
              <span class="text-sm font-medium text-gray-700">Today's Breakdown:</span>
              <div class="flex items-center space-x-4 flex-wrap">
                <div
                    v-for="(breakdown, taskId) in timeBreakdown"
                    :key="taskId"
                    class="flex items-center space-x-2 px-3 py-1 bg-gray-50 rounded-full"
                >
                  <div
                      class="w-3 h-3 rounded-full"
                      :style="{ backgroundColor: breakdown.color }"
                  ></div>
                  <span class="text-sm font-medium text-gray-700">
                    {{ breakdown.taskTitle }}
                  </span>
                  <span class="text-sm text-gray-600">
                    {{ formatDuration(breakdown.minutes) }}
                  </span>
                </div>

                <div v-if="Object.keys(timeBreakdown).length === 0" class="text-sm text-gray-500">
                  No time entries yet - start by selecting a task and painting time slots!
                </div>
              </div>
            </div>

            <!-- Action Buttons -->
            <div class="flex items-center space-x-2">
              <Button
                  @click="clearDay"
                  label="Clear Day"
                  icon="pi pi-trash"
                  size="small"
                  severity="danger"
                  :disabled="timesheetStore.timeEntries.length === 0"
              />
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Dialogs and Overlays -->

    <!-- Quick Add Time Entry Dialog -->
    <Dialog
        v-model:visible="showQuickAdd"
        modal
        header="Quick Add Time Entry"
        :style="{ width: '500px' }"
    >
      <div class="space-y-4">
        <div class="text-sm text-gray-600">
          Quickly add a time entry for today without using the timesheet grid.
        </div>
        <!-- Quick add form would go here -->
        <div class="text-center py-8 text-gray-500">
          <i class="pi pi-clock text-3xl mb-2"></i>
          <div>Quick add functionality coming soon!</div>
        </div>
      </div>

      <template #footer>
        <Button label="Cancel" severity="secondary" @click="showQuickAdd = false" />
        <Button label="Add Entry" @click="showQuickAdd = false" disabled />
      </template>
    </Dialog>

</template>

<script setup lang="ts">
import { format, addDays, subDays } from 'date-fns'

// Stores
const timesheetStore = useTimesheetStore()
const tasksStore = useTasksStore()

// Composables
const toast = useToast()
const confirm = useConfirm()
const { formatDuration, getCurrentDate } = useTime()

// Reactive state
const selectedDate = ref(new Date())
const showQuickAdd = ref(false)

// Computed properties
const timeBreakdown = computed(() => timesheetStore.timeBreakdownByTask)

const uniqueTasksCount = computed(() => {
  const taskIds = new Set(timesheetStore.timeEntries.map(entry => entry.taskId))
  return taskIds.size
})

const completionPercentage = computed(() => {
  const totalMinutes = timesheetStore.totalTrackedMinutes
  const workingHours = 8 // 8 hour work day
  const totalWorkMinutes = workingHours * 60
  return Math.min(Math.round((totalMinutes / totalWorkMinutes) * 100), 100)
})

// Methods
const formatDateLong = (date: Date) => {
  return format(date, 'EEEE, MMMM do, yyyy')
}

const onDateChange = async (event: any) => {
  selectedDate.value = event
  const dateString = format(event, 'yyyy-MM-dd')
  await timesheetStore.setCurrentDate(dateString)
}

const previousDay = async () => {
  const newDate = subDays(selectedDate.value, 1)
  selectedDate.value = newDate
  await onDateChange(newDate)
}

const nextDay = async () => {
  const newDate = addDays(selectedDate.value, 1)
  selectedDate.value = newDate
  await onDateChange(newDate)
}

const goToToday = async () => {
  const today = new Date()
  selectedDate.value = today
  await onDateChange(today)
}

const refreshTimesheet = async () => {
  await timesheetStore.fetchTimeEntriesForDate()

  if (!timesheetStore.error) {
    toast.add({
      severity: 'success',
      summary: 'Refreshed',
      detail: 'Timesheet data has been refreshed',
      life: 2000
    })
  }
}

const exportToday = () => {
  const dateString = format(selectedDate.value, 'yyyy-MM-dd')
  const csvData = timesheetStore.timeEntries
      .filter(entry => entry.entryDate === dateString)
      .map(entry => ({
        Date: entry.entryDate,
        Task: entry.taskTitle,
        Category: entry.categoryTitle,
        'Start Time': entry.startTime,
        'End Time': entry.endTime,
        'Duration (minutes)': entry.durationMinutes,
        'Duration (formatted)': formatDuration(entry.durationMinutes),
        Description: entry.description || ''
      }))

  if (csvData.length === 0) {
    toast.add({
      severity: 'warn',
      summary: 'No Data',
      detail: 'No time entries to export for this date',
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
  link.download = `timesheet-${dateString}.csv`
  link.click()
  window.URL.revokeObjectURL(url)

  toast.add({
    severity: 'success',
    summary: 'Export Complete',
    detail: `Exported ${csvData.length} time entries`,
    life: 3000
  })
}

const clearDay = () => {
  const entriesForDay = timesheetStore.timeEntries.filter(
      entry => entry.entryDate === format(selectedDate.value, 'yyyy-MM-dd')
  )

  if (entriesForDay.length === 0) {
    toast.add({
      severity: 'info',
      summary: 'Nothing to Clear',
      detail: 'No time entries found for this date',
      life: 2000
    })
    return
  }

  confirm.require({
    message: `Are you sure you want to delete all ${entriesForDay.length} time entries for ${formatDateLong(selectedDate.value)}?\n\nThis action cannot be undone.`,
    header: 'Clear All Time Entries',
    icon: 'pi pi-exclamation-triangle',
    rejectProps: {
      label: 'Cancel',
      severity: 'secondary',
      outlined: true
    },
    acceptProps: {
      label: 'Clear All',
      severity: 'danger'
    },
    accept: async () => {
      try {
        const deletePromises = entriesForDay.map(entry =>
            timesheetStore.deleteTimeEntry(entry.id)
        )
        await Promise.all(deletePromises)

        toast.add({
          severity: 'success',
          summary: 'Day Cleared',
          detail: `Deleted ${entriesForDay.length} time entries`,
          life: 3000
        })
      } catch (error: any) {
        toast.add({
          severity: 'error',
          summary: 'Clear Failed',
          detail: error.message || 'Failed to clear time entries',
          life: 5000
        })
      }
    }
  })
}

const showPlaceholder = (feature: string) => {
  toast.add({
    severity: 'info',
    summary: 'Coming Soon',
    detail: `${feature} functionality will be available in a future update`,
    life: 3000
  })
}

// Keyboard shortcuts
const handleKeydown = (event: KeyboardEvent) => {
  if (event.target && (event.target as HTMLElement).tagName === 'INPUT') {
    return
  }

  switch (event.key) {
    case 'ArrowLeft':
      if (event.altKey) {
        event.preventDefault()
        previousDay()
      }
      break
    case 'ArrowRight':
      if (event.altKey) {
        event.preventDefault()
        nextDay()
      }
      break
    case 't':
    case 'T':
      if (event.ctrlKey || event.metaKey) {
        event.preventDefault()
        goToToday()
      }
      break
  }
}

// Initialize
onMounted(async () => {
  selectedDate.value = new Date()
  await timesheetStore.setCurrentDate(format(new Date(), "yyyy-MM-dd"))

  // Initialize timesheet
  timesheetStore.initializeTimeSlots()

  // Load data
  await Promise.all([
    tasksStore.fetchTasks(),
    timesheetStore.fetchTimeEntriesForDate()
  ])

  // Add keyboard listeners
  document.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
})

// Set page title
useHead({
  title: 'Time Tracker - Visual Time Tracking'
})
</script>

<style scoped>
/* Component-specific styles */
.menubar {
  border: none !important;
  background: transparent !important;
  padding: 0 !important;
}

/* Ensure sticky header works properly */
.sticky {
  position: sticky;
  top: 0;
  z-index: 10;
}

/* Custom scrollbar for timesheet area */
.overflow-auto::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

.overflow-auto::-webkit-scrollbar-track {
  background: #f1f5f9;
}

.overflow-auto::-webkit-scrollbar-thumb {
  background: #94a3b8;
  border-radius: 4px;
}

.overflow-auto::-webkit-scrollbar-thumb:hover {
  background: #64748b;
}
</style>