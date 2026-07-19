package com.mostawdai.feature.materials

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mostawdai.domain.model.OperationResult
import com.mostawdai.domain.usecase.AddMaterialUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddMaterialUiState(
    val materialNumber: String = "",
    val name: String = "",
    val unit: String = "",
    val minQuantityAlert: String = "",
    val notes: String = "",
    val errorMessage: String? = null,
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false
)

@HiltViewModel
class AddMaterialViewModel @Inject constructor(
    private val addMaterialUseCase: AddMaterialUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddMaterialUiState())
    val uiState: StateFlow<AddMaterialUiState> = _uiState.asStateFlow()

    fun onMaterialNumberChange(value: String) {
        _uiState.value = _uiState.value.copy(materialNumber = value)
    }

    fun onNameChange(value: String) {
        _uiState.value = _uiState.value.copy(name = value, errorMessage = null)
    }

    fun onUnitChange(value: String) {
        _uiState.value = _uiState.value.copy(unit = value, errorMessage = null)
    }

    fun onMinQuantityChange(value: String) {
        _uiState.value = _uiState.value.copy(minQuantityAlert = value, errorMessage = null)
    }

    fun onNotesChange(value: String) {
        _uiState.value = _uiState.value.copy(notes = value)
    }

    fun save() {
        val state = _uiState.value
        val minQty = state.minQuantityAlert.toDoubleOrNull() ?: 0.0

        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true)
            when (val result = addMaterialUseCase(state.name, state.unit, state.materialNumber, minQty, state.notes)) {
                is OperationResult.Success -> {
                    _uiState.value = _uiState.value.copy(isSaving = false, savedSuccessfully = true)
                }
                is OperationResult.Failure -> {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }
}
