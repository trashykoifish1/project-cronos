<template>
  <div class="task-sidebar bg-app-secondary border-r border-app-primary h-full flex flex-col">
    <!-- Header -->
    <div class="p-4 border-b border-app-primary">
      <div class="flex items-center justify-between mb-3">
        <h2 class="text-lg font-semibold text-app-primary">
          Available Tasks
        </h2>
        <Button
            icon="pi pi-refresh"
            size="small"
            severity="secondary"
            @click="refreshTasks"
            :loading="tasksStore.loading"
            class="w-8 h-8"
        />
      </div>

      <!-- Selected Task Display -->
      <div v-if="selectedTask" class="mb-3">
        <div class="text-sm text-app-secondary mb-1">Selected Task:</div>
        <div class="flex items-center p-2 bg-app-blue-50 border border-app-blue-200 rounded-lg">
          <div
              class="w-4 h-4 rounded mr-2 flex-shrink-0"
              :style="{ backgroundColor: selectedTask.color }"
          ></div>
          <div class="flex-1 min-w-0">
            <div class="font-medium text-app-blue-900 truncate">{{ selectedTask.title }}</div>
            <div class="text-xs text-app-blue-700 truncate">{{ selectedTask.categoryTitle }}</div>
          </div>
          <Button
              icon="pi pi-times"
              size="small"
              text
              @click="clearSelection"
              class="ml-2 w-6 h-6 text-app-blue-600"
          />
        </div>
      </div>

      <!-- Quick Actions -->
      <div class="flex space-x-2">
        <Button
            icon="pi pi-plus"
            label="New Task"
            size="small"
            severity="secondary"
            @click="showCreateTask = true"
            class="flex-1"
        />
        <Button
            icon="pi pi-cog"
            size="small"
            severity="secondary"
            @click="showManageTasks = true"
            class="w-8 h-8"
        />
      </div>
    </div>

    <!-- Task List -->
    <div class="flex-1 overflow-y-auto">
      <!-- Loading State -->
      <div v-if="tasksStore.loading" class="p-4 text-center">
        <ProgressSpinner size="32px" />
        <div class="text-sm text-app-secondary mt-2">Loading tasks...</div>
      </div>

      <!-- Error State -->
      <div v-else-if="tasksStore.error" class="p-4">
        <Message severity="error" :closable="false">
          <template #icon>
            <i class="pi pi-exclamation-triangle"></i>
          </template>
          {{ tasksStore.error }}
        </Message>
        <Button
            label="Retry"
            icon="pi pi-refresh"
            size="small"
            @click="refreshTasks"
            class="mt-2 w-full"
        />
      </div>

      <!-- Tasks by Category -->
      <div v-else-if="Object.keys(tasksByCategory).length > 0" class="p-2">
        <div
            v-for="(tasks, categoryName) in tasksByCategory"
            :key="categoryName"
            class="mb-4 last:mb-2"
        >
          <!-- Category Header -->
          <div class="mb-2">
            <div class="text-sm font-medium text-app-primary px-2">
              {{ categoryName }}
              <span class="text-xs text-app-secondary ml-1">({{ tasks.length }})</span>
            </div>
          </div>

          <!-- Tasks in Category -->
          <div class="space-y-1">
            <div
                v-for="task in tasks"
                :key="task.id"
                @click="selectTask(task)"
                @contextmenu="handleTaskRightClick($event, task)"
                class="task-item flex items-center p-2 rounded-lg cursor-pointer transition-colors duration-150"
                :class="{
                'bg-app-blue-100 border border-app-blue-200': selectedTask?.id === task.id,
                'hover:bg-app-tertiary border border-transparent': selectedTask?.id !== task.id
              }"
            >
              <!-- Task Color -->
              <div
                  class="w-4 h-4 rounded mr-3 flex-shrink-0 border border-app-primary"
                  :style="{ backgroundColor: task.color }"
              ></div>

              <!-- Task Info -->
              <div class="flex-1 min-w-0">
                <div class="font-medium text-app-primary truncate">
                  {{ task.title }}
                </div>
                <div v-if="task.description" class="text-xs text-app-secondary truncate">
                  {{ task.description }}
                </div>
              </div>

              <!-- Task Icon -->
              <div v-if="task.icon" class="ml-2 flex-shrink-0">
                <i :class="`pi pi-${task.icon} text-app-secondary text-sm`"></i>
              </div>

              <!-- Selection Indicator -->
              <div v-if="selectedTask?.id === task.id" class="ml-2 flex-shrink-0">
                <i class="pi pi-check text-app-blue-600 text-sm"></i>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Empty State -->
      <div v-else class="p-4 text-center">
        <div class="text-app-secondary mb-2">
          <i class="pi pi-bookmark text-3xl"></i>
        </div>
        <div class="text-sm text-app-secondary mb-3">No tasks available</div>
        <Button
            label="Create First Task"
            icon="pi pi-plus"
            size="small"
            @click="showCreateTask = true"
        />
      </div>
    </div>

    <!-- Footer Stats -->
    <div class="p-3 border-t border-app-primary bg-app-primary">
      <div class="text-xs text-app-secondary text-center">
        {{ totalActiveTasks }} active tasks across {{ totalCategories }} categories
      </div>
    </div>

    <!-- Modals/Dialogs -->
    <!-- Create Task Dialog -->
    <Dialog
        v-model:visible="showCreateTask"
        modal
        header="Create New Task"
        :style="{ width: '500px' }"
        :closable="true"
    >
      <TaskForm
          :loading="taskFormLoading"
          @submit-create="handleCreateTask"
          @cancel="showCreateTask = false"
      />
    </Dialog>

    <!-- Manage Tasks Dialog -->
    <Dialog
        v-model:visible="showManageTasks"
        modal
        header="Manage Tasks"
        :style="{ width: '800px' }"
        :closable="true"
    >
      <div class="space-y-4">
        <!-- Tasks List -->
        <div class="max-h-96 overflow-y-auto">
          <DataTable
              :value="tasksStore.activeTasks"
              :paginator="false"
              stripedRows
              class="p-datatable-sm"
          >
            <Column field="title" header="Task" class="w-1/3">
              <template #body="{ data }">
                <div class="flex items-center space-x-2">
                  <div
                      class="w-4 h-4 rounded"
                      :style="{ backgroundColor: data.color }"
                  ></div>
                  <span class="font-medium">{{ data.title }}</span>
                  <i v-if="data.icon" :class="`pi pi-${data.icon} text-app-secondary`"></i>
                </div>
              </template>
            </Column>

            <Column field="categoryTitle" header="Category" class="w-1/4">
              <template #body="{ data }">
                <span class="text-app-secondary">{{ data.categoryTitle }}</span>
              </template>
            </Column>

            <Column field="sortOrder" header="Order" class="w-1/6 text-center">
              <template #body="{ data }">
                <span class="text-app-secondary">{{ data.sortOrder }}</span>
              </template>
            </Column>

            <Column header="Actions" class="w-1/4">
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
                      icon="pi pi-check"
                      size="small"
                      severity="contrast"
                      @click="selectTaskFromManage(data)"
                      v-tooltip="'Select Task'"
                  />
                  <Button
                      icon="pi pi-eye-slash"
                      size="small"
                      severity="danger"
                      @click="archiveTask(data)"
                      v-tooltip="'Archive Task'"
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
        </div>

        <!-- Actions -->
        <div class="flex justify-between items-center pt-4 border-t border-app-primary">
          <Button
              label="Create New Task"
              icon="pi pi-plus"
              @click="showCreateTaskFromManage"
          />

          <div class="flex space-x-2">
            <Button
                label="Manage Categories"
                icon="pi pi-folder"
                severity="secondary"
                @click="showCategoryManagement = true"
            />
            <Button
                label="Close"
                severity="secondary"
                @click="showManageTasks = false"
            />
          </div>
        </div>
      </div>
    </Dialog>

    <!-- Edit Task Dialog -->
    <Dialog
        v-model:visible="showEditTask"
        modal
        header="Edit Task"
        :style="{ width: '500px' }"
        :closable="true"
    >
      <TaskForm
          :task="editingTask"
          :loading="taskFormLoading"
          @submit-update="handleUpdateTask"
          @cancel="showEditTask = false"
      />
    </Dialog>

    <!-- Category Management Dialog -->
    <Dialog
        v-model:visible="showCategoryManagement"
        modal
        header="Manage Categories"
        :style="{ width: '700px' }"
        :closable="true"
    >
      <div class="space-y-4">
        <!-- Categories List -->
        <div class="max-h-80 overflow-y-auto">
          <DataTable
              :value="categoriesStore.categories"
              :paginator="false"
              stripedRows
              class="p-datatable-sm"
          >
            <Column field="title" header="Category" class="w-1/3">
              <template #body="{ data }">
                <div class="flex items-center space-x-2">
                  <span class="font-medium">{{ data.title }}</span>
                  <Tag v-if="data.isDefault" severity="info" value="Default" class="text-xs" />
                  <Tag v-if="data.isArchived" severity="warning" value="Archived" class="text-xs" />
                </div>
              </template>
            </Column>

            <Column field="activeTaskCount" header="Tasks" class="w-1/6 text-center">
              <template #body="{ data }">
                <span class="text-app-secondary">{{ data.activeTaskCount }}/{{ data.taskCount }}</span>
              </template>
            </Column>

            <Column field="sortOrder" header="Order" class="w-1/6 text-center">
              <template #body="{ data }">
                <span class="text-app-secondary">{{ data.sortOrder }}</span>
              </template>
            </Column>

            <Column header="Actions" class="w-1/3">
              <template #body="{ data }">
                <div class="flex space-x-1">
                  <Button
                      icon="pi pi-pencil"
                      size="small"
                      severity="secondary"
                      @click="editCategory(data)"
                      v-tooltip="'Edit Category'"
                  />
                  <Button
                      icon="pi pi-plus"
                      size="small"
                      severity="info"
                      @click="createTaskInCategory(data)"
                      v-tooltip="'Add Task'"
                  />
                </div>
              </template>
            </Column>
          </DataTable>
        </div>

        <!-- Actions -->
        <div class="flex justify-between items-center pt-4 border-t border-app-primary">
          <Button
              label="Create New Category"
              icon="pi pi-plus"
              @click="showCreateCategory = true"
          />

          <Button
              label="Close"
              severity="secondary"
              @click="showCategoryManagement = false"
          />
        </div>
      </div>
    </Dialog>

    <!-- Create Category Dialog -->
    <Dialog
        v-model:visible="showCreateCategory"
        modal
        header="Create New Category"
        :style="{ width: '500px' }"
        :closable="true"
    >
      <CategoryForm
          :loading="categoryFormLoading"
          @submit-create="handleCreateCategory"
          @cancel="showCreateCategory = false"
      />
    </Dialog>

    <!-- Edit Category Dialog -->
    <Dialog
        v-model:visible="showEditCategory"
        modal
        header="Edit Category"
        :style="{ width: '500px' }"
        :closable="true"
    >
      <CategoryForm
          :category="editingCategory"
          :loading="categoryFormLoading"
          @submit-update="handleUpdateCategory"
          @cancel="showEditCategory = false"
      />
    </Dialog>

    <ContextMenu ref="contextMenu" :model="contextMenuItems" />
  </div>
