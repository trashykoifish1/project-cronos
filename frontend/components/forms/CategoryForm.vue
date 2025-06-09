<template>
  <form @submit.prevent="handleSubmit" class="space-y-4">
    <!-- Category Title -->
    <div>
      <label for="title" class="block text-sm font-medium text-gray-700 mb-1">
        Category Title <span class="text-red-500">*</span>
      </label>
      <InputText
          id="title"
          v-model="formData.title"
          placeholder="Enter category title"
          :class="{ 'p-invalid': errors.title }"
          class="w-full"
          autofocus
      />
      <small v-if="errors.title" class="p-error">{{ errors.title }}</small>
    </div>

    <!-- Category Description -->
    <div>
      <label for="description" class="block text-sm font-medium text-gray-700 mb-1">
        Description
      </label>
      <Textarea
          id="description"
          v-model="formData.description"
          placeholder="Optional category description"
          rows="3"
          class="w-full"
      />
      <small class="text-gray-500">Describe what types of tasks belong in this category</small>
    </div>

    <!-- Category Options -->
    <div class="space-y-3">
      <div class="flex items-center">
        <Checkbox
            id="isDefault"
            v-model="formData.isDefault"
            :binary="true"
        />
        <label for="isDefault" class="ml-2 text-sm text-gray-700">
          Set as default category
        </label>
      </div>
      <small v-if="formData.isDefault" class="text-blue-600 text-xs">
        New tasks will be assigned to this category by default
      </small>
    </div>

    <!-- Sort Order -->
    <div>
      <label for="sortOrder" class="block text-sm font-medium text-gray-700 mb-1">
        Sort Order
      </label>
      <InputNumber
          id="sortOrder"
          v-model="formData.sortOrder"
          :min="0"
          :max="999"
          class="w-full"
      />
      <small class="text-gray-500">Lower numbers appear first in the list</small>
    </div>

    <!-- Archive Option (only for editing) -->
    <div v-if="isEditing" class="space-y-3">
      <div class="flex items-center">
        <Checkbox
            id="isArchived"
            v-model="formData.isArchived"
            :binary="true"
        />
        <label for="isArchived" class="ml-2 text-sm text-gray-700">
          Archive this category
        </label>
      </div>
      <small v-if="formData.isArchived" class="text-orange-600 text-xs">
        Archived categories are hidden but preserve existing data
      </small>
    </div>

    <!-- Submit Buttons -->
    <div class="flex justify-end space-x-3 pt-4 border-t border-gray-200">
      <Button
          type="button"
          label="Cancel"
          severity="secondary"
          @click="$emit('cancel')"
      />
      <Button
          type="submit"
          :label="isEditing ? 'Update Category' : 'Create Category'"
          :loading="loading"
          :disabled="!isFormValid"
      />
    </div>
  </form>
</template>

<script setup lang="ts">
import type { Category, CategoryCreateRequest, CategoryUpdateRequest } from '~/types/api'

// Props
interface Props {
  category?: Category | null
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  category: null,
  loading: false
})

// Emits
interface Emits {
  submitCreate: [data: CategoryCreateRequest]
  submitUpdate: [data: CategoryUpdateRequest]
  cancel: []
}

const emit = defineEmits<Emits>()

// Form data
const formData = reactive({
  title: '',
  description: '',
  isDefault: false,
  isArchived: false,
  sortOrder: 0
})

// Validation errors
const errors = reactive({
  title: ''
})

// Check if editing existing category
const isEditing = computed(() => !!props.category)

// Form validation
const isFormValid = computed(() => {
  return formData.title.trim().length > 0
})

// Validate form
const validateForm = (): boolean => {
  // Reset errors
  errors.title = ''

  let isValid = true

  // Validate title
  if (!formData.title.trim()) {
    errors.title = 'Category title is required'
    isValid = false
  } else if (formData.title.trim().length > 255) {
    errors.title = 'Category title must be less than 255 characters'
    isValid = false
  }

  return isValid
}

// Handle form submission
const handleSubmit = () => {
  if (!validateForm()) return

  if (isEditing.value) {
    // Editing existing category - emit update data
    const updateData: CategoryUpdateRequest = {
      title: formData.title.trim(),
      description: formData.description.trim() || undefined,
      isDefault: formData.isDefault,
      isArchived: formData.isArchived,
      sortOrder: formData.sortOrder
    }

    emit('submitUpdate', updateData)
  } else {
    // Creating new category - emit create data
    const createData: CategoryCreateRequest = {
      title: formData.title.trim(),
      description: formData.description.trim() || undefined,
      isDefault: formData.isDefault,
      sortOrder: formData.sortOrder
    }

    emit('submitCreate', createData)
  }
}

// Initialize form data
const initializeForm = () => {
  if (props.category) {
    // Editing existing category
    formData.title = props.category.title
    formData.description = props.category.description || ''
    formData.isDefault = props.category.isDefault
    formData.isArchived = props.category.isArchived
    formData.sortOrder = props.category.sortOrder
  } else {
    // Creating new category
    formData.title = ''
    formData.description = ''
    formData.isDefault = false
    formData.isArchived = false
    formData.sortOrder = 0
  }
}

// Watch for category changes
watch(() => props.category, initializeForm, { immediate: true })
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