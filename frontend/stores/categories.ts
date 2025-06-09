import { defineStore } from 'pinia'
import type { Category, CategoryCreateRequest, CategoryUpdateRequest } from '~/types/api'

export const useCategoriesStore = defineStore('categories', {
    state: () => ({
        categories: [] as Category[],
        loading: false,
        error: null as string | null
    }),

    getters: {
        activeCategories: (state) =>
            state.categories.filter(cat => !cat.isArchived),

        defaultCategory: (state) =>
            state.categories.find(cat => cat.isDefault),

        getCategoryById: (state) => (id: number) =>
            state.categories.find(cat => cat.id === id) || null,
        getCategoriesWithTasks: (state) => {
            // This would require the tasks store, so we'll implement it as an action instead
            return state.categories.map(category => ({
                ...category,
                // These will be populated by the action
                tasks: []
            }))
        },

        categoriesSortedByOrder: (state) =>
            [...state.categories].sort((a, b) => a.sortOrder - b.sortOrder),
    },

    actions: {
        async fetchCategories() {
            if (this.loading) return

            this.loading = true
            this.error = null

            try {
                const { getCategories } = useCategoriesApi()
                this.categories = await getCategories()
            } catch (error: any) {
                this.error = error.message || 'Failed to fetch categories'
                console.error('Error fetching categories:', error)
            } finally {
                this.loading = false
            }
        },

        async createCategory(categoryData: CategoryCreateRequest) {
            try {
                const { createCategory } = useCategoriesApi()
                const newCategory = await createCategory(categoryData)
                this.categories.push(newCategory)
                return newCategory
            } catch (error: any) {
                this.error = error.message || 'Failed to create category'
                throw error
            }
        },

        async updateCategory(id: number, categoryData: CategoryUpdateRequest) {
            try {
                const { updateCategory } = useCategoriesApi()
                const updatedCategory = await updateCategory(id, categoryData)

                const index = this.categories.findIndex(cat => cat.id === id)
                if (index !== -1) {
                    this.categories[index] = updatedCategory
                }

                return updatedCategory
            } catch (error: any) {
                this.error = error.message || 'Failed to update category'
                throw error
            }
        },

        clearError() {
            this.error = null
        },
        async archiveCategory(id: number, archived: boolean) {
            try {
                // For now, we'll use the update endpoint with isArchived flag
                const updatedCategory = await this.updateCategory(id, {
                    title: this.getCategoryById(id)?.title || '',
                    isArchived: archived
                })
                return updatedCategory
            } catch (error: any) {
                this.error = error.message || 'Failed to archive category'
                throw error
            }
        },

// Get category statistics
        getCategoryStats() {
            const total = this.categories.length
            const active = this.activeCategories.length
            const archived = total - active
            const defaultCategory = this.defaultCategory

            return {
                total,
                active,
                archived,
                hasDefault: !!defaultCategory,
                defaultCategory: defaultCategory?.title || 'None'
            }
        },

// Validate category data
        validateCategory(categoryData: CategoryCreateRequest | CategoryUpdateRequest, excludeId?: number) {
            const errors: string[] = []

            // Check title uniqueness
            const existingCategory = this.categories.find(cat =>
                cat.title.toLowerCase() === categoryData.title.toLowerCase() &&
                cat.id !== excludeId
            )
            if (existingCategory) {
                errors.push('A category with this name already exists')
            }

            // Check if trying to set multiple defaults
            if (categoryData.isDefault) {
                const currentDefault = this.defaultCategory
                if (currentDefault && currentDefault.id !== excludeId) {
                    errors.push('Only one category can be set as default')
                }
            }

            return {
                isValid: errors.length === 0,
                errors
            }
        },
    }
})