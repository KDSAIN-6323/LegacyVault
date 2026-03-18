package com.legacyvault.app.ui.pages.editors

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.legacyvault.app.crypto.PasswordGenerator
import com.legacyvault.app.domain.model.PageContent

@Composable
fun PasswordEditor(
    content: PageContent.Password,
    onContentChange: (PageContent.Password) -> Unit,
    modifier: Modifier = Modifier
) {
    val generator  = remember { PasswordGenerator() }
    val clipboard  = LocalClipboardManager.current
    var showPw     by remember { mutableStateOf(false) }
    val strength   = remember(content.password) { generator.strength(content.password) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {

        OutlinedTextField(
            value         = content.url,
            onValueChange = { onContentChange(content.copy(url = it)) },
            label         = { Text("Website URL") },
            singleLine    = true,
            modifier      = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value         = content.username,
            onValueChange = { onContentChange(content.copy(username = it)) },
            label         = { Text("Username") },
            singleLine    = true,
            trailingIcon  = {
                IconButton(onClick = { clipboard.setText(AnnotatedString(content.username)) }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy username")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Password field with visibility, copy, generate
        OutlinedTextField(
            value               = content.password,
            onValueChange       = { onContentChange(content.copy(password = it)) },
            label               = { Text("Password") },
            singleLine          = true,
            visualTransformation = if (showPw) VisualTransformation.None
                                   else PasswordVisualTransformation(),
            trailingIcon = {
                Row {
                    IconButton(onClick = { showPw = !showPw }) {
                        Icon(
                            if (showPw) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null
                        )
                    }
                    IconButton(onClick = { clipboard.setText(AnnotatedString(content.password)) }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy password")
                    }
                    IconButton(onClick = {
                        onContentChange(content.copy(password = generator.generate()))
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Generate password")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Strength bar
        if (content.password.isNotEmpty()) {
            val fraction = (strength.score / 100f).coerceIn(0f, 1f)
            val color = when (strength.level) {
                PasswordGenerator.Level.Weak      -> MaterialTheme.colorScheme.error
                PasswordGenerator.Level.Fair      -> MaterialTheme.colorScheme.tertiary
                PasswordGenerator.Level.Strong    -> MaterialTheme.colorScheme.primary
                PasswordGenerator.Level.VeryStrong -> MaterialTheme.colorScheme.secondary
            }
            Column {
                LinearProgressIndicator(
                    progress = { fraction },
                    color    = color,
                    modifier = Modifier.fillMaxWidth().height(4.dp)
                )
                Text(
                    text  = strength.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = color
                )
            }
        }

        OutlinedTextField(
            value         = content.totp ?: "",
            onValueChange = { onContentChange(content.copy(totp = it.ifBlank { null })) },
            label         = { Text("TOTP secret (optional)") },
            singleLine    = true,
            modifier      = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value         = content.notes,
            onValueChange = { onContentChange(content.copy(notes = it)) },
            label         = { Text("Notes") },
            modifier      = Modifier.fillMaxWidth()
        )
    }
}
