<template>
  <div class="min-h-screen bg-gray-50 flex flex-col">
    <!-- Top Navigation Bar -->
    <div class="bg-white border-b border-gray-200 sticky top-0 z-10">
      <div class="px-6 py-4">
        <!-- Main Navigation Menu -->
        <Menubar :model="menuItems" class="border-0 bg-transparent p-0">
          <template #start>
            <h1 class="text-2xl font-bold text-gray-900 flex items-center">
              <i class="pi pi-clock text-blue-600 mr-2"></i>
              Time Tracker
            </h1>
          </template>
          <template #end>
            <div class="flex items-center space-x-3">
              <div class="flex items-center space-x-3">
                <span class="text-sm text-gray-600">Local Admin</span>
                <Button
                    icon="pi pi-cog"
                    severity="secondary"
                    text
                    @click="showSettings = true"
                    v-tooltip.bottom="'Settings'"
                />
                <Button
                    icon="pi pi-question-circle"
                    severity="secondary"
                    text
                    @click="showHelp = true"
                    v-tooltip.bottom="'Help'"
                />
              </div>
            </div>
          </template>
        </Menubar>
      </div>
    </div>

    <!-- Page Content -->
    <slot />

    <!-- Global Dialogs -->

    <!-- Settings Dialog -->
    <Dialog
        v-model:visible="showSettings"
        modal
        header="Settings"
        :style="{ width: '600px' }"
    >
      <div class="space-y-6">
        <div>
          <h3 class="text-lg font-semibold text-gray-800 mb-3">Application Settings</h3>
          <div class="space-y-4">
            <div class="flex items-center justify-between">
              <span class="text-sm text-gray-700">Time Zone</span>
              <span class="text-sm text-gray-500">UTC (Local Admin)</span>
            </div>
            <div class="flex items-center justify-between">
              <span class="text-sm text-gray-700">Working Hours</span>
              <span class="text-sm text-gray-500">7:00 AM - 5:00 PM</span>
            </div>
            <div class="flex items-center justify-between">
              <span class="text-sm text-gray-700">Time Interval</span>
              <span class="text-sm text-gray-500">15 minutes</span>
            </div>
          </div>
        </div>

        <div class="text-center py-4 text-gray-500">
          <i class="pi pi-cog text-3xl mb-2"></i>
          <div>Advanced settings coming soon!</div>
        </div>
      </div>

      <template #footer>
        <Button label="Close" @click="showSettings = false" />
      </template>
    </Dialog>

    <!-- Help Dialog -->
    <Dialog
        v-model:visible="showHelp"
        header="How to Use Time Tracker"
        modal
    >
      <div>
        <div class="p-6">
          <div class="space-y-4 text-sm text-gray-600">
            <div>
              <h4 class="font-semibold text-gray-800 mb-2">Getting Started</h4>
              <ol class="list-decimal list-inside space-y-1">
                <li>Select a task from the sidebar on the left</li>
                <li>Click and drag across time slots to create time entries</li>
                <li>Use the date picker to navigate between days</li>
                <li>Right-click on existing entries to delete them</li>
              </ol>
            </div>

            <div>
              <h4 class="font-semibold text-gray-800 mb-2">Navigation</h4>
              <ul class="list-disc list-inside space-y-1">
                <li>Use the ← → arrows to navigate days quickly</li>
                <li>Click "Today" to jump to the current date</li>
                <li>Access Reports and Export from the main menu</li>
              </ul>
            </div>

            <div>
              <h4 class="font-semibold text-gray-800 mb-2">Tips</h4>
              <ul class="list-disc list-inside space-y-1">
                <li>Each slot represents 15 minutes of time</li>
                <li>Different tasks are shown in different colors</li>
                <li>View your daily breakdown at the bottom</li>
                <li>Export your data using the Export button</li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </Dialog>
  </div>
</template>

<script setup lang="ts">
// Composables
const toast = useToast()

// Reactive state for layout
const showSettings = ref(false)
const showHelp = ref(false)

// Menu items for navigation
const menuItems = ref([
  {
    label: 'Timesheet',
    icon: 'pi pi-calendar',
    command: () => {
      navigateTo('/')
    }
  },
  {
    label: 'Reports',
    icon: 'pi pi-chart-bar',
    items: [
      {
        label: 'Daily Reports',
        icon: 'pi pi-calendar',
        command: () => navigateTo('/reports/daily')
      },
      {
        label: 'Weekly Summary',
        icon: 'pi pi-chart-line',
        command: () => navigateTo('/reports/weekly')
      },
      {
        label: 'Monthly Overview',
        icon: 'pi pi-chart-pie',
        command: () => navigateTo('/reports/monthly')
      },
      {
        label: 'Statistics',
        icon: 'pi pi-calculator',
        command: () => navigateTo('/reports/statistics')
      }
    ]
  },
  {
    label: 'Export',
    icon: 'pi pi-download',
    items: [
      {
        label: 'Export Today',
        icon: 'pi pi-file',
        command: () => showPlaceholder('Export Today')
      },
      {
        label: 'Export Week',
        icon: 'pi pi-calendar',
        command: () => showPlaceholder('Export Week')
      },
      {
        label: 'Export Month',
        icon: 'pi pi-calendar',
        command: () => showPlaceholder('Export Month')
      },
      {
        label: 'Custom Range',
        icon: 'pi pi-filter',
        command: () => showPlaceholder('Custom Export Range')
      }
    ]
  },
  {
    label: 'Manage',
    icon: 'pi pi-cog',
    items: [
      {
        label: 'Categories',
        icon: 'pi pi-folder',
        command: () => navigateTo('/manage/categories')
      },
      {
        label: 'Tasks',
        icon: 'pi pi-bookmark',
        command: () => navigateTo('/manage/tasks')
      },
      {
        label: 'Archive',
        icon: 'pi pi-archive',
        command: () => navigateTo('/manage/archive')
      }
    ]
  }
])

// Methods
const showPlaceholder = (feature: string) => {
  toast.add({
    severity: 'info',
    summary: 'Coming Soon',
    detail: `${feature} functionality will be available in a future update`,
    life: 3000
  })
}
</script>

<style scoped>
/* Component-specific styles */
.menubar {
  border: none !important;
  background: transparent !important;
  padding: 0 !important;
}

/* Ensure sticky header works properly */
.sticky {
  position: sticky;
  top: 0;
  z-index: 10;
}
</style>