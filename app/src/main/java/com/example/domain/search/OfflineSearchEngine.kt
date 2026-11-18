package com.example.domain.search

import com.example.data.local.QuestionEntity
import java.util.Locale
import kotlin.math.ln
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

enum class MatchType(val label: String) {
    EXACT("Sama Persis"),
    TF_IDF("Kemiripan TF-IDF"),
    KEYWORD("Kata Kunci")
}

enum class SearchFilter(val label: String) {
    ALL("Semua"),
    EXACT("Sama Persis"),
    TF_IDF("Kemiripan Makna"),
    KEYWORD("Kata Kunci")
}

data class SearchResultItem(
    val question: QuestionEntity,
    val matchType: MatchType,
    val scorePercent: Int,
    val matchedTerms: List<String> = emptyList()
)

class OfflineSearchEngine {

    private val indonesianStopwords = setOf(
        "yang", "dan", "di", "ke", "dari", "adalah", "dalam", "untuk", "dengan", "pada",
        "tidak", "ini", "itu", "atau", "juga", "tetapi", "tapi", "serta", "seperti",
        "agar", "oleh", "karena", "sebagai", "saat", "ketika", "jika", "apabila", "bila",
        "akan", "sudah", "telah", "dapat", "bisa", "harus", "wajib", "hanya", "lebih",
        "antara", "disebut", "apa", "bagaimana", "berapa", "siapa", "mana", "berikut",
        "tersebut", "masuk", "jenis", "soal", "jawaban", "yg", "dg", "utk", "dlm"
    )

    private var indexedCorpusSize = -1
    private var idfMap: Map<String, Double> = emptyMap()
    private var docVectors: List<IndexedDoc> = emptyList()

    private data class IndexedDoc(
        val question: QuestionEntity,
        val normalizedQuestion: String,
        val normalizedAnswer: String,
        val normalizedFull: String,
        val tokens: Set<String>,
        val vector: Map<String, Double>,
        val norm: Double
    )

    fun rebuildIndex(questions: List<QuestionEntity>) {
        if (questions.isEmpty()) {
            indexedCorpusSize = 0
            idfMap = emptyMap()
            docVectors = emptyList()
            return
        }

        val tokenizedDocs = questions.map { q ->
            val combined = buildString {
                append(q.questionText)
                if (q.optionsText.isNotBlank()) {
                    append(' ')
                    append(q.optionsText)
                }
                if (q.answerKey.isNotBlank()) {
                    append(' ')
                    append(q.answerKey)
                }
                if (q.explanation.isNotBlank()) {
                    append(' ')
                    append(q.explanation)
                }
                if (q.category.isNotBlank()) {
                    append(' ')
                    append(q.category)
                }
            }
            val terms = extractTerms(combined)
            val normQ = normalizeText(q.questionText)
            val normA = normalizeText(q.answerKey)
            val normFull = normalizeText(combined)
            DocPrecalc(q, normQ, normA, normFull, terms, computeTermFreq(terms))
        }

        val docFreq = mutableMapOf<String, Int>()
        for (item in tokenizedDocs) {
            val uniqueTerms = item.terms.toSet()
            for (term in uniqueTerms) {
                docFreq[term] = (docFreq[term] ?: 0) + 1
            }
        }

        val n = questions.size.toDouble()
        val computedIdf = docFreq.mapValues { (_, df) ->
            ln((n + 1.0) / (df.toDouble() + 1.0)) + 1.0
        }

        val builtDocs = tokenizedDocs.map { item ->
            val vec = mutableMapOf<String, Double>()
            var sumSq = 0.0
            for ((term, tfWeight) in item.tf) {
                val weight = tfWeight * (computedIdf[term] ?: 1.0)
                vec[term] = weight
                sumSq += weight * weight
            }
            IndexedDoc(
                question = item.question,
                normalizedQuestion = item.normQ,
                normalizedAnswer = item.normA,
                normalizedFull = item.normFull,
                tokens = item.terms.filter { !it.contains('_') }.toSet(),
                vector = vec,
                norm = sqrt(sumSq)
            )
        }

        indexedCorpusSize = questions.size
        idfMap = computedIdf
        docVectors = builtDocs
    }

