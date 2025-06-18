import Aura from '@primeuix/themes/aura';

export default defineNuxtConfig({
  devtools: { enabled: true },

  components: [{
    path: '~/components',
    pathPrefix: false,
  }],

  modules: [
    '@primevue/nuxt-module',
    '@pinia/nuxt',
    '@vueuse/nuxt',
    '@nuxtjs/tailwindcss'
  ],

  plugins: [
    '~/plugins/primevue.client.ts'
  ],

  primevue: {
    options: {
      theme: {
        preset: Aura,
        options: {
          darkModeSelector: '.dark'
        },
        cssLayer: {
          name: 'primevue',
          order: 'theme, base, primevue'
        }
      },
      ripple: true
    }
  },

  tailwindcss: {
    config: {
      content: [
        './components/**/*.{js,vue,ts}',
        './layouts/**/*.vue',
        './pages/**/*.vue',
        './plugins/**/*.{js,ts}',
        './app.vue',
        './error.vue'
      ],
      darkMode: 'class',
      theme: {
        extend: {
          spacing: {
            '15': '3.75rem',
          },
          gridTemplateColumns: {
            'timesheet': '80px repeat(4, 1fr)',
          }
        }
      },
      plugins: [
        require('tailwindcss-primeui')
      ]
    }
  },

  css: [
    'primeicons/primeicons.css',
    '~/assets/css/main.css'
  ],

  runtimeConfig: {
    public: {
      apiBase: process.env.NUXT_PUBLIC_API_BASE || 'http://localhost:8080/api',
      appMode: process.env.NUXT_PUBLIC_APP_MODE || 'local',
      spotifyClientId: process.env.NUXT_PUBLIC_SPOTIFY_CLIENT_ID || '',
      spotifyRedirectUri: process.env.NUXT_PUBLIC_SPOTIFY_REDIRECT_URI || 'http://127.0.0.1:3000/auth/spotify/callback'
    }
  },

  typescript: {
    strict: true,
    typeCheck: true
  },

  app: {
    head: {
      title: 'Project Cronos',
      meta: [
        { charset: 'utf-8' },
        { name: 'viewport', content: 'width=device-width, initial-scale=1' },
        { name: 'description', content: 'Self-hostable time tracking application with intuitive drag-and-paint interface' }
      ],
      link: [
        { rel: 'icon', type: 'image/x-icon', href: '/favicon.ico' }
      ]
    }
  },

  nitro: {
    esbuild: {
      options: {
        target: 'es2020'
      }
    }
  },

  // Development server configuration
  devServer: {
    host: '127.0.0.1',
    port: 3000
  }
})