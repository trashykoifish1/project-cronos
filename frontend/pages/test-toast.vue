<template>
  <div class="min-h-screen bg-gray-50 p-8">
    <div class="max-w-4xl mx-auto">
      <!-- Header -->
      <div class="text-center mb-8">
        <h1 class="text-4xl font-bold text-gray-900 mb-4">
          Toast Functionality Test
        </h1>
        <p class="text-xl text-gray-600">
          Testing PrimeVue Toast notifications
        </p>
      </div>

      <!-- Toast Test Buttons -->
      <div class="bg-white rounded-lg shadow-lg p-6 mb-8">
        <h2 class="text-2xl font-semibold text-gray-800 mb-4 flex items-center">
          <i class="pi pi-bell text-blue-500 mr-2"></i>
          Toast Types
        </h2>

        <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
          <Button
              @click="showSuccessToast"
              label="Success Toast"
              icon="pi pi-check"
              severity="success"
              class="w-full"
          />

          <Button
              @click="showErrorToast"
              label="Error Toast"
              icon="pi pi-times"
              severity="danger"
              class="w-full"
          />

          <Button
              @click="showWarnToast"
              label="Warning Toast"
              icon="pi pi-exclamation-triangle"
              severity="warning"
              class="w-full"
          />

          <Button
              @click="showInfoToast"
              label="Info Toast"
              icon="pi pi-info-circle"
              severity="info"
              class="w-full"
          />
        </div>
      </div>

      <!-- Advanced Toast Tests -->
      <div class="bg-white rounded-lg shadow-lg p-6 mb-8">
        <h2 class="text-2xl font-semibold text-gray-800 mb-4 flex items-center">
          <i class="pi pi-cog text-purple-500 mr-2"></i>
          Advanced Toast Features
        </h2>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
          <Button
              @click="showStickyToast"
              label="Sticky Toast"
              icon="pi pi-clock"
              severity="secondary"
              class="w-full"
          />

          <Button
              @click="showDetailedToast"
              label="Detailed Toast"
              icon="pi pi-list"
              severity="secondary"
              class="w-full"
          />

          <Button
              @click="clearAllToasts"
              label="Clear All"
              icon="pi pi-trash"
              severity="secondary"
              class="w-full"
          />
        </div>
      </div>

      <!-- Toast with Confirm Dialog -->
      <div class="bg-white rounded-lg shadow-lg p-6 mb-8">
        <h2 class="text-2xl font-semibold text-gray-800 mb-4 flex items-center">
          <i class="pi pi-question-circle text-orange-500 mr-2"></i>
          Confirm Dialog Test
        </h2>

        <div class="space-y-4">
          <Button
              @click="showConfirmDialog"
              label="Show Confirm Dialog"
              icon="pi pi-question"
              severity="help"
              class="w-full md:w-auto"
          />

          <div v-if="confirmResult" class="p-4 bg-gray-50 rounded-lg">
            <strong>Last Action:</strong> {{ confirmResult }}
          </div>
        </div>
      </div>

      <!-- Real-world Example -->
      <div class="bg-white rounded-lg shadow-lg p-6">
        <h2 class="text-2xl font-semibold text-gray-800 mb-4 flex items-center">
          <i class="pi pi-database text-green-500 mr-2"></i>
          Real-world Example: Simulated API Call
        </h2>

        <div class="space-y-4">
          <div class="flex items-center space-x-4">
            <Button
                @click="simulateSuccessfulOperation"
                :loading="loading"
                label="Simulate Successful Operation"
                icon="pi pi-check"
                severity="success"
            />

            <Button
                @click="simulateFailedOperation"
                :loading="loading"
                label="Simulate Failed Operation"
                icon="pi pi-times"
                severity="danger"
            />
          </div>

          <div class="text-sm text-gray-600">
            These buttons simulate real API operations with loading states and appropriate toast notifications.
          </div>
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
</template>

<script setup lang="ts">
const toast = useToast()
const confirm = useConfirm()
const loading = ref(false)
const confirmResult = ref('')

// Basic toast examples
const showSuccessToast = () => {
  toast.add({
    severity: 'success',
    summary: 'Success Message',
    detail: 'Operation completed successfully!',
    life: 3000
  })
}

const showErrorToast = () => {
  toast.add({
    severity: 'error',
    summary: 'Error Message',
    detail: 'Something went wrong. Please try again.',
    life: 5000
  })
}

const showWarnToast = () => {
  toast.add({
    severity: 'warn',
    summary: 'Warning Message',
    detail: 'Please check your input before proceeding.',
    life: 4000
  })
}

const showInfoToast = () => {
  toast.add({
    severity: 'info',
    summary: 'Information',
    detail: 'Here is some useful information for you.',
    life: 3000
  })
}

// Advanced toast examples
const showStickyToast = () => {
  toast.add({
    severity: 'info',
    summary: 'Sticky Toast',
    detail: 'This toast will stay until manually closed. Click the X to dismiss.',
  })
}

const showDetailedToast = () => {
  toast.add({
    severity: 'success',
    summary: 'Data Processing Complete',
    detail: 'Successfully processed 150 records, updated 23 entries, and created 5 new items.',
    life: 6000
  })
}

const clearAllToasts = () => {
  toast.removeAllGroups()
  toast.add({
    severity: 'info',
    summary: 'Cleared',
    detail: 'All toast notifications have been cleared.',
    life: 2000
  })
}

// Confirm dialog example
const showConfirmDialog = () => {
  confirm.require({
    message: 'Are you sure you want to proceed with this action?',
    header: 'Confirmation',
    icon: 'pi pi-exclamation-triangle',
    rejectProps: {
      label: 'Cancel',
      severity: 'secondary',
      outlined: true
    },
    acceptProps: {
      label: 'Yes, Proceed',
      severity: 'danger'
    },
    accept: () => {
      confirmResult.value = 'User confirmed the action'
      toast.add({
        severity: 'success',
        summary: 'Confirmed',
        detail: 'Action has been confirmed and executed.',
        life: 3000
      })
    },
    reject: () => {
      confirmResult.value = 'User cancelled the action'
      toast.add({
        severity: 'info',
        summary: 'Cancelled',
        detail: 'Action has been cancelled.',
        life: 2000
      })
    }
  })
}

// Real-world operation examples
const simulateSuccessfulOperation = async () => {
  loading.value = true

  try {
    // Simulate API delay
    await new Promise(resolve => setTimeout(resolve, 2000))

    toast.add({
      severity: 'success',
      summary: 'Operation Successful',
      detail: 'Data has been saved successfully!',
      life: 3000
    })
  } catch (error) {
    toast.add({
      severity: 'error',
      summary: 'Operation Failed',
      detail: 'Failed to save data. Please try again.',
      life: 5000
    })
  } finally {
    loading.value = false
  }
}

const simulateFailedOperation = async () => {
  loading.value = true

  try {
    // Simulate API delay
    await new Promise(resolve => setTimeout(resolve, 1500))

    // Simulate an error
    throw new Error('Network connection timeout')
  } catch (error: any) {
    toast.add({
      severity: 'error',
      summary: 'Operation Failed',
      detail: `Error: ${error.message}`,
      life: 5000
    })
  } finally {
    loading.value = false
  }
}

useHead({
  title: 'Toast Test - Time Tracker'
})
</script>