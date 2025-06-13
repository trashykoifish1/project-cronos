<template>
  <div class="flex-1 p-6">
    <!-- Page Header -->
    <div class="mb-6">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-app-primary">Manage Tasks</h1>
          <p class="text-app-secondary mt-1">
            Create and organize tasks within categories with custom colors and icons
          </p>
        </div>

        <!-- Action Buttons -->
        <div class="flex items-center space-x-3">
          <Button
              label="Create Task"
              icon="pi pi-plus"
              @click="showCreateDialog = true"
          />
          <Button
              label="Export Tasks"
              icon="pi pi-download"
              severity="secondary"
              outlined
              @click="exportTasks"
          />
          <Button
              icon="pi pi-refresh"
              severity="secondary"
              @click="refreshTasks"
              v-tooltip="'Refresh Tasks'"
              :loading="loading"
          />
        </div>
      </div>

      <!-- Quick Stats -->
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4 mt-6">
        <Card class="text-center">
          <template #content>
            <div class="py-4">
              <div class="text-2xl font-bold text-app-blue-600 mb-2">
                {{ totalTasks }}
              </div>
              <div class="text-sm text-app-secondary">Total Tasks</div>
            </div>
          </template>
        </Card>

        <Card class="text-center">
          <template #content>
            <div class="py-4">
              <div class="text-2xl font-bold text-app-green-600 mb-2">
                {{ activeTasks }}
              </div>
              <div class="text-sm text-app-secondary">Active Tasks</div>
            </div>
          </template>
        </Card>

        <Card class="text-center">
          <template #content>
            <div class="py-4">
              <div class="text-2xl font-bold text-app-purple-600 mb-2">
                {{ totalCategories }}
              </div>
              <div class="text-sm text-app-secondary">Categories Used</div>
            </div>
          </template>
        </Card>

        <Card class="text-center">
          <template #content>
            <div class="py-4">
              <div class="text-2xl font-bold text-app-orange-600 mb-2">
                {{ archivedTasks }}
              </div>
              <div class="text-sm text-app-secondary">Archived Tasks</div>
            </div>
          </template>
        </Card>
      </div>
    </div>

    <!-- Tasks Table -->
    <Card>
      <template #title>
        <div class="flex items-center justify-between">
          <div class="flex items-center">
            <i class="pi pi-bookmark text-app-blue-600 mr-2"></i>
            Tasks List
          </div>
          <div class="flex items-center space-x-2">
            <Button
                label="Bulk Actions"
                icon="pi pi-list"
                size="small"
                severity="secondary"
                outlined
                @click="showBulkActions = !showBulkActions"
                :disabled="selectedTasks.length === 0"
            />
            <span class="text-sm text-app-secondary">
              {{ filteredTasks.length }} tasks
            </span>
          </div>
        </div>
      </template>

      <template #content>
        <!-- Search and Filter -->
        <div class="flex flex-col md:flex-row gap-4 py-2">
          <div class="flex-1">
            <IconField class="p-input-icon-left w-full">
              <InputIcon class="pi pi-search"></InputIcon>
              <InputText
                  v-model="searchTerm"
                  placeholder="Search tasks..."
                  class="w-full"
                  size="small"
              />
            </IconField>
          </div>

          <div class="flex items-center space-x-3">
            <label class="text-sm font-medium text-app-secondary">Category:</label>
            <Select
                v-model="selectedCategoryFilter"
                :options="categoryFilterOptions"
                option-value="value"
                option-label="label"
                size="small"
                class="min-w-40"
            />

            <label class="text-sm font-medium text-app-secondary">Status:</label>
            <Select
                v-model="statusFilter"
                :options="statusFilterOptions"
                option-value="value"
                option-label="label"
                size="small"
                class="min-w-32"
            />
          </div>
        </div>

        <!-- Bulk Actions Panel -->
        <div v-if="showBulkActions && selectedTasks.length > 0"
             class="bg-app-blue-50 border border-app-blue-200 rounded-lg p-4 mb-4">
          <div class="flex items-center justify-between">
            <div class="flex items-center space-x-4">
              <span class="text-sm font-medium text-app-blue-800">
                {{ selectedTasks.length }} tasks selected
              </span>
              <Button
                  label="Archive Selected"
                  icon="pi pi-book"
                  size="small"
                  severity="warning"
                  @click="bulkArchive"
              />
              <Button
                  label="Unarchive Selected"
                  icon="pi pi-refresh"
                  size="small"
                  severity="warning"
                  @click="bulkUnarchive"
              />
              <Button
                  label="Move to Category"
                  icon="pi pi-arrow-right"
                  size="small"
                  severity="info"
                  @click="showBulkMoveDialog = true"
              />
              <Button
                  label="Export Selected"
                  icon="pi pi-download"
                  size="small"
                  severity="info"
                  @click="exportSelected"
              />
            </div>
            <Button
                icon="pi pi-times"
                size="small"
                text
                @click="clearSelection"
            />
          </div>
        </div>

        <!-- Data Table -->
        <DataTable
            :value="filteredTasks"
            v-model:selection="selectedTasks"
            dataKey="id"
            :paginator="true"
            :rows="10"
            :rowsPerPageOptions="[5, 10, 20, 50]"
            stripedRows
            responsiveLayout="scroll"
            :globalFilterFields="['title', 'description', 'categoryTitle']"
            :loading="loading"
        >
          <Column selectionMode="multiple" headerStyle="width: 3rem"></Column>

          <Column field="title" header="Task" sortable class="min-w-48">
            <template #body="{ data }">
              <div class="flex items-center space-x-3">
                <div
                    class="w-4 h-4 rounded"
                    :style="{ backgroundColor: data.color }"
                ></div>
                <div class="flex items-center space-x-2">
                  <i v-if="data.icon" :class="`pi pi-${data.icon}`" :style="{ color: data.color }"></i>
                  <span class="font-medium text-app-primary">{{ data.title }}</span>
                  <Tag v-if="data.isArchived" severity="warning" value="Archived" class="text-xs" />
                </div>
              </div>
              <div v-if="data.description" class="text-sm text-app-secondary mt-1 ml-7">
                {{ data.description }}
              </div>
            </template>
          </Column>

          <Column field="categoryTitle" header="Category" sortable class="min-w-32">
            <template #body="{ data }">
              <div class="text-center">
                <Tag :value="data.categoryTitle" severity="secondary" />
              </div>
            </template>
          </Column>

          <Column field="sortOrder" header="Order" sortable class="text-center min-w-20">
            <template #body="{ data }">
              <div class="text-center">
                <Badge :value="data.sortOrder" severity="secondary" />
              </div>
            </template>
          </Column>

          <Column field="createdAt" header="Created" sortable class="min-w-32">
            <template #body="{ data }">
              <div class="text-sm text-app-secondary">
                {{ formatDate(data.createdAt) }}
              </div>
            </template>
          </Column>

          <Column header="Actions" class="min-w-48">
            <template #body="{ data }">
              <div class="flex space-x-1">
                <Button
                    icon="pi pi-pencil"
                    size="small"
                    severity="secondary"
                    @click="editTask(data)"
                    v-tooltip="'Edit Task'"
                />
                <Button
                    icon="pi pi-arrow-right"
                    size="small"
                    severity="info"
                    @click="moveTask(data)"
                    v-tooltip="'Move to Category'"
                />
                <Button
                    :icon="data.isArchived ? 'pi pi-replay' : 'pi pi-book'"
                    size="small"
                    :severity="data.isArchived ? 'success' : 'warning'"
                    @click="toggleArchiveTask(data)"
                    :v-tooltip="data.isArchived ? 'Restore Task' : 'Archive Task'"
                />
                <Button
                    icon="pi pi-trash"
                    size="small"
                    severity="danger"
                    @click="deleteTask(data)"
                    v-tooltip="'Delete Task'"
                />
              </div>
            </template>
          </Column>
        </DataTable>
      </template>
    </Card>

    <!-- Create Task Dialog -->
    <Dialog
        v-model:visible="showCreateDialog"
        modal
        header="Create New Task"
        :style="{ width: '600px' }"
        :closable="true"
    >
      <TaskForm
          :loading="formLoading"
          @submit-create="handleCreateTask"
          @cancel="showCreateDialog = false"
      />
    </Dialog>

    <!-- Edit Task Dialog -->
    <Dialog
        v-model:visible="showEditDialog"
        modal
        header="Edit Task"
        :style="{ width: '600px' }"
        :closable="true"
    >
      <TaskForm
          :task="editingTask"
          :loading="formLoading"
          @submit-update="handleUpdateTask"
          @cancel="showEditDialog = false"
      />
    </Dialog>

    <!-- Move Task Dialog -->
    <Dialog
        v-model:visible="showMoveDialog"
        modal
        header="Move Task to Category"
        :style="{ width: '400px' }"
        :closable="true"
    >
      <div class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-app-primary mb-2">
            Move "{{ movingTask?.title }}" to:
          </label>
          <Select
              v-model="targetCategoryId"
              :options="availableCategoriesForMove"
              option-value="id"
              option-label="title"
              placeholder="Select category"
              class="w-full"
          />
        </div>

        <div class="flex justify-end space-x-3 pt-4 border-t border-app-primary">
          <Button
              label="Cancel"
              severity="secondary"
              @click="showMoveDialog = false"
          />
          <Button
              label="Move Task"
              @click="confirmMoveTask"
              :disabled="!targetCategoryId"
              :loading="moveLoading"
          />
        </div>
      </div>
    </Dialog>

    <!-- Bulk Move Dialog -->
    <Dialog
        v-model:visible="showBulkMoveDialog"
        modal
        header="Move Selected Tasks"
        :style="{ width: '400px' }"
        :closable="true"
    >
      <div class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-app-primary mb-2">
            Move {{ selectedTasks.length }} selected tasks to:
          </label>
          <Select
              v-model="bulkTargetCategoryId"
              :options="availableCategories"
              option-value="id"
              option-label="title"
              placeholder="Select category"
              class="w-full"
          />
        </div>

        <div class="flex justify-end space-x-3 pt-4 border-t border-app-primary">
          <Button
              label="Cancel"
              severity="secondary"
              @click="showBulkMoveDialog = false"
          />
          <Button
              label="Move Tasks"
              @click="confirmBulkMove"
              :disabled="!bulkTargetCategoryId"
              :loading="bulkMoveLoading"
          />
        </div>
      </div>
    </Dialog>
  </div>
