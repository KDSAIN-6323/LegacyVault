package com.legacyvault.app.ui.pages.editors

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.legacyvault.app.domain.model.PageContent

@Composable
fun NoteEditor(
    content: PageContent.Note,
    onContentChange: (PageContent.Note) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value         = content.body,
        onValueChange = { onContentChange(content.copy(body = it)) },
        placeholder   = { Text("Start writing…") },
        colors        = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor   = Color.Transparent
        ),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 300.dp)
    )
}
