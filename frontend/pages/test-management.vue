<template>
  <div class="min-h-screen bg-gray-50 flex">
    <!-- Updated Task Sidebar with Management -->
    <div class="w-80 flex-shrink-0">
      <TaskSidebar />
    </div>

    <!-- Main Content Area -->
    <div class="flex-1 p-8">
      <div class="max-w-4xl mx-auto">
        <!-- Header -->
        <div class="mb-8">
          <h1 class="text-4xl font-bold text-gray-900 mb-4">
            Task & Category Management
          </h1>
          <p class="text-xl text-gray-600">
            Test the new task and category creation/management functionality
          </p>
        </div>

        <!-- Quick Actions -->
        <div class="bg-white rounded-lg shadow-lg p-6 mb-8">
          <h2 class="text-2xl font-semibold text-gray-800 mb-4 flex items-center">
            <i class="pi pi-bolt text-yellow-500 mr-2"></i>
            Quick Actions
          </h2>

          <div class="grid md:grid-cols-2 gap-4">
            <!-- Task Actions -->
            <div class="space-y-3">
              <h3 class="font-semibold text-gray-700">Task Management</h3>
              <div class="space-y-2">
                <Button
                    @click="openCreateTask"
                    label="Create New Task"
                    icon="pi pi-plus"
                    class="w-full"
                />
                <Button
                    @click="openManageTasks"
                    label="Manage All Tasks"
                    icon="pi pi-cog"
                    severity="secondary"
                    class="w-full"
                />
                <Button
                    @click="selectRandomTask"
                    label="Select Random Task"
                    icon="pi pi-shuffle"
                    severity="info"
                    class="w-full"
                />
              </div>
            </div>

            <!-- Category Actions -->
            <div class="space-y-3">
              <h3 class="font-semibold text-gray-700">Category Management</h3>
              <div class="space-y-2">
                <Button
                    @click="openCreateCategory"
                    label="Create New Category"
                    icon="pi pi-folder-plus"
                    class="w-full"
                />
                <Button
                    @click="openManageCategories"
                    label="Manage Categories"
                    icon="pi pi-folder-open"
                    severity="secondary"
                    class="w-full"
                />
                <Button
                    @click="refreshAll"
                    label="Refresh All Data"
                    icon="pi pi-refresh"
                    severity="help"
                    class="w-full"
                    :loading="refreshing"
                />
              </div>
            </div>
          </div>
        </div>

        <!-- Current Selection -->
        <div class="bg-white rounded-lg shadow-lg p-6 mb-8">
          <h2 class="text-2xl font-semibold text-gray-800 mb-4 flex items-center">
            <i class="pi pi-bookmark text-blue-500 mr-2"></i>
            Current Selection
          </h2>

          <div v-if="selectedTask" class="p-4 bg-blue-50 border border-blue-200 rounded-lg">
            <div class="flex items-start space-x-4">
              <div
                  class="w-8 h-8 rounded-lg flex-shrink-0 shadow-sm"
                  :style="{ backgroundColor: selectedTask.color }"
              ></div>
              <div class="flex-1">
                <h3 class="text-lg font-semibold text-blue-900">
                  {{ selectedTask.title }}
                </h3>
                <p class="text-blue-700">{{ selectedTask.categoryTitle }}</p>
                <p v-if="selectedTask.description" class="text-sm text-blue-600 mt-1">
                  {{ selectedTask.description }}
                </p>
                <div class="mt-2 flex items-center space-x-4 text-sm text-blue-600">
                  <span>Color: {{ selectedTask.color }}</span>
                  <span v-if="selectedTask.icon">Icon: {{ selectedTask.icon }}</span>
                  <span>Order: {{ selectedTask.sortOrder }}</span>
                </div>
              </div>
              <div v-if="selectedTask.icon" class="flex-shrink-0">
                <i :class="`pi pi-${selectedTask.icon} text-2xl text-blue-600`"></i>
              </div>
            </div>
          </div>

          <div v-else class="text-center py-8">
            <i class="pi pi-bookmark text-4xl text-gray-300 mb-4"></i>
            <h3 class="text-lg font-medium text-gray-600 mb-2">No Task Selected</h3>
            <p class="text-gray-500">
              Select a task from the sidebar or create a new one
            </p>
          </div>
        </div>

        <!-- Statistics -->
        <div class="bg-white rounded-lg shadow-lg p-6 mb-8">
          <h2 class="text-2xl font-semibold text-gray-800 mb-4 flex items-center">
            <i class="pi pi-chart-bar text-green-500 mr-2"></i>
            Data Statistics
          </h2>

          <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div class="text-center p-4 bg-blue-50 rounded-lg">
              <div class="text-2xl font-bold text-blue-600">{{ totalTasks }}</div>
              <div class="text-sm text-blue-800">Active Tasks</div>
            </div>
            <div class="text-center p-4 bg-green-50 rounded-lg">
              <div class="text-2xl font-bold text-green-600">{{ totalCategories }}</div>
              <div class="text-sm text-green-800">Categories</div>
            </div>
            <div class="text-center p-4 bg-purple-50 rounded-lg">
              <div class="text-2xl font-bold text-purple-600">{{ tasksWithIcons }}</div>
              <div class="text-sm text-purple-800">With Icons</div>
            </div>
            <div class="text-center p-4 bg-orange-50 rounded-lg">
              <div class="text-2xl font-bold text-orange-600">{{ uniqueColors }}</div>
              <div class="text-sm text-orange-800">Unique Colors</div>
            </div>
          </div>
        </div>

        <!-- Instructions -->
        <div class="bg-white rounded-lg shadow-lg p-6">
          <h2 class="text-2xl font-semibold text-gray-800 mb-4 flex items-center">
            <i class="pi pi-info-circle text-blue-500 mr-2"></i>
            How to Test
          </h2>

          <div class="space-y-4">
            <div>
              <h3 class="font-semibold text-gray-700 mb-2">Creating Tasks</h3>
              <ul class="list-disc list-inside space-y-1 text-gray-600">
                <li>Click "New Task" in the sidebar or use the "Create New Task" button above</li>
                <li>Fill in the task details: title, category, description, color, and icon</li>
                <li>Use the color picker or select from preset colors</li>
                <li>Choose an icon from the grid or leave blank</li>
                <li>The new task will appear in the sidebar and be auto-selected</li>
              </ul>
            </div>

            <div>
              <h3 class="font-semibold text-gray-700 mb-2">Managing Tasks</h3>
              <ul class="list-disc list-inside space-y-1 text-gray-600">
                <li>Click the settings icon in the sidebar or "Manage All Tasks" above</li>
                <li>Edit tasks by clicking the pencil icon</li>
                <li>Select tasks directly from the management view</li>
                <li>Archive tasks to hide them while preserving data</li>
              </ul>
            </div>

            <div>
              <h3 class="font-semibold text-gray-700 mb-2">Category Management</h3>
              <ul class="list-disc list-inside space-y-1 text-gray-600">
                <li>Access via "Manage Categories" from the task management view</li>
                <li>Create new categories with descriptions and sort order</li>
                <li>Set default categories for new tasks</li>
                <li>Edit existing categories and their properties</li>
              </ul>
            </div>
          </div>
        </div>

        <!-- Navigation -->
        <div class="mt-8 text-center space-y-4">
          <div>
            <NuxtLink
                to="/test-timesheet"
                class="inline-flex items-center px-6 py-3 bg-orange-600 text-white font-medium rounded-lg hover:bg-orange-700 transition-colors mr-4"
            >
              <i class="pi pi-calendar mr-2"></i>
              Full Timesheet with Management
            </NuxtLink>
            <NuxtLink
                to="/test-sidebar"
                class="inline-flex items-center px-6 py-3 bg-green-600 text-white font-medium rounded-lg hover:bg-green-700 transition-colors mr-4"
            >
              <i class="pi pi-arrow-left mr-2"></i>
              Back to Sidebar Test
            </NuxtLink>
            <NuxtLink
                to="/"
                class="inline-flex items-center px-6 py-3 bg-gray-600 text-white font-medium rounded-lg hover:bg-gray-700 transition-colors"
            >
              <i class="pi pi-home mr-2"></i>
              Home
            </NuxtLink>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
