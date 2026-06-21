package com.freelance.hores.ui.util

import java.time.LocalTime

object FormValidator {
    
    fun validateConcepteName(name: String): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult.Error(R.string.registre_error_concepte_empty)
            else -> ValidationResult.Success
        }
    }

    fun validateTimeRange(startTime: LocalTime, endTime: LocalTime): ValidationResult {
        // En cas de creuament de mitjanit, l'hora de fi és menor que la d'inici,
        // la qual cosa és vàlida en el context d'aquesta aplicació.
        return ValidationResult.Success
    }

    fun validateConceptesCount(count: Int): ValidationResult {
        return when {
            count == 0 -> ValidationResult.Error(R.string.registre_error_concepte_empty)
            else -> ValidationResult.Success
        }
    }

    fun validateTimeRangesCount(count: Int): ValidationResult {
        return when {
            count == 0 -> ValidationResult.Error(R.string.registre_error_no_rangs)
            else -> ValidationResult.Success
        }
    }

    /**
     * Arrodoneix un LocalTime al quart d'hora més proper (00, 15, 30, 45)
     */
    fun roundToNearest15Minutes(time: LocalTime): LocalTime {
        val minutes = time.minute
        val roundedMinutes = ((minutes + 7) / 15) * 15
        return if (roundedMinutes == 60) {
            time.plusHours(1).withMinute(0).withSecond(0).withNano(0)
        } else {
            time.withMinute(roundedMinutes).withSecond(0).withNano(0)
        }
    }
}

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val resId: Int) : ValidationResult()
}
