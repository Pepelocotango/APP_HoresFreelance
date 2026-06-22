package com.freelance.hores.ui.component
import com.freelance.hores.domain.model.Concepte
import kotlinx.datetime.LocalDate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton

@Composable
fun DiaCard(data: LocalDate, conceptes: List<Concepte>, totalHoras: Double, totalDiners: Double, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth().clickable(onClick = onClick), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = data.toString(), style = MaterialTheme.typography.titleMedium)
            conceptes.forEach { concepte -> Text(text = "${concepte.nom}: ${concepte.getTotalHoras()}h | ${concepte.getTotalDiners()}€", style = MaterialTheme.typography.bodySmall) }
            Text(text = "Total: ${totalHoras}h | ${totalDiners}€", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        }
    }
}
@Composable
fun ConcepteCard(concepte: Concepte, onEdit: () -> Unit, onDelete: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth().clickable(onClick = onEdit), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = concepte.nom, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "${concepte.getTotalHoras()}h | ${concepte.getTotalDiners()}€", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Eliminar", modifier = Modifier.size(20.dp)) }
                }
            }
        }
    }
}
