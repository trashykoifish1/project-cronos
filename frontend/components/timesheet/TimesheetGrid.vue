<template>
  <div class="timesheet-grid-container">
    <!-- Grid Header -->
    <div class="grid-header mb-4">
      <div class="flex items-center justify-between">
        <div class="flex items-center space-x-2 lg:space-x-4">
          <h3 class="text-base lg:text-lg font-semibold text-app-primary">
            Daily Timesheet
          </h3>
          <div class="text-xs lg:text-sm text-app-secondary">
            {{ formatDate(timesheetStore.currentDate) }}
          </div>
        </div>

        <div class="flex items-center space-x-2 lg:space-x-3">
          <!-- Time Range Info - Hidden on mobile -->
          <div class="hidden lg:block text-sm text-app-secondary">
            {{ formatTime(timesheetStore.startHour) }} - {{ formatTime(timesheetStore.endHour) }}
          </div>

          <!-- Total Time -->
          <div class="px-2 lg:px-3 py-1 bg-app-blue-50 border border-app-blue-200 rounded-lg">
            <span class="text-xs lg:text-sm font-medium text-app-blue-800">
              Total: {{ formatDuration(timesheetStore.totalTrackedMinutes) }}
            </span>
          </div>

          <!-- Refresh Button -->
          <Button
              icon="pi pi-refresh"
              size="small"
              severity="secondary"
              @click="refreshTimesheet"
              :loading="timesheetStore.loading"
              class="w-8 h-8"
          />
        </div>
      </div>
    </div>

    <!-- Task Selection Warning -->
    <div v-if="!tasksStore.selectedTask" class="mb-4">
      <Message severity="warn" :closable="false">
        <template #icon>
          <i class="pi pi-exclamation-triangle"></i>
        </template>
        <span class="text-sm">Please select a task from the sidebar to start tracking time</span>
      </Message>
    </div>

    <!-- Loading State -->
    <div v-if="timesheetStore.loading" class="flex items-center justify-center py-12">
      <div class="text-center">
        <ProgressSpinner size="32px" />
        <div class="text-sm text-app-secondary mt-2">Loading timesheet...</div>
      </div>
    </div>

    <!-- Error State -->
    <div v-else-if="timesheetStore.error" class="mb-4">
      <Message severity="error" :closable="false">
        <template #icon>
          <i class="pi pi-exclamation-triangle"></i>
        </template>
        {{ timesheetStore.error }}
      </Message>
      <Button
          label="Retry"
          icon="pi pi-refresh"
          size="small"
          @click="refreshTimesheet"
          class="mt-2"
      />
    </div>

    <!-- Timesheet Content (when not loading/error) -->
    <div v-else>
      <!-- Mobile Layout (< lg) -->
      <div class="lg:hidden space-y-3">
        <!-- Current Time Indicator -->
        <div class="bg-app-blue-50 border border-app-blue-200 rounded-lg p-3 mb-4">
          <div class="flex items-center justify-between">
            <div class="text-sm font-medium text-app-blue-800">
              {{ getCurrentTimeSlot() }}
            </div>
            <div class="text-xs text-app-blue-600">
              Current time
            </div>
          </div>
        </div>

        <!-- Mobile Hour Cards -->
        <div
            v-for="hour in timesheetStore.hoursRange"
            :key="hour"
            class="bg-app-secondary border border-app-primary rounded-lg p-3 shadow-sm"
        >
          <!-- Hour Header -->
          <div class="flex items-center justify-between mb-3">
            <div class="text-lg font-semibold text-app-primary">
              {{ formatTime(hour) }}
            </div>
            <div class="text-xs text-app-secondary">
              {{ getHourSummary(hour) }}
            </div>
          </div>

          <!-- 15-minute Slots Grid -->
          <div class="grid grid-cols-4 gap-2">
            <TimeSlot
                v-for="minute in [0, 15, 30, 45]"
                :key="`${hour}-${minute}`"
                :slot="getSlot(hour, minute)"
                :mobile="true"
                @drag-start="handleDragStart"
                @drag-update="handleDragUpdate"
                @drag-end="handleDragEnd"
                @slot-click="handleSlotClick"
                @slot-right-click="handleSlotRightClick"
                class="aspect-square min-h-12"
            />
          </div>

          <!-- Time Labels for Mobile -->
          <div class="grid grid-cols-4 gap-2 mt-1">
            <div v-for="minute in [0, 15, 30, 45]" :key="minute" class="text-center">
              <span class="text-xs text-app-secondary">:{{ minute.toString().padStart(2, '0') }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Desktop Layout (>= lg) -->
      <div class="hidden lg:block timesheet-container overflow-auto rounded-lg border border-app-primary">
        <div class="timesheet-grid" :style="gridStyle">
          <!-- Header Row -->
          <div class="timesheet-hour-label text-xs font-semibold">Time</div>
          <div class="bg-app-tertiary px-2 py-2 text-xs font-medium text-center border-r border-app-primary">:00</div>
          <div class="bg-app-tertiary px-2 py-2 text-xs font-medium text-center border-r border-app-primary">:15</div>
          <div class="bg-app-tertiary px-2 py-2 text-xs font-medium text-center border-r border-app-primary">:30</div>
          <div class="bg-app-tertiary px-2 py-2 text-xs font-medium text-center">:45</div>

          <!-- Time Slots -->
          <template v-for="hour in timesheetStore.hoursRange" :key="hour">
            <!-- Hour Label -->
            <div class="timesheet-hour-label">
              {{ formatTime(hour) }}
            </div>

            <!-- 15-minute slots for this hour -->
            <TimeSlot
                v-for="minute in [0, 15, 30, 45]"
                :key="`${hour}-${minute}`"
                :slot="getSlot(hour, minute)"
                :mobile="false"
                @drag-start="handleDragStart"
                @drag-update="handleDragUpdate"
                @drag-end="handleDragEnd"
                @slot-click="handleSlotClick"
                @slot-right-click="handleSlotRightClick"
            />
          </template>
        </div>
      </div>
    </div>

    <!-- Mobile Instructions -->
    <div class="lg:hidden mt-4 text-sm text-app-secondary">
      <div class="flex items-start space-x-2">
        <i class="pi pi-info-circle text-app-blue-500 mt-0.5 flex-shrink-0"></i>
        <div>
          <strong>Instructions:</strong>
          <ul class="mt-1 space-y-1">
            <li>• Select a task first, then tap time slots</li>
            <li>• Tap and hold to drag across multiple slots</li>
            <li>• Long press existing entries to delete</li>
          </ul>
        </div>
      </div>
    </div>

    <!-- Desktop Instructions -->
    <div class="hidden lg:block mt-4 text-sm text-app-secondary">
      <div class="flex items-start space-x-2">
        <i class="pi pi-info-circle text-app-blue-500 mt-0.5"></i>
        <div>
          <strong>Instructions:</strong>
          <ul class="mt-1 space-y-1 list-disc list-inside">
            <li>Select a task from the sidebar first</li>
            <li>Click and drag across time slots to create time entries</li>
            <li>Right-click on existing entries to delete them</li>
            <li>Each slot represents 15 minutes</li>
          </ul>
        </div>
      </div>
    </div>

    <!-- Debug Info (Development) -->
    <div v-if="showDebugInfo" class="mt-6 p-4 bg-app-primary border border-app-primary rounded-lg">
      <h4 class="font-semibold text-app-primary mb-2">Debug Information</h4>
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-4 text-sm">
        <div>
          <strong>Drag State:</strong>
          <div>Is Dragging: {{ timesheetStore.isDragging }}</div>
          <div>Selected Slots: {{ timesheetStore.selectedSlots.length }}</div>
          <div>Selected Task: {{ tasksStore.selectedTask?.title || 'None' }}</div>
        </div>
        <div>
          <strong>Data:</strong>
          <div>Time Entries: {{ timesheetStore.timeEntries.length }}</div>
          <div>Time Slots: {{ timesheetStore.timeSlots.length }}</div>
          <div>Date: {{ timesheetStore.currentDate }}</div>
        </div>
      </div>
    </div>

    <!-- Context Menu for Time Entries -->
    <ContextMenu ref="contextMenu" :model="contextMenuItems" />
  </div>
</template>

<script setup lang="ts">
import { format } from 'date-fns'
import type { TimeSlot } from '~/stores/timesheet'

// Props
interface Props {
  showDebugInfo?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  showDebugInfo: false
})

