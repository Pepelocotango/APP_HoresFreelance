package com.freelance.hores.ui.screen.calendari

import androidx.compose.runtime.Composable

@Composable
expect fun CalendariBackupDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onImportComplete: () -> Unit
)
