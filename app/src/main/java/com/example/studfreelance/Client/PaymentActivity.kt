package com.example.studfreelance.Client

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.example.studfreelance.AppNotification
import com.example.studfreelance.ApplicationRepository
import com.example.studfreelance.NotificationType
import com.example.studfreelance.WorkSubmissionRepository
import java.text.SimpleDateFormat
import java.util.*

class PaymentActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val jobId          = intent.getIntExtra("JOB_ID", -1)
        val jobTitle       = intent.getStringExtra("JOB_TITLE") ?: "Pekerjaan"
        val freelancerName = intent.getStringExtra("FREELANCER_NAME") ?: "Freelancer"
        val jobBudget = intent.getStringExtra("JOB_BUDGET") ?: "Rp0"

        setContent {
            PaymentScreen(
                jobTitle       = jobTitle,
                freelancerName = freelancerName,
                jobBudget      = jobBudget,
                onPay = {
                    // Notifikasi ke Freelancer
                    ApplicationRepository.addFreelancerNotification(
                        AppNotification(
                            id        = "notif_fl_paid_${System.currentTimeMillis()}",
                            type      = NotificationType.PAYMENT_RECEIVED,
                            title     = "Pembayaran Diterima! 💰",
                            message   = "Client telah membayar untuk pekerjaan \"$jobTitle\". Cek riwayat pekerjaan kamu!",
                            timestamp = SimpleDateFormat("HH:mm, dd MMM", Locale("id")).format(Date())
                        )
                    )
                    WorkSubmissionRepository.updateStatus(jobId, "paid")
                    startActivity(
                        android.content.Intent(this, RatingActivity::class.java).apply {
                            putExtra("JOB_ID", jobId)
                            putExtra("JOB_TITLE", jobTitle)
                            putExtra("FREELANCER_NAME", freelancerName)
                        }
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    jobTitle: String,
    freelancerName: String,
    jobBudget: String,
    onPay: () -> Unit
){
    var isPaid by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(Color(0xFF1565C0), Color(0xFF1E88E5))))
            ) {
                TopAppBar(
                    title = { Text("Pembayaran", color = Color.White, fontWeight = FontWeight.Bold) },
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
        ) {
            // Info
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Detail Pembayaran", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Pekerjaan", color = Color(0xFF6B7280), fontSize = 13.sp)
                        Text(jobTitle, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Freelancer", color = Color(0xFF6B7280), fontSize = 13.sp)
                        Text(freelancerName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(jobBudget, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF0D47A1))                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Metode Pembayaran (simulasi)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Metode Pembayaran", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccountBalance, null, tint = Color(0xFF0D47A1), modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("Transfer Bank (Simulasi)", fontWeight = FontWeight.SemiBold)
                            Text("BCA • ****1234", fontSize = 12.sp, color = Color(0xFF6B7280))
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    isPaid = true
                    onPay()
                },
                enabled  = !isPaid,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6F00))
            ) {
                Icon(Icons.Default.Payment, null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("Bayar Sekarang", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}