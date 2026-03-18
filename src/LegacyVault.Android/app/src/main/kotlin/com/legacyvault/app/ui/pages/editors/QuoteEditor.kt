package com.legacyvault.app.ui.pages.editors

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.legacyvault.app.domain.model.PageContent

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuoteEditor(
    content: PageContent.Quote,
    onContentChange: (PageContent.Quote) -> Unit,
    modifier: Modifier = Modifier
) {
    var tagInput by remember { mutableStateOf("") }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {

        OutlinedTextField(
            value         = content.text,
            onValueChange = { onContentChange(content.copy(text = it)) },
            label         = { Text("Quote text") },
            modifier      = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value         = content.author,
            onValueChange = { onContentChange(content.copy(author = it)) },
            label         = { Text("Author") },
            singleLine    = true,
            modifier      = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value         = content.source,
            onValueChange = { onContentChange(content.copy(source = it)) },
            label         = { Text("Source (book, film, speech…)") },
            singleLine    = true,
            modifier      = Modifier.fillMaxWidth()
        )

        // Tag input
        OutlinedTextField(
            value         = tagInput,
            onValueChange = { tagInput = it },
            label         = { Text("Add tag") },
            singleLine    = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                val tag = tagInput.trim()
                if (tag.isNotEmpty() && tag !in content.tags) {
                    onContentChange(content.copy(tags = content.tags + tag))
                }
                tagInput = ""
            }),
            modifier = Modifier.fillMaxWidth()
        )

        if (content.tags.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                content.tags.forEach { tag ->
                    FilterChip(
                        selected      = true,
                        onClick       = { onContentChange(content.copy(tags = content.tags - tag)) },
                        label         = { Text(tag) },
                        trailingIcon  = { Icon(Icons.Default.Close, contentDescription = "Remove") }
                    )
                }
            }
        }
    }
}
