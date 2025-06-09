<template>
  <div class="min-h-screen bg-gray-50 flex">
    <!-- Task Sidebar -->
    <TaskSidebar />

    <!-- Main Content Area -->
    <div class="flex-1 p-8">
      <div class="max-w-4xl mx-auto">
        <!-- Header -->
        <div class="mb-8">
          <h1 class="text-4xl font-bold text-gray-900 mb-4">
            Task Sidebar Test
          </h1>
          <p class="text-xl text-gray-600">
            Testing task selection and sidebar functionality
          </p>
        </div>

        <!-- Selected Task Details -->
        <div class="bg-white rounded-lg shadow-lg p-6 mb-8">
          <h2 class="text-2xl font-semibold text-gray-800 mb-4 flex items-center">
            <i class="pi pi-bookmark text-blue-500 mr-2"></i>
            Selected Task Details
          </h2>

          <div v-if="selectedTask" class="space-y-4">
            <!-- Task Summary -->
            <div class="p-4 bg-blue-50 border border-blue-200 rounded-lg">
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
                </div>
                <div v-if="selectedTask.icon" class="flex-shrink-0">
                  <i :class="`pi pi-${selectedTask.icon} text-2xl text-blue-600`"></i>
                </div>
              </div>
            </div>

            <!-- Task Properties -->
            <div class="grid md:grid-cols-2 gap-6">
              <div class="space-y-3">
                <h4 class="font-semibold text-gray-800">Task Information</h4>
                <div class="space-y-2 text-sm">
                  <div class="flex justify-between">
                    <span class="text-gray-600">ID:</span>
                    <span class="font-mono">{{ selectedTask.id }}</span>
                  </div>
                  <div class="flex justify-between">
                    <span class="text-gray-600">Category ID:</span>
                    <span class="font-mono">{{ selectedTask.categoryId }}</span>
                  </div>
                  <div class="flex justify-between">
                    <span class="text-gray-600">Color:</span>
                    <div class="flex items-center space-x-2">
                      <div
                          class="w-4 h-4 rounded border border-gray-300"
                          :style="{ backgroundColor: selectedTask.color }"
                      ></div>
                      <span class="font-mono">{{ selectedTask.color }}</span>
                    </div>
                  </div>
                  <div class="flex justify-between">
                    <span class="text-gray-600">Icon:</span>
                    <span class="font-mono">{{ selectedTask.icon || 'None' }}</span>
                  </div>
                  <div class="flex justify-between">
                    <span class="text-gray-600">Sort Order:</span>
                    <span class="font-mono">{{ selectedTask.sortOrder }}</span>
                  </div>
                </div>
              </div>

              <div class="space-y-3">
                <h4 class="font-semibold text-gray-800">Metadata</h4>
                <div class="space-y-2 text-sm">
                  <div class="flex justify-between">
                    <span class="text-gray-600">Created:</span>
                    <span class="font-mono text-xs">
                      {{ formatDate(selectedTask.createdAt) }}
                    </span>
                  </div>
                  <div class="flex justify-between">
                    <span class="text-gray-600">Updated:</span>
                    <span class="font-mono text-xs">
                      {{ formatDate(selectedTask.updatedAt) }}
                    </span>
                  </div>
                  <div class="flex justify-between">
                    <span class="text-gray-600">Archived:</span>
                    <span :class="selectedTask.isArchived ? 'text-red-600' : 'text-green-600'">
                      {{ selectedTask.isArchived ? 'Yes' : 'No' }}
                    </span>
                  </div>
                </div>
              </div>
            </div>

            <!-- Action Buttons -->
            <div class="flex space-x-3 pt-4 border-t border-gray-200">
              <Button
                  @click="simulateTimeEntry"
                  label="Simulate Time Entry"
                  icon="pi pi-clock"
                  severity="success"
              />
              <Button
                  @click="copyTaskInfo"
                  label="Copy Task Info"
                  icon="pi pi-copy"
                  severity="secondary"
              />
              <Button
                  @click="clearSelection"
                  label="Clear Selection"
                  icon="pi pi-times"
                  severity="secondary"
              />
            </div>
          </div>

          <!-- No Selection State -->
          <div v-else class="text-center py-8">
            <i class="pi pi-bookmark text-4xl text-gray-300 mb-4"></i>
            <h3 class="text-lg font-medium text-gray-600 mb-2">No Task Selected</h3>
            <p class="text-gray-500 mb-4">
              Select a task from the sidebar to see its details here
            </p>
            <div class="text-sm text-gray-400">
              👈 Click on any task in the sidebar to select it
            </div>
          </div>
        </div>

        <!-- Sidebar Statistics -->
        <div class="bg-white rounded-lg shadow-lg p-6 mb-8">
          <h2 class="text-2xl font-semibold text-gray-800 mb-4 flex items-center">
            <i class="pi pi-chart-bar text-green-500 mr-2"></i>
            Sidebar Statistics
          </h2>

          <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div class="text-center p-4 bg-blue-50 rounded-lg">
              <div class="text-2xl font-bold text-blue-600">{{ totalTasks }}</div>
              <div class="text-sm text-blue-800">Total Tasks</div>
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

        <!-- Testing Actions -->
        <div class="bg-white rounded-lg shadow-lg p-6">
          <h2 class="text-2xl font-semibold text-gray-800 mb-4 flex items-center">
            <i class="pi pi-wrench text-orange-500 mr-2"></i>
            Testing Actions
          </h2>

          <div class="grid md:grid-cols-2 gap-4">
            <Button
                @click="selectRandomTask"
                label="Select Random Task"
                icon="pi pi-shuffle"
                severity="info"
                class="w-full"
            />
            <Button
                @click="refreshTasks"
                label="Refresh Tasks"
                icon="pi pi-refresh"
                severity="secondary"
                class="w-full"
                :loading="tasksStore.loading"
            />
          </div>
        </div>

        <!-- Navigation -->
        <div class="mt-8 text-center space-y-4">
          <div>
            <NuxtLink
                to="/test-api"
                class="inline-flex items-center px-6 py-3 bg-blue-600 text-white font-medium rounded-lg hover:bg-blue-700 transition-colors mr-4"
            >
              <i class="pi pi-arrow-left mr-2"></i>
              Back to API Test
            </NuxtLink>
            <NuxtLink
                to="/test-management"
                class="inline-flex items-center px-6 py-3 bg-red-600 text-white font-medium rounded-lg hover:bg-red-700 transition-colors mr-4"
            >
              <i class="pi pi-arrow-right mr-2"></i>
              Task Management Test
            </NuxtLink>
            <NuxtLink
                to="/test-timesheet"
                class="inline-flex items-center px-6 py-3 bg-orange-600 text-white font-medium rounded-lg hover:bg-orange-700 transition-colors mr-4"
            >
              <i class="pi pi-arrow-right mr-2"></i>
              Full Timesheet Test
            </NuxtLink>
            <NuxtLink
                to="/"
                class="inline-flex items-center px-6 py-3 bg-gray-600 text-white font-medium rounded-lg hover:bg-gray-700 transition-colors"
            >
              <i class="pi pi-home mr-2"></i>
              Home
            </NuxtLink>
          </div>
          <p class="text-sm text-gray-600">
            Next: Step 3 - Task & Category Management ➜ <strong>Task Management Test</strong>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