// Stores
const timesheetStore = useTimesheetStore()
const tasksStore = useTasksStore()

// Composables
const toast = useToast()
const confirm = useConfirm()
const { formatDuration } = useTime()

// Refs
const contextMenu = ref()
const selectedSlotForContext = ref<TimeSlot | null>(null)

// Computed
const gridStyle = computed(() => ({
  gridTemplateColumns: `80px repeat(4, 1fr)`
}))

// Context menu items
const contextMenuItems = computed(() => [
  {
    label: 'Delete Time Entry',
    icon: 'pi pi-trash',
    command: () => deleteTimeEntry()
  },
  {
    label: 'Edit Time Entry',
    icon: 'pi pi-pencil',
    command: () => editTimeEntry(),
    disabled: true // Placeholder for future functionality
  }
])

// Methods
const formatDate = (dateString: string) => {
  return format(new Date(dateString + 'T00:00:00'), 'EEEE, MMMM do, yyyy')
}

const formatTime = (hour: number) => {
  return `${hour.toString().padStart(2, '0')}:00`
}

const getSlot = (hour: number, minute: number): TimeSlot => {
  const timeString = `${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`
  return timesheetStore.getSlotByTime(timeString) || {
    hour,
    minute,
    timeString,
    timeEntry: null,
    isSelected: false
  }
}

