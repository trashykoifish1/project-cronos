<template>
  <div class="flex-1 p-6">
    <!-- Page Header -->
    <div class="mb-6">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-app-primary">Manage Categories</h1>
          <p class="text-app-secondary mt-1">
            Organize your time tracking with custom categories and hierarchical structure
          </p>
        </div>

        <!-- Action Buttons -->
        <div class="flex items-center space-x-3">
          <Button
              label="Create Category"
              icon="pi pi-plus"
              @click="showCreateDialog = true"
          />
          <Button
              label="Export Categories"
              icon="pi pi-download"
              severity="secondary"
              outlined
              @click="exportCategories"
          />
          <Button
              icon="pi pi-refresh"
              severity="secondary"
              @click="refreshCategories"
              v-tooltip="'Refresh Categories'"
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
                {{ totalCategories }}
              </div>
              <div class="text-sm text-app-secondary">Total Categories</div>
            </div>
          </template>
        </Card>

        <Card class="text-center">
          <template #content>
            <div class="py-4">
              <div class="text-2xl font-bold text-app-green-600 mb-2">
                {{ activeCategories }}
              </div>
              <div class="text-sm text-app-secondary">Active Categories</div>
            </div>
          </template>
        </Card>

        <Card class="text-center">
          <template #content>
            <div class="py-4">
              <div class="text-2xl font-bold text-app-purple-600 mb-2">
                {{ totalTasks }}
              </div>
              <div class="text-sm text-app-secondary">Total Tasks</div>
            </div>
          </template>
        </Card>

        <Card class="text-center">
          <template #content>
            <div class="py-4">
              <div class="text-2xl font-bold text-app-orange-600 mb-2">
                {{ defaultCategory?.title || 'None' }}
              </div>
              <div class="text-sm text-app-secondary">Default Category</div>
            </div>
          </template>
        </Card>
      </div>
    </div>

    <!-- Categories Table -->
    <Card>
      <template #title>
        <div class="flex items-center justify-between">
          <div class="flex items-center">
            <i class="pi pi-folder text-app-blue-600 mr-2"></i>
            Categories List
          </div>
          <div class="flex items-center space-x-2">
            <Button
                label="Bulk Actions"
                icon="pi pi-list"
                size="small"
                severity="secondary"
                outlined
                @click="showBulkActions = !showBulkActions"
                :disabled="selectedCategories.length === 0"
            />
            <span class="text-sm text-app-secondary">
              {{ filteredCategories.length }} categories
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
                  placeholder="Search categories..."
                  class="w-full"
                  size="small"
              />
            </IconField>
          </div>

          <div class="flex items-center space-x-3">
            <label class="text-sm font-medium text-app-secondary">Show:</label>
            <Select
                v-model="filterType"
                :options="filterCategories"
                option-value="value"
                option-label="label"
                size="small"
            >
            </Select>
          </div>
        </div>
        <!-- Bulk Actions Panel -->
        <div v-if="showBulkActions && selectedCategories.length > 0"
             class="bg-app-blue-50 border border-app-blue-200 rounded-lg p-4 mb-4">
          <div class="flex items-center justify-between">
            <div class="flex items-center space-x-4">
              <span class="text-sm font-medium text-app-blue-800">
                {{ selectedCategories.length }} categories selected
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
                  icon="pi pi-book"
                  size="small"
                  severity="warning"
                  @click="bulkUnarchive"
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
            :value="filteredCategories"
            v-model:selection="selectedCategories"
            dataKey="id"
            :paginator="true"
            :rows="10"
            :rowsPerPageOptions="[5, 10, 20, 50]"
            stripedRows
            responsiveLayout="scroll"
            :globalFilterFields="['title', 'description']"
            :loading="loading"
        >
          <Column selectionMode="multiple" headerStyle="width: 3rem"></Column>

          <Column field="title" header="Category" sortable class="min-w-48">
            <template #body="{ data }">
              <div class="flex items-center space-x-2">
                <span class="font-medium text-app-primary">{{ data.title }}</span>
                <Tag v-if="data.isDefault" severity="info" value="Default" class="text-xs" />
                <Tag v-if="data.isArchived" severity="warning" value="Archived" class="text-xs" />
              </div>
              <div v-if="data.description" class="text-sm text-app-secondary mt-1">
                {{ data.description }}
              </div>
            </template>
          </Column>

          <Column field="activeTaskCount" header="Tasks" sortable class="text-center min-w-24">
            <template #body="{ data }">
              <div class="text-center">
                <span class="font-medium text-app-primary">{{ data.activeTaskCount }}</span>
                <span class="text-app-secondary">/{{ data.taskCount }}</span>
                <div class="text-xs text-app-secondary">active/total</div>
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

          <Column header="Actions" class="min-w-40">
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
                    @click="addTaskToCategory(data)"
                    v-tooltip="'Add Task'"
                />
                <Button
                    :icon="data.isArchived ? 'pi pi-replay' : 'pi pi-book'"
                    size="small"
                    :severity="data.isArchived ? 'success' : 'warning'"
                    @click="toggleArchiveCategory(data)"
                    :v-tooltip="data.isArchived ? 'Restore Category' : 'Archive Category'"
                />
                <Button
                    icon="pi pi-trash"
                    size="small"
                    severity="danger"
                    @click="deleteCategory(data)"
                    v-tooltip="'Delete Category'"
                    :disabled="data.isDefault"
                />
              </div>
            </template>
          </Column>
        </DataTable>
      </template>
    </Card>

    <!-- Create Category Dialog -->
    <Dialog
        v-model:visible="showCreateDialog"
        modal
        header="Create New Category"
        :style="{ width: '500px' }"
        :closable="true"
    >
      <CategoryForm
          :loading="formLoading"
          @submit-create="handleCreateCategory"
          @cancel="showCreateDialog = false"
      />
    </Dialog>

    <!-- Edit Category Dialog -->
    <Dialog
        v-model:visible="showEditDialog"
        modal
        header="Edit Category"
        :style="{ width: '500px' }"
        :closable="true"
    >
      <CategoryForm
          :category="editingCategory"
          :loading="formLoading"
          @submit-update="handleUpdateCategory"
          @cancel="showEditDialog = false"
      />
    </Dialog>

    <!-- Add Task Dialog -->
    <Dialog
        v-model:visible="showAddTaskDialog"
        modal
        header="Add Task to Category"
        :style="{ width: '500px' }"
        :closable="true"
    >
      <TaskForm
          :loading="taskFormLoading"
          @submit-create="handleCreateTask"
          @cancel="showAddTaskDialog = false"
          :passed-category-id="targetCategoryForTask ? targetCategoryForTask.id : undefined"
      />
    </Dialog>
  </div>
