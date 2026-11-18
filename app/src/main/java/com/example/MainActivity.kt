package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.components.QuestionEditorDialog
import com.example.ui.screens.DatabaseScreen
import com.example.ui.screens.FavoritScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.RiwayatScreen
import com.example.ui.screens.UploadScreen
import com.example.ui.theme.BottomBarBg
import com.example.ui.theme.DarkCanvas
import com.example.ui.theme.KotaKalongTheme
import com.example.ui.theme.MintAccent
import com.example.ui.theme.MintAccentContainer
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCardElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.KotaKalongViewModel
import kotlinx.coroutines.launch

object KotaKalongRoutes {
    const val BERANDA = "beranda"
    const val UPLOAD = "upload"
    const val DATABASE = "database"
    const val FAVORIT = "favorit"
    const val RIWAYAT = "riwayat"
}

private data class BottomNavTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

private val bottomTabs = listOf(
    BottomNavTab(
        route = KotaKalongRoutes.BERANDA,
        label = "Beranda",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        testTag = "nav_tab_beranda"
    ),
    BottomNavTab(
        route = KotaKalongRoutes.UPLOAD,
        label = "Upload",
        selectedIcon = Icons.Filled.UploadFile,
        unselectedIcon = Icons.Outlined.UploadFile,
        testTag = "nav_tab_upload"
    ),
    BottomNavTab(
        route = KotaKalongRoutes.DATABASE,
        label = "Database",
        selectedIcon = Icons.Filled.ListAlt,
        unselectedIcon = Icons.Outlined.ListAlt,
        testTag = "nav_tab_database"
    ),
    BottomNavTab(
        route = KotaKalongRoutes.FAVORIT,
        label = "Favorit",
        selectedIcon = Icons.Filled.Star,
        unselectedIcon = Icons.Outlined.StarOutline,
        testTag = "nav_tab_favorit"
    )
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(BottomBarBg.toArgb())
        )
        setContent {
            KotaKalongTheme {
                KotaKalongApp()
            }
        }
    }
}

