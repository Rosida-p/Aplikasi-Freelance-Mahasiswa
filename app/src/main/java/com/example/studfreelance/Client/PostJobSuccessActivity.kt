package com.example.studfreelance.Client

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class PostJobSuccessActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val judul    = intent.getStringExtra("JUDUL")    ?: "-"
        val budget   = intent.getStringExtra("BUDGET")   ?: "-"
        val deadline = intent.getStringExtra("DEADLINE") ?: "-"
        val kategori = intent.getStringExtra("KATEGORI") ?: "-"

        setContent {
            PostJobSuccessScreen(
                judul    = judul,
                budget   = budget,
                deadline = deadline,
                kategori = kategori,
                onBackToDashboard = {
                    // Kembali ke dashboard — data sudah tersimpan di Repository
                    val intent = Intent(this, DashboardClientActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    finish()
                },
                onPostAnother = {
                    startActivity(Intent(this, PostJobActivity::class.java))
                    finish()
                }
            )
        }
    }
}

@Composable
fun PostJobSuccessScreen(
    judul: String,
    budget: String,
    deadline: String,
    kategori: String,
    onBackToDashboard: () -> Unit,
    onPostAnother: () -> Unit
) {
    // Animasi pulse ikon centang
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue  = 1.08f,
        animationSpec = infiniteRepeatable(
            tween(800, easing = EaseInOut),
            RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0D47A1), Color(0xFF1565C0), Color(0xFF42A5F5))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Ikon sukses animasi
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(82.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = GreenSuccess,
                        modifier = Modifier.size(52.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "Pekerjaan Berhasil Diposting! 🎉",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Mahasiswa berbakat akan segera melihat dan melamar pekerjaan kamu.",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(Modifier.height(28.dp))

            // Ringkasan pekerjaan
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Ringkasan Pekerjaan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(14.dp))

                    SummaryRow(Icons.Default.Edit,     "Judul",    judul, PrimaryLight)
                    SummaryDivider()
                    SummaryRow(Icons.Default.List,     "Kategori", kategori, AccentOrange)
                    SummaryDivider()
                    SummaryRow(Icons.Default.Payments, "Budget",   budget, GreenSuccess)
                    SummaryDivider()
                    SummaryRow(Icons.Default.Schedule, "Deadline", deadline, Color(0xFF9C27B0))

                    Spacer(Modifier.height(14.dp))

                    // Badge status aktif
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = GreenLight
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(GreenSuccess)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "Status: Aktif & Terbuka",
                                    color = GreenSuccess,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Tombol kembali ke dashboard
            Button(
                onClick = onBackToDashboard,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Icon(Icons.Default.Home, contentDescription = null, tint = PrimaryBlue)
                Spacer(Modifier.width(8.dp))
                Text(
                    "Lihat di Dashboard",
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue,
                    fontSize = 15.sp
                )
            }

            Spacer(Modifier.height(12.dp))

            // Tombol posting lagi
            OutlinedButton(
                onClick = onPostAnother,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.6f))
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text(
                    "Posting Pekerjaan Lain",
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
        }
    }
}

// =============================================
// KOMPONEN REUSABLE
// =============================================
@Composable
fun SummaryRow(icon: ImageVector, label: String, value: String, iconColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 11.sp, color = TextSecondary)
            Text(value, fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun SummaryDivider() {
    Spacer(Modifier.height(8.dp))
    HorizontalDivider(color = BgLight, thickness = 1.dp)
    Spacer(Modifier.height(8.dp))
}