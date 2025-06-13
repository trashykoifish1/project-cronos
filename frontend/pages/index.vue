<template>
  <div class="min-h-screen bg-app-primary">
    <!-- Hero Section -->
    <div class="relative overflow-hidden">
      <!-- Background Decoration -->
      <div class="absolute inset-0 opacity-50"
           style="background: linear-gradient(135deg, rgb(var(--blue-50)), rgb(var(--purple-50)))"></div>
      <div class="absolute top-0 left-0 w-64 h-64 rounded-full mix-blend-multiply filter blur-xl opacity-30 animate-blob"
           style="background-color: rgb(var(--blue-200))"></div>
      <div class="absolute top-0 right-0 w-72 h-72 rounded-full mix-blend-multiply filter blur-xl opacity-30 animate-blob animation-delay-2000"
           style="background-color: rgb(var(--purple-200))"></div>
      <div class="absolute -bottom-8 left-20 w-80 h-80 rounded-full mix-blend-multiply filter blur-xl opacity-30 animate-blob animation-delay-4000"
           style="background-color: rgb(var(--green-200))"></div>

      <!-- Hero Content -->
      <div class="relative px-6 py-16 lg:py-24">
        <div class="max-w-6xl mx-auto text-center">
          <!-- Welcome Message -->
          <div class="mb-8">
            <Chip
                label="Welcome to Your Personal Time Tracker!"
                class="mb-4 bg-app-blue-50 text-app-blue-700 border-app-blue-200"
                icon="pi pi-heart"
            />
            <h1 class="text-4xl lg:text-6xl font-bold text-app-primary mb-6 leading-tight">
              Master Your Time,<br>
              <span class="text-app-blue-600">Boost Your Productivity</span>
            </h1>
            <p class="text-xl lg:text-2xl text-app-secondary max-w-3xl mx-auto leading-relaxed">
              Thank you for choosing our time tracking open-source project! We hope this would boost your productivity in some way. Best of wishes!
            </p>
          </div>

          <!-- CTA Buttons -->
          <div class="flex flex-col sm:flex-row gap-4 justify-center items-center mb-12">
            <Button
                label="Start Tracking Time"
                icon="pi pi-play"
                size="large"
                @click="navigateTo('/timesheet')"
                class="px-8 py-3"
            />
            <Button
                label="View Reports"
                icon="pi pi-chart-bar"
                size="large"
                severity="secondary"
                outlined
                @click="navigateTo('/reports/daily')"
                class="px-8 py-3"
            />
          </div>

          <!-- Quick Stats -->
          <div class="grid grid-cols-1 md:grid-cols-3 gap-6 max-w-4xl mx-auto">
            <Card class="text-center hover:shadow-lg transition-shadow duration-300">
              <template #content>
                <div class="py-4">
                  <i class="pi pi-clock text-3xl text-app-blue-600 mb-3"></i>
                  <h3 class="text-lg font-semibold text-app-primary mb-2">Smart Time Tracking</h3>
                  <p class="text-app-secondary text-sm">
                    Drag-and-paint interface makes logging time entries effortless and intuitive
                  </p>
                </div>
              </template>
            </Card>

            <Card class="text-center hover:shadow-lg transition-shadow duration-300">
              <template #content>
                <div class="py-4">
                  <i class="pi pi-chart-line text-3xl text-app-green-600 mb-3"></i>
                  <h3 class="text-lg font-semibold text-app-primary mb-2">Insightful Analytics</h3>
                  <p class="text-app-secondary text-sm">
                    Daily, weekly, and monthly reports with beautiful charts and productivity insights
                  </p>
                </div>
              </template>
            </Card>

            <Card class="text-center hover:shadow-lg transition-shadow duration-300">
              <template #content>
                <div class="py-4">
                  <i class="pi pi-download text-3xl text-app-purple-600 mb-3"></i>
                  <h3 class="text-lg font-semibold text-app-primary mb-2">Easy Export</h3>
                  <p class="text-app-secondary text-sm">
                    Export your data to CSV for reports, invoicing, or integration with other tools
                  </p>
                </div>
              </template>
            </Card>
          </div>
        </div>
      </div>
    </div>

    <!-- Features Section -->
    <div class="py-16 lg:py-24 bg-app-secondary">
      <div class="max-w-6xl mx-auto px-6">
        <div class="text-center mb-16">
          <h2 class="text-3xl lg:text-4xl font-bold text-app-primary mb-4">
            Everything You Need to Track Time Effectively
          </h2>
          <p class="text-lg text-app-secondary max-w-2xl mx-auto">
            Our comprehensive time tracking solution helps you understand where your time goes
            and optimize your productivity with features designed as a solution to a real-world problem.
          </p>
        </div>

        <!-- Feature Grid -->
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-16">
          <!-- Timesheet Feature -->
          <Card class="hover:shadow-lg transition-shadow duration-300">
            <template #content>
              <div class="flex items-start space-x-4 p-6">
                <div class="bg-app-blue-50 p-3 rounded-lg">
                  <i class="pi pi-calendar text-2xl text-app-blue-600"></i>
                </div>
                <div class="flex-1">
                  <h3 class="text-xl font-semibold text-app-primary mb-3">Interactive Timesheet</h3>
                  <p class="text-app-secondary mb-4">
                    Visual drag-and-paint interface with 15-minute precision. Click and drag to create
                    time entries, with real-time overlap detection and automatic validation.
                  </p>
                  <div class="flex flex-wrap gap-2">
                    <Tag severity="info" value="15-min precision" />
                    <Tag severity="success" value="Overlap detection" />
                    <Tag severity="secondary" value="Auto-save" />
                  </div>
                </div>
              </div>
            </template>
          </Card>

          <!-- Categories & Tasks Feature -->
          <Card class="hover:shadow-lg transition-shadow duration-300">
            <template #content>
              <div class="flex items-start space-x-4 p-6">
                <div class="bg-app-green-50 p-3 rounded-lg">
                  <i class="pi pi-tags text-2xl text-app-green-600"></i>
                </div>
                <div class="flex-1">
                  <h3 class="text-xl font-semibold text-app-primary mb-3">Smart Organization</h3>
                  <p class="text-app-secondary mb-4">
                    Organize your work with custom categories and color-coded tasks. Hierarchical
                    structure helps you maintain clarity and consistency in your time tracking.
                  </p>
                  <div class="flex flex-wrap gap-2">
                    <Tag severity="success" value="Custom categories" />
                    <Tag severity="info" value="Color coding" />
                    <Tag severity="secondary" value="Hierarchical" />
                  </div>
                </div>
              </div>
            </template>
          </Card>

          <!-- Reports Feature -->
          <Card class="hover:shadow-lg transition-shadow duration-300">
            <template #content>
              <div class="flex items-start space-x-4 p-6">
                <div class="bg-app-purple-50 p-3 rounded-lg">
                  <i class="pi pi-chart-bar text-2xl text-app-purple-600"></i>
                </div>
                <div class="flex-1">
                  <h3 class="text-xl font-semibold text-app-primary mb-3">Comprehensive Reports</h3>
                  <p class="text-app-secondary mb-4">
                    Daily summaries, weekly overviews, monthly insights, and flexible statistics.
                    Beautiful charts and actionable insights help you understand your productivity patterns.
                  </p>
                  <div class="flex flex-wrap gap-2">
                    <Tag severity="info" value="Multiple timeframes" />
                    <Tag severity="success" value="Visual charts" />
                    <Tag severity="secondary" value="Insights" />
                  </div>
                </div>
              </div>
            </template>
          </Card>

          <!-- Export Feature -->
          <Card class="hover:shadow-lg transition-shadow duration-300">
            <template #content>
              <div class="flex items-start space-x-4 p-6">
                <div class="bg-app-orange-50 p-3 rounded-lg">
                  <i class="pi pi-download text-2xl text-app-orange-600"></i>
                </div>
                <div class="flex-1">
                  <h3 class="text-xl font-semibold text-app-primary mb-3">Flexible Export</h3>
                  <p class="text-app-secondary mb-4">
                    Export your time data to CSV format for integration with invoicing tools,
                    spreadsheets, or other applications. Multiple export formats available.
                  </p>
                  <div class="flex flex-wrap gap-2">
                    <Tag severity="info" value="CSV export" />
                    <Tag severity="success" value="Multiple formats" />
                    <Tag severity="secondary" value="Easy integration" />
                  </div>
                </div>
              </div>
            </template>
          </Card>
        </div>

        <!-- Quick Start Guide -->
        <Card class="bg-gradient-to-r from-app-blue-50 to-app-purple-50">
          <template #content>
            <div class="p-8">
              <div class="text-center mb-8">
                <h3 class="text-2xl font-bold text-app-primary mb-2">Get Started in 3 Simple Steps</h3>
                <p class="text-app-secondary">Start tracking your time in less than 2 minutes</p>
              </div>

              <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
                <div class="text-center">
                  <div class="bg-app-blue-600 text-white w-12 h-12 rounded-full flex items-center justify-center mx-auto mb-4 text-xl font-bold">
                    1
                  </div>
                  <h4 class="font-semibold text-app-primary mb-2">Create Categories</h4>
                  <p class="text-app-secondary text-sm">Set up work categories like "Development", "Meetings", or "Admin"</p>
                </div>

                <div class="text-center">
                  <div class="bg-app-green-600 text-white w-12 h-12 rounded-full flex items-center justify-center mx-auto mb-4 text-xl font-bold">
                    2
                  </div>
                  <h4 class="font-semibold text-app-primary mb-2">Add Tasks</h4>
                  <p class="text-app-secondary text-sm">Create specific tasks within each category with custom colors</p>
                </div>

                <div class="text-center">
                  <div class="bg-app-purple-600 text-white w-12 h-12 rounded-full flex items-center justify-center mx-auto mb-4 text-xl font-bold">
                    3
                  </div>
                  <h4 class="font-semibold text-app-primary mb-2">Track Time</h4>
                  <p class="text-app-secondary text-sm">Drag and paint on the timesheet to log your time entries</p>
                </div>
              </div>
            </div>
          </template>
        </Card>
      </div>
    </div>

    <!-- Thank You Section -->
    <div class="py-16 lg:py-24 bg-app-primary">
      <div class="max-w-4xl mx-auto text-center px-6">
        <div class="mb-8">
          <i class="pi pi-heart text-5xl text-app-red-600 mb-6"></i>
          <h2 class="text-3xl lg:text-4xl font-bold text-app-primary mb-6">
            Thank You for Choosing Us!
          </h2>
          <p class="text-lg text-app-secondary mb-8 max-w-2xl mx-auto">
            We're honored that you've chosen our time tracking application to help manage your productivity.
            Your time is valuable, and we're committed to helping you make the most of it.
          </p>
        </div>

        <!-- Final CTA -->
        <div class="mt-12">
          <h3 class="text-xl font-semibold text-app-primary mb-4">Ready to start tracking?</h3>
          <div class="flex flex-col sm:flex-row gap-4 justify-center">
            <Button
                label="Open Timesheet"
                icon="pi pi-calendar"
                size="large"
                @click="navigateTo('/timesheet')"
                class="px-8"
            />
            <Button
                label="Explore Reports"
                icon="pi pi-chart-line"
                size="large"
                severity="secondary"
                outlined
                @click="navigateTo('/reports/daily')"
                class="px-8"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
