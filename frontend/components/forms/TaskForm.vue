<template>
  <form @submit.prevent="handleSubmit" class="space-y-4">
    <!-- Task Title -->
    <div>
      <label for="title" class="block text-sm font-medium text-app-primary mb-1">
        Task Title <span class="text-app-red-500">*</span>
      </label>
      <InputText
          id="title"
          v-model="formData.title"
          placeholder="Enter task title"
          :class="{ 'p-invalid': errors.title }"
          class="w-full"
          autofocus
      />
      <small v-if="errors.title" class="p-error">{{ errors.title }}</small>
    </div>

    <!-- Category Selection -->
    <div>
      <label for="category" class="block text-sm font-medium text-app-primary mb-1">
        Category <span class="text-app-red-500">*</span>
      </label>
      <Dropdown
          id="category"
          v-model="formData.categoryId"
          :options="availableCategories"
          optionLabel="title"
          optionValue="id"
          placeholder="Select a category"
          :class="{ 'p-invalid': errors.categoryId }"
          class="w-full"
      />
      <small v-if="errors.categoryId" class="p-error">{{ errors.categoryId }}</small>
    </div>

    <!-- Task Description -->
    <div>
      <label for="description" class="block text-sm font-medium text-app-primary mb-1">
        Description
      </label>
      <Textarea
          id="description"
          v-model="formData.description"
          placeholder="Optional task description"
          rows="3"
          class="w-full"
      />
    </div>

    <!-- Color Selection -->
    <div>
      <label class="block text-sm font-medium text-app-primary mb-2">
        Task Color <span class="text-app-red-500">*</span>
      </label>
      <div class="space-y-3">
        <!-- Color Picker -->
        <div class="flex items-center space-x-3">
          <input
              type="color"
              v-model="formData.color"
              class="w-12 h-8 border border-app-primary rounded cursor-pointer"
          />
          <InputText
              v-model="formData.color"
              placeholder="#3498db"
              class="flex-1"
              :class="{ 'p-invalid': errors.color }"
          />
        </div>

        <!-- Preset Colors -->
        <div>
          <div class="text-xs text-app-secondary mb-2">Quick Colors:</div>
          <div class="flex flex-wrap gap-2">
            <button
                v-for="color in presetColors"
                :key="color"
                type="button"
                @click="formData.color = color"
                class="w-8 h-8 rounded border-2 border-app-primary hover:scale-110 transition-transform"
                :class="{ 'ring-2 ring-blue-500': formData.color === color }"
                :style="{ backgroundColor: color }"
                :title="color"
            ></button>
          </div>
        </div>
      </div>
      <small v-if="errors.color" class="p-error">{{ errors.color }}</small>
    </div>

    <!-- Icon Selection -->
    <div>
      <label class="block text-sm font-medium text-app-primary mb-2">
        Task Icon (Optional)
      </label>
      <div class="space-y-3">
        <!-- Selected Icon Display -->
        <div v-if="formData.icon" class="flex items-center space-x-2">
          <i :class="`pi pi-${formData.icon} text-xl`" :style="{ color: formData.color }"></i>
          <span class="text-sm text-app-secondary">{{ formData.icon }}</span>
          <Button
              type="button"
              icon="pi pi-times"
              size="small"
              text
              @click="formData.icon = ''"
              class="ml-auto"
          />
        </div>

        <!-- Icon Grid -->
        <div class="grid grid-cols-8 gap-2 max-h-32 overflow-y-auto p-2 border border-app-primary rounded">
          <button
              v-for="icon in availableIcons"
              :key="icon"
              type="button"
              @click="formData.icon = icon"
              class="w-8 h-8 flex items-center justify-center rounded hover:bg-app-tertiary transition-colors"
              :class="{ 'bg-app-blue-100 text-app-blue-600': formData.icon === icon }"
              :title="icon"
          >
            <i :class="`pi pi-${icon}`"></i>
          </button>
        </div>
      </div>
    </div>

    <!-- Sort Order -->
    <div>
      <label for="sortOrder" class="block text-sm font-medium text-app-primary mb-1">
        Sort Order
      </label>
      <InputNumber
          id="sortOrder"
          v-model="formData.sortOrder"
          :min="0"
          :max="999"
          class="w-full"
      />
      <small class="text-app-secondary">Lower numbers appear first</small>
    </div>

    <!-- Submit Buttons -->
    <div class="flex justify-end space-x-3 pt-4 border-t border-app-primary">
      <Button
          type="button"
          label="Cancel"
          severity="secondary"
          @click="$emit('cancel')"
      />
      <Button
          type="submit"
          :label="isEditing ? 'Update Task' : 'Create Task'"
          :loading="loading"
          :disabled="!isFormValid"
      />
    </div>
  </form>
</template>

<script setup lang="ts">
import type { Task, TaskCreateRequest, TaskUpdateRequest } from '~/types/api'

