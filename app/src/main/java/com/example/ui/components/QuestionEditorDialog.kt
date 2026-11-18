package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.QuestionEntity
import com.example.ui.theme.MintAccent
import com.example.ui.theme.OnMintAccent
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun QuestionEditorDialog(
    initialQuestion: QuestionEntity? = null,
    onDismiss: () -> Unit,
    onSave: (
        questionText: String,
        optionsText: String,
        answerKey: String,
        explanation: String,
        category: String
    ) -> Unit
) {
    var questionText by rememberSaveable(initialQuestion?.id) {
        mutableStateOf(initialQuestion?.questionText ?: "")
    }
    var optionsText by rememberSaveable(initialQuestion?.id) {
        mutableStateOf(initialQuestion?.optionsText ?: "")
    }
    var answerKey by rememberSaveable(initialQuestion?.id) {
        mutableStateOf(initialQuestion?.answerKey ?: "")
    }
    var category by rememberSaveable(initialQuestion?.id) {
        mutableStateOf(initialQuestion?.category ?: "UMUM")
    }
    var explanation by rememberSaveable(initialQuestion?.id) {
        mutableStateOf(initialQuestion?.explanation ?: "")
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MintAccent,
        unfocusedBorderColor = SurfaceBorder,
        focusedLabelColor = MintAccent,
        unfocusedLabelColor = TextSecondary,
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,
        cursorColor = MintAccent
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(22.dp),
            color = SurfaceCard,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (initialQuestion == null) "Tambah Soal Baru" else "Edit Soal & Kunci Jawaban",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )

                OutlinedTextField(
                    value = questionText,
                    onValueChange = { questionText = it },
                    label = { Text("Teks Pertanyaan *") },
                    minLines = 3,
                    maxLines = 6,
                    colors = fieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("editor_question_input")
                )

                OutlinedTextField(
                    value = optionsText,
                    onValueChange = { optionsText = it },
                    label = { Text("Pilihan Jawaban (pisahkan dengan ;)") },
                    placeholder = { Text("Contoh: Advance; Basic; Medium; Premium") },
                    maxLines = 3,
                    colors = fieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("editor_options_input")
                )

                OutlinedTextField(
                    value = answerKey,
                    onValueChange = { answerKey = it },
                    label = { Text("Kunci Jawaban *") },
                    maxLines = 3,
                    colors = fieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("editor_answer_input")
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Kategori") },
                    placeholder = { Text("Contoh: BEANSPOT, KASIR, SOP TOKO") },
                    singleLine = true,
                    colors = fieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("editor_category_input")
                )

                OutlinedTextField(
                    value = explanation,
                    onValueChange = { explanation = it },
                    label = { Text("Pembahasan (Opsional)") },
                    minLines = 2,
                    maxLines = 4,
                    colors = fieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("editor_explanation_input")
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal", color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSave(questionText, optionsText, answerKey, explanation, category)
                        },
                        enabled = questionText.isNotBlank() && answerKey.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MintAccent,
                            contentColor = OnMintAccent
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("editor_save_button")
                    ) {
                        Text(
                            text = "Simpan",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
