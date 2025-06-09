<template>
  <div class="bg-white rounded-lg shadow p-4 border border-gray-200">
    <h3 class="text-lg font-semibold text-gray-800 mb-3 flex items-center">
      <i class="pi pi-bell text-blue-500 mr-2"></i>
      Quick Toast Test
    </h3>

    <div class="flex flex-wrap gap-2">
      <Button
          @click="showSuccess"
          size="small"
          severity="success"
          icon="pi pi-check"
          label="Success"
      />

      <Button
          @click="showError"
          size="small"
          severity="danger"
          icon="pi pi-times"
          label="Error"
      />

      <Button
          @click="showInfo"
          size="small"
          severity="info"
          icon="pi pi-info"
          label="Info"
      />

      <Button
          @click="showWarn"
          size="small"
          severity="warning"
          icon="pi pi-exclamation-triangle"
          label="Warning"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
const toast = useToast()

const showSuccess = () => {
  toast.add({
    severity: 'success',
    summary: 'Success!',
    detail: 'Toast notifications are working correctly',
    life: 3000
  })
}

const showError = () => {
  toast.add({
    severity: 'error',
    summary: 'Error Test',
    detail: 'This is a test error message',
    life: 4000
  })
}

const showInfo = () => {
  toast.add({
    severity: 'info',
    summary: 'Information',
    detail: 'This is an informational message',
    life: 3000
  })
}

const showWarn = () => {
  toast.add({
    severity: 'warn',
    summary: 'Warning',
    detail: 'This is a warning message',
    life: 3000
  })
}
</script>