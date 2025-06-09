<template>
  <div class="min-h-screen bg-gray-50 p-8">
    <div class="max-w-6xl mx-auto">
      <!-- Header -->
      <div class="text-center mb-8">
        <h1 class="text-4xl font-bold text-gray-900 mb-4">
          API Integration Test
        </h1>
        <p class="text-xl text-gray-600">
          Testing backend connectivity and data fetching
        </p>
      </div>

      <!-- Backend Connection Status -->
      <div class="bg-white rounded-lg shadow-lg p-6 mb-8">
        <h2 class="text-2xl font-semibold text-gray-800 mb-4 flex items-center">
          <i class="pi pi-server text-blue-500 mr-2"></i>
          Backend Connection
        </h2>

        <div class="flex items-center space-x-4 mb-4">
          <Button
              @click="testConnection"
              :loading="connectionTesting"
              label="Test Connection"
              icon="pi pi-wifi"
          />
          <div v-if="connectionStatus" class="flex items-center space-x-2">
            <i :class="connectionStatus.success ? 'pi pi-check-circle text-green-500' : 'pi pi-times-circle text-red-500'"></i>
            <span :class="connectionStatus.success ? 'text-green-700' : 'text-red-700'">
              {{ connectionStatus.message }}
            </span>
          </div>
        </div>

        <div v-if="healthData" class="bg-gray-50 rounded p-4 mb-4">
          <h3 class="font-semibold mb-2">Health Check Response:</h3>
          <pre class="text-sm text-gray-700">{{ JSON.stringify(healthData, null, 2) }}</pre>
        </div>

        <!-- Quick Toast Test -->
        <ToastTest />
      </div>

      <!-- Categories Test -->
      <div class="grid md:grid-cols-2 gap-6 mb-8">
        <div class="bg-white rounded-lg shadow-lg p-6">
          <h2 class="text-2xl font-semibold text-gray-800 mb-4 flex items-center">
            <i class="pi pi-folder text-purple-500 mr-2"></i>
            Categories
          </h2>

          <div class="space-y-4">
            <Button
                @click="loadCategories"
                :loading="categoriesStore.loading"
                label="Load Categories"
                icon="pi pi-refresh"
                class="w-full"
            />

            <div v-if="categoriesStore.error" class="text-red-600 text-sm">
              Error: {{ categoriesStore.error }}
            </div>

            <div v-if="categoriesStore.categories.length > 0" class="space-y-2">
              <div class="flex items-center justify-between">
                <h3 class="font-semibold">Categories ({{ categoriesStore.categories.length }}):</h3>
                <span class="text-green-600 text-sm">✅ Loaded successfully</span>
              </div>
              <div
                  v-for="category in categoriesStore.categories"
                  :key="category.id"
                  class="p-3 border border-gray-200 rounded-lg"
              >
                <div class="font-medium">{{ category.title }}</div>
                <div class="text-sm text-gray-600">
                  Tasks: {{ category.activeTaskCount }}/{{ category.taskCount }}
                  <span v-if="category.isDefault" class="ml-2 px-2 py-1 bg-blue-100 text-blue-800 text-xs rounded">
                    Default
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Tasks Test -->
        <div class="bg-white rounded-lg shadow-lg p-6">
          <h2 class="text-2xl font-semibold text-gray-800 mb-4 flex items-center">
            <i class="pi pi-bookmark text-green-500 mr-2"></i>
            Tasks
          </h2>

          <div class="space-y-4">
            <Button
                @click="loadTasks"
                :loading="tasksStore.loading"
                label="Load Tasks"
                icon="pi pi-refresh"
                class="w-full"
            />

            <div v-if="tasksStore.error" class="text-red-600 text-sm">
              Error: {{ tasksStore.error }}
            </div>

            <div v-if="tasksStore.tasks.length > 0" class="space-y-2">
              <div class="flex items-center justify-between">
                <h3 class="font-semibold">Tasks ({{ tasksStore.tasks.length }}):</h3>
                <span class="text-green-600 text-sm">✅ Loaded successfully</span>
              </div>
              <div
                  v-for="task in tasksStore.tasks"
                  :key="task.id"
                  class="p-3 border border-gray-200 rounded-lg flex items-center space-x-3"
              >
                <div
                    class="w-4 h-4 rounded"
                    :style="{ backgroundColor: task.color }"
                ></div>
                <div class="flex-1">
                  <div class="font-medium">{{ task.title }}</div>
                  <div class="text-sm text-gray-600">{{ task.categoryTitle }}</div>
                </div>
                <i v-if="task.icon" :class="`pi pi-${task.icon} text-gray-400`"></i>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Time Entries Test -->
      <div class="bg-white rounded-lg shadow-lg p-6">
        <h2 class="text-2xl font-semibold text-gray-800 mb-4 flex items-center">
          <i class="pi pi-clock text-orange-500 mr-2"></i>
          Time Entries for Today
        </h2>

        <div class="space-y-4">
          <div class="flex items-center space-x-4">
            <Button
                @click="loadTimeEntries"
                :loading="loadingTimeEntries"
                label="Load Today's Entries"
                icon="pi pi-refresh"
            />
            <div class="text-sm text-gray-600">
              Date: {{ today }}
            </div>
          </div>

          <div v-if="timeEntriesError" class="text-red-600 text-sm">
            Error: {{ timeEntriesError }}
          </div>

          <div v-if="timeEntries.length > 0" class="space-y-2">
            <div class="flex items-center justify-between">
              <h3 class="font-semibold">Time Entries ({{ timeEntries.length }}):</h3>
              <span class="text-green-600 text-sm">✅ Loaded successfully</span>
            </div>
            <div
                v-for="entry in timeEntries"
                :key="entry.id"
                class="p-3 border border-gray-200 rounded-lg flex items-center space-x-3"
            >
              <div
                  class="w-4 h-4 rounded"
                  :style="{ backgroundColor: entry.taskColor }"
              ></div>
              <div class="flex-1">
                <div class="font-medium">{{ entry.taskTitle }}</div>
                <div class="text-sm text-gray-600">
                  {{ entry.startTime }} - {{ entry.endTime }}
                  ({{ Math.floor(entry.durationMinutes / 60) }}h {{ entry.durationMinutes % 60 }}m)
                </div>
              </div>
            </div>
          </div>

          <div v-else-if="!loadingTimeEntries && !timeEntriesError" class="text-gray-500 text-center py-4">
            No time entries found for today
          </div>
        </div>
      </div>

      <!-- API Configuration -->
      <div class="mt-8 bg-blue-50 border border-blue-200 rounded-lg p-4">
        <h3 class="font-semibold text-blue-800 mb-2">Configuration:</h3>
        <div class="text-blue-700 text-sm space-y-1">
          <div>API Base URL: {{ config.public.apiBase }}</div>
          <div>App Mode: {{ config.public.appMode }}</div>
        </div>
      </div>

      <!-- Navigation -->
      <div class="mt-8 text-center space-y-4">
        <div class="grid grid-cols-2 md:grid-cols-5 gap-3 max-w-4xl mx-auto">
          <NuxtLink
              to="/test-toast"
              class="inline-flex items-center justify-center px-4 py-3 bg-purple-600 text-white font-medium rounded-lg hover:bg-purple-700 transition-colors"
          >
            <i class="pi pi-bell mr-1"></i>
            <span class="hidden sm:inline">Toast</span>
          </NuxtLink>
          <NuxtLink
              to="/test-sidebar"
              class="inline-flex items-center justify-center px-4 py-3 bg-green-600 text-white font-medium rounded-lg hover:bg-green-700 transition-colors"
          >
            <i class="pi pi-bookmark mr-1"></i>
            <span class="hidden sm:inline">Sidebar</span>
          </NuxtLink>
          <NuxtLink
              to="/test-management"
              class="inline-flex items-center justify-center px-4 py-3 bg-red-600 text-white font-medium rounded-lg hover:bg-red-700 transition-colors"
          >
            <i class="pi pi-cog mr-1"></i>
            <span class="hidden sm:inline">Management</span>
          </NuxtLink>
          <NuxtLink
              to="/test-timesheet"
              class="inline-flex items-center justify-center px-4 py-3 bg-orange-600 text-white font-medium rounded-lg hover:bg-orange-700 transition-colors"
          >
            <i class="pi pi-calendar mr-1"></i>
            <span class="hidden sm:inline">Timesheet</span>
          </NuxtLink>
          <NuxtLink
              to="/"
              class="inline-flex items-center justify-center px-4 py-3 bg-gray-600 text-white font-medium rounded-lg hover:bg-gray-700 transition-colors"
          >
            <i class="pi pi-home mr-1"></i>
            <span class="hidden sm:inline">Home</span>
          </NuxtLink>
        </div>
        <p class="text-sm text-gray-600">Test different components and functionality</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { format } from 'date-fns'
