package com.freelance.hores.ui.screen.clients

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.freelance.hores.R
import com.freelance.hores.domain.model.Client

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientsScreen(
    navController: NavHostController,
    viewModel: ClientsViewModel = hiltViewModel()
) {
    val clients by viewModel.clients.collectAsState()
    var showNewClientDialog by remember { mutableStateOf(false) }
    var clientToEdit by remember { mutableStateOf<Client?>(null) }

    // Diàleg per a NOU client
    if (showNewClientDialog) {
        var nom by remember { mutableStateOf("") }
        var preu by remember { mutableDoubleStateOf(0.0) }
        AlertDialog(
            onDismissRequest = { showNewClientDialog = false },
            title = { Text(stringResource(R.string.new_client)) },
            text = {
                Column {
                    OutlinedTextField(
                        value = nom, 
                        onValueChange = { nom = it }, 
                        label = { Text(stringResource(R.string.client_name)) }
                    )
                    OutlinedTextField(
                        value = if (preu > 0) preu.toString() else "",
                        onValueChange = { input ->
                            val normalized = input.replace(',', '.')
                            preu = normalized.toDoubleOrNull() ?: 0.0
                        },
                        label = { Text(stringResource(R.string.default_price)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.saveClient(Client(id = "", nom = nom, preuHoraDefecte = preu))
                    showNewClientDialog = false
                }) { Text(stringResource(R.string.common_save)) }
            }
        )
    }

    // Diàleg per a EDITAR client
    clientToEdit?.let { client ->
        var nom by remember { mutableStateOf(client.nom) }
        var preu by remember { mutableDoubleStateOf(client.preuHoraDefecte) }
        
        AlertDialog(
            onDismissRequest = { clientToEdit = null },
            title = { Text(stringResource(R.string.edit_client)) },
            text = {
                Column {
                    OutlinedTextField(
                        value = nom, 
                        onValueChange = { nom = it }, 
                        label = { Text(stringResource(R.string.client_name)) }
                    )
                    OutlinedTextField(
                        value = if (preu > 0) preu.toString() else "",
                        onValueChange = { input ->
                            val normalized = input.replace(',', '.')
                            preu = normalized.toDoubleOrNull() ?: 0.0
                        },
                        label = { Text(stringResource(R.string.default_price)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    // Guardem el client. El ViewModel/Repository NO ha d'actualitzar els bolos passats.
                    viewModel.saveClient(client.copy(nom = nom, preuHoraDefecte = preu))
                    clientToEdit = null
                }) { Text(stringResource(R.string.common_save)) }
            },
            dismissButton = {
                TextButton(onClick = { clientToEdit = null }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.clients_title)) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showNewClientDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_client))
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { clientToEdit = client } // Click per editar
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
                        IconButton(onClick = { viewModel.deleteClient(client) }) {
                            Icon(
                                Icons.Default.Delete, 
                                contentDescription = stringResource(R.string.common_delete),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}