package com.freelance.hores.ui.screen.resum

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.freelance.hores.R
import com.freelance.hores.ui.component.DiaCard
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumScreen(
    navController: NavHostController,
    viewModel: ResumViewModel = hiltViewModel()
) {
    val resumState by viewModel.resumState.collectAsState()
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var selectedEstat by remember { mutableStateOf<String?>(null) }
    var selectedClient by remember { mutableStateOf<com.freelance.hores.domain.model.Client?>(null) }
    var expandedClient by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // ✅ AFEGIR AQUEST CODI:
    // Calcula diasAgrupats basant-se en els filtres
    val diasAgrupats = remember(resumState.dias, selectedEstat, selectedClient) {
        var filtered = resumState.dias
        
        // Filtra per estat si està seleccionat
        if (selectedEstat != null) {
            filtered = filtered.map { dia ->
                dia.copy(conceptes = dia.conceptes.filter { concepte ->
                    concepte.estat == selectedEstat
                })
            }.filter { it.conceptes.isNotEmpty() }
        }
        
        // Filtra per client si està seleccionat
        if (selectedClient != null) {
            filtered = filtered.map { dia ->
                dia.copy(conceptes = dia.conceptes.filter { concepte ->
                    concepte.clientId == selectedClient?.id
                })
            }.filter { it.conceptes.isNotEmpty() }
        }
        
        // Agrupa conceptes
        filtered.map { dia ->
            val conceptesAgrupats = dia.conceptes
                .groupBy { it.clientId ?: "no-client" }
                .map { (clientId, conceptes) ->
                    val noms = conceptes.map { it.nom }.joinToString(" & ")
                    val rangsAgrupats = conceptes.flatMap { it.rangsHoraris }
                    
                    conceptes.first().copy(
                        nom = noms,
                        rangsHoraris = rangsAgrupats
                    )
                }
            dia.copy(conceptes = conceptesAgrupats)
        }
    }

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
                    Text(stringResource(R.string.common_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text(stringResource(R.string.common_cancel))
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
                    Text(stringResource(R.string.common_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.resum_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_cancel))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
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
                            value = selectedClient?.nom ?: stringResource(R.string.all_clients),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.filter_by_client)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedClient) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedClient,
                            onDismissRequest = { expandedClient = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.all_clients)) },
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
                
                // --- DISPOSICIÓ: Filtres -> Desglossament -> Totals -> Gràfic ---

                // 1. Filtres (mantenim la lògica actual, només reordenem)
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
                            value = selectedClient?.nom ?: stringResource(R.string.all_clients),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.filter_by_client)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedClient) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedClient,
                            onDismissRequest = { expandedClient = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.all_clients)) },
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
                
                // Selector Rang de Dates (Nou per completar la funcionalitat)
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            OutlinedButton(onClick = { viewModel.loadThisWeek() }, modifier = Modifier.weight(1f)) {
                                Text(stringResource(R.string.resum_this_week), style = MaterialTheme.typography.labelSmall)
                            }
                            OutlinedButton(onClick = { viewModel.loadThisMonth() }, modifier = Modifier.weight(1f)) {
                                Text(stringResource(R.string.resum_this_month), style = MaterialTheme.typography.labelSmall)
                            }
                            OutlinedButton(onClick = { viewModel.loadLastMonth() }, modifier = Modifier.weight(1f)) {
                                Text(stringResource(R.string.resum_last_month), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        OutlinedButton(onClick = { showStartDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.resum_custom), style = MaterialTheme.typography.labelSmall)
                        }
                        // Visualització del rang seleccionat
                        Text(
                            text = "${resumState.startDate} ${stringResource(R.string.resum_to)} ${resumState.endDate}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth().padding(top=4.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                if (diasAgrupats.isNotEmpty()) {
                    items(diasAgrupats) { dia ->
                        DiaCard(
                            data = LocalDate.parse(dia.data),
                            conceptes = dia.conceptes,
                            onClick = { navController.navigate("dia/${dia.id}") }
                        )
                    }
                } else {
                    item {
                        Text(
                            text = stringResource(R.string.resum_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp)
                        )
                    }
                }

                // 3. Ingressos i Hores (Totals)
                item {
                    val totalHores = diasAgrupats.sumOf { it.getTotalHoras() }
                    val totalDiners = diasAgrupats.sumOf { it.getTotalDiners() }
                    Text(
                        text = stringResource(R.string.resum_total_diners, String.format("%.2f", totalHores), String.format("%.2f", totalDiners)),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                // 4. Gràfic/Resum per client (ara al final)
                item {
                    if (diasAgrupats.isNotEmpty()) {
                        val dadesGrafic = diasAgrupats.flatMap { it.conceptes }
                            .groupBy { it.clientNom ?: stringResource(R.string.no_client) }
                            .mapValues { entry -> entry.value.sumOf { it.getTotalDiners() } }
                        GraficGuanys(dades = dadesGrafic)

                        val summary = diasAgrupats.flatMap { it.conceptes }.groupBy { it.nom }.mapValues { entry ->
                            entry.value.sumOf { it.getTotalHoras() }
                        }
                        ConceptesSummary(
                            conceptesSummary = summary
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
                                val intent = viewModel.exportCsv(diasAgrupats)
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.resum_export_csv))
                        }
                        Button(
                            onClick = { 
                                val intent = viewModel.exportPdf(diasAgrupats)
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.resum_export_pdf))
                        }
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
        Text(stringResource(R.string.resum_summary_by_concept), style = MaterialTheme.typography.titleMedium)

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