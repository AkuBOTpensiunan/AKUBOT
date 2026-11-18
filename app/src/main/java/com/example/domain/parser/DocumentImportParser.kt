package com.example.domain.parser

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Xml
import com.example.data.local.QuestionEntity
import org.json.JSONArray
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.zip.ZipInputStream

data class ParsedQuestionDraft(
    val questionText: String,
    val optionsText: String = "",
    val answerKey: String,
    val explanation: String = "",
    val category: String = "UMUM"
)

data class ParsedImportResult(
    val fileName: String,
    val format: String,
    val drafts: List<ParsedQuestionDraft>
)

object DocumentImportParser {

    fun parseUri(context: Context, uri: Uri): ParsedImportResult {
        val fileName = resolveFileName(context, uri)
        val ext = fileName.substringAfterLast('.', "").lowercase(Locale.ROOT)
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IllegalStateException("Gagal membaca file yang dipilih.")

        return when (ext) {
            "json" -> ParsedImportResult(fileName, "JSON", parseJson(String(bytes, StandardCharsets.UTF_8)))
            "csv" -> ParsedImportResult(fileName, "CSV", parseDelimited(String(bytes, StandardCharsets.UTF_8), ','))
            "tsv" -> ParsedImportResult(fileName, "TSV", parseDelimited(String(bytes, StandardCharsets.UTF_8), '\t'))
            "xlsx" -> ParsedImportResult(fileName, "XLSX", parseXlsx(bytes))
            "docx" -> ParsedImportResult(fileName, "DOCX", parseDocx(bytes))
            else -> {
                val text = String(bytes, StandardCharsets.UTF_8)
                val trimmed = text.trimStart()
                val drafts = when {
                    trimmed.startsWith("[") || trimmed.startsWith("{") -> parseJson(text)
                    else -> parseFlexibleText(text)
                }
                val detectedFormat = if (ext.isNotBlank()) ext.uppercase(Locale.ROOT) else "TXT"
                ParsedImportResult(fileName, detectedFormat, drafts)
            }
        }
    }

    fun parsePastedText(rawText: String, docTitle: String = "Teks_Tempel"): ParsedImportResult {
        val trimmed = rawText.trim()
        if (trimmed.isEmpty()) return ParsedImportResult(docTitle, "TEKS", emptyList())

        val drafts = when {
            trimmed.startsWith("[") || trimmed.startsWith("{") -> parseJson(trimmed)
            else -> parseFlexibleText(trimmed)
        }
        return ParsedImportResult(docTitle, "TEKS", drafts)
    }

