import { defineStore } from 'pinia'

export const useThemeStore = defineStore('theme', {
    state: () => ({
        isDark: false as boolean,
    }),

    getters: {
        currentTheme: (state) => state.isDark ? 'dark' : 'light',
    },

    actions: {
        toggleTheme() {
            this.isDark = !this.isDark
            this.applyTheme()
            this.saveToStorage()
        },

        setTheme(isDark: boolean) {
            this.isDark = isDark
            this.applyTheme()
            this.saveToStorage()
        },

        applyTheme() {
            if (import.meta.client) {
                const html = document.documentElement
                if (this.isDark) {
                    html.classList.add('dark')
                } else {
                    html.classList.remove('dark')
                }
            }
        },

        loadFromStorage() {
            if (import.meta.client) {
                const saved = localStorage.getItem('theme-preference')
                if (saved) {
                    this.isDark = saved === 'dark'
                } else {
                    // Check system preference
                    this.isDark = window.matchMedia('(prefers-color-scheme: dark)').matches
                }
                this.applyTheme()
            }
        },

        saveToStorage() {
            if (import.meta.client) {
                localStorage.setItem('theme-preference', this.currentTheme)
            }
        },

        initializeTheme() {
            this.loadFromStorage()

            // Listen for system theme changes
            if (import.meta.client) {
                window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', (e) => {
                    const saved = localStorage.getItem('theme-preference')
                    if (!saved) {
                        this.setTheme(e.matches)
                    }
                })
            }
        }
    }
})