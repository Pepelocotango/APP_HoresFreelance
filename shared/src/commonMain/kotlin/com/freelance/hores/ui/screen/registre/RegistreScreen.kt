package com.freelance.hores.ui.screen.registre

import com.freelance.hores.data.db.entity.EstatFacturacio
import com.freelance.hores.domain.model.Client
import com.freelance.hores.domain.model.RangHorari
import com.freelance.hores.util.epochMillisToLocalDate
import com.freelance.hores.util.localDateToEpochMillis
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

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
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.ui.text.input.KeyboardType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistreScreen(
    navController: NavHostController,
    diaId: Long = 0,
    initialDate: LocalDate? = null,
    viewModel: RegistreViewModel = koinInject()
) {
    val formState by viewModel.formState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDatePicker by remember { mutableStateOf(false) }
    var showNewClientDialog by remember { mutableStateOf(false) }
    val savedMessage = "Valor"
    val defaultErrorMessage = "Valor"

    val datePickerState = key(formState.data) {
        rememberDatePickerState(
            initialSelectedDateMillis = localDateToEpochMillis(formState.data)
        )
    }

    LaunchedEffect(diaId, initialDate) {
        if (diaId > 0) {
            viewModel.loadDiaForEditing(diaId)
        } else {
            if (initialDate != null) {
                viewModel.setData(initialDate)
            }
            if (viewModel.formState.value.conceptes.isEmpty()) {
                viewModel.addConcepte()
            }
        }
    }

    LaunchedEffect(formState.success) {
        if (formState.success) {
            snackbarHostState.showSnackbar(savedMessage)
            viewModel.resetSuccess()
            navController.popBackStack()
        }
    }

    LaunchedEffect(formState.errorMessage, formState.error) {
        if (formState.errorMessage != null) {
            snackbarHostState.showSnackbar(formState.errorMessage!!)
            viewModel.clearError()
        } else if (formState.error != null) {
            snackbarHostState.showSnackbar(formState.error ?: defaultErrorMessage)
            viewModel.clearError()
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = epochMillisToLocalDate(millis)
                            viewModel.setData(date)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Desa")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel·la")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    if (showNewClientDialog) {
        var nom by remember { mutableStateOf("") }
        var preuInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNewClientDialog = false },
            title = { Text("Nou Client") },
            text = {
                Column {
                    OutlinedTextField(value = nom, onValueChange = { nom = it }, label = { Text("Nom del Client") })
                    OutlinedTextField(
                        value = preuInput,
                        onValueChange = { preuInput = it },
                        label = { Text("Preu per defecte") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val parsedPreu = preuInput.replace(',', '.').toDoubleOrNull() ?: 0.0
                    viewModel.createClient(nom, parsedPreu)
                    showNewClientDialog = false
                }) { Text("Desa") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registre") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Button(
                    onClick = { viewModel.saveDia() },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Text("Desa ràpidament")
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Valor", style = MaterialTheme.typography.labelMedium)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true }
                    ) {
                        OutlinedTextField(
                            value = formState.data.toString(),
                            onValueChange = {},
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            enabled = false
                        )
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Valor", style = MaterialTheme.typography.labelMedium)
                    OutlinedTextField(
                        value = formState.notes,
                        onValueChange = { viewModel.setNotes(it) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            }

            item {
                Text("Valor", style = MaterialTheme.typography.headlineSmall)
            }

            items(formState.conceptes.size) { concepteIndex ->
                ConcepteFormItem(
                    concepte = formState.conceptes[concepteIndex],
                    clients = formState.clients,
                    onNameChange = { viewModel.updateConcepteName(concepteIndex, it) },
                    onPreuChange = { viewModel.updateConceptePreu(concepteIndex, it) },
                    onClientChange = { viewModel.updateConcepteClient(concepteIndex, it) },
                    onCreateClient = { showNewClientDialog = true },
                    onAddRang = { viewModel.addRangHorariToConcepte(concepteIndex) },
                    onRemoveRang = { rangIndex -> viewModel.removeRangHorari(concepteIndex, rangIndex) },
                    onUpdateHoraInici = { rangIndex, hora ->
                        viewModel.updateRangHorariInici(concepteIndex, rangIndex, hora)
                    },
                    onUpdateHoraFi = { rangIndex, hora ->
                        viewModel.updateRangHorariFi(concepteIndex, rangIndex, hora)
                    },
                    onDelete = { viewModel.removeConcepte(concepteIndex) },
                    onEstatChange = { viewModel.updateConcepteEstat(concepteIndex, it) },
                    onDespesesChange = { viewModel.updateConcepteDespeses(concepteIndex, it) },
                    onDespesesNotesChange = { viewModel.updateConcepteDespesesNotes(concepteIndex, it) },
                    onEsPreuFixChange = { viewModel.updateConcepteEsPreuFix(concepteIndex, it) },
                    onImportPreuFixChange = { viewModel.updateConcepteImportPreuFix(concepteIndex, it) }
                )
            }

            item {
                Button(
                    onClick = { viewModel.addConcepte() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("Valor")
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Valor")
                    }

                    Button(
                        onClick = { viewModel.saveDia() },
                        modifier = Modifier.weight(1f),
                        enabled = !formState.isSaving
                    ) {
                        if (formState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(end = 8.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Text("Valor")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConcepteFormItem(
    concepte: ConcepteForm,
    clients: List<Client>,
    onNameChange: (String) -> Unit,
    onPreuChange: (Double) -> Unit,
    onClientChange: (Long?) -> Unit,
    onCreateClient: () -> Unit,
    onAddRang: () -> Unit,
    onRemoveRang: (Int) -> Unit,
    onUpdateHoraInici: (Int, LocalTime) -> Unit,
    onUpdateHoraFi: (Int, LocalTime) -> Unit,
    onDelete: () -> Unit,
    onEstatChange: (EstatFacturacio) -> Unit,
    onDespesesChange: (Double) -> Unit,
    onDespesesNotesChange: (String) -> Unit,
    onEsPreuFixChange: (Boolean) -> Unit,
    onImportPreuFixChange: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var expandedEstat by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp)
            .then(
                Modifier
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.small)
                    .padding(12.dp)
            ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = concepte.nom,
                onValueChange = onNameChange,
                label = { Text("Valor") },
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar")
            }
        }

        // Rangs horaris a dalt
        concepte.rangsHoraris.forEachIndexed { rangIndex, rang ->
            RangHorariFormItem(
                rang = rang,
                onUpdateInici = { onUpdateHoraInici(rangIndex, it) },
                onUpdateFi = { onUpdateHoraFi(rangIndex, it) },
                onDelete = { onRemoveRang(rangIndex) }
            )
        }

        OutlinedButton(
            onClick = onAddRang,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Text("Valor")
        }

        // Preu Fix Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Preu fix per bolo", style = MaterialTheme.typography.bodyMedium)
            androidx.compose.material3.Switch(
                checked = concepte.esPreuFix,
                onCheckedChange = onEsPreuFixChange
            )
        }

        if (concepte.esPreuFix) {
            OutlinedTextField(
                value = if (concepte.importPreuFix > 0) concepte.importPreuFix.toString().replace('.', ',') else "",
                onValueChange = { input ->
                    val normalized = input.replace(',', '.')
                    onImportPreuFixChange(normalized.toDoubleOrNull() ?: 0.0)
                },
                label = { Text("Import Preu Fix (€)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        }

        // Dropdowns i preus
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = clients.find { it.id == concepte.clientId }?.nom ?: "Selecciona client",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                clients.forEach { client ->
                    DropdownMenuItem(
                        text = { Text(client.nom) },
                        onClick = {
                            onClientChange(client.id)
                            expanded = false
                        }
                    )
                }
                DropdownMenuItem(
                    text = { Text("+ Nou client") },
                    onClick = {
                        onCreateClient()
                        expanded = false
                    }
                )
            }
        }

        ExposedDropdownMenuBox(
            expanded = expandedEstat,
            onExpandedChange = { expandedEstat = !expandedEstat }
        ) {
            OutlinedTextField(
                value = concepte.estat.name,
                onValueChange = {},
                readOnly = true,
                label = { Text("Estat de facturació") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEstat) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expandedEstat,
                onDismissRequest = { expandedEstat = false }
            ) {
                EstatFacturacio.entries.forEach { estat ->
                    DropdownMenuItem(
                        text = { Text(estat.name) },
                        onClick = {
                            onEstatChange(estat)
                            expandedEstat = false
                        }
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = if (concepte.preuHora > 0) concepte.preuHora.toString().replace('.', ',') else "",
                onValueChange = { input ->
                    val normalized = input.replace(',', '.')
                    onPreuChange(normalized.toDoubleOrNull() ?: 0.0)
                },
                label = { Text("Valor") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            OutlinedTextField(
                value = if (concepte.despeses > 0) concepte.despeses.toString().replace('.', ',') else "",
                onValueChange = { input ->
                    val normalized = input.replace(',', '.')
                    onDespesesChange(normalized.toDoubleOrNull() ?: 0.0)
                },
                label = { Text("Despeses (€)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        }
        
        // Notes de despeses a baix
        OutlinedTextField(
            value = concepte.despesesNotes,
            onValueChange = onDespesesNotesChange,
            label = { Text("Notes despeses") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RangHorariFormItem(
    rang: RangHorariForm,
    onUpdateInici: (LocalTime) -> Unit,
    onUpdateFi: (LocalTime) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    if (showStartPicker) {
        val timeState = rememberTimePickerState(
            initialHour = rang.horaInici.hour,
            initialMinute = rang.horaInici.minute,
            is24Hour = true
        )
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onUpdateInici(LocalTime(timeState.hour, timeState.minute))
                        showStartPicker = false
                    }
                ) {
                    Text("Desa")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartPicker = false }) {
                    Text("Cancel·la")
                }
            },
            text = {
                TimePicker(state = timeState)
            }
        )
    }

    if (showEndPicker) {
        val timeState = rememberTimePickerState(
            initialHour = rang.horaFi.hour,
            initialMinute = rang.horaFi.minute,
            is24Hour = true
        )
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onUpdateFi(LocalTime(timeState.hour, timeState.minute))
                        showEndPicker = false
                    }
                ) {
                    Text("Desa")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndPicker = false }) {
                    Text("Cancel·la")
                }
            },
            text = {
                TimePicker(state = timeState)
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .then(
                Modifier
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.tertiaryContainer,
                        MaterialTheme.shapes.extraSmall
                    )
                    .padding(8.dp)
            ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { showStartPicker = true }
            ) {
                OutlinedTextField(
                    value = "%02d:%02d".format(rang.horaInici.hour, rang.horaInici.minute),
                    onValueChange = {},
                    label = { Text("Valor") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    enabled = false
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { showEndPicker = true }
            ) {
                OutlinedTextField(
                    value = "%02d:%02d".format(rang.horaFi.hour, rang.horaFi.minute),
                    onValueChange = {},
                    label = { Text("Valor") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    enabled = false
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar")
            }
        }

        val duration = RangHorari(
            concepteId = 0,
            horaInici = rang.horaInici,
            horaFi = rang.horaFi
        ).getDuracionaFormatada()

        Text(
            text = "Durada: $duration",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