</template>

<script setup lang="ts">
import { format } from 'date-fns'
import type { Category, CategoryCreateRequest, CategoryUpdateRequest, TaskCreateRequest } from '~/types/api'

// Page setup
definePageMeta({
  layout: 'default'
})

// Composables
const categoriesStore = useCategoriesStore()
const tasksStore = useTasksStore()
const toast = useToast()
const confirm = useConfirm()

// Reactive state
const searchTerm = ref('')
const filterType = ref('all')
const selectedCategories = ref<Category[]>([])
const showBulkActions = ref(false)

// Dialog states
const showCreateDialog = ref(false)
const showEditDialog = ref(false)
const showAddTaskDialog = ref(false)

// Form states
const formLoading = ref(false)
const taskFormLoading = ref(false)
const editingCategory = ref<Category | null>(null)
const targetCategoryForTask = ref<Category | null>(null)

// Computed properties
const loading = computed(() => categoriesStore.loading)

const filterCategories = computed(() => [
  { value: 'all', label: 'All Categories' },
  { value: 'active', label: 'Active Only' },
  { value: 'archived', label: 'Archived Only' },
  { value: 'default', label: 'Default Category' }
])

const filteredCategories = computed(() => {
  let categories = categoriesStore.categories

  // Apply filter type
  switch (filterType.value) {
    case 'active':
      categories = categories.filter(cat => !cat.isArchived)
      break
    case 'archived':
      categories = categories.filter(cat => cat.isArchived)
      break
    case 'default':
      categories = categories.filter(cat => cat.isDefault)
      break
      // 'all' shows everything
  }

  // Apply search term
  if (searchTerm.value.trim()) {
    const term = searchTerm.value.toLowerCase()
    categories = categories.filter(cat =>
        cat.title.toLowerCase().includes(term) ||
        (cat.description && cat.description.toLowerCase().includes(term))
    )
  }

  return categories
})

