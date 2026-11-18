package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.SourceDocumentEntity
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkCanvas
import com.example.ui.theme.EmeraldHeaderBottom
import com.example.ui.theme.EmeraldHeaderTop
import com.example.ui.theme.IconContainerDark
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.MintAccent
import com.example.ui.theme.OnMintAccent
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun UploadScreen(
    documents: List<SourceDocumentEntity>,
    isImporting: Boolean,
    onImportUri: (android.content.Context, android.net.Uri) -> Unit,
    onImportPastedText: (docName: String, rawText: String) -> Unit,
    onOpenManualCreate: () -> Unit,
    onDeleteDocument: (SourceDocumentEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var pasteDocTitle by rememberSaveable { mutableStateOf("") }
    var pasteRawText by rememberSaveable { mutableStateOf("") }
    var pendingDeleteDoc by remember { mutableStateOf<SourceDocumentEntity?>(null) }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            onImportUri(context, uri)
        }
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkCanvas)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(listOf(EmeraldHeaderTop, EmeraldHeaderBottom))
                )
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 18.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 840.dp)
                    .align(Alignment.Center)
            ) {
                Text(
                    text = "Upload & Impor Soal",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tambahkan file CSV, JSON, TXT, XLSX, DOCX atau tempel teks soal secara offline.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFD6E6FA)
                )
            }
        }

        if (isImporting) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = MintAccent,
                trackColor = SurfaceCard
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp)
                .align(Alignment.CenterHorizontally),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // File picker card
            item(key = "file_picker_card") {
                Surface(
                    color = SurfaceCard,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(IconContainerDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.UploadFile,
                                    contentDescription = null,
                                    tint = MintAccent
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Impor File Dokumen",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Mendukung .csv, .tsv, .json, .txt, .xlsx, .docx",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    openDocumentLauncher.launch(
                                        arrayOf(
                                            "text/*",
                                            "application/json",
                                            "text/csv",
                                            "text/comma-separated-values",
                                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                            "*/*"
                                        )
                                    )
                                },
                                enabled = !isImporting,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MintAccent,
                                    contentColor = OnMintAccent
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .testTag("upload_pick_file_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.UploadFile,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Pilih File", fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = onOpenManualCreate,
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, MintAccent),
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .testTag("upload_manual_add_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AddCircleOutline,
                                    contentDescription = null,
                                    tint = MintAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Input Manual", color = MintAccent, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // Paste raw text importer card
            item(key = "paste_importer_card") {
                Surface(
                    color = SurfaceCard,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.NoteAdd,
                                contentDescription = null,
                                tint = MintAccent
                            )
                            Text(
                                text = "Tempel Teks Soal Langsung",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceCardElevated)
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "Contoh format:\nQ: Apa kepanjangan FEFO?\nOpsi: First In First Out; First Expired First Out\nJawaban: First Expired First Out\nKategori: INVENTORY",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = JetBrainsMonoFamily
                                ),
                                color = TextSecondary
                            )
                        }

                        OutlinedTextField(
                            value = pasteDocTitle,
                            onValueChange = { pasteDocTitle = it },
                            label = { Text("Nama Sumber / Catatan (Opsional)") },
                            singleLine = true,
                            colors = fieldColors,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = pasteRawText,
                            onValueChange = { pasteRawText = it },
                            label = { Text("Tempel daftar soal & jawaban di sini") },
                            minLines = 4,
                            maxLines = 8,
                            colors = fieldColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("paste_text_input")
                        )

                        Button(
                            onClick = {
                                onImportPastedText(pasteDocTitle, pasteRawText)
                                pasteDocTitle = ""
                                pasteRawText = ""
                            },
                            enabled = pasteRawText.isNotBlank() && !isImporting,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MintAccent,
                                contentColor = OnMintAccent
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("paste_import_button")
                        ) {
                            Text("Impor Teks ke Bank Soal", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Source Documents Header
            item(key = "docs_header") {
                Text(
                    text = "Dokumen Sumber (${documents.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            }

            items(items = documents, key = { it.id }) { doc ->
                SourceDocumentCard(
                    document = doc,
                    onDelete = { pendingDeleteDoc = doc }
                )
            }
        }
    }

    if (pendingDeleteDoc != null) {
        val doc = pendingDeleteDoc!!
        AlertDialog(
            onDismissRequest = { pendingDeleteDoc = null },
            containerColor = SurfaceCard,
            title = { Text("Hapus Dokumen?", color = TextPrimary) },
            text = {
                Text(
                    text = "Menghapus '${doc.name}' juga akan menghapus ${doc.questionCount} soal yang berasal dari dokumen ini.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteDocument(doc)
                        pendingDeleteDoc = null
                    }
                ) {
                    Text("Hapus", color = DangerRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteDoc = null }) {
                    Text("Batal", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun SourceDocumentCard(
    document: SourceDocumentEntity,
    onDelete: () -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag("id-ID")) }

    Surface(
        color = SurfaceCard,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, SurfaceBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(IconContainerDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Description,
                    contentDescription = null,
                    tint = MintAccent,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = document.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${document.format} • ${document.questionCount} soal • ${dateFormatter.format(Date(document.importedAt))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.minimumInteractiveComponentSize()
            ) {
                Icon(
                    imageVector = Icons.Filled.DeleteOutline,
                    contentDescription = "Hapus dokumen",
                    tint = DangerRed,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
