<template>
  <Card class="spotify-card">
    <template #header>
      <div style="padding: 1rem 1rem 0.5rem 1rem;">
        <div class="flex items-center justify-between">
          <div class="flex items-center space-x-2">
            <i class="pi pi-play-circle text-app-green-600"></i>
            <span class="text-sm font-semibold text-app-primary">Now Playing</span>
          </div>
          <Button
              icon="pi pi-refresh"
              size="small"
              severity="secondary"
              text
              @click="refreshPlayback"
              :loading="isRefreshing"
              v-tooltip.bottom="'Refresh'"
              class="w-8 h-8"
          />
        </div>
      </div>
    </template>

    <template #content>
      <div style="padding: 0 1rem 1rem 1rem;">
        <!-- Connected State with Active Track -->
        <div v-if="spotify.isConnected.value && spotify.hasActiveTrack.value" class="space-y-4">
          <!-- Track Info -->
          <div class="flex items-center space-x-3">
            <!-- Album Art -->
            <div class="flex-shrink-0">
              <img
                  v-if="spotify.currentAlbumArt.value"
                  :src="spotify.currentAlbumArt.value"
                  :alt="`${spotify.currentTrack.value?.name} album art`"
                  class="w-12 h-12 rounded-lg shadow-md object-cover"
              />
              <div
                  v-else
                  class="w-12 h-12 bg-app-tertiary rounded-lg flex items-center justify-center"
              >
                <i class="pi pi-music-note text-app-secondary text-lg"></i>
              </div>
            </div>

            <!-- Track Details -->
            <div class="flex-1 min-w-0">
              <div class="text-sm font-medium text-app-primary truncate">
                {{ spotify.currentTrack.value?.name || 'Unknown Track' }}
              </div>
              <div class="text-xs text-app-secondary truncate">
                {{ spotify.currentArtists.value || 'Unknown Artist' }}
              </div>
              <div class="text-xs text-app-secondary truncate opacity-75">
                {{ spotify.currentTrack.value?.album.name || 'Unknown Album' }}
              </div>
            </div>
          </div>

          <!-- Progress Bar -->
          <div class="space-y-2">
            <div class="flex items-center justify-between text-xs text-app-secondary">
              <span>{{ formatTime(spotify.progress.value) }}</span>
              <span>{{ formatTime(spotify.duration.value) }}</span>
            </div>
            <div class="w-full bg-app-tertiary rounded-full" style="height: 6px;">
              <div
                  class="bg-app-green-600 rounded-full transition-all duration-300"
                  :style="{
                  width: `${spotify.progressPercentage.value}%`,
                  height: '6px'
                }"
              ></div>
            </div>
          </div>

          <!-- Playback Controls -->
          <div class="flex items-center justify-center space-x-2">
            <!-- Previous Track -->
            <Button
                icon="pi pi-step-backward"
                size="small"
                severity="secondary"
                outlined
                @click="spotify.previousTrack"
                :disabled="controlsDisabled"
                class="w-8 h-8"
                v-tooltip.bottom="'Previous'"
            />

            <!-- Play/Pause -->
            <Button
                :icon="spotify.isPlaying.value ? 'pi pi-pause' : 'pi pi-play'"
                size="large"
                :severity="spotify.isPlaying.value ? 'secondary' : 'success'"
                @click="togglePlayback"
                :disabled="controlsDisabled"
                class="w-10 h-10"
                v-tooltip.bottom="spotify.isPlaying.value ? 'Pause' : 'Play'"
            />

            <!-- Next Track -->
            <Button
                icon="pi pi-step-forward"
                size="small"
                severity="secondary"
                outlined
                @click="spotify.nextTrack"
                :disabled="controlsDisabled"
                class="w-8 h-8"
                v-tooltip.bottom="'Next'"
            />
          </div>

          <!-- Volume Control -->
          <div class="space-y-2">
            <div class="flex items-center justify-between">
              <span class="text-xs font-medium text-app-primary">Volume</span>
              <span class="text-xs text-app-secondary">{{ spotify.volume.value }}%</span>
            </div>
            <div class="flex items-center space-x-2">
              <i class="pi pi-volume-down text-app-secondary text-sm"></i>
              <Slider
                  v-model="volumeValue"
                  :min="0"
                  :max="100"
                  @slideend="handleVolumeChange"
                  class="flex-1 spotify-volume-slider"
                  :disabled="controlsDisabled"
              />
              <i class="pi pi-volume-up text-app-secondary text-sm"></i>
            </div>
          </div>
        </div>

        <!-- Connected State but No Active Track -->
        <div v-else-if="spotify.isConnected.value && !spotify.hasActiveTrack.value" class="text-center" style="padding: 1.5rem 0;">
          <div class="space-y-3">
            <i class="pi pi-music-note text-3xl text-app-secondary"></i>
            <div>
              <div class="text-sm font-medium text-app-primary">No Track Playing</div>
              <div class="text-xs text-app-secondary" style="margin-top: 0.25rem;">
                Start playing music on Spotify to see controls here
              </div>
            </div>
            <Button
                label="Open Spotify"
                icon="pi pi-external-link"
                size="small"
                severity="secondary"
                outlined
                @click="openSpotify"
            />
          </div>
        </div>

        <!-- Not Connected State -->
        <div v-else class="text-center" style="padding: 1.5rem 0;">
          <div class="space-y-3">
            <i class="pi pi-link text-3xl text-app-secondary"></i>
            <div>
              <div class="text-sm font-medium text-app-primary">Spotify Not Connected</div>
              <div class="text-xs text-app-secondary" style="margin-top: 0.25rem;">
                Connect your Spotify account to control music while tracking time
              </div>
            </div>
            <Button
                label="Connect Spotify"
                icon="pi pi-play-circle"
                size="small"
                @click="openSettings"
            />
          </div>
        </div>

        <!-- Error State -->
        <div v-if="spotify.error.value" class="bg-app-red-50 border border-app-red-200 rounded text-center" style="margin-top: 0.75rem; padding: 0.5rem;">
          <div class="text-xs text-app-red-600">{{ spotify.error.value }}</div>
          <Button
              label="Clear"
              size="small"
              severity="danger"
              text
              @click="spotify.clearError"
              style="margin-top: 0.25rem;"
          />
        </div>
      </div>
    </template>
  </Card>
