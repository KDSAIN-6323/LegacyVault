package com.legacyvault.app.ui.backup

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.legacyvault.app.data.backup.BackupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BackupUiState(
    val isWorking: Boolean    = false,
    val message: String?      = null,
    /** Non-null when JSON is ready and the screen should launch CreateDocument. */
    val pendingExportJson: String? = null
)

@HiltViewModel
class BackupViewModel @Inject constructor(
    application: Application,
    private val backupRepository: BackupRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    // ── Export ────────────────────────────────────────────────────────────────

    /**
     * Reads all data and builds the export JSON.
     * Once complete, sets [BackupUiState.pendingExportJson] — the screen
     * should react by launching a CreateDocument contract to let the user
     * pick a save location, then call [writeExportToUri].
     */
    fun prepareExport() {
        viewModelScope.launch {
            _uiState.update { it.copy(isWorking = true, message = null) }
            runCatching { backupRepository.exportJson() }
                .onSuccess { json ->
                    _uiState.update { it.copy(isWorking = false, pendingExportJson = json) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isWorking = false, message = "Export failed: ${e.message}") }
                }
        }
    }

    /** Called once the system file-picker returns a destination URI. */
    fun writeExportToUri(uri: Uri) {
        val json = _uiState.value.pendingExportJson ?: return
        _uiState.update { it.copy(pendingExportJson = null, isWorking = true) }
        viewModelScope.launch {
            runCatching {
                getApplication<Application>().contentResolver
                    .openOutputStream(uri)
                    ?.use { it.write(json.toByteArray(Charsets.UTF_8)) }
                    ?: error("Could not open output stream")
            }.onSuccess {
                _uiState.update { it.copy(isWorking = false, message = "Backup saved successfully") }
            }.onFailure { e ->
                _uiState.update { it.copy(isWorking = false, message = "Save failed: ${e.message}") }
            }
        }
    }

    fun cancelExport() = _uiState.update { it.copy(pendingExportJson = null) }

    // ── Import ────────────────────────────────────────────────────────────────

    /** Called once the system file-picker returns a source URI. */
    fun importFromUri(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isWorking = true, message = null) }
            runCatching {
                val json = getApplication<Application>().contentResolver
                    .openInputStream(uri)
                    ?.use { it.readBytes().toString(Charsets.UTF_8) }
                    ?: error("Could not read file")
                backupRepository.importJson(json).getOrThrow()
            }.onSuccess {
                _uiState.update { it.copy(isWorking = false, message = "Backup restored successfully") }
            }.onFailure { e ->
                _uiState.update { it.copy(isWorking = false, message = "Import failed: ${e.message}") }
            }
        }
    }

    fun clearMessage() = _uiState.update { it.copy(message = null) }
}
