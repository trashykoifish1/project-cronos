<template>
  <Drawer
      v-model:visible="isVisible"
      position="full"
      class="spotify-fullscreen-drawer"
      :style="{ backgroundColor: dominantColor, transition: 'background-color 1s ease' }"
      @update:visible="handleVisibilityChange"
  >
    <div
        class="spotify-fullscreen"
        :style="{ backgroundColor: dominantColor }"
    >
      <!-- Main Content -->
      <div class="fullscreen-content">
        <!-- Album Art Section -->
        <div class="album-section">
          <div class="album-art-container">
            <img
                v-if="spotify.currentAlbumArt.value"
                ref="albumImage"
                :src="spotify.currentAlbumArt.value"
                :alt="`${spotify.currentTrack.value?.name} album art`"
                class="album-art"
                crossorigin="anonymous"
                @load="extractDominantColor"
                @error="handleImageError"
            />
            <div
                v-else
                class="album-art-placeholder"
            >
              <i class="pi pi-music-note"></i>
            </div>
          </div>
        </div>

        <!-- Track Info Section -->
        <div class="track-info-section">
          <div class="track-info" :style="{color: vibrantColor}">
            <h1 class="track-title">
              {{ spotify.currentTrack.value?.name || 'Unknown Track' }}
            </h1>
            <p class="track-artist">
              {{ spotify.currentArtists.value || 'Unknown Artist' }}
            </p>
            <p class="track-album">
              {{ spotify.currentTrack.value?.album.name || 'Unknown Album' }}
            </p>
          </div>

          <!-- Progress Section -->
          <div class="progress-section">
            <div class="progress-times">
              <span>{{ formatTime(spotify.progress.value) }}</span>
              <span>{{ formatTime(spotify.duration.value) }}</span>
            </div>
            <div class="progress-bar-container">
              <div
                  class="progress-bar"
                  :style="{
                  width: `${spotify.progressPercentage.value}%`,
                  backgroundColor: accentColor
                }"
              ></div>
            </div>
          </div>

          <!-- Controls Section -->
          <div class="controls-section">
            <Button
                icon="pi pi-step-backward"
                severity="secondary"
                text
                @click="spotify.previousTrack"
                class="control-button"
                :style="{ borderColor: accentColor }"
                v-tooltip.top="'Previous'"
            />

            <Button
                :icon="spotify.isPlaying.value ? 'pi pi-pause' : 'pi pi-play'"
                severity="secondary"
                text
                @click="togglePlayback"
                class="control-button play-button"
                :style="{ borderColor: accentColor, backgroundColor: `${accentColor}20` }"
                v-tooltip.top="spotify.isPlaying.value ? 'Pause' : 'Play'"
            />

            <Button
                icon="pi pi-step-forward"
                severity="secondary"
                text
                @click="spotify.nextTrack"
                class="control-button"
                :style="{ borderColor: accentColor }"
                v-tooltip.top="'Next'"
            />
          </div>

          <!-- Volume Section -->
          <div class="volume-section">
            <div class="volume-controls">
              <i class="pi pi-volume-down volume-icon"></i>
              <Slider
                  v-model="volumeValue"
                  :min="0"
                  :max="100"
                  @slideend="handleVolumeChange"
                  class="volume-slider"
                  :style="{
                  '--accent-color': accentColor,
                }"
              />
              <i class="pi pi-volume-up volume-icon"></i>
            </div>
            <div class="volume-label">{{ spotify.volume.value }}%</div>
          </div>
        </div>
      </div>

      <!-- Ambient Background Effect -->
      <div
          class="ambient-background"
          :style="{
          backgroundImage: spotify.currentAlbumArt.value ? `url(${spotify.currentAlbumArt.value})` : 'none'
        }"
      ></div>

      <!-- Color Extraction Debug (remove in production) -->
      <div v-if="showDebugColors" class="debug-colors">
        <div class="debug-color" :style="{ backgroundColor: dominantColor }" title="Dominant"></div>
        <div class="debug-color" :style="{ backgroundColor: vibrantColor }" title="Vibrant"></div>
        <div class="debug-color" :style="{ backgroundColor: mutedColor }" title="Muted"></div>
        <div class="debug-color" :style="{ backgroundColor: accentColor }" title="Accent"></div>
      </div>
    </div>
  </Drawer>