</template>

<script setup lang="ts">
// Composables
const spotify = useSpotify()
const toast = useToast()

// Local state
const isRefreshing = ref(false)
const volumeValue = ref(50)

const emit = defineEmits(['open-settings'])

// Computed
const controlsDisabled = computed(() =>
    !spotify.isConnected.value || !spotify.hasActiveTrack.value
)

// Watch for volume changes from Spotify
watch(() => spotify.volume.value, (newVolume) => {
  volumeValue.value = newVolume
}, { immediate: true })

// Auto-refresh playback state
let refreshInterval: NodeJS.Timeout | null = null

onMounted(() => {
  // Initialize Spotify if not already done
  spotify.initialize()

  // Set up auto-refresh for connected users
  if (spotify.isConnected.value) {
    startAutoRefresh()
  }
})

onUnmounted(() => {
  if (refreshInterval) {
    clearInterval(refreshInterval)
  }
})

// Watch connection status to start/stop auto-refresh
watch(() => spotify.isConnected.value, (isConnected) => {
  if (isConnected) {
    startAutoRefresh()
  } else {
    stopAutoRefresh()
  }
})

// Methods
const formatTime = (milliseconds: number): string => {
  const seconds = Math.floor(milliseconds / 1000)
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = seconds % 60
  return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`
}

const togglePlayback = async () => {
  try {
    if (spotify.isPlaying.value) {
      await spotify.pause()
    } else {
      await spotify.play()
    }
  } catch (error) {
    console.error('Error toggling playback:', error)
  }
}

const handleVolumeChange = async () => {
  try {
    await spotify.setVolume(volumeValue.value)
  } catch (error) {
    console.error('Error setting volume:', error)
    // Revert to previous value on error
    volumeValue.value = spotify.volume.value
  }
}

const refreshPlayback = async () => {
  if (!spotify.isConnected.value) return

  isRefreshing.value = true
  try {
    await spotify.getCurrentPlayback()
  } catch (error) {
    console.error('Error refreshing playback:', error)
  } finally {
    isRefreshing.value = false
  }
}

const startAutoRefresh = () => {
  if (refreshInterval) return

  // Refresh every 5 seconds when connected
  refreshInterval = setInterval(() => {
    if (spotify.isConnected.value) {
      spotify.getCurrentPlayback()
    }
  }, 5000)
}

const stopAutoRefresh = () => {
  if (refreshInterval) {
    clearInterval(refreshInterval)
    refreshInterval = null
  }
}

const openSpotify = () => {
  window.open('https://open.spotify.com', '_blank')
}

const openSettings = () => {
  emit('open-settings')
}
</script>

<style scoped>
/* Custom slider styling using CSS variables that should be available */
.spotify-volume-slider :deep(.p-slider-handle) {
  background-color: rgb(var(--green-600));
  border-color: rgb(var(--green-600));
}

.spotify-volume-slider :deep(.p-slider-range) {
  background-color: rgb(var(--green-600));
}
</style>