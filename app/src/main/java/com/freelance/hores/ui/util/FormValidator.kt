package com.freelance.hores.ui.util

import java.time.LocalTime

import com.freelance.hores.R

object FormValidator {
    
    fun validateConcepteName(name: String): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult.Error(R.string.registre_error_concepte_empty)
            else -> ValidationResult.Success
        }
    }

    fun validateTimeRange(startTime: LocalTime, endTime: LocalTime): ValidationResult {
        return when {
            endTime <= startTime -> ValidationResult.Error(R.string.registre_error_hora_invalid)
            else -> ValidationResult.Success
        }
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
}

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val resId: Int) : ValidationResult()
}
