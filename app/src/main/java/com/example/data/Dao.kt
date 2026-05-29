package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GuidementDao {
    
    // -- Establishments --
    @Query("SELECT * FROM establishments")
    fun getAllEstablishments(): Flow<List<Establishment>>
    
    @Query("SELECT * FROM establishments WHERE id = :id")
    fun getEstablishmentById(id: String): Flow<Establishment?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEstablishments(items: List<Establishment>)

    @Query("UPDATE establishments SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: String, isFavorite: Boolean)

    // -- Reviews --
    @Query("SELECT * FROM reviews WHERE establishmentId = :establishmentId ORDER BY timestamp DESC")
    fun getReviewsForEstablishment(establishmentId: String): Flow<List<Review>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: Review)

    // -- Challenges --
    @Query("SELECT * FROM challenges")
    fun getAllChallenges(): Flow<List<Challenge>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenges(items: List<Challenge>)

    @Query("UPDATE challenges SET isCompleted = 1, imagePath = :imagePath WHERE id = :id")
    suspend fun completeChallenge(id: String, imagePath: String)

    // -- Badges --
    @Query("SELECT * FROM badges")
    fun getAllBadges(): Flow<List<Badge>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBadges(items: List<Badge>)

    @Query("UPDATE badges SET isUnlocked = 1, unlockedAt = :timestamp WHERE id = :id")
    suspend fun unlockBadge(id: String, timestamp: Long)

    // -- User Wallet --
    @Query("SELECT * FROM user_wallet WHERE id = 'primary_user'")
    fun getUserWallet(): Flow<UserWallet?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserWallet(wallet: UserWallet)

    @Query("UPDATE user_wallet SET tokens = :tokens, points = :points WHERE id = 'primary_user'")
    suspend fun updateWallet(tokens: Int, points: Int)

    // -- Token Transactions --
    @Query("SELECT * FROM token_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TokenTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TokenTransaction)
}
