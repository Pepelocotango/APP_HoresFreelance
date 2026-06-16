package com.freelance.hores.ui.util

/**
 * UI Event sealed class for handling one-time events like showing snackbars, navigation, etc.
 */
sealed class UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent()
    data class ShowError(val title: String, val message: String) : UiEvent()
    object NavigateBack : UiEvent()
    data class Navigate(val route: String) : UiEvent()
    object DismissDialog : UiEvent()
}