</template>

<script setup lang="ts">
import { format } from 'date-fns'
import type { Task, TaskCreateRequest, TaskUpdateRequest } from '~/types/api'

// Page setup
definePageMeta({
  layout: 'default'
})

// Composables
const tasksStore = useTasksStore()
const categoriesStore = useCategoriesStore()
const toast = useToast()
const confirm = useConfirm()

// Reactive state
const searchTerm = ref('')
const selectedCategoryFilter = ref('all')
const statusFilter = ref('all')
const selectedTasks = ref<Task[]>([])
const showBulkActions = ref(false)

// Dialog states
const showCreateDialog = ref(false)
const showEditDialog = ref(false)
const showMoveDialog = ref(false)
const showBulkMoveDialog = ref(false)

// Form states
const formLoading = ref(false)
const moveLoading = ref(false)
const bulkMoveLoading = ref(false)
const editingTask = ref<Task | null>(null)
const movingTask = ref<Task | null>(null)
const targetCategoryId = ref<number | null>(null)
const bulkTargetCategoryId = ref<number | null>(null)

// Computed properties
const loading = computed(() => tasksStore.loading)

const categoryFilterOptions = computed(() => [
  { value: 'all', label: 'All Categories' },
  ...categoriesStore.activeCategories.map(cat => ({
    value: cat.id.toString(),
    label: cat.title
  }))
])

