import {useSpotifyApi} from "~/composables/useApi";


interface SpotifyPlaybackState {
    is_playing: boolean
    progress_ms: number
    item: SpotifyTrack | null
    device: SpotifyDevice | null
    shuffle_state: boolean
    repeat_state: 'off' | 'track' | 'context'
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

interface SpotifyDevice {
    id: string
    name: string
    type: string
    volume_percent: number
    is_active: boolean
}

export const useSpotify = () => {
    const spotifyStore = useSpotifyStore()
    const {getSpotifyToken, getSpotifyRefreshToken} = useSpotifyApi()
    const toast = useToast()
    const { config } = useSpotifyConfig()

    // Authentication using Authorization Code Flow
    const initiateAuth = () => {
        if (!import.meta.client || !window?.location) {
            console.warn('Auth can only be initiated on client side')
            return
        }

        if (!config.clientId) {
            console.error('No Spotify Client ID configured!')
            spotifyStore.setError('Spotify Client ID not configured')
            return
        }

        const state = generateRandomString(16)
        console.log('Generated auth state:', state)

        const authUrl = new URL('https://accounts.spotify.com/authorize')
        authUrl.searchParams.append('client_id', config.clientId)
        authUrl.searchParams.append('response_type', 'code') // Changed from 'token' to 'code'
        authUrl.searchParams.append('redirect_uri', config.redirectUri)
        authUrl.searchParams.append('scope', config.scopes.join(' '))
        authUrl.searchParams.append('state', state)
        authUrl.searchParams.append('show_dialog', 'true')

        console.log('Final auth URL:', authUrl.toString())

        // Store state for verification
        localStorage.setItem('spotify_auth_state', state)

        window.location.href = authUrl.toString()
    }

    const handleAuthCallback = async (code: string, state: string): Promise<{ success: boolean; error?: string }> => {
        try {
            spotifyStore.isInitializing = true

            // Verify state parameter
            const storedState = localStorage.getItem('spotify_auth_state')
            if (state !== storedState) {
                spotifyStore.setError('Authentication state mismatch')
                return { success: false, error: 'state_mismatch' }
            }

            // Exchange code for tokens via backend
            const response = await getSpotifyToken(code, config.redirectUri);

            spotifyStore.setTokens(
                response.access_token,
                response.refresh_token,
                response.expires_in
            )

            // Clean up
            localStorage.removeItem('spotify_auth_state')

            // Initialize playback state
            await getCurrentPlayback()

            return { success: true }

        } catch (error: any) {
            console.error('Auth callback error:', error)
            spotifyStore.setError('Failed to connect to Spotify')
            return { success: false, error: 'exchange_failed' }
        } finally {
            spotifyStore.isInitializing = false
        }
    }

    const disconnect = () => {
        spotifyStore.clearTokens()

        toast.add({
            severity: 'info',
            summary: 'Spotify Disconnected',
            detail: 'Your Spotify account has been disconnected',
            life: 3000
        })
    }

    // API calls with automatic token refresh
    const makeSpotifyRequest = async <T = any>(endpoint: string, options: RequestInit = {}): Promise<T> => {
        if (!spotifyStore.accessToken) {
            throw new Error('Not authenticated with Spotify')
        }

        // Check if token needs refresh
        if (spotifyStore.needsTokenRefresh && spotifyStore.refreshToken) {
            await refreshToken()
        }

        const response = await fetch(`https://api.spotify.com/v1${endpoint}`, {
            ...options,
            headers: {
                'Authorization': `Bearer ${spotifyStore.accessToken}`,
                'Content-Type': 'application/json',
                ...options.headers
            }
        })

        if (!response.ok) {
            if (response.status === 401) {
                // Try refreshing token once
                if (spotifyStore.refreshToken) {
                    try {
                        await refreshToken()
                        // Retry the request with new token
                        const retryResponse = await fetch(`https://api.spotify.com/v1${endpoint}`, {
                            ...options,
                            headers: {
                                'Authorization': `Bearer ${spotifyStore.accessToken}`,
                                'Content-Type': 'application/json',
                                ...options.headers
                            }
                        })

                        if (retryResponse.ok) {
                            return await parseResponse<T>(retryResponse)
                        }
                    } catch (refreshError) {
                        console.error('Token refresh failed:', refreshError)
                    }
                }

                // If refresh failed or no refresh token, clear tokens
                spotifyStore.clearTokens()
                throw new Error('Authentication expired. Please reconnect.')
            }

            if (response.status === 403) {
                throw new Error('Premium subscription required for this action')
            }

            if (response.status === 404) {
                throw new Error('No active device found. Please start Spotify on a device.')
            }

            throw new Error(`Spotify API error: ${response.status}`)
        }

        return await parseResponse<T>(response)
    }

// Helper function to safely parse response
    const parseResponse = async <T>(response: Response): Promise<T> => {
        // Handle empty responses (204 No Content or empty body)
        if (response.status === 204 || response.headers.get('content-length') === '0') {
            return {} as T
        }

        // Check if response has any content
        const contentLength = response.headers.get('content-length')
        if (contentLength === '0') {
            return {} as T
        }

        // Check content type to determine if it's JSON
        const contentType = response.headers.get('content-type')
        const isJson = contentType && contentType.includes('application/json')

        if (isJson) {
            try {
                return await response.json() as T
            } catch (error) {
                console.warn('Failed to parse JSON response:', error)
                return {} as T
            }
        } else {
            return {} as T
        }
    }

    const refreshToken = async () => {
        if (!spotifyStore.refreshToken) {
            throw new Error('No refresh token available')
        }

        try {
            const response = await getSpotifyRefreshToken(spotifyStore.refreshToken)

            spotifyStore.setTokens(
                response.access_token,
                spotifyStore.refreshToken, // Keep existing refresh token
                response.expires_in
            )
        } catch (error: any) {
            console.error('Token refresh failed:', error)
            spotifyStore.clearTokens()
            throw error
        }
    }

    // Playback controls (unchanged)
    const getCurrentPlayback = async () => {
        try {
            const data = await makeSpotifyRequest<SpotifyPlaybackState>('/me/player')
            if (data && Object.keys(data).length > 0) {
                spotifyStore.updatePlaybackState(data)
            } else {
                spotifyStore.currentTrack = null
                spotifyStore.isPlaying = false
            }
        } catch (error: any) {
            console.error('Error getting current playback:', error)
            if (!error.message.includes('expired')) {
                spotifyStore.setError('No active Spotify device found')
            }
        }
    }

    const play = async () => {
        try {
            await makeSpotifyRequest('/me/player/play', { method: 'PUT' })
            spotifyStore.isPlaying = true
            spotifyStore.clearError()
            setTimeout(() => getCurrentPlayback(), 500)
        } catch (error: any) {
            console.error('Error playing:', error)
            spotifyStore.setError(error.message || 'Failed to play track')
        }
    }

    const pause = async () => {
        try {
            await makeSpotifyRequest('/me/player/pause', { method: 'PUT' })
            spotifyStore.isPlaying = false
            spotifyStore.clearError()
            setTimeout(() => getCurrentPlayback(), 500)
        } catch (error: any) {
            console.error('Error pausing:', error)
            spotifyStore.setError(error.message || 'Failed to pause track')
        }
    }

    const nextTrack = async () => {
        try {
            await makeSpotifyRequest('/me/player/next', { method: 'POST' })
            spotifyStore.clearError()
            setTimeout(() => getCurrentPlayback(), 1000)
        } catch (error: any) {
            console.error('Error skipping to next track:', error)
            spotifyStore.setError(error.message || 'Failed to skip to next track')
        }
    }

    const previousTrack = async () => {
        try {
            await makeSpotifyRequest('/me/player/previous', { method: 'POST' })
            spotifyStore.clearError()
            setTimeout(() => getCurrentPlayback(), 1000)
        } catch (error: any) {
            console.error('Error skipping to previous track:', error)
            spotifyStore.setError(error.message || 'Failed to skip to previous track')
        }
    }

    const setVolume = async (volume: number) => {
        try {
            const clampedVolume = Math.max(0, Math.min(100, volume))
            await makeSpotifyRequest(`/me/player/volume?volume_percent=${clampedVolume}`, {
                method: 'PUT'
            })
            spotifyStore.setVolume(clampedVolume)
            spotifyStore.clearError()
        } catch (error: any) {
            console.error('Error setting volume:', error)
            spotifyStore.setError(error.message || 'Failed to set volume')
        }
    }

    return {
        // State
        isConnected: computed(() => spotifyStore.isConnected),
        isAuthenticated: computed(() => spotifyStore.isAuthenticated),
        currentTrack: computed(() => spotifyStore.currentTrack),
        isPlaying: computed(() => spotifyStore.isPlaying),
        progress: computed(() => spotifyStore.progress),
        duration: computed(() => spotifyStore.duration),
        volume: computed(() => spotifyStore.volume),
        isInitializing: computed(() => spotifyStore.isInitializing),
        error: computed(() => spotifyStore.error),
        hasActiveTrack: computed(() => spotifyStore.hasActiveTrack),
        currentArtists: computed(() => spotifyStore.currentArtists),
        currentAlbumArt: computed(() => spotifyStore.currentAlbumArt),
        progressPercentage: computed(() => spotifyStore.progressPercentage),

        // Actions
        initiateAuth,
        handleAuthCallback,
        disconnect,
        getCurrentPlayback,
        play,
        pause,
        nextTrack,
        previousTrack,
        setVolume,
        clearError: () => spotifyStore.clearError(),
        initialize: () => spotifyStore.initialize(),
    }
}

// Helper function
function generateRandomString(length: number): string {
    const possible = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'
    let text = ''
    for (let i = 0; i < length; i++) {
        text += possible.charAt(Math.floor(Math.random() * possible.length))
    }
    return text
}