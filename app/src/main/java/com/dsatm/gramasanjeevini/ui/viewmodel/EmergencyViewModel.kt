package com.dsatm.gramasanjeevini.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dsatm.gramasanjeevini.data.model.StockItem
import com.dsatm.gramasanjeevini.data.repository.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EmergencyViewModel : ViewModel() {

    private val repository = FirestoreRepository()

    private val _emergencyMedicines = MutableStateFlow<List<StockItem>>(emptyList())
    val emergencyMedicines: StateFlow<List<StockItem>> = _emergencyMedicines.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadEmergencyMedicines()
    }

    private fun loadEmergencyMedicines() {
        viewModelScope.launch {
            repository.getEmergencyMedicines().collect { medicines ->
                _emergencyMedicines.value = medicines
                _isLoading.value = false
            }
        }
    }
}