const statusFilterOptions = computed(() => [
  { value: 'all', label: 'All Tasks' },
  { value: 'active', label: 'Active Only' },
  { value: 'archived', label: 'Archived Only' }
])

const availableCategories = computed(() => categoriesStore.activeCategories)

const availableCategoriesForMove = computed(() => {
  if (!movingTask.value) return categoriesStore.activeCategories
  return categoriesStore.activeCategories.filter(cat => cat.id !== movingTask.value?.categoryId)
})

const filteredTasks = computed(() => {
  let tasks = tasksStore.tasks

  // Apply category filter
  if (selectedCategoryFilter.value !== 'all') {
    const categoryId = parseInt(selectedCategoryFilter.value)
    tasks = tasks.filter(task => task.categoryId === categoryId)
  }

  // Apply status filter
  switch (statusFilter.value) {
    case 'active':
      tasks = tasks.filter(task => !task.isArchived)
      break
    case 'archived':
      tasks = tasks.filter(task => task.isArchived)
      break
      // 'all' shows everything
  }

  // Apply search term
  if (searchTerm.value.trim()) {
    const term = searchTerm.value.toLowerCase()
    tasks = tasks.filter(task =>
        task.title.toLowerCase().includes(term) ||
        (task.description && task.description.toLowerCase().includes(term)) ||
        task.categoryTitle.toLowerCase().includes(term)
    )
  }

  return tasks
})

