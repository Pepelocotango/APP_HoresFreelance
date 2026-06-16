package com.freelance.hores.ui.screen.registre

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.freelance.hores.R
import com.freelance.hores.domain.model.RangHorari
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistreScreen(
    navController: NavHostController,
    diaId: Long = 0,
    initialDate: LocalDate? = null,
    viewModel: RegistreViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val formState by viewModel.formState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDatePicker by remember { mutableStateOf(false) }
    val savedMessage = stringResource(R.string.common_saved)
    val defaultErrorMessage = stringResource(R.string.common_error)

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = formState.data.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )

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

    LaunchedEffect(formState.errorResId, formState.error) {
        if (formState.errorResId != null) {
            snackbarHostState.showSnackbar(context.getString(formState.errorResId!!))
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
                            val date = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            viewModel.setData(date)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.common_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
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
                title = { Text(stringResource(R.string.registre_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
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
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.registre_date), style = MaterialTheme.typography.labelMedium)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true }
                    ) {
                        OutlinedTextField(
                            value = formState.data.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")),
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
                    Text(stringResource(R.string.registre_notes), style = MaterialTheme.typography.labelMedium)
                    OutlinedTextField(
                        value = formState.notes,
                        onValueChange = { viewModel.setNotes(it) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            }

            item {
                Text(stringResource(R.string.dia_detall_conceptes), style = MaterialTheme.typography.headlineSmall)
            }

            items(formState.conceptes.size) { concepteIndex ->
                ConcepteFormItem(
                    concepte = formState.conceptes[concepteIndex],
                    onNameChange = { viewModel.updateConcepteName(concepteIndex, it) },
                    onAddRang = { viewModel.addRangHorariToConcepte(concepteIndex) },
                    onRemoveRang = { rangIndex -> viewModel.removeRangHorari(concepteIndex, rangIndex) },
                    onUpdateHoraInici = { rangIndex, hora ->
                        viewModel.updateRangHorariInici(concepteIndex, rangIndex, hora)
                    },
                    onUpdateHoraFi = { rangIndex, hora ->
                        viewModel.updateRangHorariFi(concepteIndex, rangIndex, hora)
                    },
                    onDelete = { viewModel.removeConcepte(concepteIndex) }
                )
            }

            item {
                Button(
                    onClick = { viewModel.addConcepte() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text(stringResource(R.string.registre_add_concepte))
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
                        Text(stringResource(R.string.registre_cancel))
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
                        Text(stringResource(R.string.registre_save))
                    }
                }
            }
        }
    }
}

@Composable
fun ConcepteFormItem(
    concepte: ConcepteForm,
    onNameChange: (String) -> Unit,
    onAddRang: () -> Unit,
    onRemoveRang: (Int) -> Unit,
    onUpdateHoraInici: (Int, LocalTime) -> Unit,
    onUpdateHoraFi: (Int, LocalTime) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                label = { Text(stringResource(R.string.registre_concepte_name)) },
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.common_delete))
            }
        }

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
            Text(stringResource(R.string.registre_add_rang))
        }
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
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

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
                        onUpdateInici(LocalTime.of(timeState.hour, timeState.minute))
                        showStartPicker = false
                    }
                ) {
                    Text(stringResource(R.string.common_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartPicker = false }) {
                    Text(stringResource(R.string.common_cancel))
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
                        onUpdateFi(LocalTime.of(timeState.hour, timeState.minute))
                        showEndPicker = false
                    }
                ) {
                    Text(stringResource(R.string.common_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndPicker = false }) {
                    Text(stringResource(R.string.common_cancel))
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
                    value = rang.horaInici.format(timeFormatter),
                    onValueChange = {},
                    label = { Text(stringResource(R.string.registre_hora_inici)) },
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
                    value = rang.horaFi.format(timeFormatter),
                    onValueChange = {},
                    label = { Text(stringResource(R.string.registre_hora_fi)) },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    enabled = false
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.common_delete))
            }
        }

        val duration = RangHorari(
            concepteId = 0,
            horaInici = rang.horaInici,
            horaFi = rang.horaFi
        ).getDuracionaFormatada()

        Text(
            text = stringResource(R.string.registre_duracio, duration),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
