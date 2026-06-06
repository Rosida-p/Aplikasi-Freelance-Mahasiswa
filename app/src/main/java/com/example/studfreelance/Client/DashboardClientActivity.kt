package com.example.studfreelance.Client

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studfreelance.JobRepository
import com.example.studfreelance.LoginActivity
import androidx.compose.ui.platform.LocalContext
import com.example.studfreelance.WorkSubmissionRepository


val PrimaryBlue   = Color(0xFF1565C0)
val PrimaryLight  = Color(0xFF1E88E5)
val AccentOrange  = Color(0xFFFF6F00)
val BgLight       = Color(0xFFF4F6FB)
val CardWhite     = Color(0xFFFFFFFF)
val TextPrimary   = Color(0xFF1A1A2E)
val TextSecondary = Color(0xFF6B7280)
val GreenSuccess  = Color(0xFF2E7D32)
val GreenLight    = Color(0xFFE8F5E9)
val BlueLight     = Color(0xFFE3F2FD)
val OrangeLight   = Color(0xFFFFF3E0)
val RedLight      = Color(0xFFFFEBEE)
val RedAccent     = Color(0xFFC62828)

// =============================================
// DATA MODEL
// =============================================
data class PostedJob(
    val id: Int,
    val judul: String,
    val budget: String,
    val deadline: String,
    val kategori: String,
    val status: String,        // "active", "done", "review"
    val jumlahPelamar: Int
)

// =============================================
// ACTIVITY
// =============================================
class DashboardClientActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showContent()
    }

    // onResume dipanggil setiap kali kembali ke activity ini
    // (termasuk setelah dari PostJobActivity) → data selalu fresh
    override fun onResume() {
        super.onResume()
        showContent()
    }

    private fun showContent() {
        setContent {
            DashboardClientScreen(
                onPostJob = {
                    startActivity(Intent(this, PostJobActivity::class.java))
                },
                onLogout = {
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            )
        }
    }
}

// =============================================
// SCREEN UTAMA DASHBOARD
// =============================================
@Composable
fun DashboardClientScreen(
    onPostJob: () -> Unit,
    onLogout: () -> Unit
) {
    // Ambil data terbaru dari Repository setiap kali recompose
    val postedJobs    = remember { mutableStateListOf<PostedJob>().also { it.addAll(JobRepository.getAllJobs()) } }
    val totalAktif    = postedJobs.count { it.status == "active" }
    val totalPelamar  = postedJobs.sumOf { it.jumlahPelamar }
    val totalSelesai  = postedJobs.count { it.status == "done" }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgLight)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {

            // --- HEADER GRADIENT ---
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(PrimaryBlue, PrimaryLight)))
                        .padding(top = 48.dp, bottom = 32.dp, start = 20.dp, end = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Selamat Datang 👋",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 14.sp
                            )
                            Text(
                                "StudFreelance",
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    "  Penyedia Pekerjaan  ",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                        // Avatar / Logout
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.25f))
                                .clickable { onLogout() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Logout",
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                }
            }

            // --- KARTU STATISTIK ---
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .offset(y = (-20).dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        modifier     = Modifier.weight(1f),
                        label        = "Aktif",
                        value        = "$totalAktif",
                        icon         = Icons.Default.Work,
                        bgColor      = BlueLight,
                        iconColor    = PrimaryLight
                    )
                    StatCard(
                        modifier     = Modifier.weight(1f),
                        label        = "Pelamar",
                        value        = "$totalPelamar",
                        icon         = Icons.Default.People,
                        bgColor      = OrangeLight,
                        iconColor    = AccentOrange
                    )
                    StatCard(
                        modifier     = Modifier.weight(1f),
                        label        = "Selesai",
                        value        = "$totalSelesai",
                        icon         = Icons.Default.CheckCircle,
                        bgColor      = GreenLight,
                        iconColor    = GreenSuccess
                    )
                }
            }

            // --- TOMBOL POST PEKERJAAN BARU ---
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .offset(y = (-10).dp)
                ) {
                    Button(
                        onClick = onPostJob,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Posting Pekerjaan Baru",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                }
            }

            // --- JUDUL LIST ---
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Pekerjaan Diposting",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                    Text(
                        "${postedJobs.size} total",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }

            // --- LIST PEKERJAAN (dari Repository) ---
            if (postedJobs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.WorkOff,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Belum ada pekerjaan diposting",
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                            Text(
                                "Tap tombol oranye di atas untuk mulai!",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } else {
                items(postedJobs) { job ->
                    val context = LocalContext.current
                    val activity = context as android.app.Activity
                    val submission = remember { WorkSubmissionRepository.getSubmissionByJob(job.id) }

                    PostedJobCard(
                        job = job,
                        onClickPelamar = {
                            val intent = Intent(activity, ApplicantListActivity::class.java)
                            intent.putExtra("JOB_ID", job.id.toString())
                            intent.putExtra("JOB_TITLE", job.judul)
                            activity.startActivity(intent)
                        },
                        onClickHasil = {
                            val intent = Intent(activity, JobProgressActivity::class.java)
                            intent.putExtra("JOB_ID", job.id)
                            intent.putExtra("JOB_TITLE", job.judul)
                            intent.putExtra("APPLICANT_NAME", submission?.freelancerName ?: "Freelancer")
                            intent.putExtra("JOB_BUDGET", job.budget)
                            activity.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

// KOMPONEN: KARTU STATISTIK
@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: ImageVector,
    bgColor: Color,
    iconColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextPrimary)
            Text(label, fontSize = 11.sp, color = TextSecondary)
        }
    }
}

// =============================================
// KOMPONEN: KARTU PEKERJAAN
// =============================================
@Composable
fun PostedJobCard(
    job: PostedJob,
    onClickPelamar: (PostedJob) -> Unit,
    onClickHasil: (PostedJob) -> Unit = {}
) {
    val (statusLabel, statusBg, statusTxt) = when (job.status) {
        "active" -> Triple("Aktif", BlueLight, PrimaryLight)
        "done"   -> Triple("Selesai", GreenLight, GreenSuccess)
        else     -> Triple("Review", OrangeLight, AccentOrange)
    }

    val submission = remember { WorkSubmissionRepository.getSubmissionByJob(job.id) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        job.judul,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = BlueLight
                    ) {
                        Text(
                            "  ${job.kategori}  ",
                            fontSize = 11.sp,
                            color = PrimaryLight,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusBg
                ) {
                    Text(
                        "  $statusLabel  ",
                        fontSize = 11.sp,
                        color = statusTxt,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = BgLight, thickness = 1.dp)
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoChip(Icons.Default.AttachMoney, job.budget)
                InfoChip(Icons.Default.Timer, job.deadline)
                Row(modifier = Modifier.clickable { onClickPelamar(job) }) {
                    InfoChip(Icons.Default.People, "${job.jumlahPelamar} Pelamar")
                }
            }

            // Tombol lihat hasil kerja (muncul kalau freelancer sudah upload)
            if (submission != null && job.status != "done") {
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = { onClickHasil(job) },
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                ) {
                    Icon(Icons.Default.Visibility, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Lihat Hasil Kerja", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
// KOMPONEN: CHIP INFO KECIL
@Composable
fun InfoChip(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(3.dp))
        Text(text, fontSize = 12.sp, color = TextSecondary)
    }
}