const totalTasks = computed(() => tasksStore.tasks.length)
const activeTasks = computed(() => tasksStore.tasks.filter(task => !task.isArchived).length)
const archivedTasks = computed(() => tasksStore.tasks.filter(task => task.isArchived).length)
const totalCategories = computed(() => {
  const categoryIds = new Set(tasksStore.tasks.map(task => task.categoryId))
  return categoryIds.size
})

// Methods
const formatDate = (dateString: string) => {
  return format(new Date(dateString), 'MMM d, yyyy')
}

const refreshTasks = async () => {
  try {
    await tasksStore.fetchTasks()
    await categoriesStore.fetchCategories()

    toast.add({
      severity: 'success',
      summary: 'Data Refreshed',
      detail: 'Tasks and categories have been updated',
      life: 2000
    })
  } catch (error: any) {
    toast.add({
      severity: 'error',
      summary: 'Refresh Failed',
      detail: error.message || 'Failed to refresh data',
      life: 3000
    })
  }
}

const clearSelection = () => {
  selectedTasks.value = []
  showBulkActions.value = false
}

// Task CRUD operations
const handleCreateTask = async (taskData: TaskCreateRequest) => {
  formLoading.value = true
  try {
    const newTask = await tasksStore.createTask(taskData)

    showCreateDialog.value = false

    toast.add({
      severity: 'success',
      summary: 'Task Created',
      detail: `Task "${newTask.title}" has been created successfully`,
      life: 3000
    })

    // Refresh categories to update task counts
    await categoriesStore.fetchCategories()

  } catch (error: any) {
    console.error('Error creating task:', error)
    toast.add({
      severity: 'error',
      summary: 'Creation Failed',
      detail: error.message || 'Failed to create task. Please try again.',
      life: 5000
    })
  } finally {
    formLoading.value = false
  }
}

const editTask = (task: Task) => {
  editingTask.value = { ...task }
  showEditDialog.value = true
}

const handleUpdateTask = async (taskData: TaskUpdateRequest) => {
  if (!editingTask.value) return

  formLoading.value = true
  try {
    const updatedTask = await tasksStore.updateTask(editingTask.value.id, taskData)

    showEditDialog.value = false
    editingTask.value = null

    toast.add({
      severity: 'success',
      summary: 'Task Updated',
      detail: `Task "${updatedTask.title}" has been updated successfully`,
      life: 3000
    })

  } catch (error: any) {
    console.error('Error updating task:', error)
    toast.add({
      severity: 'error',
      summary: 'Update Failed',
      detail: error.message || 'Failed to update task. Please try again.',
      life: 5000
    })
  } finally {
    formLoading.value = false
  }
}

