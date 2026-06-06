package com.example.studfreelance.Freelancer

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.example.studfreelance.ApplicationRepository
import com.example.studfreelance.ApplicationStatus
import com.example.studfreelance.Client.PostedJob
import com.example.studfreelance.JobApplication
import com.example.studfreelance.JobRepository
import com.example.studfreelance.LoginActivity
import com.example.studfreelance.WorkSubmissionRepository
import androidx.compose.material.icons.outlined.StarOutline

// WARNA TEMA FREELANCER
val FlPrimary    = Color(0xFF0D47A1)
val FlAccent     = Color(0xFF00897B)
val FlAccentSoft = Color(0xFFE0F2F1)
val FlBg         = Color(0xFFF4F6FB)
val FlCard       = Color.White
val FlTextMain   = Color(0xFF1A1A2E)
val FlTextSub    = Color(0xFF6B7280)
val FlGradStart  = Color(0xFF0D47A1)
val FlGradEnd    = Color(0xFF1E88E5)

// ACTIVITY
class FreelancerDashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { FreelancerDashboardApp() }
    }

    override fun onResume() {
        super.onResume()
        setContent { FreelancerDashboardApp() }
    }
}

// ROOT COMPOSABLE  (bottom nav 3 tab)
@Composable
fun FreelancerDashboardApp() {
    var selectedTab by remember { mutableStateOf(0) }

    var refreshKey by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        ApplicationRepository.addListener { refreshKey++ }
    }

    Scaffold(
        containerColor = FlBg,
        bottomBar = {
            FreelancerBottomNav(
                selected   = selectedTab,
                onSelect   = { selectedTab = it },
                notifCount = ApplicationRepository.unreadFreelancerCount()
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> FreelancerHomeTab(
                    refreshKey        = refreshKey,
                    onAvatarClick     = { selectedTab = 2 }  // ← klik avatar → tab Profil
                )
                1 -> FreelancerJobsTab(refreshKey = refreshKey)
                2 -> FreelancerProfileTab()
            }
        }
    }
}

