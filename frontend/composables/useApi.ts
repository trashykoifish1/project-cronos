import type {
    ApiResponse,
    ErrorResponse,
    Task,
    Category,
    TimeEntry,
    DailySummary,
    TaskCreateRequest,
    TaskUpdateRequest,
    CategoryCreateRequest,
    CategoryUpdateRequest,
    TimeEntryCreateRequest,
    TimeEntryUpdateRequest,
    BulkTimeEntryRequest,
    WeeklySummary,
    MonthlyStatistics, SpotifyTokenResponse, SpotifyRefreshResponse
} from '~/types/api'

export const useApi = () => {
    const config = useRuntimeConfig()

    const apiCall = async <T>(
        endpoint: string,
        options: {
            method?: 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH'
            body?: any
            query?: Record<string, any>
            headers?: Record<string, string>
            showToast?: boolean
        } = {}
    ): Promise<T> => {
        try {
            const response = await $fetch<ApiResponse<T>>(`${config.public.apiBase}${endpoint}`, {
                method: options.method || 'GET',
                body: options.body,
                query: options.query,
                headers: {
                    'Content-Type': 'application/json',
                    ...options.headers
                },
                retry: 2,
                retryDelay: 1000,
                timeout: 10000
            })

            if (!response.success) {
                throw new Error(response.message || 'API request failed')
            }

            return response.data
        } catch (error: any) {
            console.error('API Error:', error)

            // Handle different types of errors
            let errorMessage = 'An unexpected error occurred'

            if (error.data) {
                const errorResponse = error.data as ErrorResponse
                errorMessage = errorResponse.message || errorMessage
            } else if (error.message) {
                errorMessage = error.message
            }

            // Create custom error with message
            const apiError = new Error(errorMessage)
            apiError.cause = error
            throw apiError
        }
    }

    return {
        apiCall
    }
}

// Specific API methods
export const useTasksApi = () => {
    const { apiCall } = useApi()

    return {
        // Get all tasks for user
        getTasks: () =>
            apiCall<Task[]>('/tasks'),

        // Get all active tasks for user
        getActiveTasks: () =>
            apiCall<Task[]>('/tasks/active'),

        // Get tasks for specific category
        getTasksByCategory: (categoryId: number) =>
            apiCall<Task[]>(`/tasks/category/${categoryId}/active`),

        // Create new task
        createTask: (data: TaskCreateRequest) =>
            apiCall<Task>('/tasks', { method: 'POST', body: data }),

        // Update task
        updateTask: (id: number, data: TaskUpdateRequest) =>
            apiCall<Task>(`/tasks/${id}`, { method: 'PUT', body: data }),

        // Archive/unarchive task
        archiveTask: (id: number, archived: boolean) =>
            apiCall<Task>(`/tasks/${id}/archive`, {
                method: 'PATCH',
                query: { archived }
            }),

        // Move task to category
        moveTaskToCategory: (taskId: number, newCategoryId: number) =>
            apiCall<Task>(`/tasks/${taskId}/move-to-category/${newCategoryId}`, {
                method: 'PUT'
            }),

        // Delete task
        deleteTask: (id: number) =>
            apiCall<Task>(`/tasks/${id}`, {method: 'DELETE'}),
    }
}

export const useCategoriesApi = () => {
    const { apiCall } = useApi()

    return {
        // Get all active categories
        getCategories: () =>
            apiCall<Category[]>('/categories'),

        // Get active categories only
        getActiveCategories: () =>
            apiCall<Category[]>('/categories/active'),

        // Create new category
        createCategory: (data: CategoryCreateRequest) =>
            apiCall<Category>('/categories', { method: 'POST', body: data }),

        // Update category
        updateCategory: (id: number, data: CategoryUpdateRequest) =>
            apiCall<Category>(`/categories/${id}`, { method: 'PUT', body: data }),

        // Update category
        deleteCategory: (id: number) =>
            apiCall<Category>(`/categories/${id}`, { method: 'DELETE' })
    }
}

export const useTimeEntriesApi = () => {
    const { apiCall } = useApi()

    return {
        // Get time entries for specific date
        getTimeEntriesForDate: (date: string) =>
            apiCall<TimeEntry[]>(`/time-entries/date/${date}`),

        // Create new time entry
        createTimeEntry: (data: TimeEntryCreateRequest) =>
            apiCall<TimeEntry>('/time-entries', { method: 'POST', body: data }),

        // Update time entry
        updateTimeEntry: (id: number, data: TimeEntryUpdateRequest) =>
            apiCall<TimeEntry>(`/time-entries/${id}`, { method: 'PUT', body: data }),

        // Delete time entry
        deleteTimeEntry: (id: number) =>
            apiCall<void>(`/time-entries/${id}`, { method: 'DELETE' }),

        // Bulk create time entries
        bulkCreateTimeEntries: (data: BulkTimeEntryRequest) =>
            apiCall<any>('/time-entries/bulk', { method: 'POST', body: data }),

        // Get daily summary
        getDailySummary: (date: string) =>
            apiCall<DailySummary>(`/reports/daily/${date}`),

        // Validate time entry before creation
        validateTimeEntry: (data: TimeEntryCreateRequest) =>
            apiCall<any>('/time-entries/validate', { method: 'POST', body: data }),

        // Check for overlapping entries
        checkOverlaps: (date: string, startTime: string, endTime: string, excludeId?: number) =>
            apiCall<TimeEntry[]>('/time-entries/overlaps', {
                query: { date, startTime, endTime, excludeId }
            })
    }
}

export const useReportsApi = () => {
    const { apiCall } = useApi()

    return {
        // Get daily summary report
        getDailySummary: (date: string) =>
            apiCall<DailySummary>(`/reports/daily/${date}`),

        // Get weekly summary report
        getWeeklySummary: (date: string) =>
            apiCall<WeeklySummary>(`/reports/weekly/${date}`),

        // Get statistics for date range
        getStatistics: (startDate: string, endDate: string) =>
            apiCall<any>('/reports/statistics', {
                query: { startDate, endDate }
            }),

        getEnhancedStatistics: (startDate: string, endDate: string) =>
            apiCall<MonthlyStatistics>('/reports/enhanced-statistics', {
                query: { startDate, endDate }
            }),

        // Get productivity insights
        getProductivityInsights: (startDate: string, endDate: string) =>
            apiCall<any>('/reports/productivity-insights', {
                query: { startDate, endDate }
            })
    }
}

export const useSpotifyApi = () => {
    const { apiCall } = useApi()

    return {
        getSpotifyToken: (code: string, redirectUri: string) =>
            apiCall<SpotifyTokenResponse>('/spotify/token', {
                method: 'POST',
                body: {
                    code,
                    redirectUri
                }
            }),

        getSpotifyRefreshToken: (refreshToken: string) => apiCall<SpotifyRefreshResponse>('/spotify/refresh', {
            method: 'POST',
            body: {refresh_token: refreshToken}
        })
    }
}