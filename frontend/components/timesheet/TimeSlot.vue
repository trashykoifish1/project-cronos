<template>
  <div
      ref="slotElement"
      class="timesheet-slot"
      :class="{
      'filled': hasTimeEntry,
      'dragging': isSelected,
      'drag-preview': isDragPreview,
      'no-select': timesheetStore.isDragging,
      'mobile-slot': mobile,
      'desktop-slot': !mobile
    }"
      :style="slotStyle"
      @mousedown="!mobile && handleMouseDown($event)"
      @mouseenter="!mobile && handleMouseEnter()"
      @mouseleave="!mobile && handleMouseLeave()"
      @contextmenu.prevent="!mobile && handleRightClick()"
      @touchstart="mobile && handleTouchStart($event)"
      @touchmove="mobile && handleTouchMove($event)"
      @touchstart.capture="mobile && startLongPressTimer()"
      @touchend.capture="mobile && clearLongPressTimer()"
      @touchcancel.capture="mobile && clearLongPressTimer()"
      :title="!mobile ? tooltipText : ''"
  >
    <!-- Time Entry Content -->
    <div v-if="hasTimeEntry" class="slot-content">
      <div class="task-title" :class="{ 'mobile-text': mobile }">
        <i :class="`pi pi-${timeEntry?.taskIcon} text-xs pr-1`"></i>
        {{ !mobile ? timeEntry?.taskTitle: '' }}
      </div>

      <div v-if="isFirstSlotOfEntry && !mobile" class="time-range text-xs opacity-75">
        {{ formatTimeRange }}
      </div>
    </div>

    <!-- Selection Indicator -->
    <div v-if="isSelected" class="selection-indicator" :class="{ 'mobile-indicator': mobile }">
      <i class="pi pi-check text-xs"></i>
    </div>

    <!-- Hover Preview -->
    <div v-if="showHoverPreview" class="hover-preview">
      <div class="text-xs font-medium" :class="{ 'mobile-text': mobile }">
        {{ mobile ? selectedTaskTitle.substring(0, 6) + '...' : selectedTaskTitle }}
      </div>
    </div>

    <!-- Mobile Time Label -->
    <div v-if="mobile && !hasTimeEntry && !isSelected" class="mobile-time-label">
      :{{ props.slot.minute.toString().padStart(2, '0') }}
    </div>
  </div>
</template>

<script setup lang="ts">
import type { TimeSlot } from '~/stores/timesheet'

// Props
interface Props {
  slot: TimeSlot
  isPreview?: boolean
  mobile?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  isPreview: false,
  mobile: false
})

// Emits
interface Emits {
  dragStart: [slot: TimeSlot]
  dragUpdate: [slot: TimeSlot]
  dragEnd: [slot: TimeSlot]
  slotClick: [slot: TimeSlot]
  slotRightClick: [slot: TimeSlot]
}

const emit = defineEmits<Emits>()

// Stores
const timesheetStore = useTimesheetStore()
const tasksStore = useTasksStore()

// Local state
const slotElement = ref<HTMLElement>()
const isHovering = ref(false)

// Computed
const hasTimeEntry = computed(() => !!props.slot.timeEntry)
const timeEntry = computed(() => props.slot.timeEntry)
const isSelected = computed(() => props.slot.isSelected)
const isDragPreview = computed(() => props.isPreview)

const selectedTaskTitle = computed(() => tasksStore.selectedTask?.title || 'Select Task')

const showHoverPreview = computed(() =>
    isHovering.value &&
    !hasTimeEntry.value &&
    !timesheetStore.isDragging &&
    tasksStore.selectedTask
)

// Slot styling
const slotStyle = computed(() => {
  if (hasTimeEntry.value && timeEntry.value) {
    return {
      backgroundColor: timeEntry.value.taskColor,
      borderColor: timeEntry.value.taskColor
    }
  }

  if (isSelected.value && tasksStore.selectedTask) {
    return {
      backgroundColor: tasksStore.selectedTask.color,
      borderColor: tasksStore.selectedTask.color,
      opacity: '0.7'
    }
  }

  if (showHoverPreview.value && tasksStore.selectedTask) {
    return {
      backgroundColor: tasksStore.selectedTask.color,
      borderColor: tasksStore.selectedTask.color,
      opacity: '0.3'
    }
  }

  return {}
})