</template>

<script setup lang="ts">
import {Vibrant} from 'node-vibrant/browser'

// Type definitions for Vibrant
interface VibrantSwatch {
  rgb: [number, number, number]
  hex: string
  hsl: [number, number, number]
  population: number
}

interface VibrantPalette {
  Vibrant?: VibrantSwatch
  DarkVibrant?: VibrantSwatch
  LightVibrant?: VibrantSwatch
  Muted?: VibrantSwatch
  DarkMuted?: VibrantSwatch
  LightMuted?: VibrantSwatch
}

// Props
interface Props {
  visible?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  visible: false
})

// Emits
const emit = defineEmits(['update:visible', 'close'])

// Composables
const spotify = useSpotify()

// Local state
const isVisible = ref(false)
const volumeValue = ref(50)
const dominantColor = ref('#1a1a1a')
const vibrantColor = ref('#1DB954') // Spotify green default
const mutedColor = ref('#333333')
const accentColor = ref('#1DB954')
const albumImage = ref<HTMLImageElement>()
const showDebugColors = ref(false) // Set to true to see extracted colors

// Predefined color palettes for fallback
const colorPalettes = [
  {
    dominant: '#1a1a1a',
    vibrant: '#8B5CF6',
    muted: '#6B46C1',
    accent: '#A855F7'
  },
  {
    dominant: '#0f172a',
    vibrant: '#10B981',
    muted: '#065F46',
    accent: '#34D399'
  },
  {
    dominant: '#1e1b4b',
    vibrant: '#3B82F6',
    muted: '#1E40AF',
    accent: '#60A5FA'
  },
  {
    dominant: '#7c2d12',
    vibrant: '#F59E0B',
    muted: '#D97706',
    accent: '#FBBF24'
  },
  {
    dominant: '#7f1d1d',
    vibrant: '#EF4444',
    muted: '#DC2626',
    accent: '#F87171'
  },
  {
    dominant: '#831843',
    vibrant: '#EC4899',
    muted: '#BE185D',
    accent: '#F9A8D4'
  },
  {
    dominant: '#365314',
    vibrant: '#84CC16',
    muted: '#65A30D',
    accent: '#A3E635'
  },
  {
    dominant: '#0c4a6e',
    vibrant: '#0EA5E9',
    muted: '#0284C7',
    accent: '#38BDF8'
  }
]

// Watch props
watch(() => props.visible, (newValue) => {
  isVisible.value = newValue
}, { immediate: true })

// Watch for volume changes
watch(() => spotify.volume.value, (newVolume) => {
  volumeValue.value = newVolume
}, { immediate: true })

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
    volumeValue.value = spotify.volume.value
  }
}

const generateColorFromText = (text: string): typeof colorPalettes[0] => {
  if (!text) return colorPalettes[0]

  // Create a simple hash from the text
  let hash = 0
  for (let i = 0; i < text.length; i++) {
    const char = text.charCodeAt(i)
    hash = ((hash << 5) - hash) + char
    hash = hash & hash // Convert to 32-bit integer
  }

  // Use absolute value and modulo to get palette index
  const index = Math.abs(hash) % colorPalettes.length
  return colorPalettes[index]
}