// Page setup
definePageMeta({
  layout: 'default'
})

// Composables
const toast = useToast()

// Methods
const showDocumentation = () => {
  toast.add({
    severity: 'info',
    summary: 'Documentation',
    detail: 'Documentation features coming soon!',
    life: 3000
  })
}

const showSupport = () => {
  toast.add({
    severity: 'info',
    summary: 'Support',
    detail: 'Support system coming soon!',
    life: 3000
  })
}

// Performance optimization for theme changes
const { isDark } = useTheme()

// Debounce expensive operations during theme transitions
let isTransitioning = false

watch(isDark, () => {
  if (isTransitioning) return

  isTransitioning = true

  // Allow theme transition to complete before re-enabling animations
  setTimeout(() => {
    isTransitioning = false
  }, 200)
})
</script>

<style scoped>
/* Blob animation for background elements */
@keyframes blob {
  0% {
    transform: translate(0px, 0px) scale(1);
  }
  33% {
    transform: translate(30px, -50px) scale(1.1);
  }
  66% {
    transform: translate(-20px, 20px) scale(0.9);
  }
  100% {
    transform: translate(0px, 0px) scale(1);
  }
}

.animate-blob {
  animation: blob 7s infinite;
  /* Disable transitions on animated elements to prevent conflicts */
  transition: none !important;
}