</template>

<script setup lang="ts">
import type { Task, TaskCreateRequest, TaskUpdateRequest, Category, CategoryCreateRequest, CategoryUpdateRequest } from '~/types/api'

// Props
interface Props {
  width?: string
}

const props = withDefaults(defineProps<Props>(), {
  width: '320px'
})

// Stores
const tasksStore = useTasksStore()
const categoriesStore = useCategoriesStore()

// Composables
const toast = useToast()
const confirm = useConfirm()

// Local state
const showCreateTask = ref(false)
const showManageTasks = ref(false)
const showEditTask = ref(false)
const showCategoryManagement = ref(false)
const showCreateCategory = ref(false)
const showEditCategory = ref(false)

const taskFormLoading = ref(false)
const categoryFormLoading = ref(false)
const editingTask = ref<Task | null>(null)
const editingCategory = ref<Category | null>(null)
const selectedTaskForContext = ref<Task | null>(null)

// Computed
const selectedTask = computed(() => tasksStore.selectedTask)

const tasksByCategory = computed(() => tasksStore.tasksByCategories)

const totalActiveTasks = computed(() => tasksStore.activeTasks.length)

const totalCategories = computed(() => Object.keys(tasksByCategory.value).length)

const contextMenuItems = computed(() => [
  {
    label: 'Edit',
    icon: 'pi pi-pencil',
    command: () => editTask(selectedTaskForContext.value)
  },
  {
    label: 'Archive',
    icon: 'pi pi-eye-slash',
    command: () => archiveTask(selectedTaskForContext.value)
  },
  {
    label: 'Delete',
    icon: 'pi pi-trash',
    command: () => deleteTask(selectedTaskForContext.value)
  },
])