// Props
interface Props {
  task?: Task | null
  loading?: boolean
  passedCategoryId?: number
}

const props = withDefaults(defineProps<Props>(), {
  task: null,
  loading: false,
  passedCategoryId: undefined
})

// Emits
interface Emits {
  submitCreate: [data: TaskCreateRequest]
  submitUpdate: [data: TaskUpdateRequest]
  cancel: []
}

const emit = defineEmits<Emits>()

// Stores
const categoriesStore = useCategoriesStore()

// Form data
const formData = reactive({
  title: '',
  categoryId: null as number | null,
  description: '',
  color: '#3498db',
  icon: '',
  sortOrder: 0
})

// Validation errors
const errors = reactive({
  title: '',
  categoryId: '',
  color: ''
})

// Available categories
const availableCategories = computed(() => categoriesStore.activeCategories)

// Check if editing existing task
const isEditing = computed(() => !!props.task)

// Preset colors for quick selection
const presetColors = [
  '#3498db', '#e74c3c', '#2ecc71', '#f39c12', '#9b59b6',
  '#1abc9c', '#34495e', '#e67e22', '#8e44ad', '#27ae60',
  '#2980b9', '#c0392b', '#16a085', '#d35400', '#7f8c8d'
]

// Available icons
const availableIcons = [
  'calendar', 'clock', 'cog', 'book', 'briefcase', 'code', 'camera',
  'desktop', 'envelope', 'file', 'headphones', 'heart', 'home', 'lightbulb',
  'microphone', 'mobile', 'palette', 'phone', 'search',
  'shopping-cart', 'star', 'tablet', 'tag', 'users', 'video',
  'wifi', 'wrench', 'wallet', 'check', 'dollar', 'flag', 'globe'
]

// Form validation
const isFormValid = computed(() => {
  return formData.title.trim().length > 0 &&
      formData.categoryId !== null &&
      isValidHexColor(formData.color)
})

const getRandomHexColor = () => {
  return '#' + Math.floor(Math.random() * 16777215).toString(16);
}

// Validate hex color
const isValidHexColor = (color: string): boolean => {
  return /^#[0-9A-F]{6}$/i.test(color)
}

// Validate form
const validateForm = (): boolean => {
  // Reset errors
  errors.title = ''
  errors.categoryId = ''
  errors.color = ''

  let isValid = true

  // Validate title
  if (!formData.title.trim()) {
    errors.title = 'Task title is required'
    isValid = false
  } else if (formData.title.trim().length > 255) {
    errors.title = 'Task title must be less than 255 characters'
    isValid = false
  }

  // Validate category
  if (!formData.categoryId) {
    errors.categoryId = 'Please select a category'
    isValid = false
  }

  // Validate color
  if (!isValidHexColor(formData.color)) {
    errors.color = 'Please enter a valid hex color (e.g., #3498db)'
    isValid = false
  }

  return isValid
}

// Handle form submission
const handleSubmit = () => {
  if (!validateForm()) return

  if (isEditing.value) {
    // Editing existing task - emit update data
    const updateData: TaskUpdateRequest = {
      title: formData.title.trim(),
      description: formData.description.trim() || undefined,
      color: formData.color.toUpperCase(),
      icon: formData.icon || undefined,
      sortOrder: formData.sortOrder
    }

    emit('submitUpdate', updateData)
  } else {
    // Creating new task - emit create data
    const createData: TaskCreateRequest = {
      title: formData.title.trim(),
      categoryId: formData.categoryId!,
      description: formData.description.trim() || undefined,
      color: formData.color.toUpperCase(),
      icon: formData.icon || undefined,
      sortOrder: formData.sortOrder
    }

    emit('submitCreate', createData)
  }
}

// Initialize form data
const initializeForm = () => {
  if (props.task) {
    // Editing existing task
    formData.title = props.task.title
    formData.categoryId = props.task.categoryId
    formData.description = props.task.description || ''
    formData.color = props.task.color
    formData.icon = props.task.icon || ''
    formData.sortOrder = props.task.sortOrder
  } else {
    // Creating new task
    formData.title = ''
    if (props.passedCategoryId !== undefined) {
      formData.categoryId = props.passedCategoryId;
    } else formData.categoryId = null;
    formData.description = ''
    formData.color = getRandomHexColor()
    formData.icon = ''
    formData.sortOrder = 0
  }
}

// Watch for task changes
watch(() => props.task, initializeForm, { immediate: true })

// Load categories if not already loaded
onMounted(async () => {
  if (categoriesStore.categories.length === 0) {
    await categoriesStore.fetchCategories()
  }
})
</script>

<style scoped>
.p-error {
  color: #e24c4c;
  font-size: 0.75rem;
}

.p-invalid {
  border-color: #e24c4c;
}
</style>