// Mobile-specific helper methods
const getCurrentTimeSlot = () => {
  const now = new Date()
  const hour = now.getHours()
  const minute = Math.floor(now.getMinutes() / 15) * 15
  return `${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`
}

const getHourSummary = (hour: number) => {
  const hourSlots = [0, 15, 30, 45].map(minute => getSlot(hour, minute))
  const filledSlots = hourSlots.filter(slot => slot.timeEntry)

  if (filledSlots.length === 0) return 'Empty'
  if (filledSlots.length === 4) return 'Full hour'

  const totalMinutes = filledSlots.length * 15
  return `${totalMinutes}min tracked`
}

const refreshTimesheet = async () => {
  await timesheetStore.fetchTimeEntriesForDate()

  if (!timesheetStore.error) {
    toast.add({
      severity: 'success',
      summary: 'Timesheet Refreshed',
      detail: `Loaded ${timesheetStore.timeEntries.length} time entries`,
      life: 2000
    })
  }
}

// Drag handlers
const handleDragStart = (slot: TimeSlot) => {
  if (!tasksStore.selectedTask) {
    toast.add({
      severity: 'warn',
      summary: 'No Task Selected',
      detail: 'Please select a task from the sidebar first',
      life: 3000
    })
    return
  }

  timesheetStore.startDrag(slot)
}

const handleDragUpdate = (slot: TimeSlot) => {
  timesheetStore.updateDrag(slot)
}

const handleDragEnd = async (slot: TimeSlot) => {
  try {
    const newEntry = await timesheetStore.endDrag()
    if (newEntry) {
      toast.add({
        severity: 'success',
        summary: 'Time Entry Created',
        detail: `Created ${newEntry.durationMinutes}m entry for ${newEntry.taskTitle}`,
        life: 3000
      })
    }
  } catch (error: any) {
    toast.add({
      severity: 'error',
      summary: 'Failed to Create Entry',
      detail: error.message || 'Unknown error occurred',
      life: 5000
    })

    timesheetStore.cancelDrag()
  }
}

// Click handlers
const handleSlotClick = (slot: TimeSlot) => {
  if (slot.timeEntry) {
    // Show time entry details
    toast.add({
      severity: 'info',
      summary: 'Time Entry Details',
      detail: `${slot.timeEntry.taskTitle}: ${slot.timeEntry.startTime} - ${slot.timeEntry.endTime}`,
      life: 3000
    })
  } else if (!tasksStore.selectedTask) {
    toast.add({
      severity: 'warn',
      summary: 'No Task Selected',
      detail: 'Please select a task from the sidebar to start tracking time',
      life: 3000
    })
  }
}

const handleSlotRightClick = (slot: TimeSlot) => {
  if (slot.timeEntry) {
    selectedSlotForContext.value = slot
    contextMenu.value.show(event)
  }
}

// Context menu actions
const deleteTimeEntry = () => {
  if (!selectedSlotForContext.value?.timeEntry) return

  const timeEntry = selectedSlotForContext.value.timeEntry

  confirm.require({
    message: `Are you sure you want to delete this time entry?\n\n${timeEntry.taskTitle}\n${timeEntry.startTime} - ${timeEntry.endTime}`,
    header: 'Delete Time Entry',
    icon: 'pi pi-exclamation-triangle',
    rejectProps: {
      label: 'Cancel',
      severity: 'secondary',
      outlined: true
    },
    acceptProps: {
      label: 'Delete',
      severity: 'danger'
    },
    accept: async () => {
      try {
        await timesheetStore.deleteTimeEntry(timeEntry.id)

        toast.add({
          severity: 'success',
          summary: 'Time Entry Deleted',
          detail: 'Time entry has been removed',
          life: 2000
        })
      } catch (error: any) {
        toast.add({
          severity: 'error',
          summary: 'Delete Failed',
          detail: error.message || 'Failed to delete time entry',
          life: 5000
        })
      }
    }
  })
}

const editTimeEntry = () => {
  // Placeholder for future edit functionality
  toast.add({
    severity: 'info',
    summary: 'Coming Soon',
    detail: 'Edit functionality will be added in future updates',
    life: 3000
  })
}

// Initialize on mount
onMounted(async () => {
  // Initialize time slots
  timesheetStore.initializeTimeSlots()

  // Load time entries for current date
  await timesheetStore.fetchTimeEntriesForDate()
})

// Cleanup on unmount
onUnmounted(() => {
  timesheetStore.reset()
})
</script>

<style scoped>
.timesheet-grid-container {
  width: 100%;
  max-width: 100%;
}

.timesheet-grid {
  min-width: 600px;
  width: 100%;
}

.timesheet-container {
  max-height: 600px;
}

@media (max-width: 768px) {
  .timesheet-container {
    max-height: 400px;
  }

  .timesheet-grid {
    min-width: 500px;
  }
}
</style>