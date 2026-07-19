package com.mostawdai.feature.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mostawdai.domain.model.OperationResult
import com.mostawdai.domain.repository.ExportRepository
import com.mostawdai.domain.repository.ImportSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream
import javax.inject.Inject

data class BackupUiState(
    val isBusy: Boolean = false,
    val message: String? = null,
    val fileToShare: File? = null,
    val shareMimeType: String? = null
)

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val exportRepository: ExportRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    fun exportExcel() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBusy = true)
            val file = exportRepository.exportInventoryToExcel()
            _uiState.value = _uiState.value.copy(
                isBusy = false,
                fileToShare = file,
                shareMimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            )
        }
    }

    fun exportPdf() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBusy = true)
            val file = exportRepository.exportInventoryToPdf()
            _uiState.value = _uiState.value.copy(
                isBusy = false,
                fileToShare = file,
                shareMimeType = "application/pdf"
            )
        }
    }

    fun exportFullBackup() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBusy = true)
            val file = exportRepository.exportFullBackup()
            _uiState.value = _uiState.value.copy(
                isBusy = false,
                fileToShare = file,
                shareMimeType = "application/json"
            )
        }
    }

    fun importFullBackup(inputStream: InputStream) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBusy = true)
            when (val result = exportRepository.importFullBackup(inputStream)) {
                is OperationResult.Success -> {
                    val summary: ImportSummary = result.data
                    _uiState.value = _uiState.value.copy(
                        isBusy = false,
                        message = "تم الاستيراد بنجاح: ${summary.materialsImported} مادة، ${summary.transactionsImported} حركة"
                    )
                }
                is OperationResult.Failure -> {
                    _uiState.value = _uiState.value.copy(isBusy = false, message = result.message)
                }
            }
        }
    }

    fun reportMessage(msg: String) {
        _uiState.value = _uiState.value.copy(message = msg)
    }

    fun consumeShareFile() {
        _uiState.value = _uiState.value.copy(fileToShare = null, shareMimeType = null)
    }

    fun consumeMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}
