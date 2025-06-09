<template>
  <div class="min-h-screen bg-gray-50 flex">
    <!-- Task Sidebar -->
    <div class="w-80 flex-shrink-0">
      <TaskSidebar />
    </div>

    <!-- Main Timesheet Area -->
    <div class="flex-1 flex flex-col">
      <!-- Header -->
      <div class="bg-white border-b border-gray-200 p-6">
        <div class="flex items-center justify-between">
          <div>
            <h1 class="text-2xl font-bold text-gray-900">Time Tracker</h1>
            <p class="text-gray-600">Drag and paint your time entries</p>
          </div>

          <div class="flex items-center space-x-4">
            <!-- Date Navigation -->
            <div class="flex items-center space-x-2">
              <Button
                  icon="pi pi-chevron-left"
                  size="small"
                  severity="secondary"
                  @click="previousDay"
                  class="w-8 h-8"
              />

              <InputText
                  v-model="dateInputValue"
                  @keyup.enter="onDateInputChange"
                  placeholder="YYYY-MM-DD"
                  class="w-48 text-center"
              />

              <Button
                  icon="pi pi-chevron-right"
                  size="small"
                  severity="secondary"
                  @click="nextDay"
                  class="w-8 h-8"
              />
            </div>

            <!-- Today Button -->
            <Button
                label="Today"
                icon="pi pi-calendar"
                size="small"
                @click="goToToday"
            />
          </div>
        </div>

        <!-- Quick Stats -->
        <div class="mt-4 grid grid-cols-3 gap-4">
          <div class="text-center p-3 bg-blue-50 rounded-lg">
            <div class="text-lg font-semibold text-blue-600">
              {{ formatDuration(timesheetStore.totalTrackedMinutes) }}
            </div>
            <div class="text-sm text-blue-800">Total Tracked</div>
          </div>

          <div class="text-center p-3 bg-green-50 rounded-lg">
            <div class="text-lg font-semibold text-green-600">
              {{ timesheetStore.timeEntries.length }}
            </div>
            <div class="text-sm text-green-800">Time Entries</div>
          </div>

          <div class="text-center p-3 bg-purple-50 rounded-lg">
            <div class="text-lg font-semibold text-purple-600">
              {{ uniqueTasksCount }}
            </div>
            <div class="text-sm text-purple-800">Tasks Used</div>
          </div>
        </div>
      </div>

      <!-- Timesheet Grid Container -->
      <div class="flex-1 overflow-auto p-6">
        <TimesheetGrid :show-debug-info="showDebugInfo" />
      </div>

      <!-- Bottom Panel -->
      <div class="bg-white border-t border-gray-200 p-4">
        <div class="flex items-center justify-between">
          <!-- Time Breakdown -->
          <div class="flex items-center space-x-4">
            <span class="text-sm font-medium text-gray-700">Time by Task:</span>
            <div class="flex items-center space-x-3">
              <div
                  v-for="(breakdown, taskId) in timeBreakdown"
                  :key="taskId"
                  class="flex items-center space-x-1"
              >
                <div
                    class="w-3 h-3 rounded"
                    :style="{ backgroundColor: breakdown.color }"
                ></div>
                <span class="text-sm text-gray-600">
                  {{ breakdown.taskTitle }}: {{ formatDuration(breakdown.minutes) }}
                </span>
              </div>
            </div>
          </div>

          <!-- Actions -->
          <div class="flex items-center space-x-3">
            <Button
                @click="showDebugInfo = !showDebugInfo"
                :label="showDebugInfo ? 'Hide Debug' : 'Show Debug'"
                icon="pi pi-code"
                size="small"
                severity="secondary"
            />

            <Button
                @click="exportTimesheet"
                label="Export CSV"
                icon="pi pi-download"
                size="small"
                severity="info"
            />

            <Button
                @click="clearAllEntries"
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

    <!-- Floating Help Button -->
    <div class="fixed bottom-6 right-6">
      <Button
          @click="showHelp = true"
          icon="pi pi-question-circle"
          severity="help"
          class="w-12 h-12 rounded-full shadow-lg"
      />
    </div>

    <!-- Help Dialog -->
    <Dialog
        v-model:visible="showHelp"
        modal
        header="How to Use Time Tracker"
        :style="{ width: '500px' }"
    >
      <div class="space-y-4">
        <div>
          <h4 class="font-semibold text-gray-800 mb-2">Getting Started</h4>
          <ol class="list-decimal list-inside space-y-1 text-sm text-gray-600">
            <li>Select a task from the sidebar on the left</li>
            <li>Click and drag across time slots to create time entries</li>
            <li>Use the date picker to navigate between days</li>
            <li>Right-click on existing entries to delete them</li>
          </ol>
        </div>

        <div>
          <h4 class="font-semibold text-gray-800 mb-2">Tips</h4>
          <ul class="list-disc list-inside space-y-1 text-sm text-gray-600">
            <li>Each slot represents 15 minutes of time</li>
            <li>You can create entries spanning multiple hours</li>
            <li>Different tasks are shown in different colors</li>
            <li>The grid shows time from 7 AM to 5 PM</li>
          </ul>
        </div>

        <div>
          <h4 class="font-semibold text-gray-800 mb-2">Keyboard Shortcuts</h4>
          <ul class="list-disc list-inside space-y-1 text-sm text-gray-600">
            <li><kbd class="px-1 py-0.5 bg-gray-100 rounded text-xs">←/→</kbd> Navigate days</li>
            <li><kbd class="px-1 py-0.5 bg-gray-100 rounded text-xs">T</kbd> Go to today</li>
            <li><kbd class="px-1 py-0.5 bg-gray-100 rounded text-xs">Esc</kbd> Cancel drag operation</li>
          </ul>
        </div>
      </div>

      <template #footer>
        <Button
            label="Got it!"
            @click="showHelp = false"
            class="w-full"
        />
      </template>
    </Dialog>
  </div>
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

