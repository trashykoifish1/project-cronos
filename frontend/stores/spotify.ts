import { defineStore } from 'pinia'

interface SpotifyDevice {
    id: string
    name: string
    type: string
    volume_percent: number
    is_active: boolean
}

interface SpotifyTrack {
    id: string
    name: string
    artists: Array<{ name: string }>
    album: {
        name: string
        images: Array<{ url: string; width: number; height: number }>
    }
    duration_ms: number
}

interface SpotifyPlaybackState {
    is_playing: boolean
    progress_ms: number
    item: SpotifyTrack | null
    device: SpotifyDevice | null
    shuffle_state: boolean
    repeat_state: 'off' | 'track' | 'context'
}

export const useSpotifyStore = defineStore('spotify', {
    state: () => ({
        // Authentication
        isConnected: false,
        accessToken: null as string | null,
        refreshToken: null as string | null,
        tokenExpiresAt: null as number | null,

        // Playback state
        currentTrack: null as SpotifyTrack | null,
        isPlaying: false,
        progress: 0,
        duration: 0,
        volume: 50,
        devices: [] as SpotifyDevice[],
        activeDevice: null as SpotifyDevice | null,

        // UI state
        isInitializing: false,
        error: null as string | null,
        lastUpdated: null as Date | null,
    }),

    getters: {
        isAuthenticated: (state) => state.isConnected && state.accessToken !== null,

        hasActiveTrack: (state) => state.currentTrack !== null,

        currentArtists: (state) =>
            state.currentTrack?.artists.map(artist => artist.name).join(', ') || '',

        currentAlbumArt: (state) =>
            state.currentTrack?.album.images?.[0]?.url || null,

        progressPercentage: (state) =>
            state.duration > 0 ? (state.progress / state.duration) * 100 : 0,

        needsTokenRefresh: (state) =>
            state.tokenExpiresAt ? Date.now() >= state.tokenExpiresAt - 60000 : false, // 1 minute buffer
    },

    actions: {
        // Authentication
        setTokens(accessToken: string, refreshToken: string, expiresIn: number) {
            this.accessToken = accessToken
            this.refreshToken = refreshToken // Will be empty for implicit grant
            this.tokenExpiresAt = Date.now() + (expiresIn * 1000)
            this.isConnected = true
            this.error = null
            this.saveToStorage()
        },

        clearTokens() {
            this.accessToken = null
            this.refreshToken = null
            this.tokenExpiresAt = null
            this.isConnected = false
            this.currentTrack = null
            this.isPlaying = false
            this.devices = []
            this.activeDevice = null
            this.clearStorage()
        },

        // Playback state updates
        updatePlaybackState(state: Partial<SpotifyPlaybackState>) {
            if (state.item) {
                this.currentTrack = state.item
                this.duration = state.item.duration_ms
            }

            if (typeof state.is_playing === 'boolean') {
                this.isPlaying = state.is_playing
            }

            if (typeof state.progress_ms === 'number') {
                this.progress = state.progress_ms
            }

            if (state.device) {
                this.activeDevice = state.device
                this.volume = state.device.volume_percent || this.volume
            }

            this.lastUpdated = new Date()
            this.error = null
        },

        updateDevices(devices: SpotifyDevice[]) {
            this.devices = devices
            this.activeDevice = devices.find(device => device.is_active) || null
        },

        setVolume(volume: number) {
            this.volume = Math.max(0, Math.min(100, volume))
        },

        setError(error: string) {
            this.error = error
            console.error('Spotify error:', error)
        },

        clearError() {
            this.error = null
        },

        // Storage management
        saveToStorage() {
            if (import.meta.client) {
                const data = {
                    accessToken: this.accessToken,
                    refreshToken: this.refreshToken,
                    tokenExpiresAt: this.tokenExpiresAt,
                    isConnected: this.isConnected,
                }
                localStorage.setItem('spotify-auth', JSON.stringify(data))
            }
        },

        loadFromStorage() {
            if (import.meta.client) {
                const stored = localStorage.getItem('spotify-auth')
                if (stored) {
                    try {
                        const data = JSON.parse(stored)
                        this.accessToken = data.accessToken
                        this.refreshToken = data.refreshToken
                        this.tokenExpiresAt = data.tokenExpiresAt
                        this.isConnected = data.isConnected

                        // Check if token is expired
                        if (this.needsTokenRefresh) {
                            this.clearTokens()
                        }
                    } catch (error) {
                        console.error('Error loading Spotify auth from storage:', error)
                        this.clearStorage()
                    }
                }
            }
        },

        clearStorage() {
            if (import.meta.client) {
                localStorage.removeItem('spotify-auth')
            }
        },

        // Initialize store
        initialize() {
            this.loadFromStorage()
        },
    }
})