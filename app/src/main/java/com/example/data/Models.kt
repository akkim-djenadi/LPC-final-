package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "establishments")
data class Establishment(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val category: String, // "RESTAURANT", "BAR", "CAFE", "ACTIVITE"
    val rating: Float,
    val verdict: String, // "Le Verdict" de l'expert
    val address: String,
    val quartier: String, // "Écusson", "Port Marianne", "Antigone", "Beaux-Arts", "Alentours"
    val ambiance: String, // "Chic", "Festif", "Étudiant", "Détendu"
    val isMagnonLabel: Boolean, // Official "Le Magnon" award
    val imageResName: String, // Vector representation code
    val isFavorite: Boolean = false,
    val phoneNumber: String = "+33 4 67 00 00 00",
    val hours: String = "12:00 - 15:00, 19:00 - 23:00",
    val imageUrl: String = "",
    val latitude: Double = 43.6107,
    val longitude: Double = 3.8767
)

@Entity(tableName = "reviews")
data class Review(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val establishmentId: String,
    val userName: String,
    val rating: Float,
    val comment: String,
    val timestamp: Long,
    val isExpert: Boolean = false
)

@Entity(tableName = "challenges")
data class Challenge(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val points: Int,
    val type: String, // "STREET_ART", "PHOTO"
    val locationName: String, // e.g., "Place de la Comédie", "Quartier Écusson"
    val lat: Double, // local simulated coordinate offset from Montpellier Center (e.g., 43.6107)
    val lng: Double,
    val detailLabel: String, // "Monsieur BMX" cycling art code, or Monument details
    val isCompleted: Boolean = false,
    val imagePath: String? = null // Captured photo mockup
)

@Entity(tableName = "badges")
data class Badge(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val iconName: String, // "landmark", "palette", "drink", "token"
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null
)

@Entity(tableName = "user_wallet")
data class UserWallet(
    @PrimaryKey val id: String = "primary_user",
    val tokens: Int = 5, // free starting tokens
    val points: Int = 0
)

@Entity(tableName = "token_transactions")
data class TokenTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Int, // e.g. -1, +5
    val type: String, // "BUY", "SPEND", "REWARD"
    val description: String,
    val timestamp: Long,
    val qrCodeData: String? = null // One-time QR secure voucher data
)