    private data class DocPrecalc(
        val question: QuestionEntity,
        val normQ: String,
        val normA: String,
        val normFull: String,
        val terms: List<String>,
        val tf: Map<String, Double>
    )

    fun search(rawQuery: String, questions: List<QuestionEntity>): List<SearchResultItem> {
        val trimmed = rawQuery.trim()
        if (trimmed.isEmpty()) return emptyList()

        // Ensure index stays in sync with current Room snapshot
        if (docVectors.size != questions.size || indexedCorpusSize != questions.size ||
            (questions.isNotEmpty() && docVectors.firstOrNull()?.question != questions.firstOrNull())
        ) {
            rebuildIndex(questions)
        }

        val normQuery = normalizeText(trimmed)
        if (normQuery.isBlank()) return emptyList()

        // Also separate stem if user pasted "question?: opt1; opt2; opt3"
        val queryStem = normalizeText(extractStem(trimmed))
        val queryTerms = extractTerms(trimmed)
        val queryUnigrams = queryTerms.filter { !it.contains('_') }.toSet()

        val queryTf = computeTermFreq(queryTerms)
        val queryVec = mutableMapOf<String, Double>()
        var querySumSq = 0.0
        for ((term, tfWeight) in queryTf) {
            val idf = idfMap[term] ?: (ln((docVectors.size + 1.0) / 1.0) + 1.0)
            val w = tfWeight * idf
            queryVec[term] = w
            querySumSq += w * w
        }
        val queryNorm = sqrt(querySumSq)

        val results = mutableListOf<SearchResultItem>()

        for (doc in docVectors) {
            // 1. Exact / near-exact match check (question, options, and answerKey)
            val exactScore = computeExactScore(
                normQuery = normQuery,
                queryStem = queryStem,
                normQuestion = doc.normalizedQuestion,
                normAnswer = doc.normalizedAnswer,
                normFull = doc.normalizedFull
            )
            if (exactScore >= 90) {
                results.add(
                    SearchResultItem(
                        question = doc.question,
                        matchType = MatchType.EXACT,
                        scorePercent = exactScore,
                        matchedTerms = queryUnigrams.intersect(doc.tokens).toList()
                    )
                )
                continue
            }

            // 2. TF-IDF cosine similarity check
            val cosine = if (queryNorm > 0.0 && doc.norm > 0.0) {
                var dot = 0.0
                for ((term, qWeight) in queryVec) {
                    val dWeight = doc.vector[term]
                    if (dWeight != null) {
                        dot += qWeight * dWeight
                    }
                }
                dot / (queryNorm * doc.norm)
            } else {
                0.0
            }

            // 3. Keyword overlap + fuzzy prefix/typo matching
            val matchedKeywords = mutableListOf<String>()
            if (queryUnigrams.isNotEmpty()) {
                for (qToken in queryUnigrams) {
                    if (doc.tokens.contains(qToken)) {
                        matchedKeywords.add(qToken)
                    } else if (qToken.length >= 4 && doc.tokens.any { it.startsWith(qToken) || qToken.startsWith(it) }) {
                        matchedKeywords.add(qToken)
                    } else if (qToken.length >= 5 && doc.tokens.any { levenshteinDistance(qToken, it) <= 1 }) {
                        matchedKeywords.add(qToken)
                    }
                }
            }
            val keywordRatio = if (queryUnigrams.isNotEmpty()) {
                matchedKeywords.size.toDouble() / queryUnigrams.size.toDouble()
            } else {
                0.0
            }

            // Classify primary match type
            when {
                cosine >= 0.14 -> {
                    val pct = (cosine * 100.0).roundToInt().coerceIn(18, 97)
                    results.add(
                        SearchResultItem(
                            question = doc.question,
                            matchType = MatchType.TF_IDF,
                            scorePercent = pct,
                            matchedTerms = matchedKeywords
                        )
                    )
                }
                keywordRatio >= 0.25 || (queryUnigrams.size == 1 && matchedKeywords.isNotEmpty()) -> {
                    val pct = (keywordRatio * 88.0).roundToInt().coerceIn(22, 89)
                    results.add(
                        SearchResultItem(
                            question = doc.question,
                            matchType = MatchType.KEYWORD,
                            scorePercent = pct,
                            matchedTerms = matchedKeywords
                        )
                    )
                }
            }
        }

        return results.sortedWith(
            compareBy<SearchResultItem> {
                when (it.matchType) {
                    MatchType.EXACT -> 0
                    MatchType.TF_IDF -> 1
                    MatchType.KEYWORD -> 2
                }
            }.thenByDescending { it.scorePercent }
        )
    }

