package com.freelance.hores.ui.screen.resum

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.freelance.hores.R
import com.freelance.hores.data.db.entity.EstatFacturacio
import com.freelance.hores.ui.component.DiaCard
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumScreen(
    navController: NavHostController,
    viewModel: ResumViewModel = hiltViewModel()
) {
    val resumState by viewModel.resumState.collectAsState()
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var selectedEstat by remember { mutableStateOf<EstatFacturacio?>(null) }

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
                
                val filteredDias = if (selectedEstat == null) {
                    resumState.dias
                } else {
                    resumState.dias.map { dia ->
                        dia.copy(conceptes = dia.conceptes.filter { it.estat == selectedEstat })
                    }.filter { it.conceptes.isNotEmpty() }
                }

                item {
                    val dadesGrafic = filteredDias.flatMap { it.conceptes }
                        .groupBy { it.clientNom ?: "Sense Client" }
                        .mapValues { entry -> entry.value.sumOf { it.getTotalDiners() } }
                    GraficGuanys(dades = dadesGrafic)
                }
                
                // ... (rest of the content, replace item { ... } with list)
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
                                Text(stringResource(R.string.resum_this_week), style = MaterialTheme.typography.labelSmall)
                            }
                            OutlinedButton(
                                onClick = { viewModel.loadThisMonth() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(stringResource(R.string.resum_this_month), style = MaterialTheme.typography.labelSmall)
                            }
                            OutlinedButton(
                                onClick = { viewModel.loadLastMonth() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(stringResource(R.string.resum_last_month), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        OutlinedButton(
                            onClick = { showStartDatePicker = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.resum_custom), style = MaterialTheme.typography.labelSmall)
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
                            text = " ${stringResource(R.string.resum_to)} ",
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
                            text = stringResource(R.string.resum_empty),
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
                    val context = LocalContext.current
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { 
                                val intent = viewModel.exportCsv()
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.resum_export_csv))
                        }
                        Button(
                            onClick = { 
                                val intent = viewModel.exportPdf()
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
