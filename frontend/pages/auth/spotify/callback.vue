<template>
  <div class="min-h-screen bg-app-primary flex items-center justify-center">
    <Card class="w-full max-w-md">
      <template #header>
        <div class="text-center" style="padding: 1.5rem 1.5rem 0 1.5rem;">
          <i class="pi pi-play-circle text-4xl text-app-green-600 mb-4"></i>
          <h2 class="text-xl font-semibold text-app-primary">Spotify Connection</h2>
        </div>
      </template>

      <template #content>
        <div class="text-center" style="padding: 0 1.5rem 1.5rem 1.5rem;">
          <!-- Processing State -->
          <div v-if="isProcessing" class="space-y-4">
            <ProgressSpinner size="32px" />
            <div>
              <div class="text-sm font-medium text-app-primary">Connecting to Spotify...</div>
              <div class="text-xs text-app-secondary mt-1">
                Exchanging authorization code for access tokens...
              </div>
            </div>
          </div>

          <!-- Success State -->
          <div v-else-if="authResult === 'success'" class="space-y-4">
            <div class="bg-app-green-50 border border-app-green-200 rounded-lg" style="padding: 0.75rem;">
              <i class="pi pi-check-circle text-app-green-600 text-2xl mb-2"></i>
              <div class="text-sm font-medium text-app-green-800">Successfully Connected!</div>
              <div class="text-xs text-app-green-700 mt-1">
                Your Spotify account is now connected and ready to use
              </div>
            </div>

            <div class="text-xs text-app-secondary">
              Redirecting you back to the timesheet...
            </div>
          </div>

          <!-- Error State -->
          <div v-else-if="authResult === 'error'" class="space-y-4">
            <div class="bg-app-red-50 border border-app-red-200 rounded-lg" style="padding: 0.75rem;">
              <i class="pi pi-times-circle text-app-red-600 text-2xl mb-2"></i>
              <div class="text-sm font-medium text-app-red-800">Connection Failed</div>
              <div class="text-xs text-app-red-700 mt-1">
                {{ errorMessage }}
              </div>
            </div>

            <div class="space-x-2">
              <Button
                  label="Try Again"
                  icon="pi pi-refresh"
                  size="small"
                  @click="retryConnection"
              />
              <Button
                  label="Go Back"
                  icon="pi pi-arrow-left"
                  size="small"
                  severity="secondary"
                  outlined
                  @click="goBack"
              />
            </div>
          </div>

          <!-- Access Denied State -->
          <div v-else-if="authResult === 'denied'" class="space-y-4">
            <div class="bg-app-orange-50 border border-app-orange-200 rounded-lg" style="padding: 0.75rem;">
              <i class="pi pi-info-circle text-app-orange-600 text-2xl mb-2"></i>
              <div class="text-sm font-medium text-app-orange-800">Access Denied</div>
              <div class="text-xs text-app-orange-700 mt-1">
                You chose not to authorize the Spotify connection
              </div>
            </div>

            <Button
                label="Go Back to Timesheet"
                icon="pi pi-arrow-left"
                size="small"
                severity="secondary"
                @click="goBack"
            />
          </div>
        </div>
      </template>
    </Card>
  </div>
</template>

<script setup lang="ts">
// Composables
const route = useRoute()
const router = useRouter()
const spotify = useSpotify()
const toast = useToast()

// Local state
const isProcessing = ref(true)
const authResult = ref<'success' | 'error' | 'denied' | null>(null)
const errorMessage = ref('')

// Process the auth callback on mount
onMounted(async () => {
  await processAuthCallback()
})

const processAuthCallback = async () => {
  try {
    // Check for error parameters
    const error = route.query.error as string
    if (error) {
      isProcessing.value = false
      if (error === 'access_denied') {
        authResult.value = 'denied'
      } else {
        authResult.value = 'error'
        errorMessage.value = `Spotify error: ${error}`
      }
      return
    }

    // Get the authorization code and state
    const code = route.query.code as string
    const state = route.query.state as string

    if (!code) {
      isProcessing.value = false
      authResult.value = 'error'
      errorMessage.value = 'No authorization code received from Spotify'
      return
    }

    if (!state) {
      isProcessing.value = false
      authResult.value = 'error'
      errorMessage.value = 'No state parameter received from Spotify'
      return
    }

    // Handle the auth callback
    const result = await spotify.handleAuthCallback(code, state)

    isProcessing.value = false

    if (result.success) {
      authResult.value = 'success'

      toast.add({
        severity: 'success',
        summary: 'Spotify Connected',
        detail: 'Your Spotify account has been successfully connected!',
        life: 3000
      })

      // Redirect back to timesheet after a brief delay
      setTimeout(() => {
        router.push('/timesheet')
      }, 2000)

    } else {
      authResult.value = 'error'
      errorMessage.value = getErrorMessage(result.error)
    }

  } catch (error: any) {
    console.error('Auth callback error:', error)
    isProcessing.value = false
    authResult.value = 'error'
    errorMessage.value = 'An unexpected error occurred during authentication'
  }
}

const getErrorMessage = (error: string | undefined) => {
  switch (error) {
    case 'state_mismatch':
      return 'Security validation failed. Please try connecting again.'
    case 'exchange_failed':
      return 'Failed to exchange authorization code. Please check your configuration.'
    default:
      return `Authentication failed: ${error || 'Unknown error'}. Please try again.`
  }
}

const retryConnection = () => {
  if (import.meta.client) {
    localStorage.removeItem('spotify_auth_state')
  }
  spotify.initiateAuth()
}

const goBack = () => {
  router.push('/timesheet')
}

// Set page meta
definePageMeta({
  layout: 'minimal'
})

useHead({
  title: 'Spotify Authentication - Project Cronos'
})
</script>