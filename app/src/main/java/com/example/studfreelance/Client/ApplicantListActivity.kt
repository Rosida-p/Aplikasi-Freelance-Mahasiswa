package com.example.studfreelance.Client

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studfreelance.ApplicationRepository
import com.example.studfreelance.ApplicationStatus
import com.example.studfreelance.JobApplication

class ApplicantListActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val jobId    = intent.getStringExtra("JOB_ID") ?: ""
        val jobTitle = intent.getStringExtra("JOB_TITLE") ?: "Pekerjaan"

        setContent {
            ApplicantListScreen(
                jobId    = jobId,
                jobTitle = jobTitle,
                onBack   = { finish() }
            )
        }
    }
}

// =============================================
// SCREEN UTAMA LIST PELAMAR
// =============================================
@Composable
fun ApplicantListScreen(
    jobId: String,
    jobTitle: String,
    onBack: () -> Unit
) {
    // Reactive — recompose tiap ada perubahan
    var refreshKey by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        ApplicationRepository.addListener { refreshKey++ }
    }

    val applicants = remember(refreshKey) {
        ApplicationRepository.getApplicationsByJob(jobId.toIntOrNull() ?: -1)
    }

    val pendingCount  = applicants.count { it.status == ApplicationStatus.PENDING }
    val acceptedCount = applicants.count { it.status == ApplicationStatus.ACCEPTED }

    Column(modifier = Modifier.fillMaxSize().background(BgLight)) {

        // ── HEADER ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(PrimaryBlue, PrimaryLight)))
                .padding(top = 48.dp, bottom = 24.dp, start = 16.dp, end = 16.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                    Spacer(Modifier.width(4.dp))
                    Column {
                        Text(
                            "Daftar Pelamar",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            jobTitle,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Statistik mini
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ApplicantStatChip(
                        label = "${applicants.size} Total",
                        color = Color.White.copy(alpha = 0.2f)
                    )
                    ApplicantStatChip(
                        label = "$pendingCount Menunggu",
                        color = Color(0x33FFC107)
                    )
                    ApplicantStatChip(
                        label = "$acceptedCount Diterima",
                        color = Color(0x3300E676)
                    )
                }
            }
        }

        // ── LIST PELAMAR ──
        if (applicants.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.PeopleOutline,
                        null,
                        tint = TextSecondary,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("Belum ada pelamar", color = TextSecondary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Text("Tunggu mahasiswa melamar pekerjaan ini", color = TextSecondary, fontSize = 13.sp)
                }
            }
        } else {
            val sorted = applicants.sortedWith(compareBy { it.status.ordinal })
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items = sorted, key = { it.id }) { app: JobApplication ->
                    ApplicantCard(
                        application = app,
                        onAccept = {
                            ApplicationRepository.acceptApplication(app.id)
                            refreshKey++
                        },
                        onReject = {
                            ApplicationRepository.rejectApplication(app.id)
                            refreshKey++
                        }
                    )
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

// =============================================
// KARTU PELAMAR
// =============================================
@Composable
fun ApplicantCard(
    application: JobApplication,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    var showConfirmDialog by remember { mutableStateOf<String?>(null) } // "accept" | "reject"

    val avatarColor = Color(application.freelancerAvatarColor)

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── BARIS ATAS: Avatar + Nama + Status badge ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(avatarColor.copy(alpha = 0.15f))
                        .border(2.dp, avatarColor.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        application.freelancerAvatarInitial,
                        color = avatarColor,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        application.freelancerName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                    Text(
                        application.freelancerMajor,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Text(
                        application.freelancerUniversity,
                        fontSize = 11.sp,
                        color = TextSecondary.copy(alpha = 0.8f)
                    )
                }

                // Status badge
                val (badgeLabel, badgeBg, badgeTxt) = when (application.status) {
                    ApplicationStatus.PENDING  -> Triple("Menunggu", OrangeLight, AccentOrange)
                    ApplicationStatus.ACCEPTED -> Triple("Diterima ✓", GreenLight, GreenSuccess)
                    ApplicationStatus.REJECTED -> Triple("Ditolak", RedLight, RedAccent)
                }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = badgeBg
                ) {
                    Text(
                        badgeLabel,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = badgeTxt,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Rating
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, null, tint = Color(0xFFF9A825), modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    "${application.freelancerRating} Rating",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                if (application.appliedAt.isNotBlank()) {
                    Text(
                        "  •  Melamar ${application.appliedAt}",
                        fontSize = 11.sp,
                        color = TextSecondary.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Bio singkat
            if (application.freelancerBio.isNotBlank()) {
                Text(
                    application.freelancerBio,
                    fontSize   = 13.sp,
                    color      = TextSecondary,
                    lineHeight = 18.sp,
                    maxLines   = 3,
                    overflow   = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(10.dp))
            }

            // Skills chips — chunked 3 per baris, tanpa FlowRow experimental
            if (application.freelancerSkills.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    application.freelancerSkills.chunked(3).forEach { rowSkills ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            rowSkills.forEach { skill ->
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = BlueLight
                                ) {
                                    Text(
                                        skill,
                                        modifier   = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        color      = PrimaryLight,
                                        fontSize   = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // ── TOMBOL TERIMA / TOLAK (hanya jika masih PENDING) ──
            if (application.status == ApplicationStatus.PENDING) {
                HorizontalDivider(color = BgLight, thickness = 1.dp)
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Tolak
                    OutlinedButton(
                        onClick  = { showConfirmDialog = "reject" },
                        modifier = Modifier.weight(1f).height(42.dp),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.outlinedButtonColors(contentColor = RedAccent),
                        border   = BorderStroke(1.5.dp, RedAccent)
                    ) {
                        Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Tolak", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                    // Terima
                    Button(
                        onClick  = { showConfirmDialog = "accept" },
                        modifier = Modifier.weight(1f).height(42.dp),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = GreenSuccess)
                    ) {
                        Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Terima", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            } else {
                // Sudah diputuskan — tampilkan info
                HorizontalDivider(color = BgLight, thickness = 1.dp)
                Spacer(Modifier.height(10.dp))
                val msg = if (application.status == ApplicationStatus.ACCEPTED)
                    "✅ Kamu telah menerima pelamar ini. Notifikasi sudah dikirim."
                else
                    "❌ Lamaran ini sudah ditolak."
                Text(msg, fontSize = 12.sp, color = TextSecondary)
            }
        }
    }

    // ── DIALOG KONFIRMASI ──
    if (showConfirmDialog != null) {
        val isAccept = showConfirmDialog == "accept"
        AlertDialog(
            onDismissRequest = { showConfirmDialog = null },
            icon = {
                Icon(
                    if (isAccept) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    null,
                    tint = if (isAccept) GreenSuccess else RedAccent
                )
            },
            title = {
                Text(
                    if (isAccept) "Terima Pelamar?" else "Tolak Pelamar?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    if (isAccept)
                        "Kamu akan menerima ${application.freelancerName} untuk pekerjaan ini. Notifikasi akan dikirim ke pelamar."
                    else
                        "Kamu akan menolak lamaran ${application.freelancerName}. Pelamar akan mendapat notifikasi.",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (isAccept) onAccept() else onReject()
                        showConfirmDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAccept) GreenSuccess else RedAccent
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(if (isAccept) "Ya, Terima" else "Ya, Tolak", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = null }) {
                    Text("Batal", color = TextSecondary)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

// =============================================
// HELPER COMPOSABLE
// =============================================
@Composable
fun ApplicantStatChip(label: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}