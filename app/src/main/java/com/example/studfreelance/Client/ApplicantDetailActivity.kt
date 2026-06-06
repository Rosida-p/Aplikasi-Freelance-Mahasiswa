package com.example.studfreelance.Client

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studfreelance.ApplicationRepository

class ApplicantDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val applicationId  = intent.getStringExtra("APPLICATION_ID") ?: ""
        val name           = intent.getStringExtra("APPLICANT_NAME") ?: ""
        val major          = intent.getStringExtra("APPLICANT_MAJOR") ?: ""
        val university     = intent.getStringExtra("APPLICANT_UNI") ?: ""
        val bio            = intent.getStringExtra("APPLICANT_BIO") ?: ""
        val rating         = intent.getFloatExtra("APPLICANT_RATING", 0f)
        val completedJobs  = intent.getIntExtra("APPLICANT_JOBS", 0)
        val skills         = intent.getStringArrayExtra("APPLICANT_SKILLS")?.toList() ?: emptyList()
        val avatarInitial  = intent.getStringExtra("APPLICANT_AVATAR") ?: "?"
        val jobTitle       = intent.getStringExtra("JOB_TITLE") ?: ""
        val jobId          = intent.getStringExtra("JOB_ID") ?: ""

        setContent {
            ApplicantDetailScreen(
                applicationId = applicationId,
                name          = name,
                major         = major,
                university    = university,
                bio           = bio,
                rating        = rating,
                completedJobs = completedJobs,
                skills        = skills,
                avatarInitial = avatarInitial,
                jobTitle      = jobTitle,
                onBack        = { finish() },
                onAccept      = {
                    ApplicationRepository.acceptApplication(applicationId)
                    // Navigasi ke JobProgressActivity
                    val intent = Intent(this, JobProgressActivity::class.java).apply {
                        putExtra("APPLICANT_NAME", name)
                        putExtra("JOB_TITLE",      jobTitle)
                        putExtra("JOB_ID",         jobId)
                        putExtra("APPLICATION_ID", applicationId)
                    }
                    startActivity(intent)
                    finish()
                },
                onReject = {
                    ApplicationRepository.rejectApplication(applicationId)
                    finish()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicantDetailScreen(
    applicationId: String,
    name: String,
    major: String,
    university: String,
    bio: String,
    rating: Float,
    completedJobs: Int,
    skills: List<String>,
    avatarInitial: String,
    jobTitle: String,
    onBack: () -> Unit,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    var showConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(Color(0xFF0D47A1), Color(0xFF1E88E5))))
            ) {
                TopAppBar(
                    title = {
                        Text("Profil Pelamar", color = Color.White, fontWeight = FontWeight.Bold)
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        },
        containerColor = Color(0xFFF4F6FB)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Header profil
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color(0xFF1565C0), Color(0xFF1E88E5))))
                    .padding(bottom = 24.dp, start = 20.dp, end = 20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.22f))
                            .border(3.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(avatarInitial, color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(name, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(major, color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                    Text(university, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$rating ⭐", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Rating", color = Color.White.copy(alpha = 0.75f), fontSize = 10.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$completedJobs", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Job Selesai", color = Color.White.copy(alpha = 0.75f), fontSize = 10.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${skills.size}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Keahlian", color = Color.White.copy(alpha = 0.75f), fontSize = 10.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Info pekerjaan yang dilamar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Work, null, tint = Color(0xFF1565C0), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("Melamar untuk:", fontSize = 11.sp, color = Color(0xFF1565C0))
                        Text(jobTitle, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0D47A1))
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Bio
            DetailCard(title = "Bio / Tentang", icon = Icons.Default.Person) {
                Text(
                    bio.ifBlank { "Tidak ada bio." },
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280),
                    lineHeight = 22.sp
                )
            }

            Spacer(Modifier.height(10.dp))

            // Keahlian
            DetailCard(title = "Keahlian", icon = Icons.Default.StarHalf) {
                if (skills.isEmpty()) {
                    Text("Tidak ada keahlian tercantum.", fontSize = 14.sp, color = Color(0xFF6B7280))
                } else {
                    Column {
                        skills.chunked(3).forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                row.forEach { skill ->
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = Color(0xFFE0F2F1)
                                    ) {
                                        Text(
                                            skill,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            color = Color(0xFF00695C),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Tombol Accept / Reject
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { showConfirm = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Pilih Freelancer Ini", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                }

                OutlinedButton(
                    onClick  = onReject,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape  = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC62828))
                ) {
                    Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Tolak Pelamar", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    // Dialog konfirmasi pilih freelancer
    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            icon = { Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF2E7D32)) },
            title = { Text("Pilih Freelancer?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Kamu akan memilih $name untuk mengerjakan \"$jobTitle\". Freelancer akan mendapat notifikasi.",
                    lineHeight = 22.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { showConfirm = false; onAccept() },
                    colors  = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) { Text("Ya, Pilih!", color = Color.White, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) {
                    Text("Batal", color = Color(0xFF6B7280))
                }
            }
        )
    }
}

@Composable
fun DetailCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = Color(0xFF1565C0), modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1A1A2E))
            }
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}