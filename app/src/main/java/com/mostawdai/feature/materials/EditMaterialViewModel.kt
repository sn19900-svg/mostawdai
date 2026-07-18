package com.mostawdai.feature.materials

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mostawdai.domain.model.OperationResult
import com.mostawdai.domain.repository.MaterialRepository
import com.mostawdai.domain.usecase.DeleteMaterialUseCase
import com.mostawdai.domain.usecase.UpdateMaterialUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditMaterialUiState(
    val name: String = "",
    val unit: String = "",
    val currentQuantity: String = "",
    val averageCost: String = "",
    val minQuantityAlert: String = "",
    val notes: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val savedSuccessfully: Boolean = false,
    val deletedSuccessfully: Boolean = false
)

@HiltViewModel
class EditMaterialViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val materialRepository: MaterialRepository,
    private val updateMaterialUseCase: UpdateMaterialUseCase,
    private val deleteMaterialUseCase: DeleteMaterialUseCase
) : ViewModel() {

    private val materialId: Long = checkNotNull(savedStateHandle["materialId"])

    private val _uiState = MutableStateFlow(EditMaterialUiState())
    val uiState: StateFlow<EditMaterialUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val material = materialRepository.getMaterialById(materialId)
            if (material != null) {
                _uiState.value = EditMaterialUiState(
                    name = material.name,
                    unit = material.unit,
                    currentQuantity = material.currentQuantity.toString(),
                    averageCost = material.averageCost.toString(),
                    minQuantityAlert = material.minQuantityAlert.toString(),
                    notes = material.notes,
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "المادة غير موجودة")
            }
        }
    }

    fun onNameChange(v: String) { _uiState.value = _uiState.value.copy(name = v, errorMessage = null) }
    fun onUnitChange(v: String) { _uiState.value = _uiState.value.copy(unit = v, errorMessage = null) }
    fun onQuantityChange(v: String) { _uiState.value = _uiState.value.copy(currentQuantity = v, errorMessage = null) }
    fun onAverageCostChange(v: String) { _uiState.value = _uiState.value.copy(averageCost = v, errorMessage = null) }
    fun onMinQuantityChange(v: String) { _uiState.value = _uiState.value.copy(minQuantityAlert = v, errorMessage = null) }
    fun onNotesChange(v: String) { _uiState.value = _uiState.value.copy(notes = v) }

    fun save() {
        val s = _uiState.value
        val quantity = s.currentQuantity.toDoubleOrNull()
        val avgCost = s.averageCost.toDoubleOrNull()
        if (quantity == null || avgCost == null) {
            _uiState.value = s.copy(errorMessage = "الكمية ومتوسط التكلفة يجب أن يكونا أرقاماً صحيحة")
            return
        }
        val minQty = s.minQuantityAlert.toDoubleOrNull() ?: 0.0

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            when (val result = updateMaterialUseCase(materialId, s.name, s.unit, quantity, avgCost, minQty, s.notes)) {
                is OperationResult.Success -> {
                    _uiState.value = _uiState.value.copy(isSaving = false, savedSuccessfully = true)
                }
                is OperationResult.Failure -> {
                    _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = result.message)
                }
            }
        }
    }

    fun delete() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            deleteMaterialUseCase(materialId)
            _uiState.value = _uiState.value.copy(isSaving = false, deletedSuccessfully = true)
        }
    }
}