// Local state
const selectedDate = ref(new Date())
const dateInputValue = ref('')
const showDebugInfo = ref(false)
const showHelp = ref(false)

// Computed
const timeBreakdown = computed(() => timesheetStore.timeBreakdownByTask)

const uniqueTasksCount = computed(() => {
  const taskIds = new Set(timesheetStore.timeEntries.map(entry => entry.taskId))
  return taskIds.size
})

// Methods
const onDateChange = async (event: any) => {
  const date = format(event, 'yyyy-MM-dd')
  await timesheetStore.setCurrentDate(date)
  dateInputValue.value = date

  toast.add({
    severity: 'info',
    summary: 'Date Changed',
    detail: `Switched to ${format(event, 'MMMM do, yyyy')}`,
    life: 2000
  })
}

const onDateInputChange = async () => {
  try {
    const date = new Date(dateInputValue.value)
    if (!isNaN(date.getTime())) {
      selectedDate.value = date
      await onDateChange(date)
    } else {
      toast.add({
        severity: 'error',
        summary: 'Invalid Date',
        detail: 'Please enter a valid date in YYYY-MM-DD format',
        life: 3000
      })
    }
  } catch (error) {
    toast.add({
      severity: 'error',
      summary: 'Invalid Date',
      detail: 'Please enter a valid date in YYYY-MM-DD format',
      life: 3000
    })
  }
}

const previousDay = async () => {
  const newDate = subDays(selectedDate.value, 1)
  selectedDate.value = newDate
  dateInputValue.value = format(newDate, 'yyyy-MM-dd')
  await onDateChange(newDate)
}

const nextDay = async () => {
  const newDate = addDays(selectedDate.value, 1)
  selectedDate.value = newDate
  dateInputValue.value = format(newDate, 'yyyy-MM-dd')
  await onDateChange(newDate)
}

const goToToday = async () => {
  const today = new Date()
  selectedDate.value = today
  dateInputValue.value = format(today, 'yyyy-MM-dd')
  await onDateChange(today)
}

const exportTimesheet = () => {
  // Simulate CSV export
  const csvData = timesheetStore.timeEntries.map(entry => ({
    Date: entry.entryDate,
    Task: entry.taskTitle,
    Category: entry.categoryTitle,
    'Start Time': entry.startTime,
    'End Time': entry.endTime,
    'Duration (minutes)': entry.durationMinutes,
    Description: entry.description || ''
  }))

  const csvContent = [
    Object.keys(csvData[0] || {}).join(','),
    ...csvData.map(row => Object.values(row).join(','))
  ].join('\n')

  // Create download link
  const blob = new Blob([csvContent], { type: 'text/csv' })
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `timesheet-${timesheetStore.currentDate}.csv`
  link.click()
  window.URL.revokeObjectURL(url)

  toast.add({
    severity: 'success',
    summary: 'Export Complete',
    detail: 'Timesheet exported as CSV',
    life: 3000
  })
}

const clearAllEntries = () => {
  confirm.require({
    message: `Are you sure you want to delete all time entries for ${format(selectedDate.value, 'MMMM do, yyyy')}?\n\nThis action cannot be undone.`,
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
        // Delete all entries for the current date
        const deletePromises = timesheetStore.timeEntries
            .filter(entry => entry.entryDate === timesheetStore.currentDate)
            .map(entry => timesheetStore.deleteTimeEntry(entry.id))

        await Promise.all(deletePromises)

        toast.add({
          severity: 'success',
          summary: 'Day Cleared',
          detail: 'All time entries have been deleted',
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

// Keyboard shortcuts
const handleKeydown = (event: KeyboardEvent) => {
  if (event.target && (event.target as HTMLElement).tagName === 'INPUT') {
    return // Don't handle shortcuts when typing in inputs
  }

  switch (event.key) {
    case 'ArrowLeft':
      event.preventDefault()
      previousDay()
      break
    case 'ArrowRight':
      event.preventDefault()
      nextDay()
      break
    case 't':
    case 'T':
      event.preventDefault()
      goToToday()
      break
    case 'Escape':
      if (timesheetStore.isDragging) {
        timesheetStore.cancelDrag()
      }
      break
  }
}

// Initialize
onMounted(async () => {
  // Set initial date
  const today = getCurrentDate()
  selectedDate.value = new Date(today)
  dateInputValue.value = today
  await timesheetStore.setCurrentDate(today)

  // Add keyboard listeners
  document.addEventListener('keydown', handleKeydown)

  // Load tasks if not already loaded
  if (tasksStore.tasks.length === 0) {
    await tasksStore.fetchTasks()
  }
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
})

useHead({
  title: 'Timesheet - Time Tracker'
})
</script>

<style scoped>
kbd {
  font-family: monospace;
  font-size: 0.75rem;
}
</style>