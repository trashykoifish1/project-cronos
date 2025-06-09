import { format, parseISO } from 'date-fns'

export const useTime = () => {

    // Format minutes to human readable time (e.g., 90 -> "1h 30m")
    const formatDuration = (minutes: number): string => {
        if (minutes < 60) {
            return `${minutes}m`
        }

        const hours = Math.floor(minutes / 60)
        const remainingMinutes = minutes % 60

        if (remainingMinutes === 0) {
            return `${hours}h`
        }

        return `${hours}h ${remainingMinutes}m`
    }

    // Convert HH:mm format to minutes since midnight
    const timeToMinutes = (timeString: string): number => {
        const [hours, minutes] = timeString.split(':').map(Number)
        return hours * 60 + minutes
    }

    // Convert minutes since midnight to HH:mm format
    const minutesToTime = (minutes: number): string => {
        const hours = Math.floor(minutes / 60)
        const mins = minutes % 60
        return `${hours.toString().padStart(2, '0')}:${mins.toString().padStart(2, '0')}`
    }

    // Calculate duration between two times in HH:mm format
    const calculateDuration = (startTime: string, endTime: string): number => {
        const start = timeToMinutes(startTime)
        const end = timeToMinutes(endTime)

        // Handle overnight periods
        if (end < start) {
            return (24 * 60) - start + end
        }

        return end - start
    }

    // Generate time slots for a day (e.g., every 15 minutes)
    const generateTimeSlots = (intervalMinutes: number = 15): string[] => {
        const slots: string[] = []
        for (let minutes = 0; minutes < 24 * 60; minutes += intervalMinutes) {
            slots.push(minutesToTime(minutes))
        }
        return slots
    }

    // Check if a time string is valid HH:mm format
    const isValidTimeFormat = (timeString: string): boolean => {
        const timeRegex = /^([01]?[0-9]|2[0-3]):[0-5][0-9]$/
        return timeRegex.test(timeString)
    }

    // Format ISO date string for display
    const formatDate = (dateString: string, formatString: string = 'MMM dd, yyyy'): string => {
        try {
            return format(parseISO(dateString), formatString)
        } catch (error) {
            return dateString
        }
    }

    // Format ISO datetime string for display
    const formatDateTime = (dateString: string, formatString: string = 'MMM dd, yyyy HH:mm'): string => {
        try {
            return format(parseISO(dateString), formatString)
        } catch (error) {
            return dateString
        }
    }

    // Get current time in HH:mm format
    const getCurrentTime = (): string => {
        const now = new Date()
        return `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`
    }

    // Get current date in YYYY-MM-DD format
    const getCurrentDate = (): string => {
        const now = new Date()
        return now.toISOString().split('T')[0]
    }

    // Round time to nearest interval (e.g., round to nearest 15 minutes)
    const roundTimeToInterval = (timeString: string, intervalMinutes: number = 15): string => {
        const minutes = timeToMinutes(timeString)
        const rounded = Math.round(minutes / intervalMinutes) * intervalMinutes
        return minutesToTime(rounded % (24 * 60)) // Handle 24:00 case
    }

    return {
        formatDuration,
        timeToMinutes,
        minutesToTime,
        calculateDuration,
        generateTimeSlots,
        isValidTimeFormat,
        formatDate,
        formatDateTime,
        getCurrentTime,
        getCurrentDate,
        roundTimeToInterval
    }
}