package com.example.studfreelance.Freelancer

// =============================================
// MODEL DATA PROFIL FREELANCER
// =============================================

data class FreelancerProfile(
    val id: String = "fl_001",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val university: String = "",
    val major: String = "",
    val semester: String = "",
    val bio: String = "",
    val skills: List<String> = emptyList(),
    val portfolioItems: List<FreelancerPortfolioItem> = emptyList(),
    val certificates: List<FreelancerCertificate> = emptyList(),
    val rating: Float = 0f,
    val completedJobs: Int = 0,
    val avatarInitial: String = "",
    val avatarColor: Long = 0xFF1565C0
)

data class FreelancerPortfolioItem(
    val title: String,
    val description: String,
    val category: String
)

data class FreelancerCertificate(
    val name: String,
    val issuer: String,
    val year: String
)

// =============================================
// REPOSITORY PROFIL FREELANCER (singleton)
// Ini yang dipakai untuk simpan & baca profil freelancer
// Sesuai dengan struktur Applicant di sisi Client/Admin
// =============================================
object FreelancerProfileRepository {

    // Profil default (bisa diedit user)
    private var _profile = FreelancerProfile(
        id = "fl_001",
        name = "Qisthi",
        email = "qisthi@student.unila.ac.id",
        phone = "081234567890",
        university = "Universitas Lampung",
        major = "Manajemen Informatika",
        semester = "6",
        bio = "Mahasiswa Manajemen Informatika semester 6 yang berpengalaman dalam pengembangan aplikasi mobile dan desain UI/UX. Telah menyelesaikan lebih dari 10 proyek freelance.",
        skills = listOf("UI/UX Design", "Figma", "Flutter", "Kotlin"),
        portfolioItems = listOf(
            FreelancerPortfolioItem("Aplikasi E-Commerce", "Desain UI lengkap untuk toko online fashion", "Mobile App"),
            FreelancerPortfolioItem("Dashboard Admin", "Web dashboard untuk manajemen inventori", "Web Design"),
            FreelancerPortfolioItem("Logo & Branding", "Identitas visual untuk startup kuliner", "Graphic Design")
        ),
        certificates = listOf(
            FreelancerCertificate("Google UX Design Certificate", "Google", "2023"),
            FreelancerCertificate("Flutter Development Bootcamp", "Udemy", "2023"),
            FreelancerCertificate("Juara 2 Hackathon UNILA", "Universitas Lampung", "2022")
        ),
        rating = 4.5f,
        completedJobs = 12,
        avatarInitial = "Q",
        avatarColor = 0xFF1565C0
    )

    fun getProfile(): FreelancerProfile = _profile

    fun updateProfile(updated: FreelancerProfile) {
        _profile = updated
    }
}