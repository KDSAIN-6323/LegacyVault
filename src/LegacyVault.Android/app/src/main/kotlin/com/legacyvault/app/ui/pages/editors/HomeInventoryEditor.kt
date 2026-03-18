package com.legacyvault.app.ui.pages.editors

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.legacyvault.app.domain.model.PageContent

@Composable
fun HomeInventoryEditor(
    content: PageContent.HomeInventory,
    onContentChange: (PageContent.HomeInventory) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {

        OutlinedTextField(
            value         = content.itemName,
            onValueChange = { onContentChange(content.copy(itemName = it)) },
            label         = { Text("Item name") },
            singleLine    = true,
            modifier      = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value         = content.description,
            onValueChange = { onContentChange(content.copy(description = it)) },
            label         = { Text("Description") },
            modifier      = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value         = content.location,
            onValueChange = { onContentChange(content.copy(location = it)) },
            label         = { Text("Location") },
            singleLine    = true,
            modifier      = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value         = if (content.value == 0.0) "" else content.value.toString(),
            onValueChange = { raw ->
                val v = raw.toDoubleOrNull() ?: 0.0
                onContentChange(content.copy(value = v))
            },
            label           = { Text("Value ($)") },
            singleLine      = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier        = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value         = content.purchaseDate,
            onValueChange = { onContentChange(content.copy(purchaseDate = it)) },
            label         = { Text("Purchase date (YYYY-MM-DD)") },
            singleLine    = true,
            modifier      = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value         = content.serialNumber,
            onValueChange = { onContentChange(content.copy(serialNumber = it)) },
            label         = { Text("Serial number") },
            singleLine    = true,
            modifier      = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value         = content.warrantyExpiry,
            onValueChange = { onContentChange(content.copy(warrantyExpiry = it)) },
            label         = { Text("Warranty expiry (YYYY-MM-DD)") },
            singleLine    = true,
            modifier      = Modifier.fillMaxWidth()
        )
    }
}