const extractDominantColor = async () => {
  if (!albumImage.value || !spotify.currentAlbumArt.value) {
    useFallbackColor()
    return
  }

  try {
    console.log('Extracting colors from album art...')

    // Create vibrant instance with proper configuration
    const vibrantInstance = new (Vibrant as any)(albumImage.value, {
      colorCount: 64,
      quality: 1
    })

    const palette: VibrantPalette = await vibrantInstance.getPalette()

    // Extract colors with fallbacks
    const dominantSwatch = palette.DarkMuted || palette.Muted || palette.DarkVibrant
    const vibrantSwatch = palette.Vibrant || palette.LightVibrant
    const mutedSwatch = palette.Muted || palette.DarkMuted
    const accentSwatch = palette.Vibrant || palette.LightVibrant || palette.DarkVibrant

    if (dominantSwatch) {
      // Make the dominant color darker for better contrast
      const rgb = dominantSwatch.rgb
      const darkerRgb = rgb.map((c: number) => Math.floor(c * 0.3))
      dominantColor.value = `rgb(${darkerRgb.join(', ')})`
    }

    if (vibrantSwatch) {
      vibrantColor.value = vibrantSwatch.hex
    }

    if (mutedSwatch) {
      mutedColor.value = mutedSwatch.hex
    }

    if (accentSwatch) {
      accentColor.value = accentSwatch.hex
    }

  } catch (error) {
    console.warn('Vibrant color extraction failed:', error)
    useFallbackColor()
  }
}

const useFallbackColor = () => {
  // Use track name and artist to generate consistent color palette
  const trackName = spotify.currentTrack.value?.name || ''
  const artistName = spotify.currentArtists.value || ''
  const combinedText = trackName + artistName

  const palette = generateColorFromText(combinedText)

  dominantColor.value = palette.dominant
  vibrantColor.value = palette.vibrant
  mutedColor.value = palette.muted
  accentColor.value = palette.accent

  console.log('Using fallback color palette:', palette)
}

const handleImageError = () => {
  console.warn('Album art failed to load')
  useFallbackColor()
}

const closeFullscreen = () => {
  isVisible.value = false
}

const handleVisibilityChange = (visible: boolean) => {
  emit('update:visible', visible)
  if (!visible) {
    emit('close')
  }
}

// Auto-refresh when track changes
watch(() => spotify.currentAlbumArt.value, () => {
  if (spotify.currentAlbumArt.value) {
    // Wait for image to load
    nextTick(() => {
      setTimeout(() => {
        if (albumImage.value?.complete) {
          extractDominantColor()
        }
      }, 200)
    })
  } else {
    useFallbackColor()
  }
}, { immediate: true })

// Also generate color when track changes (even if album art is the same)
watch(() => [spotify.currentTrack.value?.name, spotify.currentArtists.value], () => {
  // Small delay to avoid too frequent updates
  setTimeout(() => {
    if (!spotify.currentAlbumArt.value) {
      useFallbackColor()
    }
  }, 300)
}, { immediate: true })

// Handle escape key
onMounted(() => {
  const handleEscape = (event: KeyboardEvent) => {
    if (event.key === 'Escape') {
      closeFullscreen()
    }
  }

  document.addEventListener('keydown', handleEscape)

  onUnmounted(() => {
    document.removeEventListener('keydown', handleEscape)
  })
})
</script>

<style scoped>
/* Fullscreen drawer styling - override PrimeVue defaults */
.spotify-fullscreen-drawer :deep(.p-drawer) {
  border: none;
  border-radius: 0;
  box-shadow: none;
  transition: background-clor 1s ease;

}

.spotify-fullscreen-drawer :deep(.p-drawer-content) {
  padding: 0;
  overflow: hidden;
  width: 100vw;
  height: 100vh;
  transition: background-color 1s ease;

}

.spotify-fullscreen-drawer :deep(.p-drawer-background) {
  background-color: transparent;
  transition: background-color 1s ease;

}

.spotify-fullscreen {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  transition: background-color 1s ease;
}

.ambient-background {
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background-size: cover;
  background-position: center;
  filter: blur(50px) brightness(0.3);
  z-index: -1;
  opacity: 0.5;
}

.close-button {
  background: rgba(255, 255, 255, 0.1) !important;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
  color: rgba(255, 255, 255, 0.9) !important;
}