// ──────────────────────────────────────────────
// BOTTOM NAVIGATION
// ──────────────────────────────────────────────
@Composable
fun FreelancerBottomNav(
    selected: Int,
    onSelect: (Int) -> Unit,
    notifCount: Int = 0
) {
    val items = listOf(
        Triple("Beranda",   Icons.Filled.Home,   Icons.Outlined.Home),
        Triple("Pekerjaan", Icons.Filled.Work,   Icons.Outlined.WorkOutline),
        Triple("Profil",    Icons.Filled.Person, Icons.Outlined.Person)
    )
    NavigationBar(containerColor = FlCard, tonalElevation = 8.dp) {
        items.forEachIndexed { i, (label, iconFilled, iconOutline) ->
            NavigationBarItem(
                selected = selected == i,
                onClick  = { onSelect(i) },
                icon = {
                    BadgedBox(
                        badge = {
                            if (i == 0 && notifCount > 0) {
                                Badge { Text("$notifCount") }
                            }
                        }
                    ) {
                        Icon(
                            if (selected == i) iconFilled else iconOutline,
                            contentDescription = label,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                label = {
                    Text(
                        label,
                        fontSize   = 11.sp,
                        fontWeight = if (selected == i) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = FlPrimary,
                    selectedTextColor   = FlPrimary,
                    unselectedIconColor = FlTextSub,
                    unselectedTextColor = FlTextSub,
                    indicatorColor      = Color(0xFFE3F2FD)
                )
            )
        }
    }
}

// ══════════════════════════════════════════════
// TAB 1 — BERANDA
// ══════════════════════════════════════════════
@Composable
fun FreelancerHomeTab(
    refreshKey: Int = 0,
    onAvatarClick: () -> Unit = {}

) {
    val profile    = FreelancerProfileRepository.getProfile()
    val allJobs    = remember(refreshKey) { JobRepository.getAllJobs() }
    val activeJobs = allJobs.filter { it.status == "active" || it.status == "review" }
    val doneJobs   = allJobs.filter { it.status == "done" }

    val notifications = remember(refreshKey) { ApplicationRepository.getFreelancerNotifications() }
    var showNotifPanel by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // ── HEADER GRADIENT ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(FlGradStart, FlGradEnd)))
                .padding(top = 52.dp, bottom = 32.dp, start = 20.dp, end = 20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Halo, ${profile.name}! 👋",
                            color      = Color.White,
                            fontSize   = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${profile.major} • ${profile.university}",
                            color    = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Bell notif
                        val unread = ApplicationRepository.unreadFreelancerCount()
                        BadgedBox(badge = { if (unread > 0) Badge { Text("$unread") } }) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .clickable { showNotifPanel = !showNotifPanel },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Notifications, null,
                                    tint     = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        // ── AVATAR — diklik → tab Profil ──
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.25f))
                                .clickable { onAvatarClick() },   // ← ini yang baru
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                profile.avatarInitial,
                                color      = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize   = 22.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HomeStatChip(Modifier.weight(1f), Icons.Default.WorkOutline, "${activeJobs.size}", "Job Tersedia")
                    HomeStatChip(Modifier.weight(1f), Icons.Default.CheckCircle, "${doneJobs.size}",   "Selesai")
                    HomeStatChip(Modifier.weight(1f), Icons.Default.Star,        "${profile.rating}",  "Rating")
                }
            }
        }

        AnimatedVisibility(visible = showNotifPanel) {
            NotificationPanel(
                notifications = notifications,
                onDismiss = {
                    showNotifPanel = false
                    ApplicationRepository.markFreelancerNotificationsRead()
                }
            )
        }

        val myApplications = remember(refreshKey) {
            ApplicationRepository.getApplicationsByFreelancer(profile.id)
        }
        if (myApplications.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            SectionHeader("Lamaran Saya", "${myApplications.size} lamaran terkirim", Icons.Default.Send)
            Spacer(Modifier.height(10.dp))
            myApplications.take(3).forEach { app ->
                ApplicationStatusCard(app, Modifier.padding(horizontal = 16.dp, vertical = 5.dp))
            }
        }

        Spacer(Modifier.height(20.dp))
        SectionHeader("Job Tersedia", "${activeJobs.size} pekerjaan terbuka", Icons.Default.Search)
        Spacer(Modifier.height(10.dp))
        if (activeJobs.isEmpty()) {
            EmptyState("Belum ada job tersedia saat ini")
        } else {
            activeJobs.take(3).forEach { job ->
                JobCardFreelancer(job = job, profile = profile, modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
            }
        }

        Spacer(Modifier.height(16.dp))
        SectionHeader("Riwayat Pekerjaan", "${doneJobs.size} pekerjaan selesai", Icons.Default.History)
        Spacer(Modifier.height(10.dp))
        if (doneJobs.isEmpty()) {
            EmptyState("Belum ada pekerjaan selesai")
        } else {
            doneJobs.take(3).forEach { job ->
                JobCardFreelancer(job = job, profile = profile, modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

// TAB 2 — SEMUA PEKERJAAN (tidak berubah)
@Composable
fun FreelancerJobsTab(refreshKey: Int = 0) {
    var selectedFilter by remember { mutableStateOf("Semua") }
    val filters = listOf("Semua", "Aktif", "Selesai", "Review")
    val profile = FreelancerProfileRepository.getProfile()

    val allJobs = remember(refreshKey) { JobRepository.getAllJobs() }
    val filtered = when (selectedFilter) {
        "Aktif"   -> allJobs.filter { it.status == "active" }
        "Selesai" -> allJobs.filter { it.status == "done" }
        "Review"  -> allJobs.filter { it.status == "done" }
        else      -> allJobs
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(FlGradStart, FlGradEnd)))
                .padding(top = 52.dp, start = 20.dp, end = 20.dp, bottom = 20.dp)
        ) {
            Column {
                Text("Daftar Pekerjaan", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                Text("Temukan pekerjaan yang sesuai keahlianmu", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
            }
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filters) { filter ->
                FilterChipItem(label = filter, selected = selectedFilter == filter, onClick = { selectedFilter = filter })
            }
        }

        if (filtered.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyState("Tidak ada job untuk filter ini")
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered) { job ->
                    val submission = WorkSubmissionRepository.getSubmissionByJob(job.id)
                    if (selectedFilter == "Review" && submission != null && submission.rating > 0) {
                        RatingCardFreelancer(job = job, submission = submission)
                    } else {
                        JobCardFreelancer(job = job, profile = profile)
                    }
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
fun RatingCardFreelancer(
    job: com.example.studfreelance.Client.PostedJob,
    submission: com.example.studfreelance.WorkSubmission
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FlCard),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(job.judul, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = FlTextMain)
            Spacer(Modifier.height(4.dp))
            Surface(shape = RoundedCornerShape(6.dp), color = FlAccentSoft) {
                Text(job.kategori, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), color = FlAccent, fontSize = 11.sp)
            }
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF0F2F8))
            Spacer(Modifier.height(12.dp))

            Text("Penilaian Client", fontSize = 12.sp, color = FlTextSub)
            Spacer(Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(5) { index ->
                    Icon(
                        imageVector = if (index < submission.rating) Icons.Filled.Star
                        else Icons.Outlined.StarOutline,
                        contentDescription = null,
                        tint = if (index < submission.rating) Color(0xFFFFC107) else Color(0xFFBBBBBB),
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    "${submission.rating}/5",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFFFFC107)
                )
            }

            if (submission.review.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF4F6FB)
                ) {
                    Row(modifier = Modifier.padding(12.dp)) {
                        Icon(Icons.Default.FormatQuote, null, tint = FlPrimary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(submission.review, fontSize = 13.sp, color = FlTextSub, lineHeight = 20.sp)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Text("Selesai • ${submission.submittedAt}", fontSize = 11.sp, color = FlTextSub)
        }
    }
}

// ══════════════════════════════════════════════
// TAB 3 — PROFIL FREELANCER
// ══════════════════════════════════════════════
@Composable
fun FreelancerProfileTab() {
    var editMode by remember { mutableStateOf(false) }
    val profile = FreelancerProfileRepository.getProfile()
    val context = LocalContext.current

    if (editMode) {
        FreelancerProfileEditScreen(
            profile  = profile,
            onSave   = { updated ->
                FreelancerProfileRepository.updateProfile(updated)
                editMode = false
            },
            onCancel = { editMode = false }
        )
    } else {
        FreelancerProfileViewScreen(
            profile   = profile,
            onEdit    = { editMode = true },
            onLogout  = {
                // Balik ke LoginActivity + clear back stack
                val intent = Intent(context, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                context.startActivity(intent)
            }
        )
    }
}

// ══════════════════════════════════════════════
// JOB CARD FREELANCER — tidak berubah
// ══════════════════════════════════════════════
@Composable
fun JobCardFreelancer(
    job: PostedJob,
    profile: FreelancerProfile,
    modifier: Modifier = Modifier,
    onRefresh: () -> Unit = {},
    refreshKey: Int = 0
) {
    var applicationStatus by remember(job.id, profile.id) {
        mutableStateOf(ApplicationRepository.getApplicationStatus(job.id, profile.id))
    }

    var showApplyDialog   by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showAlreadyDialog by remember { mutableStateOf(false) }

    val statusColor = when (job.status) {
        "active" -> Color(0xFF2E7D32)
        "review" -> Color(0xFFE65100)
        "done"   -> Color(0xFF546E7A)
        else     -> FlTextSub
    }
    val statusLabel = when (job.status) {
        "active" -> "Tersedia"
        "review" -> "Sedang Ditinjau"
        "done"   -> "Selesai"
        else     -> job.status
    }
    val statusBg = when (job.status) {
        "active" -> Color(0xFFE8F5E9)
        "review" -> Color(0xFFFFF3E0)
        "done"   -> Color(0xFFECEFF1)
        else     -> Color(0xFFF5F5F5)
    }

    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = FlCard),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(job.judul, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = FlTextMain, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(4.dp))
                    Surface(shape = RoundedCornerShape(6.dp), color = FlAccentSoft) {
                        Text(job.kategori, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), color = FlAccent, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }
                Spacer(Modifier.width(8.dp))
                Surface(shape = RoundedCornerShape(20.dp), color = statusBg) {
                    Text(statusLabel, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF0F2F8), thickness = 1.dp)
            Spacer(Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                JobInfoChip(Icons.Default.Payments, job.budget)
                JobInfoChip(Icons.Default.Schedule, job.deadline)
                JobInfoChip(Icons.Default.Group, "${job.jumlahPelamar} pelamar")
            }

            if (job.status == "active") {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFF0F2F8), thickness = 1.dp)
                Spacer(Modifier.height(12.dp))

                when (applicationStatus) {
                    null -> {
                        Button(
                            onClick  = { showApplyDialog = true },
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape    = RoundedCornerShape(12.dp),
                            colors   = ButtonDefaults.buttonColors(containerColor = FlPrimary)
                        ) {
                            Icon(Icons.Default.Send, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Lamar Pekerjaan", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                    ApplicationStatus.PENDING -> {
                        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = Color(0xFFFFF8E1)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                Icon(Icons.Default.HourglassEmpty, null, tint = Color(0xFFE65100), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Lamaran Terkirim • Menunggu Konfirmasi", color = Color(0xFFE65100), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    ApplicationStatus.ACCEPTED -> {
                        val context = LocalContext.current
                        val submission = remember(refreshKey) { WorkSubmissionRepository.getSubmissionByJob(job.id) }
                        Column {
                            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = Color(0xFFE8F5E9)) {
                                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                    Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("🎉 Lamaran Diterima! Segera mulai bekerja", color = Color(0xFF2E7D32), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(Modifier.height(8.dp))

                            if (submission != null) {
                                // Sudah upload → tampilkan status
                                Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = Color(0xFFFFF8E1)) {
                                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                        Icon(Icons.Default.HourglassEmpty, null, tint = Color(0xFFE65100), modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Hasil kerja sedang ditinjau client...", color = Color(0xFFE65100), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            } else {
                                // Belum upload → tampilkan tombol upload
                                Button(
                                    onClick = {
                                        val intent = android.content.Intent(context, UploadResultActivity::class.java).apply {
                                            putExtra("JOB_ID", job.id)
                                            putExtra("JOB_TITLE", job.judul)
                                            putExtra("FREELANCER_ID", profile.id)
                                            putExtra("FREELANCER_NAME", profile.name)
                                        }
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.fillMaxWidth().height(44.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B))
                                ) {
                                    Icon(Icons.Default.UploadFile, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Upload Hasil Kerja", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                    ApplicationStatus.REJECTED -> {
                        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = Color(0xFFFFEBEE)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                Icon(Icons.Default.Cancel, null, tint = Color(0xFFC62828), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Lamaran Tidak Diterima", color = Color(0xFFC62828), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showApplyDialog) {
        AlertDialog(
            onDismissRequest = { showApplyDialog = false },
            icon = {
                Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(Color(0xFFE3F2FD)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Send, null, tint = FlPrimary, modifier = Modifier.size(28.dp))
                }
            },
            title = { Text("Lamar Pekerjaan?", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = FlTextMain) },
            text = {
                Column {
                    Text("Kamu akan melamar untuk:", fontSize = 13.sp, color = FlTextSub)
                    Spacer(Modifier.height(8.dp))
                    Surface(shape = RoundedCornerShape(12.dp), color = FlBg, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(job.judul, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = FlTextMain)
                            Spacer(Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Payments, null, tint = FlPrimary, modifier = Modifier.size(13.dp))
                                    Spacer(Modifier.width(3.dp))
                                    Text(job.budget, fontSize = 12.sp, color = FlTextSub)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Schedule, null, tint = FlPrimary, modifier = Modifier.size(13.dp))
                                    Spacer(Modifier.width(3.dp))
                                    Text(job.deadline, fontSize = 12.sp, color = FlTextSub)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Profil kamu (${profile.name}) akan dikirim ke penyedia pekerjaan.", fontSize = 13.sp, color = FlTextSub, lineHeight = 18.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showApplyDialog = false
                        val application = JobApplication(
                            id                      = "app_${job.id}_${profile.id}_${System.currentTimeMillis()}",
                            jobId                   = job.id,
                            jobTitle                = job.judul,
                            freelancerId            = profile.id,
                            freelancerName          = profile.name,
                            freelancerMajor         = profile.major,
                            freelancerUniversity    = profile.university,
                            freelancerRating        = profile.rating,
                            freelancerSkills        = profile.skills,
                            freelancerBio           = profile.bio,
                            freelancerAvatarInitial = profile.avatarInitial,
                            freelancerAvatarColor   = profile.avatarColor,
                            appliedAt               = getCurrentTimeStr()
                        )
                        val success = ApplicationRepository.applyJob(application)
                        if (success) {
                            applicationStatus = ApplicationStatus.PENDING
                            showSuccessDialog  = true
                        } else {
                            showAlreadyDialog = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FlPrimary),
                    shape  = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Send, null, tint = Color.White, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Kirim Lamaran", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showApplyDialog = false }) {
                    Text("Batal", color = FlTextSub)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            icon = {
                Box(modifier = Modifier.size(64.dp).clip(CircleShape).background(Color(0xFFE8F5E9)), contentAlignment = Alignment.Center) {
                    Text("🎉", fontSize = 28.sp)
                }
            },
            title = { Text("Lamaran Terkirim!", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF2E7D32)) },
            text = { Text("Lamaranmu untuk \"${job.judul}\" sudah terkirim ke penyedia. Pantau statusnya di halaman Beranda ya!", fontSize = 14.sp, color = FlTextSub, lineHeight = 20.sp) },
            confirmButton = {
                Button(onClick = { showSuccessDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)), shape = RoundedCornerShape(12.dp)) {
                    Text("Oke, Siap!", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (showAlreadyDialog) {
        AlertDialog(
            onDismissRequest = { showAlreadyDialog = false },
            icon = { Icon(Icons.Default.Info, null, tint = Color(0xFFE65100), modifier = Modifier.size(36.dp)) },
            title = { Text("Sudah Melamar", fontWeight = FontWeight.Bold) },
            text = { Text("Kamu sudah pernah melamar untuk pekerjaan \"${job.judul}\" ini.", fontSize = 14.sp, color = FlTextSub) },
            confirmButton = {
                TextButton(onClick = { showAlreadyDialog = false }) {
                    Text("Mengerti", color = FlPrimary, fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

// ══════════════════════════════════════════════
// CARD STATUS LAMARAN
// ══════════════════════════════════════════════
@Composable
fun ApplicationStatusCard(
    application: com.example.studfreelance.JobApplication,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, icon, statusText) = when (application.status) {
        ApplicationStatus.PENDING  -> Quadruple(Color(0xFFFFF8E1), Color(0xFFE65100), Icons.Default.HourglassEmpty, "Menunggu")
        ApplicationStatus.ACCEPTED -> Quadruple(Color(0xFFE8F5E9), Color(0xFF2E7D32), Icons.Default.CheckCircle,   "Diterima ✓")
        ApplicationStatus.REJECTED -> Quadruple(Color(0xFFFFEBEE), Color(0xFFC62828), Icons.Default.Cancel,        "Tidak Diterima")
    }

    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = textColor, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(application.jobTitle, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = FlTextMain, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (application.appliedAt.isNotBlank()) {
                    Text("Dikirim ${application.appliedAt}", fontSize = 11.sp, color = FlTextSub)
                }
            }
            Surface(shape = RoundedCornerShape(20.dp), color = textColor.copy(alpha = 0.1f)) {
                Text(statusText, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), color = textColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ══════════════════════════════════════════════
// PANEL NOTIFIKASI
// ══════════════════════════════════════════════
@Composable
fun NotificationPanel(notifications: List<com.example.studfreelance.AppNotification>, onDismiss: () -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = FlCard),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Notifikasi", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = FlTextMain)
                TextButton(onClick = onDismiss) { Text("Tutup", color = FlPrimary, fontSize = 12.sp) }
            }
            Spacer(Modifier.height(8.dp))
            if (notifications.isEmpty()) {
                Text("Belum ada notifikasi", color = FlTextSub, fontSize = 13.sp, modifier = Modifier.padding(vertical = 8.dp))
            } else {
                notifications.take(5).forEach { notif ->
                    NotificationItem(notif)
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notif: com.example.studfreelance.AppNotification) {
    val (bg, icon) = when (notif.type) {
        com.example.studfreelance.NotificationType.APPLICATION_SENT     -> Pair(Color(0xFFE3F2FD), Icons.Default.Send)
        com.example.studfreelance.NotificationType.APPLICATION_ACCEPTED -> Pair(Color(0xFFE8F5E9), Icons.Default.CheckCircle)
        com.example.studfreelance.NotificationType.APPLICATION_REJECTED -> Pair(Color(0xFFFFEBEE), Icons.Default.Cancel)
        com.example.studfreelance.NotificationType.NEW_APPLICANT        -> Pair(Color(0xFFFFF8E1), Icons.Default.PersonAdd)
        com.example.studfreelance.NotificationType.WORK_SUBMITTED       -> Pair(Color(0xFFE8F5E9), Icons.Default.UploadFile)
        com.example.studfreelance.NotificationType.WORK_APPROVED        -> Pair(Color(0xFFE8F5E9), Icons.Default.CheckCircle)
        com.example.studfreelance.NotificationType.WORK_REVISION        -> Pair(Color(0xFFFFF8E1), Icons.Default.Refresh)
        com.example.studfreelance.NotificationType.PAYMENT_RECEIVED     -> Pair(Color(0xFFE8F5E9), Icons.Default.Payment)
        com.example.studfreelance.NotificationType.RATING_GIVEN -> Pair(Color(0xFFFFF8E1), Icons.Default.Star)

    }
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(bg), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = FlPrimary, modifier = Modifier.size(17.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(notif.title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = FlTextMain)
            Text(notif.message, fontSize = 12.sp, color = FlTextSub, lineHeight = 17.sp)
            if (notif.timestamp.isNotBlank()) {
                Text(notif.timestamp, fontSize = 10.sp, color = FlTextSub.copy(alpha = 0.7f))
            }
        }
    }
}

// ══════════════════════════════════════════════
// HELPER & KOMPONEN REUSABLE
// ══════════════════════════════════════════════
data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

private fun getCurrentTimeStr(): String {
    val sdf = java.text.SimpleDateFormat("HH:mm, dd MMM", java.util.Locale("id"))
    return sdf.format(java.util.Date())
}

@Composable fun HomeStatChip(modifier: Modifier = Modifier, icon: ImageVector, value: String, label: String) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = Color.White.copy(alpha = 0.18f)) {
        Column(modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(4.dp))
            Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable fun SectionHeader(title: String, subtitle: String, icon: ImageVector) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFFE3F2FD)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = FlPrimary, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = FlTextMain)
            Text(subtitle, color = FlTextSub, fontSize = 12.sp)
        }
    }
}

@Composable fun JobInfoChip(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = FlPrimary, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text(text, fontSize = 12.sp, color = FlTextSub)
    }
}

@Composable fun FilterChipItem(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape    = RoundedCornerShape(20.dp),
        color    = if (selected) FlPrimary else FlCard,
        border   = if (!selected) BorderStroke(1.dp, Color(0xFFDDE1F0)) else null,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(label, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = if (selected) Color.White else FlTextSub, fontSize = 13.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
    }
}

@Composable fun EmptyState(message: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Inbox, null, tint = Color(0xFFBBBBBB), modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(8.dp))
            Text(message, color = FlTextSub, fontSize = 14.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable fun ProfileStatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(label, color = Color.White.copy(alpha = 0.75f), fontSize = 11.sp)
    }
}

@Composable fun ProfileSection(title: String, icon: ImageVector, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = FlCard), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = FlPrimary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = FlTextMain)
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable fun ProfileInfoRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = FlPrimary, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = 14.sp, color = FlTextSub)
    }
}

@Composable fun SkillChip(skill: String) {
    Surface(shape = RoundedCornerShape(20.dp), color = FlAccentSoft) {
        Text(skill, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = FlAccent, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable fun PortfolioCard(item: FreelancerPortfolioItem) {
    Surface(shape = RoundedCornerShape(12.dp), color = FlBg, border = BorderStroke(1.dp, Color(0xFFE0E7FF)), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(item.title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = FlTextMain)
                Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFE3F2FD)) {
                    Text(item.category, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), color = FlPrimary, fontSize = 10.sp)
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(item.description, color = FlTextSub, fontSize = 12.sp, lineHeight = 18.sp)
        }
    }
}

@Composable fun CertificateItem(cert: FreelancerCertificate) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFFFFF8E1)), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.EmojiEvents, null, tint = Color(0xFFF9A825), modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(cert.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = FlTextMain, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${cert.issuer} • ${cert.year}", color = FlTextSub, fontSize = 12.sp)
        }
    }
}

@Composable fun EditFieldLabel(icon: ImageVector, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
        Icon(icon, null, tint = FlPrimary, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(5.dp))
        Text(label, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = FlTextMain)
    }
}

@Composable fun FlOutlinedField(value: String, onValueChange: (String) -> Unit, placeholder: String, singleLine: Boolean = true, minLines: Int = 1) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = FlTextSub, fontSize = 14.sp) },
        singleLine = singleLine, minLines = minLines,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FlPrimary, unfocusedBorderColor = Color(0xFFDDE1F0), focusedContainerColor = FlCard, unfocusedContainerColor = FlBg),
        modifier = Modifier.fillMaxWidth()
    )
}

// ══════════════════════════════════════════════
// PROFIL SCREEN VIEW — ditambah tombol Logout
// ══════════════════════════════════════════════
@Composable
fun FreelancerProfileViewScreen(
    profile: FreelancerProfile,
    onEdit: () -> Unit,
    onLogout: () -> Unit       // ← parameter baru
) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(Brush.verticalGradient(listOf(FlGradStart, FlGradEnd)))
                .padding(top = 52.dp, bottom = 28.dp, start = 20.dp, end = 20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier.size(80.dp).clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f))
                        .border(3.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(profile.avatarInitial, color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(12.dp))
                Text(profile.name, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("${profile.major} • Sem ${profile.semester}", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                Text(profile.university, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    ProfileStatItem("${profile.rating} ⭐", "Rating")
                    ProfileStatItem("${profile.completedJobs}", "Job Selesai")
                    ProfileStatItem("${profile.skills.size}", "Keahlian")
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Tombol Edit Profil ──
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            OutlinedButton(
                onClick  = onEdit,
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(12.dp),
                border   = BorderStroke(1.5.dp, FlPrimary)
            ) {
                Icon(Icons.Default.Edit, null, tint = FlPrimary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Edit Profil", color = FlPrimary, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.height(8.dp))

        // ── Tombol Logout ──
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            OutlinedButton(
                onClick  = onLogout,
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(12.dp),
                border   = BorderStroke(1.5.dp, Color(0xFFC62828))
            ) {
                Icon(Icons.Default.Logout, null, tint = Color(0xFFC62828), modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Keluar / Logout", color = Color(0xFFC62828), fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.height(16.dp))
        ProfileSection("Bio / Tentang Saya", Icons.Default.Person) {
            Text(if (profile.bio.isNotBlank()) profile.bio else "Belum ada bio.", color = FlTextSub, fontSize = 14.sp, lineHeight = 22.sp)
        }
        Spacer(Modifier.height(12.dp))
        ProfileSection("Informasi Kontak", Icons.Default.ContactMail) {
            ProfileInfoRow(Icons.Default.Email, profile.email.ifBlank { "-" })
            Spacer(Modifier.height(4.dp))
            ProfileInfoRow(Icons.Default.Phone, profile.phone.ifBlank { "-" })
        }
        Spacer(Modifier.height(12.dp))
        ProfileSection("Keahlian", Icons.Default.StarHalf) {
            if (profile.skills.isEmpty()) Text("Belum ada keahlian.", color = FlTextSub, fontSize = 14.sp)
            else Column { profile.skills.chunked(3).forEach { row -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { row.forEach { SkillChip(it) } }; Spacer(Modifier.height(8.dp)) } }
        }
        Spacer(Modifier.height(12.dp))
        ProfileSection("Portofolio", Icons.Default.Folder) {
            if (profile.portfolioItems.isEmpty()) Text("Belum ada portofolio.", color = FlTextSub, fontSize = 14.sp)
            else profile.portfolioItems.forEach { PortfolioCard(it); Spacer(Modifier.height(8.dp)) }
        }
        Spacer(Modifier.height(12.dp))
        ProfileSection("Sertifikat & Penghargaan", Icons.Default.CardMembership) {
            if (profile.certificates.isEmpty()) Text("Belum ada sertifikat.", color = FlTextSub, fontSize = 14.sp)
            else profile.certificates.forEach { CertificateItem(it); Spacer(Modifier.height(6.dp)) }
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
fun FreelancerProfileEditScreen(profile: FreelancerProfile, onSave: (FreelancerProfile) -> Unit, onCancel: () -> Unit) {
    var name       by remember { mutableStateOf(profile.name) }
    var email      by remember { mutableStateOf(profile.email) }
    var phone      by remember { mutableStateOf(profile.phone) }
    var university by remember { mutableStateOf(profile.university) }
    var major      by remember { mutableStateOf(profile.major) }
    var semester   by remember { mutableStateOf(profile.semester) }
    var bio        by remember { mutableStateOf(profile.bio) }
    var skillsText by remember { mutableStateOf(profile.skills.joinToString(", ")) }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(Brush.verticalGradient(listOf(FlGradStart, FlGradEnd)))
                .padding(top = 52.dp, bottom = 20.dp, start = 20.dp, end = 20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(Color.White.copy(alpha = 0.2f)).clickable { onCancel() }, contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.ArrowBack, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Edit Profil", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Perbarui biodata & portofoliomu", color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp)
                }
            }
        }
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = FlCard), elevation = CardDefaults.cardElevation(4.dp)) {
            Column(modifier = Modifier.padding(20.dp)) {
                EditFieldLabel(Icons.Default.Person, "Nama Lengkap"); FlOutlinedField(name, { name = it }, "Nama kamu")
                Spacer(Modifier.height(14.dp))
                EditFieldLabel(Icons.Default.Email, "Email"); FlOutlinedField(email, { email = it }, "email@student.ac.id")
                Spacer(Modifier.height(14.dp))
                EditFieldLabel(Icons.Default.Phone, "No. HP / WhatsApp"); FlOutlinedField(phone, { phone = it }, "08xxxxxxxxx")
                Spacer(Modifier.height(14.dp))
                EditFieldLabel(Icons.Default.School, "Universitas"); FlOutlinedField(university, { university = it }, "Nama universitas")
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(Modifier.weight(2f)) { EditFieldLabel(Icons.Default.MenuBook, "Jurusan / Prodi"); FlOutlinedField(major, { major = it }, "Teknik Informatika") }
                    Column(Modifier.weight(1f)) { EditFieldLabel(Icons.Default.Tag, "Semester"); FlOutlinedField(semester, { semester = it }, "6") }
                }
                Spacer(Modifier.height(14.dp))
                EditFieldLabel(Icons.Default.Notes, "Bio / Deskripsi Diri"); FlOutlinedField(bio, { bio = it }, "Ceritakan tentang dirimu...", singleLine = false, minLines = 4)
                Spacer(Modifier.height(14.dp))
                EditFieldLabel(Icons.Default.StarHalf, "Keahlian (pisahkan dengan koma)"); FlOutlinedField(skillsText, { skillsText = it }, "Figma, Flutter, Kotlin")
                Text("Contoh: UI/UX Design, Figma, Flutter", fontSize = 11.sp, color = FlTextSub, modifier = Modifier.padding(start = 4.dp, top = 2.dp))
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = {
                        val updatedSkills = skillsText.split(",").map { it.trim() }.filter { it.isNotBlank() }
                        onSave(profile.copy(name = name, email = email, phone = phone, university = university, major = major, semester = semester, bio = bio, skills = updatedSkills, avatarInitial = if (name.isNotBlank()) name.first().uppercaseChar().toString() else "?"))
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = FlPrimary)
                ) {
                    Icon(Icons.Default.Save, null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Simpan Perubahan", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}