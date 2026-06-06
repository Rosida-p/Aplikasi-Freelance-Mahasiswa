package com.example.studfreelance.Client

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studfreelance.JobRepository

class PostJobActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PostJobScreen(
                onPostSuccess = { judul, budget, deadline, kategori ->
                    // ✅ Simpan ke Repository dulu
                    JobRepository.addJob(judul, budget, deadline, kategori)

                    // Baru pindah ke halaman sukses
                    val intent = Intent(this, PostJobSuccessActivity::class.java).apply {
                        putExtra("JUDUL",    judul)
                        putExtra("BUDGET",   budget)
                        putExtra("DEADLINE", deadline)
                        putExtra("KATEGORI", kategori)
                    }
                    startActivity(intent)
                    finish()
                },
                onBack = { finish() }
            )
        }
    }
}

@Composable
fun PostJobScreen(
    onPostSuccess: (String, String, String, String) -> Unit,
    onBack: () -> Unit
) {
    var judul     by remember { mutableStateOf("") }
    var deskripsi by remember { mutableStateOf("") }
    var budget    by remember { mutableStateOf("") }
    var deadline  by remember { mutableStateOf("") }
    var kategori  by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var expanded  by remember { mutableStateOf(false) }

    val kategoriList = listOf("Desain", "Web Dev", "Video", "Penulisan", "Marketing", "Lainnya")
    val isFormValid  = judul.isNotBlank() && deskripsi.isNotBlank()
            && budget.isNotBlank() && deadline.isNotBlank() && kategori.isNotBlank()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // --- HEADER ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(PrimaryBlue, PrimaryLight)))
                    .padding(top = 48.dp, bottom = 28.dp, start = 20.dp, end = 20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .clickable { onBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "Posting Pekerjaan",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Temukan mahasiswa terbaik untuk tugasmu",
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // --- FORM CARD ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {

                    // Judul
                    FormSectionLabel(Icons.Default.Edit, "Judul Pekerjaan")
                    Spacer(Modifier.height(6.dp))
                    StyledTextField(
                        value         = judul,
                        onValueChange = { judul = it },
                        placeholder   = "Cth: Desain Brosur Acara Kampus",
                        isError       = showError && judul.isBlank()
                    )
                    if (showError && judul.isBlank()) ErrorText("Judul wajib diisi")

                    Spacer(Modifier.height(18.dp))

                    // Kategori (Dropdown)
                    FormSectionLabel(Icons.Default.List, "Kategori")
                    Spacer(Modifier.height(6.dp))
                    Box {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    1.5.dp,
                                    if (showError && kategori.isBlank()) Color.Red
                                    else Color(0xFFDDE1F0),
                                    RoundedCornerShape(12.dp)
                                )
                                .background(BgLight)
                                .clickable { expanded = true }
                                .padding(horizontal = 14.dp, vertical = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    if (kategori.isEmpty()) "Pilih kategori pekerjaan" else kategori,
                                    color = if (kategori.isEmpty()) TextSecondary else TextPrimary,
                                    fontSize = 14.sp
                                )
                                Icon(
                                    Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = TextSecondary
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            kategoriList.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item) },
                                    onClick = { kategori = item; expanded = false }
                                )
                            }
                        }
                    }
                    if (showError && kategori.isBlank()) ErrorText("Kategori wajib dipilih")

                    Spacer(Modifier.height(18.dp))

                    // Deskripsi
                    FormSectionLabel(Icons.Default.Notes, "Deskripsi Pekerjaan")
                    Spacer(Modifier.height(6.dp))
                    StyledTextField(
                        value         = deskripsi,
                        onValueChange = { deskripsi = it },
                        placeholder   = "Jelaskan pekerjaan secara detail...",
                        singleLine    = false,
                        minLines      = 4,
                        isError       = showError && deskripsi.isBlank()
                    )
                    if (showError && deskripsi.isBlank()) ErrorText("Deskripsi wajib diisi")

                    Spacer(Modifier.height(18.dp))

                    // Budget & Deadline (2 kolom)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            FormSectionLabel(Icons.Default.Payments, "Budget (Rp)")
                            Spacer(Modifier.height(6.dp))
                            StyledTextField(
                                value         = budget,
                                onValueChange = { budget = it },
                                placeholder   = "200.000",
                                keyboardType  = KeyboardType.Number,
                                isError       = showError && budget.isBlank()
                            )
                            if (showError && budget.isBlank()) ErrorText("Wajib diisi")
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            FormSectionLabel(Icons.Default.Schedule, "Deadline")
                            Spacer(Modifier.height(6.dp))
                            StyledTextField(
                                value         = deadline,
                                onValueChange = { deadline = it },
                                placeholder   = "Cth: 3 Hari",
                                isError       = showError && deadline.isBlank()
                            )
                            if (showError && deadline.isBlank()) ErrorText("Wajib diisi")
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Tip box
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = BlueLight,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp)) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = PrimaryLight,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Tip: Deskripsi yang jelas dan budget yang wajar menarik lebih banyak mahasiswa berkualitas!",
                                fontSize = 12.sp,
                                color = PrimaryBlue,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Tombol Pasang
                    Button(
                        onClick = {
                            if (isFormValid) {
                                onPostSuccess(judul, "Rp$budget", deadline, kategori)
                            } else {
                                showError = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFormValid) PrimaryBlue else Color(0xFFBBBBBB)
                        )
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Pasang Pekerjaan",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

// =============================================
// KOMPONEN REUSABLE
// =============================================
@Composable
fun FormSectionLabel(icon: ImageVector, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = PrimaryLight, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
    }
}

@Composable
fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = TextSecondary, fontSize = 14.sp) },
        singleLine = singleLine,
        minLines = minLines,
        isError = isError,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = PrimaryLight,
            unfocusedBorderColor = Color(0xFFDDE1F0),
            errorBorderColor     = Color.Red,
            focusedContainerColor   = CardWhite,
            unfocusedContainerColor = BgLight
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun ErrorText(msg: String) {
    Text(
        msg,
        color = Color.Red,
        fontSize = 11.sp,
        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
    )
}