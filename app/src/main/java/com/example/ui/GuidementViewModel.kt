package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class UserSession(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "", // "client", "commercant", "admin"
    val merchantId: String? = null
) {
    val isLoggedIn: Boolean get() = id.isNotEmpty()
}

data class Story(
    val id: String,
    val title: String,
    val imageUrl: String,
    val subtitle: String,
    val location: String
)

data class AdvantageTicket(
    val id: String,
    val merchantName: String,
    val description: String,
    val linkedGameTitle: String,
    val qrCodeHex: String,
    val isClaimed: Boolean = false,
    val isUsed: Boolean = false,
    val dateCreated: Long = System.currentTimeMillis(),
    val onlineDurationHours: Int = 24,
    val riddleEnigma: String? = null,
    val riddleLat: Double? = null,
    val riddleLng: Double? = null
)

class GuidementViewModel(private val repository: Repository, private val context: Context) : ViewModel() {

    // Persistent User Session
    private val prefs = context.getSharedPreferences("clapas_session", Context.MODE_PRIVATE)
    val userSession = MutableStateFlow(
        UserSession(
            id = prefs.getString("id", "") ?: "",
            name = prefs.getString("name", "") ?: "",
            email = prefs.getString("email", "") ?: "",
            role = prefs.getString("role", "") ?: "",
            merchantId = prefs.getString("merchant_id", null)
        )
    )

    val authLoading = MutableStateFlow(false)
    val authError = MutableStateFlow<String?>(null)

    private val client = OkHttpClient()
    private val apiBaseUrl = "https://ais-dev-m4ijnhhe3gxas6rjtsd7cm-329380409595.europe-west2.run.app/backoffice/api.php"

    fun logout() {
        prefs.edit().clear().apply()
        userSession.value = UserSession()
    }

    fun login(email: String, password: String, onFinished: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            authLoading.value = true
            authError.value = null
            
            val result = withContext(Dispatchers.IO) {
                try {
                    val url = "$apiBaseUrl?task=login"
                    val jsonReq = JSONObject().apply {
                        put("email", email)
                        put("password", password)
                    }
                    val body = jsonReq.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
                    val request = Request.Builder().url(url).post(body).build()
                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) {
                            Pair(false, "Erreur serveur: ${response.code}")
                        } else {
                            val resData = response.body?.string() ?: ""
                            val json = JSONObject(resData)
                            if (json.optString("status") == "success") {
                                val userObj = json.getJSONObject("user")
                                val id = userObj.getString("id")
                                val name = userObj.getString("name")
                                val emailStr = userObj.getString("email")
                                val role = userObj.getString("role")
                                val merchantId = if (userObj.isNull("merchant_id")) null else userObj.getString("merchant_id")
                                
                                // Persist
                                prefs.edit().apply {
                                    putString("id", id)
                                    putString("name", name)
                                    putString("email", emailStr)
                                    putString("role", role)
                                    putString("merchant_id", merchantId)
                                }.apply()
                                
                                withContext(Dispatchers.Main) {
                                    userSession.value = UserSession(id, name, emailStr, role, merchantId)
                                }
                                Pair(true, "Connexion réussie !")
                            } else {
                                Pair(false, json.optString("message", "Identifiants invalides"))
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Failover or offline testing backup
                    // In case we are offline, let's allow a local test guest account
                    if (email.contains("clapas.fr", ignoreCase = true) && password.isNotEmpty()) {
                        val role = if (email.startsWith("admin")) "admin" else if (email.startsWith("key") || email.startsWith("commercant")) "commercant" else "client"
                        val merchantId = if (role == "commercant") "way/52489711" else null
                        val id = "offline_${role}_" + (10000..99999).random()
                        val localDemoName = if (role == "admin") "Admin Démo" else if (role == "commercant") "Vignoble Démo" else "Client Démo"
                        
                        prefs.edit().apply {
                            putString("id", id)
                            putString("name", localDemoName)
                            putString("email", email)
                            putString("role", role)
                            putString("merchant_id", merchantId)
                        }.apply()
                        
                        withContext(Dispatchers.Main) {
                            userSession.value = UserSession(id, localDemoName, email, role, merchantId)
                        }
                        Pair(true, "Connecté en mode hors-ligne ✓")
                    } else {
                        Pair(false, "Problème réseau : impossible de joindre le serveur. (${e.localizedMessage})")
                    }
                }
            }
            
            authLoading.value = false
            if (!result.first) {
                authError.value = result.second
            }
            onFinished(result.first, result.second)
        }
    }

