<template>
  <div class="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 p-8">
    <div class="max-w-4xl mx-auto">
      <!-- Header -->
      <div class="text-center mb-8">
        <h1 class="text-4xl font-bold text-gray-900 mb-4">
          Time Tracker
        </h1>
        <p class="text-xl text-gray-600">
          Tailwind CSS + PrimeVue Integration Test
        </p>
      </div>

      <!-- Test Cards -->
      <div class="grid md:grid-cols-2 gap-6 mb-8">
        <!-- Tailwind Test Card -->
        <div class="bg-white rounded-lg shadow-lg p-6">
          <h2 class="text-2xl font-semibold text-gray-800 mb-4 flex items-center">
            <i class="pi pi-palette text-blue-500 mr-2"></i>
            Tailwind CSS Test
          </h2>
          <div class="space-y-4">
            <div class="flex items-center space-x-2">
              <div class="w-4 h-4 bg-red-500 rounded"></div>
              <span class="text-sm">Red color utility</span>
            </div>
            <div class="flex items-center space-x-2">
              <div class="w-4 h-4 bg-green-500 rounded"></div>
              <span class="text-sm">Green color utility</span>
            </div>
            <div class="flex items-center space-x-2">
              <div class="w-4 h-4 bg-blue-500 rounded"></div>
              <span class="text-sm">Blue color utility</span>
            </div>
            <button class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors duration-200">
              Tailwind Button
            </button>
          </div>
        </div>

        <!-- PrimeVue Test Card -->
        <div class="bg-white rounded-lg shadow-lg p-6">
          <h2 class="text-2xl font-semibold text-gray-800 mb-4 flex items-center">
            <i class="pi pi-sparkles text-purple-500 mr-2"></i>
            PrimeVue Test
          </h2>
          <div class="space-y-4">
            <Button
                label="PrimeVue Button"
                icon="pi pi-check"
                class="w-full"
            />
            <InputText
                v-model="testInput"
                placeholder="PrimeVue Input"
                class="w-full"
            />
            <Dropdown
                v-model="selectedOption"
                :options="dropdownOptions"
                optionLabel="name"
                placeholder="Select an option"
                class="w-full"
            />
            <Message
                severity="success"
                :closable="false"
            >
              PrimeVue components are working!
            </Message>
          </div>
        </div>
      </div>

      <!-- Timesheet Preview -->
      <div class="bg-white rounded-lg shadow-lg p-6">
        <h2 class="text-2xl font-semibold text-gray-800 mb-4 flex items-center">
          <i class="pi pi-calendar text-green-500 mr-2"></i>
          Timesheet Grid Preview
        </h2>
        <p class="text-gray-600 mb-4">
          Preview of the timesheet grid styling with Tailwind classes:
        </p>

        <!-- Mini Timesheet Grid -->
        <div class="timesheet-grid max-w-2xl">
          <!-- Header -->
          <div class="timesheet-hour-label text-xs">Time</div>
          <div class="bg-gray-100 px-2 py-2 text-xs font-medium text-center border-r border-gray-300">00</div>
          <div class="bg-gray-100 px-2 py-2 text-xs font-medium text-center border-r border-gray-300">15</div>
          <div class="bg-gray-100 px-2 py-2 text-xs font-medium text-center border-r border-gray-300">30</div>
          <div class="bg-gray-100 px-2 py-2 text-xs font-medium text-center">45</div>

          <!-- Sample Hours -->
          <div class="timesheet-hour-label">09:00</div>
          <div class="timesheet-slot filled" style="background-color: #3498db;">Work</div>
          <div class="timesheet-slot filled" style="background-color: #3498db;">Work</div>
          <div class="timesheet-slot filled" style="background-color: #3498db;">Work</div>
          <div class="timesheet-slot filled" style="background-color: #3498db;">Work</div>

          <div class="timesheet-hour-label">10:00</div>
          <div class="timesheet-slot filled" style="background-color: #e74c3c;">Break</div>
          <div class="timesheet-slot"></div>
          <div class="timesheet-slot filled" style="background-color: #9b59b6;">Meeting</div>
          <div class="timesheet-slot filled" style="background-color: #9b59b6;">Meeting</div>

          <div class="timesheet-hour-label">11:00</div>
          <div class="timesheet-slot"></div>
          <div class="timesheet-slot"></div>
          <div class="timesheet-slot"></div>
          <div class="timesheet-slot"></div>
        </div>
      </div>

      <!-- Status Information -->
      <div class="mt-8 bg-green-50 border border-green-200 rounded-lg p-4">
        <div class="flex items-center">
          <i class="pi pi-check-circle text-green-600 mr-2"></i>
          <span class="text-green-800 font-medium">Integration Status:</span>
        </div>
        <ul class="mt-2 text-green-700 text-sm space-y-1">
          <li>✓ Tailwind CSS utilities working</li>
          <li>✓ PrimeVue components functional</li>
          <li>✓ Custom timesheet classes applied</li>
          <li>✓ Icons and styling integrated</li>
        </ul>
      </div>

      <!-- Navigation to next step -->
      <div class="mt-8 text-center space-y-4">
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-6 gap-4 max-w-6xl mx-auto">
          <NuxtLink
              to="/test-api"
              class="inline-flex items-center justify-center px-6 py-3 bg-blue-600 text-white font-medium rounded-lg hover:bg-blue-700 transition-colors"
          >
            <i class="pi pi-database mr-2"></i>
            API Integration
          </NuxtLink>
          <NuxtLink
              to="/test-toast"
              class="inline-flex items-center justify-center px-6 py-3 bg-purple-600 text-white font-medium rounded-lg hover:bg-purple-700 transition-colors"
          >
            <i class="pi pi-bell mr-2"></i>
            Toast Functionality
          </NuxtLink>
          <NuxtLink
              to="/test-sidebar"
              class="inline-flex items-center justify-center px-6 py-3 bg-green-600 text-white font-medium rounded-lg hover:bg-green-700 transition-colors"
          >
            <i class="pi pi-bookmark mr-2"></i>
            Task Sidebar
          </NuxtLink>
          <NuxtLink
              to="/test-timesheet"
              class="inline-flex items-center justify-center px-6 py-3 bg-orange-600 text-white font-medium rounded-lg hover:bg-orange-700 transition-colors"
          >
            <i class="pi pi-calendar mr-2"></i>
            Full Timesheet
          </NuxtLink>
          <NuxtLink
              to="/test-management"
              class="inline-flex items-center justify-center px-6 py-3 bg-red-600 text-white font-medium rounded-lg hover:bg-red-700 transition-colors"
          >
            <i class="pi pi-cog mr-2"></i>
            Task Management
          </NuxtLink>
          <NuxtLink
              to="/simple-test"
              class="inline-flex items-center justify-center px-6 py-3 bg-gray-600 text-white font-medium rounded-lg hover:bg-gray-700 transition-colors"
          >
            <i class="pi pi-wrench mr-2"></i>
            Simple API Test
          </NuxtLink>
        </div>
        <p class="text-sm text-gray-600">Choose a test page to verify functionality</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

// Test data for PrimeVue components
const testInput = ref('')
const selectedOption = ref(null)
const dropdownOptions = ref([
  { name: 'Work', value: 'work' },
  { name: 'Break', value: 'break' },
  { name: 'Meeting', value: 'meeting' }
])

// Set page title
useHead({
  title: 'Time Tracker - Integration Test'
})
</script>