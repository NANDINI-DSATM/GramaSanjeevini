package com.dsatm.gramasanjeevini.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dsatm.gramasanjeevini.data.model.Shop
import com.dsatm.gramasanjeevini.data.model.StockItem
import com.dsatm.gramasanjeevini.data.repository.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginState(
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val shop: Shop? = null
)

class PharmacistViewModel : ViewModel() {

    private val repository = FirestoreRepository()

    // ── Login State ────────────────────────────────────────────────
    private val _loginState = MutableStateFlow(LoginState())
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    private val _phone = MutableStateFlow("")
    val phone: StateFlow<String> = _phone.asStateFlow()

    private val _pin = MutableStateFlow("")
    val pin: StateFlow<String> = _pin.asStateFlow()

    // ── Stock State ────────────────────────────────────────────────
    private val _shopStock = MutableStateFlow<List<StockItem>>(emptyList())
    val shopStock: StateFlow<List<StockItem>> = _shopStock.asStateFlow()

    private val _expiryAlerts = MutableStateFlow<List<StockItem>>(emptyList())
    val expiryAlerts: StateFlow<List<StockItem>> = _expiryAlerts.asStateFlow()

    private val _isStockLoading = MutableStateFlow(false)
    val isStockLoading: StateFlow<Boolean> = _isStockLoading.asStateFlow()

    // ── Add/Edit Dialog State ──────────────────────────────────────
    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    private val _editingItem = MutableStateFlow<StockItem?>(null)
    val editingItem: StateFlow<StockItem?> = _editingItem.asStateFlow()

    fun onPhoneChanged(value: String) {
        _phone.value = value
    }

    fun onPinChanged(value: String) {
        if (value.length <= 4) _pin.value = value
    }

    fun login() {
        val phoneVal = _phone.value.trim()
        val pinVal = _pin.value.trim()

        if (phoneVal.isEmpty() || pinVal.length != 4) {
            _loginState.value = LoginState(error = "Enter valid phone number and 4-digit PIN")
            return
        }

        _loginState.value = LoginState(isLoading = true)

        viewModelScope.launch {
            val shop = repository.loginPharmacist(phoneVal, pinVal)
            if (shop != null) {
                _loginState.value = LoginState(isLoggedIn = true, shop = shop)
                loadShopData(shop.id)
            } else {
                _loginState.value = LoginState(error = "Invalid credentials. Please check your phone number and PIN.")
            }
        }
    }

    fun logout() {
        _loginState.value = LoginState()
        _phone.value = ""
        _pin.value = ""
        _shopStock.value = emptyList()
        _expiryAlerts.value = emptyList()
    }

    private fun loadShopData(shopId: String) {
        _isStockLoading.value = true
        viewModelScope.launch {
            launch {
                repository.getShopStock(shopId).collect { stock ->
                    _shopStock.value = stock
                    _isStockLoading.value = false
                }
            }
            launch {
                repository.getExpiryAlerts(shopId).collect { alerts ->
                    _expiryAlerts.value = alerts
                }
            }
        }
    }

    // ── Stock Management ───────────────────────────────────────────
    fun showAddStockDialog(item: StockItem? = null) {
        _editingItem.value = item
        _showAddDialog.value = true
    }

    fun dismissAddStockDialog() {
        _showAddDialog.value = false
        _editingItem.value = null
    }

    fun saveStockItem(item: StockItem) {
        val shop = _loginState.value.shop ?: return
        val itemWithShop = item.copy(
            shopId = shop.id,
            shopName = shop.name,
            shopLocation = shop.location
        )
        viewModelScope.launch {
            repository.addStockItem(itemWithShop)
            dismissAddStockDialog()
        }
    }

    fun deleteStockItem(itemId: String) {
        viewModelScope.launch {
            repository.deleteStockItem(itemId)
        }
    }
}