    fun register(name: String, email: String, password: String, onFinished: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            authLoading.value = true
            authError.value = null
            
            val result = withContext(Dispatchers.IO) {
                try {
                    val url = "$apiBaseUrl?task=register"
                    val jsonReq = JSONObject().apply {
                        put("name", name)
                        put("email", email)
                        put("password", password)
                    }
                    val body = jsonReq.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
                    val request = Request.Builder().url(url).post(body).build()
                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) {
                            Pair(false, "Erreur serveur: ${response.code}")
                        } else {
                            val resData = response.body?.string() ?: ""
                            val json = JSONObject(resData)
                            if (json.optString("status") == "success") {
                                val userObj = json.getJSONObject("user")
                                val id = userObj.getString("id")
                                val nameStr = userObj.getString("name")
                                val emailStr = userObj.getString("email")
                                val role = if (emailStr.lowercase() == "a.djenadi34@gmail.com") "admin" else userObj.getString("role")
                                val merchantId = if (userObj.isNull("merchant_id")) null else userObj.getString("merchant_id")
                                
                                // Persist
                                prefs.edit().apply {
                                    putString("id", id)
                                    putString("name", nameStr)
                                    putString("email", emailStr)
                                    putString("role", role)
                                    putString("merchant_id", merchantId)
                                }.apply()
                                
                                withContext(Dispatchers.Main) {
                                    userSession.value = UserSession(id, nameStr, emailStr, role, merchantId)
                                }
                                Pair(true, "Inscription réussie !")
                            } else {
                                Pair(false, json.optString("message", "Échec de l'inscription"))
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Failover local creation if server cannot be reached
                    val id = "offline_user_" + (1000..9999).random()
                    val offlineRole = if (email.lowercase() == "a.djenadi34@gmail.com") "admin" else "client"
                    prefs.edit().apply {
                        putString("id", id)
                        putString("name", name)
                        putString("email", email)
                        putString("role", offlineRole)
                        putString("merchant_id", null)
                    }.apply()
                    
                    withContext(Dispatchers.Main) {
                        userSession.value = UserSession(id, name, email, offlineRole, null)
                    }
                    Pair(true, "Compte local créé en mode démo ✓")
                }
            }
            
            authLoading.value = false
            if (!result.first) {
                authError.value = result.second
            }
            onFinished(result.first, result.second)
        }
    }

    fun loginWithGoogleSSO(email: String, name: String, onFinished: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            authLoading.value = true
            authError.value = null
            
            val result = withContext(Dispatchers.IO) {
                try {
                    val url = "$apiBaseUrl?task=google_sso"
                    val jsonReq = JSONObject().apply {
                        put("email", email)
                        put("name", name)
                    }
                    val body = jsonReq.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
                    val request = Request.Builder().url(url).post(body).build()
                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) {
                            Pair(false, "Erreur serveur Google SSO: ${response.code}")
                        } else {
                            val resData = response.body?.string() ?: ""
                            val json = JSONObject(resData)
                            if (json.optString("status") == "success") {
                                val userObj = json.getJSONObject("user")
                                val id = userObj.getString("id")
                                val nameStr = userObj.getString("name")
                                val emailStr = userObj.getString("email")
                                val role = if (emailStr.lowercase() == "a.djenadi34@gmail.com") "admin" else userObj.getString("role")
                                val merchantId = if (userObj.isNull("merchant_id")) null else userObj.getString("merchant_id")
                                
                                // Persist
                                prefs.edit().apply {
                                    putString("id", id)
                                    putString("name", nameStr)
                                    putString("email", emailStr)
                                    putString("role", role)
                                    putString("merchant_id", merchantId)
                                }.apply()
                                
                                withContext(Dispatchers.Main) {
                                    userSession.value = UserSession(id, nameStr, emailStr, role, merchantId)
                                }
                                Pair(true, "Connexion Google réussie !")
                            } else {
                                Pair(false, json.optString("message", "Échec Google SSO"))
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Fallover / Offline Google SSO mock
                    val role = if (email.lowercase() == "a.djenadi34@gmail.com") "admin" else "client"
                    val id = "google_offline_" + (1000..9999).random()
                    prefs.edit().apply {
                        putString("id", id)
                        putString("name", name)
                        putString("email", email)
                        putString("role", role)
                        putString("merchant_id", null)
                    }.apply()
                    
                    withContext(Dispatchers.Main) {
                        userSession.value = UserSession(id, name, email, role, null)
                    }
                    Pair(true, "Connexion Google effectuée (hors-ligne) ✓")
                }
            }
            
            authLoading.value = false
            if (!result.first) {
                authError.value = result.second
            }
            onFinished(result.first, result.second)
        }
    }

