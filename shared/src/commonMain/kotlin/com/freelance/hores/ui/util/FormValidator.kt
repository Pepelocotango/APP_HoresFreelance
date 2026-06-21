package com.freelance.hores.ui.util

import kotlinx.datetime.LocalTime

object FormValidator {

    fun validateConcepteName(name: String): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult.Error("El nom del bolo no pot estar buit")
            else -> ValidationResult.Success
        }
    }

    fun validateTimeRange(startTime: LocalTime, endTime: LocalTime): ValidationResult {
        return ValidationResult.Success
    }

    fun validateConceptesCount(count: Int): ValidationResult {
        return when {
            count == 0 -> ValidationResult.Error("El nom del bolo no pot estar buit")
            else -> ValidationResult.Success
        }
    }

    fun validateTimeRangesCount(count: Int): ValidationResult {
        return when {
            count == 0 -> ValidationResult.Error("Almenys un rang horari per bolo")
            else -> ValidationResult.Success
        }
    }

    fun roundToNearest15Minutes(time: LocalTime): LocalTime {
        val minutes = time.minute
        val roundedMinutes = ((minutes + 7) / 15) * 15
        return if (roundedMinutes >= 60) {
            LocalTime(time.hour + 1, 0)
        } else {
            LocalTime(time.hour, roundedMinutes)
        }
    }
}

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}
