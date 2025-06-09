import { defineStore } from 'pinia'
import { format } from 'date-fns'
import type { DailySummary, WeeklySummary } from '~/types/api'
import {useReportsApi} from "~/composables/useApi";

export const useReportsStore = defineStore('reports', {
    state: () => ({
        // Daily Reports
        dailyReports: {} as Record<string, DailySummary>, // date -> DailySummary
        currentDailyReport: null as DailySummary | null,

        // Weekly Reports
        weeklyReports: {} as Record<string, WeeklySummary>, // week key -> WeeklySummary
        currentWeeklyReport: null as WeeklySummary | null,

        // Statistics
        statistics: null as any,

        // Loading states
        loadingDaily: false,
        loadingWeekly: false,
        loadingStatistics: false,

        // Errors
        error: null as string | null
    }),

    getters: {
        // Get daily report by date
        getDailyReportByDate: (state) => (date: string) =>
            state.dailyReports[date] || null,

        // Check if daily report exists
        hasDailyReport: (state) => (date: string) =>
            !!state.dailyReports[date],

        // Get current daily statistics
        currentDailyStats: (state) => {
            if (!state.currentDailyReport) return null

            return {
                totalTime: state.currentDailyReport.totalTimeFormatted,
                totalEntries: state.currentDailyReport.totalEntries,
                categoriesUsed: state.currentDailyReport.categoryBreakdowns.length,
                tasksUsed: state.currentDailyReport.taskBreakdowns.length,
                hasWarnings: state.currentDailyReport.warnings.length > 0
            }
        },

        // Get productivity percentage (8 hour day)
        productivityPercentage: (state) => {
            if (!state.currentDailyReport) return 0
            const workingHours = 8
            const totalWorkMinutes = workingHours * 60
            return Math.min(
                Math.round((state.currentDailyReport.totalMinutes / totalWorkMinutes) * 100),
                100
            )
        }
    },

    actions: {
        // Fetch daily report
        async fetchDailyReport(date: string) {
            // Return cached if available
            if (this.dailyReports[date]) {
                this.currentDailyReport = this.dailyReports[date]
                return this.currentDailyReport
            }

            this.loadingDaily = true
            this.error = null

            try {
                const { getDailySummary } = useReportsApi()
                const report = await getDailySummary(date)

                // Cache the report
                this.dailyReports[date] = report
                this.currentDailyReport = report

                return report
            } catch (error: any) {
                this.error = error.message || 'Failed to fetch daily report'
                console.error('Error fetching daily report:', error)
                throw error
            } finally {
                this.loadingDaily = false
            }
        },

        // Fetch weekly report
        async fetchWeeklyReport(date: string) {
            const weekKey = this.getWeekKey(date)

            // Return cached if available
            if (this.weeklyReports[weekKey]) {
                this.currentWeeklyReport = this.weeklyReports[weekKey]
                return this.currentWeeklyReport
            }

            this.loadingWeekly = true
            this.error = null

            try {
                const { getWeeklySummary } = useReportsApi()
                const report = await getWeeklySummary(date)

                // Cache the report
                this.weeklyReports[weekKey] = report
                this.currentWeeklyReport = report

                return report
            } catch (error: any) {
                this.error = error.message || 'Failed to fetch weekly report'
                console.error('Error fetching weekly report:', error)
                throw error
            } finally {
                this.loadingWeekly = false
            }
        },

        // Fetch statistics for date range
        async fetchStatistics(startDate: string, endDate: string) {
            this.loadingStatistics = true
            this.error = null

            try {
                const { getStatistics } = useReportsApi()
                const stats = await getStatistics(startDate, endDate)

                this.statistics = stats
                return stats
            } catch (error: any) {
                this.error = error.message || 'Failed to fetch statistics'
                console.error('Error fetching statistics:', error)
                throw error
            } finally {
                this.loadingStatistics = false
            }
        },

        // Clear current daily report
        clearCurrentDaily() {
            this.currentDailyReport = null
        },

        // Clear current weekly report
        clearCurrentWeekly() {
            this.currentWeeklyReport = null
        },

        // Clear error
        clearError() {
            this.error = null
        },

        // Reset store
        reset() {
            this.dailyReports = {}
            this.weeklyReports = {}
            this.currentDailyReport = null
            this.currentWeeklyReport = null
            this.statistics = null
            this.loadingDaily = false
            this.loadingWeekly = false
            this.loadingStatistics = false
            this.error = null
        },

        // Helper method to generate week key
        getWeekKey(date: string): string {
            const dateObj = new Date(date)
            const year = dateObj.getFullYear()
            const weekOfYear = this.getWeekOfYear(dateObj)
            return `${year}-W${weekOfYear}`
        },

        // Helper to get week number
        getWeekOfYear(date: Date): number {
            const firstDayOfYear = new Date(date.getFullYear(), 0, 1)
            const pastDaysOfYear = (date.getTime() - firstDayOfYear.getTime()) / 86400000
            return Math.ceil((pastDaysOfYear + firstDayOfYear.getDay() + 1) / 7)
        },

        // Invalidate cache for specific date (useful after creating/updating time entries)
        invalidateDaily(date: string) {
            delete this.dailyReports[date]
            if (this.currentDailyReport?.date === date) {
                this.currentDailyReport = null
            }
        },

        // Invalidate weekly cache for date
        invalidateWeekly(date: string) {
            const weekKey = this.getWeekKey(date)
            delete this.weeklyReports[weekKey]
            if (this.currentWeeklyReport && this.getWeekKey(this.currentWeeklyReport.weekStartDate) === weekKey) {
                this.currentWeeklyReport = null
            }
        },

        // Invalidate multiple dates at once (useful for bulk operations)
        invalidateDateRange(startDate: string, endDate: string) {
            const start = new Date(startDate)
            const end = new Date(endDate)

            // Invalidate each day in the range
            for (let date = new Date(start); date <= end; date.setDate(date.getDate() + 1)) {
                const dateString = format(date, 'yyyy-MM-dd')
                this.invalidateDaily(dateString)
                this.invalidateWeekly(dateString)
            }
        },

        // Invalidate current displayed report
        invalidateCurrent() {
            if (this.currentDailyReport) {
                this.invalidateDaily(this.currentDailyReport.date)
            }
            if (this.currentWeeklyReport) {
                this.invalidateWeekly(this.currentWeeklyReport.weekStartDate)
            }
        },

        // Get report summary for quick stats
        getQuickStats(date: string) {
            const report = this.getDailyReportByDate(date)
            if (!report) return null

            return {
                totalTime: report.totalTimeFormatted,
                totalEntries: report.totalEntries,
                topTask: report.taskBreakdowns[0]?.taskTitle || 'None',
                productivity: this.productivityPercentage
            }
        }
    }
})