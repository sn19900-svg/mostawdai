package com.mostawdai.feature.profit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mostawdai.domain.usecase.GetProfitOverviewUseCase
import com.mostawdai.domain.usecase.ProfitOverview
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ProfitViewModel @Inject constructor(
    getProfitOverviewUseCase: GetProfitOverviewUseCase
) : ViewModel() {
    val overview: StateFlow<ProfitOverview> = getProfitOverviewUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ProfitOverview(0.0, 0.0, 0.0)
        )
}
