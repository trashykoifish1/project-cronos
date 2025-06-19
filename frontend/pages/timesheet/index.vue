<template>
  <!-- Main Content Area -->
  <div class="flex flex-1 overflow-hidden relative">
    <!-- Mobile Task Sidebar Overlay -->
    <div
        v-if="showMobileSidebar"
        class="fixed inset-0 bg-black bg-opacity-50 z-40 lg:hidden"
        @click="showMobileSidebar = false"
    >
      <div
          class="bg-app-secondary w-80 h-full shadow-xl transform transition-transform duration-300"
          @click.stop
      >
        <!-- Mobile Sidebar Header -->
        <div class="flex items-center justify-between p-4 border-b border-app-primary">
          <h3 class="text-lg font-semibold text-app-primary">Select Task</h3>
          <Button
              icon="pi pi-times"
              severity="secondary"
              text
              @click="showMobileSidebar = false"
          />
        </div>

        <!-- Mobile Sidebar Content -->
        <div class="h-full pb-16 flex flex-col">
          <!-- Task Selection -->
          <div class="flex-1 min-h-0">
            <TaskSidebar @task-selected="showMobileSidebar = false" />
          </div>

          <!-- Spotify Card for Mobile -->
          <div class="flex-shrink-0 border-t border-app-primary">
            <div style="padding: 1rem;">
              <SpotifyCard @open-settings="handleOpenSettings" />
            </div>
          </div>
        </div>
      </div>
    </div>


    <!-- Desktop Task Sidebar -->
    <div class="hidden lg:block w-80 flex-shrink-0 bg-app-primary border-r border-app-primary">
      <div class="h-full flex flex-col">
        <!-- Spotify Integration Section -->
        <div class="flex-shrink-0 border-t border-app-primary">
          <div style="padding: 1rem;">
            <SpotifyCard @open-settings="handleOpenSettings" />
          </div>
        </div>
        <!-- Task Selection Section -->
        <div class="flex-1 min-h-0">
          <TaskSidebar />
        </div>


      </div>
    </div>

    <!-- Timesheet Area -->
    <div class="flex-1 flex flex-col min-w-0">
      <!-- Current Day Header -->
      <div class="bg-app-secondary border-b border-app-primary p-4 lg:p-6">
        <!-- Mobile Header -->
        <div class="lg:hidden">
          <!-- Mobile Top Row: Task Button + Date -->
          <div class="flex items-center justify-between mb-3">
            <Button
                @click="showMobileSidebar = true"
                icon="pi pi-bookmark"
                label="Tasks"
                severity="info"
                class="flex-shrink-0"
            />

            <div class="flex items-center space-x-1 flex-1 justify-center">
              <Button
                  icon="pi pi-chevron-left"
                  severity="secondary"
                  text
                  @click="previousDay"
                  class="w-10 h-10"
              />

              <DatePicker
                  v-model="selectedDate"
                  @date-select="onDateChange"
                  showIcon
                  dateFormat="M dd, yy"
                  placeholder="Select Date"
                  class="w-32 text-center"
                  inputClass="text-sm text-center"
              />

              <Button
                  icon="pi pi-chevron-right"
                  severity="secondary"
                  text
                  @click="nextDay"
                  class="w-10 h-10"
              />
            </div>

            <Button
                @click="showMobileActions = !showMobileActions"
                icon="pi pi-ellipsis-v"
                severity="secondary"
                text
                class="w-10 h-10"
            />
          </div>

          <!-- Mobile Actions Panel -->
          <div v-if="showMobileActions" class="bg-app-primary rounded-lg p-3 mb-3">
            <div class="grid grid-cols-3 gap-2">
              <Button
                  @click="showQuickAdd = true; showMobileActions = false"
                  label="Add"
                  icon="pi pi-plus"
                  size="small"
                  severity="info"
                  class="text-xs"
              />
              <Button
                  @click="exportToday; showMobileActions = false"
                  label="Export"
                  icon="pi pi-download"
                  size="small"
                  severity="secondary"
                  class="text-xs"
              />
              <Button
                  @click="refreshTimesheet; showMobileActions = false"
                  icon="pi pi-refresh"
                  size="small"
                  severity="secondary"
                  :loading="timesheetStore.loading"
                  class="text-xs"
              />
            </div>
          </div>

          <p class="text-app-secondary text-sm text-center">
            Tap Tasks to select, then paint time slots
          </p>
        </div>

        <!-- Desktop Header -->
        <div class="hidden lg:block">
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
              <p class="text-app-secondary mt-1">
                Track your time by selecting a task and painting time slots
              </p>
            </div>

            <!-- Desktop Quick Actions -->
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
        </div>

          <!-- Daily Statistics -->
        <div class="mt-4 lg:mt-6 grid grid-cols-2 lg:grid-cols-4 gap-3 lg:gap-4">
          <div class="bg-app-blue-50 rounded-lg p-3 lg:p-4 text-center">
            <div class="text-lg lg:text-2xl font-bold text-app-blue-600">
              {{ formatDuration(timesheetStore.totalTrackedMinutes) }}
            </div>
            <div class="text-xs lg:text-sm text-app-blue-800">Total Tracked</div>
          </div>

          <div class="bg-app-green-50 rounded-lg p-3 lg:p-4 text-center">
            <div class="text-lg lg:text-2xl font-bold text-app-green-600">
              {{ timesheetStore.timeEntries.length }}
            </div>
            <div class="text-xs lg:text-sm text-app-green-800">Time Entries</div>
          </div>

          <div class="bg-app-purple-50 rounded-lg p-3 lg:p-4 text-center">
            <div class="text-lg lg:text-2xl font-bold text-app-purple-600">
              {{ uniqueTasksCount }}
            </div>
            <div class="text-xs lg:text-sm text-app-purple-800">Tasks Used</div>
          </div>

          <div class="bg-app-orange-50 rounded-lg p-3 lg:p-4 text-center">
            <div class="text-lg lg:text-2xl font-bold text-app-orange-600">
              {{ completionPercentage }}%
            </div>
            <div class="text-xs lg:text-sm text-app-orange-800">Day Progress</div>
          </div>
        </div>

        <!-- Timesheet Grid -->
        <div class="flex-1 overflow-auto p-2 lg:p-6">
          <div class="bg-app-secondary rounded-lg border border-app-primary p-2 lg:p-4">
            <TimesheetGrid />
          </div>
        </div>

        <!-- Time Breakdown Footer -->
        <div class="bg-app-secondary border-t border-app-gray-primary p-2 lg:p-4">
          <div class="block lg:flex lg:items-center lg:justify-between">
            <!-- Time Breakdown by Task -->
            <div class="mb-3 lg:mb-0">
              <span class="text-sm font-medium text-app-primary block lg:inline">Today's Breakdown:</span>
              <div class="flex items-center space-x-2 lg:space-x-4 flex-wrap mt-2 lg:mt-0 lg:ml-4">
                <div
                    v-for="(breakdown, taskId) in timeBreakdown"
                    :key="taskId"
                    class="flex items-center space-x-1 lg:space-x-2 px-2 lg:px-3 py-1 bg-app-primary rounded-full"
                >
                  <div
                      class="w-2 h-2 lg:w-3 lg:h-3 rounded-full"
                      :style="{ backgroundColor: breakdown.color }"
                  ></div>
                  <span class="text-xs lg:text-sm font-medium text-app-primary truncate max-w-20 lg:max-w-none">
            {{ breakdown.taskTitle }}
          </span>
                  <span class="text-xs lg:text-sm text-app-secondary">
            {{ formatDuration(breakdown.minutes) }}
          </span>
                </div>

                <div v-if="Object.keys(timeBreakdown).length === 0" class="text-xs lg:text-sm text-app-secondary">
                  No time entries yet - start by selecting a task and painting time slots!
                </div>
              </div>
            </div>

            <!-- Action Buttons -->
            <div class="flex items-center justify-center lg:justify-end">
              <Button
                  @click="clearDay"
                  label="Clear Day"
                  icon="pi pi-trash"
                  size="small"
                  severity="danger"
                  :disabled="timesheetStore.timeEntries.length === 0"
                  class="text-xs lg:text-sm"
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
        <div class="text-sm text-app-secondary">
          Quickly add a time entry for today without using the timesheet grid.
        </div>
        <!-- Quick add form would go here -->
        <div class="text-center py-8 text-app-secondary">
          <i class="pi pi-clock text-3xl mb-2"></i>
          <div>Quick add functionality coming soon!</div>
        </div>
      </div>

      <template #footer>
        <Button label="Cancel" severity="secondary" @click="showQuickAdd = false" />
        <Button label="Add Entry" @click="showQuickAdd = false" disabled />
      </template>
    </Dialog>
  </div>
</template>

<script setup lang="ts">
import { format, addDays, subDays } from 'date-fns'
import {useSpotifyStore} from "~/stores/spotify";

// Stores
const timesheetStore = useTimesheetStore()
const tasksStore = useTasksStore()
const spotifyStore = useSpotifyStore()

// Composables
const toast = useToast()
const confirm = useConfirm()
const { formatDuration, getCurrentDate } = useTime()

// Reactive state
const selectedDate = ref(new Date())
const showQuickAdd = ref(false)
const showMobileSidebar = ref(false)
const showMobileActions = ref(false)

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

const handleOpenSettings = () => {
  // For now, show a helpful toast since we can't directly open the settings dialog from here
  // In a real implementation, you might use a global event bus or store
  toast.add({
    severity: 'info',
    summary: 'Open Settings',
    detail: 'Click the settings (⚙️) icon in the top navigation bar to connect Spotify',
    life: 5000
  })
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
  title: 'Project Cronos - Visual Time Tracking'
})
</script>

<style scoped>
/* Component-specific styles */

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