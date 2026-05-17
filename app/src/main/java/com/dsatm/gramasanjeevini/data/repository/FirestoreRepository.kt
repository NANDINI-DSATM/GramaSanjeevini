package com.dsatm.gramasanjeevini.data.repository

import android.util.Log
import com.dsatm.gramasanjeevini.data.model.Shop
import com.dsatm.gramasanjeevini.data.model.StockItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

private const val TAG = "FirestoreRepo"

class FirestoreRepository {

    private val db = FirebaseFirestore.getInstance()
    private val shopsCollection = db.collection("shops")
    private val stockCollection = db.collection("stock")

    // ── Search medicines across all shops ──────────────────────────
    fun searchMedicines(query: String): Flow<List<StockItem>> = callbackFlow {
        val listener = stockCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val results = snapshot?.documents
                    ?.mapNotNull { it.toObject(StockItem::class.java)?.copy(id = it.id) }
                    ?.filter {
                        it.medicineName.contains(query, ignoreCase = true) && it.quantity > 0
                    }
                    ?: emptyList()
                trySend(results)
            }
        awaitClose { listener.remove() }
    }

    // ── Get all life-saving / emergency medicines ──────────────────
    fun getEmergencyMedicines(): Flow<List<StockItem>> = callbackFlow {
        val listener = stockCollection
            .whereEqualTo("lifeSaving", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val results = snapshot?.documents
                    ?.mapNotNull { it.toObject(StockItem::class.java)?.copy(id = it.id) }
                    ?.filter { it.quantity > 0 }
                    ?: emptyList()
                trySend(results)
            }
        awaitClose { listener.remove() }
    }

    // ── Get stock for a specific shop ──────────────────────────────
    fun getShopStock(shopId: String): Flow<List<StockItem>> = callbackFlow {
        val listener = stockCollection
            .whereEqualTo("shopId", shopId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val results = snapshot?.documents
                    ?.mapNotNull { it.toObject(StockItem::class.java)?.copy(id = it.id) }
                    ?: emptyList()
                trySend(results)
            }
        awaitClose { listener.remove() }
    }

    // ── Get medicines expiring within 30 days for a shop ───────────
    fun getExpiryAlerts(shopId: String): Flow<List<StockItem>> = callbackFlow {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val today = calendar.time
        calendar.add(Calendar.DAY_OF_YEAR, 30)
        val thirtyDaysLater = calendar.time

        val listener = stockCollection
            .whereEqualTo("shopId", shopId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val results = snapshot?.documents
                    ?.mapNotNull { it.toObject(StockItem::class.java)?.copy(id = it.id) }
                    ?.filter { item ->
                        try {
                            val expiry = sdf.parse(item.expiryDate)
                            expiry != null && expiry.before(thirtyDaysLater) && expiry.after(today)
                        } catch (e: Exception) {
                            false
                        }
                    }
                    ?.sortedBy { it.expiryDate }
                    ?: emptyList()
                trySend(results)
            }
        awaitClose { listener.remove() }
    }

    // ── Login pharmacist by phone + PIN ────────────────────────────
    suspend fun loginPharmacist(phone: String, pin: String): Shop? {
        return try {
            val snapshot = shopsCollection
                .whereEqualTo("phone", phone)
                .whereEqualTo("pin", pin)
                .get()
                .await()
            snapshot.documents.firstOrNull()
                ?.toObject(Shop::class.java)
                ?.copy(id = snapshot.documents.first().id)
        } catch (e: Exception) {
            Log.e(TAG, "Login failed", e)
            null
        }
    }

    // ── Add or update stock item ───────────────────────────────────
    suspend fun addStockItem(item: StockItem): Boolean {
        return try {
            val docId = if (item.id.isNotEmpty()) item.id else UUID.randomUUID().toString()
            stockCollection.document(docId).set(item.copy(id = docId)).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Add stock failed", e)
            false
        }
    }

    // ── Delete stock item ──────────────────────────────────────────
    suspend fun deleteStockItem(stockItemId: String): Boolean {
        return try {
            stockCollection.document(stockItemId).delete().await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Delete stock failed", e)
            false
        }
    }

    // ── Seed mock data ─────────────────────────────────────────────
    suspend fun seedMockData(): String? {
        return try {
            // Clear existing data
            val existingShops = shopsCollection.get().await()
            for (doc in existingShops.documents) {
                doc.reference.delete().await()
            }
            val existingStock = stockCollection.get().await()
            for (doc in existingStock.documents) {
                doc.reference.delete().await()
            }

            // ── 3 Mock Shops ───────────────────────────────────────
            val shops = listOf(
                Shop(
                    id = "shop1",
                    name = "Grama Pharmacy",
                    location = "Hoskote Village",
                    latitude = 13.0707,
                    longitude = 77.7956,
                    ownerName = "Ramesh Kumar",
                    phone = "9876543210",
                    pin = "1234"
                ),
                Shop(
                    id = "shop2",
                    name = "Village Health Store",
                    location = "Nandagudi Village",
                    latitude = 13.1200,
                    longitude = 77.8500,
                    ownerName = "Suresh Reddy",
                    phone = "9876543211",
                    pin = "5678"
                ),
                Shop(
                    id = "shop3",
                    name = "Rural Medicals",
                    location = "Avathi Village",
                    latitude = 13.0500,
                    longitude = 77.7200,
                    ownerName = "Lakshmi Devi",
                    phone = "9876543212",
                    pin = "9012"
                )
            )

            for (shop in shops) {
                shopsCollection.document(shop.id).set(shop).await()
            }

            // ── Mock Stock Items ───────────────────────────────────
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            // Helper to get date strings
            fun daysFromNow(days: Int): String {
                val c = Calendar.getInstance()
                c.add(Calendar.DAY_OF_YEAR, days)
                return sdf.format(c.time)
            }

            val stockItems = listOf(
                // ── Shop 1: Grama Pharmacy ──
                StockItem("s1m1", "Paracetamol 500mg", "shop1", "Grama Pharmacy", "Hoskote Village", 120, 12.0, daysFromNow(180), false, "Painkiller"),
                StockItem("s1m2", "Amoxicillin 250mg", "shop1", "Grama Pharmacy", "Hoskote Village", 45, 35.0, daysFromNow(90), false, "Antibiotic"),
                StockItem("s1m3", "Insulin Glargine", "shop1", "Grama Pharmacy", "Hoskote Village", 10, 450.0, daysFromNow(25), true, "Diabetes"),
                StockItem("s1m4", "Aspirin 75mg", "shop1", "Grama Pharmacy", "Hoskote Village", 200, 8.0, daysFromNow(365), true, "Cardiac"),
                StockItem("s1m5", "ORS Powder", "shop1", "Grama Pharmacy", "Hoskote Village", 80, 15.0, daysFromNow(5), false, "Rehydration"),
                StockItem("s1m6", "Cetirizine 10mg", "shop1", "Grama Pharmacy", "Hoskote Village", 60, 10.0, daysFromNow(200), false, "Antiallergy"),

                // ── Shop 2: Village Health Store ──
                StockItem("s2m1", "Paracetamol 500mg", "shop2", "Village Health Store", "Nandagudi Village", 80, 10.0, daysFromNow(150), false, "Painkiller"),
                StockItem("s2m2", "Metformin 500mg", "shop2", "Village Health Store", "Nandagudi Village", 30, 25.0, daysFromNow(12), false, "Diabetes"),
                StockItem("s2m3", "Adrenaline Injection", "shop2", "Village Health Store", "Nandagudi Village", 5, 120.0, daysFromNow(60), true, "Emergency"),
                StockItem("s2m4", "Azithromycin 500mg", "shop2", "Village Health Store", "Nandagudi Village", 25, 65.0, daysFromNow(8), false, "Antibiotic"),
                StockItem("s2m5", "Atorvastatin 10mg", "shop2", "Village Health Store", "Nandagudi Village", 40, 30.0, daysFromNow(300), false, "Cardiac"),
                StockItem("s2m6", "Aspirin 75mg", "shop2", "Village Health Store", "Nandagudi Village", 150, 7.0, daysFromNow(200), true, "Cardiac"),

                // ── Shop 3: Rural Medicals ──
                StockItem("s3m1", "Paracetamol 500mg", "shop3", "Rural Medicals", "Avathi Village", 200, 11.0, daysFromNow(240), false, "Painkiller"),
                StockItem("s3m2", "Insulin Glargine", "shop3", "Rural Medicals", "Avathi Village", 8, 480.0, daysFromNow(3), true, "Diabetes"),
                StockItem("s3m3", "Diazepam 5mg", "shop3", "Rural Medicals", "Avathi Village", 15, 18.0, daysFromNow(100), true, "Emergency"),
                StockItem("s3m4", "Amoxicillin 250mg", "shop3", "Rural Medicals", "Avathi Village", 55, 32.0, daysFromNow(20), false, "Antibiotic"),
                StockItem("s3m5", "Omeprazole 20mg", "shop3", "Rural Medicals", "Avathi Village", 70, 22.0, daysFromNow(180), false, "Gastric"),
                StockItem("s3m6", "Salbutamol Inhaler", "shop3", "Rural Medicals", "Avathi Village", 12, 150.0, daysFromNow(15), true, "Respiratory"),
                StockItem("s3m7", "ORS Powder", "shop3", "Rural Medicals", "Avathi Village", 100, 14.0, daysFromNow(270), false, "Rehydration")
            )

            for (item in stockItems) {
                stockCollection.document(item.id).set(item).await()
            }

            null // success
        } catch (e: Exception) {
            Log.e(TAG, "Seed mock data FAILED", e)
            e.message ?: "Unknown error"
        }
    }
}
