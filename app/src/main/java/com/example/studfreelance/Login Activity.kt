package com.example.studfreelance

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studfreelance.Client.*
import com.example.studfreelance.Freelancer.FreelancerDashboardActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : ComponentActivity() {
    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()   // ← tambah ini


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoginScreen(
                onLogin = { email, password, role ->
                    loginWithFirebase(email, password, role)
                },
                onGoToRegister = {
                    startActivity(Intent(this, RegisterActivity::class.java))
                }
            )
        }
    }

    private fun loginWithFirebase(email: String, password: String, roleYangDipilih: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener
                // Ambil role dari Firestore
                db.collection("users").document(uid).get()
                    .addOnSuccessListener { doc ->
                        val roleTersimpan = doc.getString("role") ?: ""
                        if (roleTersimpan != roleYangDipilih) {
                            // Role tidak cocok → logout paksa
                            auth.signOut()
                            currentErrorCallback?.invoke(
                                "Akun ini terdaftar sebagai $roleTersimpan, bukan $roleYangDipilih!"
                            )
                        } else {
                            val intent = when (roleTersimpan) {
                                "client" -> Intent(this, DashboardClientActivity::class.java)
                                else     -> Intent(this, FreelancerDashboardActivity::class.java)
                            }
                            startActivity(intent)
                        }
                    }
                    .addOnFailureListener { e: Exception ->
                        auth.signOut()
                        currentErrorCallback?.invoke(e.message ?: "Gagal mengambil data role")
                    }
            }
            .addOnFailureListener { e: Exception ->
                currentErrorCallback?.invoke(e.message ?: "Login gagal")
            }
    }

    var currentErrorCallback: ((String) -> Unit)? = null
}

@Composable
fun LoginScreen(
    onLogin: (email: String, password: String, role: String) -> Unit,
    onGoToRegister: () -> Unit
) {
    var email     by remember { mutableStateOf("") }
    var password  by remember { mutableStateOf("") }
    var role      by remember { mutableStateOf("freelancer") }
    var showPass  by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMsg  by remember { mutableStateOf("") }

    val activity = androidx.compose.ui.platform.LocalContext.current as LoginActivity
    activity.currentErrorCallback = { msg ->
        errorMsg  = msg
        isLoading = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(PrimaryBlue, PrimaryLight, Color(0xFFBBDEFB)))
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "StudFreelance",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                "Platform Freelance Mahasiswa",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )

            Spacer(Modifier.height(36.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {

                    Text(
                        "Masuk",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Silakan login sesuai peranmu",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )

                    Spacer(Modifier.height(20.dp))

                    // Input Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; errorMsg = "" },
                        label = { Text("Email") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, null, tint = PrimaryLight)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryLight,
                            unfocusedBorderColor = Color(0xFFDDE1F0)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(12.dp))

                    // Input Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; errorMsg = "" },
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, null, tint = PrimaryLight)
                        },
                        trailingIcon = {
                            IconButton(onClick = { showPass = !showPass }) {
                                Icon(
                                    if (showPass) Icons.Default.VisibilityOff
                                    else Icons.Default.Visibility,
                                    null, tint = TextSecondary
                                )
                            }
                        },
                        visualTransformation = if (showPass) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryLight,
                            unfocusedBorderColor = Color(0xFFDDE1F0)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Pesan Error
                    if (errorMsg.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text("❌ $errorMsg", color = Color.Red, fontSize = 12.sp)
                    }

                    Spacer(Modifier.height(20.dp))

                    // Pilih Role
                    Text(
                        "Masuk sebagai:",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        RoleButton(
                            Modifier.weight(1f), "Freelancer",
                            Icons.Default.Person, role == "freelancer"
                        ) { role = "freelancer" }
                        RoleButton(
                            Modifier.weight(1f), "Penyedia",
                            Icons.Default.Business, role == "client"
                        ) { role = "client" }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Tombol Login
                    Button(
                        onClick = {
                            when {
                                email.isBlank() || password.isBlank() -> {
                                    errorMsg = "Email dan password tidak boleh kosong!"
                                }
                                else -> {
                                    isLoading = true
                                    errorMsg  = ""
                                    onLogin(email, password, role)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text(
                                "Masuk",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Tombol ke Register
                    TextButton(
                        onClick = onGoToRegister,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Belum punya akun? Daftar",
                            color = PrimaryLight,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun RoleButton(
    modifier: Modifier = Modifier,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor    = if (isSelected) PrimaryBlue else Color(0xFFF0F4FF)
    val textColor  = if (isSelected) Color.White else TextSecondary
    val borderColor = if (isSelected) PrimaryBlue else Color(0xFFDDE1F0)

    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = bgColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Icon(icon, contentDescription = null, tint = textColor, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, color = textColor, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

