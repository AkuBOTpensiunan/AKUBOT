package com.example

import com.example.data.seed.StarterQuestionBank
import com.example.domain.parser.DocumentImportParser
import com.example.domain.search.MatchType
import com.example.domain.search.OfflineSearchEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class OfflineSearchAndParserTest {

    @Test
    fun exactSearchMatchesBeanspotQuestionAt100Percent() {
        val engine = OfflineSearchEngine()
        val corpus = StarterQuestionBank.createInitialQuestions(1L, 0L)
        engine.rebuildIndex(corpus)

        val query = "Bagus masuk dalam jenis toko beanspot concept?: Advance; Basic; Medium; Premium; RRI"
        val results = engine.search(query, corpus)

        assertTrue("Expected at least one search result", results.isNotEmpty())
        assertEquals(MatchType.EXACT, results.first().matchType)
        assertEquals(100, results.first().scorePercent)
        assertEquals("Medium", results.first().question.answerKey)

        // Exact answer key searches must also return Sama Persis 100%
        val answerResults = engine.search("FEFO", corpus)
        assertTrue(answerResults.isNotEmpty())
        assertEquals(MatchType.EXACT, answerResults.first().matchType)
        assertEquals(100, answerResults.first().scorePercent)
        assertEquals("FEFO", answerResults.first().question.answerKey)

        val phraseAnswerResults = engine.search("Kopi Susu Gula Aren", corpus)
        assertTrue(phraseAnswerResults.isNotEmpty())
        assertEquals(MatchType.EXACT, phraseAnswerResults.first().matchType)
        assertEquals(100, phraseAnswerResults.first().scorePercent)
    }

    @Test
    fun tfidfAndKeywordSearchFindRelevantQuestions() {
        val engine = OfflineSearchEngine()
        val corpus = StarterQuestionBank.createInitialQuestions(1L, 0L)
        engine.rebuildIndex(corpus)

        val tfidfResults = engine.search("penyimpanan biji kopi suhu ideal gerai", corpus)
        assertTrue("Expected TF-IDF results for coffee storage query", tfidfResults.isNotEmpty())
        assertEquals("15°C - 25°C", tfidfResults.first().question.answerKey)

        val keywordResults = engine.search("shrinkage", corpus)
        assertTrue("Expected keyword result for shrinkage", keywordResults.isNotEmpty())
        assertEquals("Shrinkage", keywordResults.first().question.answerKey)
    }

    @Test
    fun documentParserParsesCsvJsonAndQaText() {
        val csv = """
            soal,opsi,kunci_jawaban,kategori,pembahasan
            "Apa kepanjangan FIFO?","First In First Out; Last In First Out","First In First Out","INVENTORY","Barang masuk awal keluar awal"
        """.trimIndent()
        val csvDrafts = DocumentImportParser.parseDelimited(csv, ',')
        assertEquals(1, csvDrafts.size)
        assertEquals("First In First Out", csvDrafts.first().answerKey)
        assertEquals("INVENTORY", csvDrafts.first().category)

        val json = """
            [{"pertanyaan":"Suhu chiller minuman?","opsi":"0-4 C; 15-25 C","jawaban":"0-4 C","kategori":"SOP TOKO"}]
        """.trimIndent()
        val jsonDrafts = DocumentImportParser.parseJson(json)
        assertEquals(1, jsonDrafts.size)
        assertEquals("0-4 C", jsonDrafts.first().answerKey)

        val qaText = """
            Q: Alat pemadat bubuk kopi disebut?
            Opsi: Grinder; Tamper; Kettle
            Jawaban: Tamper
            Kategori: BEANSPOT
        """.trimIndent()
        val txtDrafts = DocumentImportParser.parseFlexibleText(qaText)
        assertEquals(1, txtDrafts.size)
        assertEquals("Tamper", txtDrafts.first().answerKey)
        assertEquals("BEANSPOT", txtDrafts.first().category)
    }
}
