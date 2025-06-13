<template>
  <div class="min-h-screen bg-app-primary flex flex-col">
    <!-- Top Navigation Bar -->
    <div class="bg-app-secondary border-b border-app-primary sticky top-0 z-10">
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
          <NuxtLink to="/">
            <h1 class="text-xl font-bold text-app-primary flex items-center">
              <i class="pi pi-clock text-app-blue-600 mr-2"></i>
              Project Cronos
            </h1>
          </NuxtLink>

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
            <ThemeToggle variant="text" :is-mobile="true" />
          </div>
        </div>

        <!-- Desktop Navigation -->
        <div class="hidden md:block">
          <Menubar :model="menuItems" class="border-0 bg-transparent p-0 menubar">
            <template #start>
              <NuxtLink to="/">
                <h1 class="text-2xl font-bold text-app-primary flex items-center">
                  <i class="pi pi-clock text-app-blue-600 mr-2"></i>
                  Project Cronos
                </h1>
              </NuxtLink>
            </template>
            <template #end>
              <div class="flex items-center space-x-3">
                <div class="flex items-center space-x-3">
                  <span class="text-sm text-app-secondary">Local Admin</span>
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
                  <ThemeToggle variant="text" :is-mobile="true" />
                </div>
              </div>
            </template>
          </Menubar>
        </div>
      </div>
    </div>

    <!-- Mobile Menu Overlay -->
    <Drawer
        v-model:visible="showMobileMenu"
        class="bg-black md:hidden"
    >
        <!-- Mobile Menu Content -->
        <div class="p-4 space-y-2">
          <!-- User Info -->
          <div class="flex items-center space-x-3 p-3 bg-app-primary rounded-lg mb-4">
            <div class="w-10 h-10 bg-app-blue-600 rounded-full flex items-center justify-center">
              <i class="pi pi-user text-white"></i>
            </div>
            <div>
              <div class="font-medium text-app-primary">Local Admin</div>
              <div class="text-sm text-app-secondary">Administrator</div>
            </div>
          </div>

          <!-- Navigation Items -->
          <div class="space-y-1">
            <button
                @click="navigateAndClose('/timesheet')"
                class="w-full flex items-center p-3 text-left hover:bg-app-tertiary rounded-lg transition-colors"
            >
              <i class="pi pi-calendar text-app-blue-600 mr-3"></i>
              <span class="font-medium">Timesheet</span>
            </button>

            <!-- Reports Submenu -->
            <div class="space-y-1">
              <button
                  @click="showReportsSubmenu = !showReportsSubmenu"
                  class="w-full flex items-center justify-between p-3 text-left hover:bg-app-tertiary rounded-lg transition-colors"
              >
                <div class="flex items-center">
                  <i class="pi pi-chart-bar text-app-purple-600 mr-3"></i>
                  <span class="font-medium">Reports</span>
                </div>
                <i class="pi" :class="showReportsSubmenu ? 'pi-chevron-down' : 'pi-chevron-right'"></i>
              </button>

              <div v-if="showReportsSubmenu" class="ml-6 space-y-1">
                <button
                    @click="navigateAndClose('/reports/daily')"
                    class="w-full flex items-center p-2 text-left hover:bg-app-tertiary rounded-lg transition-colors text-sm"
                >
                  <i class="pi pi-calendar text-app-secondary mr-2"></i>
                  Daily Reports
                </button>
                <button
                    @click="navigateAndClose('/reports/weekly')"
                    class="w-full flex items-center p-2 text-left hover:bg-app-tertiary rounded-lg transition-colors text-sm"
                >
                  <i class="pi pi-chart-line text-app-secondary mr-2"></i>
                  Weekly Summary
                </button>
                <button
                    @click="navigateAndClose('/reports/monthly')"
                    class="w-full flex items-center p-2 text-left hover:bg-app-tertiary rounded-lg transition-colors text-sm"
                >
                  <i class="pi pi-chart-pie text-app-secondary mr-2"></i>
                  Monthly Overview
                </button>
              </div>
            </div>

            <!-- Manage -->
            <button
                @click="showPlaceholder('Manage'); showMobileMenu = false"
                class="w-full flex items-center p-3 text-left hover:bg-app-tertiary rounded-lg transition-colors"
            >
              <i class="pi pi-cog text-app-orange-600 mr-3"></i>
              <span class="font-medium">Manage</span>
            </button>
          </div>
        </div>
    </Drawer>

    <!-- Page Content -->
    <div class="app-main">
      <slot />
    </div>
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
          <h3 class="text-lg font-semibold text-app-primary mb-3">Application Settings</h3>
          <div class="space-y-4">
            <div class="flex items-center justify-between">
              <span class="text-sm text-app-primary">Time Zone</span>
              <span class="text-sm text-app-secondary">UTC (Local Admin)</span>
            </div>
            <div class="flex items-center justify-between">
              <span class="text-sm text-app-primary">Working Hours</span>
              <span class="text-sm text-app-secondary">7:00 AM - 5:00 PM</span>
            </div>
            <div class="flex items-center justify-between">
              <span class="text-sm text-app-primary">Time Interval</span>
              <span class="text-sm text-app-secondary">15 minutes</span>
            </div>
          </div>
        </div>

        <div class="text-center py-4 text-app-secondary">
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
          <div class="space-y-4 text-sm text-app-secondary">
            <div>
              <h4 class="font-semibold text-app-primary mb-2">Getting Started</h4>
              <ol class="list-decimal list-inside space-y-1">
                <li>Select a task from the sidebar on the left</li>
                <li>Click and drag across time slots to create time entries</li>
                <li>Use the date picker to navigate between days</li>
                <li>Right-click on existing entries to delete them</li>
              </ol>
            </div>

            <div>
              <h4 class="font-semibold text-app-primary mb-2">Navigation</h4>
              <ul class="list-disc list-inside space-y-1">
                <li>Use the ← → arrows to navigate days quickly</li>
                <li>Click "Today" to jump to the current date</li>
                <li>Access Reports and Export from the main menu</li>
              </ul>
            </div>

            <div>
              <h4 class="font-semibold text-app-primary mb-2">Tips</h4>
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

    <!-- Footer -->
    <div class="bg-app-secondary border-t border-app-primary py-8">
      <div class="max-w-6xl mx-auto px-6">
        <div class="flex flex-col md:flex-row justify-between items-center">
          <div class="flex items-center space-x-4 mb-4 md:mb-0">
            <i class="pi pi-clock text-2xl text-app-blue-600"></i>
            <span class="text-lg font-semibold text-app-primary">Project Cronos</span>
          </div>

          <div class="flex items-center space-x-6">
            <Button
                label="GitHub"
                icon="pi pi-github"
                text
                size="small"
                severity="secondary"
                @click="navigateTo('https://github.com/trashykoifish1/project-cronos', {external: true, open: {target: '_blank'}})"
            />
            <Button
                label="Support"
                icon="pi pi-question-circle"
                text
                size="small"
                severity="secondary"
                @click="navigateTo('https://github.com/trashykoifish1/project-cronos/issues', {external: true, open: {target: '_blank'}})"
            />
            <ThemeToggle variant="text"/>
          </div>
        </div>

        <div class="text-center mt-6 pt-6 border-t border-app-primary">
          <p class="text-app-secondary text-sm">
            © {{new Date().getFullYear()}} Project Cronos. Built with ❤️ for productivity enthusiasts.
          </p>
        </div>
      </div>
    </div>
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
      navigateTo('/timesheet')
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