.close-button:hover {
  background: rgba(255, 255, 255, 0.2) !important;
}

.fullscreen-content {
  display: flex;
  align-items: center;
  justify-content: center;
  max-width: 1200px;
  width: 100%;
  padding: 2rem;
  gap: 4rem;
}

.album-section {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
}

.album-art-container {
  position: relative;
  width: 400px;
  height: 400px;
  max-width: 50vw;
  max-height: 50vw;
  aspect-ratio: 1;
}

.album-art {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.4);
  transition: transform 0.3s ease;
}

.album-art:hover {
  transform: scale(1.02);
}

.album-art-placeholder {
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10px);
}

.album-art-placeholder i {
  font-size: 4rem;
  color: rgba(255, 255, 255, 0.5);
}

.track-info-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2rem;
  color: white;
  text-align: center;
}

.track-info {
  margin-bottom: 1rem;
}

.track-title {
  font-size: 3rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
  line-height: 1.2;
}

.track-artist {
  font-size: 1.5rem;
  font-weight: 500;
  margin-bottom: 0.25rem;
  opacity: 0.9;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
}

.track-album {
  font-size: 1rem;
  opacity: 0.7;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
}

.progress-section {
  margin: 2rem 0;
}

.progress-times {
  display: flex;
  justify-content: space-between;
  font-size: 0.9rem;
  margin-bottom: 0.5rem;
  opacity: 0.8;
}

.progress-bar-container {
  width: 100%;
  height: 6px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 3px;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  border-radius: 3px;
  transition: all 0.3s ease;
}

.controls-section {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  margin: 2rem 0;
}

.control-button {
  width: 60px !important;
  height: 60px !important;
  background: rgba(255, 255, 255, 0.1) !important;
  backdrop-filter: blur(10px);
  border: 2px solid rgba(255, 255, 255, 0.2) !important;
  color: white !important;
  transition: all 0.3s ease;
}

.control-button:hover {
  background: rgba(255, 255, 255, 0.2) !important;
  transform: scale(1.05);
}

.play-button {
  width: 80px !important;
  height: 80px !important;
  font-size: 1.5rem !important;
}

.volume-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
}

.volume-controls {
  display: flex;
  align-items: center;
  gap: 1rem;
  width: 300px;
}

.volume-icon {
  color: rgba(255, 255, 255, 0.7);
  font-size: 1.2rem;
}

.volume-slider {
  flex: 1;
}

.volume-slider :deep(.p-slider) {
  background: rgba(255, 255, 255, 0.2);
}

.volume-slider :deep(.p-slider-range) {
  background: var(--accent-color, #1DB954);
}

.volume-slider :deep(.p-slider-handle) {
  background: var(--accent-color, #1DB954);
  border: 2px solid var(--accent-color, #1DB954);
}

.volume-label {
  font-size: 0.9rem;
  opacity: 0.8;
}

/* Debug colors display */
.debug-colors {
  position: absolute;
  top: 60px;
  right: 20px;
  display: flex;
  gap: 5px;
  z-index: 20;
}

.debug-color {
  width: 30px;
  height: 30px;
  border-radius: 4px;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

/* Responsive Design */
@media (max-width: 768px) {
  .fullscreen-content {
    flex-direction: column;
    gap: 2rem;
    padding: 1rem;
  }

  .album-art-container {
    width: 250px;
    height: 250px;
  }

  .track-title {
    font-size: 2rem;
  }

  .track-artist {
    font-size: 1.2rem;
  }

  .volume-controls {
    width: 250px;
  }
}

@media (max-width: 480px) {
  .album-art-container {
    width: 200px;
    height: 200px;
  }

  .track-title {
    font-size: 1.5rem;
  }

  .track-artist {
    font-size: 1rem;
  }

  .control-button {
    width: 50px !important;
    height: 50px !important;
  }

  .play-button {
    width: 65px !important;
    height: 65px !important;
  }
}
</style>