package com.freelance.hores.ui.screen.clients


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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.text.input.KeyboardType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientsScreen(
    navController: NavHostController,
    viewModel: ClientsViewModel = koinInject()
) {
    val clients by viewModel.clients.collectAsState()
    var showClientDialog by remember { mutableStateOf(false) }
    var editingClient by remember { mutableStateOf<com.freelance.hores.domain.model.Client?>(null) }

    if (showClientDialog) {
        var nom by remember { mutableStateOf(editingClient?.nom ?: "") }
        var preuInput by remember { mutableStateOf(editingClient?.preuHoraDefecte?.toString() ?: "") }

        AlertDialog(
            onDismissRequest = {
                showClientDialog = false
                editingClient = null
            },
            title = { Text(if (editingClient == null) "Nou Client" else "Editar Client") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = nom,
                        onValueChange = { nom = it },
                        label = { Text("Nom del Client") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = preuInput,
                        onValueChange = { preuInput = it },
                        label = { Text("Preu per defecte (€/h)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val preu = preuInput.replace(',', '.').toDoubleOrNull() ?: 0.0
                    viewModel.saveClient(
                        com.freelance.hores.domain.model.Client(
                            id = editingClient?.id ?: 0L,
                            nom = nom,
                            preuHoraDefecte = preu
                        )
                    )
                    showClientDialog = false
                    editingClient = null
                }) { Text("Desa") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showClientDialog = false
                    editingClient = null
                }) { Text("Cancel·la") }
            }
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Clients") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editingClient = null
                showClientDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Afegir client")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(clients) { client ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = client.nom, style = MaterialTheme.typography.titleMedium)
                            Text(text = "${client.preuHoraDefecte} €/h", style = MaterialTheme.typography.bodyMedium)
                        }
                        Row {
                            IconButton(onClick = {
                                editingClient = client
                                showClientDialog = true
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar")
                            }
                            IconButton(onClick = { viewModel.deleteClient(client) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                            }
                        }
                    }
                }
            }
        }
    }
}