// Stores
const tasksStore = useTasksStore()
const categoriesStore = useCategoriesStore()

// Composables
const toast = useToast()

// Local state
const refreshing = ref(false)

// Computed
const selectedTask = computed(() => tasksStore.selectedTask)
const totalTasks = computed(() => tasksStore.activeTasks.length)
const totalCategories = computed(() => categoriesStore.activeCategories.length)
const tasksWithIcons = computed(() =>
    tasksStore.activeTasks.filter(task => task.icon).length
)
const uniqueColors = computed(() => {
  const colors = new Set(tasksStore.activeTasks.map(task => task.color))
  return colors.size
})

// Methods
const openCreateTask = () => {
  // Trigger the sidebar's create task dialog
  // This would need to be implemented with refs or events
  toast.add({
    severity: 'info',
    summary: 'Create Task',
    detail: 'Click "New Task" in the sidebar to create a task',
    life: 3000
  })
}

const openManageTasks = () => {
  toast.add({
    severity: 'info',
    summary: 'Manage Tasks',
    detail: 'Click the settings icon in the sidebar to manage tasks',
    life: 3000
  })
}

const openCreateCategory = () => {
  toast.add({
    severity: 'info',
    summary: 'Create Category',
    detail: 'Use "Manage Tasks" → "Manage Categories" → "Create New Category"',
    life: 4000
  })
}

const openManageCategories = () => {
  toast.add({
    severity: 'info',
    summary: 'Manage Categories',
    detail: 'Use "Manage Tasks" → "Manage Categories" in the sidebar',
    life: 4000
  })
}

const selectRandomTask = () => {
  const tasks = tasksStore.activeTasks
  if (tasks.length > 0) {
    const randomTask = tasks[Math.floor(Math.random() * tasks.length)]
    tasksStore.selectTask(randomTask)

    toast.add({
      severity: 'info',
      summary: 'Random Task Selected',
      detail: `Selected "${randomTask.title}"`,
      life: 2000
    })
  } else {
    toast.add({
      severity: 'warn',
      summary: 'No Tasks Available',
      detail: 'Create some tasks first',
      life: 3000
    })
  }
}

const refreshAll = async () => {
  refreshing.value = true

  try {
    await Promise.all([
      tasksStore.fetchTasks(),
      categoriesStore.fetchCategories()
    ])

    toast.add({
      severity: 'success',
      summary: 'Data Refreshed',
      detail: `Loaded ${totalTasks.value} tasks and ${totalCategories.value} categories`,
      life: 3000
    })
  } catch (error: any) {
    toast.add({
      severity: 'error',
      summary: 'Refresh Failed',
      detail: error.message || 'Failed to refresh data',
      life: 5000
    })
  } finally {
    refreshing.value = false
  }
}

// Initialize
onMounted(async () => {
  // Load data if not already loaded
  if (tasksStore.tasks.length === 0) {
    await tasksStore.fetchTasks()
  }

  if (categoriesStore.categories.length === 0) {
    await categoriesStore.fetchCategories()
  }
})

useHead({
  title: 'Task & Category Management - Time Tracker'
})
</script>