const contextMenu = ref();

const handleTaskRightClick = (event: Event, task: Task) => {
  if (task) {
    selectedTaskForContext.value = task
    contextMenu.value.show(event);
  }
};

// Methods
const selectTask = (task: Task) => {
  tasksStore.selectTask(task)

  // Show feedback
  // toast.add({
  //   severity: 'info',
  //   summary: 'Task Selected',
  //   detail: `Selected "${task.title}" for time tracking`,
  //   life: 2000
  // })
}

const clearSelection = () => {
  tasksStore.selectTask(null)

  toast.add({
    severity: 'info',
    summary: 'Selection Cleared',
    detail: 'No task selected',
    life: 2000
  })
}

const refreshTasks = async () => {
  await tasksStore.fetchTasks()

  if (!tasksStore.error) {
    toast.add({
      severity: 'success',
      summary: 'Tasks Refreshed',
      detail: `Loaded ${totalActiveTasks.value} tasks`,
      life: 2000
    })
  }
}

// Load tasks and categories on mount
onMounted(async () => {
  if (tasksStore.tasks.length === 0) {
    await refreshTasks()
  }

  if (categoriesStore.categories.length === 0) {
    await categoriesStore.fetchCategories()
  }
})

// Additional missing methods that were referenced in template
const showCreateTaskFromManage = () => {
  showManageTasks.value = false
  showCreateTask.value = true
}

