package com.legacyvault.app.ui.pages.editors

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.legacyvault.app.domain.model.PageContent
import com.legacyvault.app.domain.model.enums.NotifyUnit
import com.legacyvault.app.domain.model.enums.ReminderRecurrence
import com.legacyvault.app.domain.model.enums.ReminderTag

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReminderEditor(
    content: PageContent.Reminder,
    onContentChange: (PageContent.Reminder) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {

        OutlinedTextField(
            value         = content.date,
            onValueChange = { onContentChange(content.copy(date = it)) },
            label         = { Text("Date (YYYY-MM-DD)") },
            singleLine    = true,
            modifier      = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value         = content.endDate ?: "",
            onValueChange = { onContentChange(content.copy(endDate = it.ifBlank { null })) },
            label         = { Text("End date (optional)") },
            singleLine    = true,
            modifier      = Modifier.fillMaxWidth()
        )

        HorizontalDivider()

        // Tag chips
        Text("Tag", style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ReminderTag.entries.forEach { tag ->
                FilterChip(
                    selected = content.tag == tag,
                    onClick  = { onContentChange(content.copy(tag = tag)) },
                    label    = { Text(tag.label) }
                )
            }
        }

        HorizontalDivider()

        // Recurrence chips
        Text("Recurrence", style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ReminderRecurrence.entries.forEach { rec ->
                FilterChip(
                    selected = content.recurrence == rec,
                    onClick  = { onContentChange(content.copy(recurrence = rec)) },
                    label    = { Text(rec.label) }
                )
            }
        }

        if (content.recurrence != ReminderRecurrence.once) {
            OutlinedTextField(
                value         = if (content.recurrenceInterval <= 0) "" else content.recurrenceInterval.toString(),
                onValueChange = { onContentChange(content.copy(recurrenceInterval = it.toIntOrNull() ?: 1)) },
                label         = { Text("Every N ${content.recurrence.label.lowercase()}(s)") },
                singleLine    = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier      = Modifier.fillMaxWidth()
            )
        }

        HorizontalDivider()

        OutlinedTextField(
            value         = content.notes,
            onValueChange = { onContentChange(content.copy(notes = it)) },
            label         = { Text("Notes") },
            modifier      = Modifier.fillMaxWidth()
        )

        HorizontalDivider()

        // Notification
        Row(
            modifier          = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Enable notification", style = MaterialTheme.typography.bodyMedium)
            Switch(
                checked         = content.notifyEnabled,
                onCheckedChange = { onContentChange(content.copy(notifyEnabled = it)) }
            )
        }

        if (content.notifyEnabled) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text("Notify", style = MaterialTheme.typography.bodyMedium)

                OutlinedTextField(
                    value         = if (content.notifyBefore <= 0) "" else content.notifyBefore.toString(),
                    onValueChange = { onContentChange(content.copy(notifyBefore = it.toIntOrNull() ?: 1)) },
                    singleLine    = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier      = Modifier.weight(1f)
                )

                Text("before — unit:", style = MaterialTheme.typography.bodyMedium)
            }

            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NotifyUnit.entries.forEach { unit ->
                    FilterChip(
                        selected = content.notifyUnit == unit,
                        onClick  = { onContentChange(content.copy(notifyUnit = unit)) },
                        label    = { Text(unit.label) }
                    )
                }
            }
        }
    }
}
