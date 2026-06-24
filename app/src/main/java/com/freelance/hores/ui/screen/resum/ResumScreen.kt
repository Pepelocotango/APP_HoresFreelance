package com.freelance.hores.ui.screen.resum

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.freelance.hores.R
import java.time.Instant
import java.time.LocalDate
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
    var expandedClient by remember { mutableStateOf(false) }
    var expandedStatus by remember { mutableStateOf(false) }

    val context = LocalContext.current

    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = (resumState.startDate ?: LocalDate.now()).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                            viewModel.setCustomPeriod(date, resumState.endDate)
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
            initialSelectedDateMillis = (resumState.endDate ?: LocalDate.now()).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                            viewModel.setCustomPeriod(resumState.startDate, date)
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
                actions = {
                    TextButton(onClick = { context.startActivity(viewModel.exportCsv()) }) {
                        Text("CSV", fontWeight = FontWeight.Bold)
                    }
                    TextButton(onClick = { context.startActivity(viewModel.exportPdf()) }) {
                        Text("PDF", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        if (resumState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. FILTRES
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                                Text(
                                    text = stringResource(R.string.filter_all).uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Range Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf(
                                    DateRangeType.SETMANA to R.string.resum_this_week,
                                    DateRangeType.MES to R.string.resum_this_month,
                                    DateRangeType.ANTERIOR to R.string.resum_last_month,
                                    DateRangeType.TOTS to R.string.filter_all,
                                    DateRangeType.LLIURE to R.string.resum_custom
                                ).forEach { (type, labelRes) ->
                                    val isSelected = resumState.rangeType == type
                                    SuggestionChip(
                                        onClick = { viewModel.setRangeType(type) },
                                        label = { Text(stringResource(labelRes), fontSize = 10.sp) },
                                        colors = SuggestionChipDefaults.suggestionChipColors(
                                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            labelColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }

                            if (resumState.rangeType == DateRangeType.LLIURE) {
                                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(onClick = { showStartDatePicker = true }, modifier = Modifier.weight(1f)) {
                                        Text(resumState.startDate?.toString() ?: stringResource(R.string.resum_from), style = MaterialTheme.typography.labelSmall)
                                    }
                                    OutlinedButton(onClick = { showEndDatePicker = true }, modifier = Modifier.weight(1f)) {
                                        Text(resumState.endDate?.toString() ?: stringResource(R.string.resum_to), style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Status Filter
                                ExposedDropdownMenuBox(
                                    expanded = expandedStatus,
                                    onExpandedChange = { expandedStatus = !expandedStatus },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    OutlinedTextField(
                                        value = when(resumState.statusFilter) {
                                            "PENDENT" -> stringResource(R.string.status_pending)
                                            "FACTURAT" -> stringResource(R.string.status_invoiced)
                                            "COBRAT" -> stringResource(R.string.status_paid)
                                            else -> stringResource(R.string.filter_all)
                                        },
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text(stringResource(R.string.status_title), fontSize = 10.sp) },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus) },
                                        modifier = Modifier.menuAnchor(),
                                        textStyle = MaterialTheme.typography.bodySmall
                                    )
                                    ExposedDropdownMenu(expanded = expandedStatus, onDismissRequest = { expandedStatus = false }) {
                                        DropdownMenuItem(text = { Text(stringResource(R.string.filter_all)) }, onClick = { viewModel.setStatusFilter("Tots"); expandedStatus = false })
                                        listOf("PENDENT", "FACTURAT", "COBRAT").forEach { status ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text(when(status) {
                                                        "PENDENT" -> stringResource(R.string.status_pending)
                                                        "FACTURAT" -> stringResource(R.string.status_invoiced)
                                                        "COBRAT" -> stringResource(R.string.status_paid)
                                                        else -> status
                                                    })
                                                },
                                                onClick = { viewModel.setStatusFilter(status); expandedStatus = false }
                                            )
                                        }
                                    }
                                }

                                // Client Filter
                                ExposedDropdownMenuBox(
                                    expanded = expandedClient,
                                    onExpandedChange = { expandedClient = !expandedClient },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    OutlinedTextField(
                                        value = resumState.clients.find { it.id == resumState.clientFilter }?.nom ?: stringResource(R.string.all_clients),
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text(stringResource(R.string.filter_by_client), fontSize = 10.sp) },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedClient) },
                                        modifier = Modifier.menuAnchor(),
                                        textStyle = MaterialTheme.typography.bodySmall
                                    )
                                    ExposedDropdownMenu(expanded = expandedClient, onDismissRequest = { expandedClient = false }) {
                                        DropdownMenuItem(text = { Text(stringResource(R.string.all_clients)) }, onClick = { viewModel.setClientFilter("Tots"); expandedClient = false })
                                        resumState.clients.forEach { client ->
                                            DropdownMenuItem(text = { Text(client.nom) }, onClick = { viewModel.setClientFilter(client.id); expandedClient = false })
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. DESGLOSSAMENT
                item {
                    Text(
                        text = stringResource(R.string.resum_summary_by_concept).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                if (resumState.filteredItems.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.resum_empty),
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                } else {
                    items(resumState.filteredItems) { item ->
                        DesglossamentItem(item, onClick = { navController.navigate("dia/${item.diaId}") })
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                }

                // 3. TOTALS
                item {
                    val totalHores = resumState.filteredItems.sumOf { it.hours }
                    val totalEarnings = resumState.filteredItems.sumOf { it.earnings }
                    val totalDespeses = resumState.filteredItems.sumOf { it.despeses }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Ingressos
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(stringResource(R.string.resum_total_diners_label).uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
                                    Text(text = "${String.format("%.2f", totalEarnings)} €", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                            // Hores
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(stringResource(R.string.resum_total_hours_label).uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                    Text(text = "${String.format("%.1f", totalHores)} h", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (totalDespeses > 0) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                            ) {
                                Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(stringResource(R.string.expenses_title), style = MaterialTheme.typography.bodyMedium, color = Color(0xFFB71C1C), fontWeight = FontWeight.Medium)
                                    Text(text = "${String.format("%.2f", totalDespeses)} €", style = MaterialTheme.typography.bodyLarge, color = Color(0xFFB71C1C), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // 4. GRÀFIC PER CLIENT
                item {
                    if (resumState.filteredItems.isNotEmpty()) {
                        val dadesGrafic = resumState.filteredItems
                            .groupBy { it.clientNom }
                            .mapValues { entry ->
                                Triple(
                                    entry.value.sumOf { it.earnings },
                                    entry.value.sumOf { it.hours },
                                    entry.value.sumOf { it.earnings } / (resumState.filteredItems.sumOf { it.earnings }.takeIf { it > 0 } ?: 1.0)
                                )
                            }
                            .toList()
                            .sortedByDescending { it.second.first }

                        GraficGuanys(dades = dadesGrafic)
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
fun DesglossamentItem(item: GroupedResumItem, onClick: () -> Unit) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val date = LocalDate.parse(item.data).format(dateFormatter)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "$date - ${item.conceptesNoms.joinToString(" & ")}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
            Text(
                text = "${item.clientNom} · ${when(item.estat) {
                    "PENDENT" -> stringResource(R.string.status_pending)
                    "FACTURAT" -> stringResource(R.string.status_invoiced)
                    "COBRAT" -> stringResource(R.string.status_paid)
                    else -> item.estat
                }}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                text = item.rangsHoraris.joinToString(" | ") { "${it.horaInici}-${it.horaFi}" },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(text = "${String.format("%.2f", item.earnings)} €", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(text = "${String.format("%.1f", item.hours)} h", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        }
    }
}
