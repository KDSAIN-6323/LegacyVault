package com.legacyvault.app.ui.categories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.legacyvault.app.domain.model.Category
import com.legacyvault.app.domain.model.enums.CategoryType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryCreateEditSheet(
    existing: Category?,
    onDismiss: () -> Unit,
    onSave: (name: String, icon: String, type: CategoryType, vaultPassword: String, passwordHint: String) -> Unit
) {
    val isEditing = existing != null
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name            by rememberSaveable { mutableStateOf(existing?.name ?: "") }
    var icon            by rememberSaveable { mutableStateOf(existing?.icon ?: "📁") }
    var selectedType    by rememberSaveable { mutableStateOf(existing?.type ?: CategoryType.General) }
    var vaultPassword   by rememberSaveable { mutableStateOf("") }
    var passwordHint    by rememberSaveable { mutableStateOf(existing?.passwordHint ?: "") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text  = if (isEditing) "Edit vault" else "New vault",
                style = MaterialTheme.typography.titleLarge
            )

            OutlinedTextField(
                value         = name,
                onValueChange = { name = it },
                label         = { Text("Name") },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value         = icon,
                onValueChange = { icon = it },
                label         = { Text("Icon (emoji)") },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth()
            )

            // Type selector — only shown when creating; type cannot change after creation
            if (!isEditing) {
                Text("Type", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CategoryType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick  = { selectedType = type },
                            label    = { Text(type.name) }
                        )
                    }
                }
            }

            // Vault-specific fields
            if (selectedType == CategoryType.Vault && !isEditing) {
                OutlinedTextField(
                    value         = vaultPassword,
                    onValueChange = { vaultPassword = it },
                    label         = { Text("Vault password") },
                    singleLine    = true,
                    visualTransformation = if (passwordVisible)
                        VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (selectedType == CategoryType.Vault) {
                OutlinedTextField(
                    value         = passwordHint,
                    onValueChange = { passwordHint = it },
                    label         = { Text("Password hint (optional)") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(4.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                    Text("Cancel")
                }
                Button(
                    onClick  = { onSave(name, icon, selectedType, vaultPassword, passwordHint) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (isEditing) "Save" else "Create")
                }
            }
        }
    }
}