import type { TimeEntry, HealthResponse } from '~/types/api'

// Stores
const categoriesStore = useCategoriesStore()
const tasksStore = useTasksStore()

// Configuration
const config = useRuntimeConfig()

// API composables
const { apiCall } = useApi()
const { getTimeEntriesForDate } = useTimeEntriesApi()

// Toast service
const toast = useToast()

// Reactive state
const connectionTesting = ref(false)
const connectionStatus = ref<{ success: boolean; message: string } | null>(null)
const healthData = ref<HealthResponse | null>(null)

const loadingTimeEntries = ref(false)
const timeEntries = ref<TimeEntry[]>([])
const timeEntriesError = ref<string | null>(null)

// Current date
const today = format(new Date(), 'yyyy-MM-dd')

// Methods
const testConnection = async () => {
  connectionTesting.value = true
  connectionStatus.value = null

  try {
    const health = await apiCall<HealthResponse>('/health')
    healthData.value = health
    connectionStatus.value = {
      success: true,
      message: 'Backend connection successful!'
    }

    // Show success toast
    toast.add({
      severity: 'success',
      summary: 'Connection Success',
      detail: 'Successfully connected to backend!',
      life: 3000
    })
  } catch (error: any) {
    connectionStatus.value = {
      success: false,
      message: error.message || 'Failed to connect to backend'
    }

    // Show error toast
    toast.add({
      severity: 'error',
      summary: 'Connection Failed',
      detail: error.message || 'Failed to connect to backend',
      life: 5000
    })
  } finally {
    connectionTesting.value = false
  }
}

