package com.mostawdai.feature.materials

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mostawdai.domain.model.Material
import com.mostawdai.domain.model.OperationResult
import com.mostawdai.domain.model.StockTransaction
import com.mostawdai.domain.repository.MaterialRepository
import com.mostawdai.domain.repository.TransactionRepository
import com.mostawdai.domain.usecase.StockInUseCase
import com.mostawdai.domain.usecase.StockOutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MaterialDetailUiState(
    val material: Material? = null,
    val transactions: List<StockTransaction> = emptyList(),
    val errorMessage: String? = null,
    val isProcessing: Boolean = false
)

@HiltViewModel
class MaterialDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val materialRepository: MaterialRepository,
    private val transactionRepository: TransactionRepository,
    private val stockInUseCase: StockInUseCase,
    private val stockOutUseCase: StockOutUseCase
) : ViewModel() {

    private val materialId: Long = checkNotNull(savedStateHandle["materialId"])

    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _isProcessing = MutableStateFlow(false)

    val uiState: StateFlow<MaterialDetailUiState> = combine(
        materialRepository.observeAllMaterials(),
        transactionRepository.observeTransactionsForMaterial(materialId),
        _errorMessage,
        _isProcessing
    ) { materials, transactions, error, processing ->
        MaterialDetailUiState(
            material = materials.find { it.id == materialId },
            transactions = transactions,
            errorMessage = error,
            isProcessing = processing
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MaterialDetailUiState()
    )

    fun stockIn(quantity: Double, unitCost: Double, note: String) {
        viewModelScope.launch {
            _isProcessing.value = true
            _errorMessage.value = null
            when (val result = stockInUseCase(materialId, quantity, unitCost, note)) {
                is OperationResult.Failure -> _errorMessage.value = result.message
                is OperationResult.Success -> {}
            }
            _isProcessing.value = false
        }
    }

    fun stockOut(quantity: Double, sellingPricePerUnit: Double?, note: String) {
        viewModelScope.launch {
            _isProcessing.value = true
            _errorMessage.value = null
            when (val result = stockOutUseCase(materialId, quantity, sellingPricePerUnit, note)) {
                is OperationResult.Failure -> _errorMessage.value = result.message
                is OperationResult.Success -> {}
            }
            _isProcessing.value = false
        }
    }

    fun dismissError() {
        _errorMessage.value = null
    }
}
