package com.mostawdai.feature.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mostawdai.domain.repository.UpdateInfo
import com.mostawdai.domain.repository.UpdateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val updateRepository: UpdateRepository
) : ViewModel() {

    private val _updateInfo = MutableStateFlow<UpdateInfo?>(null)
    val updateInfo: StateFlow<UpdateInfo?> = _updateInfo.asStateFlow()

    private val _isChecking = MutableStateFlow(false)
    val isChecking: StateFlow<Boolean> = _isChecking.asStateFlow()

    init { checkForUpdate() }

    fun checkForUpdate() {
        viewModelScope.launch {
            _isChecking.value = true
            _updateInfo.value = updateRepository.checkForUpdate()
            _isChecking.value = false
        }
    }
}