    private fun computeExactScore(
        normQuery: String,
        queryStem: String,
        normQuestion: String,
        normAnswer: String,
        normFull: String
    ): Int {
        // 100% exact match against question, answer key, or full text
        if (normQuery == normQuestion ||
            normQuery == normAnswer ||
            normQuery == normFull ||
            (queryStem.isNotBlank() && (queryStem == normQuestion || queryStem == normAnswer))
        ) {
            return 100
        }

        // 100% exact phrase/stem match when pasted question stem (e.g. "question?: opt1; opt2...") matches verbatim inside stored question
        if (queryStem.length >= 16 && normQuestion.contains(queryStem)) {
            return 100
        }

        // 100% exact substring match on question or answer key for sufficiently specific queries
        if (normQuery.length >= 12 && (normQuestion.contains(normQuery) || normAnswer.contains(normQuery))) {
            return 100
        }

        if (normQuestion.length >= 16 && normQuery.contains(normQuestion)) {
            return 100
        }

        if (normQuery.length >= 14 && normFull.contains(normQuery)) {
            val ratio = normQuery.length.toDouble() / normFull.length.coerceAtLeast(1)
            if (ratio >= 0.45) return 100
        }

        return 0
    }

    private fun extractStem(raw: String): String {
        val qIdx = raw.indexOf("?:")
        if (qIdx > 10) return raw.substring(0, qIdx + 1)
        val semiIdx = raw.indexOf(';')
        if (semiIdx > 15) {
            val colonBefore = raw.lastIndexOf(':', semiIdx)
            if (colonBefore > 10) return raw.substring(0, colonBefore)
        }
        return raw
    }

    fun normalizeText(input: String): String {
        return input
            .lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun extractTerms(input: String): List<String> {
        val words = normalizeText(input)
            .split(' ')
            .filter { it.length >= 2 && it !in indonesianStopwords }
        if (words.isEmpty()) return emptyList()

        val terms = ArrayList<String>(words.size * 2)
        terms.addAll(words)
        for (i in 0 until words.size - 1) {
            terms.add("${words[i]}_${words[i + 1]}")
        }
        return terms
    }

    private fun computeTermFreq(terms: List<String>): Map<String, Double> {
        if (terms.isEmpty()) return emptyMap()
        val counts = mutableMapOf<String, Int>()
        for (t in terms) {
            counts[t] = (counts[t] ?: 0) + 1
        }
        val total = terms.size.toDouble()
        return counts.mapValues { (_, c) -> c.toDouble() / total }
    }

    private fun levenshteinDistance(a: String, b: String): Int {
        if (kotlin.math.abs(a.length - b.length) > 1) return 2
        val dp = IntArray(b.length + 1) { it }
        for (i in 1..a.length) {
            var prev = dp[0]
            dp[0] = i
            for (j in 1..b.length) {
                val temp = dp[j]
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                dp[j] = min(min(dp[j] + 1, dp[j - 1] + 1), prev + cost)
                prev = temp
            }
        }
        return dp[b.length]
    }
}
