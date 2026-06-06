package com.example.studfreelance.Client

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.example.studfreelance.AppNotification
import com.example.studfreelance.ApplicationRepository
import com.example.studfreelance.NotificationType
import com.example.studfreelance.WorkSubmissionRepository
import java.text.SimpleDateFormat
import java.util.*

class JobProgressActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val jobId          = intent.getIntExtra("JOB_ID", -1)
        val jobTitle       = intent.getStringExtra("JOB_TITLE") ?: "Pekerjaan"
        val freelancerName = intent.getStringExtra("APPLICANT_NAME") ?: "Freelancer"

        setContent {
            JobProgressScreen(
                jobId          = jobId,
                jobTitle       = jobTitle,
                freelancerName = freelancerName,
                onApprove      = {
                    val intent = android.content.Intent(this, PaymentActivity::class.java).apply {
                        putExtra("JOB_ID", jobId)
                        putExtra("JOB_TITLE", jobTitle)
                        putExtra("FREELANCER_NAME", freelancerName)
                        putExtra("JOB_BUDGET", intent.getStringExtra("JOB_BUDGET") ?: "")
                    }
                    startActivity(intent)
                },
                onBack = { finish() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobProgressScreen(
    jobId: Int,
    jobTitle: String,
    freelancerName: String,
    onApprove: () -> Unit,
    onBack: () -> Unit
) {
    val context    = LocalContext.current
    val submission = remember { WorkSubmissionRepository.getSubmissionByJob(jobId) }
    var showRevisionDialog by remember { mutableStateOf(false) }
    var revisionNote       by remember { mutableStateOf("") }
    var isApproved         by remember { mutableStateOf(false) }
    var isRevision         by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(Color(0xFF1565C0), Color(0xFF1E88E5))))
            ) {
                TopAppBar(
                    title = { Text("Progress Pekerjaan", color = Color.White, fontWeight = FontWeight.Bold) },
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
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Info Job
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Pekerjaan", fontSize = 12.sp, color = Color(0xFF6B7280))
                    Text(jobTitle, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("Dikerjakan oleh: $freelancerName", fontSize = 13.sp, color = Color(0xFF6B7280))
                }
            }

            Spacer(Modifier.height(16.dp))

            // Hasil Kerja
            if (submission != null) {
                Text("Hasil Kerja", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(Modifier.height(8.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, null, tint = Color(0xFF0D47A1), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(submission.freelancerName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(submission.submittedAt, fontSize = 11.sp, color = Color(0xFF6B7280))
                        }
                        Spacer(Modifier.height(10.dp))
                        Text("Deskripsi:", fontSize = 12.sp, color = Color(0xFF6B7280))
                        Text(submission.description, fontSize = 14.sp)
                        if (submission.fileName.isNotBlank()) {
                            Spacer(Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AttachFile, null, tint = Color(0xFF0D47A1), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(submission.fileName, fontSize = 13.sp, color = Color(0xFF0D47A1))
                            }
                        }
                    }
                }
            } else {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.HourglassEmpty, null, tint = Color(0xFFE65100))
                        Spacer(Modifier.width(8.dp))
                        Text("Freelancer belum mengumpulkan hasil kerja.", color = Color(0xFFE65100), fontSize = 13.sp)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            if (submission != null && !isApproved && !isRevision) {
                Button(
                    onClick = {
                        WorkSubmissionRepository.updateStatus(jobId, "approved")
                        ApplicationRepository.addFreelancerNotification(
                            AppNotification(
                                id        = "notif_fl_approved_${System.currentTimeMillis()}",
                                type      = NotificationType.WORK_APPROVED,
                                title     = "Hasil Kerja Disetujui! 🎉",
                                message   = "Client menyetujui hasil kerjamu untuk \"$jobTitle\". Pembayaran sedang diproses.",
                                timestamp = SimpleDateFormat("HH:mm, dd MMM", Locale("id")).format(Date())
                            )
                        )
                        isApproved = true
                        onApprove()
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Setujui Hasil Kerja", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(10.dp))

                OutlinedButton(
                    onClick  = { showRevisionDialog = true },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Refresh, null, tint = Color(0xFF0D47A1))
                    Spacer(Modifier.width(8.dp))
                    Text("Minta Revisi", color = Color(0xFF0D47A1), fontWeight = FontWeight.Bold)
                }
            }

            if (isRevision) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Refresh, null, tint = Color(0xFFE65100))
                        Spacer(Modifier.width(8.dp))
                        Text("Permintaan revisi telah dikirim ke freelancer.", color = Color(0xFFE65100), fontSize = 13.sp)
                    }
                }
            }
        }
    }

    if (showRevisionDialog) {
        AlertDialog(
            onDismissRequest = { showRevisionDialog = false },
            title = { Text("Minta Revisi", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Tuliskan catatan revisi untuk freelancer:", fontSize = 13.sp, color = Color(0xFF6B7280))
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value         = revisionNote,
                        onValueChange = { revisionNote = it },
                        placeholder   = { Text("Contoh: Tolong perbaiki bagian desain header...") },
                        modifier      = Modifier.fillMaxWidth().height(100.dp),
                        shape         = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        WorkSubmissionRepository.updateStatus(jobId, "revision")
                        ApplicationRepository.addFreelancerNotification(
                            AppNotification(
                                id        = "notif_fl_revision_${System.currentTimeMillis()}",
                                type      = NotificationType.WORK_REVISION,
                                title     = "Revisi Diminta 🔄",
                                message   = "Client meminta revisi untuk \"$jobTitle\". Catatan: ${revisionNote.ifBlank { "Tidak ada catatan." }}",
                                timestamp = SimpleDateFormat("HH:mm, dd MMM", Locale("id")).format(Date())
                            )
                        )
                        showRevisionDialog = false
                        isRevision = true
                        Toast.makeText(context, "Permintaan revisi terkirim!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1)),
                    shape  = RoundedCornerShape(12.dp)
                ) {
                    Text("Kirim Revisi", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRevisionDialog = false }) {
                    Text("Batal", color = Color(0xFF6B7280))
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}