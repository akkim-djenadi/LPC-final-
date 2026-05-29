package com.example.ui

import java.util.UUID
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.viewinterop.AndroidView

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Challenge
import com.example.data.Establishment
import com.example.data.Review
import com.example.data.UserWallet
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// Selections navigation helper
enum class AppTab {
    ACCUEIL, CARTE, JEUX, AGENDA, PROFIL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLayout(viewModel: GuidementViewModel) {
    var currentTab by remember { mutableStateOf(AppTab.ACCUEIL) }
    val selectedEst by viewModel.selectedEstablishment.collectAsStateWithLifecycle()
    var activeStory by remember { mutableStateOf<Story?>(null) }
    
    val wallet by viewModel.wallet.collectAsStateWithLifecycle()
    val wonAdvantageTickets by viewModel.wonAdvantageTickets.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                contentColor = MontpellierNavyDark,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == AppTab.ACCUEIL,
                    onClick = { currentTab = AppTab.ACCUEIL },
                    icon = { Icon(if (currentTab == AppTab.ACCUEIL) Icons.Filled.Home else Icons.Outlined.Home, contentDescription = "Accueil") },
                    label = { Text("Explorer") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MontpellierOrangePrimary,
                        unselectedIconColor = MontpellierNavyLight,
                        selectedTextColor = MontpellierOrangePrimary,
                        unselectedTextColor = MontpellierNavyLight,
                        indicatorColor = MontpellierOrangeLight
                    )
                )
                NavigationBarItem(
                    selected = currentTab == AppTab.CARTE,
                    onClick = { currentTab = AppTab.CARTE },
                    icon = { Icon(if (currentTab == AppTab.CARTE) Icons.Filled.Map else Icons.Outlined.Map, contentDescription = "Carte") },
                    label = { Text("Carte") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MontpellierOrangePrimary,
                        unselectedIconColor = MontpellierNavyLight,
                        selectedTextColor = MontpellierOrangePrimary,
                        unselectedTextColor = MontpellierNavyLight,
                        indicatorColor = MontpellierOrangeLight
                    )
                )
                NavigationBarItem(
                    selected = currentTab == AppTab.JEUX,
                    onClick = { currentTab = AppTab.JEUX },
                    icon = { Icon(if (currentTab == AppTab.JEUX) Icons.Filled.EmojiEvents else Icons.Outlined.EmojiEvents, contentDescription = "Quiz") },
                    label = { Text("Défis") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MontpellierOrangePrimary,
                        unselectedIconColor = MontpellierNavyLight,
                        selectedTextColor = MontpellierOrangePrimary,
                        unselectedTextColor = MontpellierNavyLight,
                        indicatorColor = MontpellierOrangeLight
                    )
                )
                NavigationBarItem(
                    selected = currentTab == AppTab.AGENDA,
                    onClick = { currentTab = AppTab.AGENDA },
                    icon = { Icon(if (currentTab == AppTab.AGENDA) Icons.Filled.Event else Icons.Outlined.Event, contentDescription = "Agenda") },
                    label = { Text("Agenda") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MontpellierOrangePrimary,
                        unselectedIconColor = MontpellierNavyLight,
                        selectedTextColor = MontpellierOrangePrimary,
                        unselectedTextColor = MontpellierNavyLight,
                        indicatorColor = MontpellierOrangeLight
                    )
                )
                NavigationBarItem(
                    selected = currentTab == AppTab.PROFIL,
                    onClick = { currentTab = AppTab.PROFIL },
                    icon = { 
                        BadgedBox(badge = {
                            if ((wallet?.tokens ?: 0) > 0) {
                                Badge(containerColor = MontpellierOrangePrimary) {
                                    Text(wallet?.tokens.toString(), color = Color.White)
                                }
                            }
                        }) {
                            Icon(if (currentTab == AppTab.PROFIL) Icons.Filled.AccountCircle else Icons.Outlined.AccountCircle, contentDescription = "Profil")
                        }
                    },
                    label = { Text("Profil") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MontpellierOrangePrimary,
                        unselectedIconColor = MontpellierNavyLight,
                        selectedTextColor = MontpellierOrangePrimary,
                        unselectedTextColor = MontpellierNavyLight,
                        indicatorColor = MontpellierOrangeLight
                    )
                )
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    fadeIn(animationSpec = spring()) togetherWith fadeOut(animationSpec = spring())
                },
                label = "TabAnimations"
            ) { tab ->
                when (tab) {
                    AppTab.ACCUEIL -> HomeScreen(
                        viewModel = viewModel,
                        onStoryClick = { activeStory = it }
                    )
                    AppTab.CARTE -> MapScreen(viewModel = viewModel)
                    AppTab.JEUX -> ChallengesScreen(viewModel = viewModel, onNavigateTo = { currentTab = it })
                    AppTab.AGENDA -> AgendaScreen(viewModel = viewModel)
                    AppTab.PROFIL -> ProfileScreen(viewModel = viewModel)
                }
            }

            // Bottom popup detail sheet if an establishment is selected
            selectedEst?.let { est ->
                EstablishmentDetailSheet(
                    establishment = est,
                    viewModel = viewModel,
                    onDismiss = { viewModel.selectEstablishment(null) }
                )
            }

            // Immersion fullscreen story viewer
            activeStory?.let { story ->
                StoryPlayer(
                    story = story,
                    onDismiss = { activeStory = null }
                )
            }

            // --- IDEA 1: FLOATING INTERACTIVE ALERTS ---
            val newestAlert by viewModel.newestLiveTicketAlert.collectAsStateWithLifecycle()
            AnimatedVisibility(
                visible = newestAlert != null,
                enter = slideInVertically { -it } + fadeIn(),
                exit = slideOutVertically { -it } + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .zIndex(99f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp)
            ) {
                newestAlert?.let { alert ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.dismissLiveTicketAlert()
                                currentTab = AppTab.JEUX
                            },
                        colors = CardDefaults.cardColors(containerColor = MontpellierNavyDark),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                        border = BorderStroke(2.dp, MontpellierOrangePrimary)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Campaign,
                                        contentDescription = null,
                                        tint = MontpellierOrangePrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "FLASH INFO ESTIVALES ⚡️",
                                        color = MontpellierOrangePrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.sp
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .background(MontpellierOrangePrimary, RoundedCornerShape(10.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "LIVE",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Nouveau jeu mis en ligne par le commerçant '${alert.merchantName}' !",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "En jeu : ${alert.description}",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 12.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Rattaché à : ${alert.linkedGameTitle}",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "RELEVER LE DÉFI",
                                        color = MontpellierOrangePrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = null,
                                        tint = MontpellierOrangePrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------------------------------
// 1. HOME / ACCUEIL SCREEN
// ---------------------------------------------------------------------------------------------------------------------

@Composable
fun HomeScreen(
    viewModel: GuidementViewModel,
    onStoryClick: (Story) -> Unit
) {
    val searchVal by viewModel.searchQuery.collectAsStateWithLifecycle()
    val onlyMagnon by viewModel.onlyMagnon.collectAsStateWithLifecycle()
    val selectedQuartier by viewModel.selectedQuartier.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedAmbiance by viewModel.selectedAmbiance.collectAsStateWithLifecycle()
    
    val establishments by viewModel.filteredEstablishments.collectAsStateWithLifecycle()
    val wallet by viewModel.wallet.collectAsStateWithLifecycle()

    val quartiers = listOf("Écusson", "Port Marianne", "Antigone", "Beaux-Arts")
    val categories = listOf("RESTAURANT" to "Resto", "BAR" to "Bars", "CAFE" to "Brunch & Cafés")
    val ambiances = listOf("Détendu" to "🍃 Détendu", "Chic" to "💎 Chic", "Festif" to "🎉 Festif", "Étudiant" to "🎓 Étudiant")

    var simulatedAmbiance by remember { mutableStateOf("sunset") } // "day", "sunset", "night"

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Grand Header Montpellier (Moderne & Épuré - Thème Ambiance Locale)
        item {
            val hGradient = when(simulatedAmbiance) {
                "day" -> Brush.verticalGradient(colors = listOf(Color(0xFFFEF3C7), Color(0xFFFFF7ED)))
                "sunset" -> Brush.verticalGradient(colors = listOf(Color(0xFFFFEDD5), Color(0xFFFFF1F2)))
                else -> Brush.verticalGradient(colors = listOf(Color(0xFF0F172A), Color(0xFF1E1B4B)))
            }
            val mainTextColor = if (simulatedAmbiance == "night") Color.White else MontpellierNavyDark
            val subheadingColor = if (simulatedAmbiance == "night") Color(0xFF94A3B8) else MontpellierNavyLight
            val bodyTextColor = if (simulatedAmbiance == "night") Color(0xFFE2E8F0) else MontpellierNavyMedium
            val accentCardColor = if (simulatedAmbiance == "night") Color(0xFF1E293B) else MontpellierNavySurface
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                border = BorderStroke(1.dp, if(simulatedAmbiance == "night") Color(0xFF334155) else Color(0xFFF1F5F9))
            ) {
                Box(modifier = Modifier.fillMaxWidth().background(hGradient)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "LE PETIT CLAPAS",
                                    color = MontpellierOrangePrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Crits & Clapas 🐾",
                                    color = mainTextColor,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.SansSerif
                                )
                            }
                            
                            // Sleek modern points/wallet badge
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(
                                        accentCardColor,
                                        RoundedCornerShape(30.dp)
                                    )
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Stars,
                                    contentDescription = "Points",
                                    tint = MontpellierOrangePrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "${wallet?.points ?: 0} Pts",
                                    color = mainTextColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Filled.ConfirmationNumber,
                                    contentDescription = "Jetons",
                                    tint = MontpellierOrangePrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "${wallet?.tokens ?: 0} J",
                                    color = mainTextColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = when(simulatedAmbiance) {
                                "day" -> "“ Le ciel brille sur la Comédie - 27°C • Idéal pour une splendide promenade vieux Clapas ! ☀️ ”"
                                "sunset" -> "“ Coucher de soleil rougeoyant sur l'Arc de Triomphe - 24°C • Rendez-vous aux Estivales ce soir ! 🌅 ”"
                                else -> "“ Nuit douce & festive sur la Place de la Canourgue - 20°C • Les terrasses s'illuminent sous les étoiles ! 🌌 ”"
                            },
                            color = bodyTextColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                        
                        Spacer(modifier = Modifier.height(14.dp))
                        
                        // Selectors Row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(
                                Triple("day", "☀️ Journée", Color(0xFFFBBF24)),
                                Triple("sunset", "🌅 Couchant", Color(0xFFF97316)),
                                Triple("night", "🌌 Nocturne", Color(0xFF6366F1))
                            ).forEach { (mode, label, activeColor) ->
                                val active = simulatedAmbiance == mode
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { simulatedAmbiance = mode },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (active) activeColor else accentCardColor
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (active) Color.White else subheadingColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Live Alerts Ticker of Merchant Tickets & Game launches
        item {
            val notifications by viewModel.appNotifications.collectAsStateWithLifecycle()
            if (notifications.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MontpellierOrangeLight.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.2.dp, MontpellierOrangePrimary.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = MontpellierOrangePrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Alertes Commerçants & Jeux en Direct ⚡️",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MontpellierNavyDark
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .background(MontpellierOrangePrimary, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${notifications.size} Actif",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Active notification
                        Text(
                            text = notifications.first(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MontpellierNavyDark,
                            lineHeight = 15.sp
                        )
                        
                        if (notifications.size > 1) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Divider(color = MontpellierNavyLight.copy(alpha = 0.2f), thickness = 0.5.dp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Précédent : ${notifications[1]}",
                                fontSize = 9.sp,
                                color = MontpellierNavyLight,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // Horizontal circular stories row (PRD A: "Stories éphémères")
        item {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Text(
                    text = "Nouvelles Ouvertures & Actus 🔥",
                    color = MontpellierNavyDark,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 10.dp)
                )
                
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(viewModel.stories) { story ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { onStoryClick(story) }
                                .width(90.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .border(
                                        width = 3.dp,
                                        brush = Brush.sweepGradient(
                                            colors = listOf(
                                                MontpellierOrangePrimary,
                                                MontpellierOrangeAccent,
                                                MontpellierOrangePrimary
                                            )
                                        ),
                                        shape = CircleShape
                                    )
                                    .padding(4.dp)
                                    .background(Color.White, CircleShape)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color(0xFFE2E8F0), CircleShape)
                                        .clip(CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (story.imageUrl) {
                                            "tapas" -> Icons.Default.Fastfood
                                            "wine" -> Icons.Default.SportsBar
                                            else -> Icons.Default.DirectionsBike
                                        },
                                        contentDescription = null,
                                        tint = MontpellierNavyPrimary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = story.title,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.Bold,
                                color = SlateDark,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // Filtre & Recherche Block
        item {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                // Search field
                val keyboardController = LocalSoftwareKeyboardController.current
                OutlinedTextField(
                    value = searchVal,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Rechercher un resto, bar, café...", fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MontpellierNavyLight) },
                    trailingIcon = {
                        if (searchVal.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MontpellierNavyPrimary,
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() })
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Official label Recommendation "RPPLC" Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.setOnlyMagnon(!onlyMagnon) }
                        .background(
                            if (onlyMagnon) MontpellierOrangeLight.copy(alpha = 0.5f) else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(MontpellierOrangePrimary, CircleShape)
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = "Label RPPLC",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Recommandés RPPLC 🐾",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MontpellierNavyDark
                        )
                        Text(
                            text = "Uniquement les pépites recommandées par le Petit Clapas",
                            fontSize = 11.sp,
                            color = MontpellierNavyLight
                        )
                    }
                    Switch(
                        checked = onlyMagnon,
                        onCheckedChange = { viewModel.setOnlyMagnon(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = MontpellierOrangePrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Neighborhood tags
                Text("Quartiers de l'Écusson & Montpellier :", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MontpellierNavyMedium)
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = selectedQuartier == null,
                            onClick = { viewModel.setQuartier(null) },
                            label = { Text("Tous", fontSize = 12.sp) }
                        )
                    }
                    items(quartiers) { q ->
                        FilterChip(
                            selected = selectedQuartier == q,
                            onClick = { viewModel.setQuartier(q) },
                            label = { Text(q, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Category and Ambiance tags
                Text("Ambiance souhaitée :", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MontpellierNavyMedium)
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = selectedAmbiance == null,
                            onClick = { viewModel.setAmbiance(null) },
                            label = { Text("Toutes", fontSize = 12.sp) }
                        )
                    }
                    items(ambiances) { (key, display) ->
                        FilterChip(
                            selected = selectedAmbiance == key,
                            onClick = { viewModel.setAmbiance(key) },
                            label = { Text(display, fontSize = 12.sp) }
                        )
                    }
                }
            }
        }

        // List Header and counter
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Les meilleures adresses",
                    color = MontpellierNavyDark,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
                Text(
                    text = "${establishments.size} trouvées",
                    color = MontpellierNavyLight,
                    fontSize = 12.sp
                )
            }
        }

        // Establishments list items (Fiches Établissements with Expert Verdict)
        if (establishments.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = "Aucun résultat",
                            tint = MontpellierNavyLight,
                            modifier = Modifier.size(52.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Aucun établissement ne correspond aux filtres.",
                            color = MontpellierNavyMedium,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        } else {
            items(establishments) { est ->
                EstablishmentCard(
                    establishment = est,
                    onToggleFavorite = { viewModel.toggleFavorite(est) },
                    onClick = { viewModel.selectEstablishment(est) }
                )
            }
        }
    }
}

@Composable
fun EstablishmentCard(
    establishment: Establishment,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Column {
            // Visual header representation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(
                        Brush.linearGradient(
                            colors = when (establishment.category) {
                                "RESTAURANT" -> listOf(Color(0xFFE29578), Color(0xFFE76F51))
                                "BAR" -> listOf(Color(0xFF2E86AB), Color(0xFF1B4965))
                                else -> listOf(Color(0xFFF4A261), Color(0xFFE76F51))
                            }
                        )
                    )
                    .padding(12.dp)
            ) {
                // Category pill and Magnon label
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = when (establishment.category) {
                                    "RESTAURANT" -> "🍔 Resto"
                                    "BAR" -> "🍸 Bar Pub"
                                    else -> "☕ Café Brunch"
                                },
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (establishment.isMagnonLabel) {
                            Box(
                                modifier = Modifier
                                    .background(MontpellierOrangePrimary, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.WorkspacePremium,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "RPPLC 🐾",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Favorite button
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.8f), CircleShape)
                            .size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (establishment.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favori",
                            tint = if (establishment.isFavorite) Color.Red else MontpellierNavyDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Neighborhood text
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "📍 ${establishment.quartier}",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Text block info
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = establishment.name,
                        color = MontpellierNavyDark,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color(0xFFFEF3C7), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = String.format("%.1f", establishment.rating),
                            color = Color(0xFF92400E),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = establishment.description,
                    color = MontpellierNavyMedium,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(10.dp))
                Divider(color = Color(0xFFF1F5F9))
                Spacer(modifier = Modifier.height(10.dp))

                // Custom critic quote "Le Verdict"
                Column(
                    modifier = Modifier
                        .background(MontpellierOrangeLight.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FormatQuote,
                            contentDescription = null,
                            tint = MontpellierOrangePrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "L'AVIS DU PETIT CLAPAS 🐾",
                            color = MontpellierOrangePrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = establishment.verdict,
                        color = SlateDark,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------------------------------
// 2. INTERACTIVE OPENSTREETMAP MAP SCREEN
// ---------------------------------------------------------------------------------------------------------------------

fun generateOsmHtml(challenges: List<Challenge>, rawEsts: List<Establishment>): String {
    val markersJs = StringBuilder()
    
    // 1. Add challenges
    challenges.forEach { ch ->
        val emoji = if (ch.type == "STREET_ART") "🚲" else "📸"
        val bgColor = if (ch.isCompleted) "#10B981" else "#F77F00"
        markersJs.append("""
            L.marker([${ch.lat}, ${ch.lng}], {
                icon: L.divIcon({
                    html: '<div style="background-color: $bgColor; width: 34px; height: 34px; border-radius: 50%; border: 2.5px solid white; display: flex; align-items: center; justify-content: center; font-size: 16px; box-shadow: 0 2px 5px rgba(0,0,0,0.35)">$emoji</div>',
                    className: '',
                    iconSize: [34, 34],
                    iconAnchor: [17, 17]
                })
            }).addTo(map)
              .on('click', function() {
                  Android.selectNode('${ch.id}', 'CHALLENGE');
              });
        """.trimIndent())
    }

    // 2. Add establishments with predefined coords relative to Montpellier center
    rawEsts.forEach { est ->
        val (lat, lng) = when (est.id) {
            "est_grillardin" -> Pair(43.6105, 3.8755)
            "est_diligence" -> Pair(43.6120, 3.8785)
            "est_barbote" -> Pair(43.6068, 3.8760)
            "est_cafe_mer" -> Pair(43.5995, 3.8975)
            "est_bonobo" -> Pair(43.6142, 3.8795)
            "est_pates" -> Pair(43.6186, 3.8824)
            "est_antigone" -> Pair(43.6081, 3.8872)
            else -> Pair(43.6107, 3.8767)
        }
        val emoji = when (est.category) {
            "RESTAURANT" -> "🍽️"
            "BAR" -> "🍺"
            "CAFE" -> "☕"
            else -> "✨"
        }
        markersJs.append("""
            L.marker([$lat, $lng], {
                icon: L.divIcon({
                    html: '<div style="background-color: #1E293B; width: 34px; height: 34px; border-radius: 50%; border: 2.5px solid white; display: flex; align-items: center; justify-content: center; font-size: 16px; box-shadow: 0 2px 5px rgba(0,0,0,0.35)">$emoji</div>',
                    className: '',
                    iconSize: [34, 34],
                    iconAnchor: [17, 17]
                })
            }).addTo(map)
              .on('click', function() {
                  Android.selectNode('${est.id}', 'EST');
              });
        """.trimIndent())
    }

    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
            <style>
                body { padding: 0; margin: 0; }
                html, body, #map { height: 100%; width: 100vw; background: #E2E8F0; }
                .leaflet-control-attribution { display: none !important; }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script>
                var map = L.map('map', {
                    zoomControl: false
                }).setView([43.6107, 3.8767], 14);

                L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                    maxZoom: 19
                }).addTo(map);

                // Add zoom control at bottom right
                L.control.zoom({
                    position: 'bottomright'
                }).addTo(map);

                map.on('click', function() {
                    Android.clearSelection();
                });

                $markersJs
            </script>
        </body>
        </html>
    """.trimIndent()
}

@Composable
fun MapScreen(viewModel: GuidementViewModel) {
    val challenges by viewModel.challenges.collectAsStateWithLifecycle()
    val rawEsts by viewModel.filteredEstablishments.collectAsStateWithLifecycle()
    val wallet by viewModel.wallet.collectAsStateWithLifecycle()
    
    var selectedNodeId by remember { mutableStateOf<String?>(null) }
    var selectedNodeType by remember { mutableStateOf<String?>(null) } // "CHALLENGE" or "EST"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Map intro banner
        Card(
            colors = CardDefaults.cardColors(containerColor = MontpellierNavyDark),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Montpellier interactive OpenStreetMap 🗺️",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = "Explorez la carte réelle de Montpellier. Cliquez sur les épingles street-art (orange 🚲) pour réaliser l'action ou les adresses phares (bleu marine 🍽️) pour consulter l'avis de nos experts !",
                    color = MontpellierNavyLight,
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            }
        }

        // The OpenStreetMap WebView container
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(2.dp, MontpellierNavyPrimary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .background(Color(0xFFF1F5F9))
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        webViewClient = WebViewClient()
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        
                        addJavascriptInterface(object {
                            @android.webkit.JavascriptInterface
                            fun selectNode(id: String, type: String) {
                                post {
                                    selectedNodeId = id
                                    selectedNodeType = type
                                }
                            }
                            
                            @android.webkit.JavascriptInterface
                            fun clearSelection() {
                                post {
                                    selectedNodeId = null
                                }
                            }
                        }, "Android")
                    }
                },
                update = { webView ->
                    val html = generateOsmHtml(challenges, rawEsts)
                    webView.loadDataWithBaseURL("https://openstreetmap.org", html, "text/html", "UTF-8", null)
                }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Selected interactive node panel
        AnimatedVisibility(
            visible = selectedNodeId != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.fillMaxWidth()
        ) {
            val matchingChallenge = if (selectedNodeType == "CHALLENGE") challenges.find { it.id == selectedNodeId } else null
            val matchingEst = if (selectedNodeType == "EST") rawEsts.find { it.id == selectedNodeId } else null
            
            if (matchingChallenge != null || matchingEst != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    border = BorderStroke(2.dp, MontpellierNavyPrimary.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = matchingChallenge?.title ?: matchingEst?.name ?: "",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MontpellierNavyDark
                                )
                                Text(
                                    text = if (matchingChallenge != null) "📍 ${matchingChallenge.locationName}" else "🍷 ${matchingEst?.category} - ${matchingEst?.quartier}",
                                    fontSize = 12.sp,
                                    color = MontpellierNavyMedium
                                )
                            }
                            
                            IconButton(onClick = { selectedNodeId = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Fermer")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Show actions based on node type
                        if (matchingChallenge != null) {
                            Text(
                                text = matchingChallenge.description,
                                fontSize = 11.sp,
                                color = SlateDark,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            if (matchingChallenge.isCompleted) {
                                Button(
                                    onClick = { },
                                    enabled = false,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Défi accompli ! (+${matchingChallenge.points} Pts gagnés)", color = Color.White)
                                }
                            } else {
                                Button(
                                    onClick = {
                                        viewModel.completeChallenge(matchingChallenge.id)
                                        selectedNodeId = null
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MontpellierOrangePrimary),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(
                                        imageVector = if (matchingChallenge.type == "PHOTO") Icons.Default.PhotoCamera else Icons.Default.MyLocation,
                                        contentDescription = "Action"
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (matchingChallenge.type == "PHOTO") "Prendre la photo du monument (+${matchingChallenge.points} Pts)" 
                                               else "Faire le Check-In Street-Art (+${matchingChallenge.points} Pts)",
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        } else if (matchingEst != null) {
                            Text(
                                text = matchingEst.description,
                                fontSize = 11.sp,
                                color = SlateDark,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.toggleFavorite(matchingEst) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(
                                        imageVector = if (matchingEst.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = null,
                                        tint = if (matchingEst.isFavorite) Color.Red else MontpellierNavyDark,
                                        modifier = Modifier.keepTouchTargetCompact()
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (matchingEst.isFavorite) "Enregistré" else "Enregistrer", fontSize = 11.sp, color = MontpellierNavyDark)
                                }

                                Button(
                                    onClick = { viewModel.selectEstablishment(matchingEst) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MontpellierNavyPrimary),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Voir la fiche", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Local wrapper for our simulated coordinate map node
class MapNode(
    val id: String,
    val title: String,
    val subtitle: String,
    val x: Float,
    val y: Float,
    val type: String,
    val color: Color,
    val completed: Boolean,
    val originalObject: Any
)

// ---------------------------------------------------------------------------------------------------------------------
// 3. DEFIS / JEUX / QUIZ & SCORE LEADERBOARD
// ---------------------------------------------------------------------------------------------------------------------

@Composable
fun PixelArtMatrix(type: String, modifier: Modifier = Modifier) {
    val matrix = when (type) {
        "INVADER" -> listOf(
            listOf(0,0,1,0,0,1,0,0),
            listOf(0,0,0,1,1,0,0,0),
            listOf(0,0,1,1,1,1,0,0),
            listOf(0,1,1,0,0,1,1,0),
            listOf(0,1,1,1,1,1,1,0),
            listOf(0,0,1,0,0,1,0,0),
            listOf(0,1,0,0,0,0,1,0)
        )
        "HEART" -> listOf(
            listOf(0,0,0,0,0,0,0,0),
            listOf(0,1,1,0,0,1,1,0),
            listOf(1,1,1,1,1,1,1,1),
            listOf(1,1,1,1,1,1,1,1),
            listOf(0,1,1,1,1,1,1,0),
            listOf(0,0,1,1,1,1,0,0),
            listOf(0,0,0,1,1,0,0,0)
        )
        else -> listOf(
            listOf(0,0,0,1,0,0,0,0),
            listOf(0,0,1,1,1,0,0,0),
            listOf(0,1,1,1,1,1,0,0),
            listOf(1,1,1,1,1,1,1,0),
            listOf(0,1,1,1,1,1,0,0),
            listOf(0,0,1,1,1,0,0,0),
            listOf(0,0,0,1,0,0,0,0)
        )
    }

    val primaryColor = when (type) {
        "INVADER" -> Color(0xFF3B82F6)
        "HEART" -> Color(0xFFEF4444)
        else -> Color(0xFFF1C40F)
    }
    val tileBg = Color(0xFFE2E8F0)

    Column(
        modifier = modifier
            .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
            .border(1.5.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        matrix.forEach { row ->
            Row {
                row.forEach { cell ->
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .padding(1.5.dp)
                            .background(if (cell == 1) primaryColor else tileBg, RoundedCornerShape(3.dp))
                    )
                }
            }
        }
    }
}

@Composable
fun ChallengesScreen(viewModel: GuidementViewModel, onNavigateTo: (AppTab) -> Unit) {
    val challenges by viewModel.challenges.collectAsStateWithLifecycle()
    val wallet by viewModel.wallet.collectAsStateWithLifecycle()
    
    // Interactive Chrono Mosaic Game States
    var gameLevel by remember { mutableStateOf(0) } // 0: Start, 1..3: levels, 4: Finished/Reward
    var gameRunning by remember { mutableStateOf(false) }
    var gameStartTime by remember { mutableStateOf(0L) }
    var gameElapsedTime by remember { mutableStateOf(0f) }
    var gamePenaltiesSecs by remember { mutableStateOf(0f) }
    
    var levelSelectedAnswer by remember { mutableStateOf<Int?>(null) }
    var levelChecked by remember { mutableStateOf(false) }
    var levelIsCorrect by remember { mutableStateOf(false) }
    var wonBonusTicket by remember { mutableStateOf<AdvantageTicket?>(null) }
    
    val leaderboard = listOf(
        Triple("Clapassier_Pro_34", 320, "🥇"),
        Triple("Sun_Lover34", 250, "🥈"),
        Triple("Vous (Aventurier)", wallet?.points ?: 0, "⭐️"),
        Triple("Julie_Ecusson", 90, "4e"),
        Triple("Pierrot_Peyrou", 40, "5e")
    ).sortedByDescending { it.second }

    // --- INTERACTIVE LUCK & AR CHALLENGE STATES ---
    val coroutineScope = rememberCoroutineScope()
    val wheelSectors = remember {
        listOf(
            Pair("Pic Saint-Loup 🍷", Color(0xFFC084FC)),
            Pair("+10 Points XP ⚡", Color(0xFFFDE047)),
            Pair("1 Jeton Gratuit 🎟️", Color(0xFF86EFAC)),
            Pair("Grés de Mtp 🍇", Color(0xFFF472B6)),
            Pair("+20 Points XP 🔥", Color(0xFFFB923C)),
            Pair("Vignoble d'Or 👑", Color(0xFF60A5FA))
        )
    }
    val wheelSpinAngle = remember { Animatable(0f) }
    var isWheelSpinning by remember { mutableStateOf(false) }
    var wheelSpinMessage by remember { mutableStateOf<String?>(null) }
    var wheelWinSuccess by remember { mutableStateOf(false) }
    
    // AR Photo Matcher Game States (Monsieur BMX / Street-Art)
    var selectedArtChallengeForCam by remember { mutableStateOf<Challenge?>(null) }
    var scannerAlignmentSlider by remember { mutableStateOf(45f) } // target is 0f (aligned)
    var scannerZoomSlider by remember { mutableStateOf(1.8f) } // target is 1.0f (zoom match)
    var scannerWinMessage by remember { mutableStateOf<String?>(null) }

    // --- BUBBLE SHOOTER GAME STATES ---
    var bsPlaying by remember { mutableStateOf(false) }
    var bsScore by remember { mutableStateOf(0) }
    var bsHighScore by remember { mutableStateOf(0) }
    var bsBulletsLeft by remember { mutableStateOf(10) }
    var bsBulletX by remember { mutableStateOf(100f) }
    var bsBulletY by remember { mutableStateOf(260f) }
    var bsAimAngle by remember { mutableStateOf(0f) } // degrees, -75..75
    var bsBulletColor by remember { mutableStateOf(Color(0xFF8B5CF6)) }
    var bsNextBulletColor by remember { mutableStateOf(Color(0xFFFBBF24)) }
    var bsIsAnimating by remember { mutableStateOf(false) }
    var bsWinTicketResult by remember { mutableStateOf<AdvantageTicket?>(null) }
    
    val bsGridColors = remember {
        mutableStateListOf(
            Color(0xFF8B5CF6), Color(0xFF10B981), Color(0xFFFBBF24), Color(0xFFEF4444), Color(0xFF8B5CF6),
            Color(0xFFEF4444), Color(0xFFFBBF24), Color(0xFF10B981), Color(0xFF8B5CF6), Color(0xFF10B981),
            Color(0xFF10B981), Color(0xFF8B5CF6), Color(0xFFEF4444), Color(0xFFFBBF24), Color(0xFFEF4444),
            Color(0xFFFBBF24), Color(0xFFEF4444), Color(0xFF10B981), Color(0xFF8B5CF6), Color(0xFFFBBF24)
        )
    }
    val bsGridActive = remember {
        mutableStateListOf(
            true, true, true, true, true,
            true, true, true, true, true,
            true, true, true, true, true,
            true, true, true, true, true
        )
    }
    
    val bsLeaderboard = remember {
        mutableStateListOf(
            Pair("Marius_Castelnau", 1200),
            Pair("Estelle_Antigone", 950),
            Pair("Lino_Peyrou", 600)
        )
    }

    // --- GOELAND FLAPPY GAME STATES ---
    var gPlaying by remember { mutableStateOf(false) }
    var gScore by remember { mutableStateOf(0) }
    var gHighScore by remember { mutableStateOf(0) }
    var gGoelandY by remember { mutableStateOf(100f) } // simulated height coordinate 0..200
    var gGoelandVelocity by remember { mutableStateOf(0f) }
    var gSecondsElapsed by remember { mutableStateOf(0) }
    var gCurrentSpeed by remember { mutableStateOf(1.0f) }
    var gGameOver by remember { mutableStateOf(false) }
    var gWinTicketResult by remember { mutableStateOf<AdvantageTicket?>(null) }
    
    val gPigeons = remember {
        mutableStateListOf(
            mutableMapOf("x" to 320f, "y" to 50f, "passed" to 0f),
            mutableMapOf("x" to 490f, "y" to 150f, "passed" to 0f)
        )
    }

    val gLeaderboard = remember {
        mutableStateListOf(
            Pair("Sarah_Saint_Roch", 45),
            Pair("Clapas_King", 30),
            Pair("Hugo_Le_Grec", 18)
        )
    }

    // --- GEOLOCALIZED RIDE / JEU DE PISTE GAME STATES ---
    var pStarted by remember { mutableStateOf(false) }
    var pStartTime by remember { mutableStateOf(0L) }
    var pElapsedTimeSeconds by remember { mutableStateOf(0L) }
    var pUserLat by remember { mutableStateOf(43.6000) } // starts far away
    var pUserLng by remember { mutableStateOf(3.8700) } // starts far away
    var pFinished by remember { mutableStateOf(false) }
    var pSuccessTimeTakenSeconds by remember { mutableStateOf(0L) }
    var pClaimedTicket by remember { mutableStateOf<AdvantageTicket?>(null) }
    
    val pLeaderboard = remember {
        mutableStateListOf(
            Pair("Thomas_Peyrou", 48L),
            Pair("Clara_BeauxArts", 72L),
            Pair("Mika_Comedie", 95L)
        )
    }

    // Active timer loop for the Geolocation Treasure Hunt
    LaunchedEffect(pStarted, pFinished) {
        if (pStarted && !pFinished) {
            pStartTime = System.currentTimeMillis()
            while (pStarted && !pFinished) {
                kotlinx.coroutines.delay(200)
                pElapsedTimeSeconds = (System.currentTimeMillis() - pStartTime) / 1000
            }
        }
    }

    // 1. Time / Seconds tracker loop which triggers the exponential speedup!
    LaunchedEffect(gPlaying) {
        if (gPlaying) {
            gSecondsElapsed = 0
            gCurrentSpeed = 1.0f
            gGameOver = false
            while (gPlaying && !gGameOver) {
                kotlinx.coroutines.delay(1000)
                gSecondsElapsed += 1
                // After 8 seconds, accelerate exponentially.
                if (gSecondsElapsed > 8) {
                    val ex = gSecondsElapsed - 8
                    gCurrentSpeed = (1.0f + 0.18f * java.lang.Math.pow(1.32, ex.toDouble())).toFloat().coerceAtMost(6.5f)
                } else {
                    gCurrentSpeed = 1.0f
                }
            }
        }
    }

    // 2. Physics & Engine Loop
    LaunchedEffect(gPlaying) {
        if (gPlaying) {
            gGoelandY = 100f
            gGoelandVelocity = 0f
            gScore = 0
            // reset pigeon positions to start
            gPigeons[0]["x"] = 320f
            gPigeons[0]["y"] = 60f
            gPigeons[0]["passed"] = 0f
            gPigeons[1]["x"] = 490f
            gPigeons[1]["y"] = 140f
            gPigeons[1]["passed"] = 0f
            
            while (gPlaying && !gGameOver) {
                kotlinx.coroutines.delay(30)
                // Gravity
                gGoelandVelocity += 0.42f
                gGoelandY += gGoelandVelocity
                
                // Boundaries check
                if (gGoelandY < 0f || gGoelandY > 200f) {
                    gGameOver = true
                }
                
                // Move pigeons
                for (i in 0 until gPigeons.size) {
                    val p = gPigeons[i]
                    val curX = p["x"] ?: 320f
                    val nextX = curX - (4.2f * gCurrentSpeed)
                    p["x"] = nextX
                    
                    // Score counting when goéland passes pigeon
                    val passed = p["passed"] ?: 0f
                    if (passed == 0f && nextX < 50f) {
                        p["passed"] = 1f
                        gScore += 1
                    }
                    
                    // If pigeon is fully off-screen on the left, recycle it to the right
                    if (nextX < -30f) {
                        p["x"] = 350f + (0..100).random().toFloat()
                        p["y"] = 20f + (0..150).random().toFloat()
                        p["passed"] = 0f
                    }
                    
                    // Collision check with pigeon
                    val pigY = p["y"] ?: 100f
                    val dx = 50f - nextX
                    val dy = gGoelandY - pigY
                    val distance = java.lang.Math.sqrt((dx * dx + dy * dy).toDouble())
                    if (distance < 20.0) {
                        gGameOver = true
                    }
                }
            }
        }
    }

    val mosaicLevels = listOf(
        MosaicLevel(
            title = "Mosaïque 'Space Invader' Azur 👾",
            description = "Un petit alien rétro bleu azur fixé en hauteur sur un angle de mur médiéval.",
            type = "INVADER",
            options = listOf("Rue de l'Ancien Courrier", "Rue de la Loge", "Avenue Foch", "Rue Saint-Guilhem"),
            correctIndex = 0,
            explanation = "La Rue de l'Ancien Courrier ! C'est le spot favori des mosaïques Space Invaders de l'Écusson.",
            helpHint = "C'est la rue commerçante pavée la plus étroite de la vieille ville de Montpellier."
        ),
        MosaicLevel(
            title = "Mosaïque 'Cœur de l'Écusson' ❤️",
            description = "Un grand cœur rouge vif pixelisé collé sur une façade d'immeuble en pierre de Castries.",
            type = "HEART",
            options = listOf("Avenue de l'Europe", "Place des Martyrs", "Rue de la Valfère", "Rue de l'Aiguillerie"),
            correctIndex = 2,
            explanation = "La Rue de la Valfère ! Un trésor caché d'art urbain menant vers le quartier Sainte-Anne.",
            helpHint = "Elle relie la rue Saint-Guilhem au cours Gambetta."
        ),
        MosaicLevel(
            title = "Mosaïque 'La Rose des Sables Or' 🧭",
            description = "Une céramique géométrique dorée étincelant au soleil près des voûtes médiévales.",
            type = "ROSE",
            options = listOf("Rue de l'Argenterie", "Grand Rue Jean Moulin", "Rue Foch", "Place de la Comédie"),
            correctIndex = 0,
            explanation = "La Rue de l'Argenterie ! La ruelle connue des orfèvres et des argentiers historiques du Moyen-Âge.",
            helpHint = "Elle tire son nom des anciens orfèvres qui y résidaient."
        )
    )

    LaunchedEffect(gameRunning) {
        if (gameRunning) {
            gameStartTime = System.currentTimeMillis()
            while (gameRunning) {
                val now = System.currentTimeMillis()
                gameElapsedTime = ((now - gameStartTime) / 1000f) + gamePenaltiesSecs
                kotlinx.coroutines.delay(50)
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 60.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Grand Banner gamification
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(MontpellierOrangePrimary, MontpellierNavyPrimary)
                        ),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Défis & Expérience 🏆",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Accumulez des points d'exploration pour les échanger contre des jetons des Estivales !",
                            color = MontpellierOrangeLight,
                            fontSize = 12.sp,
                            lineHeight = 15.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                            .padding(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
        }

        // Section A: LE DEFI CHRONO MOSAIQUE (24h)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⏱️ Défi Chrono Mosaïques",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MontpellierNavyDark
                )
                
                Box(
                    modifier = Modifier
                        .background(MontpellierOrangePrimary.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Événement 24h",
                        color = MontpellierOrangePrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.2.dp, if (gameRunning) MontpellierOrangePrimary.copy(alpha = 0.6f) else Color(0xFFE2E8F0)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    when (gameLevel) {
                        0 -> {
                            // Intro Page
                            Text(
                                text = "L'art s'affiche sur nos vieilles pierres ! Devinez la rue de Montpellier de chaque mosaïque le plus rapidement possible. Chaque erreur vous inflige une pénalité de +3.0s de retard !",
                                fontSize = 12.sp,
                                color = MontpellierNavyMedium,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Custom info row about the award
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MontpellierOrangeLight.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CardGiftcard, null, tint = MontpellierOrangePrimary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Le plus rapide en 24h remporte 1 Jeton Dégustation Gratuit !",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MontpellierNavyDark
                                )
                            }
                            
                            // Check for customized active merchant tickets linked to this game
                            val activeBons by viewModel.activeMerchantTickets.collectAsStateWithLifecycle()
                            val linkedBon = activeBons.find { it.linkedGameTitle == "Défi Chrono Mosaïques ⏱️" }
                            if (linkedBon != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MontpellierOrangeLight.copy(alpha = 0.6f)),
                                    border = BorderStroke(1.2.dp, MontpellierOrangePrimary),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Celebration,
                                            contentDescription = null,
                                            tint = MontpellierOrangePrimary,
                                            modifier = Modifier.size(26.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "🔥 TICKET BONUS EN JEU !",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MontpellierOrangePrimary,
                                                letterSpacing = 1.sp
                                            )
                                            Text(
                                                text = linkedBon.description,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MontpellierNavyDark
                                            )
                                            Text(
                                                text = "Offert par : ${linkedBon.merchantName} 🍇",
                                                fontSize = 9.sp,
                                                color = MontpellierNavyMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(14.dp))
                            
                            // Mock community contest times
                            Text("⏱️ Classement communautaire d'aujourd'hui :", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MontpellierNavyLight)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("🥇 Clapassier_Pro_34", fontSize = 11.sp, color = SlateDark)
                                Text("12.4s", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MontpellierNavyDark)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("🥈 Sun_Lover12", fontSize = 11.sp, color = SlateDark)
                                Text("16.8s", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MontpellierNavyDark)
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Button(
                                onClick = {
                                    gameElapsedTime = 0f
                                    gamePenaltiesSecs = 0f
                                    levelSelectedAnswer = null
                                    levelChecked = false
                                    gameLevel = 1
                                    gameRunning = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MontpellierNavyPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Timer, null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Lancer le Défi Mosaïque 🚀", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }

                        1, 2, 3 -> {
                            val lvl = mosaicLevels[gameLevel - 1]
                            
                            // Top level indicator + stopwatch
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Mosaïque $gameLevel / 3",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MontpellierOrangePrimary
                                )
                                
                                // Live ticking stopwatch!
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MontpellierOrangePrimary),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "${String.format("%.1f", gameElapsedTime)}s",
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Beautiful interactive graphic vector of the mosaic
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                PixelArtMatrix(type = lvl.type, modifier = Modifier.size(120.dp, 120.dp))
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text(
                                text = lvl.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MontpellierNavyDark
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = lvl.description,
                                fontSize = 11.sp,
                                color = MontpellierNavyMedium,
                                lineHeight = 14.sp
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            // Quick light bulb hint
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lightbulb, null, tint = Color(0xFFF1C40F), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Indice : ${lvl.helpHint}",
                                    fontSize = 10.sp,
                                    color = MontpellierNavyLight,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(14.dp))

                            // Options buttons
                            lvl.options.forEachIndexed { optIndex, optionText ->
                                val isSelected = levelSelectedAnswer == optIndex
                                val isCorrectOption = optIndex == lvl.correctIndex
                                val showCorrectionColors = levelChecked

                                OutlinedButton(
                                    onClick = {
                                        if (!levelChecked) {
                                            levelSelectedAnswer = optIndex
                                            levelIsCorrect = (optIndex == lvl.correctIndex)
                                            levelChecked = true
                                            if (!levelIsCorrect) {
                                                gamePenaltiesSecs += 3.0f
                                            }
                                        }
                                    },
                                    border = BorderStroke(
                                        width = 1.5.dp,
                                        color = when {
                                            showCorrectionColors && isCorrectOption -> Color(0xFF10B981)
                                            showCorrectionColors && isSelected && !isCorrectOption -> Color(0xFFEF4444)
                                            isSelected -> MontpellierNavyPrimary
                                            else -> Color(0xFFE2E8F0)
                                        }
                                    ),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = when {
                                            showCorrectionColors && isCorrectOption -> Color(0xFFD1FAE5)
                                            showCorrectionColors && isSelected && !isCorrectOption -> Color(0xFFFEE2E2)
                                            isSelected -> MontpellierNavyLight.copy(alpha = 0.15f)
                                            else -> Color.Transparent
                                        }
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = optionText,
                                            fontSize = 12.sp,
                                            color = if (isSelected) MontpellierNavyDark else SlateDark,
                                            textAlign = TextAlign.Start,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (showCorrectionColors && isCorrectOption) {
                                            Icon(Icons.Default.Check, null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                                        } else if (showCorrectionColors && isSelected && !isCorrectOption) {
                                            Icon(Icons.Default.Close, null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            if (levelChecked) {
                                // Feedback details
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (levelIsCorrect) Color(0xFFD1FAE5).copy(alpha = 0.6f) else Color(0xFFFEE2E2).copy(alpha = 0.6f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(10.dp)
                                        .fillMaxWidth()
                                ) {
                                    Column {
                                        Text(
                                            text = if (levelIsCorrect) "🎉 Correct !" else "⚡ Pénalité +3.0s ! Ce n'est pas cette rue.",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = if (levelIsCorrect) Color(0xFF065F46) else Color(0xFF991B1B)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = lvl.explanation,
                                            fontSize = 11.sp,
                                            color = SlateDark,
                                            lineHeight = 14.sp
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Button(
                                    onClick = {
                                        if (gameLevel == 3) {
                                            gameRunning = false
                                            gameLevel = 4 // Reward/Finish screen
                                            
                                            // Claim linked merchant ticket if any
                                            wonBonusTicket = viewModel.claimLinkedTicket("Défi Chrono Mosaïques ⏱️")
                                            
                                            // Secure points and award a digital token voucher directly
                                            viewModel.addPoints(50, "Casse-tête Mosaïque Chrono complété en ${String.format("%.1f", gameElapsedTime)}s")
                                            viewModel.buyTokens(1) // add +1 voucher token instantly to digital wallet
                                        } else {
                                            gameLevel++
                                            levelSelectedAnswer = null
                                            levelChecked = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MontpellierNavyPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = if (gameLevel == 3) "Enregistrer mon score final" else "Passer à la mosaïque suivante",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        4 -> {
                            // Final Completion Game screen
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MontpellierOrangeLight.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.EmojiEvents,
                                        contentDescription = null,
                                        tint = MontpellierOrangePrimary,
                                        modifier = Modifier.size(56.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "VICTOIRE CHRONO ! 🎉",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MontpellierNavyDark
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Vous avez résolu le parcours en :",
                                        fontSize = 12.sp,
                                        color = SlateDark
                                    )
                                    Text(
                                        text = "${String.format("%.1f", gameElapsedTime)} secondes",
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MontpellierOrangePrimary
                                    )
                                    if (gamePenaltiesSecs > 0) {
                                        Text(
                                            text = "(dont ${gamePenaltiesSecs.toInt()}s de pénalités d'erreurs)",
                                            fontSize = 10.sp,
                                            color = MontpellierNavyLight,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(14.dp))
                            
                            Text(
                                text = "🏆 Vous prenez la 1ère place du contest des dernières 24h !",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF10B981)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Récompense générale attribuée : 1 Jeton Dégustation Gratuit des Estivales a été déposé et sécurisé dans votre Profil. Utilisez-le ce soir !",
                                fontSize = 11.sp,
                                color = MontpellierNavyMedium,
                                lineHeight = 14.sp
                            )
                            
                            // SHOW WON MERCHANT BONUS TICKET CORRELATION
                            if (wonBonusTicket != null) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                                    border = BorderStroke(1.5.dp, Color(0xFF10B981)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Celebration, null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "BON AVANTAGE COMMERÇANT REMPORTÉ ! 🎉",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF047857)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = wonBonusTicket?.description ?: "",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Black,
                                            color = MontpellierNavyDark,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Offert par stand : ${wonBonusTicket?.merchantName} 🍇",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MontpellierNavyPrimary
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Divider(color = Color(0xFF10B981).copy(alpha = 0.2f), thickness = 0.5.dp)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Code QR unique : ${wonBonusTicket?.qrCodeHex}",
                                            fontSize = 9.sp,
                                            color = MontpellierNavyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "Présentez ce code QR sur le stand pour réclamer votre avantage !",
                                            fontSize = 8.sp,
                                            color = MontpellierNavyLight,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        gameLevel = 0
                                        gameElapsedTime = 0f
                                        gamePenaltiesSecs = 0f
                                        levelSelectedAnswer = null
                                        levelChecked = false
                                        wonBonusTicket = null
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, MontpellierNavyPrimary)
                                ) {
                                    Text("Rejouer ⏱️", color = MontpellierNavyPrimary, fontSize = 11.sp)
                                }
                                
                                Button(
                                    onClick = {
                                        // Navigate straight to Profil tab
                                        onNavigateTo(AppTab.PROFIL)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MontpellierOrangePrimary),
                                    modifier = Modifier.weight(1.3f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Voir mon code QR ➔", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section NOUVEAUTÉ: LE BUBBLE SHOOTER DE L'ÉCUSSON
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🎯 Bubble Shooter de l'Écusson",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MontpellierNavyDark
                )
                
                Box(
                    modifier = Modifier
                        .background(MontpellierNavyPrimary.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Mode Compétition 🏆",
                        color = MontpellierNavyPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        item {
            val activeBons by viewModel.activeMerchantTickets.collectAsStateWithLifecycle()
            val bsLinkedTicket = activeBons.find { t ->
                t.linkedGameTitle == "Bubble Shooter de l'Écusson 🎯" &&
                (System.currentTimeMillis() - t.dateCreated) < t.onlineDurationHours * 3600 * 1000L
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.2.dp, if (bsPlaying) MontpellierOrangePrimary.copy(alpha = 0.6f) else Color(0xFFE2E8F0)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (!bsPlaying) {
                        // OFF-GAME RECRUITMENT SCREEN
                        Text(
                            text = "Détruisez les grappes de bulles colorées en alignant 3 couleurs identiques ! Atteignez le sommet du classement pour remporter immédiatement le bon d'avantage offert par le stand partenaire !",
                            fontSize = 12.sp,
                            color = MontpellierNavyMedium,
                            lineHeight = 16.sp
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Active Ticket Incentive Banner
                        if (bsLinkedTicket != null) {
                            val elapsed = System.currentTimeMillis() - bsLinkedTicket.dateCreated
                            val remainingMs = (bsLinkedTicket.onlineDurationHours * 3600 * 1000L) - elapsed
                            val hoursLeft = maxOf(0L, remainingMs / (1000 * 3600))
                            val minsLeft = maxOf(0L, (remainingMs % (1000 * 3600)) / (1000 * 60))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
                                border = BorderStroke(1.5.dp, MontpellierOrangePrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Celebration,
                                        contentDescription = null,
                                        tint = MontpellierOrangePrimary,
                                        modifier = Modifier.size(34.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "🔥 TICKET DE STAND EN JEU !",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MontpellierOrangePrimary,
                                            letterSpacing = 1.sp
                                        )
                                        Text(
                                            text = bsLinkedTicket.description,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Black,
                                            color = MontpellierNavyDark
                                        )
                                        Text(
                                            text = "Offert par : ${bsLinkedTicket.merchantName} 🍇 • Expire dans : ${hoursLeft}h ${minsLeft}m ⌛",
                                            fontSize = 9.sp,
                                            color = MontpellierNavyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF1F5F9), RoundedCornerShape(10.dp))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Info, null, tint = MontpellierNavyMedium, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Aucun commerçant n'a déployé de ticket sur ce jeu pour le moment. Vous gagnez +30 XP d'exploration si vous décrochez le record !",
                                    fontSize = 11.sp,
                                    color = MontpellierNavyMedium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // LEADERBOARD SECTION
                        Text(
                            text = "🏆 Tableau des scores du stand :",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MontpellierNavyDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            // Check if current user is leader
                            val activeLeaderboardList = bsLeaderboard.toList().toMutableList()
                            val userIdx = activeLeaderboardList.indexOfFirst { it.first == "Vous (Aventurier)" }
                            if (userIdx == -1 && bsHighScore > 0) {
                                activeLeaderboardList.add(Pair("Vous (Aventurier)", bsHighScore))
                            } else if (userIdx != -1) {
                                activeLeaderboardList[userIdx] = Pair("Vous (Aventurier)", maxOf(bsHighScore, activeLeaderboardList[userIdx].second))
                            }
                            val sortedList = activeLeaderboardList.sortedByDescending { it.second }
                            
                            sortedList.forEachIndexed { idx, player ->
                                val colors = if (player.first == "Vous (Aventurier)") Color(0xFFFDF43F).copy(alpha = 0.25f) else Color(0xFFF8FAFC)
                                val border = if (player.first == "Vous (Aventurier)") BorderStroke(1.2.dp, MontpellierOrangePrimary) else BorderStroke(1.dp, Color(0xFFE2E8F0))
                                
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = colors),
                                    border = border,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = when (idx) {
                                                    0 -> "🥇"
                                                    1 -> "🥈"
                                                    2 -> "🥉"
                                                    else -> "${idx + 1}e"
                                                },
                                                fontSize = 12.sp,
                                                modifier = Modifier.width(24.dp)
                                            )
                                            Text(
                                                text = player.first,
                                                fontSize = 11.sp,
                                                fontWeight = if (player.first == "Vous (Aventurier)") FontWeight.Bold else FontWeight.Medium,
                                                color = MontpellierNavyDark
                                            )
                                        }
                                        Text(
                                            text = "${player.second} pts",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            color = MontpellierNavyDark
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // SUCCESS MERCHANDISE WINNER PROMPT
                        val currentLeader = if (bsLeaderboard.isNotEmpty()) {
                            val activeLeaderboardList = bsLeaderboard.toList().toMutableList()
                            val userIdx = activeLeaderboardList.indexOfFirst { it.first == "Vous (Aventurier)" }
                            if (userIdx != -1) {
                                activeLeaderboardList[userIdx] = Pair("Vous (Aventurier)", maxOf(bsHighScore, activeLeaderboardList[userIdx].second))
                            } else if (bsHighScore > 0) {
                                activeLeaderboardList.add(Pair("Vous (Aventurier)", bsHighScore))
                            }
                            activeLeaderboardList.sortedByDescending { it.second }.first().first
                        } else {
                            ""
                        }
                        
                        val amILeader = currentLeader == "Vous (Aventurier)"

                        if (amILeader && bsLinkedTicket != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFD1FAE5)),
                                border = BorderStroke(1.2.dp, Color(0xFF10B981)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "👑 VOUS ÊTES EN TÊTE DU SCOREBOARD !",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF065F46),
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Le ticket de stand '${bsLinkedTicket.merchantName}' vous appartient tant que vous maintenez votre record ! Réclamez-le dès maintenant !",
                                        fontSize = 11.sp,
                                        color = Color(0xFF047857),
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            val win = viewModel.claimLinkedTicket("Bubble Shooter de l'Écusson 🎯")
                                            if (win != null) {
                                                bsWinTicketResult = win
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Réclamer mon Ticket ! 🎁", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        // POPUP ON CLAIMED TICKET
                        bsWinTicketResult?.let { won ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                                border = BorderStroke(1.5.dp, Color(0xFF10B981)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.Celebration, null, tint = Color(0xFF10B981), modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "TICKET ACCORDÉ AVEC SUCCÈS ! 🎉",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF047857)
                                    )
                                    Text(
                                        text = won.description,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MontpellierNavyDark,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "Offert par : ${won.merchantName} • QR: ${won.qrCodeHex}",
                                        fontSize = 10.sp,
                                        color = MontpellierNavyPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Retrouvez-le directement dans votre onglet Profil !",
                                        fontSize = 9.sp,
                                        color = MontpellierNavyLight,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                bsScore = 0
                                bsBulletsLeft = 10
                                bsGridActive.fill(true)
                                bsPlaying = true
                                bsWinTicketResult = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MontpellierNavyPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PlayArrow, null, tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Lancer de Bubble Shooter 🎯 (Gratuit)", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        // ON-GAME PLAYING CANVAS SCREEN
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Score actuel :", fontSize = 10.sp, color = MontpellierNavyLight)
                                Text("$bsScore pts", fontSize = 16.sp, fontWeight = FontWeight.Black, color = MontpellierOrangePrimary)
                            }
                            
                            // Remaining Shots Badge
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MontpellierNavyPrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "$bsBulletsLeft coups restants",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // DRAW OF GAME SCREEN CANVAS
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                                .border(1.5.dp, MontpellierNavyPrimary.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val canvasWidth = size.width
                                val canvasHeight = size.height
                                
                                // Draw rows of active bubbles in grid
                                for (row in 0..3) {
                                    for (col in 0..4) {
                                        val idx = row * 5 + col
                                        if (idx in 0..19 && bsGridActive[idx]) {
                                            val bubbleCol = bsGridColors[idx]
                                            val cx = (col + 0.5f) * (canvasWidth / 5f)
                                            val cy = 25f + row * 38f
                                            drawCircle(
                                                color = bubbleCol,
                                                radius = 14f,
                                                center = Offset(cx, cy)
                                            )
                                            // draw little reflection dot
                                            drawCircle(
                                                color = Color.White.copy(alpha = 0.6f),
                                                radius = 3f,
                                                center = Offset(cx - 5f, cy - 5f)
                                            )
                                        }
                                    }
                                }

                                // Cannon base center bottom
                                val cannonCenterX = canvasWidth / 2f
                                val cannonCenterY = canvasHeight - 20f
                                
                                // Draw aimer line dotted
                                val rad = java.lang.Math.toRadians((bsAimAngle - 90f).toDouble())
                                val lineLength = 120f
                                val endX = cannonCenterX + (lineLength * java.lang.Math.cos(rad)).toFloat()
                                val endY = cannonCenterY + (lineLength * java.lang.Math.sin(rad)).toFloat()
                                
                                drawLine(
                                    color = MontpellierOrangePrimary.copy(alpha = 0.6f),
                                    start = Offset(cannonCenterX, cannonCenterY),
                                    end = Offset(endX, endY),
                                    strokeWidth = 3f,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                )

                                // Draw cannon metal stand
                                drawCircle(
                                    color = Color(0xFF475569),
                                    radius = 24f,
                                    center = Offset(cannonCenterX, cannonCenterY)
                                )
                                drawCircle(
                                    color = MontpellierOrangePrimary,
                                    radius = 16f,
                                    center = Offset(cannonCenterX, cannonCenterY)
                                )

                                // Draw the traveling bubble bullet
                                val bulletPositionX = if (bsIsAnimating) {
                                    // Scale coordinates from simulated 300 scale to real canvas width
                                    (bsBulletX / 300f) * canvasWidth
                                } else {
                                    cannonCenterX
                                }
                                val bulletPositionY = if (bsIsAnimating) {
                                    (bsBulletY / 260f) * canvasHeight
                                } else {
                                    cannonCenterY
                                }

                                drawCircle(
                                    color = bsBulletColor,
                                    radius = 14f,
                                    center = Offset(bulletPositionX, bulletPositionY)
                                )
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.7f),
                                    radius = 3.5f,
                                    center = Offset(bulletPositionX - 5.5f, bulletPositionY - 5.5f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🧭 Pivoter :",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MontpellierNavyDark,
                                modifier = Modifier.width(64.dp)
                            )
                            Slider(
                                value = bsAimAngle,
                                onValueChange = { if (!bsIsAnimating) bsAimAngle = it },
                                valueRange = -75f..75f,
                                colors = SliderDefaults.colors(
                                    thumbColor = MontpellierOrangePrimary,
                                    activeTrackColor = MontpellierOrangePrimary,
                                    inactiveTrackColor = Color(0xFFE2E8F0)
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val canShoot = !bsIsAnimating && bsBulletsLeft > 0

                        Button(
                            onClick = {
                                if (canShoot) {
                                    bsBulletsLeft -= 1
                                    
                                    coroutineScope.launch {
                                        bsIsAnimating = true
                                        // coordinates simulation
                                        var x = 150f
                                        var y = 240f
                                        val angleRad = java.lang.Math.toRadians((bsAimAngle - 90f).toDouble())
                                        val step = 16f
                                        val dx = (step * java.lang.Math.cos(angleRad)).toFloat()
                                        val dy = (step * java.lang.Math.sin(angleRad)).toFloat()
                                        
                                        var hitOccurred = false
                                        while (y > 10f && x > 5f && x < 295f && !hitOccurred) {
                                            x += dx
                                            y += dy
                                            
                                            // Wall bounces
                                            if (x < 10f) {
                                                x = 10f
                                                break
                                            }
                                            if (x > 290f) {
                                                x = 290f
                                                break
                                            }
                                            
                                            bsBulletX = x
                                            bsBulletY = y
                                            
                                            kotlinx.coroutines.delay(16)
                                            
                                            // Check collision with the 4x5 grid
                                            val approxCol = ((x / 300f) * 5f).toInt().coerceIn(0, 4)
                                            val approxRow = ((y - 15f) / 38f).toInt().coerceIn(0, 3)
                                            val gridIdx = approxRow * 5 + approxCol
                                            
                                            if (gridIdx in 0..19 && bsGridActive[gridIdx]) {
                                                hitOccurred = true
                                                // Check matching color
                                                if (bsGridColors[gridIdx] == bsBulletColor) {
                                                    bsGridActive[gridIdx] = false
                                                    bsScore += 150
                                                    
                                                    // chained pop adjacent indexes in list
                                                    listOf(-1, 1, -5, 5).forEach { offset ->
                                                        val adj = gridIdx + offset
                                                        if (adj in 0..19 && bsGridColors[adj] == bsBulletColor && bsGridActive[adj]) {
                                                            bsGridActive[adj] = false
                                                            bsScore += 100
                                                        }
                                                    }
                                                } else {
                                                    // Snap and occupy surrounding closest space
                                                    bsGridColors[gridIdx] = bsBulletColor
                                                    bsScore += 20
                                                }
                                            }
                                        }
                                        
                                        // Reset shoot bullet
                                        bsBulletX = 150f
                                        bsBulletY = 240f
                                        bsBulletColor = bsNextBulletColor
                                        bsNextBulletColor = listOf(Color(0xFF8B5CF6), Color(0xFF10B981), Color(0xFFFBBF24), Color(0xFFEF4444)).random()
                                        bsIsAnimating = false
                                        
                                        // Regenerate grid if all bubbles cleared
                                        if (bsGridActive.none { it }) {
                                            bsGridActive.fill(true)
                                            bsScore += 500
                                        }
                                    }
                                }
                            },
                            enabled = canShoot,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MontpellierOrangePrimary,
                                disabledContainerColor = Color(0xFFCBD5E1)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.FlashOn, null, tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("TIRER LA BULLE ! 🍇", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // ABANDON / QUIT BUTTON
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Mini next bullet preview
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Suivant : ", fontSize = 10.sp, color = MontpellierNavyMedium)
                                Box(modifier = Modifier.size(12.dp).background(bsNextBulletColor, CircleShape))
                            }
                            
                            TextButton(
                                onClick = {
                                    bsPlaying = false
                                    bsHighScore = maxOf(bsHighScore, bsScore)
                                    // if beat high score, inject user to local leaderboard
                                    if (bsHighScore > 1200) {
                                        val idx = bsLeaderboard.indexOfFirst { it.first == "Vous (Aventurier)" }
                                        if (idx != -1) {
                                            bsLeaderboard[idx] = Pair("Vous (Aventurier)", bsHighScore)
                                        } else {
                                            bsLeaderboard.add(0, Pair("Vous (Aventurier)", bsHighScore))
                                        }
                                    }
                                }
                            ) {
                                Text("Quitter la partie & Enregistrer 🚫", color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // GAME OVER OVERLAY / CONFIRMATION PANEL
                        if (bsBulletsLeft <= 0 && !bsIsAnimating) {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                                border = BorderStroke(1.2.dp, Color(0xFFF59E0B)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "⚡ PARTIE TERMINÉE ! SCORE MAXIMUM : $bsScore",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp,
                                        color = Color(0xFFB45309)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Vous n'avez plus de tirs disponibles ! Enregistrez votre score pour voir votre place de champion.",
                                        fontSize = 11.sp,
                                        color = Color(0xFF92400E)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = {
                                            bsPlaying = false
                                            bsHighScore = maxOf(bsHighScore, bsScore)
                                            if (bsHighScore > 1200) {
                                                // Add or update user to leaderboard
                                                val idx = bsLeaderboard.indexOfFirst { it.first == "Vous (Aventurier)" }
                                                if (idx != -1) {
                                                    bsLeaderboard[idx] = Pair("Vous (Aventurier)", bsHighScore)
                                                } else {
                                                    bsLeaderboard.add(0, Pair("Vous (Aventurier)", bsHighScore))
                                                }
                                                // reward points
                                                viewModel.addPoints(40, "Bubble Shooter de l'Écusson : Nouveau Record de $bsHighScore pts !")
                                                viewModel.buyTokens(1)
                                            } else {
                                                viewModel.addPoints(10, "Bubble Shooter : Participation et Score de $bsScore pts")
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Enregistrer mon score ✅", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section NOUVEAUTÉ: JEU DE PISTE GÉOLOCALISÉ DES ESTIVALES
        item {
            val pActiveTickets by viewModel.activeMerchantTickets.collectAsStateWithLifecycle()
            val currentPisteTicket = pActiveTickets.find {
                it.linkedGameTitle == "Jeu de Piste Géolocalisé 24H 🗺️" || it.linkedGameTitle == "Jeu de Piste Géolocalisé 🗺️"
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🗺️ Jeu de Piste Géolocalisé des Estivales",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MontpellierNavyDark
                )
                Box(
                    modifier = Modifier
                        .background(MontpellierOrangePrimary.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Vitesse & GPS ⏱️",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MontpellierOrangePrimary
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("jeu_de_piste_card"),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.2.dp, MontpellierNavyPrimary.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (currentPisteTicket == null) {
                        // Empty states when no geolocalized riddle launched
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = null,
                                tint = MontpellierNavyLight.copy(alpha = 0.4f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Aucune Énigme Active de Commerçant",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MontpellierNavyDark
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Pour jouer, allez dans 'Espace Commerçant' ci-dessus pour simuler la mise en ligne d'une énigme et de coordonnées !",
                                fontSize = 10.sp,
                                color = MontpellierNavyLight,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 14.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    // Let's seed a beautiful demo riddle so the user can test immediately!
                                    viewModel.createAndLinkAdvantageTicket(
                                        merchantName = "Vignoble Saint-Jean",
                                        description = "Une bouteille de Grés de Montpellier cuvée d'Or offerte 🍷",
                                        gameTitle = "Jeu de Piste Géolocalisé 🗺️",
                                        durationHours = 12,
                                        enigma = "Je possède une fontaine majestueuse au centre de Montpellier, et trois déesses veillent sur moi. Les gens s'y rejoignent souvent avant de faire la fête. Quelle est cette magnifique place ?",
                                        lat = 43.6085,
                                        lng = 3.8794
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MontpellierNavyPrimary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("💡 Lancer une Énigme de Démo instantanée !", fontSize = 10.sp, color = Color.White)
                            }
                        }
                    } else {
                        // Game active
                        val targetLat = currentPisteTicket.riddleLat ?: 43.6085
                        val targetLng = currentPisteTicket.riddleLng ?: 3.8794
                        
                        // Approximate distance calculation
                        val dLat = pUserLat - targetLat
                        val dLng = pUserLng - targetLng
                        val distanceInMeters = Math.sqrt(dLat * dLat + dLng * dLng) * 111000.0
                        val arrived = distanceInMeters <= 15.0

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "STAND SPONSOR : ${currentPisteTicket.merchantName.uppercase()}",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MontpellierOrangePrimary,
                                letterSpacing = 0.5.sp
                            )
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFFEF3C7), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "LOT : ${currentPisteTicket.description}",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        
                        // Enigma text
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "💬 L'ÉNIGME DU COMMERÇANT :",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MontpellierNavyMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = currentPisteTicket.riddleEnigma ?: "Suivez la piste pour trouver les coordonnées !",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MontpellierNavyDark,
                                    lineHeight = 15.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (!pStarted) {
                            // Stage 1: Click to start
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Pour remporter ce ticket avantage, vous devez vous rendre sur les lieux de l'énigme. Dès que vous cliquez sur démarrer, le chrono se lance ! Le vainqueur est la personne qui s'y rend le plus rapidement.",
                                    fontSize = 10.sp,
                                    color = MontpellierNavyLight,
                                    lineHeight = 14.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        pStarted = true
                                        pFinished = false
                                        pUserLat = 43.6000 // far away
                                        pUserLng = 3.8700 // far away
                                        pSuccessTimeTakenSeconds = 0L
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MontpellierOrangePrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.PlayArrow, null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("▶️ DÉMARRER LA COURSE CONTRE LA MONTRE ⏱️", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        } else if (!pFinished) {
                            // Stage 2: Hunt is active and ticking
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "⚡ Course en cours :",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MontpellierOrangePrimary
                                    )
                                    Text(
                                        text = "⌚ CHRONO : ${pElapsedTimeSeconds} s",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MontpellierNavyDark
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(10.dp))
                                
                                // Coordinates & Radar
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "🛰️ RADAR GÉOLOCALISÉ",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MontpellierNavyMedium
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text("CIBLE GPS", fontSize = 8.sp, color = MontpellierNavyLight)
                                                Text(String.format("%.4f, %.4f", targetLat, targetLng), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MontpellierNavyDark)
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text("VOTRE POSITION", fontSize = 8.sp, color = MontpellierNavyLight)
                                                Text(String.format("%.4f, %.4f", pUserLat, pUserLng), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MontpellierNavyDark)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Distance Feedback
                                        val distText = if (arrived) "🟢 VOUS Y ÊTES ! (~${distanceInMeters.toInt()}m) - Validez !" else "🔴 À ${distanceInMeters.toInt()} m de l'objectif"
                                        Text(
                                            text = distText,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (arrived) Color(0xFF10B981) else MontpellierOrangePrimary,
                                            modifier = Modifier.align(Alignment.CenterHorizontally)
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Manual Simulator controls so they don't get stuck in the web emulator!
                                        Text(
                                            text = "🕹️ Simulateur GPS (Marchez vers les coordonnées) :",
                                            fontSize = 8.sp,
                                            color = MontpellierNavyLight,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    // Move closer incrementally
                                                    val latStep = (targetLat - pUserLat) * 0.25
                                                    val lngStep = (targetLng - pUserLng) * 0.25
                                                    pUserLat += latStep
                                                    pUserLng += lngStep
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = MontpellierNavyPrimary),
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.weight(1f),
                                                contentPadding = PaddingValues(0.dp)
                                            ) {
                                                Text("👣 Marcher", fontSize = 9.sp, color = Color.White)
                                            }

                                            Button(
                                                onClick = {
                                                    // Teleport pile sur place
                                                    pUserLat = targetLat
                                                    pUserLng = targetLng
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = MontpellierOrangePrimary),
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.weight(1f),
                                                contentPadding = PaddingValues(0.dp)
                                            ) {
                                                Text("🚀 Se Téléporter", fontSize = 9.sp, color = Color.White)
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        pFinished = true
                                        pSuccessTimeTakenSeconds = pElapsedTimeSeconds
                                    },
                                    enabled = arrived,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF10B981),
                                        disabledContainerColor = Color(0xFFE2E8F0)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("🎯 VALIDER MA POSITION & ARRÊTER LE CHRONO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (arrived) Color.White else MontpellierNavyLight)
                                }
                            }
                        } else {
                            // Stage 3: Finished! Report results
                            val beatRecord = pSuccessTimeTakenSeconds < (pLeaderboard.getOrNull(0)?.second ?: 999L)
                            
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = if (beatRecord) Icons.Default.EmojiEvents else Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = if (beatRecord) MontpellierOrangeAccent else MontpellierNavyLight,
                                    modifier = Modifier.size(54.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (beatRecord) "🏆 NOUVEAU RECORD !" else "🏁 COURSE TERMINÉE !",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (beatRecord) Color(0xFF92400E) else MontpellierNavyDark
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Vous avez rallié les coordonnées de l'énigme en ${pSuccessTimeTakenSeconds} secondes !",
                                    fontSize = 11.sp,
                                    color = MontpellierNavyDark
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                if (beatRecord) {
                                    // User won and is the best!
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                                        border = BorderStroke(1.dp, Color(0xFF10B981))
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "🎉 Vous êtes premier du classement !",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF065F46)
                                            )
                                            Text(
                                                text = "Vous remportez le Ticket d'avantage de '${currentPisteTicket.merchantName}' !",
                                                fontSize = 10.sp,
                                                color = Color(0xFF047857),
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = {
                                            val win = viewModel.claimLinkedTicket(currentPisteTicket.linkedGameTitle)
                                            if (win != null) {
                                                pClaimedTicket = win
                                                
                                                // Prepend ourselves to leaderboard
                                                val existingUserIndex = pLeaderboard.indexOfFirst { it.first == "Vous (Arrivé)" }
                                                if (existingUserIndex != -1) {
                                                    pLeaderboard.removeAt(existingUserIndex)
                                                }
                                                pLeaderboard.add(0, Pair("Vous (Arrivé)", pSuccessTimeTakenSeconds))
                                                pLeaderboard.sortBy { it.second }
                                            }
                                        },
                                        enabled = pClaimedTicket == null,
                                        colors = ButtonDefaults.buttonColors(containerColor = MontpellierOrangePrimary),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(if (pClaimedTicket == null) "🎁 RÉCLAMER LE TICKET UNIQUE !" else "✅ TICKET RÉCLAMÉ !", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                } else {
                                    // User did not beat record
                                    Text(
                                        text = "Le record à battre est de ${pLeaderboard.firstOrNull()?.second}s par ${pLeaderboard.firstOrNull()?.first}.",
                                        fontSize = 10.sp,
                                        color = Color.Red,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                
                                OutlinedButton(
                                    onClick = {
                                        pStarted = false
                                        pFinished = false
                                        pElapsedTimeSeconds = 0L
                                        pClaimedTicket = null
                                    },
                                    border = BorderStroke(1.dp, MontpellierOrangePrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("🔄 Recommencer pour battre un record !", fontSize = 11.sp, color = MontpellierOrangePrimary)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        
                        // Leaderboard Section
                        Text(
                            text = "🏆 LES CHASSEURS LES PLUS RAPIDES :",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MontpellierNavyMedium,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            pLeaderboard.forEachIndexed { idx, pair ->
                                val scoreText = "${pair.second}s"
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            if (pair.first.contains("Vous")) Color(0xFFFEF3C7) else MontpellierNavySurface,
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "#${idx + 1}",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (idx == 0) MontpellierOrangePrimary else MontpellierNavyLight
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = pair.first,
                                            fontSize = 9.sp,
                                            fontWeight = if (pair.first.contains("Vous")) FontWeight.Bold else FontWeight.Normal,
                                            color = MontpellierNavyDark
                                        )
                                    }
                                    Text(
                                        text = scoreText,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MontpellierNavyDark
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section NOUVEAUTÉ: ENVOL DU GOELAND DE LA COMEDIE
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🐦 L'Envol du Goéland de la Comédie",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MontpellierNavyDark
                )
                
                Box(
                    modifier = Modifier
                        .background(MontpellierOrangePrimary.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Vitesse Progressive ⚡",
                        color = MontpellierOrangePrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        item {
            val activeBons by viewModel.activeMerchantTickets.collectAsStateWithLifecycle()
            val gLinkedTicket = activeBons.find { t ->
                t.linkedGameTitle == "L'Envol du Goéland 🐦" &&
                (System.currentTimeMillis() - t.dateCreated) < t.onlineDurationHours * 3600 * 1000L
            }

            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.2.dp, if (gPlaying) MontpellierNavyPrimary.copy(alpha = 0.6f) else Color(0xFFE2E8F0)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (!gPlaying) {
                        // OFF-GAME SCREEN
                        Text(
                            text = "Aidez notre goéland intrépide de la Comédie à éviter les pigeons ! Attention, restez ultra vif : après 8 secondes du jeu, la vitesse s'accélère exponentiellement.",
                            fontSize = 12.sp,
                            color = MontpellierNavyMedium,
                            lineHeight = 16.sp
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Active Ticket Incentive Banner
                        if (gLinkedTicket != null) {
                            val elapsed = System.currentTimeMillis() - gLinkedTicket.dateCreated
                            val remainingMs = (gLinkedTicket.onlineDurationHours * 3600 * 1000L) - elapsed
                            val hoursLeft = maxOf(0L, remainingMs / (1000 * 3600))
                            val minsLeft = maxOf(0L, (remainingMs % (1000 * 3600)) / (1000 * 60))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                                border = BorderStroke(1.5.dp, MontpellierNavyPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Celebration,
                                        contentDescription = null,
                                        tint = MontpellierNavyPrimary,
                                        modifier = Modifier.size(34.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "🔥 TICKET EXCLUSIF SUR CET ENVOL !",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MontpellierNavyPrimary,
                                            letterSpacing = 1.sp
                                        )
                                        Text(
                                            text = gLinkedTicket.description,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Black,
                                            color = MontpellierNavyDark
                                        )
                                        Text(
                                            text = "Offert par : ${gLinkedTicket.merchantName} 🍇 • Temps restant : ${hoursLeft}h ${minsLeft}m ⌛",
                                            fontSize = 9.sp,
                                            color = MontpellierNavyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF1F5F9), RoundedCornerShape(10.dp))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Info, null, tint = MontpellierNavyMedium, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Aucun commerçant n'a déployé de ticket sur ce jeu pour l'instant. Décrochez le record pour gagner +30 XP d'exploration !",
                                    fontSize = 11.sp,
                                    color = MontpellierNavyMedium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // LEADERBOARD SECTION
                        Text(
                            text = "🏆 Tableau des scores du Goéland :",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MontpellierNavyDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            val activeLeaderboardList = gLeaderboard.toList().toMutableList()
                            val userIdx = activeLeaderboardList.indexOfFirst { it.first == "Vous (Aventurier)" }
                            if (userIdx == -1 && gHighScore > 0) {
                                activeLeaderboardList.add(Pair("Vous (Aventurier)", gHighScore))
                            } else if (userIdx != -1) {
                                activeLeaderboardList[userIdx] = Pair("Vous (Aventurier)", maxOf(gHighScore, activeLeaderboardList[userIdx].second))
                            }
                            val sortedList = activeLeaderboardList.sortedByDescending { it.second }
                            
                            sortedList.forEachIndexed { idx, player ->
                                val colors = if (player.first == "Vous (Aventurier)") Color(0xFFFDF43F).copy(alpha = 0.25f) else Color(0xFFF8FAFC)
                                val border = if (player.first == "Vous (Aventurier)") BorderStroke(1.2.dp, MontpellierOrangePrimary) else BorderStroke(1.dp, Color(0xFFE2E8F0))
                                
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = colors),
                                    border = border,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = when (idx) {
                                                    0 -> "🥇"
                                                    1 -> "🥈"
                                                    2 -> "🥉"
                                                    else -> "${idx + 1}e"
                                                },
                                                fontSize = 12.sp,
                                                modifier = Modifier.width(24.dp)
                                            )
                                            Text(
                                                text = player.first,
                                                fontSize = 11.sp,
                                                fontWeight = if (player.first == "Vous (Aventurier)") FontWeight.Bold else FontWeight.Medium,
                                                color = MontpellierNavyDark
                                            )
                                        }
                                        Text(
                                            text = "${player.second} esquives",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            color = MontpellierNavyDark
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // SUCCESS GOELAND WINNER PROMPT
                        val currentLeader = if (gLeaderboard.isNotEmpty()) {
                            val activeLeaderboardList = gLeaderboard.toList().toMutableList()
                            val userIdx = activeLeaderboardList.indexOfFirst { it.first == "Vous (Aventurier)" }
                            if (userIdx != -1) {
                                activeLeaderboardList[userIdx] = Pair("Vous (Aventurier)", maxOf(gHighScore, activeLeaderboardList[userIdx].second))
                            } else if (gHighScore > 0) {
                                activeLeaderboardList.add(Pair("Vous (Aventurier)", gHighScore))
                            }
                            activeLeaderboardList.sortedByDescending { it.second }.first().first
                        } else {
                            ""
                        }
                        
                        val amILeader = currentLeader == "Vous (Aventurier)"

                        if (amILeader && gLinkedTicket != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFD1FAE5)),
                                border = BorderStroke(1.2.dp, Color(0xFF10B981)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "👑 VOUS DÉTENEZ LE RECORD DU GOÉLAND DE FER !",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF065F46),
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Le ticket de stand '${gLinkedTicket.merchantName}' vous appartient tant que vous maintenez votre record ! Cliquez pour le réclamer immédiatement !",
                                        fontSize = 11.sp,
                                        color = Color(0xFF047857),
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            val win = viewModel.claimLinkedTicket("L'Envol du Goéland 🐦")
                                            if (win != null) {
                                                gWinTicketResult = win
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Réclamer mon Ticket ! 🎁", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        // POPUP ON CLAIMED TICKET
                        gWinTicketResult?.let { won ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                                border = BorderStroke(1.5.dp, Color(0xFF10B981)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.Celebration, null, tint = Color(0xFF10B981), modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "TICKET ACCORDÉ AVEC SUCCÈS ! 🎉",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF047857)
                                    )
                                    Text(
                                        text = won.description,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MontpellierNavyDark,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "Offert par : ${won.merchantName} • Code QR: ${won.qrCodeHex}",
                                        fontSize = 10.sp,
                                        color = MontpellierNavyPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Retrouvez-le directement dans votre onglet Profil !",
                                        fontSize = 9.sp,
                                        color = MontpellierNavyLight,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                gScore = 0
                                gSecondsElapsed = 0
                                gCurrentSpeed = 1.0f
                                gPlaying = true
                                gGameOver = false
                                gWinTicketResult = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MontpellierNavyPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PlayArrow, null, tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Voler avec le Goéland de la Comédie 🐦 (Gratuit)", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        // ON-GAME PLAYING CANVAS SCREEN
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Défis évités :", fontSize = 10.sp, color = MontpellierNavyLight)
                                Text("$gScore pigeons", fontSize = 16.sp, fontWeight = FontWeight.Black, color = MontpellierOrangePrimary)
                            }
                            
                            // Speedup / Acceleration Progress Bar
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = null,
                                    tint = if (gCurrentSpeed > 1.2f) Color.Red else MontpellierNavyMedium,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Vitesse: ${String.format("%.2f", gCurrentSpeed)}x",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (gCurrentSpeed > 1.2) Color.Red else MontpellierNavyDark
                                    )
                                    Text(
                                        text = "Temps survécu : ${gSecondsElapsed}s",
                                        fontSize = 9.sp,
                                        color = MontpellierNavyMedium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // DRAW OF FLAPPY GAME SCREEN CANVAS
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                                .border(1.5.dp, MontpellierNavyPrimary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .clickable {
                                    if (!gGameOver) {
                                        gGoelandVelocity = -6.5f
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val canvasWidth = size.width
                                val canvasHeight = size.height
                                
                                // Sunset Gradient Sky Background decoration
                                drawRect(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFF1E1E38),
                                            Color(0xFFFF7E5F),
                                            Color(0xFFFEB47B)
                                        )
                                    ),
                                    topLeft = Offset(0f, 0f),
                                    size = size
                                )

                                // Draw setting sun
                                drawCircle(
                                    color = Color(0xFFFDE047).copy(alpha = 0.5f),
                                    radius = 35f,
                                    center = Offset(canvasWidth * 0.75f, 60f)
                                )

                                // Place de la Comédie cobblestones bottom frame line
                                drawRect(
                                    color = Color(0xFF334155),
                                    topLeft = Offset(0f, canvasHeight - 15f),
                                    size = androidx.compose.ui.geometry.Size(canvasWidth, 15f)
                                )

                                // 1. Draw Pigeons
                                gPigeons.forEach { p ->
                                    val px = p["x"] ?: 320f
                                    val py = p["y"] ?: 100f
                                    
                                    // Scale coordinates from simulated 300x200 scale
                                    val realX = (px / 300f) * canvasWidth
                                    val realY = (py / 200f) * (canvasHeight - 30f) + 15f
                                    
                                    // Pigeon shape body (Grey slate)
                                    drawCircle(
                                        color = Color(0xFF64748B),
                                        radius = 12f,
                                        center = Offset(realX, realY)
                                    )
                                    // Pigeon little wing
                                    drawCircle(
                                        color = Color(0xFF94A3B8),
                                        radius = 6f,
                                        center = Offset(realX + 3f, realY - 2f)
                                    )
                                    // Beak (Orange)
                                    drawCircle(
                                        color = Color(0xFFF97316),
                                        radius = 3f,
                                        center = Offset(realX - 11f, realY)
                                    )
                                    // Red eye dot
                                    drawCircle(
                                        color = Color.Red,
                                        radius = 1.8f,
                                        center = Offset(realX - 7f, realY - 4f)
                                    )
                                }

                                // 2. Draw Goeland (White and yellow beak)
                                val gxReal = (50f / 300f) * canvasWidth
                                val gyReal = (gGoelandY / 200f) * (canvasHeight - 30f) + 15f

                                // Goéland body (White)
                                drawCircle(
                                    color = Color.White,
                                    radius = 14f,
                                    center = Offset(gxReal, gyReal)
                                )
                                // Flapping wing (White / light grey)
                                val wingOffset = if ((System.currentTimeMillis() / 200) % 2 == 0L) 6f else -6f
                                drawCircle(
                                    color = Color(0xFFF1F5F9),
                                    radius = 8f,
                                    center = Offset(gxReal - 2f, gyReal + wingOffset)
                                )
                                // Beak (Bright Yellow)
                                drawCircle(
                                    color = Color(0xFFEAB308),
                                    radius = 4f,
                                    center = Offset(gxReal + 13f, gyReal)
                                )
                                // Beak tip
                                drawCircle(
                                    color = Color.Red,
                                    radius = 1.5f,
                                    center = Offset(gxReal + 15f, gyReal + 0.5f)
                                )
                                // Black tail decoration
                                drawCircle(
                                    color = Color.Black,
                                    radius = 4f,
                                    center = Offset(gxReal - 12f, gyReal - 2f)
                                )
                            }
                            
                            // Touch to jump indicator
                            Text(
                                text = "TAP SUR L'ÉCRAN POUR VOLER ☝️",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    if (!gGameOver) {
                                        gGoelandVelocity = -6.5f
                                    }
                                },
                                enabled = !gGameOver,
                                colors = ButtonDefaults.buttonColors(containerColor = MontpellierOrangePrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(2f)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ArrowUpward, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("BATTRE DES AILES 🪶", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }

                            Button(
                                onClick = {
                                    gPlaying = false
                                    gHighScore = maxOf(gHighScore, gScore)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Quitter 🚫", color = Color.White, fontSize = 11.sp)
                            }
                        }

                        // GAME OVER COVER / END STATUS SCREEN
                        if (gGameOver) {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                                border = BorderStroke(1.2.dp, Color(0xFFEF4444)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "💥 LE GOÉLAND S'EST COLLISIONNÉ ! PARTIE EXPIRÉE : $gScore ESQUIVES",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp,
                                        color = Color(0xFF991B1B)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Vous avez survécu ${gSecondsElapsed}s avec une vitesse finale de ${String.format("%.2f", gCurrentSpeed)}x !",
                                        fontSize = 11.sp,
                                        color = Color(0xFF7F1D1D)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = {
                                            gPlaying = false
                                            gHighScore = maxOf(gHighScore, gScore)
                                            if (gHighScore > 45) {
                                                // Add or update user to leaderboard
                                                val idx = gLeaderboard.indexOfFirst { it.first == "Vous (Aventurier)" }
                                                if (idx != -1) {
                                                    gLeaderboard[idx] = Pair("Vous (Aventurier)", gHighScore)
                                                } else {
                                                    gLeaderboard.add(0, Pair("Vous (Aventurier)", gHighScore))
                                                }
                                                // reward points
                                                viewModel.addPoints(40, "Goéland Flappy de la Comédie : Nouveau Record de $gHighScore esquives !")
                                                viewModel.buyTokens(1)
                                            } else {
                                                viewModel.addPoints(10, "Vol du Goéland : Participation - $gScore pigeons evités")
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Enregistrer mon score de vol ✅", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section NOUVEAUTÉ: LA ROUE DES ESTIVALES
        item {
            Text(
                text = "🍇 La Roue des Estivales de Montpellier",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MontpellierNavyDark
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.2.dp, Color(0xFFF1F5F9)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Tentez votre chance ! Utilisez 15 Points XP d'exploration pour faire tourner la roue des vins et remporter des récompenses exclusives ou un ticket VIP direct ! 🍷",
                        fontSize = 12.sp,
                        color = MontpellierNavyMedium,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // The animated Canvas Wheel
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val radius = size.minDimension / 2
                            val rotation = wheelSpinAngle.value
                            
                            rotate(rotation) {
                                for (i in 0..5) {
                                    val startAngle = i * 60f
                                    drawArc(
                                        color = wheelSectors[i].second,
                                        startAngle = startAngle,
                                        sweepAngle = 60f,
                                        useCenter = true,
                                        size = size
                                    )
                                    
                                    // Divider Line
                                    val angleRad = Math.toRadians(startAngle.toDouble())
                                    val lineX = center.x + radius * Math.cos(angleRad).toFloat()
                                    val lineY = center.y + radius * Math.sin(angleRad).toFloat()
                                    drawLine(
                                        color = Color.White.copy(alpha = 0.5f),
                                        start = center,
                                        end = Offset(lineX, lineY),
                                        strokeWidth = 3f
                                    )
                                }
                                
                                // Sleek inner ring
                                drawCircle(
                                    color = Color.White,
                                    radius = radius * 0.4f,
                                    center = center
                                )
                            }
                            
                            // Outer wheel border frame
                            drawCircle(
                                color = MontpellierNavyPrimary,
                                radius = radius,
                                center = center,
                                style = Stroke(width = 8f)
                            )
                            
                            // Lights on the outer frame
                            for (i in 0..11) {
                                val dotAngle = i * 30f
                                val dotAngleRad = Math.toRadians(dotAngle.toDouble())
                                val dotX = center.x + (radius - 5f) * Math.cos(dotAngleRad).toFloat()
                                val dotY = center.y + (radius - 5f) * Math.sin(dotAngleRad).toFloat()
                                drawCircle(
                                    color = if ((wheelSpinAngle.value / 15).toInt() % 2 == i % 2) Color.White else Color(0xFFFFCC33),
                                    radius = 5f,
                                    center = Offset(dotX, dotY)
                                )
                            }
                        }
                        
                        // Overlapping center logo "clapas"
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .background(Color.White, CircleShape)
                                .border(2.dp, MontpellierOrangePrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🐾 MTP", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MontpellierNavyDark)
                        }
                        
                        // Top indicator pointer arrow pointing down
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = MontpellierOrangePrimary,
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.TopCenter)
                                .offset(y = (-4).dp)
                                .background(Color.White, CircleShape)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Show slice rewards legend
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        wheelSectors.take(3).forEach { (label, col) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(modifier = Modifier.size(8.dp).background(col, RoundedCornerShape(2.dp)))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(label, fontSize = 8.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MontpellierNavyMedium)
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        wheelSectors.drop(3).forEach { (label, col) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(modifier = Modifier.size(8.dp).background(col, RoundedCornerShape(2.dp)))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(label, fontSize = 8.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MontpellierNavyMedium)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Messages and controls
                    wheelSpinMessage?.let { msg ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MontpellierOrangeLight, RoundedCornerShape(10.dp))
                                .border(1.2.dp, MontpellierOrangePrimary.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = msg,
                                color = MontpellierNavyDark,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                    
                    Button(
                        onClick = {
                            val currentPoints = wallet?.points ?: 0
                            if (currentPoints < 15) {
                                wheelSpinMessage = "XP insuffisant ! Il vous faut au moins 15 Points pour faire tourner la roue."
                                wheelWinSuccess = false
                            } else {
                                coroutineScope.launch {
                                    isWheelSpinning = true
                                    wheelSpinMessage = "La Roue tourne à toute allure ! 🍇"
                                    
                                    val targetIndex = (0..5).random()
                                    val reward = wheelSectors[targetIndex]
                                    val sectorAngle = 60f
                                    val finalAngle = 360f * 6 + (360f - (targetIndex * sectorAngle + sectorAngle / 2f))
                                    
                                    // Deduct 15 Points
                                    viewModel.addPoints(-15, "Estivales : Lancement de la Roue Multi-Cadeaux 🍇")
                                    
                                    wheelSpinAngle.snapTo(0f)
                                    wheelSpinAngle.animateTo(
                                        targetValue = finalAngle,
                                        animationSpec = tween(
                                            durationMillis = 4000,
                                            easing = CubicBezierEasing(0.15f, 0.85f, 0.2f, 1.0f)
                                        )
                                    )
                                    
                                    isWheelSpinning = false
                                    wheelWinSuccess = true
                                    
                                    when (targetIndex) {
                                        0 -> { // Pic Saint-Loup 🍷
                                            viewModel.addPoints(5, "Roue : Échantillon Pic Saint-Loup offert 🍷")
                                            wheelSpinMessage = "Félicitations ! Vous gagnez un verre dégustation chez nos vignerons partenaires & +5 XP ! 🍷"
                                        }
                                        1 -> { // +10 Points XP ⚡
                                            viewModel.addPoints(10, "Roue : Bonus de points exploration ⚡")
                                            wheelSpinMessage = "Super ! Vous récupérez +10 Points XP d'exploration ! ⚡"
                                        }
                                        2 -> { // 1 Jeton Gratuit 🎟️
                                            viewModel.buyTokens(1)
                                            wheelSpinMessage = "MAGNIFIQUE ! Vous avez gagné 1 Jeton Dégustation Gratuit inséré dans votre profil ! 🎟️"
                                        }
                                        3 -> { // Grés de Mtp 🍇
                                            viewModel.addPoints(5, "Roue : Vignoble Grés de Montpellier 🍇")
                                            wheelSpinMessage = "Cépage d'exception ! Un verre de Grés de Montpellier de bienvenue offert & +5 XP ! 🍇"
                                        }
                                        4 -> { // +20 Points XP 🔥
                                            viewModel.addPoints(20, "Roue : Super Jackpot XP d'Élite 🔥")
                                            wheelSpinMessage = "INCROYABLE ! Vous remportez le super Jackpot d'exploration de +20 XP ! 🔥"
                                        }
                                        5 -> { // Vignoble d'Or 👑
                                            viewModel.createAndLinkAdvantageTicket("Le Vignoble d'Or 👑", "Dégustation Exclusive VIP Prestige")
                                            wheelSpinMessage = "C'EST LE JACKPOT ! Un ticket privilège VIP de 'Vignoble d'Or' vous a été attribué ! Retrouvez le QR Code unique dans votre profil. 👑"
                                        }
                                    }
                                }
                            }
                        },
                        enabled = !isWheelSpinning,
                        colors = ButtonDefaults.buttonColors(containerColor = MontpellierNavyPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Celebration, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isWheelSpinning) "Lancement..." else "Tourner la roue ! (15 XP ⚡)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Section B: ACTIVE EXPLORATION QUESTS LIST
        item {
            Text(
                text = "🚴‍♂️ Chasses Street-Art & Défis Monuments",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MontpellierNavyDark
            )
        }

        items(challenges) { ch ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = if (ch.isCompleted) Color(0xFFD1FAE5) else MontpellierOrangeLight,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (ch.type == "STREET_ART") Icons.Default.DirectionsBike else Icons.Default.AddAPhoto,
                            contentDescription = null,
                            tint = if (ch.isCompleted) Color(0xFF10B981) else MontpellierOrangePrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = ch.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MontpellierNavyDark,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .background(MontpellierNavyPrimary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text("+${ch.points} Pts", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MontpellierNavyPrimary)
                            }
                        }
                        Text(
                            text = "📍 ${ch.locationName}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MontpellierNavyLight
                        )
                        Text(
                            text = ch.description,
                            fontSize = 10.sp,
                            color = MontpellierNavyMedium,
                            lineHeight = 12.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    if (ch.isCompleted) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Completed",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(28.dp)
                        )
                    } else {
                        val isCameraChallenge = ch.type == "STREET_ART" || ch.type == "PHOTO"
                        IconButton(
                            onClick = {
                                if (isCameraChallenge) {
                                    selectedArtChallengeForCam = ch
                                } else {
                                    viewModel.completeChallenge(ch.id)
                                }
                            },
                            modifier = Modifier
                                .background(MontpellierOrangePrimary, RoundedCornerShape(8.dp))
                                .size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isCameraChallenge) Icons.Default.PhotoCamera else Icons.Default.FlashOn,
                                contentDescription = if (isCameraChallenge) "Scanner l'art" else "Faire",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Section C: EXPLORER LEADERBOARD
        item {
            Text(
                text = "🎖️ Classement des Clapassiers d'Élite",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MontpellierNavyDark
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    leaderboard.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (item.first.contains("Vous")) MontpellierOrangeLight.copy(alpha = 0.4f) else Color.Transparent,
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(vertical = 8.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = item.third,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(24.dp)
                                )
                                Text(
                                    text = item.first,
                                    fontWeight = if (item.first.contains("Vous")) FontWeight.Bold else FontWeight.Medium,
                                    color = if (item.first.contains("Vous")) MontpellierOrangePrimary else SlateDark,
                                    fontSize = 13.sp
                                )
                            }
                            Text(
                                text = "${item.second} Pts",
                                fontWeight = FontWeight.Bold,
                                color = MontpellierNavyDark,
                                fontSize = 13.sp
                            )
                        }
                        if (index < leaderboard.size - 1) {
                            Divider(color = Color(0xFFF1F5F9))
                        }
                    }
                }
            }
        }
    }

    // --- POPUP STREET-ART & MONUMENTS CAMERA SCAN SIMULATOR ---
    selectedArtChallengeForCam?.let { ch ->
        Dialog(
            onDismissRequest = { selectedArtChallengeForCam = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)) // Neon Dark Camera HUD Style
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // HUD Title Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color.Red, CircleShape) // simulated blinking red REC dot
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "MTP SCAN_LINK ACTIVE",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                        
                        TextButton(onClick = { selectedArtChallengeForCam = null }) {
                            Text("Fermer [X]", color = Color.White, fontSize = 11.sp)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // The Camera Lens Simulation Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(Color.Black, RoundedCornerShape(12.dp))
                            .border(1.5.dp, Color(0xFF10B981), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Simulated background scene drawn dynamically!
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // draw stylized bricks grid background
                            val brickW = 40f
                            val brickH = 20f
                            for (y in 0..(size.height / brickH).toInt() + 1) {
                                val offset = if (y % 2 == 0) brickW / 2 else 0f
                                for (x in -1..(size.width / brickW).toInt() + 1) {
                                    drawRect(
                                        color = Color(0xFF1E293B),
                                        topLeft = Offset((x * brickW) + offset, y * brickH),
                                        size = androidx.compose.ui.geometry.Size(brickW - 2f, brickH - 2f),
                                        style = Stroke(width = 1f)
                                    )
                                }
                            }
                            
                            // Draws a gorgeous neon outline of the target art based on alignment and zoom
                            val centerX = center.x + (scannerAlignmentSlider - 0f) * 2f
                            val diameter = 100f * scannerZoomSlider
                            
                            // Target silhouette background Monsieur BMX
                            drawCircle(
                                color = if (java.lang.Math.abs(scannerAlignmentSlider) < 10 && java.lang.Math.abs(scannerZoomSlider - 1f) < 0.15) Color(0xFF10B981).copy(alpha = 0.5f) else Color(0xFFFFCC33).copy(alpha = 0.25f),
                                radius = diameter / 2f,
                                center = Offset(centerX, center.y)
                            )
                            
                            // Target fixed silhouette stencil in center of screen
                            drawCircle(
                                color = Color.White.copy(alpha = 0.1f),
                                radius = 50f,
                                center = center,
                                style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
                            )
                        }
                        
                        // Overlay HUD markers/camera crosshairs
                        Text(
                            text = "[  +  ]",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        // Scanner Line scanning up and down
                        val infiniteTransition = rememberInfiniteTransition(label = "scan")
                        val scanY by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 200f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(2000, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "scanLine"
                        )
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .offset(y = (scanY - 100).dp)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Color.Transparent, Color(0xFF10B981), Color.Transparent)
                                    )
                                )
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    // Target instructions
                    Text(
                        text = "CHASSE : ${ch.title}",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Ajustez les curseurs pour faire coïncider l'œuvre d'art urbain repérée dans l'Écusson avec le viseur de calibrage MTP-LENS.",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 14.sp
                    )
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    // Adjustment Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Alignement :", color = Color.White, fontSize = 10.sp, modifier = Modifier.width(70.dp))
                        Slider(
                            value = scannerAlignmentSlider,
                            onValueChange = { scannerAlignmentSlider = it },
                            valueRange = -80f..80f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF10B981),
                                activeTrackColor = Color(0xFF10B981).copy(alpha = 0.7f),
                                inactiveTrackColor = Color(0xFF334155)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Zoom Objectif :", color = Color.White, fontSize = 10.sp, modifier = Modifier.width(70.dp))
                        Slider(
                            value = scannerZoomSlider,
                            onValueChange = { scannerZoomSlider = it },
                            valueRange = 0.4f..2.2f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF10B981),
                                activeTrackColor = Color(0xFF10B981).copy(alpha = 0.7f),
                                inactiveTrackColor = Color(0xFF334155)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // Status Badge
                    val aligned = java.lang.Math.abs(scannerAlignmentSlider) < 10
                    val zoomMatched = java.lang.Math.abs(scannerZoomSlider - 1f) < 0.15f
                    val calibrateMatches = aligned && zoomMatched
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (calibrateMatches) Color(0xFF064E3B) else Color(0xFF7F1D1D),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = if (calibrateMatches) Icons.Default.CheckCircle else Icons.Default.Cancel,
                            contentDescription = null,
                            tint = if (calibrateMatches) Color(0xFF34D399) else Color(0xFFFCA5A5),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (calibrateMatches) "CALIBRAGE PARFAIT ! PRÊT À CRAPTER" else "CALIBRAGE : ALIGNEMENT INSTABLE 🔍",
                            color = if (calibrateMatches) Color(0xFF34D399) else Color(0xFFFCA5A5),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    scannerWinMessage?.let { msg ->
                        Text(
                            text = msg,
                            color = Color(0xFF34D399),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        )
                    }
                    
                    Button(
                        onClick = {
                            if (calibrateMatches) {
                                viewModel.completeChallenge(ch.id)
                                scannerWinMessage = "Oeuvre identifiée avec succès ! Défi terminé ! +${ch.points} XP accordés !"
                                coroutineScope.launch {
                                    delay(1500)
                                    selectedArtChallengeForCam = null
                                    scannerWinMessage = null
                                    scannerAlignmentSlider = 45f
                                    scannerZoomSlider = 1.8f
                                }
                            }
                        },
                        enabled = calibrateMatches,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981),
                            disabledContainerColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PhotoCamera, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Prendre la photo & Valider ✅", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

class MosaicLevel(
    val title: String,
    val description: String,
    val type: String, // "INVADER", "HEART", "ROSE"
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val helpHint: String
)

// ---------------------------------------------------------------------------------------------------------------------
// 3.5 AGENDA DES CLAPASSIERS & ESTIVALES
// ---------------------------------------------------------------------------------------------------------------------

data class AgendaEvent(
    val id: String,
    val title: String,
    val date: String,
    val location: String,
    val category: String,
    val description: String,
    val highlight: String,
    val slotsText: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaScreen(viewModel: GuidementViewModel) {
    val events = remember {
        listOf(
            AgendaEvent(
                id = "evt_1",
                title = "Les Estivales de Montpellier 🍷",
                date = "Chaque Vendredi soir, 18h30 - 23h30",
                location = "Esplanade Charles-de-Gaulle",
                category = "Estivales",
                description = "Le grand rendez-vois de l'année ! Dégustez les vins des producteurs régionaux sous les platanes, savourez des assiettes d'artisans locaux, le tout rythmé par des concerts live.",
                highlight = "Dégustation & Concert",
                slotsText = "Entrée libre (Jetons requis)"
            ),
            AgendaEvent(
                id = "evt_2",
                title = "Mystère Urbain : À la recherche de Monsieur BMX 🚲",
                date = "Samedi 30 Mai, 14h00 - 17h00",
                location = "Départ : Place de la Comédie",
                category = "Street-Art",
                description = "Chasse au trésor urbaine pour découvrir les nouvelles œuvres de l'artiste incontournable de Montpellier. Des énigmes et indices révélés au fil du parcours !",
                highlight = "Chasse urbaine insolite",
                slotsText = "15 places restantes"
            ),
            AgendaEvent(
                id = "evt_3",
                title = "Vernissage & Street-Art aux Beaux-Arts 🎨",
                date = "Mercredi 3 Juin, 17h00 - 19h00",
                location = "Quartier des Beaux-Arts",
                category = "Street-Art",
                description = "Visite guidée et festive à travers les ruelles décorées, suivie d'un apéritif convivial en présence d'artistes locaux.",
                highlight = "Art urbain & Apéro",
                slotsText = "Entrée libre sur inscription"
            ),
            AgendaEvent(
                id = "evt_4",
                title = "Coquillages & Picpoul de Pinet 🦪",
                date = "Jeudi 4 Juin, 19h00 - 22h00",
                location = "Bassin Jacques-Cœur, Port Marianne",
                category = "Terroir",
                description = "Accord parfait sous la brise marine ! Dégustez les huîtres fraîches de Bouzigues accompagnées d'un verre de Picpoul bien frais.",
                highlight = "Dégustation d'huîtres",
                slotsText = "8 places restantes"
            ),
            AgendaEvent(
                id = "evt_5",
                title = "Nocturne des Créateurs du Clapas 🛍️",
                date = "Samedi 6 Juin, 18h00 - 23h30",
                location = "Rues de l'Écusson, Montpellier",
                category = "Estivales",
                description = "Un marché nocturne envoûtant mettant en valeur les talents des créateurs d'ici : bijoux, céramiques, produits bio locaux dans une ambiance festive.",
                highlight = "Marché artisanal",
                slotsText = "Accès libre"
            )
        )
    }

    var selectedFilter by remember { mutableStateOf("Tous") }
    var registeredEventIds by remember { mutableStateOf(setOf<String>()) }
    val filteredEvents = remember(selectedFilter) {
        if (selectedFilter == "Tous") {
            events
        } else {
            events.filter { it.category == selectedFilter }
        }
    }

    val registeredEvents = events.filter { it.id in registeredEventIds }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Grand Header Branded
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MontpellierNavyDark),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "EXPLOREZ L'AGENDA",
                        color = MontpellierOrangeAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "L'Agenda des Clapassiers 📅",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Retrouvez les temps forts de Montpellier : soirées des Estivales, marchés de créateurs et rencontres insolites.",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Horizontal visual summary of registered events
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "🎯 Mes Réservations & Rappels (${registeredEventIds.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MontpellierNavyDark
                )
                
                if (registeredEvents.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = null,
                                tint = MontpellierNavyLight,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Aucune inscription enregistrée. Parcourez l'agenda ci-dessous et réservez votre place !",
                                fontSize = 11.sp,
                                color = MontpellierNavyMedium,
                                lineHeight = 15.sp
                            )
                        }
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(registeredEvents) { evt ->
                            Card(
                                modifier = Modifier
                                    .width(220.dp),
                                colors = CardDefaults.cardColors(containerColor = MontpellierOrangeLight),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, MontpellierOrangePrimary)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = evt.category.uppercase(),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MontpellierOrangePrimary
                                        )
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = evt.title,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MontpellierNavyDark,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = evt.date,
                                        fontSize = 10.sp,
                                        color = MontpellierNavyMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Category Selection
        item {
            val categories = listOf("Tous", "Estivales", "Street-Art", "Terroir")
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedFilter == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = cat },
                        label = { Text(cat) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MontpellierOrangePrimary,
                            selectedLabelColor = Color.White,
                            containerColor = Color.White,
                            labelColor = MontpellierNavyDark
                        ),
                        border = BorderStroke(1.dp, if (isSelected) Color.Transparent else Color(0xFFCBD5E1))
                    )
                }
            }
        }

        // Event List
        if (filteredEvents.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Aucun événement pour cette catégorie.", color = MontpellierNavyLight)
                }
            }
        } else {
            items(filteredEvents) { evt ->
                val isRegistered = evt.id in registeredEventIds
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(MontpellierOrangeLight, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = evt.category,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MontpellierOrangePrimary
                                )
                            }

                            Text(
                                text = evt.slotsText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (evt.slotsText.contains("places")) MontpellierOrangePrimary else MontpellierNavyLight
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = evt.title,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MontpellierNavyDark
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Date & Location metadata
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = MontpellierOrangePrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = evt.date,
                                    fontSize = 12.sp,
                                    color = MontpellierNavyMedium
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MontpellierOrangePrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = evt.location,
                                    fontSize = 12.sp,
                                    color = MontpellierNavyMedium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = evt.description,
                            fontSize = 12.sp,
                            color = MontpellierNavyMedium.copy(alpha = 0.9f),
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Highlight pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(MontpellierNavySurface, RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Box(modifier = Modifier.size(6.dp).background(MontpellierOrangePrimary, CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = evt.highlight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MontpellierNavyDark
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (isRegistered) {
                                        registeredEventIds = registeredEventIds - evt.id
                                    } else {
                                        registeredEventIds = registeredEventIds + evt.id
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isRegistered) Color(0xFFE2E8F0) else MontpellierNavyPrimary,
                                    contentColor = if (isRegistered) MontpellierNavyDark else Color.White
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                if (isRegistered) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = Color(0xFF10B981)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Inscrit(e) !", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Event,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("M'inscrire", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------------------------------
// 4. PROTOCOLE ESTIVALES PORTRAIT / VIRTUAL TOKENS WALLET & QR CODES
// ---------------------------------------------------------------------------------------------------------------------

@Composable
fun ProfileScreen(viewModel: GuidementViewModel) {
    val wallet by viewModel.wallet.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val badges by viewModel.badges.collectAsStateWithLifecycle()
    val challenges by viewModel.challenges.collectAsStateWithLifecycle()
    val establishments by viewModel.filteredEstablishments.collectAsStateWithLifecycle()
    val wonAdvantageTickets by viewModel.wonAdvantageTickets.collectAsStateWithLifecycle()

    val completedChallenges = challenges.filter { it.isCompleted }
    val favoriteEsts = establishments.filter { it.isFavorite }

    // Portal subtabs: 0 = Espace Utilisateur (Client), 1 = Espace Commerçant (Merchant)
    var activeSubTab by remember { mutableStateOf(0) }
    var buyDialogShown by remember { mutableStateOf(false) }

    // QR dialog popup state (Title, CodeText)
    var selectedQrDialog by remember { mutableStateOf<Pair<String, String>?>(null) }

    // Merchant stand controls
    var mockCommercantName by remember { mutableStateOf("Vignoble Saint-Jean") }
    var scanResultLog by remember { mutableStateOf("") }
    
    // Merchant stats session memory
    var merchantValidatedCount by remember { mutableStateOf(18) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // --- PORTAL BRANDED TOP CARD ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            colors = CardDefaults.cardColors(containerColor = MontpellierNavyDark),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "LE PETIT CLAPAS",
                    color = MontpellierOrangeAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "L'Espace Clapas & Estivales 🐾",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // --- CUSTOM SEGMENTED PILLS TABS ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (activeSubTab == 0) MontpellierOrangePrimary else Color.Transparent,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { activeSubTab = 0 }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = if (activeSubTab == 0) Color.White else MontpellierNavyLight,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Espace Utilisateur 🐾",
                                color = if (activeSubTab == 0) Color.White else MontpellierNavyLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (activeSubTab == 1) MontpellierOrangePrimary else Color.Transparent,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { activeSubTab = 1 }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                tint = if (activeSubTab == 1) Color.White else MontpellierNavyLight,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Espace Commerçant 🍇",
                                color = if (activeSubTab == 1) Color.White else MontpellierNavyLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        if (activeSubTab == 0) {
            // ==========================================
            // 🐾 CLIENT / USER SPACE
            // ==========================================
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 64.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Subsection 1: Wallet state card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MontpellierNavySurface),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "MON PORTEFEUILLE NUMÉRIQUE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MontpellierNavyMedium,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("SOLDE JETONS", fontSize = 9.sp, color = MontpellierNavyLight, fontWeight = FontWeight.Bold)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.ConfirmationNumber, null, tint = MontpellierOrangePrimary, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("${wallet?.tokens ?: 0}", fontSize = 24.sp, fontWeight = FontWeight.Black, color = MontpellierNavyDark)
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("REPUTATION POINTS", fontSize = 9.sp, color = MontpellierNavyLight, fontWeight = FontWeight.Bold)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Stars, null, tint = MontpellierOrangeAccent, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("${wallet?.points ?: 0}", fontSize = 24.sp, fontWeight = FontWeight.Black, color = MontpellierNavyDark)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        if ((wallet?.points ?: 0) >= 100) {
                                            viewModel.convertPointsToToken()
                                        }
                                    },
                                    enabled = (wallet?.points ?: 0) >= 100,
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MontpellierOrangePrimary,
                                        disabledContentColor = MontpellierNavyLight.copy(alpha = 0.5f)
                                    ),
                                    border = BorderStroke(1.dp, if ((wallet?.points ?: 0) >= 100) MontpellierOrangePrimary else Color(0xFFCBD5E1)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 4.dp)
                                ) {
                                    Text("Échanger 100 Pts ➔ 1 Jeton", fontSize = 9.sp, maxLines = 1)
                                }
                                Button(
                                    onClick = { buyDialogShown = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = MontpellierOrangePrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(0.8f),
                                    contentPadding = PaddingValues(horizontal = 4.dp)
                                ) {
                                    Icon(Icons.Default.Add, null, modifier = Modifier.size(12.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("Acheter Jetons", fontSize = 9.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }

                // Subsection 2: QR retrieval for won games and completed challenges
                item {
                    Column {
                        Text(
                            text = "🎁 Mes QR Codes & Récompenses de Jeu",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MontpellierNavyDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Récupérez ici vos tickets d'exploration gagnés lors des défis street-art et quiz.",
                            fontSize = 11.sp,
                            color = MontpellierNavyLight
                        )
                    }
                }

                if (completedChallenges.isEmpty() && (wallet?.tokens ?: 0) <= 0 && wonAdvantageTickets.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.QrCodeScanner, null, tint = MontpellierNavyLight.copy(alpha = 0.7f), modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Aucun QR Code disponible", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MontpellierNavyDark)
                                Text(
                                    text = "Accomplissez des défis street-art dans l'onglet 'Défis' ou créditez votre solde de jetons pour débloquer des bons de dégustation sécurisés.",
                                    fontSize = 10.sp,
                                    color = MontpellierNavyLight,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                } else {
                    // Render generic active Token QR Voucher if they have tokens
                    if ((wallet?.tokens ?: 0) > 0) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.2.dp, MontpellierOrangePrimary.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .background(MontpellierOrangeLight, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.ConfirmationNumber, null, tint = MontpellierOrangePrimary, modifier = Modifier.size(20.dp))
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text("Ticket Dégustation Estivales", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MontpellierNavyDark)
                                            Text("Jeton actif prêt pour validation", fontSize = 10.sp, color = MontpellierNavyLight)
                                        }
                                    }
                                    Button(
                                        onClick = {
                                            selectedQrDialog = Pair(
                                                "Ticket Dégustation Actif 🍷",
                                                "VOUCHER-MTP-${UUID.randomUUID().toString().take(6).uppercase()}"
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MontpellierOrangePrimary),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text("Voir QR Code 📱", fontSize = 10.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }

                    // Render completed challenges which are games won!
                    items(completedChallenges) { ch ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(Color(0xFFD1FAE5), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (ch.type == "STREET_ART") Icons.Default.Palette else Icons.Default.PhotoCamera,
                                            contentDescription = null,
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(ch.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MontpellierNavyDark, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text("Récompense Street-Art validée • +${ch.points} Pts", fontSize = 10.sp, color = Color(0xFF10B981))
                                    }
                                }
                                Button(
                                    onClick = {
                                        selectedQrDialog = Pair(
                                            "Bon de Succès: ${ch.title} 🏆",
                                            "REWARD-CHALLENGE-${ch.id.uppercase()}"
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MontpellierNavyPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text("Voucher QR 🎟️", fontSize = 10.sp, color = Color.White)
                                }
                            }
                        }
                    }

                    // Render won merchant advantage tickets
                    items(wonAdvantageTickets) { ticket ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (ticket.isUsed) Color(0xFFF1F5F9) else Color(0xFFF0FDF4)
                            ),
                            border = BorderStroke(
                                1.2.dp, 
                                if (ticket.isUsed) Color(0xFFCBD5E1) else Color(0xFF10B981).copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(
                                                if (ticket.isUsed) Color(0xFFE2E8F0) else Color(0xFFD1FAE5), 
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (ticket.isUsed) Icons.Default.CheckCircle else Icons.Default.CardGiftcard,
                                            contentDescription = null,
                                            tint = if (ticket.isUsed) Color(0xFF64748B) else Color(0xFF047857),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = ticket.description,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (ticket.isUsed) Color(0xFF64748B) else MontpellierNavyDark,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = if (ticket.isUsed) "Consommé le ${java.text.SimpleDateFormat("dd/MM HH:mm").format(java.util.Date(ticket.dateCreated))} 🍇" else "Offert par : ${ticket.merchantName} 🍇",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (ticket.isUsed) Color(0xFF64748B) else Color(0xFF047857)
                                        )
                                    }
                                }
                                Button(
                                    onClick = {
                                        selectedQrDialog = Pair(
                                            "Bon Avantage: ${ticket.description} 🎁",
                                            ticket.qrCodeHex
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (ticket.isUsed) Color(0xFF64748B) else Color(0xFF10B981)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = if (ticket.isUsed) "Historique 📜" else "Présenter QR 📱", 
                                        fontSize = 10.sp, 
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                // Subsection 3: User Favorites ("mettre des établissements en favoris")
                item {
                    Column(modifier = Modifier.padding(top = 6.dp)) {
                        Text(
                            text = "⭐️ Mes Adresses Favorites (${favoriteEsts.size})",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MontpellierNavyDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Retrouvez rapidement vos pépites enregistrées pour y retourner facilement.",
                            fontSize = 11.sp,
                            color = MontpellierNavyLight
                        )
                    }
                }

                if (favoriteEsts.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.FavoriteBorder, null, tint = MontpellierNavyLight.copy(alpha = 0.7f), modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Aucun fav' pour le moment", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MontpellierNavyDark)
                                Text(
                                    text = "Ajoutez un cœur sur la fiche de vos cafés, restaurants et bars préférés pour les épingler ici !",
                                    fontSize = 10.sp,
                                    color = MontpellierNavyLight,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                } else {
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(favoriteEsts) { est ->
                                Card(
                                    modifier = Modifier
                                        .width(220.dp)
                                        .clickable { viewModel.selectEstablishment(est) },
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .background(MontpellierOrangeLight, RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = est.category,
                                                    fontSize = 8.sp,
                                                    color = MontpellierOrangePrimary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            IconButton(
                                                onClick = { viewModel.toggleFavorite(est) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Favorite,
                                                    contentDescription = "Unfavorite",
                                                    tint = Color.Red,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))
                                        
                                        Text(
                                            text = est.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MontpellierNavyDark,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Text(
                                            text = est.address,
                                            fontSize = 10.sp,
                                            color = MontpellierNavyLight,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Star, null, tint = MontpellierOrangeAccent, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text("${est.rating}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MontpellierNavyDark)
                                            }
                                            
                                            Box(
                                                modifier = Modifier
                                                    .background(MontpellierNavyPrimary, RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text("Fiche 🐾", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Subsection 4: Badges
                item {
                    Text(
                        text = "🎖️ Mes Badges de Guide Estival",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MontpellierNavyDark
                    )
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        val chunkedBadges = badges.chunked(3)
                        chunkedBadges.forEach { chunk ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                chunk.forEach { badge ->
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(52.dp)
                                                .background(
                                                    color = if (badge.isUnlocked) MontpellierOrangeLight.copy(alpha = 0.5f) else Color(0xFFF1F5F9),
                                                    shape = CircleShape
                                                )
                                                .border(
                                                    width = 1.dp,
                                                    color = if (badge.isUnlocked) MontpellierOrangePrimary else Color.Transparent,
                                                    shape = CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = when (badge.iconName) {
                                                    "palette" -> Icons.Default.Palette
                                                    "landmark" -> Icons.Default.AccountBalance
                                                    "star" -> Icons.Default.RateReview
                                                    "token" -> Icons.Default.LocalActivity
                                                    "bronze" -> Icons.Default.EmojiEvents
                                                    else -> Icons.Default.WorkspacePremium
                                                },
                                                contentDescription = badge.title,
                                                tint = if (badge.isUnlocked) MontpellierOrangePrimary else Color(0xFF94A3B8),
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = badge.title,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (badge.isUnlocked) MontpellierNavyDark else Color(0xFF94A3B8),
                                            textAlign = TextAlign.Center,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                if (chunk.size < 3) {
                                    for (i in 1..(3 - chunk.size)) {
                                        Box(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // ==========================================
            // 🍇 MERCHANT / COMMERÇANT SPACE
            // ==========================================
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 64.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Subsection 1: Selected active stand selector
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MontpellierNavySurface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "MON STAND COMMERÇANT DES ESTIVALES",
                                color = MontpellierNavyMedium,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Stand Actif: $mockCommercantName 🍇",
                                color = MontpellierNavyDark,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            val stands = listOf("Vignoble Saint-Jean", "Huîtres de Bouzigues", "Languedoc Gourmet", "Olives et Clapas")
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                stands.forEach { stand ->
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (mockCommercantName == stand) MontpellierOrangePrimary else Color.White,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .border(1.dp, if (mockCommercantName == stand) MontpellierOrangePrimary else Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                                            .clickable { mockCommercantName = stand }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = stand,
                                            fontSize = 11.sp,
                                            color = if (mockCommercantName == stand) Color.White else MontpellierNavyDark,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // --- NEW : CREATE AND LINK ADVANTAGE TICKET ---
                item {
                    Text(
                        text = "🎟️ Créer & Lier un Ticket Avantage des Estivales",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MontpellierNavyDark
                    )
                }

                item {
                    val quota by viewModel.merchantTicketQuota.collectAsStateWithLifecycle()
                    val activeTickets by viewModel.activeMerchantTickets.collectAsStateWithLifecycle()
                    
                    var ticketDescInput by remember { mutableStateOf("") }
                    var creationFeedbackMsg by remember { mutableStateOf("") }
                    var merchantEnigmaInput by remember { mutableStateOf("Sur quelle célèbre place de Montpellier se trouve la Fontaine des Trois Grâces ?") }
                    var merchantRiddleLat by remember { mutableStateOf("43.6085") }
                    var merchantRiddleLng by remember { mutableStateOf("3.8794") }
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.2.dp, MontpellierNavyPrimary.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "CONFIGURATION DU BON AVANTAGE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MontpellierNavyMedium
                                )
                                // Quota badge
                                Box(
                                    modifier = Modifier
                                        .background(if (quota > 0) Color(0xFFD1FAE5) else Color(0xFFFEE2E2), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "Forfait : $quota restant(s)",
                                        color = if (quota > 0) Color(0xFF065F46) else Color(0xFF991B1B),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Écrivez vous-même ce que contient ce ticket d'avantage (consommation gratuite, pourcentage, etc.). Une fois validé, il sera AUTOMATIQUEMENT et au HASARD rattaché à l'un de nos jeux du Clapas. Tous les utilisateurs recevront une notification en temps réel !",
                                fontSize = 11.sp,
                                color = MontpellierNavyLight,
                                lineHeight = 15.sp
                            )
                            
                            Spacer(modifier = Modifier.height(14.dp))
                            
                            OutlinedTextField(
                                value = ticketDescInput,
                                onValueChange = { ticketDescInput = it },
                                label = { Text("Ex: Un verre de muscat offert, -15% sur notre stand...", fontSize = 11.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("ticket_description_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MontpellierOrangePrimary,
                                    unfocusedBorderColor = Color(0xFFCBD5E1)
                                ),
                                maxLines = 2,
                                shape = RoundedCornerShape(8.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(14.dp))
                            
                            // Game Selector
                            Text(
                                text = "🎮 1. Sélectionnez le jeu à lier :",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MontpellierNavyDark
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            val gamesOptions = listOf(
                                "Bubble Shooter de l'Écusson 🎯",
                                "L'Envol du Goéland 🐦",
                                "Défi Chrono Mosaïques ⏱️",
                                "Chasse BMX Street-Art 🚲",
                                "Quiz de l'Écusson Historique 🎓",
                                "Jeu de Piste Géolocalisé 🗺️"
                            )
                            var chosenGameToDeploy by remember { mutableStateOf("Bubble Shooter de l'Écusson 🎯") }
                            var gameDurationHours by remember { mutableStateOf(12f) } // default 12h, slider 5..24

                            gamesOptions.forEach { gameItem ->
                                val selected = chosenGameToDeploy == gameItem
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                        .clickable { chosenGameToDeploy = gameItem },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selected) MontpellierNavyPrimary.copy(alpha = 0.08f) else Color.Transparent
                                    ),
                                    border = BorderStroke(
                                        1.2.dp,
                                        if (selected) MontpellierNavyPrimary else Color(0xFFE2E8F0)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = selected,
                                            onClick = { chosenGameToDeploy = gameItem },
                                            colors = RadioButtonDefaults.colors(selectedColor = MontpellierNavyPrimary)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = gameItem,
                                            fontSize = 11.sp,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                            color = MontpellierNavyDark
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                             if (chosenGameToDeploy == "Jeu de Piste Géolocalisé 🗺️") {
                                 Spacer(modifier = Modifier.height(10.dp))
                                 Card(
                                     modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                     colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                     border = BorderStroke(1.2.dp, MontpellierNavyPrimary.copy(alpha = 0.25f)),
                                     shape = RoundedCornerShape(10.dp)
                                 ) {
                                     Column(modifier = Modifier.padding(12.dp)) {
                                         Text(
                                             text = "📍 CONFIGURATION DE LA PISTE GÉOLOCALISÉE",
                                             fontSize = 10.sp,
                                             fontWeight = FontWeight.Bold,
                                             color = MontpellierNavyPrimary,
                                             letterSpacing = 0.5.sp
                                         )
                                         Spacer(modifier = Modifier.height(8.dp))
                                         OutlinedTextField(
                                             value = merchantEnigmaInput,
                                             onValueChange = { merchantEnigmaInput = it },
                                             label = { Text("Énigme à résoudre par le joueur", fontSize = 11.sp) },
                                             modifier = Modifier.fillMaxWidth(),
                                             colors = OutlinedTextFieldDefaults.colors(
                                                 focusedBorderColor = MontpellierOrangePrimary,
                                                 unfocusedBorderColor = Color(0xFFCBD5E1)
                                             ),
                                             shape = RoundedCornerShape(8.dp),
                                             maxLines = 3
                                         )
                                         Spacer(modifier = Modifier.height(8.dp))
                                         Row(
                                             modifier = Modifier.fillMaxWidth(),
                                             horizontalArrangement = Arrangement.spacedBy(8.dp)
                                         ) {
                                             OutlinedTextField(
                                                 value = merchantRiddleLat,
                                                 onValueChange = { merchantRiddleLat = it },
                                                 label = { Text("Latitude cible (ex: 43.6085)", fontSize = 11.sp) },
                                                 modifier = Modifier.weight(1f),
                                                 colors = OutlinedTextFieldDefaults.colors(
                                                     focusedBorderColor = MontpellierOrangePrimary,
                                                     unfocusedBorderColor = Color(0xFFCBD5E1)
                                                 ),
                                                 shape = RoundedCornerShape(8.dp)
                                             )
                                             OutlinedTextField(
                                                 value = merchantRiddleLng,
                                                 onValueChange = { merchantRiddleLng = it },
                                                 label = { Text("Longitude cible (ex: 3.8794)", fontSize = 11.sp) },
                                                 modifier = Modifier.weight(1f),
                                                 colors = OutlinedTextFieldDefaults.colors(
                                                     focusedBorderColor = MontpellierOrangePrimary,
                                                     unfocusedBorderColor = Color(0xFFCBD5E1)
                                                 ),
                                                 shape = RoundedCornerShape(8.dp)
                                             )
                                         }
                                         Spacer(modifier = Modifier.height(6.dp))
                                         Text(
                                             text = "💡 Exemples de Montpellier : Place de la Comédie [43.6085, 3.8794], Arc de Triomphe du Peyrou [43.6111, 3.8715], Jardin des Plantes [43.6141, 3.8718], Église Saint-Roch [43.6080, 3.8770]",
                                             fontSize = 9.sp,
                                             color = MontpellierNavyLight,
                                             lineHeight = 12.sp,
                                             fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                         )
                                     }
                                 }
                             }

                             Spacer(modifier = Modifier.height(14.dp))

                            // Duration Selector
                            Text(
                                text = "⏳ 2. Durée de mise en ligne : ${gameDurationHours.toInt()} heures (de 5h à 24h)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MontpellierNavyDark
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Slider(
                                value = gameDurationHours,
                                onValueChange = { gameDurationHours = it.coerceIn(5f, 24f) },
                                valueRange = 5f..24f,
                                steps = 18, // 5 to 24 results in 1-hour step increments
                                colors = SliderDefaults.colors(
                                    thumbColor = MontpellierOrangePrimary,
                                    activeTrackColor = MontpellierOrangePrimary,
                                    inactiveTrackColor = Color(0xFFE2E8F0)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(14.dp))
                            
                            Button(
                                onClick = {
                                    if (ticketDescInput.isNotBlank() && quota > 0) {
                                        val testSuccess = viewModel.createAndLinkAdvantageTicket(
                                             enigma = if (chosenGameToDeploy == "Jeu de Piste Géolocalisé 🗺️") merchantEnigmaInput else null,
                                             lat = if (chosenGameToDeploy == "Jeu de Piste Géolocalisé 🗺️") (merchantRiddleLat.toDoubleOrNull() ?: 43.6085) else null,
                                             lng = if (chosenGameToDeploy == "Jeu de Piste Géolocalisé 🗺️") (merchantRiddleLng.toDoubleOrNull() ?: 3.8794) else null,
                                            merchantName = mockCommercantName,
                                            description = ticketDescInput,
                                            gameTitle = chosenGameToDeploy,
                                            durationHours = gameDurationHours.toInt()
                                        )
                                        if (testSuccess) {
                                            creationFeedbackMsg = "✅ Succès ! Votre bon '$ticketDescInput' est activé sur le jeu '${chosenGameToDeploy}' pour ${gameDurationHours.toInt()}h. Une notification d'événement a été envoyée !"
                                            ticketDescInput = ""
                                        } else {
                                            creationFeedbackMsg = "❌ Échec de la création. Forfait de tickets épuisé pour ce soir."
                                        }
                                    }
                                },
                                enabled = quota > 0 && ticketDescInput.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = MontpellierOrangePrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("launch_ticket_button")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Share, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Lancer le Jeu en Lenge avec son Ticket 🚀", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            
                            if (creationFeedbackMsg.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (creationFeedbackMsg.contains("✅")) Color(0xFFECFDF5) else Color(0xFFFEF2F2)
                                    ),
                                    border = BorderStroke(1.dp, if (creationFeedbackMsg.contains("✅")) Color(0xFF10B981) else Color(0xFFEF4444)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = creationFeedbackMsg,
                                        color = if (creationFeedbackMsg.contains("✅")) Color(0xFF047857) else Color(0xFFB91C1C),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(10.dp),
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                            
                            // Disp active tickets if any exists
                            val myActiveBons = activeTickets.filter { it.merchantName == mockCommercantName }
                            if (myActiveBons.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Text("💡 Vos tickets actuellement en ligne :", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MontpellierNavyDark)
                                Spacer(modifier = Modifier.height(6.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    myActiveBons.forEach { t ->
                                        val elapsed = System.currentTimeMillis() - t.dateCreated
                                        val totalMs = t.onlineDurationHours * 3600 * 1000L
                                        val remainingMs = totalMs - elapsed
                                        val active = remainingMs > 0
                                        
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(MontpellierNavySurface, RoundedCornerShape(8.dp))
                                                .padding(8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(t.description, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MontpellierNavyDark)
                                                Text("Lié au jeu : ${t.linkedGameTitle}", fontSize = 9.sp, color = MontpellierNavyLight)
                                                val countdownText = if (active) {
                                                    val hoursLeft = remainingMs / (1000 * 3600)
                                                    val minsLeft = (remainingMs % (1000 * 3600)) / (1000 * 60)
                                                    "Temps restant : ${hoursLeft}h ${minsLeft}m ⌛ (Durée tot. ${t.onlineDurationHours}h)"
                                                } else {
                                                    "Expiré (Il y a ${(elapsed - totalMs) / (1000 * 3600)}h)"
                                                }
                                                Text(countdownText, fontSize = 8.sp, color = if (active) MontpellierOrangePrimary else Color.Red, fontWeight = FontWeight.Bold)
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .background(if (active) MontpellierOrangePrimary.copy(alpha = 0.15f) else Color.Red.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                                            ) {
                                                Text(
                                                    text = if (active) "En Ligne" else "Expiré",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (active) MontpellierOrangePrimary else Color.Red
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Subsection 2: Statistics & Companion metrics ("les statistiques et Cie.")
                item {
                    Text(
                        text = "📊 Statistiques & Performances Stand",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MontpellierNavyDark
                    )
                }

                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Icon(Icons.Default.ConfirmationNumber, null, tint = MontpellierOrangePrimary, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Jetons Validés", fontSize = 10.sp, color = MontpellierNavyLight, fontWeight = FontWeight.Bold)
                                    Text("$merchantValidatedCount Tx", fontSize = 18.sp, fontWeight = FontWeight.Black, color = MontpellierNavyDark)
                                }
                            }
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Icon(Icons.Default.Stars, null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Chiffre d'Affaires", fontSize = 10.sp, color = MontpellierNavyLight, fontWeight = FontWeight.Bold)
                                    Text("${merchantValidatedCount * 6.00} €", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF10B981))
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Icon(Icons.Default.Group, null, tint = MontpellierNavyPrimary, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Visiteurs Estimés", fontSize = 10.sp, color = MontpellierNavyLight, fontWeight = FontWeight.Bold)
                                    Text("${(merchantValidatedCount * 1.6).toInt()} Claps", fontSize = 18.sp, fontWeight = FontWeight.Black, color = MontpellierNavyDark)
                                }
                            }
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Icon(Icons.Default.TrendingUp, null, tint = MontpellierOrangeAccent, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Popularité Stand", fontSize = 10.sp, color = MontpellierNavyLight, fontWeight = FontWeight.Bold)
                                    Text("4.9 ★ Excellent", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MontpellierNavyDark)
                                }
                            }
                        }
                    }
                }

                // Interactive peak hours validation chart drawn dynamically on Canvas
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "📈 CRÉNEAUX D'AFFLUENCE ESTIVALES (Jetons/Heure)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MontpellierNavyMedium,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(115.dp)
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val barWidth = 40f
                                    val spacing = (size.width - (5 * barWidth)) / 6f
                                    val dataPoints = listOf(3f, 8f, 15f, 24f, 12f) // validations for 18h, 19h, 20h, 21h, 22h
                                    val maxVal = 25f
                                    
                                    // Draw dotted horizontal grid lines
                                    drawLine(
                                        color = Color(0xFFE8EDF2),
                                        start = Offset(0f, size.height * 0.35f),
                                        end = Offset(size.width, size.height * 0.35f),
                                        strokeWidth = 2f
                                    )
                                    drawLine(
                                        color = Color(0xFFE8EDF2),
                                        start = Offset(0f, size.height * 0.7f),
                                        end = Offset(size.width, size.height * 0.7f),
                                        strokeWidth = 2f
                                    )

                                    // Draw bars
                                    dataPoints.forEachIndexed { idx, value ->
                                        val x = spacing + idx * (barWidth + spacing)
                                        val barHeight = (value / maxVal) * size.height
                                        val y = size.height - barHeight
                                        
                                        // Colors: orange accent for busiest hour (idx=3, 21h), blue/navy for others
                                        val color = if (idx == 3) MontpellierOrangePrimary else MontpellierNavyPrimary.copy(alpha = 0.75f)
                                        
                                        drawRoundRect(
                                            color = color,
                                            topLeft = Offset(x, y),
                                            size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                val hoursLabels = listOf("18h", "19h", "20h", "21h (Peak)", "22h")
                                hoursLabels.forEachIndexed { i, lbl ->
                                    Text(
                                        text = lbl,
                                        fontSize = 9.sp,
                                        fontWeight = if (i == 3) FontWeight.Bold else FontWeight.Normal,
                                        color = if (i == 3) MontpellierOrangePrimary else MontpellierNavyLight
                                    )
                                }
                            }
                        }
                    }
                }

                // QR Scanner Emulator with validation action shortcut
                item {
                    Text(
                        text = "📷 Terminal de Validation & Scan Rapide",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MontpellierNavyDark
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "RACCOURCI VALIDATEUR SANS CONTACT ⚡️",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MontpellierOrangePrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Débitez instantanément un voucher de dégustation auprès du stand $mockCommercantName",
                                fontSize = 11.sp,
                                color = MontpellierNavyLight,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Viewfinder simulation
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .background(Color.Black, RoundedCornerShape(10.dp))
                                    .border(1.5.dp, MontpellierOrangePrimary, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.QrCodeScanner, null, tint = Color.Green, modifier = Modifier.size(28.dp))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Flux Caméra d'Encaisser Actif [Simulation]", fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Action Shortcut Button
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        scanResultLog = "Scannage du code client..."
                                        delay(1000)
                                        val currentWallet = viewModel.wallet.value
                                        if ((currentWallet?.tokens ?: 0) > 0) {
                                            viewModel.useToken(mockCommercantName)
                                            merchantValidatedCount += 1
                                            scanResultLog = "✅ JETON ACCÉPTÉ: 1 jeton débité avec succès chez $mockCommercantName !"
                                        } else {
                                            scanResultLog = "❌ ERREUR: QR Code non validé. Solde insuffisant (0 jetons)."
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MontpellierOrangePrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.QrCodeScanner, null, modifier = Modifier.size(16.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Scanner un Voucher Client 🐾", color = Color.White, fontSize = 12.sp)
                            }

                            if (scanResultLog.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (scanResultLog.contains("✅")) Color(0xFFECFDF5) else Color(0xFFFEF2F2)
                                    ),
                                    border = BorderStroke(1.dp, if (scanResultLog.contains("✅")) Color(0xFF10B981) else Color(0xFFEF4444)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = scanResultLog,
                                        color = if (scanResultLog.contains("✅")) Color(0xFF047857) else Color(0xFFB91C1C),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(8.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Purchase Dialog Simulator
    if (buyDialogShown) {
        Dialog(onDismissRequest = { buyDialogShown = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(2.dp, MontpellierOrangePrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Achat de Jetons Estivales 🍷",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MontpellierNavyDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Pour continuer la dégustation au cœur du Clapas, achetez de nouveaux jetons ou simulez de nouvelles entrées !",
                        fontSize = 12.sp,
                        color = MontpellierNavyMedium,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.buyTokens(5)
                                buyDialogShown = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MontpellierOrangePrimary),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("+5 Jetons", fontSize = 11.sp, color = Color.White)
                        }
                        
                        Button(
                            onClick = {
                                viewModel.buyTokens(1)
                                buyDialogShown = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MontpellierNavyPrimary),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("+1 Jeton", fontSize = 11.sp, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(onClick = { buyDialogShown = false }) {
                        Text("Annuler", color = MontpellierNavyLight)
                    }
                }
            }
        }
    }

    // --- POPUP REWARD QR DISPLAY VIRTUAL CARD (WITH MOCK INTEGRATED SCANNER AND SECURITY STAMP) ---
    selectedQrDialog?.let { data ->
        val matchingTicket = wonAdvantageTickets.find { it.qrCodeHex == data.second }
        val infiniteTransition = rememberInfiniteTransition(label = "laser")
        val laserFraction by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2200, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "laserAnimation"
        )

        Dialog(onDismissRequest = { selectedQrDialog = null }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(2.dp, if (matchingTicket?.isUsed == true) Color(0xFF94A3B8) else MontpellierNavyPrimary),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (matchingTicket != null) "Bon d'Avantage Estivales 🍇" else "Ticket Récompense 🎟️",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MontpellierNavyDark
                        )
                        IconButton(
                            onClick = { selectedQrDialog = null },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, null, tint = MontpellierNavyLight)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Text(
                        text = data.first,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (matchingTicket?.isUsed == true) Color(0xFF64748B) else MontpellierOrangePrimary,
                        textAlign = TextAlign.Center
                    )
                    
                    if (matchingTicket != null) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Par le commerçant : ${matchingTicket.merchantName}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MontpellierNavyMedium
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (matchingTicket?.isUsed == true) 
                            "Cet avantage a bien été consommé et validé auprès du commerçant."
                            else "Veuillez présenter ce code QR unique sur le stand du commerçant pour scanner l'avantage.",
                        fontSize = 10.sp,
                        color = MontpellierNavyLight,
                        textAlign = TextAlign.Center,
                        lineHeight = 14.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // QR CONTAINER WITH LASER OR DIAGONAL STAMP
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .background(Color.White)
                            .border(1.5.dp, if (matchingTicket?.isUsed == true) Color(0xFFEF4444).copy(alpha = 0.5f) else MontpellierNavyPrimary, RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Canvas(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer(alpha = if (matchingTicket?.isUsed == true) 0.15f else 1.0f)
                            ) {
                                val unitSize = 35f
                                drawRect(color = Color.Black, size = androidx.compose.ui.geometry.Size(unitSize, unitSize), topLeft = Offset(0f, 0f))
                                drawRect(color = Color.Black, size = androidx.compose.ui.geometry.Size(unitSize, unitSize), topLeft = Offset(size.width - unitSize, 0f))
                                drawRect(color = Color.Black, size = androidx.compose.ui.geometry.Size(unitSize, unitSize), topLeft = Offset(0f, size.height - unitSize))
                                
                                drawRect(color = Color.White, size = androidx.compose.ui.geometry.Size(15f, 15f), topLeft = Offset(10f, 10f))
                                drawRect(color = Color.White, size = androidx.compose.ui.geometry.Size(15f, 15f), topLeft = Offset(size.width - 25f, 10f))
                                drawRect(color = Color.White, size = androidx.compose.ui.geometry.Size(15f, 15f), topLeft = Offset(10f, size.height - 25f))

                                drawRect(color = Color.Black, size = androidx.compose.ui.geometry.Size(12f, 12f), topLeft = Offset(55f, 55f))
                                drawRect(color = Color.Black, size = androidx.compose.ui.geometry.Size(15f, 15f), topLeft = Offset(80f, 25f))
                                drawRect(color = Color.Black, size = androidx.compose.ui.geometry.Size(12f, 15f), topLeft = Offset(110f, 50f))
                                drawRect(color = Color.Black, size = androidx.compose.ui.geometry.Size(20f, 12f), topLeft = Offset(35f, 95f))
                                drawRect(color = Color.Black, size = androidx.compose.ui.geometry.Size(15f, 15f), topLeft = Offset(130f, 125f))
                                drawRect(color = Color.Black, size = androidx.compose.ui.geometry.Size(14f, 14f), topLeft = Offset(95f, 95f))
                                drawRect(color = Color.Black, size = androidx.compose.ui.geometry.Size(15f, 15f), topLeft = Offset(70f, 130f))
                                drawRect(color = Color.Black, size = androidx.compose.ui.geometry.Size(18f, 18f), topLeft = Offset(135f, 80f))
                            }
                            
                            // Laser line animation (only active when NOT used)
                            if (matchingTicket == null || !matchingTicket.isUsed) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(3.dp)
                                        .align(Alignment.TopCenter)
                                        .offset(y = 136.dp * laserFraction)
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color(0xFF10B981).copy(alpha = 0.2f),
                                                    Color(0xFF10B981),
                                                    Color(0xFF10B981).copy(alpha = 0.2f)
                                                )
                                            )
                                        )
                                )
                            } else {
                                // Diagonal STAMP validation
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .align(Alignment.Center),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "UTILISÉ 🍷\nESTIVALES",
                                        color = Color(0xFFEF4444),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .border(3.dp, Color(0xFFEF4444), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                            .graphicsLayer(rotationZ = -22f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = data.second,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (matchingTicket?.isUsed == true) Color(0xFF94A3B8) else MontpellierNavyDark,
                        letterSpacing = 1.sp,
                        modifier = Modifier
                            .background(Color(0xFFF1F5F9), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // LIVE SIMULATOR FOR MERCHANT SCANNER
                    if (matchingTicket != null && !matchingTicket.isUsed) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                            border = BorderStroke(1.dp, Color(0xFFFBBF24)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "📱 [TEST] SYSTÈME DE SCAN DU COMMERÇANT",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB45309)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "En situation réelle, le commerçant scanne ce QR Code depuis son écran de contrôle pour débiter l'usage unique.",
                                    fontSize = 9.sp,
                                    color = Color(0xFF78350F),
                                    textAlign = TextAlign.Center,
                                    lineHeight = 12.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        viewModel.debitWonTicket(matchingTicket.id)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("simulate_merchant_scan_button")
                                ) {
                                    Text("Simuler la validation du Stand ✅", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else if (matchingTicket?.isUsed == true) {
                        // Success confirmation feedback
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                            border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Verified, null, tint = Color(0xFF16A34A), modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "VALIDÉ AVEC SUCCÈS !",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF16A34A)
                                    )
                                    Text(
                                        text = "Ce QR code d'avantage à usage unique a été composté avec succès.",
                                        fontSize = 9.sp,
                                        color = Color(0xFF15803D),
                                        lineHeight = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { selectedQrDialog = null },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (matchingTicket?.isUsed == true) Color(0xFF64748B) else MontpellierNavyPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (matchingTicket?.isUsed == true) "Fermer le reçu 📜" else "Fermer le voucher 🎟️",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------------------------------
// 5. IMMERSIVE VERTICAL STORY PLAYER DIALOG
// ---------------------------------------------------------------------------------------------------------------------

@Composable
fun StoryPlayer(
    story: Story,
    onDismiss: () -> Unit
) {
    var progress by remember { mutableStateOf(0f) }
    
    // Auto increment visual progress line mimicking standard Instagram Stories
    LaunchedEffect(story) {
        while (progress < 1.0f) {
            delay(50)
            progress += 0.01f
        }
        onDismiss()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable { onDismiss() }
        ) {
            // High-fidelity background representing sunset/sea of Montpellier
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = when (story.id) {
                                "story_1" -> listOf(Color(0xFFFC9842), Color(0xFFFE5F75)) // Spicy sunset
                                "story_2" -> listOf(Color(0xFF5E2563), Color(0xFF650B0B)) // Vintage wine purple
                                else -> listOf(Color(0xFF185A9D), Color(0xFF43CEA2)) // Monsieur bmx turquoise
                            }
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Progressive linear loading indicators
                Column {
                    Spacer(modifier = Modifier.height(24.dp))
                    LinearProgressIndicator(
                        progress = progress,
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.3f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color.White.copy(alpha = 0.3f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = MontpellierOrangePrimary
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "L'Actu Clapassière",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = story.location,
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                                .size(32.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close story", tint = Color.White)
                        }
                    }
                }

                // Middle Text Message
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {
                    Text(
                        text = story.title,
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        lineHeight = 38.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = story.subtitle,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 22.sp
                    )
                }

                // Actions indicator at the bottom
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = "Glisser ou tapoter pour fermer l'histoire",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------------------------------
// 6. ESTABLISHMENT DETAILED SHEET & REVIEWS FORM (PRD A: Fiches Établissements)
// ---------------------------------------------------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstablishmentDetailSheet(
    establishment: Establishment,
    viewModel: GuidementViewModel,
    onDismiss: () -> Unit
) {
    val reviews by viewModel.getReviews(establishment.id).collectAsStateWithLifecycle(initialValue = emptyList())
    
    var userRating by remember { mutableStateOf(5) }
    var userComment by remember { mutableStateOf("") }
    var reviewerName by remember { mutableStateOf("") }

    // Use full fullscreen dialog styled beautifully to work as a sheet on both small and large screens
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = establishment.name,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.toggleFavorite(establishment) }) {
                            Icon(
                                imageVector = if (establishment.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = null,
                                tint = if (establishment.isFavorite) Color.Red else MontpellierNavyDark
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White,
                        titleContentColor = MontpellierNavyDark
                    )
                )
            },
            modifier = Modifier.fillMaxSize()
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFF8FAFC))
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Visual Brand Representation
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = when (establishment.category) {
                                    "RESTAURANT" -> listOf(Color(0xFFE29578), Color(0xFFE76F51))
                                    "BAR" -> listOf(Color(0xFF2E86AB), Color(0xFF1B4965))
                                    else -> listOf(Color(0xFFF4A261), Color(0xFFE76F51))
                                }
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.align(Alignment.BottomStart)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = establishment.category,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (establishment.isMagnonLabel) {
                                Box(
                                    modifier = Modifier
                                        .background(MontpellierOrangePrimary, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("Recommandé RPPLC 🐾", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Title info & ratings
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Fiche Établissement",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MontpellierOrangePrimary
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .background(Color(0xFFFEF3C7), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${establishment.rating} / 5",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp,
                                        color = Color(0xFF92400E)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = establishment.description,
                                color = SlateDark,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            Divider(color = Color(0xFFF1F5F9))
                            Spacer(modifier = Modifier.height(12.dp))

                            // Details
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = MontpellierNavyPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = establishment.address, fontSize = 12.sp, color = MontpellierNavyDark, fontWeight = FontWeight.Medium)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = MontpellierNavyPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = establishment.phoneNumber, fontSize = 12.sp, color = MontpellierNavyDark, fontWeight = FontWeight.Medium)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccessTime, contentDescription = null, tint = MontpellierNavyPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = establishment.hours, fontSize = 12.sp, color = MontpellierNavyDark, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    // "Le Verdict" critique of the Expert
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MontpellierOrangeLight.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, MontpellierOrangePrimary.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.WorkspacePremium,
                                    contentDescription = null,
                                    tint = MontpellierOrangePrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "L'AVIS DU PETIT CLAPAS 🐾",
                                    color = MontpellierOrangePrimary,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp,
                                    letterSpacing = 1.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = establishment.verdict,
                                color = SlateDark,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    // Reviews List
                    Text(
                        text = "Avis des Clapassiers (${reviews.size})",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = MontpellierNavyDark
                    )

                    if (reviews.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Box(
                                modifier = Modifier.padding(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Soyez le premier à rédiger un avis !", fontSize = 12.sp, color = MontpellierNavyLight)
                            }
                        }
                    } else {
                        reviews.forEach { review ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = review.userName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = MontpellierNavyDark
                                        )
                                        Row {
                                            repeat(review.rating.toInt()) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = null,
                                                    tint = Color(0xFFF59E0B),
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = review.comment,
                                        fontSize = 12.sp,
                                        color = SlateDark
                                    )
                                }
                            }
                        }
                    }

                    // Rédiger un avis Form (+10 points promotion)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Réclamer vos points : Rédiger un avis (+10 Pts) 🎉",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MontpellierNavyDark
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = reviewerName,
                                onValueChange = { reviewerName = it },
                                placeholder = { Text("Votre pseudo (ex: Chasseur34)", fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Stars selection layout
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Note :", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = SlateDark)
                                repeat(5) { index ->
                                    val starNum = index + 1
                                    IconButton(
                                        onClick = { userRating = starNum },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (userRating >= starNum) Icons.Default.Star else Icons.Default.StarBorder,
                                            contentDescription = null,
                                            tint = if (userRating >= starNum) Color(0xFFF59E0B) else MontpellierNavyLight
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = userComment,
                                onValueChange = { userComment = it },
                                placeholder = { Text("Votre verdict sur l'établissement... qu'en pensez-vous ?", fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 4,
                                shape = RoundedCornerShape(8.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    if (userComment.isNotBlank()) {
                                        viewModel.addReview(
                                            establishmentId = establishment.id,
                                            userName = reviewerName,
                                            rating = userRating.toFloat(),
                                            comment = userComment
                                        )
                                        // clear
                                        reviewerName = ""
                                        userComment = ""
                                        userRating = 5
                                    }
                                },
                                enabled = userComment.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = MontpellierOrangePrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Enregistrer mon verdict d'estivant (+10 Pts)", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Custom Modifier extension to ensure we keep touch targets compliant while keeping visually compact layouts
 */
fun Modifier.keepTouchTargetCompact(): Modifier = this.defaultMinSize(minWidth = 32.dp, minHeight = 32.dp)
