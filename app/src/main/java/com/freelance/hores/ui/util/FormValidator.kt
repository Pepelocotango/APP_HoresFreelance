package com.freelance.hores.ui.util

import java.time.LocalTime

object FormValidator {
    
    fun validateConcepteName(name: String): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult.Error("Concept name cannot be empty")
            name.length < 2 -> ValidationResult.Error("Concept name must be at least 2 characters")
            name.length > 100 -> ValidationResult.Error("Concept name is too long")
            else -> ValidationResult.Success
        }
    }

    fun validateTimeRange(startTime: LocalTime, endTime: LocalTime): ValidationResult {
        return when {
            endTime <= startTime -> ValidationResult.Error("End time must be after start time")
            else -> ValidationResult.Success
        }
    }

    fun validateNotes(notes: String): ValidationResult {
        return when {
            notes.length > 500 -> ValidationResult.Error("Notes are too long (max 500 characters)")
            else -> ValidationResult.Success
        }
    }

    fun validateConceptesCount(count: Int): ValidationResult {
        return when {
            count == 0 -> ValidationResult.Error("At least one concept is required")
            count > 50 -> ValidationResult.Error("Too many concepts (max 50)")
            else -> ValidationResult.Success
        }
    }

    fun validateTimeRangesCount(count: Int): ValidationResult {
        return when {
            count == 0 -> ValidationResult.Error("Each concept needs at least one time range")
            count > 10 -> ValidationResult.Error("Too many time ranges (max 10 per concept)")
            else -> ValidationResult.Success
        }
    }
}

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}
