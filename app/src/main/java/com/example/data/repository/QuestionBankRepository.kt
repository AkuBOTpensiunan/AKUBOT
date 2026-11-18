package com.example.data.repository

import com.example.data.local.QuestionDao
import com.example.data.local.QuestionEntity
import com.example.data.local.SearchHistoryDao
import com.example.data.local.SearchHistoryEntity
import com.example.data.local.SourceDocumentDao
import com.example.data.local.SourceDocumentEntity
import com.example.data.seed.StarterQuestionBank
import com.example.domain.parser.ParsedQuestionDraft
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class QuestionBankRepository(
    private val questionDao: QuestionDao,
    private val documentDao: SourceDocumentDao,
    private val historyDao: SearchHistoryDao
) {
    private val seedMutex = Mutex()

    val allQuestions: Flow<List<QuestionEntity>> = questionDao.getAllQuestions()
    val favoriteQuestions: Flow<List<QuestionEntity>> = questionDao.getFavoriteQuestions()
    val questionCount: Flow<Int> = questionDao.getQuestionCount()
    val allDocuments: Flow<List<SourceDocumentEntity>> = documentDao.getAllDocuments()
    val documentCount: Flow<Int> = documentDao.getDocumentCount()
    val searchHistory: Flow<List<SearchHistoryEntity>> = historyDao.getRecentHistory()

    suspend fun ensureSeeded() {
        seedMutex.withLock {
            if (documentDao.countSync() == 0 && questionDao.countSync() == 0) {
                val now = System.currentTimeMillis()
                val starterDrafts = StarterQuestionBank.createInitialQuestions(0L, now)
                val docId = documentDao.insertDocument(
                    SourceDocumentEntity(
                        name = StarterQuestionBank.DEFAULT_DOCUMENT_NAME,
                        format = StarterQuestionBank.DEFAULT_DOCUMENT_FORMAT,
                        questionCount = starterDrafts.size,
                        importedAt = now
                    )
                )
                questionDao.insertQuestions(starterDrafts.map { it.copy(sourceDocumentId = docId) })
            }
        }
    }

    suspend fun importDocumentWithDrafts(
        name: String,
        format: String,
        drafts: List<ParsedQuestionDraft>
    ): Int {
        if (drafts.isEmpty()) return 0
        val now = System.currentTimeMillis()
        val docId = documentDao.insertDocument(
            SourceDocumentEntity(
                name = name,
                format = format,
                questionCount = drafts.size,
                importedAt = now
            )
        )
        val entities = drafts.map { draft ->
            QuestionEntity(
                questionText = draft.questionText,
                optionsText = draft.optionsText,
                answerKey = draft.answerKey,
                explanation = draft.explanation,
                category = draft.category.ifBlank { "UMUM" },
                sourceDocumentId = docId,
                createdAt = now,
                updatedAt = now
            )
        }
        questionDao.insertQuestions(entities)
        return entities.size
    }

    suspend fun addManualQuestion(
        questionText: String,
        optionsText: String,
        answerKey: String,
        explanation: String,
        category: String
    ) {
        val now = System.currentTimeMillis()
        val existingDocs = documentDao.countSync()
        val docId = if (existingDocs == 0) {
            documentDao.insertDocument(
                SourceDocumentEntity(
                    name = "Input_Manual_Pengguna",
                    format = "MANUAL",
                    questionCount = 1,
                    importedAt = now
                )
            )
        } else {
            1L
        }
        if (existingDocs > 0) {
            documentDao.incrementQuestionCount(docId, 1)
        }
        questionDao.insertQuestion(
            QuestionEntity(
                questionText = questionText.trim(),
                optionsText = optionsText.trim(),
                answerKey = answerKey.trim(),
                explanation = explanation.trim(),
                category = category.trim().ifBlank { "UMUM" }.uppercase(),
                sourceDocumentId = docId,
                createdAt = now,
                updatedAt = now
            )
        )
    }

    suspend fun updateQuestion(question: QuestionEntity) {
        questionDao.updateQuestion(
            question.copy(
                updatedAt = System.currentTimeMillis(),
                category = question.category.trim().ifBlank { "UMUM" }.uppercase()
            )
        )
    }

    suspend fun toggleFavorite(id: Long, currentFavorite: Boolean) {
        questionDao.setFavorite(id = id, isFavorite = !currentFavorite)
    }

    suspend fun deleteQuestion(question: QuestionEntity) {
        questionDao.deleteQuestionById(question.id)
        documentDao.incrementQuestionCount(question.sourceDocumentId, -1)
    }

    suspend fun deleteDocument(documentId: Long) {
        questionDao.deleteByDocumentId(documentId)
        documentDao.deleteDocumentById(documentId)
    }

    suspend fun recordSearch(query: String, resultCount: Int, topMatchScore: Int) {
        val clean = query.trim()
        if (clean.isEmpty()) return
        historyDao.deleteByQueryText(clean)
        historyDao.insertHistory(
            SearchHistoryEntity(
                query = clean,
                resultCount = resultCount,
                topMatchScore = topMatchScore,
                searchedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteHistoryItem(id: Long) {
        historyDao.deleteById(id)
    }

    suspend fun clearAllHistory() {
        historyDao.clearAll()
    }
}