const totalCategories = computed(() => categoriesStore.categories.length)
const activeCategories = computed(() => categoriesStore.categories.filter(cat => !cat.isArchived).length)
const totalTasks = computed(() => categoriesStore.categories.reduce((sum, cat) => sum + cat.taskCount, 0))
const defaultCategory = computed(() => categoriesStore.categories.find(cat => cat.isDefault))

// Methods
const formatDate = (dateString: string) => {
  return format(new Date(dateString), 'MMM d, yyyy')
}

const refreshCategories = async () => {
  try {
    await categoriesStore.fetchCategories()
    await tasksStore.fetchTasks()

    toast.add({
      severity: 'success',
      summary: 'Data Refreshed',
      detail: 'Categories and tasks have been updated',
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
  selectedCategories.value = []
  showBulkActions.value = false
}

// Category CRUD operations
const handleCreateCategory = async (categoryData: CategoryCreateRequest) => {
  formLoading.value = true
  try {
    const newCategory = await categoriesStore.createCategory(categoryData)

    showCreateDialog.value = false

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
    formLoading.value = false
  }
}

const editCategory = (category: Category) => {
  editingCategory.value = { ...category }
  showEditDialog.value = true
}

const handleUpdateCategory = async (categoryData: CategoryUpdateRequest) => {
  if (!editingCategory.value) return

  formLoading.value = true
  try {
    const updatedCategory = await categoriesStore.updateCategory(editingCategory.value.id, categoryData)

    showEditDialog.value = false
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
    formLoading.value = false
  }
}

const toggleArchiveCategory = (category: Category) => {
  const action = category.isArchived ? 'restore' : 'archive'
  const actionPast = category.isArchived ? 'restored' : 'archived'

  confirm.require({
    message: `Are you sure you want to ${action} "${category.title}"?`,
    header: `${action.charAt(0).toUpperCase() + action.slice(1)} Category`,
    icon: category.isArchived ? 'pi pi-replay' : 'pi pi-book',
    accept: async () => {
      try {
        await categoriesStore.archiveCategory(category.id, !category.isArchived)

        toast.add({
          severity: 'success',
          summary: `Category ${actionPast.charAt(0).toUpperCase() + actionPast.slice(1)}`,
          detail: `Category "${category.title}" has been ${actionPast}`,
          life: 3000
        })
      } catch (error: any) {
        toast.add({
          severity: 'error',
          summary: 'Operation Failed',
          detail: error.message || `Failed to ${action} category`,
          life: 5000
        })
      }
    }
  })
}

const deleteCategory = (category: Category) => {
  if (category.taskCount > 0) {
    toast.add({
      severity: 'warn',
      summary: 'Cannot Delete',
      detail: 'Category has tasks. Archive it instead or delete all tasks first.',
      life: 5000
    })
    return
  }

  confirm.require({
    message: `Are you sure you want to permanently delete "${category.title}"? This action cannot be undone.`,
    header: 'Delete Category',
    icon: 'pi pi-trash',
    rejectClass: 'p-button-secondary p-button-outlined',
    acceptClass: 'p-button-danger',
    accept: async () => {
      try {
        await categoriesStore.deleteCategory(category.id)

        toast.add({
          severity: 'success',
          summary: 'Category Deleted',
          detail: `Category "${category.title}" has been permanently deleted`,
          life: 3000
        })
      } catch (error: any) {
        toast.add({
          severity: 'error',
          summary: 'Delete Failed',
          detail: error.message || 'Failed to delete category',
          life: 5000
        })
      }
    }
  })
}

// Task operations
const addTaskToCategory = (category: Category) => {
  targetCategoryForTask.value = category
  showAddTaskDialog.value = true
}

const handleCreateTask = async (taskData: TaskCreateRequest) => {
  if (!targetCategoryForTask.value) return

  // Store category info before clearing
  const categoryTitle = targetCategoryForTask.value.title
  const categoryId = targetCategoryForTask.value.id

  // Set the category ID from the target category
  const taskDataWithCategory: TaskCreateRequest = {
    ...taskData,
    categoryId
  }

  taskFormLoading.value = true
  try {
    const newTask = await tasksStore.createTask(taskDataWithCategory)

    showAddTaskDialog.value = false
    targetCategoryForTask.value = null

    toast.add({
      severity: 'success',
      summary: 'Task Created',
      detail: `Task "${newTask.title}" has been added to "${categoryTitle}"`,
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
    taskFormLoading.value = false
  }
}

// Bulk operations
const bulkArchive = () => {
  const count = selectedCategories.value.length

  confirm.require({
    message: `Are you sure you want to archive ${count} selected categories?`,
    header: 'Bulk Archive',
    icon: 'pi pi-book',
    accept: async () => {
      try {
        const promises = selectedCategories.value.map(category =>
            categoriesStore.archiveCategory(category.id, true)
        )

        await Promise.all(promises)

        toast.add({
          severity: 'success',
          summary: 'Bulk Archive Complete',
          detail: `${count} categories have been archived`,
          life: 3000
        })

        clearSelection()
      } catch (error: any) {
        toast.add({
          severity: 'error',
          summary: 'Bulk Archive Failed',
          detail: 'Some categories could not be archived',
          life: 5000
        })
      }
    }
  })
}

const bulkUnarchive = () => {
  const count = selectedCategories.value.length

  confirm.require({
    message: `Are you sure you want to unarchive ${count} selected categories?`,
    header: 'Bulk Unarchive',
    icon: 'pi pi-book',
    accept: async () => {
      try {
        const promises = selectedCategories.value.map(category =>
            categoriesStore.archiveCategory(category.id, false)
        )

        await Promise.all(promises)

        toast.add({
          severity: 'success',
          summary: 'Bulk Unarchive Complete',
          detail: `${count} categories have been unarchived`,
          life: 3000
        })

        clearSelection()
      } catch (error: any) {
        toast.add({
          severity: 'error',
          summary: 'Bulk Unarchive Failed',
          detail: 'Some categories could not be unarchived',
          life: 5000
        })
      }
    }
  })
}

// Export operations
const exportCategories = () => {
  if (!categoriesStore.categories.length) {
    toast.add({
      severity: 'warn',
      summary: 'No Data',
      detail: 'No categories to export',
      life: 3000
    })
    return
  }

  const csvData = categoriesStore.categories.map(category => ({
    ID: category.id,
    Title: category.title,
    Description: category.description || '',
    'Is Default': category.isDefault ? 'Yes' : 'No',
    'Is Archived': category.isArchived ? 'Yes' : 'No',
    'Sort Order': category.sortOrder,
    'Task Count': category.taskCount,
    'Active Tasks': category.activeTaskCount,
    'Created At': formatDate(category.createdAt),
    'Updated At': formatDate(category.updatedAt)
  }))

  exportToCsv(csvData, 'categories')
}

const exportSelected = () => {
  if (!selectedCategories.value.length) {
    toast.add({
      severity: 'warn',
      summary: 'No Selection',
      detail: 'No categories selected for export',
      life: 3000
    })
    return
  }

  const csvData = selectedCategories.value.map(category => ({
    ID: category.id,
    Title: category.title,
    Description: category.description || '',
    'Is Default': category.isDefault ? 'Yes' : 'No',
    'Is Archived': category.isArchived ? 'Yes' : 'No',
    'Sort Order': category.sortOrder,
    'Task Count': category.taskCount,
    'Active Tasks': category.activeTaskCount,
    'Created At': formatDate(category.createdAt),
    'Updated At': formatDate(category.updatedAt)
  }))

  exportToCsv(csvData, 'selected-categories')
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
  if (!categoriesStore.categories.length) {
    categoriesStore.fetchCategories()
  }
  if (!tasksStore.tasks.length) {
    tasksStore.fetchTasks()
  }
})
</script>