const toggleArchiveTask = (task: Task) => {
  const action = task.isArchived ? 'restore' : 'archive'
  const actionPast = task.isArchived ? 'restored' : 'archived'

  confirm.require({
    message: `Are you sure you want to ${action} "${task.title}"?`,
    header: `${action.charAt(0).toUpperCase() + action.slice(1)} Task`,
    icon: task.isArchived ? 'pi pi-replay' : 'pi pi-book',
    accept: async () => {
      try {
        await tasksStore.archiveTask(task.id, !task.isArchived)

        toast.add({
          severity: 'success',
          summary: `Task ${actionPast.charAt(0).toUpperCase() + actionPast.slice(1)}`,
          detail: `Task "${task.title}" has been ${actionPast}`,
          life: 3000
        })
      } catch (error: any) {
        toast.add({
          severity: 'error',
          summary: 'Operation Failed',
          detail: error.message || `Failed to ${action} task`,
          life: 5000
        })
      }
    }
  })
}

const deleteTask = (task: Task) => {
  confirm.require({
    message: `Are you sure you want to delete "${task.title}"? This action cannot be undone.`,
    header: 'Delete Task',
    icon: 'pi pi-trash',
    rejectClass: 'p-button-secondary p-button-outlined',
    acceptClass: 'p-button-danger',
    accept: async () => {
      try {
        await tasksStore.deleteTask(task.id)

        toast.add({
          severity: 'success',
          summary: 'Task Deleted',
          detail: `Task "${task.title}" has been deleted`,
          life: 3000
        })

        // Refresh categories to update task counts
        await categoriesStore.fetchCategories()
      } catch (error: any) {
        toast.add({
          severity: 'error',
          summary: 'Delete Failed',
          detail: error.message || 'Failed to delete task',
          life: 5000
        })
      }
    }
  })
}

// Move operations
const moveTask = (task: Task) => {
  movingTask.value = task
  targetCategoryId.value = null
  showMoveDialog.value = true
}

const confirmMoveTask = async () => {
  if (!movingTask.value || !targetCategoryId.value) return

  moveLoading.value = true
  try {
    await tasksStore.moveTaskToCategory(movingTask.value.id, targetCategoryId.value)

    showMoveDialog.value = false
    movingTask.value = null
    targetCategoryId.value = null

    toast.add({
      severity: 'success',
      summary: 'Task Moved',
      detail: 'Task has been moved to the selected category',
      life: 3000
    })

    // Refresh categories to update task counts
    await categoriesStore.fetchCategories()
  } catch (error: any) {
    toast.add({
      severity: 'error',
      summary: 'Move Failed',
      detail: error.message || 'Failed to move task',
      life: 5000
    })
  } finally {
    moveLoading.value = false
  }
}

// Bulk operations
const bulkArchive = () => {
  const count = selectedTasks.value.length

  confirm.require({
    message: `Are you sure you want to archive ${count} selected tasks?`,
    header: 'Bulk Archive',
    icon: 'pi pi-book',
    accept: async () => {
      try {
        const promises = selectedTasks.value.map(task =>
            tasksStore.archiveTask(task.id, true)
        )

        await Promise.all(promises)

        toast.add({
          severity: 'success',
          summary: 'Bulk Archive Complete',
          detail: `${count} tasks have been archived`,
          life: 3000
        })

        clearSelection()
      } catch (error: any) {
        toast.add({
          severity: 'error',
          summary: 'Bulk Archive Failed',
          detail: 'Some tasks could not be archived',
          life: 5000
        })
      }
    }
  })
}

