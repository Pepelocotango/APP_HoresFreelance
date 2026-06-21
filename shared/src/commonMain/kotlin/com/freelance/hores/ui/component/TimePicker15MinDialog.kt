package com.freelance.hores.ui.component

import kotlinx.datetime.LocalTime

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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePicker15MinDialog(
    initialTime: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit
) {
    var selectedHour by remember { mutableStateOf(initialTime.hour) }
    var selectedMinute by remember { mutableStateOf(
        run {
            val currentMin = initialTime.minute
            val rounded = ((currentMin + 7) / 15) * 15
            if (rounded == 60) 0 else rounded
        }
    ) }

    var hourExpanded by remember { mutableStateOf(false) }
    var minuteExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Selecciona l'hora") },
        text = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Selector d'hores (00 a 23)
                ExposedDropdownMenuBox(
                    expanded = hourExpanded,
                    onExpandedChange = { hourExpanded = !hourExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = "%02d".format(selectedHour),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Hora") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = hourExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = hourExpanded,
                        onDismissRequest = { hourExpanded = false }
                    ) {
                        (0..23).forEach { hour ->
                            DropdownMenuItem(
                                text = { Text("%02d".format(hour)) },
                                onClick = {
                                    selectedHour = hour
                                    hourExpanded = false
                                }
                            )
                        }
                    }
                }

                Text(":", style = MaterialTheme.typography.headlineMedium)

                // Selector de minuts restrictiu de 15 en 15 (00, 15, 30, 45)
                ExposedDropdownMenuBox(
                    expanded = minuteExpanded,
                    onExpandedChange = { minuteExpanded = !minuteExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = "%02d".format(selectedMinute),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Minuts") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = minuteExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = minuteExpanded,
                        onDismissRequest = { minuteExpanded = false }
                    ) {
                        listOf(0, 15, 30, 45).forEach { minute ->
                            DropdownMenuItem(
                                text = { Text("%02d".format(minute)) },
                                onClick = {
                                    selectedMinute = minute
                                    minuteExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(LocalTime(selectedHour, selectedMinute))
                }
            ) {
                Text("Desa")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel·la")
            }
        }
    )
}
