package com.freelance.hores.ui.screen.resum
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

import com.freelance.hores.data.db.entity.EstatFacturacio
import com.freelance.hores.ui.component.DiaCard
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, KoinExperimentalAPI::class)
@Composable
fun ResumScreen(
    navController: NavHostController,
    viewModel: ResumViewModel = koinViewModel()
) {
    val resumState by viewModel.resumState.collectAsState()
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var selectedEstat by remember { mutableStateOf<EstatFacturacio?>(null) }
    var selectedClient by remember { mutableStateOf<com.freelance.hores.domain.model.Client?>(null) }
    var expandedClient by remember { mutableStateOf(false) }

    val context = LocalContext.current

    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = resumState.startDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = Instant.ofEpochMilli(millis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                            viewModel.loadCustomPeriod(date, resumState.endDate)
                        }
                        showStartDatePicker = false
                    }
                ) {
                    Text("Desa")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("Cancel·la")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = resumState.endDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = Instant.ofEpochMilli(millis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                            viewModel.loadCustomPeriod(resumState.startDate, date)
                        }
                        showEndDatePicker = false
                    }
                ) {
                    Text("Desa")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("Cancel·la")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resum") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cancel·la")
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
        if (resumState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    FilterFacturacio(selectedEstat = selectedEstat, onEstatSelected = { selectedEstat = it })
                }

                // Selector Client
                item {
                    ExposedDropdownMenuBox(
                        expanded = expandedClient,
                        onExpandedChange = { expandedClient = !expandedClient },
                        modifier = Modifier.fillMaxWidth().padding(8.dp)
                    ) {
                        OutlinedTextField(
                            value = selectedClient?.nom ?: "Tots els clients",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Filtrar per client") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedClient) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedClient,
                            onDismissRequest = { expandedClient = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Tots els clients") },
                                onClick = {
                                    selectedClient = null
                                    expandedClient = false
                                }
                            )
                            resumState.clients.forEach { client ->
                                DropdownMenuItem(
                                    text = { Text(client.nom) },
                                    onClick = {
                                        selectedClient = client
                                        expandedClient = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                val filteredDias = resumState.dias.map { dia ->
                    dia.copy(conceptes = dia.conceptes.filter { concepte ->
                        (selectedEstat == null || concepte.estat == selectedEstat) &&
                        (selectedClient == null || concepte.clientId == selectedClient?.id)
                    })
                }.filter { it.conceptes.isNotEmpty() }

                item {
                    val dadesGrafic = filteredDias.flatMap { it.conceptes }
                        .groupBy { it.clientNom ?: "Sense Client" }
                        .mapValues { entry -> entry.value.sumOf { it.getTotalDiners() } }
                    GraficGuanys(dades = dadesGrafic)
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.loadThisWeek() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Valor", style = MaterialTheme.typography.labelSmall)
                            }
                            OutlinedButton(
                                onClick = { viewModel.loadThisMonth() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Valor", style = MaterialTheme.typography.labelSmall)
                            }
                            OutlinedButton(
                                onClick = { viewModel.loadLastMonth() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Valor", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        OutlinedButton(
                            onClick = { showStartDatePicker = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Valor", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${resumState.startDate}",
                            modifier = Modifier.clickable { showStartDatePicker = true },
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = " ${"Valor"} ",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "${resumState.endDate}",
                            modifier = Modifier.clickable { showEndDatePicker = true },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                item {
                    Text(
                        text = stringResource(R.string.resum_total_diners, String.format("%.2f", filteredDias.flatMap { it.conceptes }.sumOf { it.getTotalHoras() }), String.format("%.2f", filteredDias.flatMap { it.conceptes }.sumOf { it.getTotalDiners() })),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (filteredDias.isNotEmpty()) {
                    items(filteredDias) { dia ->
                        DiaCard(
                            data = dia.data,
                            conceptes = dia.conceptes,
                            totalHoras = dia.getTotalHoras(),
                            totalDiners = dia.getTotalDiners(),
                            onClick = { navController.navigate("dia/${dia.id}") }
                        )
                    }
                } else {
                    item {
                        Text(
                            text = "Valor",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp)
                        )
                    }
                }

                item {
                    if (filteredDias.isNotEmpty()) {
                        ConceptesSummary(
                            conceptesSummary = filteredDias.flatMap { it.conceptes }.groupBy { it.nom }.mapValues { entry -> entry.value.sumOf { it.getTotalHoras() } }
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { 
                                val intent = viewModel.exportCsv(filteredDias)
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Valor")
                        }
                        Button(
                            onClick = { 
                                val intent = viewModel.exportPdf(filteredDias)
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Valor")
                        }
                    }
                }

                item {
                    if (filteredDias.isNotEmpty()) {
                        ConceptesSummary(
                            conceptesSummary = filteredDias.flatMap { it.conceptes }.groupBy { it.nom }.mapValues { entry -> entry.value.sumOf { it.getTotalHoras() } }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ConceptesSummary(
    conceptesSummary: Map<String, Double>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .then(
                Modifier
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant,
                        MaterialTheme.shapes.small
                    )
                    .padding(12.dp)
            ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Valor", style = MaterialTheme.typography.titleMedium)

        conceptesSummary.forEach { (concepteName, horas) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = concepteName,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "${String.format("%.2f", horas)}h",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
