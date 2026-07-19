package com.mostawdai.feature.materials

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mostawdai.domain.usecase.GetInventorySummaryUseCase
import com.mostawdai.domain.usecase.InventorySummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MaterialsListViewModel @Inject constructor(
    getInventorySummaryUseCase: GetInventorySummaryUseCase
) : ViewModel() {

    val summary: StateFlow<InventorySummary> = getInventorySummaryUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = InventorySummary(
                materials = emptyList(),
                totalInventoryValue = 0.0,
                lowStockCount = 0,
                totalPaidForMaterials = 0.0
            )
        )
}
