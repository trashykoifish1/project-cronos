// API Response wrapper
export interface ApiResponse<T> {
    success: boolean
    message?: string
    data: T
    timestamp: string
}

// Error response
export interface ErrorResponse {
    success: false
    error: string
    message: string
    details?: Record<string, any>
    errorId?: string
    timestamp: string
    path?: string
}

// Health check response
export interface HealthResponse {
    status: string
    timestamp: string
    service: string
    version: string
    mode: string
}

// User (for future use)
export interface User {
    id: number
    email: string
    name: string
    timeZone: string
    isActive: boolean
    createdAt: string
    updatedAt: string
}

// Category
export interface Category {
    id: number
    title: string
    description?: string
    isArchived: boolean
    isDefault: boolean
    sortOrder: number
    taskCount: number
    activeTaskCount: number
    createdAt: string
    updatedAt: string
}

export interface CategoryCreateRequest {
    title: string
    description?: string
    isDefault?: boolean
    sortOrder?: number
}

export interface CategoryUpdateRequest {
    title: string
    description?: string
    isArchived?: boolean
    isDefault?: boolean
    sortOrder?: number
}

// Task
export interface Task {
    id: number
    categoryId: number
    categoryTitle: string
    title: string
    description?: string
    color: string // hex color
    icon?: string
    isArchived: boolean
    sortOrder: number
    createdAt: string
    updatedAt: string
}

export interface TaskCreateRequest {
    categoryId: number
    title: string
    description?: string
    color: string
    icon?: string
    sortOrder?: number
}

export interface TaskUpdateRequest {
    title: string
    description?: string
    color?: string
    icon?: string
    isArchived?: boolean
    sortOrder?: number
}

// Time Entry
export interface TimeEntry {
    id: number
    taskId: number
    taskTitle: string
    taskColor: string
    taskIcon?: string
    categoryId: number
    categoryTitle: string
    entryDate: string // YYYY-MM-DD
    startTime: string // HH:mm
    endTime: string // HH:mm
    durationMinutes: number
    description?: string
    isBillable: boolean
    createdAt: string
    updatedAt: string
}

export interface TimeEntryCreateRequest {
    taskId: number
    entryDate: string
    startTime: string
    endTime: string
    description?: string
    isBillable?: boolean
}

export interface TimeEntryUpdateRequest {
    taskId: number
    entryDate: string
    startTime: string
    endTime: string
    description?: string
    isBillable?: boolean
}

// Bulk time entry
export interface BulkTimeEntryRequest {
    entryDate: string
    replaceExisting?: boolean
    timeEntries: {
        taskId: number
        startTime: string
        endTime: string
        description?: string
    }[]
}

// Daily summary
export interface DailySummary {
    date: string
    totalMinutes: number
    totalTimeFormatted: string
    totalEntries: number
    categoryBreakdowns: CategoryBreakdown[]
    taskBreakdowns: TaskBreakdown[]
    timeEntries: TimeEntry[]
    warnings: string[]
}

export interface CategoryBreakdown {
    categoryId: number
    categoryTitle: string
    totalMinutes: number
    timeFormatted: string
    entryCount: number
}

export interface TaskBreakdown {
    taskId: number
    taskTitle: string
    taskColor: string
    taskIcon?: string
    categoryTitle: string
    totalMinutes: number
    timeFormatted: string
    entryCount: number
}

export interface WeeklySummary {
    weekStartDate: string
    weekEndDate: string
    totalMinutes: number
    totalTimeFormatted: string
    averageDailyHours: number
    dailySummaries: DailySummary[]
}