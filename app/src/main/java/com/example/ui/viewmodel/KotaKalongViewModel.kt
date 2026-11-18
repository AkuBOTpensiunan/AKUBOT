package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.KotaKalongDatabase
import com.example.data.local.QuestionEntity
import com.example.data.local.SearchHistoryEntity
import com.example.data.local.SourceDocumentEntity
import com.example.data.repository.QuestionBankRepository
import com.example.domain.parser.DocumentImportParser
import com.example.domain.search.MatchType
import com.example.domain.search.OfflineSearchEngine
import com.example.domain.search.SearchFilter
import com.example.domain.search.SearchResultItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class HomeUiState(
    val query: String = "",
    val submittedQuery: String = "",
    val selectedFilter: SearchFilter = SearchFilter.ALL,
    val totalQuestions: Int = 0,
    val totalDocuments: Int = 0,
    val allResults: List<SearchResultItem> = emptyList(),
    val filteredResults: List<SearchResultItem> = emptyList(),
    val isSearching: Boolean = false,
    val hasSearched: Boolean = false
)

class KotaKalongViewModel(application: Application) : AndroidViewModel(application) {

    private val database = KotaKalongDatabase.getInstance(application)
    private val repository = QuestionBankRepository(
        questionDao = database.questionDao(),
        documentDao = database.sourceDocumentDao(),
        historyDao = database.searchHistoryDao()
    )
    private val searchEngine = OfflineSearchEngine()

    private val _searchQuery = MutableStateFlow("")
    private val _submittedQuery = MutableStateFlow("")
    private val _selectedFilter = MutableStateFlow(SearchFilter.ALL)
    private val _hasSearched = MutableStateFlow(false)
    private val _isSearching = MutableStateFlow(false)

    private val _databaseQuery = MutableStateFlow("")
    val databaseQuery: StateFlow<String> = _databaseQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("SEMUA")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _favoriteQuery = MutableStateFlow("")
    val favoriteQuery: StateFlow<String> = _favoriteQuery.asStateFlow()

    private val _editingQuestion = MutableStateFlow<QuestionEntity?>(null)
    val editingQuestion: StateFlow<QuestionEntity?> = _editingQuestion.asStateFlow()

    private val _showCreateDialog = MutableStateFlow(false)
    val showCreateDialog: StateFlow<Boolean> = _showCreateDialog.asStateFlow()

    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    val allQuestions: StateFlow<List<QuestionEntity>> = repository.allQuestions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteQuestions: StateFlow<List<QuestionEntity>> = repository.favoriteQuestions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sourceDocuments: StateFlow<List<SourceDocumentEntity>> = repository.allDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val searchHistory: StateFlow<List<SearchHistoryEntity>> = repository.searchHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<String>> = combine(allQuestions) { (questions) ->
        listOf("SEMUA") + questions
            .map { it.category.trim().uppercase() }
            .filter { it.isNotEmpty() }
            .distinct()
            .sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("SEMUA"))

