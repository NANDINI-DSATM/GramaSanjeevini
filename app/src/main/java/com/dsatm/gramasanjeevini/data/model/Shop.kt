package com.dsatm.gramasanjeevini.data.model

import com.google.firebase.firestore.PropertyName

data class Shop(
    val id: String = "",
    val name: String = "",
    val location: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val ownerName: String = "",
    val phone: String = "",
    val pin: String = ""  // 4-digit PIN for pharmacist login
)