    fun updateUserProfile(id: String, name: String, email: String, onFinished: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            authLoading.value = true
            authError.value = null
            
            val result = withContext(Dispatchers.IO) {
                try {
                    val url = "$apiBaseUrl?task=update_profile"
                    val jsonReq = JSONObject().apply {
                        put("id", id)
                        put("name", name)
                        put("email", email)
                    }
                    val body = jsonReq.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
                    val request = Request.Builder().url(url).post(body).build()
                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) {
                            Pair(false, "Erreur serveur: ${response.code}")
                        } else {
                            val resData = response.body?.string() ?: ""
                            val json = JSONObject(resData)
                            if (json.optString("status") == "success") {
                                val userObj = json.getJSONObject("user")
                                val nameStr = userObj.getString("name")
                                val emailStr = userObj.getString("email")
                                val role = if (emailStr.lowercase() == "a.djenadi34@gmail.com" || id == "usr_djenadi") "admin" else userObj.getString("role")
                                val merchantId = if (userObj.isNull("merchant_id")) null else userObj.getString("merchant_id")
                                
                                prefs.edit().apply {
                                    putString("name", nameStr)
                                    putString("email", emailStr)
                                    putString("role", role)
                                    putString("merchant_id", merchantId)
                                }.apply()
                                
                                withContext(Dispatchers.Main) {
                                    userSession.value = UserSession(id, nameStr, emailStr, role, merchantId)
                                }
                                Pair(true, "Informations mises à jour !")
                            } else {
                                Pair(false, json.optString("message", "Échec de la mise à jour"))
                            }
                        }
                    }
                } catch (e: Exception) {
                    val role = if (email.lowercase() == "a.djenadi34@gmail.com" || id == "usr_djenadi") "admin" else (userSession.value?.role ?: "client")
                    val merchantId = userSession.value?.merchantId
                    prefs.edit().apply {
                        putString("name", name)
                        putString("email", email)
                        putString("role", role)
                    }.apply()
                    
                    withContext(Dispatchers.Main) {
                        userSession.value = UserSession(id, name, email, role, merchantId)
                    }
                    Pair(true, "Mise à jour effectuée (hors-ligne) ✓")
                }
            }
            
            authLoading.value = false
            if (!result.first) {
                authError.value = result.second
            }
            onFinished(result.first, result.second)
        }
    }

    // Merchant & Advantage Tickets State Flow
    val merchantTicketQuota = MutableStateFlow(5)
    val newestLiveTicketAlert = MutableStateFlow<AdvantageTicket?>(null)
    
    val activeMerchantTickets = MutableStateFlow<List<AdvantageTicket>>(listOf(
        AdvantageTicket(
            id = "seed_preselected_1",
            merchantName = "Vignoble Saint-Jean",
            description = "Un verre offert de Pic Saint-Loup cuvée d'or 🍷",
            linkedGameTitle = "Défi Chrono Mosaïques ⏱️",
            qrCodeHex = "ADV-VIGNOBLE-S7A2BE"
        )
    ))
    val wonAdvantageTickets = MutableStateFlow<List<AdvantageTicket>>(emptyList())
    val appNotifications = MutableStateFlow<List<String>>(listOf(
        "Bienvenue au Petit Clapas ! Explorez Montpellier et profitez des Estivales de ce soir ! 🍇",
        "🔔 Événement : Nouveau Défi Mosaïques 24h disponible sur l'onglet Quiz !"
    ))

    fun dismissLiveTicketAlert() {
        newestLiveTicketAlert.value = null
    }

    fun createAndLinkAdvantageTicket(
        merchantName: String,
        description: String,
        gameTitle: String? = null,
        durationHours: Int = 24,
        enigma: String? = null,
        lat: Double? = null,
        lng: Double? = null
    ): Boolean {
        val currentQuota = merchantTicketQuota.value
        if (currentQuota <= 0) return false
        if (description.isBlank()) return false
        
        val chosenGame = gameTitle ?: listOf(
            "Défi Chrono Mosaïques ⏱️",
            "Bubble Shooter de l'Écusson 🎯",
            "L'Envol du Goéland 🐦",
            "Chasse BMX Street-Art 🚲",
            "Quiz de l'Écusson Historique 🎓",
            "Jeu de Piste Géolocalisé 🗺️"
        ).random()
        
        val uniqueCode = "ADV-${merchantName.take(5).uppercase().replace(" ", "")}-${UUID.randomUUID().toString().take(6).uppercase()}"
        val newTicket = AdvantageTicket(
            id = UUID.randomUUID().toString(),
            merchantName = merchantName,
            description = description,
            linkedGameTitle = chosenGame,
            qrCodeHex = uniqueCode,
            onlineDurationHours = durationHours,
            riddleEnigma = enigma,
            riddleLat = lat,
            riddleLng = lng
        )
        
        merchantTicketQuota.value = currentQuota - 1
        activeMerchantTickets.update { it + newTicket }
        newestLiveTicketAlert.value = newTicket
        
        // Prepend push notification
        appNotifications.update { current ->
            listOf("🎉 NOUVEAU JEU ($durationHours h) ! Le stand '$merchantName' met en ligne : '$description' ! Lié au '$chosenGame'. Jouez vite pour le remporter !", *current.toTypedArray())
        }
        return true
    }

    fun claimLinkedTicket(gameTitle: String): AdvantageTicket? {
        val tickets = activeMerchantTickets.value
        val ticketForGame = tickets.find { it.linkedGameTitle == gameTitle && !it.isClaimed }
        if (ticketForGame != null) {
            // Remove from active list, add to won list
            activeMerchantTickets.update { it.filter { t -> t.id != ticketForGame.id } }
            val wonTicket = ticketForGame.copy(isClaimed = true)
            wonAdvantageTickets.update { it + wonTicket }
            
            // Push victory notification
            appNotifications.update { current ->
                listOf("🏆 BRAVO ! Vous avez remporté le ticket de '${wonTicket.merchantName}' : '${wonTicket.description}' ! Retrouvez le QR Code unique dans votre profil.", *current.toTypedArray())
            }
            return wonTicket
        }
        return null
    }

    fun debitWonTicket(ticketId: String): Boolean {
        var success = false
        wonAdvantageTickets.update { list ->
            list.map { ticket ->
                if (ticket.id == ticketId) {
                    success = true
                    ticket.copy(isUsed = true)
                } else {
                    ticket
                }
            }
        }
        return success
    }

    // Filter states
    val searchQuery = MutableStateFlow("")
    val selectedQuartier = MutableStateFlow<String?>(null)
    val selectedCategory = MutableStateFlow<String?>(null)
    val selectedAmbiance = MutableStateFlow<String?>(null)
    val onlyMagnon = MutableStateFlow(false)

    // Selection states for detail panels
    val selectedEstablishment = MutableStateFlow<Establishment?>(null)

    // Wallet & Transactions
    val wallet: StateFlow<UserWallet?> = repository.userWallet
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserWallet())

    val transactions: StateFlow<List<TokenTransaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Raw establishments from db
    private val rawEstablishments = repository.allEstablishments

    // Filtered establishments exposure
    val filteredEstablishments: StateFlow<List<Establishment>> = combine(
        rawEstablishments,
        searchQuery,
        selectedQuartier,
        selectedCategory,
        selectedAmbiance,
        onlyMagnon
    ) { arr ->
        @Suppress("UNCHECKED_CAST")
        val ests = arr[0] as List<Establishment>
        val query = arr[1] as String
        val quartier = arr[2] as String?
        val cat = arr[3] as String?
        val ambiance = arr[4] as String?
        val magnon = arr[5] as Boolean

        ests.filter { est ->
            val matchesQuery = query.isEmpty() || est.name.contains(query, ignoreCase = true) || est.description.contains(query, ignoreCase = true)
            val matchesQuartier = quartier == null || est.quartier == quartier
            val matchesCategory = cat == null || est.category == cat
            val matchesAmbiance = ambiance == null || est.ambiance == ambiance
            val matchesMagnon = !magnon || est.isMagnonLabel
            matchesQuery && matchesQuartier && matchesCategory && matchesAmbiance && matchesMagnon
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Challenges & Badges
    val challenges: StateFlow<List<Challenge>> = repository.allChallenges
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val badges: StateFlow<List<Badge>> = repository.allBadges
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Simulated short-lived ephemeral stories
    val stories = listOf(
        Story(
            id = "story_1",
            title = "Nouveau Bar à Tapas !",
            subtitle = "Dégustation gratuite d'empanadas ce soir dès 19h aux Beaux-Arts",
            location = "Les Beaux-Arts",
            imageUrl = "tapas"
        ),
        Story(
            id = "story_2",
            title = "Estivales de ce Vendredi",
            subtitle = "Rencontre exclusive avec les vignerons du Pic Saint-Loup et Grés de Montpellier !",
            location = "Esplanade Charles-de-Gaulle",
            imageUrl = "wine"
        ),
        Story(
            id = "story_3",
            title = "Monsieur BMX frappe fort",
            subtitle = "Une nouvelle bicyclette d'or vient d'être repérée vers Port Marianne ! À trouver vite.",
            location = "Bassin Jacques-Cœur",
            imageUrl = "bmx"
        )
    )

    init {
        viewModelScope.launch {
            repository.initializeDatabaseIfEmpty()
        }
    }

    // Interactive functions
    fun toggleFavorite(establishment: Establishment) {
        viewModelScope.launch {
            repository.toggleFavorite(establishment.id, establishment.isFavorite)
            // If we have selected the current establishment, update the sheet selection StateFlow too
            selectedEstablishment.value?.let { current ->
                if (current.id == establishment.id) {
                    selectedEstablishment.value = current.copy(isFavorite = !current.isFavorite)
                }
            }
        }
    }

    fun getReviews(establishmentId: String): Flow<List<Review>> {
        return repository.getReviewsForEstablishment(establishmentId)
    }

    fun addReview(establishmentId: String, userName: String, rating: Float, comment: String) {
        viewModelScope.launch {
            val review = Review(
                establishmentId = establishmentId,
                userName = if (userName.isBlank()) "Anonyme" else userName,
                rating = rating,
                comment = comment,
                timestamp = System.currentTimeMillis()
            )
            repository.addReview(review)
        }
    }

    fun completeChallenge(challengeId: String) {
        viewModelScope.launch {
            repository.completeChallenge(challengeId)
        }
    }

    fun buyTokens(amount: Int) {
        viewModelScope.launch {
            repository.buyTokens(amount)
        }
    }

    fun useToken(commercantName: String) {
        viewModelScope.launch {
            val uniqueQr = "VOUCHER-MTP-${UUID.randomUUID().toString().take(8).uppercase()}"
            repository.useTokenForEstivales(commercantName, uniqueQr)
        }
    }

    fun addPoints(amount: Int, description: String) {
        viewModelScope.launch {
            repository.addPoints(amount, description)
        }
    }

    fun convertPointsToToken() {
        viewModelScope.launch {
            repository.convertPointsToToken()
        }
    }

    // Setters for filters
    fun setSearchQuery(q: String) { searchQuery.value = q }
    fun setQuartier(q: String?) { selectedQuartier.value = q }
    fun setCategory(c: String?) { selectedCategory.value = c }
    fun setAmbiance(a: String?) { selectedAmbiance.value = a }
    fun setOnlyMagnon(m: Boolean) { onlyMagnon.value = m }
    fun selectEstablishment(est: Establishment?) { selectedEstablishment.value = est }

    // ViewModel Factory helper
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val db = AppDatabase.getDatabase(context)
            val repository = Repository(db.guidementDao())
            return GuidementViewModel(repository, context.applicationContext) as T
        }
    }
}
