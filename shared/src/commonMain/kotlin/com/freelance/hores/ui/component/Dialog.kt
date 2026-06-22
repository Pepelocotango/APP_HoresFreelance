package com.freelance.hores.ui.component


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String = "Valor",
    dismissText: String = "Cancel·la",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    showDialog: Boolean
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(title) },
            text = { Text(message) },
            confirmButton = {
                Button(onClick = {
                    onConfirm()
                    onDismiss()
                }) {
                    Text(confirmText)
                }
            },
            dismissButton = {
                Button(onClick = onDismiss) {
                    Text(dismissText)
                }
            }
        )
    }
}

@Composable
fun ErrorDialog(
    title: String = "Valor",
    message: String,
    onDismiss: () -> Unit,
    showDialog: Boolean
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(title) },
            text = { Text(message) },
            confirmButton = {
                Button(onClick = onDismiss) {
                    Text("Valor")
                }
            }
        )
    }
}
