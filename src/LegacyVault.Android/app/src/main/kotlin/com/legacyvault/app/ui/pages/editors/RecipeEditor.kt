package com.legacyvault.app.ui.pages.editors

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.legacyvault.app.domain.model.PageContent

@Composable
fun RecipeEditor(
    content: PageContent.Recipe,
    onContentChange: (PageContent.Recipe) -> Unit,
    modifier: Modifier = Modifier,
    onAddToShoppingList: (() -> Unit)? = null
) {
    var ingredientInput  by remember { mutableStateOf("") }
    var instructionInput by remember { mutableStateOf("") }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {

        // Servings / times
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value         = if (content.servings == 0) "" else content.servings.toString(),
                onValueChange = { onContentChange(content.copy(servings = it.toIntOrNull() ?: 0)) },
                label         = { Text("Servings") },
                singleLine    = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier      = Modifier.weight(1f)
            )
            OutlinedTextField(
                value         = content.prepTime,
                onValueChange = { onContentChange(content.copy(prepTime = it)) },
                label         = { Text("Prep time") },
                singleLine    = true,
                modifier      = Modifier.weight(1f)
            )
            OutlinedTextField(
                value         = content.cookTime,
                onValueChange = { onContentChange(content.copy(cookTime = it)) },
                label         = { Text("Cook time") },
                singleLine    = true,
                modifier      = Modifier.weight(1f)
            )
        }

        HorizontalDivider()

        // Ingredients
        Text("Ingredients", style = MaterialTheme.typography.labelLarge)

        content.ingredients.forEachIndexed { index, item ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text     = "• $item",
                    modifier = Modifier.weight(1f),
                    style    = MaterialTheme.typography.bodyMedium
                )
                IconButton(onClick = {
                    onContentChange(content.copy(ingredients = content.ingredients.filterIndexed { i, _ -> i != index }))
                }) {
                    Icon(Icons.Default.Close, contentDescription = "Remove ingredient")
                }
            }
        }

        OutlinedTextField(
            value         = ingredientInput,
            onValueChange = { ingredientInput = it },
            label         = { Text("Add ingredient") },
            singleLine    = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                val item = ingredientInput.trim()
                if (item.isNotEmpty()) {
                    onContentChange(content.copy(ingredients = content.ingredients + item))
                    ingredientInput = ""
                }
            }),
            modifier = Modifier.fillMaxWidth()
        )

        if (onAddToShoppingList != null && content.ingredients.isNotEmpty()) {
            OutlinedButton(
                onClick  = onAddToShoppingList,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.ShoppingCart,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Text("  Add ingredients to shopping list")
            }
        }

        HorizontalDivider()

        // Instructions
        Text("Instructions", style = MaterialTheme.typography.labelLarge)

        content.instructions.forEachIndexed { index, step ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text     = "${index + 1}. $step",
                    modifier = Modifier.weight(1f),
                    style    = MaterialTheme.typography.bodyMedium
                )
                IconButton(onClick = {
                    onContentChange(content.copy(instructions = content.instructions.filterIndexed { i, _ -> i != index }))
                }) {
                    Icon(Icons.Default.Close, contentDescription = "Remove step")
                }
            }
        }

        OutlinedTextField(
            value         = instructionInput,
            onValueChange = { instructionInput = it },
            label         = { Text("Add step") },
            singleLine    = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                val step = instructionInput.trim()
                if (step.isNotEmpty()) {
                    onContentChange(content.copy(instructions = content.instructions + step))
                    instructionInput = ""
                }
            }),
            modifier = Modifier.fillMaxWidth()
        )

        HorizontalDivider()

        OutlinedTextField(
            value         = content.notes,
            onValueChange = { onContentChange(content.copy(notes = it)) },
            label         = { Text("Notes") },
            modifier      = Modifier.fillMaxWidth()
        )
    }
}
