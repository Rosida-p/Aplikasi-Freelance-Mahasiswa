package com.example.studfreelance.Freelancer

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studfreelance.Client.DashboardClientActivity

class JobFinishedActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            JobFinishedScreen(
                onBackToDashboard = {
                    val intent = Intent(this, DashboardClientActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            )
        }
    }
}

@Composable
fun JobFinishedScreen(
    onBackToDashboard: () -> Unit
) {
    Column(
        modifier = Modifier.Companion
            .fillMaxSize()
            .background(Color(0xFFF4F6FB))
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Companion.CenterHorizontally
    ) {

        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF1565C0),
            modifier = Modifier.Companion.size(80.dp)
        )

        Spacer(modifier = Modifier.Companion.height(20.dp))

        Text(
            "Pekerjaan Selesai 🎉",
            fontSize = 22.sp,
            fontWeight = FontWeight.Companion.Bold
        )

        Spacer(modifier = Modifier.Companion.height(10.dp))

        Text(
            "Terima kasih sudah menggunakan aplikasi kami.",
            color = Color.Companion.Gray
        )

        Spacer(modifier = Modifier.Companion.height(30.dp))

        Button(
            onClick = onBackToDashboard,
            modifier = Modifier.Companion.fillMaxWidth()
        ) {
            Text("Kembali ke Dashboard")
        }
    }
}