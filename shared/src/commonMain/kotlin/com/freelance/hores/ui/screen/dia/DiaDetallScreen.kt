package com.freelance.hores.ui.screen.dia

import com.freelance.hores.data.db.entity.EstatFacturacio
import com.freelance.hores.ui.component.ConcepteCard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import org.koin.compose.koinInject
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaDetallScreen(
    navController: NavHostController,
    diaId: Long,
    viewModel: DiaDetallViewModel = koinInject()
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
                title = { Text("Detall del dia") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Cancel·la")
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
                            text = localDia.data.toString(),
                            style = MaterialTheme.typography.headlineSmall
                        )
                        if (localDia.notes.isNotEmpty()) {
                            Text(
                                text = "${"Valor"}: ${localDia.notes}",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }

                item {
                    Column {
                        Text(
                            text = "Total hores: ${String.format("%.2f", localDia.getTotalHoras())}",
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
                            label = { Text(concepte.estat.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = when (concepte.estat) {
                                    EstatFacturacio.PENDENT -> MaterialTheme.colorScheme.surfaceVariant
                                    EstatFacturacio.FACTURAT -> MaterialTheme.colorScheme.tertiaryContainer
                                    EstatFacturacio.COBRAT -> MaterialTheme.colorScheme.primaryContainer
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
                            Text("Valor")
                        }
                        Button(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Text("Valor")
                        }
                    }
                }
            }

            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Valor") },
                    text = { Text("Valor") },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.deleteDia()
                                showDeleteDialog = false
                                navController.popBackStack()
                            }
                        ) {
                            Text("Eliminar")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showDeleteDialog = false }) {
                            Text("Cancel·la")
                        }
                    }
                )
            }
        }
    }
}
