package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Repository(private val dao: GuidementDao) {

    val allEstablishments: Flow<List<Establishment>> = dao.getAllEstablishments()
    val allChallenges: Flow<List<Challenge>> = dao.getAllChallenges()
    val allBadges: Flow<List<Badge>> = dao.getAllBadges()
    val userWallet: Flow<UserWallet?> = dao.getUserWallet()
    val allTransactions: Flow<List<TokenTransaction>> = dao.getAllTransactions()

    fun getReviewsForEstablishment(id: String): Flow<List<Review>> {
        return dao.getReviewsForEstablishment(id)
    }

    suspend fun getEstablishmentById(id: String): Establishment? {
        return withContext(Dispatchers.IO) {
            dao.getEstablishmentById(id).firstOrNull()
        }
    }

    suspend fun toggleFavorite(id: String, currentFavorite: Boolean) {
        withContext(Dispatchers.IO) {
            dao.updateFavorite(id, !currentFavorite)
        }
    }

    suspend fun insertEstablishments(items: List<Establishment>) {
        withContext(Dispatchers.IO) {
            dao.insertEstablishments(items)
        }
    }

    suspend fun addReview(review: Review) {
        withContext(Dispatchers.IO) {
            dao.insertReview(review)
            
            // Add reviews can give points
            val wallet = dao.getUserWallet().firstOrNull() ?: UserWallet()
            val newPoints = wallet.points + 10
            dao.updateWallet(wallet.tokens, newPoints)
            
            // Log reward transaction
            dao.insertTransaction(
                TokenTransaction(
                    amount = 10,
                    type = "REWARD",
                    description = "Avis rédigé pour ${getEstablishmentNameSync(review.establishmentId)}",
                    timestamp = System.currentTimeMillis()
                )
            )
            
            // Check review badge
            dao.unlockBadge("bd_critic", System.currentTimeMillis())
            checkPointsBadges(newPoints)
        }
    }

    private suspend fun getEstablishmentNameSync(id: String): String {
        return dao.getEstablishmentById(id).firstOrNull()?.name ?: "Établissement"
    }

    suspend fun completeChallenge(id: String) {
        withContext(Dispatchers.IO) {
            dao.completeChallenge(id, "mocked_path_to_image_challenge_${id}.png")
            
            // Reward points
            val challenges = dao.getAllChallenges().firstOrNull() ?: emptyList()
            val target = challenges.find { it.id == id }
            val ptsAwarded = target?.points ?: 20
            
            val wallet = dao.getUserWallet().firstOrNull() ?: UserWallet()
            val newPoints = wallet.points + ptsAwarded
            dao.updateWallet(wallet.tokens, newPoints)
            
            // Save transaction log for points
            dao.insertTransaction(
                TokenTransaction(
                    amount = ptsAwarded,
                    type = "REWARD",
                    description = "Défi accompli: ${target?.title ?: "Défi"}",
                    timestamp = System.currentTimeMillis()
                )
            )

            // Unlock Street Art Badge if it's street-art style
            if (target?.type == "STREET_ART") {
                dao.unlockBadge("bd_explorer", System.currentTimeMillis())
            } else {
                dao.unlockBadge("bd_photopro", System.currentTimeMillis())
            }

            checkPointsBadges(newPoints)
        }
    }

    private suspend fun checkPointsBadges(newPoints: Int) {
        if (newPoints >= 100) {
            dao.unlockBadge("bd_pro", System.currentTimeMillis())
        }
        if (newPoints >= 150) {
            dao.unlockBadge("bd_soleil", System.currentTimeMillis())
        }
    }

    suspend fun buyTokens(amount: Int) {
        withContext(Dispatchers.IO) {
            val wallet = dao.getUserWallet().firstOrNull() ?: UserWallet()
            val newTokens = wallet.tokens + amount
            dao.updateWallet(newTokens, wallet.points)

            dao.insertTransaction(
                TokenTransaction(
                    amount = amount,
                    type = "BUY",
                    description = "Achat de $amount jetons Estivales",
                    timestamp = System.currentTimeMillis()
                )
            )

            dao.unlockBadge("bd_estivales", System.currentTimeMillis())
        }
    }

    suspend fun useTokenForEstivales(commercantName: String, qrCode: String) {
        withContext(Dispatchers.IO) {
            val wallet = dao.getUserWallet().firstOrNull() ?: UserWallet()
            if (wallet.tokens > 0) {
                val newTokens = wallet.tokens - 1
                dao.updateWallet(newTokens, wallet.points)

                dao.insertTransaction(
                    TokenTransaction(
                        amount = -1,
                        type = "SPEND",
                        description = "Jeton validé chez $commercantName aux Estivales",
                        timestamp = System.currentTimeMillis(),
                        qrCodeData = qrCode
                    )
                )
            }
        }
    }

    suspend fun addPoints(amount: Int, description: String) {
        withContext(Dispatchers.IO) {
            val wallet = dao.getUserWallet().firstOrNull() ?: UserWallet()
            val newPoints = wallet.points + amount
            dao.updateWallet(wallet.tokens, newPoints)

            dao.insertTransaction(
                TokenTransaction(
                    amount = amount,
                    type = "REWARD",
                    description = description,
                    timestamp = System.currentTimeMillis()
                )
            )

            checkPointsBadges(newPoints)
        }
    }

    suspend fun convertPointsToToken() {
        withContext(Dispatchers.IO) {
            val wallet = dao.getUserWallet().firstOrNull() ?: UserWallet()
            if (wallet.points >= 100) {
                val newPoints = wallet.points - 100
                val newTokens = wallet.tokens + 1
                dao.updateWallet(newTokens, newPoints)

                dao.insertTransaction(
                    TokenTransaction(
                        amount = 1,
                        type = "BUY",
                        description = "Échange de 100 Points contre 1 Jeton",
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    suspend fun initializeDatabaseIfEmpty() {
        withContext(Dispatchers.IO) {
            val ests = dao.getAllEstablishments().firstOrNull()
            if (ests.isNullOrEmpty()) {
                // Prepopulate Wallet
                dao.insertUserWallet(UserWallet(tokens = 5, points = 0))

                // Prepopulate Establishments
                dao.insertEstablishments(
                    listOf(
                        Establishment(
                            id = "est_grillardin",
                            name = "Le Grillardin",
                            description = "Une véritable institution culinaire montpelliéraine au cœur de l'historique Écusson. Viandes savoureuses cuites au feu de bois et sélection de vins du Languedoc.",
                            category = "RESTAURANT",
                            rating = 4.8f,
                            verdict = "Le Grillardin nous régale depuis des décennies. Les Clapassiers s'y retrouvent les yeux fermés pour une côte de bœuf ou une grillade à la perfection. Exceptionnel, convivial et indémodable !",
                            address = "3 Rue des Teissiers, 34000 Montpellier",
                            quartier = "Écusson",
                            ambiance = "Détendu",
                            isMagnonLabel = true,
                            imageResName = "meat",
                            phoneNumber = "+33 4 67 66 24 10",
                            hours = "12:00 - 14:00, 19:30 - 22:30",
                            imageUrl = "https://raw.githubusercontent.com/akkim-djenadi/LPC-final-/main/images_commerces/le_grillardin.jpg",
                            latitude = 43.6105,
                            longitude = 3.8755
                        ),
                        Establishment(
                            id = "est_diligence",
                            name = "La Diligence",
                            description = "Cuisine gastronomique raffinée servie sous de sublimes voûtes en pierre datant du XIVe siècle. Un cadre majestueux et intemporel pour une soirée inoubliable.",
                            category = "RESTAURANT",
                            rating = 4.9f,
                            verdict = "Un décor moyenâgeux d'une beauté rare combiné à une cuisine d'une justesse infinie. Notre coup de cœur absolu pour célébrer un moment unique à Montpellier.",
                            address = "2 Place de la Chapelle Neuve, 34000 Montpellier",
                            quartier = "Écusson",
                            ambiance = "Chic",
                            isMagnonLabel = true,
                            imageResName = "wine",
                            phoneNumber = "+33 4 67 66 12 21",
                            hours = "19:30 - 22:00",
                            imageUrl = "https://raw.githubusercontent.com/akkim-djenadi/LPC-final-/main/images_commerces/la_diligence.jpg",
                            latitude = 43.6120,
                            longitude = 3.8785
                        ),
                        Establishment(
                            id = "est_barbote",
                            name = "La Barbote",
                            description = "Micro-brasserie artisanale dynamique de l'Écusson. Plusieurs bières brassées sur place, frites maison croustillantes et ambiance étudiante ultra festive.",
                            category = "BAR",
                            rating = 4.7f,
                            verdict = "La Barbote excelle dans l'art de la bière locale ! L'ambiance est conviviale, le service est extrêmement chaleureux, et on prend un plaisir immense à déguster leurs créations brassicoles.",
                            address = "8 Rue des Multipliants, 34000 Montpellier",
                            quartier = "Écusson",
                            ambiance = "Étudiant",
                            isMagnonLabel = true,
                            imageResName = "beer",
                            phoneNumber = "+33 4 67 58 75 41",
                            hours = "17:00 - 01:00",
                            imageUrl = "https://raw.githubusercontent.com/akkim-djenadi/LPC-final-/main/images_commerces/la_barbote.jpg",
                            latitude = 43.6068,
                            longitude = 3.8760
                        ),
                        Establishment(
                            id = "est_cafe_mer",
                            name = "Le Café de la Mer",
                            description = "Le spot de Port Marianne le plus convoité des beaux jours face au Bassin Jacques Cœur. Cuisine légère méditerranéenne et bar à cocktails branché.",
                            category = "BAR",
                            rating = 4.5f,
                            verdict = "Vue imprenable, décoration épurée moderne, et cocktails de haute volée. C'est le spot idéal pour contempler le coucher de soleil après une journée de cours ou de boulot.",
                            address = "65 Bassin Jacques Cœur, 34000 Montpellier",
                            quartier = "Port Marianne",
                            ambiance = "Festif",
                            isMagnonLabel = false,
                            imageResName = "cocktail",
                            phoneNumber = "+33 4 67 15 15 15",
                            hours = "10:00 - 01:00",
                            imageUrl = "https://raw.githubusercontent.com/akkim-djenadi/LPC-final-/main/images_commerces/le_café_de_la_mer.jpg",
                            latitude = 43.5995,
                            longitude = 3.8975
                        ),
                        Establishment(
                            id = "est_bonobo",
                            name = "Bonobo Cafe",
                            description = "Boutique-café de spécialité réputée pour son café d'origine, ses pancakes incroyablement généreux et son brunch complet d'inspiration anglo-saxonne.",
                            category = "CAFE",
                            rating = 4.6f,
                            verdict = "C'est indiscutablement le temple du brunch décontracté à Montpellier. Leur granola maison et les pancakes empilés à la perfection justifient pleinement l'attente le week-end !",
                            address = "46 Rue de l'Université, 34000 Montpellier",
                            quartier = "Écusson",
                            ambiance = "Étudiant",
                            isMagnonLabel = false,
                            imageResName = "coffee",
                            phoneNumber = "+33 4 67 55 99 11",
                            hours = "09:00 - 17:00",
                            imageUrl = "https://raw.githubusercontent.com/akkim-djenadi/LPC-final-/main/images_commerces/bonobo_cafe.jpg",
                            latitude = 43.6142,
                            longitude = 3.8795
                        ),
                        Establishment(
                            id = "est_pates",
                            name = "Le Jardin des Pâtes",
                            description = "Dans le quartier chaleureux et bohème des Beaux-Arts, un restaurant pittoiresque servant d'exquises pâtes bio faites maison dans un charmant jardin arboré.",
                            category = "RESTAURANT",
                            rating = 4.4f,
                            verdict = "On adore ce jardin caché en plein cœur des Beaux-Arts. Des assiettes de pâtes généreuses issues de blés anciens. Une parenthèse poétique et gourmande loin du tumulte.",
                            address = "14 Rue de la Cavalerie, 34000 Montpellier",
                            quartier = "Beaux-Arts",
                            ambiance = "Détendu",
                            isMagnonLabel = false,
                            imageResName = "pasta",
                            phoneNumber = "+33 4 67 60 77 66",
                            hours = "12:00 - 14:30, 19:30 - 22:30",
                            imageUrl = "https://raw.githubusercontent.com/akkim-djenadi/LPC-final-/main/images_commerces/le_jardin_des_pâtes.jpg",
                            latitude = 43.6186,
                            longitude = 3.8824
                        ),
                        Establishment(
                            id = "est_antigone",
                            name = "Brasserie du Corum",
                            description = "Café & Brasserie à l'architecture néo-classique surplombant Antigone. Terrasse ensoleillée parfaite pour observer le dynamisme de la ville.",
                            category = "CAFE",
                            rating = 4.2f,
                            verdict = "Une halte agréable et rafraîchissante juste en face du Corum pour se poser après une longue marche sur l'Esplanade Charles-de-Gaulle.",
                            address = "Esplanade Charles-de-Gaulle, 34000 Montpellier",
                            quartier = "Antigone",
                            ambiance = "Détendu",
                            isMagnonLabel = false,
                            imageResName = "coffee",
                            phoneNumber = "+33 4 67 02 02 02",
                            hours = "08:00 - 20:00",
                            imageUrl = "https://raw.githubusercontent.com/akkim-djenadi/LPC-final-/main/images_commerces/brasserie_du_corum.jpg",
                            latitude = 43.6081,
                            longitude = 3.8872
                        )
                    )
                )

                // Prepopulate Reviews
                dao.insertReview(Review(id = 1, establishmentId = "est_grillardin", userName = "Hugo (Label Magnon)", rating = 5f, comment = "Simplement la meilleure entrecôte de l'Hérault ! Le service est chaleureux et la sélection de vins de chez nous est superbe.", timestamp = System.currentTimeMillis() - 432000000L, isExpert = true))
                dao.insertReview(Review(id = 2, establishmentId = "est_grillardin", userName = "Clara34", rating = 4f, comment = "Très bon, ambiance rustique très agréable. Pensez à réserver !", timestamp = System.currentTimeMillis() - 86400000L, isExpert = false))
                dao.insertReview(Review(id = 3, establishmentId = "est_diligence", userName = "Amandine L.", rating = 5f, comment = "Un moment hors du temps. La cuisine associe tradition et une pointe d'audace moléculaire. Féerique.", timestamp = System.currentTimeMillis() - 172800000L, isExpert = true))
                dao.insertReview(Review(id = 4, establishmentId = "est_barbote", userName = "Matthieu_Stud", rating = 5f, comment = "La bière 'La Meute' rousse est incroyable ! Excellent rapport qualité-prix étudiant.", timestamp = System.currentTimeMillis() - 259200000L, isExpert = false))
                dao.insertReview(Review(id = 5, establishmentId = "est_cafe_mer", userName = "PlageLover", rating = 4f, comment = "Cocktail signature passion délicieux. Idéal le vendredi soir !", timestamp = System.currentTimeMillis() - 22000000L, isExpert = false))

                // Prepopulate Challenges
                dao.insertChallenges(
                    listOf(
                        Challenge(
                            id = "ch_street_1",
                            title = "Le Vélo Jaune de Monsieur BMX",
                            description = "Rassemblez votre sens de l'observation pour repérer la célèbre demi-bicyclette jaune incrustée dans un mur de pierre historique en haut des escaliers près des Arceaux.",
                            points = 50,
                            type = "STREET_ART",
                            locationName = "Rue de l'Ancien Courrier",
                            lat = 43.6080,
                            lng = 3.8760,
                            detailLabel = "Monsieur BMX - Jaune"
                        ),
                        Challenge(
                            id = "ch_street_2",
                            title = "La bicyclette d'Antigone",
                            description = "Découvrez le vélo rose fushia suspendu au milieu des colonnes néo-classiques conçues par Ricardo Bofill, offrant un contraste de couleurs saisissant.",
                            points = 60,
                            type = "STREET_ART",
                            locationName = "Esplanade de l'Europe",
                            lat = 43.6075,
                            lng = 3.8965,
                            detailLabel = "Monsieur BMX - Rose"
                        ),
                        Challenge(
                            id = "ch_photo_1",
                            title = "La Fontaine des Trois Grâces",
                            description = "Prenez en photo la statue emblématique qui trône fièrement sur la Place de la Comédie au lever ou coucher du soleil.",
                            points = 30,
                            type = "PHOTO",
                            locationName = "Place de la Comédie",
                            lat = 43.6085,
                            lng = 3.8795,
                            detailLabel = "Opéra Comédie"
                        ),
                        Challenge(
                            id = "ch_photo_2",
                            title = "L'Oasis du Peyrou",
                            description = "Photographiez l'alignement majestueux du Château d'eau historique au bout de l'aqueduc Saint-Clément depuis la perspective des grilles dorées.",
                            points = 40,
                            type = "PHOTO",
                            locationName = "Promenade du Peyrou",
                            lat = 43.6110,
                            lng = 3.8710,
                            detailLabel = "Château d'eau du Peyrou"
                        )
                    )
                )

                // Prepopulate Badges
                dao.insertBadges(
                    listOf(
                        Badge(
                            id = "bd_explorer",
                            title = "Chasseur de BMX",
                            description = "Débloqué en complétant votre premier défi de street-art montpelliérain.",
                            iconName = "palette"
                        ),
                        Badge(
                            id = "bd_photopro",
                            title = "Œil de Clapas",
                            description = "Débloqué en prenant l'un des glorieux monuments de la ville en photo.",
                            iconName = "landmark"
                        ),
                        Badge(
                            id = "bd_critic",
                            title = "Clapassier d'Honneur",
                            description = "Décerné aux utilisateurs engagés qui partagent leur verdict critique sur les adresses.",
                            iconName = "star"
                        ),
                        Badge(
                            id = "bd_estivales",
                            title = "Sommelier des Estivales",
                            description = "Acheté ou validé vos premiers jetons de dégustation des Estivales.",
                            iconName = "token"
                        ),
                        Badge(
                            id = "bd_pro",
                            title = "Explorateur de Bronze",
                            description = "Atteindre 100 points de réputation d'exploration locale.",
                            iconName = "bronze"
                        ),
                        Badge(
                            id = "bd_soleil",
                            title = "Général du Soleil",
                            description = "Devenez une légende en atteignant 150 points d'exploration montpelliéraine.",
                            iconName = "gold"
                        )
                    )
                )
            }
        }
    }
}