.animation-delay-2000 {
  animation-delay: 2s;
}

.animation-delay-4000 {
  animation-delay: 4s;
}

/* Optimized theme transitions - only apply to specific properties */
.bg-app-primary,
.bg-app-secondary,
.text-app-primary,
.text-app-secondary {
  transition: background-color 0.15s ease-out, color 0.15s ease-out;
}

/* Card hover effects with optimized transitions */
.hover\:shadow-lg {
  transition: box-shadow 0.2s ease-out;
}

.hover\:shadow-lg:hover {
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
}

/* Optimized gradient backgrounds */
.bg-gradient-to-br {
  background-image: linear-gradient(to bottom right, rgb(var(--blue-50)), rgb(var(--purple-50)));
  transition: none; /* Remove transition from gradients for better performance */
}

.bg-gradient-to-r {
  background-image: linear-gradient(to right, rgb(var(--blue-50)), rgb(var(--purple-50)));
  transition: none; /* Remove transition from gradients for better performance */
}

/* Specific theme-aware elements with controlled transitions */
:deep(.p-card) {
  transition: background-color 0.15s ease-out, border-color 0.15s ease-out;
}

:deep(.p-button) {
  transition: background-color 0.15s ease-out, border-color 0.15s ease-out, color 0.15s ease-out;
}

:deep(.p-chip) {
  transition: background-color 0.15s ease-out, border-color 0.15s ease-out, color 0.15s ease-out;
}

:deep(.p-tag) {
  transition: background-color 0.15s ease-out, border-color 0.15s ease-out, color 0.15s ease-out;
}

/* Reduce motion for users who prefer it */
@media (prefers-reduced-motion: reduce) {
  .animate-blob {
    animation: none;
  }

  * {
    transition: none !important;
  }
}

/* Performance optimization: Use transform3d for hardware acceleration */
.animate-blob {
  transform: translate3d(0, 0, 0);
  will-change: transform;
}

/* Optimize background decorations */
.absolute {
  backface-visibility: hidden;
  perspective: 1000px;
}
</style>