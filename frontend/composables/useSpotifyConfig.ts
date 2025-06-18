export const useSpotifyConfig = () => {
    const runtimeConfig = useRuntimeConfig()

    const config = {
        clientId: runtimeConfig.public.spotifyClientId || '',
        redirectUri: runtimeConfig.public.spotifyRedirectUri || 'http://127.0.0.1:3000/auth/spotify/callback',
        scopes: [
            'user-read-playback-state',
            'user-modify-playback-state',
            'user-read-currently-playing',
            'streaming'
        ]
    }

    // Validate configuration
    const isConfigured = computed(() => {
        return config.clientId.length > 0
    })

    return {
        config,
        isConfigured
    }
}