    fun parseJson(raw: String): List<ParsedQuestionDraft> {
        val trimmed = raw.trim().removePrefix("\uFEFF")
        val jsonArray = when {
            trimmed.startsWith("[") -> JSONArray(trimmed)
            trimmed.startsWith("{") -> {
                val root = JSONObject(trimmed)
                root.optJSONArray("questions")
                    ?: root.optJSONArray("soal")
                    ?: root.optJSONArray("data")
                    ?: root.optJSONArray("items")
                    ?: JSONArray().apply { put(root) }
            }
            else -> return emptyList()
        }

        val drafts = mutableListOf<ParsedQuestionDraft>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.optJSONObject(i) ?: continue
            val question = firstNonBlank(
                obj, "questionText", "question", "pertanyaan", "soal", "tanya", "q"
            )
            val answer = firstNonBlank(
                obj, "answerKey", "answer", "jawaban", "kunci", "kunci_jawaban", "a"
            )
            if (question.isBlank() || answer.isBlank()) continue

            val options = extractOptionsFromJson(obj)
            val explanation = firstNonBlank(obj, "explanation", "pembahasan", "penjelasan", "note")
            val category = firstNonBlank(obj, "category", "kategori", "topik", "mapel").ifBlank { "UMUM" }

            drafts.add(
                ParsedQuestionDraft(
                    questionText = question,
                    optionsText = options,
                    answerKey = answer,
                    explanation = explanation,
                    category = category.uppercase(Locale.ROOT)
                )
            )
        }
        return drafts
    }

    private fun extractOptionsFromJson(obj: JSONObject): String {
        val arr = obj.optJSONArray("options") ?: obj.optJSONArray("opsi") ?: obj.optJSONArray("pilihan")
        if (arr != null) {
            val list = mutableListOf<String>()
            for (j in 0 until arr.length()) {
                val v = arr.optString(j).trim()
                if (v.isNotEmpty()) list.add(v)
            }
            return list.joinToString("; ")
        }
        return firstNonBlank(obj, "optionsText", "options", "opsi", "pilihan")
    }

    private fun firstNonBlank(obj: JSONObject, vararg keys: String): String {
        for (key in keys) {
            val value = obj.optString(key, "").trim()
            if (value.isNotEmpty() && value != "null") return value
        }
        return ""
    }

    fun parseDelimited(raw: String, delimiter: Char): List<ParsedQuestionDraft> {
        val rows = parseCsvRows(raw.removePrefix("\uFEFF"), delimiter)
        if (rows.isEmpty()) return emptyList()

        val firstRowLower = rows.first().map { it.lowercase(Locale.ROOT).trim() }
        val hasHeader = firstRowLower.any {
            it in setOf("soal", "pertanyaan", "question", "jawaban", "kunci", "answer", "kategori", "category")
        }

        val startIndex = if (hasHeader) 1 else 0
        var qIdx = 0
        var optIdx = -1
        var ansIdx = 1
        var expIdx = -1
        var catIdx = -1

        if (hasHeader) {
            firstRowLower.forEachIndexed { idx, col ->
                when {
                    col.contains("soal") || col.contains("pertanyaan") || col.contains("question") -> qIdx = idx
                    col.contains("opsi") || col.contains("pilihan") || col.contains("option") -> optIdx = idx
                    col.contains("kunci") || col.contains("jawaban") || col.contains("answer") -> ansIdx = idx
                    col.contains("pembahasan") || col.contains("penjelasan") || col.contains("explanation") -> expIdx = idx
                    col.contains("kategori") || col.contains("category") || col.contains("topik") -> catIdx = idx
                }
            }
        } else {
            // Heuristic without header:
            // 2 cols: question, answer
            // 3 cols: question, options, answer
            // 4+ cols: question, options, answer, category, (explanation)
            val colCount = rows.first().size
            if (colCount >= 3) {
                optIdx = 1
                ansIdx = 2
            }
            if (colCount >= 4) catIdx = 3
            if (colCount >= 5) expIdx = 4
        }

        val drafts = mutableListOf<ParsedQuestionDraft>()
        for (i in startIndex until rows.size) {
            val row = rows[i]
            val q = row.getOrNull(qIdx)?.trim().orEmpty()
            val a = row.getOrNull(ansIdx)?.trim().orEmpty()
            if (q.isBlank() || a.isBlank()) continue
            val opt = if (optIdx >= 0) row.getOrNull(optIdx)?.trim().orEmpty() else ""
            val cat = if (catIdx >= 0) row.getOrNull(catIdx)?.trim().orEmpty() else "UMUM"
            val exp = if (expIdx >= 0) row.getOrNull(expIdx)?.trim().orEmpty() else ""

            drafts.add(
                ParsedQuestionDraft(
                    questionText = q,
                    optionsText = opt,
                    answerKey = a,
                    explanation = exp,
                    category = cat.ifBlank { "UMUM" }.uppercase(Locale.ROOT)
                )
            )
        }
        return drafts
    }

    fun parseFlexibleText(raw: String): List<ParsedQuestionDraft> {
        val clean = raw.removePrefix("\uFEFF").replace("\r\n", "\n").replace("\r", "\n")
        val lines = clean.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.isEmpty()) return emptyList()

        // Check if pipe-delimited or semicolon/tab CSV-like lines
        val pipeCount = lines.count { it.contains('|') }
        if (pipeCount >= (lines.size / 2).coerceAtLeast(1) && lines.first().contains('|')) {
            return parseDelimited(clean, '|')
        }
        val tabCount = lines.count { it.contains('\t') }
        if (tabCount >= (lines.size / 2).coerceAtLeast(1) && lines.first().contains('\t')) {
            return parseDelimited(clean, '\t')
        }
        val commaCsvHeader = lines.first().lowercase(Locale.ROOT)
        if (commaCsvHeader.contains(",") &&
            (commaCsvHeader.contains("soal") || commaCsvHeader.contains("pertanyaan") || commaCsvHeader.contains("jawaban"))
        ) {
            return parseDelimited(clean, ',')
        }

        // Parse Q/A structured blocks (Pertanyaan:/Q:/1. ... Opsi:/A. B. C. ... Jawaban:/A:/Kunci: ...)
        val drafts = mutableListOf<ParsedQuestionDraft>()
        var currentQuestion = StringBuilder()
        val currentOptions = mutableListOf<String>()
        var currentAnswer = ""
        var currentExplanation = ""
        var currentCategory = "UMUM"

        fun flushBlock() {
            val qText = currentQuestion.toString().trim()
            if (qText.isNotBlank() && currentAnswer.isNotBlank()) {
                drafts.add(
                    ParsedQuestionDraft(
                        questionText = qText,
                        optionsText = currentOptions.joinToString("; "),
                        answerKey = currentAnswer.trim(),
                        explanation = currentExplanation.trim(),
                        category = currentCategory.trim().ifBlank { "UMUM" }.uppercase(Locale.ROOT)
                    )
                )
            }
            currentQuestion = StringBuilder()
            currentOptions.clear()
            currentAnswer = ""
            currentExplanation = ""
            currentCategory = "UMUM"
        }

        val numberedPrefix = Regex("^\\d+[.)]\\s+(.+)$")
        val optionPrefix = Regex("^[A-Ea-e][.)]\\s+(.+)$")

        for (line in clean.lines()) {
            val trimmed = line.trim()
            if (trimmed.isEmpty() || trimmed == "---" || trimmed == "===") {
                if (currentQuestion.isNotEmpty() && currentAnswer.isNotBlank()) {
                    flushBlock()
                }
                continue
            }

            val lower = trimmed.lowercase(Locale.ROOT)
            when {
                lower.startsWith("q:") || lower.startsWith("soal:") || lower.startsWith("pertanyaan:") -> {
                    if (currentQuestion.isNotEmpty() && currentAnswer.isNotBlank()) flushBlock()
                    val body = trimmed.substringAfter(':').trim()
                    currentQuestion.append(body)
                }
                lower.startsWith("opsi:") || lower.startsWith("pilihan:") || lower.startsWith("options:") -> {
                    val body = trimmed.substringAfter(':').trim()
                    if (body.isNotBlank()) currentOptions.addAll(body.split(';').map { it.trim() }.filter { it.isNotEmpty() })
                }
                lower.startsWith("a:") || lower.startsWith("jawaban:") || lower.startsWith("kunci:") || lower.startsWith("kunci jawaban:") -> {
                    currentAnswer = trimmed.substringAfter(':').trim()
                }
                lower.startsWith("pembahasan:") || lower.startsWith("penjelasan:") -> {
                    currentExplanation = trimmed.substringAfter(':').trim()
                }
                lower.startsWith("kategori:") || lower.startsWith("category:") -> {
                    currentCategory = trimmed.substringAfter(':').trim()
                }
                numberedPrefix.matches(trimmed) -> {
                    if (currentQuestion.isNotEmpty() && currentAnswer.isNotBlank()) flushBlock()
                    val match = numberedPrefix.find(trimmed)!!
                    if (currentQuestion.isNotEmpty()) currentQuestion.append(' ')
                    currentQuestion.append(match.groupValues[1].trim())
                }
                optionPrefix.matches(trimmed) && currentQuestion.isNotEmpty() -> {
                    currentOptions.add(trimmed)
                }
                else -> {
                    if (currentAnswer.isBlank()) {
                        if (currentQuestion.isNotEmpty()) currentQuestion.append(' ')
                        currentQuestion.append(trimmed)
                    } else if (currentExplanation.isBlank()) {
                        currentExplanation = trimmed
                    }
                }
            }
        }
        flushBlock()
        return drafts
    }

    private fun parseDocx(bytes: ByteArray): List<ParsedQuestionDraft> {
        var documentXml: ByteArray? = null
        ZipInputStream(ByteArrayInputStream(bytes)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                if (entry.name == "word/document.xml") {
                    documentXml = zis.readBytes()
                    break
                }
                entry = zis.nextEntry
            }
        }
        val xmlBytes = documentXml ?: return emptyList()
        val paragraphs = mutableListOf<String>()
        val parser = Xml.newPullParser()
        parser.setInput(ByteArrayInputStream(xmlBytes), "UTF-8")
        var currentPara = StringBuilder()
        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> {
                    if (parser.name == "p" || parser.name.endsWith(":p")) {
                        currentPara = StringBuilder()
                    }
                }
                XmlPullParser.TEXT -> {
                    currentPara.append(parser.text)
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "p" || parser.name.endsWith(":p")) {
                        val line = currentPara.toString().trim()
                        if (line.isNotEmpty()) paragraphs.add(line)
                    }
                }
            }
            event = parser.next()
        }
        return parseFlexibleText(paragraphs.joinToString("\n"))
    }

    private fun parseXlsx(bytes: ByteArray): List<ParsedQuestionDraft> {
        var sharedStringsXml: ByteArray? = null
        var sheetXml: ByteArray? = null
        ZipInputStream(ByteArrayInputStream(bytes)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                when (entry.name) {
                    "xl/sharedStrings.xml" -> sharedStringsXml = zis.readBytes()
                    "xl/worksheets/sheet1.xml" -> sheetXml = zis.readBytes()
                }
                entry = zis.nextEntry
            }
        }
        val sheetBytes = sheetXml ?: return emptyList()
        val sharedStrings = sharedStringsXml?.let { parseSharedStrings(it) } ?: emptyList()

        val rows = mutableListOf<List<String>>()
        val parser = Xml.newPullParser()
        parser.setInput(ByteArrayInputStream(sheetBytes), "UTF-8")

        var currentRow = mutableListOf<String>()
        var currentCellType = ""
        var currentVal = StringBuilder()
        var inVal = false
        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> when (parser.name) {
                    "row" -> currentRow = mutableListOf()
                    "c" -> {
                        currentCellType = parser.getAttributeValue(null, "t") ?: ""
                        currentVal = StringBuilder()
                    }
                    "v", "t" -> inVal = true
                }
                XmlPullParser.TEXT -> if (inVal) currentVal.append(parser.text)
                XmlPullParser.END_TAG -> when (parser.name) {
                    "v", "t" -> inVal = false
                    "c" -> {
                        val raw = currentVal.toString().trim()
                        val resolved = if (currentCellType == "s") {
                            raw.toIntOrNull()?.let { idx -> sharedStrings.getOrNull(idx) } ?: raw
                        } else {
                            raw
                        }
                        currentRow.add(resolved)
                    }
                    "row" -> if (currentRow.any { it.isNotBlank() }) rows.add(currentRow)
                }
            }
            event = parser.next()
        }

        if (rows.isEmpty()) return emptyList()
        val csvText = rows.joinToString("\n") { row ->
            row.joinToString(",") { cell -> "\"${cell.replace("\"", "\"\"")}\"" }
        }
        return parseDelimited(csvText, ',')
    }

    private fun parseSharedStrings(xmlBytes: ByteArray): List<String> {
        val list = mutableListOf<String>()
        val parser = Xml.newPullParser()
        parser.setInput(ByteArrayInputStream(xmlBytes), "UTF-8")
        var current = StringBuilder()
        var inSi = false
        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> if (parser.name == "si") {
                    current = StringBuilder()
                    inSi = true
                }
                XmlPullParser.TEXT -> if (inSi) current.append(parser.text)
                XmlPullParser.END_TAG -> if (parser.name == "si") {
                    list.add(current.toString())
                    inSi = false
                }
            }
            event = parser.next()
        }
        return list
    }

    private fun parseCsvRows(input: String, delimiter: Char): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        var currentRow = mutableListOf<String>()
        var currentField = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < input.length) {
            val c = input[i]
            when {
                c == '"' -> {
                    if (inQuotes && i + 1 < input.length && input[i + 1] == '"') {
                        currentField.append('"')
                        i++
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                c == delimiter && !inQuotes -> {
                    currentRow.add(currentField.toString())
                    currentField = StringBuilder()
                }
                (c == '\n' || c == '\r') && !inQuotes -> {
                    if (c == '\r' && i + 1 < input.length && input[i + 1] == '\n') i++
                    currentRow.add(currentField.toString())
                    currentField = StringBuilder()
                    if (currentRow.any { it.isNotBlank() }) rows.add(currentRow)
                    currentRow = mutableListOf()
                }
                else -> currentField.append(c)
            }
            i++
        }
        currentRow.add(currentField.toString())
        if (currentRow.any { it.isNotBlank() }) rows.add(currentRow)
        return rows
    }

    fun exportToCsv(questions: List<QuestionEntity>): String {
        val sb = StringBuilder()
        sb.appendLine("soal,opsi,kunci_jawaban,kategori,pembahasan")
        for (q in questions) {
            sb.append(csvEscape(q.questionText)).append(',')
            sb.append(csvEscape(q.optionsText)).append(',')
            sb.append(csvEscape(q.answerKey)).append(',')
            sb.append(csvEscape(q.category)).append(',')
            sb.appendLine(csvEscape(q.explanation))
        }
        return sb.toString()
    }

    private fun csvEscape(value: String): String {
        val escaped = value.replace("\"", "\"\"")
        return "\"$escaped\""
    }

    fun writeTextToUri(context: Context, uri: Uri, content: String) {
        context.contentResolver.openOutputStream(uri)?.use { out ->
            out.write(content.toByteArray(StandardCharsets.UTF_8))
        } ?: throw IllegalStateException("Gagal menyimpan file ekspor.")
    }

    private fun resolveFileName(context: Context, uri: Uri): String {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) {
                    val name = cursor.getString(idx)
                    if (!name.isNullOrBlank()) return name
                }
            }
        }
        return uri.lastPathSegment?.substringAfterLast('/') ?: "dokumen_soal.txt"
    }
}
