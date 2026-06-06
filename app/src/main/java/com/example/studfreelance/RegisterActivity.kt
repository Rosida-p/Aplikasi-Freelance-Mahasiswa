package com.example.studfreelance

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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : ComponentActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    var currentSuccessCallback: (() -> Unit)? = null
    var currentErrorCallback: ((String) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RegisterScreen(
                onRegister = { email, password, role ->
                    registerWithFirebase(email, password, role)
                },
                onBackToLogin = { finish() }
            )
        }
    }

    private fun registerWithFirebase(email: String, password: String, role: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener
                // Simpan role ke Firestore
                val userData = mapOf(
                    "email" to email,
                    "role"  to role
                )
                db.collection("users").document(uid)
                    .set(userData)
                    .addOnSuccessListener {
                        currentSuccessCallback?.invoke()
                    }
                    .addOnFailureListener { e: Exception ->
                        currentErrorCallback?.invoke(e.message ?: "Gagal menyimpan data")
                    }
            }
            .addOnFailureListener { e: Exception ->
                currentErrorCallback?.invoke(e.message ?: "Registrasi gagal")
            }
    }
}

@Composable
fun RegisterScreen(
    onRegister: (email: String, password: String, role: String) -> Unit,
    onBackToLogin: () -> Unit
) {
    var email      by remember { mutableStateOf("") }
    var password   by remember { mutableStateOf("") }
    var konfirmasi by remember { mutableStateOf("") }
    var role       by remember { mutableStateOf("freelancer") }
    var showPass   by remember { mutableStateOf(false) }
    var isLoading  by remember { mutableStateOf(false) }
    var errorMsg   by remember { mutableStateOf("") }
    var successMsg by remember { mutableStateOf("") }

    val activity = androidx.compose.ui.platform.LocalContext.current as RegisterActivity

    activity.currentSuccessCallback = {
        isLoading  = false
        successMsg = "✅ Akun berhasil dibuat! Silakan login."
        errorMsg   = ""
    }
    activity.currentErrorCallback = { msg ->
        isLoading = false
        errorMsg  = msg
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(PrimaryBlue, PrimaryLight, Color(0xFFBBDEFB)))
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("StudFreelance", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
            Text("Daftar Akun Baru", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
            Spacer(Modifier.height(36.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Daftar", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextPrimary)
                    Spacer(Modifier.height(4.dp))
                    Text("Buat akun untuk mulai menggunakan StudFreelance", fontSize = 13.sp, color = TextSecondary)
                    Spacer(Modifier.height(20.dp))

                    // Pilih Role DULU sebelum isi email
                    Text("Daftar sebagai:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        RoleButton(Modifier.weight(1f), "Freelancer", Icons.Default.Person, role == "freelancer") {
                            role = "freelancer"; email = ""; errorMsg = ""
                        }
                        RoleButton(Modifier.weight(1f), "Penyedia", Icons.Default.Business, role == "client") {
                            role = "client"; email = ""; errorMsg = ""
                        }
                    }

                    // Info hint sesuai role
                    Spacer(Modifier.height(8.dp))
                    if (role == "freelancer") {
                        Text(
                            "ℹ️ Freelancer wajib menggunakan email kampus (.ac.id)",
                            fontSize = 11.sp,
                            color = PrimaryLight
                        )
                    } else {
                        Text(
                            "ℹ️ Penyedia bisa menggunakan email apapun",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Input Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; errorMsg = "" },
                        label = { Text(if (role == "freelancer") "Email Kampus (.ac.id)" else "Email") },
                        leadingIcon = { Icon(Icons.Default.Email, null, tint = PrimaryLight) },
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
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = PrimaryLight) },
                        trailingIcon = {
                            IconButton(onClick = { showPass = !showPass }) {
                                Icon(
                                    if (showPass) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    null, tint = TextSecondary
                                )
                            }
                        },
                        visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryLight,
                            unfocusedBorderColor = Color(0xFFDDE1F0)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(12.dp))

                    // Input Konfirmasi Password
                    OutlinedTextField(
                        value = konfirmasi,
                        onValueChange = { konfirmasi = it; errorMsg = "" },
                        label = { Text("Konfirmasi Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = PrimaryLight) },
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryLight,
                            unfocusedBorderColor = Color(0xFFDDE1F0)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (errorMsg.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text("❌ $errorMsg", color = Color.Red, fontSize = 12.sp)
                    }
                    if (successMsg.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text(successMsg, color = GreenSuccess, fontSize = 12.sp)
                    }

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = {
                            when {
                                email.isBlank() || password.isBlank() -> {
                                    errorMsg = "Email dan password tidak boleh kosong!"
                                }
                                role == "freelancer" && !email.endsWith(".ac.id") -> {
                                    errorMsg = "Freelancer wajib menggunakan email kampus (.ac.id)!"
                                }
                                password != konfirmasi -> {
                                    errorMsg = "Password dan konfirmasi tidak sama!"
                                }
                                password.length < 6 -> {
                                    errorMsg = "Password minimal 6 karakter!"
                                }
                                else -> {
                                    isLoading = true
                                    errorMsg  = ""
                                    onRegister(email, password, role)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        } else {
                            Text("Daftar", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    TextButton(onClick = onBackToLogin, modifier = Modifier.fillMaxWidth()) {
                        Text("Sudah punya akun? Masuk", color = PrimaryLight, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}