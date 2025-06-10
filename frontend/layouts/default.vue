<template>
  <div class="min-h-screen bg-gray-50 flex flex-col">
    <!-- Top Navigation Bar -->
    <div class="bg-white border-b border-gray-200 sticky top-0 z-10">
      <div class="px-4 sm:px-6 py-3 sm:py-4">
        <!-- Mobile Header -->
        <div class="flex items-center justify-between md:hidden">
          <!-- Mobile Menu Button -->
          <Button
              icon="pi pi-bars"
              severity="secondary"
              text
              @click="showMobileMenu = true"
              v-tooltip.bottom="'Menu'"
              class="mr-3"
          />

          <!-- Mobile Logo -->
          <h1 class="text-xl font-bold text-gray-900 flex items-center">
            <i class="pi pi-clock text-blue-600 mr-2"></i>
            Time Tracker
          </h1>

          <!-- Mobile User Actions -->
          <div class="flex items-center space-x-2">
            <Button
                icon="pi pi-cog"
                severity="secondary"
                text
                @click="showSettings = true"
                v-tooltip.bottom="'Settings'"
                class="w-8 h-8"
            />
            <Button
                icon="pi pi-question-circle"
                severity="secondary"
                text
                @click="showHelp = true"
                v-tooltip.bottom="'Help'"
                class="w-8 h-8"
            />
          </div>
        </div>

        <!-- Desktop Navigation -->
        <div class="hidden md:block">
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
    </div>

    <!-- Mobile Menu Overlay -->
    <div
        v-if="showMobileMenu"
        class="fixed inset-0 bg-black bg-opacity-50 z-50 md:hidden"
        @click="showMobileMenu = false"
    >
      <div
          class="bg-white w-80 h-full shadow-xl transform transition-transform duration-300"
          @click.stop
      >
        <!-- Mobile Menu Header -->
        <div class="flex items-center justify-between p-4 border-b border-gray-200">
          <h2 class="text-lg font-semibold text-gray-900">Navigation</h2>
          <Button
              icon="pi pi-times"
              severity="secondary"
              text
              @click="showMobileMenu = false"
          />
        </div>

        <!-- Mobile Menu Content -->
        <div class="p-4 space-y-2">
          <!-- User Info -->
          <div class="flex items-center space-x-3 p-3 bg-gray-50 rounded-lg mb-4">
            <div class="w-10 h-10 bg-blue-600 rounded-full flex items-center justify-center">
              <i class="pi pi-user text-white"></i>
            </div>
            <div>
              <div class="font-medium text-gray-900">Local Admin</div>
              <div class="text-sm text-gray-600">Administrator</div>
            </div>
          </div>

          <!-- Navigation Items -->
          <div class="space-y-1">
            <button
                @click="navigateAndClose('/')"
                class="w-full flex items-center p-3 text-left hover:bg-gray-100 rounded-lg transition-colors"
            >
              <i class="pi pi-calendar text-blue-600 mr-3"></i>
              <span class="font-medium">Timesheet</span>
            </button>

            <!-- Reports Submenu -->
            <div class="space-y-1">
              <button
                  @click="showReportsSubmenu = !showReportsSubmenu"
                  class="w-full flex items-center justify-between p-3 text-left hover:bg-gray-100 rounded-lg transition-colors"
              >
                <div class="flex items-center">
                  <i class="pi pi-chart-bar text-purple-600 mr-3"></i>
                  <span class="font-medium">Reports</span>
                </div>
                <i class="pi" :class="showReportsSubmenu ? 'pi-chevron-down' : 'pi-chevron-right'"></i>
              </button>

              <div v-if="showReportsSubmenu" class="ml-6 space-y-1">
                <button
                    @click="navigateAndClose('/reports/daily')"
                    class="w-full flex items-center p-2 text-left hover:bg-gray-100 rounded-lg transition-colors text-sm"
                >
                  <i class="pi pi-calendar text-gray-500 mr-2"></i>
                  Daily Reports
                </button>
                <button
                    @click="navigateAndClose('/reports/weekly')"
                    class="w-full flex items-center p-2 text-left hover:bg-gray-100 rounded-lg transition-colors text-sm"
                >
                  <i class="pi pi-chart-line text-gray-500 mr-2"></i>
                  Weekly Summary
                </button>
                <button
                    @click="navigateAndClose('/reports/monthly')"
                    class="w-full flex items-center p-2 text-left hover:bg-gray-100 rounded-lg transition-colors text-sm"
                >
                  <i class="pi pi-chart-pie text-gray-500 mr-2"></i>
                  Monthly Overview
                </button>
              </div>
            </div>

            <!-- Export -->
            <button
                @click="showPlaceholder('Export'); showMobileMenu = false"
                class="w-full flex items-center p-3 text-left hover:bg-gray-100 rounded-lg transition-colors"
            >
              <i class="pi pi-download text-green-600 mr-3"></i>
              <span class="font-medium">Export</span>
            </button>

            <!-- Manage -->
            <button
                @click="showPlaceholder('Manage'); showMobileMenu = false"
                class="w-full flex items-center p-3 text-left hover:bg-gray-100 rounded-lg transition-colors"
            >
              <i class="pi pi-cog text-orange-600 mr-3"></i>
              <span class="font-medium">Manage</span>
            </button>
          </div>
        </div>
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
const showMobileMenu = ref(false)
const showReportsSubmenu = ref(false)

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
        icon: 'pi pi-book',
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

const navigateAndClose = (path: string) => {
  navigateTo(path)
  showMobileMenu.value = false
  showReportsSubmenu.value = false
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

/* Mobile menu animation */
.mobile-menu-enter-active,
.mobile-menu-leave-active {
  transition: transform 0.3s ease;
}

.mobile-menu-enter-from {
  transform: translateX(-100%);
}

.mobile-menu-leave-to {
  transform: translateX(-100%);
}

/* Ensure mobile menu is above everything */
.mobile-menu-overlay {
  z-index: 9999;
}

/* Hide scrollbar on mobile menu */
.mobile-menu-content {
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.mobile-menu-content::-webkit-scrollbar {
  display: none;
}
</style>