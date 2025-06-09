import { defineStore } from 'pinia'
import { format } from 'date-fns'
import { useReportsStore } from '~/stores/reports'

import type { TimeEntry, TimeEntryCreateRequest, TimeEntryUpdateRequest } from '~/types/api'

export interface TimeSlot {
    hour: number
    minute: number
    timeString: string // "07:00", "07:15", etc.
    timeEntry: TimeEntry | null
    isSelected: boolean
}

export const useTimesheetStore = defineStore('timesheet', {
    state: () => ({
        currentDate: format(new Date(), 'yyyy-MM-dd'),
        timeEntries: [] as TimeEntry[],
        timeSlots: [] as TimeSlot[],
        loading: false,
        error: null as string | null,

        // Drag state
        isDragging: false,
        dragStartSlot: null as TimeSlot | null,
        dragEndSlot: null as TimeSlot | null,
        selectedSlots: [] as TimeSlot[],

        // Time range (7am to 5pm = 10 hours)
        startHour: 7,
        endHour: 17, // 5pm (exclusive, so 16:45 is the last slot)
        intervalMinutes: 15
    }),

    getters: {
        // Get time slot by time string
        getSlotByTime: (state) => (timeString: string) =>
            state.timeSlots.find(slot => slot.timeString === timeString) || null,

        // Get time entry for specific slot
        getTimeEntryForSlot: (state) => (slot: TimeSlot) => {
            return state.timeEntries.find(entry => {
                if (entry.entryDate !== state.currentDate) return false

                // Normalize time formats - strip seconds from API times
                const entryStart = entry.startTime.substring(0, 5) // "07:00:00" -> "07:00"
                const entryEnd = entry.endTime.substring(0, 5)     // "07:15:00" -> "07:15"
                return entryStart <= slot.timeString && slot.timeString < entryEnd
            }) || null
        },

        // Calculate total tracked time for the day
        totalTrackedMinutes: (state) => {
            return state.timeEntries.reduce((total, entry) => {
                if (entry.entryDate === state.currentDate) {
                    return total + entry.durationMinutes
                }
                return total
            }, 0)
        },

        // Get time breakdown by task
        timeBreakdownByTask: (state) => {
            const breakdown: Record<string, { minutes: number; color: string; taskTitle: string }> = {}

            state.timeEntries.forEach(entry => {
                if (entry.entryDate === state.currentDate) {
                    if (!breakdown[entry.taskId]) {
                        breakdown[entry.taskId] = {
                            minutes: 0,
                            color: entry.taskColor,
                            taskTitle: entry.taskTitle
                        }
                    }
                    breakdown[entry.taskId].minutes += entry.durationMinutes
                }
            })

            return breakdown
        },

        // Check if any slots are selected for dragging
        hasSelectedSlots: (state) => state.selectedSlots.length > 0,

        // Get hours array for the current range
        hoursRange: (state) => {
            const hours = []
            for (let hour = state.startHour; hour < state.endHour; hour++) {
                hours.push(hour)
            }
            return hours
        }
    },

    actions: {
        // Initialize time slots for the current time range
        initializeTimeSlots() {
            this.timeSlots = []

            for (let hour = this.startHour; hour < this.endHour; hour++) {
                for (let minute = 0; minute < 60; minute += this.intervalMinutes) {
                    const timeString = `${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`

                    this.timeSlots.push({
                        hour,
                        minute,
                        timeString,
                        timeEntry: null,
                        isSelected: false
                    })
                }
            }

            this.updateSlotTimeEntries()
        },

        // Update slots with current time entries
        updateSlotTimeEntries() {
            this.timeSlots.forEach(slot => {
                slot.timeEntry = this.getTimeEntryForSlot(slot)
            })
        },

        // Set current date and reload data
        async setCurrentDate(date: string) {
            this.currentDate = date
            this.clearSelection()
            await this.fetchTimeEntriesForDate()
        },

        // Fetch time entries for current date
        async fetchTimeEntriesForDate() {
            this.loading = true
            this.error = null

            try {
                const { getTimeEntriesForDate } = useTimeEntriesApi()
                this.timeEntries = await getTimeEntriesForDate(this.currentDate)
                this.updateSlotTimeEntries()
            } catch (error: any) {
                this.error = error.message || 'Failed to fetch time entries'
                console.error('Error fetching time entries:', error)
            } finally {
                this.loading = false
            }
        },

        // Start drag operation
        startDrag(slot: TimeSlot) {
            this.isDragging = true
            this.dragStartSlot = slot
            this.dragEndSlot = slot
            this.selectedSlots = [slot]
            slot.isSelected = true
        },

        // Update drag operation
        updateDrag(slot: TimeSlot) {
            if (!this.isDragging || !this.dragStartSlot) return

            this.dragEndSlot = slot
            this.clearSelection()

            // Select all slots between start and end
            const startIndex = this.timeSlots.indexOf(this.dragStartSlot)
            const endIndex = this.timeSlots.indexOf(slot)
            const minIndex = Math.min(startIndex, endIndex)
            const maxIndex = Math.max(startIndex, endIndex)

            for (let i = minIndex; i <= maxIndex; i++) {
                this.timeSlots[i].isSelected = true
                this.selectedSlots.push(this.timeSlots[i])
            }
        },

        // End drag operation and create time entry
        async endDrag(taskId?: number) {
            if (!this.isDragging || !this.dragStartSlot || !this.dragEndSlot) {
                this.cancelDrag()
                return
            }

            const tasksStore = useTasksStore()
            let selectedTask = null

            if (taskId) {
                selectedTask = tasksStore.getTaskById(taskId)
            } else {
                selectedTask = tasksStore.selectedTask
            }

            if (!selectedTask) {
                this.cancelDrag()
                throw new Error('No task selected')
            }

            try {
                // Calculate start and end times
                const startTime = this.dragStartSlot.timeString
                const endSlotIndex = this.timeSlots.indexOf(this.dragEndSlot)
                const endSlot = this.timeSlots[endSlotIndex + 1] || this.dragEndSlot
                let endTime = endSlot.timeString

                // If dragging to the last slot, add interval minutes
                if (endSlot === this.dragEndSlot) {
                    const [hours, minutes] = endTime.split(':').map(Number)
                    const totalMinutes = hours * 60 + minutes + this.intervalMinutes
                    const endHours = Math.floor(totalMinutes / 60)
                    const endMins = totalMinutes % 60
                    endTime = `${endHours.toString().padStart(2, '0')}:${endMins.toString().padStart(2, '0')}`
                }

                // Create time entry
                const { createTimeEntry } = useTimeEntriesApi()
                const request: TimeEntryCreateRequest = {
                    taskId: selectedTask.id,
                    entryDate: this.currentDate,
                    startTime,
                    endTime,
                    description: `${selectedTask.title} work session`,
                    isBillable: false
                }

                const newEntry = await createTimeEntry(request)
                this.timeEntries.push(newEntry)
                this.updateSlotTimeEntries()

                const reportsStore = useReportsStore()
                reportsStore.invalidateDaily(this.currentDate)
                reportsStore.invalidateWeekly(this.currentDate)

                return newEntry
            } catch (error) {
                throw error
            } finally {
                this.cancelDrag()
            }
        },

        // Cancel drag operation
        cancelDrag() {
            this.isDragging = false
            this.dragStartSlot = null
            this.dragEndSlot = null
            this.clearSelection()
        },

        // Clear slot selection
        clearSelection() {
            this.selectedSlots = []
            this.timeSlots.forEach(slot => {
                slot.isSelected = false
            })
        },

        // Delete time entry
        async deleteTimeEntry(entryId: number) {
            try {
                const { deleteTimeEntry } = useTimeEntriesApi()
                await deleteTimeEntry(entryId)

                // Remove from local state
                this.timeEntries = this.timeEntries.filter(entry => entry.id !== entryId)
                this.updateSlotTimeEntries()

                const reportsStore = useReportsStore()
                reportsStore.invalidateDaily(this.currentDate)
                reportsStore.invalidateWeekly(this.currentDate)

                return true
            } catch (error: any) {
                this.error = error.message || 'Failed to delete time entry'
                throw error
            }
        },

        // Clear error
        // Clear error
        clearError() {
            this.error = null
        },

        // Reset store
        reset() {
            this.timeEntries = []
            this.timeSlots = []
            this.cancelDrag()
            this.loading = false
            this.error = null
        }
    }
})