// Check if this is the first slot of a time entry (for showing time range)
const isFirstSlotOfEntry = computed(() => {
  if (!hasTimeEntry.value || !timeEntry.value) return false
  return timeEntry.value.startTime.substring(0, 5) === props.slot.timeString
})

// Format time range for display
const formatTimeRange = computed(() => {
  if (!timeEntry.value) return ''
  return `${timeEntry.value.startTime} - ${timeEntry.value.endTime}`
})

// Tooltip text
const tooltipText = computed(() => {
  if (hasTimeEntry.value && timeEntry.value) {
    return `${timeEntry.value.taskTitle}\n${timeEntry.value.startTime} - ${timeEntry.value.endTime}\nDuration: ${timeEntry.value.durationMinutes}m`
  }

  if (tasksStore.selectedTask) {
    return `Click and drag to create time entry for: ${tasksStore.selectedTask.title}`
  }

  return 'Select a task from the sidebar to start tracking time'
})

// Mouse event handlers
const handleMouseDown = (event: MouseEvent) => {
  // Prevent default to avoid text selection
  event.preventDefault()

  // Only start drag if we have a selected task and no existing time entry
  if (!tasksStore.selectedTask || hasTimeEntry.value) {
    emit('slotClick', props.slot)
    return
  }

  // Start drag operation
  emit('dragStart', props.slot)

  // Add global mouse event listeners
  document.addEventListener('mousemove', handleGlobalMouseMove)
  document.addEventListener('mouseup', handleGlobalMouseUp)
}

const handleMouseEnter = () => {
  isHovering.value = true

  // Update drag if currently dragging
  if (timesheetStore.isDragging) {
    emit('dragUpdate', props.slot)
  }
}

const handleMouseLeave = () => {
  isHovering.value = false
}

const handleRightClick = () => {
  emit('slotRightClick', props.slot)
}

const handleGlobalMouseMove = (event: MouseEvent) => {
  // Find the slot element under the mouse
  const elementUnderMouse = document.elementFromPoint(event.clientX, event.clientY)
  const slotUnderMouse = elementUnderMouse?.closest('.timesheet-slot')

  if (slotUnderMouse) {
    // Find the corresponding slot data
    const allSlots = document.querySelectorAll('.timesheet-slot')
    const slotIndex = Array.from(allSlots).indexOf(slotUnderMouse as Element)

    if (slotIndex >= 0 && timesheetStore.timeSlots[slotIndex]) {
      emit('dragUpdate', timesheetStore.timeSlots[slotIndex])
    }
  }
}

const handleGlobalMouseUp = () => {
  // Remove global event listeners
  document.removeEventListener('mousemove', handleGlobalMouseMove)
  document.removeEventListener('mouseup', handleGlobalMouseUp)

  // End drag operation
  if (timesheetStore.isDragging) {
    emit('dragEnd', props.slot)
  }
}

// Touch event handlers for mobile
const handleTouchStart = (event: TouchEvent) => {
  if (!props.mobile) return

  event.preventDefault()

  // Only start drag if we have a selected task and no existing time entry
  if (!tasksStore.selectedTask || hasTimeEntry.value) {
    emit('slotClick', props.slot)
    return
  }

  // Start drag operation
  emit('dragStart', props.slot)

  // Add global touch event listeners
  document.addEventListener('touchmove', handleGlobalTouchMove)
  document.addEventListener('touchend', handleGlobalTouchEnd)
}

const handleTouchMove = (event: TouchEvent) => {
  if (!props.mobile) return

  isHovering.value = true

  // Update drag if currently dragging
  if (timesheetStore.isDragging) {
    emit('dragUpdate', props.slot)
  }
}

const handleLongPress = () => {
  if (!props.mobile || !hasTimeEntry.value) return

  // Long press on mobile = right click equivalent
  emit('slotRightClick', props.slot)
}

