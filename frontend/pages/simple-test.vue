<template>
  <div class="min-h-screen bg-gray-50 p-8">
    <div class="max-w-4xl mx-auto">
      <h1 class="text-4xl font-bold text-gray-900 mb-8 text-center">
        Simple API Test
      </h1>

      <!-- Connection Test -->
      <div class="bg-white rounded-lg shadow p-6 mb-6">
        <h2 class="text-xl font-semibold mb-4">Backend Connection</h2>
        <div class="space-y-4">
          <Button
              @click="testHealth"
              :loading="loading"
              label="Test Backend Health"
              icon="pi pi-wifi"
          />

          <div v-if="result" class="p-4 rounded-lg"
               :class="result.success ? 'bg-green-50 border border-green-200' : 'bg-red-50 border border-red-200'">
            <div class="flex items-center mb-2">
              <i :class="result.success ? 'pi pi-check-circle text-green-600' : 'pi pi-times-circle text-red-600'" class="mr-2"></i>
              <span class="font-medium">{{ result.message }}</span>
            </div>
            <pre v-if="result.data" class="text-sm text-gray-600 mt-2">{{ JSON.stringify(result.data, null, 2) }}</pre>
          </div>
        </div>
      </div>

      <!-- Manual URL Test -->
      <div class="bg-white rounded-lg shadow p-6">
        <h2 class="text-xl font-semibold mb-4">Manual API URLs</h2>
        <div class="space-y-2 text-sm">
          <div><strong>Backend Base:</strong> {{ config.public.apiBase }}</div>
          <div><strong>Health Check:</strong>
            <a :href="config.public.apiBase + '/health'" target="_blank" class="text-blue-600 hover:underline">
              {{ config.public.apiBase }}/health
            </a>
          </div>
          <div><strong>Categories:</strong>
            <a :href="config.public.apiBase + '/categories'" target="_blank" class="text-blue-600 hover:underline">
              {{ config.public.apiBase }}/categories
            </a>
          </div>
          <div><strong>Tasks:</strong>
            <a :href="config.public.apiBase + '/tasks/active'" target="_blank" class="text-blue-600 hover:underline">
              {{ config.public.apiBase }}/tasks/active
            </a>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
// Simple test with minimal typing
const config = useRuntimeConfig()
const loading = ref(false)
const result = ref<any>(null)

const testHealth = async () => {
  loading.value = true
  result.value = null

  try {
    // Simple fetch without complex typing
    const response = await $fetch(`${config.public.apiBase}/health`)
    result.value = {
      success: true,
      message: 'Backend connection successful!',
      data: response
    }
  } catch (error: any) {
    result.value = {
      success: false,
      message: `Failed to connect: ${error.message || 'Unknown error'}`,
      data: null
    }
  } finally {
    loading.value = false
  }
}

useHead({
  title: 'Simple API Test - Time Tracker'
})
</script>