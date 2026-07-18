package com.mostawdai.feature.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mostawdai.domain.repository.ExportRepository
import com.mostawdai.domain.usecase.GetProfitReportUseCase
import com.mostawdai.domain.usecase.ProfitReport
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar
import javax.inject.Inject

data class SalesReportUiState(
    val startDate: Long,
    val endDate: Long,
    val report: ProfitReport? = null,
    val isLoading: Boolean = false,
    val fileToShare: File? = null,
    val shareMimeType: String? = null
)

@HiltViewModel
class SalesReportViewModel @Inject constructor(
    private val getProfitReportUseCase: GetProfitReportUseCase,
    private val exportRepository: ExportRepository
) : ViewModel() {

    private fun startOfThisMonthMillis(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun endOfTodayMillis(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59); cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }

    private val _uiState = MutableStateFlow(
        SalesReportUiState(startDate = startOfThisMonthMillis(), endDate = endOfTodayMillis())
    )
    val uiState: StateFlow<SalesReportUiState> = _uiState.asStateFlow()

    init { loadReport() }

    fun setStartDate(millis: Long) {
        _uiState.value = _uiState.value.copy(startDate = millis)
        loadReport()
    }

    fun setEndDate(millis: Long) {
        _uiState.value = _uiState.value.copy(endDate = millis)
        loadReport()
    }

    private fun loadReport() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val report = getProfitReportUseCase(_uiState.value.startDate, _uiState.value.endDate)
            _uiState.value = _uiState.value.copy(isLoading = false, report = report)
        }
    }

    fun exportExcel() {
        viewModelScope.launch {
            val s = _uiState.value
            val file = exportRepository.exportSalesReportToExcel(s.startDate, s.endDate)
            _uiState.value = _uiState.value.copy(
                fileToShare = file,
                shareMimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            )
        }
    }

    fun exportPdf() {
        viewModelScope.launch {
            val s = _uiState.value
            val file = exportRepository.exportSalesReportToPdf(s.startDate, s.endDate)
            _uiState.value = _uiState.value.copy(
                fileToShare = file,
                shareMimeType = "application/pdf"
            )
        }
    }

    fun consumeShareFile() {
        _uiState.value = _uiState.value.copy(fileToShare = null, shareMimeType = null)
    }
}