const createTaskInCategory = (category: Category) => {
  // Close category management and open task creation
  showCategoryManagement.value = false
  showCreateTask.value = true

  // Note: We could pre-select the category in the form here if needed
}

// Style for dynamic width
const sidebarStyle = computed(() => ({
  width: props.width,
  minWidth: props.width
}))

// Task Management Methods
const handleCreateTask = async (data: TaskCreateRequest) => {
  taskFormLoading.value = true
  try {
    const newTask = await tasksStore.createTask(data)

    showCreateTask.value = false

    toast.add({
      severity: 'success',
      summary: 'Task Created',
      detail: `Task "${newTask.title}" has been created successfully`,
      life: 3000
    })

    // Auto-select the newly created task
    tasksStore.selectTask(newTask)

  } catch (error: any) {
    console.error('Error creating task:', error)
    toast.add({
      severity: 'error',
      summary: 'Creation Failed',
      detail: error.message || 'Failed to create task. Please try again.',
      life: 5000
    })
  } finally {
    taskFormLoading.value = false
  }
}

const handleUpdateTask = async (taskData: TaskUpdateRequest) => {
  if (!editingTask.value) return

  taskFormLoading.value = true
  try {
    const updatedTask = await tasksStore.updateTask(editingTask.value.id, taskData)

    showEditTask.value = false
    editingTask.value = null

    toast.add({
      severity: 'success',
      summary: 'Task Updated',
      detail: `Task "${updatedTask.title}" has been updated successfully`,
      life: 3000
    })

    // If this was the selected task, update the selection
    if (selectedTask.value?.id === updatedTask.id) {
      tasksStore.selectTask(updatedTask)
    }

  } catch (error: any) {
    console.error('Error updating task:', error)
    toast.add({
      severity: 'error',
      summary: 'Update Failed',
      detail: error.message || 'Failed to update task. Please try again.',
      life: 5000
    })
  } finally {
    taskFormLoading.value = false
  }
}

const editTask = (task: Task | null) => {
  if (task !== null) {
    editingTask.value = { ...task }
    showEditTask.value = true

    // Close manage dialog if open
    showManageTasks.value = false
  }
}

const selectTaskFromManage = (task: Task) => {
  selectTask(task)
  showManageTasks.value = false
}

const archiveTask = (task: Task | null) => {
  if (task !== null) {
    confirm.require({
      message: `Are you sure you want to archive "${task.title}"? This will hide it from the active tasks list but preserve all time entries.`,
      header: 'Archive Task',
      icon: 'pi pi-exclamation-triangle',
      acceptClass: 'p-button-warning',
      accept: async () => {
        try {
          await tasksStore.archiveTask(task.id, true)

          toast.add({
            severity: 'info',
            summary: 'Task Archived',
            detail: `Task "${task.title}" has been archived`,
            life: 3000
          })

          // Clear selection if this was the selected task
          if (selectedTask.value?.id === task.id) {
            clearSelection()
          }

        } catch (error: any) {
          console.error('Error archiving task:', error)
          toast.add({
            severity: 'error',
            summary: 'Archive Failed',
            detail: error.message || 'Failed to archive task. Please try again.',
            life: 5000
          })
        }
      }
    })
  }
}

