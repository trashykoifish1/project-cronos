export const useTheme = () => {
    const themeStore = useThemeStore()

    return {
        isDark: computed(() => themeStore.isDark),
        currentTheme: computed(() => themeStore.currentTheme),
        toggleTheme: () => themeStore.toggleTheme(),
        setTheme: (isDark: boolean) => themeStore.setTheme(isDark),
        initializeTheme: () => themeStore.initializeTheme()
    }
}