const loadCategories = async () => {
  try {
    await categoriesStore.fetchCategories()
    console.log('✅ Categories loaded successfully:', categoriesStore.categories.length)

    // Show success toast
    toast.add({
      severity: 'success',
      summary: 'Categories Loaded',
      detail: `Successfully loaded ${categoriesStore.categories.length} categories`,
      life: 3000
    })
  } catch (error: any) {
    console.error('❌ Failed to load categories:', error)

    // Show error toast
    toast.add({
      severity: 'error',
      summary: 'Failed to Load Categories',
      detail: error.message || 'Unknown error occurred',
      life: 5000
    })
  }
}

const loadTasks = async () => {
  try {
    await tasksStore.fetchTasks()
    console.log('✅ Tasks loaded successfully:', tasksStore.tasks.length)

    // Show success toast
    toast.add({
      severity: 'success',
      summary: 'Tasks Loaded',
      detail: `Successfully loaded ${tasksStore.tasks.length} tasks`,
      life: 3000
    })
  } catch (error: any) {
    console.error('❌ Failed to load tasks:', error)

    // Show error toast
    toast.add({
      severity: 'error',
      summary: 'Failed to Load Tasks',
      detail: error.message || 'Unknown error occurred',
      life: 5000
    })
  }
}

const loadTimeEntries = async () => {
  loadingTimeEntries.value = true
  timeEntriesError.value = null

  try {
    timeEntries.value = await getTimeEntriesForDate(today)

    // Show success toast
    if (timeEntries.value.length > 0) {
      toast.add({
        severity: 'success',
        summary: 'Time Entries Loaded',
        detail: `Found ${timeEntries.value.length} time entries for today`,
        life: 3000
      })
    } else {
      toast.add({
        severity: 'info',
        summary: 'No Time Entries',
        detail: 'No time entries found for today',
        life: 3000
      })
    }
  } catch (error: any) {
    timeEntriesError.value = error.message || 'Failed to load time entries'

    // Show error toast
    toast.add({
      severity: 'error',
      summary: 'Failed to Load Time Entries',
      detail: error.message || 'Unknown error occurred',
      life: 5000
    })
  } finally {
    loadingTimeEntries.value = false
  }
}

// Set page title
useHead({
  title: 'API Test - Time Tracker'
})
</script>