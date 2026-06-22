package com.freelance.hores.ui.screen.clients

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientsScreen(
    navController: NavHostController,
    viewModel: ClientsViewModel = hiltViewModel()
) {
    val clients by viewModel.clients.collectAsState()
    var showNewClientDialog by remember { mutableStateOf(false) }

    if (showNewClientDialog) {
        var nom by remember { mutableStateOf("") }
        var preu by remember { mutableDoubleStateOf(0.0) }
        AlertDialog(
            onDismissRequest = { showNewClientDialog = false },
            title = { Text("Nou Client") },
            text = {
                Column {
                    OutlinedTextField(value = nom, onValueChange = { nom = it }, label = { Text("Nom del Client") })
                    OutlinedTextField(
                        value = if (preu > 0) preu.toString() else "",
                        onValueChange = { input ->
                            val normalized = input.replace(',', '.')
                            preu = normalized.toDoubleOrNull() ?: 0.0
                        },
                        label = { Text("Preu per defecte") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.saveClient(com.freelance.hores.domain.model.Client(id = "", nom = nom, preuHoraDefecte = preu))
                    showNewClientDialog = false
                }) { Text("Desa") }
            }
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Clients") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showNewClientDialog = true }) {
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
                        Column {
                            Text(text = client.nom, style = MaterialTheme.typography.titleMedium)
                            Text(text = "${client.preuHoraDefecte} €/h", style = MaterialTheme.typography.bodyMedium)
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