// Stores
const tasksStore = useTasksStore()
const toast = useToast()

// Composables
const { formatDateTime } = useTime()

// Computed
const selectedTask = computed(() => tasksStore.selectedTask)

const totalTasks = computed(() => tasksStore.activeTasks.length)

const totalCategories = computed(() =>
    Object.keys(tasksStore.tasksByCategories).length
)

const tasksWithIcons = computed(() =>
    tasksStore.activeTasks.filter(task => task.icon).length
)

const uniqueColors = computed(() => {
  const colors = new Set(tasksStore.activeTasks.map(task => task.color))
  return colors.size
})

// Methods
const formatDate = (dateString: string) => {
  return formatDateTime(dateString, 'MMM dd, yyyy HH:mm')
}

const clearSelection = () => {
  tasksStore.selectTask(null)
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
  }
}

const refreshTasks = async () => {
  await tasksStore.fetchTasks()
}

const simulateTimeEntry = () => {
  if (!selectedTask.value) return

  toast.add({
    severity: 'success',
    summary: 'Time Entry Simulated',
    detail: `Would create time entry for "${selectedTask.value.title}"`,
    life: 3000
  })
}

const copyTaskInfo = async () => {
  if (!selectedTask.value) return

  const taskInfo = {
    id: selectedTask.value.id,
    title: selectedTask.value.title,
    category: selectedTask.value.categoryTitle,
    color: selectedTask.value.color,
    icon: selectedTask.value.icon
  }

  try {
    await navigator.clipboard.writeText(JSON.stringify(taskInfo, null, 2))
    toast.add({
      severity: 'success',
      summary: 'Copied to Clipboard',
      detail: 'Task information copied',
      life: 2000
    })
  } catch (error) {
    toast.add({
      severity: 'error',
      summary: 'Copy Failed',
      detail: 'Could not copy to clipboard',
      life: 3000
    })
  }
}

useHead({
  title: 'Task Sidebar Test - Time Tracker'
})
</script>