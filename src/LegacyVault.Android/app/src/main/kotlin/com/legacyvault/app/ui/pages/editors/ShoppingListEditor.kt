package com.legacyvault.app.ui.pages.editors

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.legacyvault.app.domain.model.PageContent
import com.legacyvault.app.domain.model.ShoppingListItem
import java.util.UUID

@Composable
fun ShoppingListEditor(
    content: PageContent.ShoppingList,
    onContentChange: (PageContent.ShoppingList) -> Unit,
    modifier: Modifier = Modifier
) {
    var itemInput    by remember { mutableStateOf("") }
    var quantityInput by remember { mutableStateOf("") }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {

        // Existing items
        content.items.forEachIndexed { index, item ->
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier              = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked         = item.checked,
                    onCheckedChange = { checked ->
                        val updated = content.items.toMutableList()
                        updated[index] = item.copy(checked = checked)
                        onContentChange(content.copy(items = updated))
                    }
                )
                Text(
                    text     = if (item.quantity.isNotBlank()) "${item.name} (${item.quantity})" else item.name,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {
                    onContentChange(content.copy(items = content.items.toMutableList().also { it.removeAt(index) }))
                }) {
                    Icon(Icons.Default.Close, contentDescription = "Remove item")
                }
            }
        }

        if (content.items.isNotEmpty()) HorizontalDivider()

        // Add item row
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value         = itemInput,
                onValueChange = { itemInput = it },
                label         = { Text("Item") },
                singleLine    = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    val name = itemInput.trim()
                    if (name.isNotEmpty()) {
                        onContentChange(content.copy(
                            items = content.items + ShoppingListItem(
                                id       = UUID.randomUUID().toString(),
                                name     = name,
                                quantity = quantityInput.trim()
                            )
                        ))
                        itemInput     = ""
                        quantityInput = ""
                    }
                }),
                modifier = Modifier.weight(2f)
            )
            OutlinedTextField(
                value         = quantityInput,
                onValueChange = { quantityInput = it },
                label         = { Text("Qty") },
                singleLine    = true,
                modifier      = Modifier.weight(1f)
            )
        }

        HorizontalDivider()

        OutlinedTextField(
            value         = content.notes,
            onValueChange = { onContentChange(content.copy(notes = it)) },
            label         = { Text("Notes") },
            modifier      = Modifier.fillMaxWidth()
        )
    }
}
