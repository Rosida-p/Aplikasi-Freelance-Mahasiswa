package com.example.studfreelance.Freelancer

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.studfreelance.ApplicationRepository
import com.example.studfreelance.AppNotification
import com.example.studfreelance.NotificationType
import com.example.studfreelance.WorkSubmission
import com.example.studfreelance.WorkSubmissionRepository
import java.text.SimpleDateFormat
import java.util.*

class UploadResultActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val jobId    = intent.getIntExtra("JOB_ID", -1)
        val jobTitle = intent.getStringExtra("JOB_TITLE") ?: "Pekerjaan"
        val freelancerId   = intent.getStringExtra("FREELANCER_ID") ?: ""
        val freelancerName = intent.getStringExtra("FREELANCER_NAME") ?: ""

        setContent {
            UploadResultScreen(
                jobId          = jobId,
                jobTitle       = jobTitle,
                freelancerId   = freelancerId,
                freelancerName = freelancerName,
                onBack         = { finish() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadResultScreen(
    jobId: Int,
    jobTitle: String,
    freelancerId: String,
    freelancerName: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var description by remember { mutableStateOf("") }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var fileName    by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }
    var isDone      by remember { mutableStateOf(false) }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedUri = it
            fileName = it.lastPathSegment ?: "file_terpilih"
        }
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(Color(0xFF0D47A1), Color(0xFF1E88E5))))
            ) {
                TopAppBar(
                    title = { Text("Upload Hasil Kerja", color = Color.White, fontWeight = FontWeight.Bold) },
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
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFFE3F2FD), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Work, null, tint = Color(0xFF0D47A1), modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Pekerjaan", fontSize = 12.sp, color = Color(0xFF6B7280))
                        Text(jobTitle, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Deskripsi
            Text("Deskripsi Hasil Kerja", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = { Text("Jelaskan apa yang sudah kamu kerjakan...") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                shape = RoundedCornerShape(12.dp),
                maxLines = 5
            )

            Spacer(Modifier.height(16.dp))

            // Upload File
            Text("Lampiran File", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, Color(0xFF0D47A1), RoundedCornerShape(12.dp))
                    .clickable { filePicker.launch("*/*") }
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                if (selectedUri == null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.UploadFile, null, tint = Color(0xFF0D47A1), modifier = Modifier.size(40.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Ketuk untuk pilih file", color = Color(0xFF0D47A1), fontWeight = FontWeight.SemiBold)
                        Text("PDF, gambar, atau file lainnya", fontSize = 12.sp, color = Color(0xFF6B7280))
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.InsertDriveFile, null, tint = Color(0xFF0D47A1))
                        Spacer(Modifier.width(8.dp))
                        Text(fileName, fontWeight = FontWeight.Medium, color = Color(0xFF0D47A1))
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Tombol Submit
            Button(
                onClick = {
                    if (description.isBlank()) {
                        Toast.makeText(context, "Deskripsi tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isUploading = true

                    fun saveSubmission(fileUrl: String, fName: String) {
                        val submission = WorkSubmission(
                            id             = "sub_${jobId}_${System.currentTimeMillis()}",
                            jobId          = jobId,
                            jobTitle       = jobTitle,
                            freelancerId   = freelancerId,
                            freelancerName = freelancerName,
                            description    = description,
                            fileUrl        = fileUrl,
                            fileName       = fName,
                            submittedAt    = SimpleDateFormat("HH:mm, dd MMM", Locale("id")).format(Date()),
                            status         = "pending"
                        )
                        WorkSubmissionRepository.addSubmission(submission)

                        // Notifikasi ke Client
                        ApplicationRepository.addClientNotification(
                            AppNotification(
                                id        = "notif_cl_upload_${System.currentTimeMillis()}",
                                type      = NotificationType.WORK_SUBMITTED,
                                title     = "Hasil Kerja Masuk! 📁",
                                message   = "$freelancerName telah mengumpulkan hasil kerja untuk \"$jobTitle\".",
                                timestamp = SimpleDateFormat("HH:mm, dd MMM", Locale("id")).format(Date())
                            )
                        )
                        isUploading = false
                        isDone      = true
                    }

                    if (selectedUri != null) {
                        MediaManager.get().upload(selectedUri)
                            .callback(object : UploadCallback {
                                override fun onStart(requestId: String) {}
                                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                                    val url = resultData["secure_url"] as? String ?: ""
                                    saveSubmission(url, fileName)
                                }
                                override fun onError(requestId: String, error: ErrorInfo) {
                                    isUploading = false
                                    Toast.makeText(context, "Upload gagal: ${error.description}", Toast.LENGTH_SHORT).show()
                                }
                                override fun onReschedule(requestId: String, error: ErrorInfo) {}
                            })
                            .dispatch()
                    } else {
                        saveSubmission("", "")
                    }
                },
                enabled  = !isUploading && !isDone,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1))
            ) {
                if (isUploading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Mengupload...", color = Color.White, fontWeight = FontWeight.Bold)
                } else if (isDone) {
                    Icon(Icons.Default.CheckCircle, null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Berhasil Dikumpulkan!", color = Color.White, fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.Send, null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Kumpulkan Hasil Kerja", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            if (isDone) {
                Spacer(Modifier.height(12.dp))
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF2E7D32))
                        Spacer(Modifier.width(8.dp))
                        Text("Hasil kerja berhasil dikumpulkan! Client akan segera mereview.", color = Color(0xFF2E7D32), fontSize = 13.sp)
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick  = onBack,
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(12.dp)
                ) {
                    Text("Kembali")
                }
            }
        }
    }
}