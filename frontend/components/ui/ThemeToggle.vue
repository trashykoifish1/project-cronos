<template>
  <Button
      :icon="isDark ? 'pi pi-moon' : 'pi pi-sun'"
      :label="isMobile ? undefined : (isDark ? 'Dark' : 'Light')"
      :text="!variant || variant === 'text'"
      :outlined="variant === 'outlined'"
      :severity="variant === 'contrast' ? 'contrast' : 'secondary'"
      :size="size"
      @click="toggleTheme"
      :aria-label="`Switch to ${isDark ? 'light' : 'dark'} mode`"
      class="theme-toggle"
  />
</template>

<script setup lang="ts">
interface Props {
  variant?: 'text' | 'outlined' | 'contrast'
  size?: 'small' | 'large'
  isMobile?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'text',
  size: undefined,
  isMobile: false
})

const { isDark, toggleTheme } = useTheme()
</script>

<style scoped>
.theme-toggle {
  transition: all 0.2s ease-in-out;
}

@media (max-width: 768px) {
  .theme-toggle {
    min-width: 2.5rem;
  }
}
</style>