const deleteTask = (task: Task | null) => {
  if (task !== null) {
    confirm.require({
      message: `Are you sure you want to delete "${task.title}"? This will delete it from the active tasks and all time entries.`,
      header: 'Delete Task',
      icon: 'pi pi-exclamation-triangle',
      acceptClass: 'p-button-warning',
      accept: async () => {
        try {
          await tasksStore.deleteTask(task.id)

          toast.add({
            severity: 'info',
            summary: 'Task Deleted',
            detail: `Task "${task.title}" has been deleted`,
            life: 3000
          })

          // Clear selection if this was the selected task
          if (selectedTask.value?.id === task.id) {
            clearSelection()
          }

        } catch (error: any) {
          console.error('Error deleting task:', error)
          toast.add({
            severity: 'error',
            summary: 'Delete Failed',
            detail: error.message || 'Failed to delete task. Please try again.',
            life: 5000
          })
        }
      }
    })
  }
}

// Category Management Methods
const handleCreateCategory = async (categoryData: CategoryCreateRequest) => {
  categoryFormLoading.value = true
  try {
    const newCategory = await categoriesStore.createCategory(categoryData)

    showCreateCategory.value = false

    toast.add({
      severity: 'success',
      summary: 'Category Created',
      detail: `Category "${newCategory.title}" has been created successfully`,
      life: 3000
    })

    // Refresh tasks to update categories
    await tasksStore.fetchTasks()

  } catch (error: any) {
    console.error('Error creating category:', error)
    toast.add({
      severity: 'error',
      summary: 'Creation Failed',
      detail: error.message || 'Failed to create category. Please try again.',
      life: 5000
    })
  } finally {
    categoryFormLoading.value = false
  }
}

const handleUpdateCategory = async (categoryData: CategoryUpdateRequest) => {
  if (!editingCategory.value) return

  categoryFormLoading.value = true
  try {
    const updatedCategory = await categoriesStore.updateCategory(editingCategory.value.id, categoryData)

    showEditCategory.value = false
    editingCategory.value = null

    toast.add({
      severity: 'success',
      summary: 'Category Updated',
      detail: `Category "${updatedCategory.title}" has been updated successfully`,
      life: 3000
    })

    // Refresh tasks to update categories
    await tasksStore.fetchTasks()

  } catch (error: any) {
    console.error('Error updating category:', error)
    toast.add({
      severity: 'error',
      summary: 'Update Failed',
      detail: error.message || 'Failed to update category. Please try again.',
      life: 5000
    })
  } finally {
    categoryFormLoading.value = false
  }
}

const editCategory = (category: Category) => {
  editingCategory.value = { ...category }
  showEditCategory.value = true

  // Close category management dialog if open
  showCategoryManagement.value = false
}

// Utility Methods
const handleTaskFormError = (error: any) => {
  let message = 'An unexpected error occurred'

  if (error.response?.data?.fieldErrors) {
    // Handle validation errors
    const fieldErrors = error.response.data.fieldErrors
    const firstError = Object.values(fieldErrors)[0]
    message = Array.isArray(firstError) ? firstError[0] : firstError
  } else if (error.response?.data?.message) {
    message = error.response.data.message
  } else if (error.message) {
    message = error.message
  }

  return message
}

const handleCategoryFormError = (error: any) => {
  let message = 'An unexpected error occurred'

  if (error.response?.data?.fieldErrors) {
    const fieldErrors = error.response.data.fieldErrors
    const firstError = Object.values(fieldErrors)[0]
    message = Array.isArray(firstError) ? firstError[0] : firstError
  } else if (error.response?.data?.message) {
    message = error.response.data.message
  } else if (error.message) {
    message = error.message
  }

  return message
}

// Keyboard Shortcuts (Optional Enhancement)
const handleKeyboardShortcuts = (event: KeyboardEvent) => {
  // Ctrl/Cmd + N = Create new task
  if ((event.ctrlKey || event.metaKey) && event.key === 'n') {
    event.preventDefault()
    showCreateTask.value = true
  }

  // Ctrl/Cmd + M = Manage tasks
  if ((event.ctrlKey || event.metaKey) && event.key === 'm') {
    event.preventDefault()
    showManageTasks.value = true
  }

  // Escape = Clear selection
  if (event.key === 'Escape' && selectedTask.value) {
    clearSelection()
  }
}

// Add keyboard event listeners
onMounted(() => {
  document.addEventListener('keydown', handleKeyboardShortcuts)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeyboardShortcuts)
})

// Cleanup on component unmount
onUnmounted(() => {
  // Clear any pending loading states
  taskFormLoading.value = false
  categoryFormLoading.value = false
  editingTask.value = null
  editingCategory.value = null
})
</script>

<style scoped>
.task-sidebar {
  width: v-bind('props.width');
  min-width: v-bind('props.width');
}

.task-item {
  transition: all 0.15s ease-in-out;
}

.task-item:hover {
  transform: translateX(2px);
}
</style>