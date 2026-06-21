package com.freelance.hores.ui.screen.resum
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

import com.freelance.hores.data.db.entity.EstatFacturacio

@OptIn(ExperimentalMaterial3Api::class, KoinExperimentalAPI::class)
@Composable
fun FilterFacturacio(
    selectedEstat: EstatFacturacio?,
    onEstatSelected: (EstatFacturacio?) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedEstat == null,
            onClick = { onEstatSelected(null) },
            label = { Text("Tots") }
        )
        EstatFacturacio.entries.forEach { estat ->
            FilterChip(
                selected = selectedEstat == estat,
                onClick = { onEstatSelected(estat) },
                label = { Text(estat.name) }
            )
        }
    }
}

@Composable
fun GraficGuanys(
    dades: Map<String, Double>,
    modifier: Modifier = Modifier
) {
    val totalMax = dades.values.maxOrNull() ?: 1.0
    Column(modifier = modifier.fillMaxWidth().padding(16.dp)) {
        Text("Guanys per Client", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        dades.forEach { (client, valor) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(client, modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .weight(2f)
                        .height(20.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth((valor / totalMax).toFloat())
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
                Text("${String.format("%.2f", valor)}€", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}
