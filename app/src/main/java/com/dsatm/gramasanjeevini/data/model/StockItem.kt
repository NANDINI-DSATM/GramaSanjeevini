package com.dsatm.gramasanjeevini.data.model

import com.google.firebase.firestore.PropertyName

data class StockItem(
    val id: String = "",
    val medicineName: String = "",
    val shopId: String = "",
    val shopName: String = "",
    val shopLocation: String = "",
    val quantity: Int = 0,
    val price: Double = 0.0,
    val expiryDate: String = "",      // "YYYY-MM-DD"
    @get:PropertyName("lifeSaving")
    @set:PropertyName("lifeSaving")
    var isLifeSaving: Boolean = false,
    val category: String = ""
)