    val filteredDatabaseQuestions: StateFlow<List<QuestionEntity>> = combine(
        allQuestions,
        _databaseQuery,
        _selectedCategory
    ) { questions, query, category ->
        val qTrim = query.trim().lowercase()
        questions.filter { item ->
            val catOk = category == "SEMUA" || item.category.equals(category, ignoreCase = true)
            val textOk = qTrim.isEmpty() ||
                item.questionText.lowercase().contains(qTrim) ||
                item.optionsText.lowercase().contains(qTrim) ||
                item.answerKey.lowercase().contains(qTrim) ||
                item.category.lowercase().contains(qTrim)
            catOk && textOk
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredFavorites: StateFlow<List<QuestionEntity>> = combine(
        favoriteQuestions,
        _favoriteQuery
    ) { favorites, query ->
        val qTrim = query.trim().lowercase()
        if (qTrim.isEmpty()) favorites
        else favorites.filter { item ->
            item.questionText.lowercase().contains(qTrim) ||
                item.answerKey.lowercase().contains(qTrim) ||
                item.category.lowercase().contains(qTrim)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val searchResultsFlow: StateFlow<List<SearchResultItem>> = combine(
        allQuestions,
        _searchQuery,
        _hasSearched
    ) { questions, query, hasSearched ->
        if (query.isBlank()) {
            // Show recent stored questions when search box is empty for immediate browsing
            questions.take(15).map {
                SearchResultItem(
                    question = it,
                    matchType = MatchType.EXACT,
                    scorePercent = 100
                )
            }
        } else if (hasSearched || query.trim().length >= 2) {
            withContext(Dispatchers.Default) {
                searchEngine.search(query, questions)
            }
        } else {
            emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val homeUiState: StateFlow<HomeUiState> = combine(
        _searchQuery,
        _submittedQuery,
        _selectedFilter,
        repository.questionCount,
        repository.documentCount,
        searchResultsFlow,
        _isSearching,
        _hasSearched
    ) { values ->
        val query = values[0] as String
        val submitted = values[1] as String
        val filter = values[2] as SearchFilter
        val qCount = values[3] as Int
        val dCount = values[4] as Int
        @Suppress("UNCHECKED_CAST")
        val results = values[5] as List<SearchResultItem>
        val isSearching = values[6] as Boolean
        val hasSearched = values[7] as Boolean

        val filtered = when (filter) {
            SearchFilter.ALL -> results
            SearchFilter.EXACT -> results.filter { it.matchType == MatchType.EXACT }
            SearchFilter.TF_IDF -> results.filter { it.matchType == MatchType.TF_IDF }
            SearchFilter.KEYWORD -> results.filter { it.matchType == MatchType.KEYWORD }
        }

        HomeUiState(
            query = query,
            submittedQuery = submitted,
            selectedFilter = filter,
            totalQuestions = qCount,
            totalDocuments = dCount,
            allResults = results,
            filteredResults = filtered,
            isSearching = isSearching,
            hasSearched = hasSearched
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.ensureSeeded()
        }
    }

    fun onQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
        if (newQuery.isBlank()) {
            _hasSearched.value = false
            _submittedQuery.value = ""
        }
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _submittedQuery.value = ""
        _hasSearched.value = false
        _selectedFilter.value = SearchFilter.ALL
    }

    fun selectFilter(filter: SearchFilter) {
        _selectedFilter.value = filter
    }

    fun executeSearch(explicitQuery: String? = null) {
        val q = (explicitQuery ?: _searchQuery.value).trim()
        if (explicitQuery != null) {
            _searchQuery.value = q
        }
        if (q.isEmpty()) {
            _snackbarMessage.tryEmit("Masukkan teks soal atau kata kunci terlebih dahulu.")
            return
        }
        viewModelScope.launch {
            _isSearching.value = true
            _submittedQuery.value = q
            _hasSearched.value = true
            val currentQuestions = allQuestions.value
            val results = withContext(Dispatchers.Default) {
                searchEngine.search(q, currentQuestions)
            }
            withContext(Dispatchers.IO) {
                repository.recordSearch(
                    query = q,
                    resultCount = results.size,
                    topMatchScore = results.firstOrNull()?.scorePercent ?: 0
                )
            }
            _isSearching.value = false
        }
    }

    fun onDatabaseQueryChanged(query: String) {
        _databaseQuery.value = query
    }

    fun onCategorySelected(category: String) {
        _selectedCategory.value = category
    }

    fun onFavoriteQueryChanged(query: String) {
        _favoriteQuery.value = query
    }

    fun toggleFavorite(question: QuestionEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleFavorite(question.id, question.isFavorite)
        }
    }

    fun openEditDialog(question: QuestionEntity) {
        _editingQuestion.value = question
    }

    fun dismissEditDialog() {
        _editingQuestion.value = null
    }

    fun openCreateDialog() {
        _showCreateDialog.value = true
    }

    fun dismissCreateDialog() {
        _showCreateDialog.value = false
    }

    fun saveEditedQuestion(
        original: QuestionEntity,
        questionText: String,
        optionsText: String,
        answerKey: String,
        explanation: String,
        category: String
    ) {
        if (questionText.isBlank() || answerKey.isBlank()) {
            _snackbarMessage.tryEmit("Pertanyaan dan kunci jawaban tidak boleh kosong.")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateQuestion(
                original.copy(
                    questionText = questionText.trim(),
                    optionsText = optionsText.trim(),
                    answerKey = answerKey.trim(),
                    explanation = explanation.trim(),
                    category = category.trim().ifBlank { "UMUM" }.uppercase()
                )
            )
            _editingQuestion.value = null
            _snackbarMessage.tryEmit("Soal dan kunci jawaban berhasil diperbarui.")
        }
    }

    fun createManualQuestion(
        questionText: String,
        optionsText: String,
        answerKey: String,
        explanation: String,
        category: String
    ) {
        if (questionText.isBlank() || answerKey.isBlank()) {
            _snackbarMessage.tryEmit("Pertanyaan dan kunci jawaban wajib diisi.")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository.addManualQuestion(
                questionText = questionText,
                optionsText = optionsText,
                answerKey = answerKey,
                explanation = explanation,
                category = category
            )
            _showCreateDialog.value = false
            _snackbarMessage.tryEmit("Soal baru ditambahkan ke bank soal offline.")
        }
    }

    fun deleteQuestion(question: QuestionEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteQuestion(question)
            _snackbarMessage.tryEmit("Soal berhasil dihapus.")
        }
    }

    fun importFileFromUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            _isImporting.value = true
            try {
                val parsed = withContext(Dispatchers.IO) {
                    DocumentImportParser.parseUri(context, uri)
                }
                if (parsed.drafts.isEmpty()) {
                    _snackbarMessage.tryEmit("Tidak ditemukan format soal & kunci jawaban yang valid dalam '${parsed.fileName}'.")
                } else {
                    val savedCount = withContext(Dispatchers.IO) {
                        repository.importDocumentWithDrafts(
                            name = parsed.fileName,
                            format = parsed.format,
                            drafts = parsed.drafts
                        )
                    }
                    _snackbarMessage.tryEmit("Berhasil mengimpor $savedCount soal dari ${parsed.fileName}.")
                }
            } catch (e: Exception) {
                _snackbarMessage.tryEmit("Gagal mengimpor file: ${e.message ?: "format tidak dikenali"}")
            } finally {
                _isImporting.value = false
            }
        }
    }

    fun importPastedText(docName: String, rawText: String) {
        if (rawText.isBlank()) {
            _snackbarMessage.tryEmit("Tempel teks soal dan kunci jawaban terlebih dahulu.")
            return
        }
        viewModelScope.launch {
            _isImporting.value = true
            try {
                val title = docName.trim().ifBlank { "Catatan_Soal_${System.currentTimeMillis() % 10000}.txt" }
                val parsed = withContext(Dispatchers.Default) {
                    DocumentImportParser.parsePastedText(rawText, title)
                }
                if (parsed.drafts.isEmpty()) {
                    _snackbarMessage.tryEmit("Format tidak terbaca. Gunakan format Q:/A:, baris '|', CSV, atau JSON.")
                } else {
                    val savedCount = withContext(Dispatchers.IO) {
                        repository.importDocumentWithDrafts(
                            name = parsed.fileName,
                            format = parsed.format,
                            drafts = parsed.drafts
                        )
                    }
                    _snackbarMessage.tryEmit("Berhasil menyimpan $savedCount soal dari teks yang ditempel.")
                }
            } finally {
                _isImporting.value = false
            }
        }
    }

    fun deleteDocument(document: SourceDocumentEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteDocument(document.id)
            _snackbarMessage.tryEmit("Dokumen '${document.name}' dan soalnya telah dihapus.")
        }
    }

    fun exportAllQuestionsToUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val questions = allQuestions.value
                val csv = withContext(Dispatchers.Default) {
                    DocumentImportParser.exportToCsv(questions)
                }
                withContext(Dispatchers.IO) {
                    DocumentImportParser.writeTextToUri(context, uri, csv)
                }
                _snackbarMessage.tryEmit("Berhasil mengekspor ${questions.size} soal ke CSV.")
            } catch (e: Exception) {
                _snackbarMessage.tryEmit("Gagal mengekspor: ${e.message ?: "kesalahan I/O"}")
            }
        }
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteHistoryItem(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAllHistory()
            _snackbarMessage.tryEmit("Seluruh riwayat pencarian telah dibersihkan.")
        }
    }
}
