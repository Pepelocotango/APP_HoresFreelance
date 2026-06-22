package com.freelance.hores.ui.screen.dia

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.freelance.hores.R
import com.freelance.hores.ui.component.ConcepteCard
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaDetallScreen(
    navController: NavHostController,
    diaId: String,
    viewModel: DiaDetallViewModel = hiltViewModel()
) {
    val dia by viewModel.dia.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(diaId) {
        viewModel.loadDia(diaId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.dia_detall_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_cancel))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        val localDia = dia // Assignació segura
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (localDia != null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Column {
                        Text(
                            text = LocalDate.parse(localDia.data).format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.getDefault())),
                            style = MaterialTheme.typography.headlineSmall
                        )
                        if (localDia.notes.isNotEmpty()) {
                            Text(
                                text = "${stringResource(R.string.dia_detall_notes)}: ${localDia.notes}",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }

                item {
                    Column {
                        Text(
                            text = stringResource(R.string.dia_detall_total_hours, String.format("%.2f", localDia.getTotalHoras())),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Total Hores: ${String.format("%.2f", localDia.getTotalDinersHores())} € | Despeses: ${String.format("%.2f", localDia.getTotalDinersDespeses())} €",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                items(localDia.conceptes, key = { it.id }) { concepte ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        ConcepteCard(
                            concepte = concepte,
                            onEdit = { navController.navigate("registre?diaId=${localDia.id}") },
                            onDelete = { viewModel.deleteConcepte(concepte) }
                        )
                        FilterChip(
                            selected = true,
                            onClick = {},
                            label = { Text(concepte.estat) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = when (concepte.estat) {
                                    "PENDENT" -> MaterialTheme.colorScheme.surfaceVariant
                                    "FACTURAT" -> MaterialTheme.colorScheme.tertiaryContainer
                                    "COBRAT" -> MaterialTheme.colorScheme.primaryContainer
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                        )
                        if (concepte.despeses > 0) {
                            Text(
                                text = "Despeses (${String.format("%.2f", concepte.despeses)} €): ${concepte.despesesNotes}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { navController.navigate("registre?diaId=${localDia.id}") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                            Text(stringResource(R.string.dia_detall_edit))
                        }
                        Button(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Text(stringResource(R.string.dia_detall_delete))
                        }
                    }
                }
            }

            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text(stringResource(R.string.common_confirm)) },
                    text = { Text(stringResource(R.string.common_confirm_delete)) },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.deleteDia()
                                showDeleteDialog = false
                                navController.popBackStack()
                            }
                        ) {
                            Text(stringResource(R.string.common_delete))
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showDeleteDialog = false }) {
                            Text(stringResource(R.string.common_cancel))
                        }
                    }
                )
            }
        }
    }
}
