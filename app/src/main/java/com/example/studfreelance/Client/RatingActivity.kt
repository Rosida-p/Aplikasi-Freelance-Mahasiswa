package com.example.studfreelance.Client

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.example.studfreelance.AppNotification
import com.example.studfreelance.ApplicationRepository
import com.example.studfreelance.JobRepository
import com.example.studfreelance.NotificationType
import java.text.SimpleDateFormat
import java.util.*
import com.example.studfreelance.Freelancer.JobFinishedActivity
import com.example.studfreelance.WorkSubmissionRepository

class RatingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val jobId          = intent.getIntExtra("JOB_ID", -1)
        val jobTitle       = intent.getStringExtra("JOB_TITLE") ?: "Pekerjaan"
        val freelancerName = intent.getStringExtra("FREELANCER_NAME") ?: "Freelancer"

        setContent {
            RatingScreen(
                jobTitle       = jobTitle,
                freelancerName = freelancerName,
                onSubmit = { rating, review ->
                    // Simpan rating ke submission
                    WorkSubmissionRepository.updateRating(jobId, rating, review)
                    // Update status job jadi "done"
                    JobRepository.updateJobStatus(jobId, "done")

                    // Notifikasi ke Freelancer
                    ApplicationRepository.addFreelancerNotification(
                        AppNotification(
                            id        = "notif_fl_rating_${System.currentTimeMillis()}",
                            type      = NotificationType.RATING_GIVEN,
                            title     = "Kamu Mendapat Rating! ⭐",
                            message   = "Client memberimu rating $rating bintang untuk \"$jobTitle\". Terima kasih!",
                            timestamp = SimpleDateFormat("HH:mm, dd MMM", Locale("id")).format(Date())
                        )
                    )
                    startActivity(
                        android.content.Intent(this, JobFinishedActivity::class.java)
                    )
                    finish()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatingScreen(
    jobTitle: String,
    freelancerName: String,
    onSubmit: (Int, String) -> Unit
) {
    var rating by remember { mutableStateOf(0) }
    var review by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(Color(0xFF1565C0), Color(0xFF1E88E5))))
            ) {
                TopAppBar(
                    title = { Text("Beri Penilaian", color = Color.White, fontWeight = FontWeight.Bold) },
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
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // Info
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Pekerjaan", fontSize = 12.sp, color = Color(0xFF6B7280))
                        Text(jobTitle, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(Modifier.height(4.dp))
                        Text("Freelancer: $freelancerName", fontSize = 13.sp, color = Color(0xFF6B7280))
                    }
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    "Seberapa puas kamu dengan hasil kerja freelancer ini?",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )

                Spacer(Modifier.height(16.dp))

                // Bintang interaktif
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(5) { index ->
                        Icon(
                            imageVector = if (index < rating) Icons.Filled.Star else Icons.Outlined.StarOutline,
                            contentDescription = null,
                            tint = if (index < rating) Color(0xFFFFC107) else Color(0xFFBBBBBB),
                            modifier = Modifier
                                .size(48.dp)
                                .clickable { rating = index + 1 }
                                .padding(4.dp)
                        )
                    }
                }

                if (rating > 0) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = when (rating) {
                            1 -> "😞 Sangat Tidak Puas"
                            2 -> "😐 Tidak Puas"
                            3 -> "🙂 Cukup"
                            4 -> "😊 Puas"
                            5 -> "🤩 Sangat Puas!"
                            else -> ""
                        },
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = 14.sp,
                        color = Color(0xFF0D47A1),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(Modifier.height(20.dp))

                Text("Tulis Ulasan (opsional)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value         = review,
                    onValueChange = { review = it },
                    placeholder   = { Text("Ceritakan pengalamanmu...") },
                    modifier      = Modifier.fillMaxWidth().height(120.dp),
                    shape         = RoundedCornerShape(12.dp),
                    maxLines      = 5
                )
            }

            Button(
                onClick  = { if (rating > 0) onSubmit(rating, review) },
                enabled  = rating > 0,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1))
            ) {
                Icon(Icons.Filled.Star, null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("Kirim Penilaian", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}