const bulkUnarchive = () => {
  const count = selectedTasks.value.length

  confirm.require({
    message: `Are you sure you want to unarchive ${count} selected tasks?`,
    header: 'Bulk Unarchive',
    icon: 'pi pi-refresh',
    accept: async () => {
      try {
        const promises = selectedTasks.value.map(task =>
            tasksStore.archiveTask(task.id, false)
        )

        await Promise.all(promises)

        toast.add({
          severity: 'success',
          summary: 'Bulk Unarchive Complete',
          detail: `${count} tasks have been unarchived`,
          life: 3000
        })

        clearSelection()
      } catch (error: any) {
        toast.add({
          severity: 'error',
          summary: 'Bulk Unarchive Failed',
          detail: 'Some tasks could not be unarchived',
          life: 5000
        })
      }
    }
  })
}

const confirmBulkMove = async () => {
  if (!bulkTargetCategoryId.value || !selectedTasks.value.length) return

  bulkMoveLoading.value = true
  try {
    const promises = selectedTasks.value.map(task =>
        tasksStore.moveTaskToCategory(task.id, bulkTargetCategoryId.value!)
    )

    await Promise.all(promises)

    showBulkMoveDialog.value = false
    bulkTargetCategoryId.value = null

    toast.add({
      severity: 'success',
      summary: 'Bulk Move Complete',
      detail: `${selectedTasks.value.length} tasks have been moved`,
      life: 3000
    })

    clearSelection()

    // Refresh categories to update task counts
    await categoriesStore.fetchCategories()
  } catch (error: any) {
    toast.add({
      severity: 'error',
      summary: 'Bulk Move Failed',
      detail: 'Some tasks could not be moved',
      life: 5000
    })
  } finally {
    bulkMoveLoading.value = false
  }
}

// Export operations
const exportTasks = () => {
  if (!tasksStore.tasks.length) {
    toast.add({
      severity: 'warn',
      summary: 'No Data',
      detail: 'No tasks to export',
      life: 3000
    })
    return
  }

  const csvData = tasksStore.tasks.map(task => ({
    ID: task.id,
    Title: task.title,
    Description: task.description || '',
    Category: task.categoryTitle,
    'Category ID': task.categoryId,
    Color: task.color,
    Icon: task.icon || '',
    'Is Archived': task.isArchived ? 'Yes' : 'No',
    'Sort Order': task.sortOrder,
    'Created At': formatDate(task.createdAt),
    'Updated At': formatDate(task.updatedAt)
  }))

  exportToCsv(csvData, 'tasks')
}

const exportSelected = () => {
  if (!selectedTasks.value.length) {
    toast.add({
      severity: 'warn',
      summary: 'No Selection',
      detail: 'No tasks selected for export',
      life: 3000
    })
    return
  }

  const csvData = selectedTasks.value.map(task => ({
    ID: task.id,
    Title: task.title,
    Description: task.description || '',
    Category: task.categoryTitle,
    'Category ID': task.categoryId,
    Color: task.color,
    Icon: task.icon || '',
    'Is Archived': task.isArchived ? 'Yes' : 'No',
    'Sort Order': task.sortOrder,
    'Created At': formatDate(task.createdAt),
    'Updated At': formatDate(task.updatedAt)
  }))

  exportToCsv(csvData, 'selected-tasks')
}

const exportToCsv = (data: any[], filename: string) => {
  const csvContent = [
    Object.keys(data[0]).join(','),
    ...data.map(row => Object.values(row).map(val => `"${val}"`).join(','))
  ].join('\n')

  const blob = new Blob([csvContent], { type: 'text/csv' })
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `${filename}-${format(new Date(), 'yyyy-MM-dd')}.csv`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)

  toast.add({
    severity: 'success',
    summary: 'Export Complete',
    detail: `${filename}.csv has been downloaded`,
    life: 3000
  })
}

// Initialize data on mount
onMounted(() => {
  if (!tasksStore.tasks.length) {
    tasksStore.fetchTasks()
  }
  if (!categoriesStore.categories.length) {
    categoriesStore.fetchCategories()
  }
})
</script>