@Composable
fun KotaKalongApp(
    viewModel: KotaKalongViewModel = viewModel()
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: KotaKalongRoutes.BERANDA

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val homeState by viewModel.homeUiState.collectAsStateWithLifecycle()
    val documents by viewModel.sourceDocuments.collectAsStateWithLifecycle()
    val isImporting by viewModel.isImporting.collectAsStateWithLifecycle()
    val databaseQuestions by viewModel.filteredDatabaseQuestions.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val databaseQuery by viewModel.databaseQuery.collectAsStateWithLifecycle()
    val favorites by viewModel.filteredFavorites.collectAsStateWithLifecycle()
    val favoriteQuery by viewModel.favoriteQuery.collectAsStateWithLifecycle()
    val historyItems by viewModel.searchHistory.collectAsStateWithLifecycle()
    val editingQuestion by viewModel.editingQuestion.collectAsStateWithLifecycle()
    val showCreateDialog by viewModel.showCreateDialog.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    fun navigateTopTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun showCopiedMessage() {
        coroutineScope.launch {
            snackbarHostState.showSnackbar("Soal & kunci jawaban disalin ke clipboard.")
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCanvas)
    ) {
        val useNavigationRail = maxWidth >= 600.dp

        Scaffold(
            containerColor = DarkCanvas,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = SurfaceCardElevated,
                        contentColor = TextPrimary,
                        actionColor = MintAccent
                    )
                }
            },
            bottomBar = {
                if (!useNavigationRail && currentRoute != KotaKalongRoutes.RIWAYAT) {
                    NavigationBar(
                        containerColor = BottomBarBg,
                        contentColor = TextPrimary,
                        tonalElevation = 0.dp,
                        windowInsets = WindowInsets.navigationBars
                    ) {
                        bottomTabs.forEach { tab ->
                            val selected = currentRoute == tab.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateTopTab(tab.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MintAccent,
                                    selectedTextColor = MintAccent,
                                    indicatorColor = MintAccentContainer,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                ),
                                modifier = Modifier.testTag(tab.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavigationRail && currentRoute != KotaKalongRoutes.RIWAYAT) {
                    NavigationRail(
                        containerColor = BottomBarBg,
                        contentColor = TextPrimary,
                        windowInsets = WindowInsets.safeDrawing
                    ) {
                        bottomTabs.forEach { tab ->
                            val selected = currentRoute == tab.route
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateTopTab(tab.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.label,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = MintAccent,
                                    selectedTextColor = MintAccent,
                                    indicatorColor = MintAccentContainer,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                ),
                                modifier = Modifier.testTag(tab.testTag)
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    NavHost(
                        navController = navController,
                        startDestination = KotaKalongRoutes.BERANDA
                    ) {
                        composable(KotaKalongRoutes.BERANDA) {
                            HomeScreen(
                                uiState = homeState,
                                onQueryChanged = viewModel::onQueryChanged,
                                onClearSearch = viewModel::clearSearch,
                                onExecuteSearch = viewModel::executeSearch,
                                onFilterSelected = viewModel::selectFilter,
                                onNavigateToUpload = { navigateTopTab(KotaKalongRoutes.UPLOAD) },
                                onNavigateToDatabase = { navigateTopTab(KotaKalongRoutes.DATABASE) },
                                onNavigateToFavorites = { navigateTopTab(KotaKalongRoutes.FAVORIT) },
                                onNavigateToHistory = {
                                    navController.navigate(KotaKalongRoutes.RIWAYAT) {
                                        launchSingleTop = true
                                    }
                                },
                                onToggleFavorite = viewModel::toggleFavorite,
                                onEditQuestion = viewModel::openEditDialog,
                                onCopied = ::showCopiedMessage
                            )
                        }

                        composable(KotaKalongRoutes.UPLOAD) {
                            UploadScreen(
                                documents = documents,
                                isImporting = isImporting,
                                onImportUri = viewModel::importFileFromUri,
                                onImportPastedText = viewModel::importPastedText,
                                onOpenManualCreate = viewModel::openCreateDialog,
                                onDeleteDocument = viewModel::deleteDocument
                            )
                        }

                        composable(KotaKalongRoutes.DATABASE) {
                            DatabaseScreen(
                                questions = databaseQuestions,
                                categories = categories,
                                selectedCategory = selectedCategory,
                                searchQuery = databaseQuery,
                                onSearchQueryChanged = viewModel::onDatabaseQueryChanged,
                                onCategorySelected = viewModel::onCategorySelected,
                                onToggleFavorite = viewModel::toggleFavorite,
                                onEditQuestion = viewModel::openEditDialog,
                                onDeleteQuestion = viewModel::deleteQuestion,
                                onAddQuestionClick = viewModel::openCreateDialog,
                                onExportCsvUri = viewModel::exportAllQuestionsToUri,
                                onCopied = ::showCopiedMessage
                            )
                        }

                        composable(KotaKalongRoutes.FAVORIT) {
                            FavoritScreen(
                                favorites = favorites,
                                searchQuery = favoriteQuery,
                                onSearchQueryChanged = viewModel::onFavoriteQueryChanged,
                                onToggleFavorite = viewModel::toggleFavorite,
                                onEditQuestion = viewModel::openEditDialog,
                                onCopied = ::showCopiedMessage
                            )
                        }

                        composable(KotaKalongRoutes.RIWAYAT) {
                            RiwayatScreen(
                                historyItems = historyItems,
                                onBack = { navController.popBackStack() },
                                onReplaySearch = { query ->
                                    viewModel.executeSearch(query)
                                    navController.popBackStack()
                                    navigateTopTab(KotaKalongRoutes.BERANDA)
                                },
                                onDeleteItem = viewModel::deleteHistoryItem,
                                onClearAll = viewModel::clearAllHistory
                            )
                        }
                    }
                }
            }
        }
    }

    if (editingQuestion != null) {
        val current = editingQuestion!!
        QuestionEditorDialog(
            initialQuestion = current,
            onDismiss = viewModel::dismissEditDialog,
            onSave = { qText, optText, ansKey, expl, cat ->
                viewModel.saveEditedQuestion(current, qText, optText, ansKey, expl, cat)
            }
        )
    }

    if (showCreateDialog) {
        QuestionEditorDialog(
            initialQuestion = null,
            onDismiss = viewModel::dismissCreateDialog,
            onSave = viewModel::createManualQuestion
        )
    }
}
