package com.freelance.hores.ui.screen.clients
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI


@OptIn(ExperimentalMaterial3Api::class, KoinExperimentalAPI::class)
@Composable
fun ClientsScreen(
    navController: NavHostController,
    viewModel: ClientsViewModel = koinViewModel()
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
