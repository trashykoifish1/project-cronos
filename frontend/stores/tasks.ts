// Complete updated tasks.ts store with all missing functions:

import { defineStore } from 'pinia'
import type { Task, TaskCreateRequest, TaskUpdateRequest } from '~/types/api'

export const useTasksStore = defineStore('tasks', {
    state: () => ({
        tasks: [] as Task[],
        selectedTask: null as Task | null,
        loading: false,
        error: null as string | null
    }),

    getters: {
        activeTasks: (state) =>
            state.tasks.filter(task => !task.isArchived),

        tasksByCategory: (state) => (categoryId: number) =>
            state.tasks.filter(task => task.categoryId === categoryId && !task.isArchived),

        getTaskById: (state) => (id: number) =>
            state.tasks.find(task => task.id === id) || null,

        // Group tasks by category for sidebar display
        tasksByCategories: (state) => {
            const grouped: Record<string, Task[]> = {}
            state.tasks.forEach(task => {
                if (!task.isArchived) {
                    if (!grouped[task.categoryTitle]) {
                        grouped[task.categoryTitle] = []
                    }
                    grouped[task.categoryTitle].push(task)
                }
            })

            // Sort tasks within each category by sortOrder
            Object.keys(grouped).forEach(categoryName => {
                grouped[categoryName].sort((a, b) => a.sortOrder - b.sortOrder)
            })

            return grouped
        },

        // Get selected task with additional computed info
        selectedTaskInfo: (state) => {
            if (!state.selectedTask) return null

            return {
                ...state.selectedTask,
                isSelected: true,
                canCreateTimeEntry: true
            }
        }
    },

    actions: {
        async fetchTasks() {
            if (this.loading) return

            this.loading = true
            this.error = null

            try {
                const { getActiveTasks } = useTasksApi()
                this.tasks = await getActiveTasks()

                // Clear selection if the selected task is no longer available
                if (this.selectedTask) {
                    const stillExists = this.tasks.find(task => task.id === this.selectedTask!.id)
                    if (!stillExists) {
                        this.selectedTask = null
                    }
                }
            } catch (error: any) {
                this.error = error.message || 'Failed to fetch tasks'
                console.error('Error fetching tasks:', error)
            } finally {
                this.loading = false
            }
        },

        async createTask(taskData: TaskCreateRequest) {
            try {
                this.error = null
                const { createTask } = useTasksApi()
                const newTask = await createTask(taskData)
                this.tasks.push(newTask)
                return newTask
            } catch (error: any) {
                this.error = error.message || 'Failed to create task'
                throw error
            }
        },

        async updateTask(id: number, taskData: TaskUpdateRequest) {
            try {
                this.error = null
                const { updateTask } = useTasksApi()
                const updatedTask = await updateTask(id, taskData)

                const index = this.tasks.findIndex(task => task.id === id)
                if (index !== -1) {
                    this.tasks[index] = updatedTask

                    // Update selected task if it's the one being updated
                    if (this.selectedTask?.id === id) {
                        this.selectedTask = updatedTask
                    }
                }

                return updatedTask
            } catch (error: any) {
                this.error = error.message || 'Failed to update task'
                throw error
            }
        },

        async archiveTask(id: number, archived: boolean) {
            try {
                this.error = null
                const { archiveTask } = useTasksApi()
                const updatedTask = await archiveTask(id, archived)

                const index = this.tasks.findIndex(task => task.id === id)
                if (index !== -1) {
                    this.tasks[index] = updatedTask

                    // Clear selection if archiving the selected task
                    if (archived && this.selectedTask?.id === id) {
                        this.selectedTask = null
                    }
                }

                return updatedTask
            } catch (error: any) {
                this.error = error.message || 'Failed to archive task'
                throw error
            }
        },

        async deleteTask(id: number) {
            try {
                this.error = null
                const { deleteTask } = useTasksApi()
                await deleteTask(id)

                const index = this.tasks.findIndex(task => task.id === id)
                if (index !== -1) {
                    this.tasks = this.tasks.filter(task => task.id !== id)

                    // Clear selection if deleting the selected task
                    if (this.selectedTask?.id === id) {
                        this.selectedTask = null
                    }
                }

                return true;
            } catch (error: any) {
                this.error = error.message || 'Failed to delete task'
                throw error
            }
        },



        selectTask(task: Task | null) {
            this.selectedTask = task

            // Clear any previous errors when selecting a task
            if (task) {
                this.error = null
            }
        },

        // Helper method to select task by ID
        selectTaskById(taskId: number) {
            const task = this.getTaskById(taskId)
            if (task) {
                this.selectTask(task)
                return task
            }
            return null
        },

        // Clear selection and errors
        clearSelection() {
            this.selectedTask = null
            this.error = null
        },

        clearError() {
            this.error = null
        },

        // Reset store state
        reset() {
            this.tasks = []
            this.selectedTask = null
            this.loading = false
            this.error = null
        },

        // Get tasks by category with better filtering
        getTasksByCategory(categoryId: number, includeArchived: boolean = false) {
            return this.tasks.filter(task =>
                task.categoryId === categoryId &&
                (includeArchived || !task.isArchived)
            )
        },

        // Get task statistics
        getTaskStats() {
            const total = this.tasks.length
            const active = this.tasks.filter(task => !task.isArchived).length
            const archived = total - active
            const withIcons = this.tasks.filter(task => task.icon).length
            const uniqueColors = new Set(this.tasks.map(task => task.color)).size

            return {
                total,
                active,
                archived,
                withIcons,
                uniqueColors
            }
        },

        // Validate task data before creation/update
        validateTask(taskData: TaskCreateRequest | TaskUpdateRequest, excludeId?: number) {
            const errors: string[] = []

            // Check title uniqueness within category
            if ('categoryId' in taskData) {
                const existingTask = this.tasks.find(task =>
                    task.categoryId === taskData.categoryId &&
                    task.title.toLowerCase() === taskData.title.toLowerCase() &&
                    task.id !== excludeId
                )
                if (existingTask) {
                    errors.push('A task with this name already exists in this category')
                }
            }

            // Check color uniqueness within category
            if (taskData.color && 'categoryId' in taskData) {
                const existingTask = this.tasks.find(task =>
                    task.categoryId === taskData.categoryId &&
                    task.color.toLowerCase() === taskData.color.toLowerCase() &&
                    task.id !== excludeId
                )
                if (existingTask) {
                    errors.push('This color is already used by another task in this category')
                }
            }

            // Validate color format
            if (taskData.color && !/^#[0-9A-F]{6}$/i.test(taskData.color)) {
                errors.push('Color must be a valid hex color (e.g., #FF0000)')
            }

            return {
                isValid: errors.length === 0,
                errors
            }
        }
    }
})