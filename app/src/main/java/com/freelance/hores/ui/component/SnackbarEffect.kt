package com.freelance.hores.ui.component

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import kotlinx.coroutines.launch

@Composable
fun ErrorSnackbarEffect(
    errorMessage: String?,
    snackbarHostState: SnackbarHostState,
    onErrorDismissed: () -> Unit = {}
) {
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(
                message = errorMessage,
                duration = SnackbarDuration.Short
            )
            onErrorDismissed()
        }
    }
}

@Composable
fun SuccessSnackbarEffect(
    isSuccess: Boolean,
    snackbarHostState: SnackbarHostState,
    message: String = "Saved successfully",
    onDismissed: () -> Unit = {}
) {
    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            val result = snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.Dismissed) {
                onDismissed()
            }
        }
    }
}
