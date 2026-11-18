package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.QuestionEntity
import com.example.domain.search.SearchFilter
import com.example.ui.components.QuestionCard
import com.example.ui.theme.AmberBadge
import com.example.ui.theme.DarkCanvas
import com.example.ui.theme.EmeraldBadgeBg
import com.example.ui.theme.EmeraldHeaderBottom
import com.example.ui.theme.EmeraldHeaderTop
import com.example.ui.theme.EmeraldPillBg
import com.example.ui.theme.EmeraldPillBorder
import com.example.ui.theme.IconContainerDark
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.MintAccent
import com.example.ui.theme.OnMintAccent
import com.example.ui.theme.SearchActionGreen
import com.example.ui.theme.SearchCardBg
import com.example.ui.theme.SearchCardPlaceholder
import com.example.ui.theme.SearchCardText
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.HomeUiState

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onQueryChanged: (String) -> Unit,
    onClearSearch: () -> Unit,
    onExecuteSearch: (String?) -> Unit,
    onFilterSelected: (SearchFilter) -> Unit,
    onNavigateToUpload: () -> Unit,
    onNavigateToDatabase: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onToggleFavorite: (QuestionEntity) -> Unit,
    onEditQuestion: (QuestionEntity) -> Unit,
    onCopied: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkCanvas)
    ) {
        // Deep Emerald Brand & Search Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(EmeraldHeaderTop, EmeraldHeaderBottom)
                    )
                )
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 840.dp)
                    .align(Alignment.Center)
            ) {
                // Brand row + Offline badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.brand_title),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = Color.White
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(AmberBadge)
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.brand_badge),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp
                                    ),
                                    color = Color.White
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.brand_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFD6E6FA)
                        )
                    }

                    // Offline status pill
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(EmeraldPillBg)
                            .border(1.dp, EmeraldPillBorder, RoundedCornerShape(50))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CloudOff,
                            contentDescription = null,
                            tint = Color(0xFFE8F1FC),
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = stringResource(R.string.status_offline),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = Color(0xFFE8F1FC)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // White Search Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = SearchCardBg,
                    shadowElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(
                            start = 16.dp,
                            end = 14.dp,
                            top = 14.dp,
                            bottom = 12.dp
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = null,
                                tint = SearchCardText,
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(top = 2.dp)
                            ) {
                                if (uiState.query.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.search_placeholder),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = SearchCardPlaceholder
                                    )
                                }
                                BasicTextField(
                                    value = uiState.query,
                                    onValueChange = onQueryChanged,
                                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                                        color = SearchCardText,
                                        lineHeight = 22.sp
                                    ),
                                    cursorBrush = SolidColor(SearchActionGreen),
                                    maxLines = 4,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                    keyboardActions = KeyboardActions(
                                        onSearch = {
                                            keyboardController?.hide()
                                            onExecuteSearch(null)
                                        }
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("home_search_input")
                                )
                            }

                            if (uiState.query.isNotEmpty()) {
                                IconButton(
                                    onClick = onClearSearch,
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("clear_search_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = stringResource(R.string.action_clear_search),
                                        tint = SearchCardPlaceholder,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.search_modes_caption),
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                color = SearchCardPlaceholder,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    keyboardController?.hide()
                                    onExecuteSearch(null)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SearchActionGreen,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("search_submit_button")
                            ) {
                                Text(
                                    text = stringResource(R.string.action_search),
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Scrollable Body
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp)
                .align(Alignment.CenterHorizontally),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Two Stats Cards Row
            item(key = "stats_row") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        icon = Icons.Filled.Quiz,
                        count = uiState.totalQuestions,
                        label = stringResource(R.string.stat_saved_questions),
                        onClick = onNavigateToDatabase,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("stat_questions_card")
                    )
                    StatCard(
                        icon = Icons.Filled.Folder,
                        count = uiState.totalDocuments,
                        label = stringResource(R.string.stat_source_docs),
                        onClick = onNavigateToUpload,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("stat_documents_card")
                    )
                }
            }

            // Menu Utama Section
            item(key = "main_menu") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = stringResource(R.string.section_main_menu),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = TextPrimary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MainMenuCard(
                            icon = Icons.Filled.UploadFile,
                            label = stringResource(R.string.menu_upload),
                            onClick = onNavigateToUpload,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("menu_upload_card")
                        )
                        MainMenuCard(
                            icon = Icons.Filled.ListAlt,
                            label = stringResource(R.string.menu_database),
                            onClick = onNavigateToDatabase,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("menu_database_card")
                        )
                        MainMenuCard(
                            icon = Icons.Filled.Star,
                            label = stringResource(R.string.menu_favorite),
                            onClick = onNavigateToFavorites,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("menu_favorite_card")
                        )
                        MainMenuCard(
                            icon = Icons.Filled.History,
                            label = stringResource(R.string.menu_history),
                            onClick = onNavigateToHistory,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("menu_history_card")
                        )
                    }
                }
            }

            // Quick Try Queries when search bar is empty
            if (uiState.query.isBlank()) {
                item(key = "quick_examples") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Coba Pencarian Cepat",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val quickQueries = listOf(
                                "Bagus masuk dalam jenis toko beanspot concept?: Advance; Basic; Medium; Premium; RRI",
                                "Perbedaan FIFO dan FEFO",
                                "Pembayaran QRIS kasir"
                            )
                            quickQueries.forEach { sample ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(SurfaceCard)
                                        .border(1.dp, SurfaceBorder, RoundedCornerShape(50))
                                        .clickable { onExecuteSearch(sample) }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = sample,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MintAccent,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.widthIn(max = 220.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Results Header & Filter Chips
            item(key = "results_header") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val sectionTitle = if (uiState.query.isBlank()) {
                        "Bank Soal Siap Pakai (${uiState.filteredResults.size})"
                    } else {
                        "Hasil Pencarian (${uiState.filteredResults.size})"
                    }
                    Text(
                        text = sectionTitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = TextPrimary
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SearchFilter.entries.forEach { filter ->
                            val isSelected = uiState.selectedFilter == filter
                            val countForFilter = when (filter) {
                                SearchFilter.ALL -> uiState.allResults.size
                                SearchFilter.EXACT -> uiState.allResults.count {
                                    it.matchType == com.example.domain.search.MatchType.EXACT
                                }
                                SearchFilter.TF_IDF -> uiState.allResults.count {
                                    it.matchType == com.example.domain.search.MatchType.TF_IDF
                                }
                                SearchFilter.KEYWORD -> uiState.allResults.count {
                                    it.matchType == com.example.domain.search.MatchType.KEYWORD
                                }
                            }
                            val labelText = if (uiState.query.isNotBlank() && filter != SearchFilter.ALL && countForFilter > 0) {
                                "${filter.label} ($countForFilter)"
                            } else {
                                filter.label
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(if (isSelected) MintAccent else SurfaceCard)
                                    .border(
                                        1.dp,
                                        if (isSelected) MintAccent else SurfaceBorder,
                                        RoundedCornerShape(50)
                                    )
                                    .clickable { onFilterSelected(filter) }
                                    .padding(horizontal = 16.dp, vertical = 9.dp)
                                    .testTag("filter_chip_${filter.name.lowercase()}")
                            ) {
                                Text(
                                    text = labelText,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    ),
                                    color = if (isSelected) OnMintAccent else TextSecondary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // Results List or Empty State
            if (uiState.filteredResults.isEmpty()) {
                item(key = "empty_search") {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = SurfaceCard,
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, SurfaceBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SearchOff,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(40.dp)
                            )
                            Text(
                                text = "Soal belum ditemukan",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary
                            )
                            Text(
                                text = "Coba pilih filter 'Semua', ubah kata kunci, atau unggah dokumen bank soal baru di menu Upload.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            } else {
                items(
                    items = uiState.filteredResults,
                    key = { "${it.question.id}_${it.matchType.name}" }
                ) { result ->
                    QuestionCard(
                        question = result.question,
                        matchType = if (uiState.query.isBlank()) null else result.matchType,
                        scorePercent = if (uiState.query.isBlank()) null else result.scorePercent,
                        onToggleFavorite = { onToggleFavorite(result.question) },
                        onEdit = { onEditQuestion(result.question) },
                        onCopied = onCopied
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    count: Int,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        color = SurfaceCard,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, SurfaceBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(IconContainerDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MintAccent,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 21.sp
                    ),
                    color = TextPrimary,
                    maxLines = 1
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun MainMenuCard(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = SurfaceCard,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, SurfaceBorder)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(EmeraldBadgeBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MintAccent,
                    modifier = Modifier.size(23.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