const handleGlobalTouchMove = (event: TouchEvent) => {
  if (!props.mobile) return

  const touch = event.touches[0]
  const elementUnderTouch = document.elementFromPoint(touch.clientX, touch.clientY)
  const slotUnderTouch = elementUnderTouch?.closest('.timesheet-slot')

  if (slotUnderTouch) {
    // Find the corresponding slot data
    const allSlots = document.querySelectorAll('.timesheet-slot')
    const slotIndex = Array.from(allSlots).indexOf(slotUnderTouch as Element)

    if (slotIndex >= 0 && timesheetStore.timeSlots[slotIndex]) {
      emit('dragUpdate', timesheetStore.timeSlots[slotIndex])
    }
  }
}

const handleGlobalTouchEnd = () => {
  if (!props.mobile) return

  // Remove global event listeners
  document.removeEventListener('touchmove', handleGlobalTouchMove)
  document.removeEventListener('touchend', handleGlobalTouchEnd)

  // End drag operation
  if (timesheetStore.isDragging) {
    emit('dragEnd', props.slot)
  }
}

// Long press detection for mobile
let longPressTimer: NodeJS.Timeout | null = null

const startLongPressTimer = () => {
  if (!props.mobile) return

  longPressTimer = setTimeout(() => {
    handleLongPress()
  }, 500) // 500ms long press
}

const clearLongPressTimer = () => {
  if (longPressTimer) {
    clearTimeout(longPressTimer)
    longPressTimer = null
  }
}

// Cleanup on unmount
onUnmounted(() => {
  document.removeEventListener('mousemove', handleGlobalMouseMove)
  document.removeEventListener('mouseup', handleGlobalMouseUp)
})
</script>

<style scoped>
.slot-content {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 2px;
  text-align: center;
}

.task-title {
  font-size: 0.75rem;
  font-weight: 500;
  line-height: 1;
}

.time-range {
  margin-top: 2px;
  line-height: 1;
}

.selection-indicator {
  position: absolute;
  top: 2px;
  right: 2px;
  background: rgba(59, 130, 246, 0.9);
  color: white;
  border-radius: 50%;
  width: 16px;
  height: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.hover-preview {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2px;
  text-align: center;
  color: white;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
}

.timesheet-slot {
  position: relative;
  overflow: hidden;
}

.timesheet-slot:hover:not(.filled):not(.no-select) {
  transform: scale(1.02);
  z-index: 1;
}

.timesheet-slot.dragging {
  transform: scale(1.05);
  z-index: 2;
}

.timesheet-slot.drag-preview {
  opacity: 0.5;
  transform: scale(0.95);
}

/* Mobile-specific styles */
.mobile-slot {
  min-height: 48px; /* Minimum touch target */
  min-width: 60px;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  user-select: none;
  -webkit-user-select: none;
  -webkit-touch-callout: none;
}

.mobile-slot:active {
  transform: scale(0.95);
  background-color: rgba(59, 130, 246, 0.1);
}

.mobile-slot.filled {
  border-width: 3px;
}

.mobile-text {
  font-size: 0.7rem;
  line-height: 1.1;
  font-weight: 600;
}

.mobile-indicator {
  top: 1px;
  right: 1px;
  width: 14px;
  height: 14px;
}

.mobile-time-label {
  position: absolute;
  bottom: 2px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 0.6rem;
  color: #9ca3af;
  font-weight: 500;
}

/* Desktop styles */
.desktop-slot {
  border: 1px solid #d1d5db;
  min-height: 32px;
  cursor: pointer;
  transition: all 0.15s ease;
}

.desktop-slot:hover:not(.filled):not(.no-select) {
  transform: scale(1.02);
  z-index: 1;
}

.desktop-slot.dragging {
  transform: scale(1.05);
  z-index: 2;
}

.desktop-slot.drag-preview {
  opacity: 0.5;
  transform: scale(0.95);
}

/* Common styles */
.slot-content {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 2px;
  text-align: center;
}

.task-title {
  font-size: 0.75rem;
  font-weight: 500;
  line-height: 1;
}

.selection-indicator {
  position: absolute;
  background: rgba(59, 130, 246, 0.9);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.hover-preview {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2px;
  text-align: center;
  color: white;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
}

.timesheet-slot {
  position: relative;
  overflow: hidden;
}
</style>